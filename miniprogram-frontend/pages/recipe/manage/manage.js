const api = require('../../../utils/api.js');

const CATEGORIES = [
  { label: '全部', value: '' },
  { label: '蔬菜', value: '蔬菜' },
  { label: '肉类', value: '肉类' },
  { label: '海鲜', value: '海鲜' },
  { label: '主食', value: '主食' },
  { label: '蛋类', value: '蛋类' },
  { label: '汤类', value: '汤类' }
];

const STATUSES = [
  { label: '全部', value: '' },
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已拒绝', value: 'rejected' }
];

const STATUS_TEXT = {
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝'
};

Page({
  data: {
    recipeList: [],
    searchKeyword: '',
    categories: CATEGORIES,
    activeCategory: '',
    statuses: STATUSES,
    activeStatus: ''
  },

  onLoad() {
    this.loadRecipes();
  },

  onShow() {
    this.loadRecipes();
  },

  buildQuery() {
    return {
      keyword: this.data.searchKeyword.trim(),
      category: this.data.activeCategory,
      status: this.data.activeStatus
    };
  },

  loadRecipes() {
    wx.showLoading({ title: '加载中...' });
    api.get('/recipes/admin', this.buildQuery())
      .then(res => {
        if (res.code === 200) {
          const recipeList = api.formatRecipeImages(res.data || []).map(item => ({
            ...item,
            statusText: STATUS_TEXT[item.status] || item.status || ''
          }));
          this.setData({ recipeList });
        } else {
          wx.showToast({ title: res.message || '加载失败', icon: 'none' });
        }
      })
      .catch(err => {
        console.error('加载管理员食谱失败:', err);
        wx.showToast({ title: '加载失败', icon: 'none' });
      })
      .finally(() => {
        wx.hideLoading();
      });
  },

  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value });
  },

  onSearch() {
    this.loadRecipes();
  },

  onCategoryTap(e) {
    this.setData({ activeCategory: e.currentTarget.dataset.category });
    this.loadRecipes();
  },

  onStatusTap(e) {
    this.setData({ activeStatus: e.currentTarget.dataset.status });
    this.loadRecipes();
  },

  onImageError(e) {
    const index = e.currentTarget.dataset.index;
    const recipeList = this.data.recipeList.slice();
    if (recipeList[index]) {
      recipeList[index].image = api.DEFAULT_RECIPE_IMAGE;
      this.setData({ recipeList });
    }
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/recipe/detail/detail?id=${id}`
    });
  },

  editRecipe(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/recipe/create/create?id=${id}&mode=admin`
    });
  },

  deleteRecipe(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: '删除后食谱将无法恢复，是否继续？',
      success: (res) => {
        if (!res.confirm) return;

        wx.showLoading({ title: '删除中...' });
        api.delete(`/recipes/${id}/admin`)
          .then(deleteRes => {
            if (deleteRes.code === 200) {
              wx.showToast({ title: '删除成功', icon: 'success' });
              this.loadRecipes();
            } else {
              wx.showToast({ title: deleteRes.message || '删除失败', icon: 'none' });
            }
          })
          .catch(err => {
            console.error('删除食谱失败:', err);
            wx.showToast({ title: '删除失败', icon: 'none' });
          })
          .finally(() => {
            wx.hideLoading();
          });
      }
    });
  }
});
