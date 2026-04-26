const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    selectedDate: '',
    today: '',
    currentMonthText: '',
    calendarDays: [],
    selectedRecords: [],
    selectedTotal: {},
    allRecords: [],
    progressItems: []
  },

  onLoad() {
    if (!this.checkNeedLogin()) return;
    const today = app.formatLocalDate(new Date());
    this.setData({ today, selectedDate: today });
    this.loadRecords();
  },

  onShow() {
    if (!app.globalData.isLoggedIn) {
      this.clearData();
      this.checkNeedLogin();
      return;
    }
    this.loadRecords();
  },

  clearData() {
    this.setData({
      selectedRecords: [],
      selectedTotal: {},
      allRecords: [],
      calendarDays: [],
      progressItems: []
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

  async loadRecords() {
    if (!app.globalData.isLoggedIn) return;
    const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null;
    if (!userId) return;

    wx.showLoading({ title: '加载中...' });
    try {
      const selectedDate = this.data.selectedDate || app.formatLocalDate(new Date());
      const recordsRes = await api.get('/records/user/' + userId);
      const nutritionRes = await api.get('/records/user/' + userId + '/nutrition/' + selectedDate);

      if (recordsRes.code === 200) {
        const allRecords = recordsRes.data || [];
        const selectedRecords = allRecords.filter(item => item.recordDate === selectedDate);
        const selectedTotal = nutritionRes.code === 200 ? nutritionRes.data : {};
        this.setData({
          allRecords,
          selectedRecords,
          selectedTotal,
          progressItems: this.buildProgressItems(selectedTotal)
        });
        this.buildCalendar(allRecords);
      }
    } catch (err) {
      console.error('加载记录失败:', err);
      wx.showToast({ title: '加载失败，请重试', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  buildProgressItems(nutrition) {
    const data = nutrition || {};
    return [
      this.progressItem('热量', data.totalCalories || 0, data.targetCalories || 0, 'kcal', data.caloriesProgress || 0),
      this.progressItem('蛋白质', data.totalProtein || 0, data.targetProtein || 0, 'g', data.proteinProgress || 0),
      this.progressItem('碳水', data.totalCarbs || 0, data.targetCarbs || 0, 'g', data.carbsProgress || 0),
      this.progressItem('脂肪', data.totalFat || 0, data.targetFat || 0, 'g', data.fatProgress || 0)
    ];
  },

  progressItem(label, value, target, unit, progress) {
    const safeProgress = Math.min(Math.round(progress || 0), 100);
    const displayValue = unit === 'kcal' ? Math.round(value) : Number(value || 0).toFixed(1);
    const displayTarget = unit === 'kcal' ? Math.round(target) : Number(target || 0).toFixed(1);
    return {
      label,
      value: displayValue,
      target: displayTarget,
      unit,
      progress: safeProgress
    };
  },

  buildCalendar(records) {
    const selected = new Date((this.data.selectedDate || this.data.today) + 'T00:00:00');
    const year = selected.getFullYear();
    const month = selected.getMonth();
    const firstDay = new Date(year, month, 1);
    const start = new Date(firstDay);
    start.setDate(firstDay.getDate() - firstDay.getDay());

    const recordDateSet = {};
    (records || []).forEach(item => {
      recordDateSet[item.recordDate] = true;
    });

    const calendarDays = [];
    for (let i = 0; i < 42; i++) {
      const date = new Date(start);
      date.setDate(start.getDate() + i);
      const dateText = app.formatLocalDate(date);
      calendarDays.push({
        date: dateText,
        day: date.getDate(),
        inMonth: date.getMonth() === month,
        isToday: dateText === this.data.today,
        selected: dateText === this.data.selectedDate,
        hasRecord: !!recordDateSet[dateText]
      });
    }

    this.setData({
      calendarDays,
      currentMonthText: `${year}年${month + 1}月`
    });
  },

  async selectDate(e) {
    const date = e.currentTarget.dataset.date;
    this.setData({ selectedDate: date });
    await this.loadRecords();
  },

  changeMonth(e) {
    const offset = Number(e.currentTarget.dataset.offset || 0);
    const current = new Date((this.data.selectedDate || this.data.today) + 'T00:00:00');
    current.setMonth(current.getMonth() + offset);
    current.setDate(1);
    this.setData({ selectedDate: app.formatLocalDate(current) });
    this.loadRecords();
  },

  goToRecipeList() {
    wx.switchTab({ url: '/pages/recipe/list/list' });
  },

  deleteRecord(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条记录吗？',
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '删除中...' });
          try {
            await api.delete(`/records/${id}`);
            wx.showToast({ title: '删除成功', icon: 'success' });
            this.loadRecords();
          } catch (err) {
            console.error('删除记录失败:', err);
            wx.showToast({ title: '删除失败，请重试', icon: 'none' });
          } finally {
            wx.hideLoading();
          }
        }
      }
    });
  }
});
