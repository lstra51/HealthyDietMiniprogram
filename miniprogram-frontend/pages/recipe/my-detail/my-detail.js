const api = require('../../../utils/api.js');

Page({
  data: {
    id: null,
    recipe: null
  },

  onLoad(options) {
    this.setData({ id: options.id });
    this.loadDetail();
  },

  loadDetail() {
    api.get(`/recipes/${this.data.id}`).then(res => {
      if (res.code === 200) {
        this.setData({ recipe: api.formatRecipeImage(res.data) });
      }
    });
  },

  edit() {
    wx.navigateTo({
      url: `/pages/recipe/create/create?id=${this.data.id}`
    });
  }
});
