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

  onShow() {
    if (app.globalData.chatMessages && app.globalData.chatMessages.length > 0) {
      this.setData({
        messages: app.globalData.chatMessages
      });
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
      loading: false,
      canSend: false,
      isStreaming: true,
      currentAssistantMessage: ''
    });
    app.globalData.chatMessages = newMessages;

    this.scrollToBottom();
    this.connectWebSocket(text);
  },

  connectWebSocket(userMessage) {
    const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null;
    const history = this.data.messages.slice(0, -1).map(msg => ({
      role: msg.role,
      content: msg.content
    }));

    const socketUrl = 'ws://localhost:8080/api/ai/chat/ws';
    
    const socketTask = wx.connectSocket({
      url: socketUrl,
      protocols: [],
      success: () => {
        console.log('WebSocket连接成功');
      },
      fail: (err) => {
        console.error('WebSocket连接失败', err);
        this.sendMessageFallback(userMessage);
      }
    });

    socketTask.onOpen(() => {
      console.log('WebSocket连接已打开');
      
      const requestData = {
        userId: userId,
        message: userMessage,
        history: history
      };
      
      socketTask.send({
        data: JSON.stringify(requestData),
        success: () => {
          console.log('消息发送成功');
        },
        fail: (err) => {
          console.error('消息发送失败', err);
          this.handleError();
        }
      });
    });

    socketTask.onMessage((res) => {
      try {
        const data = JSON.parse(res.data);
        
        if (data.type === 'message') {
          this.setData({
            currentAssistantMessage: this.data.currentAssistantMessage + data.content
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
        } else if (data.type === 'complete') {
          this.handleComplete();
          this.scrollToBottom();
        } else if (data.type === 'error') {
          console.error('AI错误:', data.content);
          this.handleError(data.content);
        }
      } catch (e) {
        console.error('解析消息失败', e);
      }
    });

    socketTask.onError((err) => {
      console.error('WebSocket错误', err);
      this.handleError();
    });

    socketTask.onClose(() => {
      console.log('WebSocket连接已关闭');
      this.handleComplete();
    });

    this.setData({
      socketTask: socketTask
    });
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
      this.data.socketTask.close();
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
