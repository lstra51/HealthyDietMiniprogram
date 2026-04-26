const api = require('../../../utils/api.js');

Page({
  data: {
    recipes: [],
    categories: ['全部', '蔬菜', '肉类', '海鲜', '主食', '汤'],
    activeCategory: '全部',
    searchKeyword: '',
    filterOpen: false,
    minCalories: '',
    maxCalories: '',
    minProtein: '',
    tag: '',
    goal: '',
    goals: ['全部', '减脂', '增肌', '保持']
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
      const params = this.buildQuery();
      const res = await api.get('/recipes', params);
      if (res.code === 200) {
        this.setData({ recipes: api.formatRecipeImages(res.data) });
      }
    } catch (err) {
      console.error('加载食谱失败:', err);
      wx.showToast({ title: '加载失败，请重试', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  buildQuery() {
    const data = this.data;
    return {
      category: data.activeCategory !== '全部' ? data.activeCategory : '',
      keyword: data.searchKeyword,
      minCalories: data.minCalories,
      maxCalories: data.maxCalories,
      minProtein: data.minProtein,
      tag: data.tag,
      goal: data.goal && data.goal !== '全部' ? data.goal : ''
    };
  },

  onCategoryTap(e) {
    this.setData({ activeCategory: e.currentTarget.dataset.category });
    this.loadRecipes();
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  onSearch() {
    this.loadRecipes();
  },

  toggleFilter() {
    this.setData({ filterOpen: !this.data.filterOpen });
  },

  onFilterInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  onGoalChange(e) {
    const goal = this.data.goals[e.detail.value];
    this.setData({ goal });
  },

  applyFilters() {
    this.loadRecipes();
  },

  resetFilters() {
    this.setData({
      minCalories: '',
      maxCalories: '',
      minProtein: '',
      tag: '',
      goal: ''
    });
    this.loadRecipes();
  },

  onImageError(e) {
    const index = e.currentTarget.dataset.index;
    const recipes = this.data.recipes.slice();
    if (recipes[index]) {
      recipes[index].image = api.DEFAULT_RECIPE_IMAGE;
      this.setData({ recipes });
    }
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/recipe/detail/detail?id=${id}` });
  }
});
