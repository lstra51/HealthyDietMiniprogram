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
    api.get('/recipes/user').then(res => {
      if (res.code === 200) {
        this.setData({ recipeList: api.formatRecipeImages(res.data) });
      }
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    // 用户的所有食谱都跳转到 my-detail 页面
    wx.navigateTo({
      url: `/pages/recipe/my-detail/my-detail?id=${id}`
    });
  },

  editRecipe(e) {
    const id = e.currentTarget.dataset.id;
    e.stopPropagation();
    wx.navigateTo({
      url: `/pages/recipe/create/create?id=${id}`
    });
  }
});
