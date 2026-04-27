const app = getApp();

Page({
  data: {
    todayRecommendation: null,
    todayRecommendationFormatted: null,
    healthInfoFilled: false,
    userName: '用户',
    currentDate: '',
    isLoggedIn: false,
    isAdmin: false,
    healthEntryIcon: '🩺',
    healthEntryLabel: '填写健康信息'
  },

  onLoad() {
    this.setCurrentDate();
    this.checkLoginStatus();
    this.loadTodayRecommendation();
    this.checkHealthInfo();
  },

  onShow() {
    this.setCurrentDate();
    this.checkLoginStatus();
    this.checkHealthInfo();
    if (!app.globalData.isLoggedIn) {
      this.clearData();
    } else {
      this.loadTodayRecommendation();
    }
  },

  setCurrentDate() {
    const now = new Date();
    const month = now.getMonth() + 1;
    const day = now.getDate();
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    this.setData({
      currentDate: `${month}月${day}日 ${weekdays[now.getDay()]}`
    });
  },

  clearData() {
    this.setData({
      todayRecommendation: null,
      todayRecommendationFormatted: null
    });
  },

  checkLoginStatus() {
    const isLoggedIn = !!app.globalData.isLoggedIn;
    const userInfo = app.globalData.userInfo || {};
    this.setData({
      isLoggedIn,
      userName: userInfo.nickname || userInfo.username || '用户',
      isAdmin: userInfo.role === 'admin'
    });
  },

  async loadTodayRecommendation() {
    if (!app.globalData.isLoggedIn) return;

    const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null;
    if (!userId) return;

    try {
      const recommendations = await app.getRecommendations(userId);
      if (recommendations.length > 0) {
        const rec = recommendations[0];
        this.setData({
          todayRecommendation: rec,
          todayRecommendationFormatted: {
            ...rec,
            scoreFormatted: Math.round((rec.score || 0) * 100),
            reason: rec.reason || '根据你的健康信息推荐'
          }
        });
      } else {
        this.clearData();
      }
    } catch (err) {
      console.error('加载今日推荐失败:', err);
    }
  },

  checkHealthInfo() {
    const healthInfoFilled = !!app.globalData.healthInfo;
    this.setData({
      healthInfoFilled,
      healthEntryIcon: healthInfoFilled ? '✏️' : '🩺',
      healthEntryLabel: healthInfoFilled ? '修改健康信息' : '填写健康信息'
    });
  },

  checkNeedLogin() {
    if (!app.globalData.isLoggedIn) {
      wx.showModal({
        title: '需要登录',
        content: '该功能需要登录后使用，是否前往登录？',
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

  goToDishRecognition() {
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/dish-recognition/dish-recognition'
    });
  },

  goToLogin() {
    wx.navigateTo({
      url: '/pages/auth/login/login'
    });
  },

  goToCreateRecipe() {
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/recipe/create/create'
    });
  },

  goToManageRecipes() {
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/recipe/manage/manage'
    });
  },

  goToAudit() {
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/recipe/audit/audit'
    });
  },

  goToMyRecipes() {
    if (!this.checkNeedLogin()) return;
    wx.navigateTo({
      url: '/pages/recipe/my-recipes/my-recipes'
    });
  }
});
