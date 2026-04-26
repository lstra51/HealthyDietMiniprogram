const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    messages: [],
    inputText: '',
    loading: false,
    scrollToView: '',
    canSend: false,
    socketTask: null,
    isStreaming: false,
    currentAssistantMessage: ''
  },

  onLoad() {
    if (!this.checkNeedLogin()) return;
    if (app.globalData.chatMessages && app.globalData.chatMessages.length > 0) {
      this.setData({
        messages: app.globalData.chatMessages
      });
    } else {
      const defaultMessages = [
        {
          role: 'assistant',
          content: '你好！我是你的AI营养师，有什么可以帮助你的吗？'
        }
      ];
      this.setData({
        messages: defaultMessages
      });
      app.globalData.chatMessages = defaultMessages;
    }
  },

  checkNeedLogin() {
    if (!app.globalData.isLoggedIn) {
      wx.showModal({
        title: '需要登录',
        content: '此功能需要登录后才能使用，是否前往登录？',
        confirmText: '去登录',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({
              url: '/pages/auth/login/login'
            });
          } else {
            wx.navigateBack();
          }
        }
      });
      return false;
    }
    return true;
  },

  onShow() {
    if (!app.globalData.isLoggedIn) {
      this.clearData();
      return;
    }
    if (app.globalData.chatMessages && app.globalData.chatMessages.length > 0) {
      this.setData({
        messages: app.globalData.chatMessages
      });
    }
  },

  clearData() {
    this.setData({
      messages: [],
      inputText: '',
      loading: false,
      scrollToView: '',
      canSend: false,
      socketTask: null,
      isStreaming: false,
      currentAssistantMessage: ''
    });
    if (app.globalData.chatMessages) {
      app.globalData.chatMessages = null;
    }
  },

  onUnload() {
    this.closeSocket();
  },

  onHide() {
    this.closeSocket();
  },

  onInput(e) {
    const text = e.detail.value;
    this.setData({
      inputText: text,
      canSend: text.trim().length > 0
    });
  },

  sendMessage() {
    const text = this.data.inputText.trim();
    if (!text || this.data.loading || this.data.isStreaming || !this.data.canSend) return;

    const userMessage = {
      role: 'user',
      content: text
    };

    const newMessages = [...this.data.messages, userMessage];
    this.setData({
      messages: newMessages,
      inputText: '',
      loading: true,
      canSend: false,
      isStreaming: true,
      currentAssistantMessage: ''
    });
    app.globalData.chatMessages = newMessages;

    this.scrollToBottom();
    this.connectWebSocket(text);
  },

  sendWeeklyAdvice() {
    if (this.data.loading || this.data.isStreaming) return;
    this.setData({
      inputText: '请结合我近7天饮食记录，给出今日饮食调整建议和接下来三天的注意事项。',
      canSend: true
    });
    this.sendMessage();
  },

  connectWebSocket(userMessage) {
    const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null;
    const token = app.globalData.userInfo && app.globalData.userInfo.token ? app.globalData.userInfo.token : '';
    const history = this.data.messages.slice(0, -1).map(msg => ({
      role: msg.role,
      content: msg.content
    }));

    const socketUrl = api.BASE_URL
      .replace(/^https:/, 'wss:')
      .replace(/^http:/, 'ws:') + '/ai/chat/ws?token=' + encodeURIComponent(token);

    const socketTask = wx.connectSocket({
      url: socketUrl,
      fail: () => {
        this.sendMessageFallback(userMessage);
      }
    });

    socketTask.onOpen(() => {
      socketTask.send({
        data: JSON.stringify({
          userId,
          message: userMessage,
          history
        }),
        fail: () => {
          this.sendMessageFallback(userMessage);
        }
      });
    });

    socketTask.onMessage((res) => {
      try {
        const data = JSON.parse(res.data);
        if (data.type === 'message') {
          this.appendAssistantContent(data.content || '');
        } else if (data.type === 'complete') {
          this.handleComplete();
          this.scrollToBottom();
        } else if (data.type === 'error') {
          this.handleError(data.content || '发送失败，请重试');
        }
      } catch (e) {
        this.handleError('解析消息失败');
      }
    });

    socketTask.onError(() => {
      if (this.data.isStreaming) {
        this.sendMessageFallback(userMessage);
      }
    });

    socketTask.onClose(() => {
      if (this.data.isStreaming && this.data.currentAssistantMessage) {
        this.handleComplete();
      }
    });

    this.setData({
      socketTask: socketTask
    });
  },

  appendAssistantContent(content) {
    if (!content) return;
    if (this.data.loading) {
      this.setData({ loading: false });
    }
    this.setData({
      currentAssistantMessage: this.data.currentAssistantMessage + content
    });

    const tempMessages = [...this.data.messages];
    if (tempMessages.length > 0 && tempMessages[tempMessages.length - 1].role === 'assistant') {
      tempMessages[tempMessages.length - 1].content = this.data.currentAssistantMessage;
    } else {
      tempMessages.push({
        role: 'assistant',
        content: this.data.currentAssistantMessage
      });
    }

    this.setData({
      messages: tempMessages
    });
    app.globalData.chatMessages = tempMessages;
    this.scrollToBottom();
  },

  handleComplete() {
    this.setData({
      loading: false,
      isStreaming: false,
      socketTask: null
    });
  },

  handleError(errorMsg) {
    if (!this.data.isStreaming) return;
    
    wx.showToast({
      title: errorMsg || '发送失败，请重试',
      icon: 'none'
    });
    
    this.setData({
      loading: false,
      isStreaming: false,
      socketTask: null
    });
  },

  closeSocket() {
    if (this.data.socketTask) {
      if (this.data.socketTask.abort) {
        this.data.socketTask.abort();
      } else if (this.data.socketTask.close) {
        this.data.socketTask.close();
      }
      this.setData({
        socketTask: null
      });
    }
  },

  sendMessageFallback(userMessage) {
    console.log('使用备用方式发送消息');
    this.closeSocket();
    
    this.setData({
      loading: true
    });
    
    (async () => {
      try {
        const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null;
        
        const history = this.data.messages.slice(0, -1).map(msg => ({
          role: msg.role,
          content: msg.content
        }));

        const res = await api.post('/ai/chat', {
          userId: userId,
          message: userMessage,
          history: history
        });

        if (res.code === 200) {
          const assistantMessage = {
            role: 'assistant',
            content: res.data
          };
          const updatedMessages = [...this.data.messages, assistantMessage];
          this.setData({
            messages: updatedMessages
          });
          app.globalData.chatMessages = updatedMessages;
        } else {
          throw new Error(res.message || '请求失败');
        }
      } catch (error) {
        console.error('Chat error:', error);
        wx.showToast({
          title: '发送失败，请重试',
          icon: 'none'
        });
      } finally {
        this.handleComplete();
        this.scrollToBottom();
      }
    })();
  },

  scrollToBottom() {
    setTimeout(() => {
      this.setData({
        scrollToView: 'msg-' + (this.data.messages.length - 1)
      });
    }, 100);
  }
});
