const BASE_URL = 'https://health.cupk.space/api';
const ASSET_BASE_URL = 'https://health.cupk.space';
// const BASE_URL = 'http://localhost:8080/api';

function getToken() {
  const userInfo = wx.getStorageSync('userInfo');
  return userInfo && userInfo.token ? userInfo.token : '';
}

function buildHeaders(contentType) {
  const headers = {
    'content-type': contentType || 'application/json'
  };
  const token = getToken();
  if (token) {
    headers.Authorization = 'Bearer ' + token;
  }
  return headers;
}

function buildAuthHeaders() {
  const headers = {};
  const token = getToken();
  if (token) {
    headers.Authorization = 'Bearer ' + token;
  }
  return headers;
}

function handleUnauthorized() {
  wx.removeStorageSync('userInfo');
  wx.removeStorageSync('healthInfo');
  const app = getApp();
  if (app && app.globalData) {
    app.globalData.userInfo = null;
    app.globalData.healthInfo = null;
    app.globalData.isLoggedIn = false;
    app.globalData.chatMessages = null;
  }
}

function buildQueryParams(data) {
  if (!data || Object.keys(data).length === 0) {
    return '';
  }
  const params = Object.keys(data)
    .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`)
    .join('&');
  return '?' + params;
}

function request(url, method, data) {
  return new Promise((resolve, reject) => {
    let requestUrl = BASE_URL + url;
    let requestData = data;
    let contentType = 'application/json';

    if (method === 'GET' || method === 'DELETE') {
      requestUrl += buildQueryParams(data);
      requestData = undefined;
      if (method === 'DELETE') {
        contentType = 'application/x-www-form-urlencoded';
      }
    }

    wx.request({
      url: requestUrl,
      method: method || 'GET',
      data: requestData,
      header: buildHeaders(contentType),
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          handleUnauthorized();
          reject(new Error((res.data && res.data.message) || '请先登录'));
        } else {
          reject(new Error((res.data && res.data.message) || '请求失败'));
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

function streamRequest(url, data, callbacks) {
  const requestUrl = BASE_URL + url;
  let task = null;
  let buffer = '';
  let completed = false;

  try {
    task = wx.request({
      url: requestUrl,
      method: 'POST',
      data: data,
      header: buildHeaders('application/json'),
      responseType: 'text',
      enableChunked: true,
      success: (res) => {
        if (!completed && res.statusCode && res.statusCode !== 200 && callbacks.onError) {
          callbacks.onError(new Error((res.data && res.data.message) || `请求失败(${res.statusCode})`));
          completed = true;
          return;
        }
        if (!completed && callbacks.onComplete) {
          callbacks.onComplete();
          completed = true;
        }
      },
      fail: (err) => {
        if (callbacks.onError) {
          callbacks.onError(err);
        }
      }
    });

    if (task.onHeadersReceived) {
      task.onHeadersReceived(() => {});
    }

    if (task.onChunkReceived) {
      task.onChunkReceived((res) => {
        const chunk = res.data;
        if (chunk) {
          const text = typeof chunk === 'string' ? chunk : arrayBufferToString(chunk);
          if (callbacks.onMessage) {
            buffer += text;
            const lines = buffer.split('\n');
            buffer = lines.pop() || '';
            lines.forEach(line => {
              if (line.startsWith('data:')) {
                const content = line.substring(5).trim();
                if (content) {
                  if (content === '[DONE]') {
                    if (!completed && callbacks.onComplete) {
                      callbacks.onComplete();
                      completed = true;
                    }
                    return;
                  }
                  callbacks.onMessage(content);
                }
              }
            });
          }
        }
      });
    }
  } catch (error) {
    if (callbacks.onError) {
      callbacks.onError(error);
    }
  }

  return {
    abort: () => {
      if (task && task.abort) {
        task.abort();
      }
    }
  };
}

function arrayBufferToString(buffer) {
  if (typeof TextDecoder !== 'undefined') {
    return new TextDecoder('utf-8').decode(new Uint8Array(buffer));
  }
  return decodeURIComponent(escape(String.fromCharCode.apply(null, new Uint8Array(buffer))));
}

function uploadFile(url, filePath, formData) {
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + url,
      filePath: filePath,
      name: 'file',
      formData: formData,
      header: buildAuthHeaders(),
      success: (res) => {
        let responseData = null;
        if (res.data) {
          try {
            responseData = JSON.parse(res.data);
          } catch (e) {
            responseData = null;
          }
        }
        if (res.statusCode === 200) {
          resolve(responseData || res.data);
        } else if (res.statusCode === 401) {
          handleUnauthorized();
          reject(new Error('请先登录'));
        } else {
          reject(new Error((responseData && responseData.message) || `上传失败(${res.statusCode})`));
        }
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '上传失败，请检查网络'));
      }
    });
  });
}

function formatImageUrl(url) {
  if (!url) {
    return '';
  }
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:image')) {
    return url;
  }
  if (url.startsWith('/')) {
    return ASSET_BASE_URL + url;
  }
  return ASSET_BASE_URL + '/' + url;
}

function formatRecipeImage(recipe) {
  if (!recipe) {
    return recipe;
  }
  return Object.assign({}, recipe, {
    image: formatImageUrl(recipe.image)
  });
}

function formatRecipeImages(recipes) {
  return (recipes || []).map(formatRecipeImage);
}

module.exports = {
  BASE_URL,
  ASSET_BASE_URL,
  formatImageUrl,
  formatRecipeImage,
  formatRecipeImages,
  get(url, data) {
    return request(url, 'GET', data);
  },
  post(url, data) {
    return request(url, 'POST', data);
  },
  put(url, data) {
    return request(url, 'PUT', data);
  },
  delete(url, data) {
    return request(url, 'DELETE', data);
  },
  streamRequest,
  uploadFile
};
