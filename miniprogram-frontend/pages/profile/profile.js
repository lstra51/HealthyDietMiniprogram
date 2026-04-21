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
    if (!app.globalData.isLoggedIn) {
      this.clearData();
    } else {
      this.loadHealthInfo();
    }
  },

  clearData() {
    this.setData({
      userInfo: null,
      displayName: '用户',
      avatarInitial: '?',
      healthInfo: null,
      bmi: '',
      bmiStatus: ''
    });
  },

  checkNeedLogin(callback) {
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
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/health/health'
    });
  },

  goToFavorites() {
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/favorites/favorites'
    });
  },

  goToRecommend() {
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/recommend/recommend'
    });
  },

  goToRecord() {
    if (!this.checkNeedLogin()) return;
    wx.switchTab({
      url: '/pages/record/record'
    });
  },

  onLogout() {
    if (!this.checkNeedLogin()) return;
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 使用 app.logout() 统一处理退出登录
          app.logout();
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          });

          setTimeout(() => {
            wx.switchTab({
              url: '/pages/home/home'
            });
          }, 1000);
        }
      }
    });
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/auth/login/login'
    });
  }
});
