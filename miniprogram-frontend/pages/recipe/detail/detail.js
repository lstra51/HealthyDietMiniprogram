const app = getApp();
const api = require('../../../utils/api.js');

Page({
  data: {
    recipe: null,
    showRecordModal: false,
    mealType: '早餐',
    portion: 1,
    isFavorited: false
  },

  onLoad(options) {
    const id = parseInt(options.id);
    this.loadRecipeDetail(id);
  },

  async loadRecipeDetail(id) {
    wx.showLoading({ title: '加载中...' });
    
    try {
      var res = await api.get('/recipes/' + id);
      wx.hideLoading();
      
      if (res.code === 200) {
        var recipe = res.data;
        wx.setNavigationBarTitle({ title: recipe.name });
        this.setData({ recipe: recipe });
        
        this.recordBehavior('view');
        
        var userId = null;
        if (app.globalData.userInfo) {
          userId = app.globalData.userInfo.id;
          this.checkFavoriteStatus(userId, id);
        }
      }
    } catch (err) {
      wx.hideLoading();
      console.error('加载食谱详情失败:', err);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    }
  },

  async checkFavoriteStatus(userId, recipeId) {
    try {
      var res = await api.get('/favorites/check', { userId: userId, recipeId: recipeId });
      if (res.code === 200) {
        this.setData({ isFavorited: res.data });
      }
    } catch (err) {
      console.error('检查收藏状态失败:', err);
    }
  },

  async toggleFavorite() {
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

    var recipeId = this.data.recipe.id;
    var isFavorited = this.data.isFavorited;

    try {
      if (isFavorited) {
        await api.delete('/favorites', { userId: userId, recipeId: recipeId });
        this.setData({ isFavorited: false });
        wx.showToast({ title: '已取消收藏', icon: 'success' });
      } else {
        await api.post('/favorites', { userId: userId, recipeId: recipeId });
        this.setData({ isFavorited: true });
        wx.showToast({ title: '收藏成功', icon: 'success' });
        this.recordBehavior('like');
      }
    } catch (err) {
      console.error('操作失败:', err);
      wx.showToast({
        title: '操作失败，请重试',
        icon: 'none'
      });
    }
  },

  async recordBehavior(behaviorType) {
    var userId = null;
    if (app.globalData.userInfo) {
      userId = app.globalData.userInfo.id;
    }
    
    if (!userId || !this.data.recipe) return;

    try {
      await api.post('/recommendations/behavior', {
        userId: userId,
        recipeId: this.data.recipe.id,
        behaviorType: behaviorType
      });
    } catch (err) {
      console.error('记录行为失败:', err);
    }
  },

  showAddRecordModal() {
    this.setData({ showRecordModal: true });
    this.recordBehavior('click');
  },

  hideAddRecordModal() {
    this.setData({ showRecordModal: false });
  },

  onMealTypeChange(e) {
    const mealTypes = ['早餐', '午餐', '晚餐', '加餐'];
    this.setData({ mealType: mealTypes[e.detail.value] });
  },

  onPortionChange(e) {
    this.setData({ portion: parseFloat(e.detail.value) || 1 });
  },

  async addToRecord() {
    var recipe = this.data.recipe;
    var mealType = this.data.mealType;
    var portion = this.data.portion;
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
    
    if (!recipe) return;

    wx.showLoading({ title: '保存中...' });

    try {
      var recordDate = new Date().toISOString().split('T')[0];
      var protein = (recipe.protein * portion).toFixed(2);
      var carbs = (recipe.carbs * portion).toFixed(2);
      var fat = (recipe.fat * portion).toFixed(2);
      var res = await api.post('/records', {
        userId: userId,
        recipeId: recipe.id,
        recipeName: recipe.name,
        mealType: mealType,
        portion: portion,
        calories: Math.round(recipe.calories * portion),
        protein: protein,
        carbs: carbs,
        fat: fat,
        recordDate: recordDate
      });

      wx.hideLoading();

      if (res.code === 200) {
        wx.showToast({
          title: '记录成功',
          icon: 'success'
        });
        this.hideAddRecordModal();
        this.recordBehavior('cook');
      } else {
        wx.showToast({
          title: res.message || '记录失败',
          icon: 'none'
        });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('保存饮食记录失败:', err);
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      });
    }
  }
});
