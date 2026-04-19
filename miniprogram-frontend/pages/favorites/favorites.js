const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    favorites: [],
    loading: false
  },

  onLoad() {
    this.loadFavorites();
  },

  onShow() {
    this.loadFavorites();
  },

  async loadFavorites() {
    var userId = null;
    if (app.globalData.userInfo) {
      userId = app.globalData.userInfo.id;
    }

    if (!userId) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }

    this.setData({ loading: true });

    try {
      var res = await api.get('/favorites/user/' + userId);
      this.setData({ loading: false });

      if (res.code === 200) {
        this.setData({ favorites: res.data || [] });
      }
    } catch (err) {
      this.setData({ loading: false });
      console.error('加载收藏失败:', err);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    }
  },

  goToRecipeDetail(e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/recipe/detail/detail?id=' + id
    });
  }
});
