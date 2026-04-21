const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    recommendations: [],
    recommendationsFormatted: [],
    healthInfo: null,
    bmi: ''
  },

  onLoad() {
    if (!this.checkNeedLogin()) return;
    this.loadHealthInfo();
    this.loadRecommendations();
  },

  onShow() {
    if (!app.globalData.isLoggedIn) {
      this.clearData();
      this.checkNeedLogin();
      return;
    }
    this.loadHealthInfo();
    this.loadRecommendations();
  },

  clearData() {
    this.setData({
      recommendations: [],
      recommendationsFormatted: [],
      healthInfo: null,
      bmi: ''
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

  loadHealthInfo() {
    const healthInfo = app.globalData.healthInfo;
    if (healthInfo) {
      const bmi = app.calculateBMI(healthInfo.height, healthInfo.weight);
      this.setData({ healthInfo, bmi });
    }
  },

  async loadRecommendations() {
    if (!app.globalData.isLoggedIn) return;
    
    const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null;
    if (!userId) return;

    wx.showLoading({ title: '加载中...' });

    const recommendations = await app.getRecommendations(userId);
    wx.hideLoading();
    
    var recommendationsFormatted = [];
    for (var i = 0; i < recommendations.length; i++) {
      var rec = recommendations[i];
      var formatted = {};
      for (var key in rec) {
        if (rec.hasOwnProperty(key)) {
          formatted[key] = rec[key];
        }
      }
      formatted.scoreFormatted = Math.round(rec.score * 100);
      recommendationsFormatted.push(formatted);
    }
    
    this.setData({ 
      recommendations, 
      recommendationsFormatted 
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/recipe/detail/detail?id=${id}`
    });
  },

  goToHealth() {
    wx.navigateTo({
      url: '/pages/health/health'
    });
  }
});
