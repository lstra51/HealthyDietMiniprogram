const api = require('../../../utils/api.js');

Page({
  data: {
    username: '',
    password: '',
    confirmPassword: ''
  },

  onUsernameInput(e) {
    this.setData({
      username: e.detail.value
    });
  },

  onPasswordInput(e) {
    this.setData({
      password: e.detail.value
    });
  },

  onConfirmPasswordInput(e) {
    this.setData({
      confirmPassword: e.detail.value
    });
  },

  async onRegister() {
    const { username, password, confirmPassword } = this.data;

    if (!username) {
      wx.showToast({
        title: '用户名不能为空',
        icon: 'none'
      });
      return;
    }

    if (!password) {
      wx.showToast({
        title: '密码不能为空',
        icon: 'none'
      });
      return;
    }

    if (password.length < 6 || password.length > 50) {
      wx.showToast({
        title: '密码长度需为6到50位',
        icon: 'none'
      });
      return;
    }

    if (password !== confirmPassword) {
      wx.showToast({
        title: '两次密码不一致',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '注册中...' });

    try {
      const res = await api.post('/auth/register', { username, password });
      wx.hideLoading();

      if (res.code === 200) {
        wx.showToast({
          title: '注册成功',
          icon: 'success'
        });

        setTimeout(() => {
          wx.navigateBack();
        }, 1000);
      } else {
        wx.showToast({
          title: res.message || '注册失败',
          icon: 'none'
        });
      }
    } catch (err) {
      wx.hideLoading();
      wx.showToast({
        title: err.message || '注册失败，请重试',
        icon: 'none'
      });
      console.error('注册请求失败:', err);
    }
  },

  onBackLogin() {
    wx.navigateBack();
  }
});
