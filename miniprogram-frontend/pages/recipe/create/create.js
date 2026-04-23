const api = require('../../../utils/api.js');

Page({
  data: {
    id: null,
    name: '',
    category: '蔬菜',
    categories: ['蔬菜', '肉类', '海鲜', '主食', '汤'],
    image: '',
    description: '',
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0,
    ingredients: [''],
    tags: [''],
    suitableGoals: ['减脂'],
    goals: ['减脂', '增肌', '保持'],
    steps: [''],
    loading: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ id: options.id });
      this.loadRecipeDetail();
    }
  },

  loadRecipeDetail() {
    api.get(`/recipes/${this.data.id}`).then(res => {
      if (res.code === 200) {
        const recipe = res.data;
        this.setData({
          name: recipe.name,
          category: recipe.category,
          image: api.formatImageUrl(recipe.image),
          description: recipe.description,
          calories: recipe.calories,
          protein: recipe.protein,
          carbs: recipe.carbs,
          fat: recipe.fat,
          ingredients: recipe.ingredients && recipe.ingredients.length > 0 ? recipe.ingredients : [''],
          tags: recipe.tags && recipe.tags.length > 0 ? recipe.tags : [''],
          suitableGoals: recipe.suitableGoals && recipe.suitableGoals.length > 0 ? recipe.suitableGoals : ['减脂'],
          steps: recipe.steps && recipe.steps.length > 0 ? recipe.steps : ['']
        });
      }
    });
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  onCategoryChange(e) {
    const index = e.detail.value;
    this.setData({ category: this.data.categories[index] });
  },

  clearImage() {
    this.setData({ image: '' });
  },

  onNumberInput(e) {
    const field = e.currentTarget.dataset.field;
    const value = parseFloat(e.detail.value) || 0;
    this.setData({ [field]: value });
  },

  chooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const filePath = res.tempFilePaths[0];
        this.uploadImage(filePath);
      }
    });
  },

  uploadImage(filePath) {
    wx.showLoading({ title: '上传中...' });
    api.uploadFile('/upload/image', filePath)
      .then(res => {
        wx.hideLoading();
        if (res.code === 200) {
          this.setData({ image: res.data.url });
          wx.showToast({ title: '上传成功', icon: 'success' });
        } else {
          wx.showToast({ title: res.message || '上传失败', icon: 'none' });
        }
      })
      .catch(err => {
        wx.hideLoading();
        console.error(err);
        wx.showToast({ title: '上传失败', icon: 'none' });
      });
  },

  addIngredient() {
    const ingredients = this.data.ingredients;
    ingredients.push('');
    this.setData({ ingredients });
  },

  removeIngredient(e) {
    const index = e.currentTarget.dataset.index;
    const ingredients = this.data.ingredients;
    if (ingredients.length > 1) {
      ingredients.splice(index, 1);
      this.setData({ ingredients });
    }
  },

  onIngredientInput(e) {
    const index = e.currentTarget.dataset.index;
    const value = e.detail.value;
    const ingredients = this.data.ingredients;
    ingredients[index] = value;
    this.setData({ ingredients });
  },

  addTag() {
    const tags = this.data.tags;
    tags.push('');
    this.setData({ tags });
  },

  removeTag(e) {
    const index = e.currentTarget.dataset.index;
    const tags = this.data.tags;
    if (tags.length > 1) {
      tags.splice(index, 1);
      this.setData({ tags });
    }
  },

  onTagInput(e) {
    const index = e.currentTarget.dataset.index;
    const value = e.detail.value;
    const tags = this.data.tags;
    tags[index] = value;
    this.setData({ tags });
  },

  onGoalChange(e) {
    const index = e.currentTarget.dataset.index;
    const value = this.data.goals[e.detail.value];
    const suitableGoals = this.data.suitableGoals;
    suitableGoals[index] = value;
    this.setData({ suitableGoals });
  },

  addGoal() {
    const suitableGoals = this.data.suitableGoals;
    suitableGoals.push('减脂');
    this.setData({ suitableGoals });
  },

  removeGoal(e) {
    const index = e.currentTarget.dataset.index;
    const suitableGoals = this.data.suitableGoals;
    if (suitableGoals.length > 1) {
      suitableGoals.splice(index, 1);
      this.setData({ suitableGoals });
    }
  },

  addStep() {
    const steps = this.data.steps;
    steps.push('');
    this.setData({ steps });
  },

  removeStep(e) {
    const index = e.currentTarget.dataset.index;
    const steps = this.data.steps;
    if (steps.length > 1) {
      steps.splice(index, 1);
      this.setData({ steps });
    }
  },

  onStepInput(e) {
    const index = e.currentTarget.dataset.index;
    const value = e.detail.value;
    const steps = this.data.steps;
    steps[index] = value;
    this.setData({ steps });
  },

  validate() {
    const data = this.data;
    if (!data.name.trim()) {
      wx.showToast({ title: '请输入食谱名称', icon: 'none' });
      return false;
    }
    if (!data.category) {
      wx.showToast({ title: '请选择分类', icon: 'none' });
      return false;
    }
    if (data.calories <= 0) {
      wx.showToast({ title: '请输入热量', icon: 'none' });
      return false;
    }
    const validIngredients = data.ingredients.filter(i => i.trim()).length;
    if (validIngredients === 0) {
      wx.showToast({ title: '请至少添加一个食材', icon: 'none' });
      return false;
    }
    const validSteps = data.steps.filter(s => s.trim()).length;
    if (validSteps === 0) {
      wx.showToast({ title: '请至少添加一个步骤', icon: 'none' });
      return false;
    }
    return true;
  },

  submit() {
    if (!this.validate()) return;
    if (this.data.loading) return;

    this.setData({ loading: true });
    wx.showLoading({ title: '提交中...' });

    const data = this.data;
    const recipeData = {
      name: data.name,
      category: data.category,
      image: data.image,
      description: data.description,
      calories: data.calories,
      protein: data.protein,
      carbs: data.carbs,
      fat: data.fat,
      ingredients: data.ingredients.filter(i => i.trim()),
      tags: data.tags.filter(t => t.trim()),
      suitableGoals: [...new Set(data.suitableGoals)],
      steps: data.steps.filter(s => s.trim())
    };

    if (this.data.id) {
      api.put(`/recipes/${this.data.id}`, recipeData)
        .then(res => {
          wx.hideLoading();
          if (res.code === 200) {
            wx.showToast({ title: '提交成功', icon: 'success' });
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          } else {
            wx.showToast({ title: res.message || '提交失败', icon: 'none' });
          }
        })
        .catch(err => {
          wx.hideLoading();
          console.error(err);
          wx.showToast({ title: '提交失败', icon: 'none' });
        })
        .finally(() => {
          this.setData({ loading: false });
        });
    } else {
      api.post('/recipes', recipeData)
        .then(res => {
          wx.hideLoading();
          if (res.code === 200) {
            wx.showToast({ title: '提交成功', icon: 'success' });
            setTimeout(() => {
              wx.navigateBack();
            }, 1500);
          } else {
            wx.showToast({ title: res.message || '提交失败', icon: 'none' });
          }
        })
        .catch(err => {
          wx.hideLoading();
          console.error(err);
          wx.showToast({ title: '提交失败', icon: 'none' });
        })
        .finally(() => {
          this.setData({ loading: false });
        });
    }
  }
});
