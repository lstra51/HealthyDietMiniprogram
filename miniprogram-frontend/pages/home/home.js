const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    todayRecommendation: null,
    todayRecommendationFormatted: null,
    healthInfoFilled: false,
    userName: '',
    currentDate: ''
  },

  onLoad() {
    this.setCurrentDate();
    this.checkLoginStatus();
    this.loadTodayRecommendation();
    this.checkHealthInfo();
  },

  setCurrentDate() {
    const now = new Date();
    const month = now.getMonth() + 1;
    const day = now.getDate();
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    const weekday = weekdays[now.getDay()];
    this.setData({ currentDate: `${month}月${day}日 ${weekday}` });
  },

  onShow() {
    this.checkLoginStatus();
    this.checkHealthInfo();
  },

  checkLoginStatus() {
    if (!app.globalData.isLoggedIn) {
      wx.reLaunch({
        url: '/pages/auth/login/login'
      });
      return;
    }
    if (app.globalData.userInfo) {
      const userInfo = app.globalData.userInfo;
      const displayName = userInfo.nickname || userInfo.username || '用户';
      this.setData({ userName: displayName });
    }
  },

  async loadTodayRecommendation() {
    if (!app.globalData.isLoggedIn) return;
    
    const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null;
    if (!userId) return;

    const recommendations = await app.getRecommendations(userId);
    if (recommendations.length > 0) {
      const rec = recommendations[0];
      var formatted = {};
      for (var key in rec) {
        if (rec.hasOwnProperty(key)) {
          formatted[key] = rec[key];
        }
      }
      formatted.scoreFormatted = Math.round(rec.score * 100);
      this.setData({ 
        todayRecommendation: rec,
        todayRecommendationFormatted: formatted
      });
    }
  },

  checkHealthInfo() {
    this.setData({ healthInfoFilled: !!app.globalData.healthInfo });
  },

  onViewTodayDetail() {
    if (this.data.todayRecommendation) {
      wx.navigateTo({
        url: `/pages/recipe/detail/detail?id=${this.data.todayRecommendation.recipeId}`
      });
    }
  },

  goToRecommend() {
    wx.navigateTo({
      url: '/pages/recommend/recommend'
    });
  },

  goToRecipeList() {
    wx.switchTab({
      url: '/pages/recipe/list/list'
    });
  },

  goToHealth() {
    wx.navigateTo({
      url: '/pages/health/health'
    });
  },

  goToChat() {
    wx.navigateTo({
      url: '/pages/chat/chat'
    });
  }
});
