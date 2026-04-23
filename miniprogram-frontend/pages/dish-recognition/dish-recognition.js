const api = require('../../utils/api.js');

Page({
  data: {
    imagePath: '',
    result: null,
    bestProbabilityText: '',
    loading: false,
    errorText: ''
  },

  chooseFromCamera() {
    this.chooseImage(['camera']);
  },

  chooseFromAlbum() {
    this.chooseImage(['album']);
  },

  chooseImage(sourceType) {
    if (this.data.loading) return;
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType,
      success: (res) => {
        const filePath = res.tempFilePaths[0];
        this.setData({ imagePath: filePath, result: null, errorText: '' });
        this.prepareAndRecognize(filePath);
      },
      fail: (err) => {
        if (err.errMsg && err.errMsg.indexOf('cancel') === -1) {
          this.showError(err.errMsg);
        }
      }
    });
  },

  prepareAndRecognize(filePath) {
    if (!wx.compressImage) {
      this.recognize(filePath);
      return;
    }

    wx.compressImage({
      src: filePath,
      quality: 70,
      success: (res) => {
        this.recognize(res.tempFilePath || filePath);
      },
      fail: () => {
        this.recognize(filePath);
      }
    });
  },

  async recognize(filePath) {
    this.setData({ loading: true, errorText: '' });
    wx.showLoading({ title: '识别中...' });

    try {
      const res = await api.uploadFile('/nutrition/recognize', filePath);
      if (res.code === 200) {
        const result = this.formatResult(res.data);
        this.setData({
          result,
          bestProbabilityText: this.formatProbability(result.bestProbability)
        });
      } else {
        this.showError(res.message || '识别失败');
      }
    } catch (err) {
      this.showError(err.message || '识别失败');
    } finally {
      wx.hideLoading();
      this.setData({ loading: false });
    }
  },

  formatResult(result) {
    const items = (result.items || []).map(item => ({
      name: item.name,
      calorie: item.calorie,
      probability: item.probability,
      probabilityText: this.formatProbability(item.probability)
    }));
    return Object.assign({}, result, { items });
  },

  formatProbability(probability) {
    const value = Number(probability || 0);
    if (value <= 1) {
      return Math.round(value * 100) + '%';
    }
    return Math.round(value) + '%';
  },

  showError(message) {
    const text = message || '识别失败，请稍后重试';
    this.setData({ errorText: text });
    wx.showModal({
      title: '识别失败',
      content: text,
      showCancel: false
    });
  }
});
