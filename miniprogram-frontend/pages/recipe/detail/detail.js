const app = getApp();
const api = require('../../../utils/api.js');

Page({
  data: {
    recipe: null,
    showRecordModal: false,
    showShareModal: false,
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
        var recipe = api.formatRecipeImage(res.data);
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

  async showShareModal() {
    this.setData({ showShareModal: true });
    await this.drawPoster();
  },

  hideShareModal() {
    this.setData({ showShareModal: false });
  },

  async drawPoster() {
    const { recipe } = this.data;
    if (!recipe) return;

    const query = wx.createSelectorQuery();
    query.select('#posterCanvas')
      .fields({ node: true, size: true })
      .exec(async (res) => {
        if (!res[0]) return;
        
        const canvas = res[0].node;
        const ctx = canvas.getContext('2d');
        const dpr = wx.getSystemInfoSync().pixelRatio;
        
        canvas.width = res[0].width * dpr;
        canvas.height = res[0].height * dpr;
        ctx.scale(dpr, dpr);

        const width = res[0].width;
        const height = res[0].height;

        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, width, height);

        try {
          const img = canvas.createImage();
          await new Promise((resolve) => {
            img.onload = resolve;
            img.onerror = resolve;
            img.src = api.formatImageUrl(recipe.image) || 'https://via.placeholder.com/400';
          });
          ctx.drawImage(img, 0, 0, width, 300);
        } catch (e) {
          ctx.fillStyle = '#4CAF50';
          ctx.fillRect(0, 0, width, 300);
          ctx.fillStyle = '#ffffff';
          ctx.font = '80px sans-serif';
          ctx.textAlign = 'center';
          ctx.fillText('🥗', width / 2, 180);
        }

        ctx.fillStyle = '#333';
        ctx.font = 'bold 32px sans-serif';
        ctx.textAlign = 'left';
        ctx.fillText(recipe.name, 20, 350);

        ctx.fillStyle = '#666';
        ctx.font = '24px sans-serif';
        ctx.fillText(`热量: ${recipe.calories}kcal`, 20, 390);

        const cardY = 410;
        const cardHeight = 60;
        const cardWidth = (width - 60) / 3;

        ctx.fillStyle = '#4CAF50';
        ctx.fillRect(20, cardY, cardWidth, cardHeight);
        ctx.fillStyle = '#fff';
        ctx.font = '20px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(`蛋白质 ${recipe.protein}g`, 20 + cardWidth / 2, cardY + 38);

        ctx.fillStyle = '#8BC34A';
        ctx.fillRect(30 + cardWidth, cardY, cardWidth, cardHeight);
        ctx.fillStyle = '#fff';
        ctx.font = '20px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(`碳水 ${recipe.carbs}g`, 30 + cardWidth + cardWidth / 2, cardY + 38);

        ctx.fillStyle = '#FFC107';
        ctx.fillRect(40 + cardWidth * 2, cardY, cardWidth, cardHeight);
        ctx.fillStyle = '#fff';
        ctx.font = '20px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(`脂肪 ${recipe.fat}g`, 40 + cardWidth * 2 + cardWidth / 2, cardY + 38);

        ctx.fillStyle = '#f5f5f5';
        ctx.fillRect(0, height - 120, width, 120);
        
        ctx.fillStyle = '#4CAF50';
        ctx.font = 'bold 28px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('健康饮食推荐助手', width / 2, height - 65);
        
        ctx.fillStyle = '#999';
        ctx.font = '20px sans-serif';
        ctx.fillText('扫码查看更多食谱', width / 2, height - 30);
      });
  },

  async savePoster() {
    try {
      const query = wx.createSelectorQuery();
      query.select('#posterCanvas')
        .fields({ node: true, size: true })
        .exec(async (res) => {
          if (!res[0]) return;
          
          const canvas = res[0].node;
          wx.canvasToTempFilePath({
            canvas: canvas,
            success: (fileRes) => {
              wx.saveImageToPhotosAlbum({
                filePath: fileRes.tempFilePath,
                success: () => {
                  wx.showToast({
                    title: '保存成功',
                    icon: 'success'
                  });
                  this.hideShareModal();
                },
                fail: (err) => {
                  if (err.errMsg.includes('auth deny')) {
                    wx.showModal({
                      title: '提示',
                      content: '需要您授权保存图片到相册',
                      confirmText: '去授权',
                      success: (modalRes) => {
                        if (modalRes.confirm) {
                          wx.openSetting();
                        }
                      }
                    });
                  } else {
                    wx.showToast({
                      title: '保存失败',
                      icon: 'none'
                    });
                  }
                }
              });
            },
            fail: () => {
              wx.showToast({
                title: '生成海报失败',
                icon: 'none'
              });
            }
          }, this);
        });
    } catch (e) {
      wx.showToast({
        title: '保存失败',
        icon: 'none'
      });
    }
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
      var recordDate = app.formatLocalDate(new Date());
      var res = await api.post('/records', {
        userId: userId,
        recipeId: recipe.id,
        recipeName: recipe.name,
        mealType: mealType,
        portion: portion,
        calories: recipe.calories,
        protein: recipe.protein,
        carbs: recipe.carbs,
        fat: recipe.fat,
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
