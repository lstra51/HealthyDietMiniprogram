const app = getApp();

Page({
  data: {
    height: '',
    weight: '',
    gender: '男',
    goal: '减脂',
    goalText: '减脂',
    bmi: '',
    bmiStatus: '',
    preferenceOptions: [
      { label: '糖尿病', active: false },
      { label: '低盐', active: false },
      { label: '忌辣', active: false },
      { label: '素食', active: false },
      { label: '海鲜过敏', active: false },
      { label: '花生过敏', active: false }
    ],
    dietaryPreferences: []
  },

  onLoad() {
    if (!this.checkNeedLogin()) return;
    this.loadSavedInfo();
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
            wx.navigateTo({ url: '/pages/auth/login/login' });
          } else {
            wx.switchTab({ url: '/pages/home/home' });
          }
        }
      });
      return false;
    }
    return true;
  },

  clearData() {
    this.setData({
      height: '',
      weight: '',
      gender: '男',
      goal: '减脂',
      goalText: '减脂',
      bmi: '',
      bmiStatus: '',
      preferenceOptions: [
        { label: '糖尿病', active: false },
        { label: '低盐', active: false },
        { label: '忌辣', active: false },
        { label: '素食', active: false },
        { label: '海鲜过敏', active: false },
        { label: '花生过敏', active: false }
      ],
      dietaryPreferences: []
    });
  },

  loadSavedInfo() {
    const savedInfo = app.globalData.healthInfo;
    if (savedInfo) {
      const newData = {};
      if (savedInfo.height) newData.height = savedInfo.height;
      if (savedInfo.weight) newData.weight = savedInfo.weight;
      if (savedInfo.gender) newData.gender = savedInfo.gender;
      if (savedInfo.goal) {
        newData.goal = savedInfo.goal;
        newData.goalText = savedInfo.goal;
      }
      const savedPreferences = savedInfo.dietaryPreferences || [];
      newData.dietaryPreferences = savedPreferences;
      newData.preferenceOptions = this.data.preferenceOptions.map(opt => ({
        ...opt,
        active: savedPreferences.includes(opt.label)
      }));
      this.setData(newData);
      this.calculateBMI();
    }
  },

  onHeightInput(e) {
    this.setData({ height: e.detail.value });
    this.calculateBMI();
  },

  onWeightInput(e) {
    this.setData({ weight: e.detail.value });
    this.calculateBMI();
  },

  onGenderChange(e) {
    const genders = ['男', '女'];
    this.setData({ gender: genders[e.detail.value] });
  },

  selectGoal(e) {
    const goal = e.currentTarget.dataset.goal;
    this.setData({ goal, goalText: goal });
  },

  togglePreference(e) {
    const index = e.currentTarget.dataset.index;
    const preferenceOptions = this.data.preferenceOptions.slice();
    preferenceOptions[index].active = !preferenceOptions[index].active;
    const dietaryPreferences = preferenceOptions
      .filter(opt => opt.active)
      .map(opt => opt.label);
    this.setData({ preferenceOptions, dietaryPreferences });
  },

  calculateBMI() {
    const { height, weight } = this.data;
    if (height && weight) {
      const bmi = app.calculateBMI(parseFloat(height), parseFloat(weight));
      let bmiStatus = '';
      if (bmi < 18.5) {
        bmiStatus = '偏瘦';
      } else if (bmi < 24) {
        bmiStatus = '正常';
      } else if (bmi < 28) {
        bmiStatus = '超重';
      } else {
        bmiStatus = '肥胖';
      }
      this.setData({ bmi, bmiStatus });
    }
  },

  async onSave() {
    const { height, weight, gender, goal, dietaryPreferences } = this.data;
    const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null;

    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    if (!height) {
      wx.showToast({ title: '请输入身高', icon: 'none' });
      return;
    }
    if (!weight) {
      wx.showToast({ title: '请输入体重', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '保存中...' });
    const success = await app.saveHealthInfo(userId, {
      height: parseFloat(height),
      weight: parseFloat(weight),
      gender,
      goal,
      dietaryPreferences
    });
    wx.hideLoading();

    if (success) {
      wx.showToast({ title: '保存成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 1000);
    } else {
      wx.showToast({ title: '保存失败，请重试', icon: 'none' });
    }
  }
});
