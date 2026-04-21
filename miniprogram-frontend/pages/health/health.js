const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    height: '',
    weight: '',
    gender: '男',
    goal: '减脂',
    goalText: '减脂',
    bmi: '',
    bmiStatus: ''
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
            wx.navigateTo({
              url: '/pages/auth/login/login'
            });
          } else {
            wx.navigateBack();
          }
        }
      });
      return false;
    }
    return true;
  },

  loadSavedInfo() {
    var savedInfo = app.globalData.healthInfo;
    if (savedInfo) {
      var newData = {};
      if (savedInfo.height) newData.height = savedInfo.height;
      if (savedInfo.weight) newData.weight = savedInfo.weight;
      if (savedInfo.gender) newData.gender = savedInfo.gender;
      if (savedInfo.goal) {
        newData.goal = savedInfo.goal;
        newData.goalText = savedInfo.goal;
      }
      this.setData(newData);
      this.calculateBMI();
    }
  },

  onHeightInput(e) {
    this.setData({ height: e.detail.value });
  },

  onWeightInput(e) {
    this.setData({ weight: e.detail.value });
    this.calculateBMI();
  },

  onGenderChange(e) {
    const genders = ['男', '女'];
    this.setData({ gender: genders[e.detail.value] });
  },

  onGoalChange(e) {
    const goals = ['减脂', '增肌', '保持'];
    const goal = goals[e.detail.value];
    this.setData({ goal, goalText: goal });
  },

  selectGoal(e) {
    const goal = e.currentTarget.dataset.goal;
    this.setData({ goal, goalText: goal });
  },

  calculateBMI() {
    var height = this.data.height;
    var weight = this.data.weight;
    if (height && weight) {
      var bmi = app.calculateBMI(parseFloat(height), parseFloat(weight));
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
      this.setData({ bmi: bmi, bmiStatus: bmiStatus });
    }
  },

  async onSave() {
    var height = this.data.height;
    var weight = this.data.weight;
    var gender = this.data.gender;
    var goal = this.data.goal;
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

    if (!height) {
      wx.showToast({
        title: '请输入身高',
        icon: 'none'
      });
      return;
    }

    if (!weight) {
      wx.showToast({
        title: '请输入体重',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '保存中...' });

    const success = await app.saveHealthInfo(userId, {
      height: parseFloat(height),
      weight: parseFloat(weight),
      gender,
      goal
    });

    wx.hideLoading();

    if (success) {
      wx.showToast({
        title: '保存成功',
        icon: 'success'
      });

      setTimeout(() => {
        wx.navigateBack();
      }, 1000);
    } else {
      wx.showToast({
        title: '保存失败，请重试',
        icon: 'none'
      });
    }
  }
});
