const app = getApp();
const api = require('../../utils/api.js');

Page({
  data: {
    period: 'weekly',
    statistics: null,
    trendChart: null,
    pieChart: null,
    avgCaloriesDisplay: '0',
    avgProteinDisplay: '0.0',
    avgCarbsDisplay: '0.0',
    avgFatDisplay: '0.0'
  },

  onLoad() {
    if (!app.globalData.isLoggedIn) {
      wx.showModal({
        title: '需要登录',
        content: '此功能需要登录后才能使用',
        showCancel: false,
        success: () => {
          wx.navigateBack();
        }
      });
      return;
    }
    this.loadStatistics();
  },

  onReady() {
  },

  async switchPeriod(e) {
    const period = e.currentTarget.dataset.period;
    this.setData({ period });
    await this.loadStatistics();
  },

  async loadStatistics() {
    wx.showLoading({ title: '加载中...' });
    try {
      const userId = app.globalData.userInfo.id;
      const endpoint = this.data.period === 'weekly' 
        ? '/statistics/weekly/' + userId 
        : '/statistics/monthly/' + userId;
      
      const res = await api.get(endpoint);
      if (res.code === 200) {
        const stats = res.data;
        this.setData({ statistics: stats });
        this.formatDisplayData(stats);
        this.initCharts();
      }
    } catch (error) {
      console.error('加载统计数据失败', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    } finally {
      wx.hideLoading();
    }
  },

  formatDisplayData(stats) {
    if (!stats) {
      this.setData({
        avgCaloriesDisplay: '0',
        avgProteinDisplay: '0.0',
        avgCarbsDisplay: '0.0',
        avgFatDisplay: '0.0'
      });
      return;
    }

    const avgCalories = stats.avgCalories || 0;
    const avgProtein = stats.avgProtein || 0;
    const avgCarbs = stats.avgCarbs || 0;
    const avgFat = stats.avgFat || 0;

    this.setData({
      avgCaloriesDisplay: avgCalories.toFixed(0),
      avgProteinDisplay: avgProtein.toFixed(1),
      avgCarbsDisplay: avgCarbs.toFixed(1),
      avgFatDisplay: avgFat.toFixed(1)
    });
  },

  async initCharts() {
    const { statistics } = this.data;
    if (!statistics || !statistics.trend) return;

    await this.drawTrendChart(statistics.trend);
    await this.drawPieChart(statistics.pieData);
  },

  async drawTrendChart(trend) {
    const query = wx.createSelectorQuery();
    query.select('#trendChart')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (!res[0]) return;
        
        const canvas = res[0].node;
        const ctx = canvas.getContext('2d');
        const dpr = wx.getSystemInfoSync().pixelRatio;
        
        canvas.width = res[0].width * dpr;
        canvas.height = res[0].height * dpr;
        ctx.scale(dpr, dpr);

        const width = res[0].width;
        const height = res[0].height;
        const padding = { top: 30, right: 20, bottom: 40, left: 50 };
        const chartWidth = width - padding.left - padding.right;
        const chartHeight = height - padding.top - padding.bottom;

        ctx.clearRect(0, 0, width, height);

        const maxCalories = Math.max(...trend.calories, 1);
        const dates = trend.dates.map(d => {
          const month = d.split('-')[1];
          const day = d.split('-')[2];
          return `${month}-${day}`;
        });

        ctx.strokeStyle = '#e0e0e0';
        ctx.lineWidth = 1;
        for (let i = 0; i <= 4; i++) {
          const y = padding.top + (chartHeight / 4) * i;
          ctx.beginPath();
          ctx.moveTo(padding.left, y);
          ctx.lineTo(width - padding.right, y);
          ctx.stroke();
        }

        ctx.fillStyle = '#999';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'right';
        for (let i = 0; i <= 4; i++) {
          const value = Math.round(maxCalories - (maxCalories / 4) * i);
          const y = padding.top + (chartHeight / 4) * i;
          ctx.fillText(value.toString(), padding.left - 5, y + 3);
        }

        const points = trend.calories.map((cal, i) => {
          const x = padding.left + (chartWidth / (trend.calories.length - 1)) * i;
          const y = padding.top + chartHeight - (cal / maxCalories) * chartHeight;
          return { x, y };
        });

        ctx.beginPath();
        ctx.strokeStyle = '#4CAF50';
        ctx.lineWidth = 2;
        ctx.lineJoin = 'round';
        points.forEach((point, i) => {
          if (i === 0) {
            ctx.moveTo(point.x, point.y);
          } else {
            ctx.lineTo(point.x, point.y);
          }
        });
        ctx.stroke();

        points.forEach((point) => {
          ctx.beginPath();
          ctx.arc(point.x, point.y, 4, 0, Math.PI * 2);
          ctx.fillStyle = '#4CAF50';
          ctx.fill();
        });

        ctx.fillStyle = '#666';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'center';
        
        const totalDates = dates.length;
        let step = 1;
        
        if (totalDates > 7) {
          step = Math.ceil(totalDates / 7);
        }
        
        dates.forEach((date, i) => {
          if (i % step === 0 || i === totalDates - 1) {
            const x = padding.left + (chartWidth / (dates.length - 1)) * i;
            ctx.fillText(date, x, height - padding.bottom + 15);
          }
        });
      });
  },

  async drawPieChart(pieData) {
    if (!pieData || pieData.length === 0) return;

    const query = wx.createSelectorQuery();
    query.select('#pieChart')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (!res[0]) return;
        
        const canvas = res[0].node;
        const ctx = canvas.getContext('2d');
        const dpr = wx.getSystemInfoSync().pixelRatio;
        
        canvas.width = res[0].width * dpr;
        canvas.height = res[0].height * dpr;
        ctx.scale(dpr, dpr);

        const width = res[0].width;
        const height = res[0].height;
        const centerX = width / 2;
        const centerY = height / 2 - 10;
        const radius = Math.min(width, height) / 2 - 40;

        ctx.clearRect(0, 0, width, height);

        const colors = ['#4CAF50', '#8BC34A', '#FFC107'];
        let startAngle = -Math.PI / 2;

        pieData.forEach((item, i) => {
          const sliceAngle = (item.percentage / 100) * Math.PI * 2;
          const endAngle = startAngle + sliceAngle;

          ctx.beginPath();
          ctx.moveTo(centerX, centerY);
          ctx.arc(centerX, centerY, radius, startAngle, endAngle);
          ctx.closePath();
          ctx.fillStyle = colors[i];
          ctx.fill();

          startAngle = endAngle;
        });

        const legendY = height - 25;
        pieData.forEach((item, i) => {
          const legendX = 20 + i * (width / 3);
          
          ctx.fillStyle = colors[i];
          ctx.fillRect(legendX, legendY, 12, 12);
          
          ctx.fillStyle = '#333';
          ctx.font = '12px sans-serif';
          ctx.textAlign = 'left';
          ctx.fillText(`${item.name} ${item.percentage.toFixed(1)}%`, legendX + 18, legendY + 10);
        });
      });
  }
});
