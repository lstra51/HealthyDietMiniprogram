const api = require('../../../utils/api.js');

const CATEGORIES = ['蔬菜', '肉类', '海鲜', '主食', '蛋类', '汤类'];
const GOALS = ['减脂', '增肌', '保持'];

Page({
  data: {
    id: null,
    isAdminMode: false,
    name: '',
    category: '蔬菜',
    categories: CATEGORIES,
    image: '',
    description: '',
    calories: 0,
    protein: 0,
    carbs: 0,
    fat: 0,
    ingredients: [''],
    tags: [''],
    suitableGoals: ['减脂'],
    goals: GOALS,
    steps: [''],
    loading: false,
    submitButtonText: '提交食谱'
  },

  onLoad(options) {
    const isAdminMode = options.mode === 'admin';
    this.setData({
      id: options.id || null,
      isAdminMode,
      submitButtonText: options.id ? '保存食谱' : '提交食谱'
    });

    wx.setNavigationBarTitle({
      title: options.id ? '编辑食谱' : '上传食谱'
    });

    if (options.id) {
      this.loadRecipeDetail();
    }
  },

  loadRecipeDetail() {
    api.get(`/recipes/${this.data.id}`).then(res => {
      if (res.code === 200) {
        const recipe = res.data;
        this.setData({
          name: recipe.name || '',
          category: recipe.category || '蔬菜',
          image: api.formatImageUrl(recipe.image),
          description: recipe.description || '',
          calories: recipe.calories || 0,
          protein: recipe.protein || 0,
          carbs: recipe.carbs || 0,
          fat: recipe.fat || 0,
          ingredients: recipe.ingredients && recipe.ingredients.length > 0 ? recipe.ingredients : [''],
          tags: recipe.tags && recipe.tags.length > 0 ? recipe.tags : [''],
          suitableGoals: recipe.suitableGoals && recipe.suitableGoals.length > 0 ? recipe.suitableGoals : ['减脂'],
          steps: recipe.steps && recipe.steps.length > 0 ? recipe.steps : ['']
        });
      }
    }).catch(err => {
      console.error('加载食谱详情失败:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  onCategoryChange(e) {
    const index = Number(e.detail.value);
    this.setData({ category: this.data.categories[index] });
  },

  clearImage() {
    this.setData({ image: '' });
  },

  onNumberInput(e) {
    const field = e.currentTarget.dataset.field;
    const value = parseFloat(e.detail.value);
    this.setData({ [field]: Number.isNaN(value) ? 0 : value });
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
        if (res.code === 200) {
          this.setData({ image: res.data.url });
          wx.showToast({ title: '上传成功', icon: 'success' });
        } else {
          wx.showToast({ title: res.message || '上传失败', icon: 'none' });
        }
      })
      .catch(err => {
        console.error(err);
        wx.showToast({ title: '上传失败', icon: 'none' });
      })
      .finally(() => {
        wx.hideLoading();
      });
  },

  addIngredient() {
    this.setData({ ingredients: [...this.data.ingredients, ''] });
  },

  removeIngredient(e) {
    const index = Number(e.currentTarget.dataset.index);
    if (this.data.ingredients.length <= 1) return;
    const ingredients = this.data.ingredients.slice();
    ingredients.splice(index, 1);
    this.setData({ ingredients });
  },

  onIngredientInput(e) {
    const index = Number(e.currentTarget.dataset.index);
    const ingredients = this.data.ingredients.slice();
    ingredients[index] = e.detail.value;
    this.setData({ ingredients });
  },

  addTag() {
    this.setData({ tags: [...this.data.tags, ''] });
  },

  removeTag(e) {
    const index = Number(e.currentTarget.dataset.index);
    if (this.data.tags.length <= 1) return;
    const tags = this.data.tags.slice();
    tags.splice(index, 1);
    this.setData({ tags });
  },

  onTagInput(e) {
    const index = Number(e.currentTarget.dataset.index);
    const tags = this.data.tags.slice();
    tags[index] = e.detail.value;
    this.setData({ tags });
  },

  onGoalChange(e) {
    const index = Number(e.currentTarget.dataset.index);
    const suitableGoals = this.data.suitableGoals.slice();
    suitableGoals[index] = this.data.goals[Number(e.detail.value)] || '减脂';
    this.setData({ suitableGoals });
  },

  addGoal() {
    this.setData({ suitableGoals: [...this.data.suitableGoals, '减脂'] });
  },

  removeGoal(e) {
    const index = Number(e.currentTarget.dataset.index);
    if (this.data.suitableGoals.length <= 1) return;
    const suitableGoals = this.data.suitableGoals.slice();
    suitableGoals.splice(index, 1);
    this.setData({ suitableGoals });
  },

  addStep() {
    this.setData({ steps: [...this.data.steps, ''] });
  },

  removeStep(e) {
    const index = Number(e.currentTarget.dataset.index);
    if (this.data.steps.length <= 1) return;
    const steps = this.data.steps.slice();
    steps.splice(index, 1);
    this.setData({ steps });
  },

  onStepInput(e) {
    const index = Number(e.currentTarget.dataset.index);
    const steps = this.data.steps.slice();
    steps[index] = e.detail.value;
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
      wx.showToast({ title: '请输入正确的热量', icon: 'none' });
      return false;
    }
    if (data.ingredients.filter(item => item.trim()).length === 0) {
      wx.showToast({ title: '请至少填写一种食材', icon: 'none' });
      return false;
    }
    if (data.steps.filter(item => item.trim()).length === 0) {
      wx.showToast({ title: '请至少填写一个步骤', icon: 'none' });
      return false;
    }
    return true;
  },

  submit() {
    if (!this.validate() || this.data.loading) return;

    this.setData({ loading: true });
    wx.showLoading({ title: '提交中...' });

    const data = this.data;
    const recipeData = {
      name: data.name.trim(),
      category: data.category,
      image: data.image,
      description: data.description.trim(),
      calories: data.calories,
      protein: data.protein,
      carbs: data.carbs,
      fat: data.fat,
      ingredients: data.ingredients.map(item => item.trim()).filter(Boolean),
      tags: data.tags.map(item => item.trim()).filter(Boolean),
      suitableGoals: [...new Set(data.suitableGoals.map(item => item.trim()).filter(Boolean))],
      steps: data.steps.map(item => item.trim()).filter(Boolean)
    };

    let requestPromise;
    if (this.data.id && this.data.isAdminMode) {
      requestPromise = api.put(`/recipes/${this.data.id}/admin`, recipeData);
    } else if (this.data.id) {
      requestPromise = api.put(`/recipes/${this.data.id}`, recipeData);
    } else {
      requestPromise = api.post('/recipes', recipeData);
    }

    requestPromise
      .then(res => {
        if (res.code === 200) {
          const successText = this.data.id ? '保存成功' : '提交成功';
          wx.showToast({ title: successText, icon: 'success' });
          setTimeout(() => {
            wx.navigateBack();
          }, 1200);
        } else {
          wx.showToast({ title: res.message || '提交失败', icon: 'none' });
        }
      })
      .catch(err => {
        console.error(err);
        wx.showToast({ title: '提交失败', icon: 'none' });
      })
      .finally(() => {
        wx.hideLoading();
        this.setData({ loading: false });
      });
  }
});
