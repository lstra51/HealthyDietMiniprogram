const api = require('../../../utils/api.js');

Page({
  data: {
    recipeList: []
  },

  onLoad() {
    this.loadRecipes();
  },

  onShow() {
    this.loadRecipes();
  },

  loadRecipes() {
    api.get('/recipes/pending').then(res => {
      if (res.code === 200) {
        this.setData({ recipeList: res.data });
      }
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/recipe/audit-detail/audit-detail?id=${id}`
    });
  }
});
