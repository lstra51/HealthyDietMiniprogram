const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    todayRecords: [],
    todayTotal: {
      calories: 0,
      protein: 0,
      carbs: 0,
      fat: 0
    },
    allRecords: [],
    today: ''
  },

  onLoad() {
    this.loadRecords();
  },

  onShow() {
    this.loadRecords();
  },

  async loadRecords() {
    if (!app.globalData.isLoggedIn) return;
    
    var userId = null;
    if (app.globalData.userInfo) {
      userId = app.globalData.userInfo.id;
    }
    if (!userId) return;

    var today = new Date().toISOString().split('T')[0];
    
    wx.showLoading({ title: '加载中...' });
    
    try {
      var recordsRes = await api.get('/records/user/' + userId);
      var nutritionRes = await api.get('/records/user/' + userId + '/nutrition/' + today);

      wx.hideLoading();

      if (recordsRes.code === 200) {
        var allRecords = recordsRes.data;
        var todayRecords = [];
        for (var i = 0; i < allRecords.length; i++) {
          if (allRecords[i].recordDate === today) {
            todayRecords.push(allRecords[i]);
          }
        }
        
        var todayTotal = { calories: 0, protein: 0, carbs: 0, fat: 0 };
        if (nutritionRes.code === 200) {
          todayTotal = nutritionRes.data;
        }

        this.setData({ 
          today: today, 
          todayRecords: todayRecords, 
          todayTotal: todayTotal,
          allRecords: allRecords 
        });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('加载记录失败:', err);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    }
  },

  goToRecipeList() {
    wx.switchTab({
      url: '/pages/recipe/list/list'
    });
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
            wx.hideLoading();
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            });
            this.loadRecords();
          } catch (err) {
            wx.hideLoading();
            console.error('删除记录失败:', err);
            wx.showToast({
              title: '删除失败，请重试',
              icon: 'none'
            });
          }
        }
      }
    });
  }
});
