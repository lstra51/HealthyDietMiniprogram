const api = require('../../../utils/api.js');

const GOALS = ['全部', '减脂', '增肌', '保持'];

Page({
  data: {
    recipes: [],
    availableTags: [],
    activeTag: '',
    searchKeyword: '',
    filterOpen: false,
    minCalories: '',
    maxCalories: '',
    minProtein: '',
    goal: '全部',
    goals: GOALS
  },

  onLoad() {
    this.loadPageData();
  },

  onShow() {
    this.loadPageData();
  },

  async loadPageData() {
    await Promise.all([this.loadAvailableTags(), this.loadRecipes()]);
  },

  async loadAvailableTags() {
    try {
      const res = await api.get('/recipes/tags');
      if (res.code === 200) {
        const availableTags = res.data || [];
        const nextData = { availableTags };
        if (this.data.activeTag && !availableTags.includes(this.data.activeTag)) {
          nextData.activeTag = '';
        }
        this.setData(nextData);
      }
    } catch (err) {
      console.error('加载标签失败:', err);
    }
  },

  async loadRecipes() {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await api.get('/recipes', this.buildQuery());
      if (res.code === 200) {
        this.setData({ recipes: api.formatRecipeImages(res.data || []) });
      }
    } catch (err) {
      console.error('加载食谱失败:', err);
      wx.showToast({ title: '加载食谱失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  buildQuery() {
    return {
      keyword: this.data.searchKeyword.trim(),
      minCalories: this.data.minCalories,
      maxCalories: this.data.maxCalories,
      minProtein: this.data.minProtein,
      tag: this.data.activeTag,
      goal: this.data.goal !== '全部' ? this.data.goal : ''
    };
  },

  onTagTap(e) {
    const tag = e.currentTarget.dataset.tag || '';
    this.setData({
      activeTag: this.data.activeTag === tag ? '' : tag
    });
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
    const goal = this.data.goals[Number(e.detail.value)] || '全部';
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
      activeTag: '',
      goal: '全部'
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
