const app = getApp();
const api = require('../../../utils/api.js');

Page({
  data: {
    username: '',
    password: ''
  },

  onLoad() {
    if (app.globalData.isLoggedIn) {
      wx.switchTab({
        url: '/pages/home/home'
      });
    }
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

  async onLogin() {
    const { username, password } = this.data;

    if (!username) {
      wx.showToast({
        title: '请输入用户名',
        icon: 'none'
      });
      return;
    }

    if (!password) {
      wx.showToast({
        title: '请输入密码',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '登录中...' });

    try {
      const res = await api.post('/auth/login', { username, password });
      wx.hideLoading();
      
      if (res.code === 200) {
        const userInfo = res.data;
        app.globalData.userInfo = userInfo;
        app.globalData.isLoggedIn = true;
        wx.setStorageSync('userInfo', userInfo);

        await app.loadHealthInfo(userInfo.id);

        wx.showToast({
          title: '登录成功',
          icon: 'success'
        });

        setTimeout(() => {
          wx.switchTab({
            url: '/pages/home/home'
          });
        }, 1000);
      } else {
        wx.showToast({
          title: res.message || '登录失败',
          icon: 'none'
        });
      }
    } catch (err) {
      wx.hideLoading();
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      });
      console.error('登录请求失败:', err);
    }
  },

  async onWechatLogin() {
    try {
      const profile = await new Promise((resolve, reject) => {
        wx.getUserProfile({
          desc: '用于完善用户资料',
          success: resolve,
          fail: reject
        });
      });

      if (profile && profile.userInfo) {
        this.doWechatLogin(profile.userInfo.nickName, profile.userInfo.avatarUrl);
      } else {
        wx.showToast({
          title: '需要授权才能登录',
          icon: 'none'
        });
      }
    } catch (err) {
      wx.showToast({
        title: '授权失败',
        icon: 'none'
      });
    }
  },

  async doWechatLogin(nickname, avatarUrl) {
    wx.showLoading({ title: '登录中...' });

    try {
      const loginRes = await new Promise((resolve, reject) => {
        wx.login({
          success: resolve,
          fail: reject
        });
      });

      if (!loginRes.code) {
        wx.hideLoading();
        wx.showToast({
          title: '获取登录凭证失败',
          icon: 'none'
        });
        return;
      }

      const res = await api.post('/auth/wechat-login', {
        code: loginRes.code,
        nickname: nickname,
        avatarUrl: avatarUrl
      });

      wx.hideLoading();

      if (res.code === 200) {
        const userInfo = res.data;
        app.globalData.userInfo = userInfo;
        app.globalData.isLoggedIn = true;
        wx.setStorageSync('userInfo', userInfo);

        await app.loadHealthInfo(userInfo.id);

        wx.showToast({
          title: '登录成功',
          icon: 'success'
        });

        setTimeout(() => {
          wx.switchTab({
            url: '/pages/home/home'
          });
        }, 1000);
      } else {
        wx.showToast({
          title: res.message || '登录失败',
          icon: 'none'
        });
      }
    } catch (err) {
      wx.hideLoading();
      wx.showToast({
        title: '网络错误，请稍后重试',
        icon: 'none'
      });
      console.error('微信登录失败:', err);
    }
  },

  onGoRegister() {
    wx.navigateTo({
      url: '/pages/auth/register/register'
    });
  },

  goBack() {
    wx.switchTab({
      url: '/pages/home/home'
    });
  }
});
