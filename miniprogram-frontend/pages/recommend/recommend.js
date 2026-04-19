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
    this.loadHealthInfo();
    this.loadRecommendations();
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
