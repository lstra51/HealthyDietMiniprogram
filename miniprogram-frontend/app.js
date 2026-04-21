const api = require('./utils/api.js');

App({
  globalData: {
    userInfo: null,
    healthInfo: null,
    isLoggedIn: false,
    chatMessages: null
  },

  onLaunch() {
    this.checkLoginStatus();
  },

  checkLoginStatus() {
    var userInfo = wx.getStorageSync('userInfo');
    if (userInfo) {
      this.globalData.userInfo = userInfo;
      this.globalData.isLoggedIn = true;
      var healthInfo = wx.getStorageSync('healthInfo');
      if (healthInfo) {
        this.globalData.healthInfo = healthInfo;
      } else {
        this.loadHealthInfo(userInfo.id);
      }
    } else {
      // 确保退出登录后清理所有数据
      this.globalData.userInfo = null;
      this.globalData.healthInfo = null;
      this.globalData.isLoggedIn = false;
      this.globalData.chatMessages = null;
    }
  },

  logout() {
    this.globalData.userInfo = null;
    this.globalData.healthInfo = null;
    this.globalData.isLoggedIn = false;
    this.globalData.chatMessages = null;
    wx.removeStorageSync('userInfo');
    wx.removeStorageSync('healthInfo');
  },

  calculateBMI(height, weight) {
    const heightM = height / 100;
    return (weight / (heightM * heightM)).toFixed(1);
  },

  async getRecommendations(userId) {
    try {
      var res = await api.get('/recommendations/user/' + userId);
      if (res.code === 200) {
        return res.data;
      }
      return [];
    } catch (err) {
      console.error('获取推荐失败:', err);
      return [];
    }
  },

  async loadHealthInfo(userId) {
    try {
      var res = await api.get('/health/' + userId);
      if (res.code === 200) {
        this.globalData.healthInfo = res.data;
        wx.setStorageSync('healthInfo', res.data);
        return res.data;
      }
      return null;
    } catch (err) {
      console.error('加载健康信息失败:', err);
      return null;
    }
  },

  async saveHealthInfo(userId, healthInfo) {
    try {
      const res = await api.post('/health', {
        userId,
        height: healthInfo.height,
        weight: healthInfo.weight,
        gender: healthInfo.gender,
        goal: healthInfo.goal
      });
      if (res.code === 200) {
        this.globalData.healthInfo = res.data;
        wx.setStorageSync('healthInfo', res.data);
        return true;
      }
      return false;
    } catch (err) {
      console.error('保存健康信息失败:', err);
      return false;
    }
  }
})
