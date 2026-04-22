const BASE_URL = 'https://health.cupk.space/api';
// const BASE_URL = 'http://localhost:8080/api';


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
      header: {
        'content-type': contentType
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else {
          reject(new Error('请求失败'));
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

  try {
    task = wx.request({
      url: requestUrl,
      method: 'POST',
      data: data,
      header: {
        'content-type': 'application/json'
      },
      responseType: 'text',
      enableChunked: true,
      success: (res) => {
        if (callbacks.onComplete) {
          callbacks.onComplete();
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
          const text = typeof chunk === 'string' ? chunk : String.fromCharCode.apply(null, new Uint8Array(chunk));
          if (callbacks.onMessage) {
            const lines = text.split('\n');
            lines.forEach(line => {
              if (line.startsWith('data:')) {
                const content = line.substring(5).trim();
                if (content) {
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

function uploadFile(url, filePath, formData) {
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: BASE_URL + url,
      filePath: filePath,
      name: 'file',
      formData: formData,
      success: (res) => {
        if (res.statusCode === 200) {
          try {
            const data = JSON.parse(res.data);
            resolve(data);
          } catch (e) {
            resolve(res.data);
          }
        } else {
          reject(new Error('上传失败'));
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

module.exports = {
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
