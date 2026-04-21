const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    todayRecommendation: null,
    todayRecommendationFormatted: null,
    healthInfoFilled: false,
    userName: '',
    currentDate: '',
    isLoggedIn: false
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
    if (!app.globalData.isLoggedIn) {
      this.clearData();
    } else {
      this.loadTodayRecommendation();
    }
  },

  clearData() {
    this.setData({
      todayRecommendation: null,
      todayRecommendationFormatted: null
    });
  },

  checkLoginStatus() {
    const isLoggedIn = app.globalData.isLoggedIn;
    this.setData({ isLoggedIn });
    if (isLoggedIn && app.globalData.userInfo) {
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
          }
        }
      });
      return false;
    }
    return true;
  },

  onViewTodayDetail() {
    if (!this.checkNeedLogin()) return;
    if (this.data.todayRecommendation) {
      wx.navigateTo({
        url: `/pages/recipe/detail/detail?id=${this.data.todayRecommendation.recipeId}`
      });
    }
  },

  goToRecommend() {
    if (!this.checkNeedLogin()) return;
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
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/health/health'
    });
  },

  goToChat() {
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/chat/chat'
    });
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/auth/login/login'
    });
  }
});
