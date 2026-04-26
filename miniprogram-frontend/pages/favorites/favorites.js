const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    favorites: [],
    loading: false
  },

  onLoad() {
    if (!this.checkNeedLogin()) return;
    this.loadFavorites();
  },

  onShow() {
    if (!app.globalData.isLoggedIn) {
      this.clearData();
      this.checkNeedLogin();
      return;
    }
    this.loadFavorites();
  },

  clearData() {
    this.setData({
      favorites: [],
      loading: false
    });
  },

  checkNeedLogin() {
    if (!app.globalData.isLoggedIn) {
      wx.showModal({
        title: '需要登录',
        content: '此功能需要登录后才能使用，是否前往登录？',
        confirmText: '去登录',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/auth/login/login'
            });
          } else {
            wx.switchTab({
              url: '/pages/home/home'
            });
          }
        }
      });
      return false;
    }
    return true;
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
        this.setData({ favorites: api.formatRecipeImages(res.data) });
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
  },

  onImageError(e) {
    const index = e.currentTarget.dataset.index;
    const favorites = this.data.favorites.slice();
    if (favorites[index]) {
      favorites[index].image = api.DEFAULT_RECIPE_IMAGE;
      this.setData({ favorites });
    }
  }
});
