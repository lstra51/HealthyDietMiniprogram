const app = getApp();

Page({
  data: {
    userInfo: null,
    displayName: '用户',
    avatarInitial: '?',
    healthInfo: null,
    bmi: '',
    bmiStatus: ''
  },

  onLoad() {
    this.loadUserInfo();
    this.loadHealthInfo();
  },

  onShow() {
    this.loadUserInfo();
    this.loadHealthInfo();
  },

  loadUserInfo() {
    const userInfo = app.globalData.userInfo;
    let displayName = '用户';
    let avatarInitial = '?';

    if (userInfo) {
      displayName = userInfo.nickname || userInfo.username || '用户';
      const name = userInfo.nickname || userInfo.username;
      if (name) {
        avatarInitial = name.charAt(0).toUpperCase();
      }
    }

    this.setData({ userInfo, displayName, avatarInitial });
  },

  async loadHealthInfo() {
    var healthInfo = app.globalData.healthInfo;
    if (!healthInfo && app.globalData.isLoggedIn && app.globalData.userInfo) {
      healthInfo = await app.loadHealthInfo(app.globalData.userInfo.id);
    }
    if (healthInfo) {
      var bmi = app.calculateBMI(healthInfo.height, healthInfo.weight);
      var bmiStatus = '';
      if (bmi < 18.5) {
        bmiStatus = '偏瘦';
      } else if (bmi < 24) {
        bmiStatus = '正常';
      } else if (bmi < 28) {
        bmiStatus = '超重';
      } else {
        bmiStatus = '肥胖';
      }
      this.setData({ healthInfo: healthInfo, bmi: bmi, bmiStatus: bmiStatus });
    } else {
      this.setData({ healthInfo: null, bmi: '', bmiStatus: '' });
    }
  },

  goToHealth() {
    wx.navigateTo({
      url: '/pages/health/health'
    });
  },

  goToFavorites() {
    wx.navigateTo({
      url: '/pages/favorites/favorites'
    });
  },

  goToRecommend() {
    wx.navigateTo({
      url: '/pages/recommend/recommend'
    });
  },

  goToRecord() {
    wx.switchTab({
      url: '/pages/record/record'
    });
  },

  onLogout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          app.globalData.userInfo = null;
          app.globalData.healthInfo = null;
          app.globalData.isLoggedIn = false;
          wx.removeStorageSync('userInfo');
          wx.removeStorageSync('healthInfo');
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          });

          setTimeout(() => {
            wx.reLaunch({
              url: '/pages/auth/login/login'
            });
          }, 1000);
        }
      }
    });
  }
});
