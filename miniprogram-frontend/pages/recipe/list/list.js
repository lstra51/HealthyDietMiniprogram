const app = getApp();
const api = require('../../../utils/api.js');

Page({
  data: {
    recipes: [],
    categories: ['全部', '蔬菜', '肉类', '海鲜', '主食'],
    activeCategory: '全部',
    searchKeyword: '',
    filteredRecipes: []
  },

  onLoad() {
    this.loadRecipes();
  },

  onShow() {
    this.loadRecipes();
  },

  async loadRecipes() {
    wx.showLoading({ title: '加载中...' });
    
    try {
      const res = await api.get('/recipes');
      wx.hideLoading();
      
      if (res.code === 200) {
        const recipes = res.data;
        this.setData({ 
          recipes, 
          filteredRecipes: recipes 
        });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('加载食谱失败:', err);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    }
  },

  onCategoryTap(e) {
    const category = e.currentTarget.dataset.category;
    this.setData({ activeCategory: category });
    this.filterRecipes();
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  onSearch() {
    this.filterRecipes();
  },

  filterRecipes() {
    var filtered = [];
    for (var i = 0; i < this.data.recipes.length; i++) {
      filtered.push(this.data.recipes[i]);
    }
    var activeCategory = this.data.activeCategory;
    var searchKeyword = this.data.searchKeyword;

    if (activeCategory !== '全部') {
      var newFiltered = [];
      for (var j = 0; j < filtered.length; j++) {
        if (filtered[j].category === activeCategory) {
          newFiltered.push(filtered[j]);
        }
      }
      filtered = newFiltered;
    }

    if (searchKeyword) {
      var keyword = searchKeyword.toLowerCase();
      var newFiltered2 = [];
      for (var k = 0; k < filtered.length; k++) {
        if (filtered[k].name.toLowerCase().indexOf(keyword) !== -1) {
          newFiltered2.push(filtered[k]);
        }
      }
      filtered = newFiltered2;
    }

    this.setData({ filteredRecipes: filtered });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/recipe/detail/detail?id=${id}`
    });
  }
});
