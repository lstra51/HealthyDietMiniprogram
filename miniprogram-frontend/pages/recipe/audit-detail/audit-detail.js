const api = require('../../../utils/api.js');

Page({
  data: {
    id: null,
    recipe: null,
    showReject: false,
    rejectReason: ''
  },

  onLoad(options) {
    this.setData({ id: options.id });
    this.loadDetail();
  },

  loadDetail() {
    api.get(`/recipes/${this.data.id}`).then(res => {
      if (res.code === 200) {
        this.setData({ recipe: res.data });
      }
    });
  },

  approve() {
    wx.showModal({
      title: '确认通过',
      content: '确定要通过该食谱吗？',
      success: (res) => {
        if (res.confirm) {
          api.put(`/recipes/${this.data.id}/approve`).then(res => {
            if (res.code === 200) {
              wx.showToast({ title: '审核通过', icon: 'success' });
              setTimeout(() => {
                wx.navigateBack();
              }, 1500);
            }
          });
        }
      }
    });
  },

  showRejectDialog() {
    this.setData({ showReject: true, rejectReason: '' });
  },

  hideRejectDialog() {
    this.setData({ showReject: false });
  },

  onReasonInput(e) {
    this.setData({ rejectReason: e.detail.value });
  },

  reject() {
    if (!this.data.rejectReason.trim()) {
      wx.showToast({ title: '请输入拒绝原因', icon: 'none' });
      return;
    }

    api.put(`/recipes/${this.data.id}/reject`, { reason: this.data.rejectReason }).then(res => {
      if (res.code === 200) {
        wx.showToast({ title: '已拒绝', icon: 'success' });
        this.setData({ showReject: false });
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      }
    });
  }
});
