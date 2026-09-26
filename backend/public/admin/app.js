// State
let allCategories = [];
let allWallpapers = [];
let allRingtones = [];
let allFestivalEvents = [];
let currentAudio = null;
let currentPlayingId = null;
let adminToken = localStorage.getItem('rewall_admin_token') || null;
let currentUser = null;

// Analytics State & Chart Instances
let currentAnalyticsData = null;
let currentAnalyticsDays = 7;
let trendChartInstance = null;
let categoryChartInstance = null;
let topWallsChartInstance = null;

// Authenticated Fetch Helper
async function authFetch(url, options = {}) {
  const headers = options.headers ? { ...options.headers } : {};
  if (adminToken) {
    headers['Authorization'] = `Bearer ${adminToken}`;
  }

  const response = await fetch(url, { ...options, headers });
  if (response.status === 401) {
    console.warn('Authentication required or session expired.');
    adminToken = null;
    localStorage.removeItem('rewall_admin_token');
    showLoginOverlay();
  }
  return response;
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
  initAuth();
  initNavigation();
  initModals();
  initFileUploads();
  initParallaxPreview();
  initBulkUpload();
  initPushNotifications();
  initAnalyticsTimeframeToggle();
  initBackupRestore();
  initImageCropper();
  initWeatherSimulator();
  initRemoteConfigStudio();
  initDeviceTelemetry();
  initHeroBanners();
  initTrendingTagsStudio();
  initFeatureFlagsSwitchboard();
  initSocialHub();
  initRatingPromptStudio();
  initAdmobStudio();
  initChargingStudio();
  initEdgeLightingStudio();
  initDynamicIslandStudio();
  initAODStudio();
  initCallScreenStudio();
  initDuoStudio();
  initTouchStudio();
  initFingerprintStudio();
  initPersonalizationSuite();
  initFestivalScheduler();
  initCommandPalette();
  checkAuth();
  checkAdbDeviceStatus();
  setInterval(checkAdbDeviceStatus, 12000);
});

// Toast Notification
function showToast(message) {
  const toast = document.getElementById('toast');
  toast.textContent = message;
  toast.classList.add('show');
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}

// Navigation Tabs
function initNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      const tab = item.dataset.tab;
      switchTab(tab);
    });
  });

  document.getElementById('refreshBtn').addEventListener('click', () => {
    loadAllData();
    showToast('Data refreshed!');
  });
}

function switchTab(tabId) {
  document.querySelectorAll('.nav-item').forEach(el => {
    el.classList.toggle('active', el.dataset.tab === tabId);
  });
  document.querySelectorAll('.tab-pane').forEach(el => {
    el.classList.toggle('active', el.id === `pane-${tabId}`);
  });

  const titles = {
    overview: { title: 'Dashboard Overview', sub: 'Live statistics and connected client management' },
    wallpapers: { title: 'Wallpapers & 3D Studio', sub: 'Manage cloud wallpapers, 3D multi-layers, and 4K assets' },
    banners: { title: 'Home Screen Hero Banners', sub: 'Publish auto-sliding promotional banners and festival drops to the Android home screen' },
    charging: { title: 'Charging Animation Studio', sub: 'Upload, customize, and simulate dynamic battery charging effects' },
    'edge-lighting': { title: 'Edge Lighting Studio', sub: 'Create, customize, and preview RGB border neon & camera punch-hole lighting' },
    'dynamic-island': { title: 'Dynamic Island Studio', sub: 'Smart iPhone-style camera punch-hole capsule, music equalizer & live notification alerts' },
    aod: { title: 'Always-On Display (AOD) Studio', sub: 'Manage pitch-black AMOLED battery-saving clocks, real-time widgets, and anti-burn-in protection' },
    callscreen: { title: '3D Color Call Screen & Flash Themes Studio', sub: 'Manage full-screen 3D animated incoming call themes, glowing neon buttons, caller avatars, and camera flashlight alert strobe.' },
    duo: { title: '👥 Duo / Double Wallpapers Studio', sub: 'Curate matching 4K/3D Lock Screen and Home Screen magic pairs with unlock transitions' },
    'touch-effects': { title: '👆 Touch Fluid & Ripple Effects Studio', sub: 'Touch-reactive fluid dye swirls, water ripples, electric arcs, stardust and gravity vortexes' },
    fingerprint: { title: '🔓 In-Display Fingerprint Animations Studio', sub: 'Simulate and customize biometric fingerprint sensor animations, holographic HUD rings, cosmic bursts & laser scans' },
    categories: { title: 'Category Management', sub: 'Create and organize wallpaper categories' },
    ringtones: { title: 'Ringtone Catalog', sub: 'Upload and preview high-fidelity audio ringtones' },
    notifications: { title: 'Push Notification Studio', sub: 'Broadcast instant alerts, wallpaper highlights, and daily picks to all app users' },
    config: { title: 'Live Remote Flags & Weather Studio', sub: 'Dynamic weather overlays, announcement banners, force update, and monetization' },
    devices: { title: 'Connected Devices & Geo Radar', sub: 'Real-time telemetry, Android OS distribution, and live global map' },
    'suite-manager': { title: '⚡ Flagship Personalization Suite Manager', sub: 'Over-the-Air (OTA) control for the 8 flagship modules: toggle ON/OFF, badges & reordering' },
    festivals: { title: '🏷️ Festival & Event Banner Scheduler', sub: 'Automate seasonal festival banners, special categories, and trending tags with live date-range countdowns' }
  };

  if (titles[tabId]) {
    document.getElementById('pageTitle').textContent = titles[tabId].title;
    document.getElementById('pageSubtitle').textContent = titles[tabId].sub;
  }

  if (tabId === 'notifications') {
    loadNotifications();
  } else if (tabId === 'devices') {
    loadTelemetryData();
  } else if (tabId === 'banners') {
    loadBanners();
  } else if (tabId === 'festivals') {
    loadFestivalEvents();
  } else if (tabId === 'charging') {
    loadChargingAnimations();
  } else if (tabId === 'edge-lighting') {
    loadEdgeLightingPresets();
  } else if (tabId === 'dynamic-island') {
    loadDynamicIslandThemes();
  } else if (tabId === 'aod') {
    loadAODClocks();
  } else if (tabId === 'callscreen') {
    loadCallScreenThemes();
  } else if (tabId === 'duo') {
    loadDuoWallpapers();
  } else if (tabId === 'touch-effects') {
    loadTouchPresets();
  } else if (tabId === 'fingerprint') {
    loadFingerprintPresets();
  } else if (tabId === 'suite-manager') {
    loadPersonalizationSuite();
  } else if (tabId === 'config') {
    loadTrendingTags();
    loadFeatureFlags();
    loadSocialHubConfig();
    loadRatingPromptConfig();
    loadAdmobConfig();
  }
}

// ========================================================
// AUTHENTICATION SYSTEM
// ========================================================
function showLoginOverlay() {
  const overlay = document.getElementById('loginOverlay');
  if (overlay) {
    overlay.classList.remove('hidden');
    overlay.style.display = 'flex';
    overlay.style.opacity = '1';
    overlay.style.visibility = 'visible';
    overlay.style.pointerEvents = 'auto';
  }
}

function hideLoginOverlay() {
  const overlay = document.getElementById('loginOverlay');
  if (overlay) {
    overlay.classList.add('hidden');
    overlay.style.opacity = '0';
    overlay.style.visibility = 'hidden';
    overlay.style.pointerEvents = 'none';
    setTimeout(() => {
      if (overlay.classList.contains('hidden')) {
        overlay.style.display = 'none';
      }
    }, 300);
  }
}

async function checkAuth() {
  if (!adminToken) {
    showLoginOverlay();
    return;
  }

  try {
    const res = await authFetch('/api/admin/me');
    const json = await res.json();
    if (json.success && json.user) {
      currentUser = json.user;
      document.getElementById('adminUsernameDisplay').textContent = currentUser.username;
      document.getElementById('adminRoleDisplay').textContent = currentUser.role.toUpperCase();
      hideLoginOverlay();
      loadAllData();
    } else {
      showLoginOverlay();
    }
  } catch (err) {
    console.error('Auth verification failed', err);
    showLoginOverlay();
  }
}

function initAuth() {
  // Login Form
  const loginForm = document.getElementById('loginForm');
  const loginErr = document.getElementById('loginErrorAlert');
  const togglePassBtn = document.getElementById('togglePasswordBtn');
  const passInput = document.getElementById('loginPassword');

  if (togglePassBtn) {
    togglePassBtn.addEventListener('click', () => {
      const type = passInput.getAttribute('type') === 'password' ? 'text' : 'password';
      passInput.setAttribute('type', type);
      togglePassBtn.textContent = type === 'password' ? '👁️' : '🙈';
    });
  }

  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      loginErr.style.display = 'none';
      const username = document.getElementById('loginUsername').value.trim();
      const password = passInput.value;
      const submitBtn = document.getElementById('submitLoginBtn');

      submitBtn.disabled = true;
      submitBtn.innerHTML = 'Signing In...';

      try {
        const res = await fetch('/api/admin/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ username, password })
        });
        const json = await res.json();
        if (json.success) {
          adminToken = json.token;
          currentUser = json.user;
          localStorage.setItem('rewall_admin_token', adminToken);
          document.getElementById('adminUsernameDisplay').textContent = currentUser.username;
          document.getElementById('adminRoleDisplay').textContent = currentUser.role.toUpperCase();
          hideLoginOverlay();
          showToast(`Welcome back, ${currentUser.username}!`);
          loadAllData();
        } else {
          loginErr.textContent = json.error || 'Invalid credentials. Please try again.';
          loginErr.style.display = 'block';
        }
      } catch (err) {
        loginErr.textContent = 'Server connection failed. Is the server running?';
        loginErr.style.display = 'block';
      } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = `
          <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"></path><polyline points="10 17 15 12 10 7"></polyline><line x1="15" y1="12" x2="3" y2="12"></line></svg>
          Sign In to Control Center
        `;
      }
    });
  }

  // Global Sign Out Handler
  window.handleSignOut = function (e) {
    if (e && e.preventDefault) e.preventDefault();
    adminToken = null;
    currentUser = null;
    localStorage.removeItem('rewall_admin_token');

    // Reset password field in login card
    const passInput = document.getElementById('loginPassword');
    if (passInput) passInput.value = '';
    const loginErr = document.getElementById('loginErrorAlert');
    if (loginErr) loginErr.style.display = 'none';

    showLoginOverlay();
    showToast('Signed out of Control Center');
  };

  // Logout button in sidebar footer
  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', (e) => {
      window.handleSignOut(e);
    });
  }

  // Logout button in top header actions
  const headerLogoutBtn = document.getElementById('headerLogoutBtn');
  if (headerLogoutBtn) {
    headerLogoutBtn.addEventListener('click', (e) => {
      window.handleSignOut(e);
    });
  }

  // Change Password Modal & Form
  const changePassModal = document.getElementById('changePasswordModal');
  const openChangeBtn = document.getElementById('openChangePassBtn');
  const closeChangeBtn = document.getElementById('closeChangePassModal');
  const cancelChangeBtn = document.getElementById('cancelChangePassBtn');
  const changePassForm = document.getElementById('changePasswordForm');
  const changePassErr = document.getElementById('changePassErrorAlert');

  if (openChangeBtn) {
    openChangeBtn.addEventListener('click', () => {
      changePassForm.reset();
      changePassErr.style.display = 'none';
      changePassModal.classList.add('active');
    });
  }

  [closeChangeBtn, cancelChangeBtn].forEach(btn => {
    if (btn) {
      btn.addEventListener('click', () => {
        changePassModal.classList.remove('active');
      });
    }
  });

  if (changePassForm) {
    changePassForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      changePassErr.style.display = 'none';
      const currentPassword = document.getElementById('currentPassInput').value;
      const newPassword = document.getElementById('newPassInput').value;
      const confirmPass = document.getElementById('confirmNewPassInput').value;

      if (newPassword !== confirmPass) {
        changePassErr.textContent = 'New passwords do not match!';
        changePassErr.style.display = 'block';
        return;
      }

      try {
        const res = await authFetch('/api/admin/change-password', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ currentPassword, newPassword })
        });
        const json = await res.json();
        if (json.success) {
          showToast('Password successfully changed!');
          changePassModal.classList.remove('active');
        } else {
          changePassErr.textContent = json.error || 'Password update failed';
          changePassErr.style.display = 'block';
        }
      } catch (err) {
        changePassErr.textContent = 'Error: ' + err.message;
        changePassErr.style.display = 'block';
      }
    });
  }
}

// Fetch all core data
async function loadAllData() {
  await Promise.all([
    loadStats(),
    loadAnalyticsData(),
    loadCategories(),
    loadWallpapers(),
    loadRingtones(),
    loadConfig(),
    loadTelemetryData(),
    loadBanners(),
    loadTrendingTags(),
    loadFeatureFlags(),
    loadChargingAnimations(),
    loadEdgeLightingPresets(),
    loadCallScreenThemes(),
    loadFingerprintPresets(),
    loadPersonalizationSuite()
  ]);
}

// 1. STATS
async function loadStats() {
  try {
    const res = await authFetch('/api/admin/stats');
    const json = await res.json();
    if (json.success) {
      const d = json.data;
      document.getElementById('statWallpapers').textContent = (d.totalWallpapers || 0).toLocaleString();
      document.getElementById('statParallax').textContent = (d.totalParallax || 0).toLocaleString();
      document.getElementById('statDownloads').textContent = (d.totalDownloads || 0).toLocaleString();
      document.getElementById('statLikes').textContent = (d.totalLikes || 0).toLocaleString();
    }
  } catch (e) {
    console.error('Stats load error', e);
  }
}

// ==========================================
// VISUAL ANALYTICS & CHART.JS INTEGRATION
// ==========================================

function initAnalyticsTimeframeToggle() {
  const btn7 = document.getElementById('timeframe7Btn');
  const btn14 = document.getElementById('timeframe14Btn');

  if (btn7) {
    btn7.addEventListener('click', () => {
      currentAnalyticsDays = 7;
      btn7.classList.add('active');
      if (btn14) btn14.classList.remove('active');
      renderTrendChart(7);
    });
  }

  if (btn14) {
    btn14.addEventListener('click', () => {
      currentAnalyticsDays = 14;
      btn14.classList.add('active');
      if (btn7) btn7.classList.remove('active');
      renderTrendChart(14);
    });
  }
}

async function loadAnalyticsData() {
  try {
    const res = await authFetch('/api/admin/analytics');
    const json = await res.json();
    if (json.success && json.data) {
      currentAnalyticsData = json.data;
      const s = json.data.summary;

      // Update Summary Badges
      const topCatEl = document.getElementById('anTopCategory');
      const topCatSub = document.getElementById('anTopCatDownloads');
      const topWallEl = document.getElementById('anTopWallpaper');
      const topWallSub = document.getElementById('anTopWallDownloads');
      const parallaxRatioEl = document.getElementById('anParallaxRatio');
      const avgLikesEl = document.getElementById('anAvgLikes');

      if (topCatEl) topCatEl.textContent = s.topCategory || 'N/A';
      if (topCatSub) topCatSub.textContent = `${(s.topCategoryDownloads || 0).toLocaleString()} downloads`;
      if (topWallEl) topWallEl.textContent = s.topWallpaperTitle || 'N/A';
      if (topWallSub) topWallSub.textContent = `${(s.topWallpaperDownloads || 0).toLocaleString()} downloads`;
      if (parallaxRatioEl) parallaxRatioEl.textContent = `${s.parallaxPercent || 0}%`;
      if (avgLikesEl) avgLikesEl.textContent = (s.avgLikesPerWall || 0).toLocaleString();

      // Render Charts
      renderTrendChart(currentAnalyticsDays);
      renderCategoryChart();
      renderTopWallsChart();
    }
  } catch (err) {
    console.error('Failed to load analytics data:', err);
  }
}

function renderTrendChart(days = 7) {
  if (!currentAnalyticsData || !window.Chart) return;
  const canvas = document.getElementById('trendChart');
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  if (trendChartInstance) {
    trendChartInstance.destroy();
    trendChartInstance = null;
  }

  const rawTrend = currentAnalyticsData.dailyTrend || [];
  const trendSlice = rawTrend.slice(-days);

  const labels = trendSlice.map(item => {
    const parts = item.date.split('-');
    const dateObj = new Date(parts[0], parts[1] - 1, parts[2]);
    return dateObj.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  });

  const likesData = trendSlice.map(item => item.newLikes);
  const downloadsData = trendSlice.map(item => item.newDownloads);

  // Gradient Pink (Likes)
  const gradPink = ctx.createLinearGradient(0, 0, 0, 260);
  gradPink.addColorStop(0, 'rgba(236, 72, 153, 0.35)');
  gradPink.addColorStop(1, 'rgba(236, 72, 153, 0.00)');

  // Gradient Cyan (Downloads)
  const gradCyan = ctx.createLinearGradient(0, 0, 0, 260);
  gradCyan.addColorStop(0, 'rgba(0, 229, 255, 0.35)');
  gradCyan.addColorStop(1, 'rgba(0, 229, 255, 0.00)');

  trendChartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: 'Daily Likes',
          data: likesData,
          borderColor: '#EC4899',
          backgroundColor: gradPink,
          borderWidth: 2.5,
          tension: 0.38,
          fill: true,
          pointBackgroundColor: '#EC4899',
          pointBorderColor: '#fff',
          pointBorderWidth: 1.5,
          pointRadius: 3.5,
          pointHoverRadius: 6
        },
        {
          label: 'Daily Downloads',
          data: downloadsData,
          borderColor: '#00E5FF',
          backgroundColor: gradCyan,
          borderWidth: 2.5,
          tension: 0.38,
          fill: true,
          pointBackgroundColor: '#00E5FF',
          pointBorderColor: '#fff',
          pointBorderWidth: 1.5,
          pointRadius: 3.5,
          pointHoverRadius: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#0E1626',
          borderColor: 'rgba(0, 229, 255, 0.3)',
          borderWidth: 1,
          titleColor: '#F8FAFC',
          bodyColor: '#94A3B8',
          padding: 10,
          cornerRadius: 8,
          displayColors: true,
          boxPadding: 4,
          callbacks: {
            label: function (ctx) {
              return `  ${ctx.dataset.label}: ${ctx.parsed.y.toLocaleString()}`;
            }
          }
        }
      },
      scales: {
        x: {
          grid: { color: 'rgba(255, 255, 255, 0.04)' },
          ticks: { color: '#8E99B0', font: { size: 11 } }
        },
        y: {
          grid: { color: 'rgba(255, 255, 255, 0.05)' },
          ticks: {
            color: '#8E99B0',
            font: { size: 11 },
            callback: function (val) {
              return val >= 1000 ? (val / 1000).toFixed(1) + 'k' : val;
            }
          }
        }
      }
    }
  });
}

function renderCategoryChart() {
  if (!currentAnalyticsData || !window.Chart) return;
  const canvas = document.getElementById('categoryChart');
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  if (categoryChartInstance) {
    categoryChartInstance.destroy();
    categoryChartInstance = null;
  }

  const catShare = currentAnalyticsData.categoryShare || [];
  const labels = catShare.map(c => c.category);
  const data = catShare.map(c => c.totalDownloads);

  const colors = [
    '#00E5FF',
    '#8B5CF6',
    '#10B981',
    '#EC4899',
    '#F59E0B',
    '#3B82F6',
    '#06B6D4'
  ];

  categoryChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [
        {
          data,
          backgroundColor: colors.slice(0, data.length),
          borderColor: '#0B0F19',
          borderWidth: 2,
          hoverOffset: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '68%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: '#CBD5E1',
            boxWidth: 12,
            boxHeight: 12,
            font: { size: 11 },
            padding: 10
          }
        },
        tooltip: {
          backgroundColor: '#0E1626',
          borderColor: 'rgba(139, 92, 246, 0.3)',
          borderWidth: 1,
          titleColor: '#F8FAFC',
          bodyColor: '#94A3B8',
          padding: 10,
          cornerRadius: 8,
          callbacks: {
            label: function (ctx) {
              const val = ctx.parsed;
              const cat = catShare[ctx.dataIndex];
              return ` ${val.toLocaleString()} downloads (${cat ? cat.wallpaperCount : 0} walls)`;
            }
          }
        }
      }
    }
  });
}

function renderTopWallsChart() {
  if (!currentAnalyticsData || !window.Chart) return;
  const canvas = document.getElementById('topWallsChart');
  if (!canvas) return;

  const ctx = canvas.getContext('2d');
  if (topWallsChartInstance) {
    topWallsChartInstance.destroy();
    topWallsChartInstance = null;
  }

  const topWalls = currentAnalyticsData.topWallpapers || [];
  const labels = topWalls.map(w => w.title.length > 24 ? w.title.substring(0, 22) + '...' : w.title);
  const downloads = topWalls.map(w => w.downloads);

  // Gradient Bar
  const gradBar = ctx.createLinearGradient(0, 0, 600, 0);
  gradBar.addColorStop(0, '#00E5FF');
  gradBar.addColorStop(1, '#8B5CF6');

  topWallsChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: 'Total Downloads',
          data: downloads,
          backgroundColor: gradBar,
          borderRadius: 6,
          borderSkipped: false,
          maxBarThickness: 24
        }
      ]
    },
    options: {
      indexAxis: 'y',
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#0E1626',
          borderColor: 'rgba(0, 229, 255, 0.3)',
          borderWidth: 1,
          titleColor: '#F8FAFC',
          bodyColor: '#94A3B8',
          padding: 10,
          cornerRadius: 8,
          callbacks: {
            afterLabel: function (ctx) {
              const item = topWalls[ctx.dataIndex];
              return item ? `Likes: ${item.likes.toLocaleString()} | ${item.category} (${item.isParallax ? '3D' : '2D'})` : '';
            }
          }
        }
      },
      scales: {
        x: {
          grid: { color: 'rgba(255, 255, 255, 0.05)' },
          ticks: {
            color: '#8E99B0',
            font: { size: 11 },
            callback: function (val) {
              return val >= 1000 ? (val / 1000).toFixed(0) + 'k' : val;
            }
          }
        },
        y: {
          grid: { display: false },
          ticks: {
            color: '#F1F5F9',
            font: { size: 12, weight: '600' }
          }
        }
      }
    }
  });
}

// 2. CATEGORIES
async function loadCategories() {
  try {
    const res = await fetch('/api/categories');
    const json = await res.json();
    if (json.success) {
      allCategories = json.data;
      renderCategoriesList();
      populateCategoryDropdowns();
    }
  } catch (e) {
    console.error('Categories load error', e);
  }
}

function renderCategoriesList() {
  const container = document.getElementById('categoriesList');
  container.innerHTML = allCategories.map(cat => `
    <div class="category-row">
      <div class="cat-icon-name">
        <img src="${cat.iconUrl || 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png'}" class="cat-icon" onerror="this.src='https://cdn-icons-png.flaticon.com/512/3135/3135715.png'">
        <div>
          <strong>${cat.name}</strong>
          <div style="font-size:11px; color:#8E99B0;">ID: ${cat.id} &bull; ${cat.count || 0} wallpapers</div>
        </div>
      </div>
      <button class="btn btn-sm btn-danger" onclick="deleteCategory('${cat.id}')">Delete</button>
    </div>
  `).join('');
}

function populateCategoryDropdowns() {
  const filter = document.getElementById('wallCategoryFilter');
  const modalSelect = document.getElementById('wpCategory');
  const bulkSelect = document.getElementById('bulkCategory');

  if (filter) {
    filter.innerHTML = '<option value="ALL">All Categories</option>' +
      allCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  }

  if (modalSelect) {
    modalSelect.innerHTML = allCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  }

  if (bulkSelect) {
    bulkSelect.innerHTML = allCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  }

  const batchCatSelect = document.getElementById('batchCategorySelect');
  if (batchCatSelect) {
    batchCatSelect.innerHTML = '<option value="">Move Category...</option>' +
      allCategories.map(c => `<option value="${c.name}">📁 ${c.name}</option>`).join('');
  }
}

// 3. WALLPAPERS & BATCH MULTI-SELECT
let selectedWallpaperIds = new Set();
let currentFilteredWallpapers = [];
let lastCheckedWallpaperIndex = null;

async function loadWallpapers() {
  try {
    const res = await fetch('/api/wallpapers?limit=100');
    const json = await res.json();
    if (json.success) {
      allWallpapers = json.data;
      renderRecentWallpapers();
      renderWallpapersTable();
      populateDailyPickSelect();
      populateNotifWallpaperSelect();
      populateCategoryDropdowns();
    }
  } catch (e) {
    console.error('Wallpapers load error', e);
  }
}

function renderRecentWallpapers() {
  const grid = document.getElementById('recentWallpapersGrid');
  const recent = allWallpapers.slice(0, 6);
  grid.innerHTML = recent.map(w => `
    <div class="wall-card-preview">
      <img src="${w.previewUrl}" class="wall-card-img" alt="${w.title}">
      <div class="wall-card-info">
        <div class="wall-card-title">${w.title}</div>
        <div class="wall-card-meta">
          <span>${w.isParallax ? '⚡ 3D Parallax' : '🖼️ 2D'}</span>
          <span>❤️ ${w.likes.toLocaleString()}</span>
        </div>
      </div>
    </div>
  `).join('');
}

function renderWallpapersTable() {
  const tbody = document.getElementById('wallpapersTableBody');
  const searchInput = document.getElementById('wallSearchInput');
  const searchVal = searchInput ? searchInput.value.toLowerCase() : '';
  const catVal = document.getElementById('wallCategoryFilter').value;
  const typeVal = document.getElementById('wallTypeFilter').value;

  currentFilteredWallpapers = allWallpapers.filter(w => {
    const matchesSearch = w.title.toLowerCase().includes(searchVal) || w.id.toLowerCase().includes(searchVal);
    const matchesCat = catVal === 'ALL' || w.category === catVal;
    const matchesType = typeVal === 'ALL' ||
      (typeVal === 'PARALLAX' && w.isParallax) ||
      (typeVal === 'TWO_D' && !w.isParallax);
    return matchesSearch && matchesCat && matchesType;
  });

  tbody.innerHTML = currentFilteredWallpapers.map((w, idx) => {
    const isChecked = selectedWallpaperIds.has(w.id);
    return `
      <tr class="${isChecked ? 'table-row-selected' : ''}" id="row-wp-${w.id}">
        <td style="text-align: center;">
          <input type="checkbox" class="wallpaper-row-check" data-id="${w.id}" data-index="${idx}" ${isChecked ? 'checked' : ''} onclick="handleWallpaperCheckClick('${w.id}', this, event)">
        </td>
        <td><img src="${w.previewUrl}" class="table-thumb" alt="" onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'40\\' height=\\'40\\' fill=\\'%23334155\\'><rect width=\\'40\\' height=\\'40\\'/></svg>'"></td>
        <td>
          <strong>${w.title}</strong>
          <div style="font-size:11px; color:#8E99B0;">ID: ${w.id}</div>
        </td>
        <td><span class="badge badge-purple">${w.category}</span></td>
        <td>
          ${w.isParallax 
            ? `<span class="badge badge-cyan">3D (${w.layers ? w.layers.length : 0} Layers)</span>` 
            : `<span class="badge badge-gray">2D Static</span>`}
        </td>
        <td>
          <div style="font-size:12px;">⬇️ ${(w.downloads || 0).toLocaleString()} &bull; ❤️ ${(w.likes || 0).toLocaleString()}</div>
        </td>
        <td>
          ${w.isPremium ? `<span class="badge badge-pink">PRO</span>` : `<span class="badge badge-green">FREE</span>`}
        </td>
        <td>
          <div style="display:flex; gap:6px; align-items:center;">
            <button class="btn btn-sm btn-adb-push" onclick="pushToDevice('wallpaper', '${w.id}', event)" title="1-Click Live Test on connected Phone (I2212)">📲 Push to Phone</button>
            <button class="btn btn-sm" style="background:rgba(0,229,255,0.14); border:1px solid rgba(0,229,255,0.45); color:var(--accent-cyan); font-weight:700; padding:4px 8px;" onclick="openParallaxStudio('${w.id}')" title="Launch 3D Live Parallax Gyro Simulator">🎮 3D Studio</button>
            <button class="btn btn-sm btn-secondary" onclick="openEditWallpaperModal('${w.id}')">Edit</button>
            <button class="btn btn-sm btn-danger" onclick="deleteWallpaper('${w.id}')">Delete</button>
          </div>
        </td>
      </tr>
    `;
  }).join('');

  updateSelectAllCheckboxState();
  updateWallpaperBatchBar();
}

// Checkbox selection & Shift+Click range handler
function handleWallpaperCheckClick(id, checkboxEl, event) {
  const currentIndex = parseInt(checkboxEl.dataset.index, 10);
  const isChecked = checkboxEl.checked;

  if (event.shiftKey && lastCheckedWallpaperIndex !== null && lastCheckedWallpaperIndex !== currentIndex) {
    const start = Math.min(lastCheckedWallpaperIndex, currentIndex);
    const end = Math.max(lastCheckedWallpaperIndex, currentIndex);
    for (let i = start; i <= end; i++) {
      const item = currentFilteredWallpapers[i];
      if (item) {
        if (isChecked) {
          selectedWallpaperIds.add(item.id);
        } else {
          selectedWallpaperIds.delete(item.id);
        }
        const row = document.getElementById(`row-wp-${item.id}`);
        if (row) {
          row.classList.toggle('table-row-selected', isChecked);
          const cb = row.querySelector('.wallpaper-row-check');
          if (cb) cb.checked = isChecked;
        }
      }
    }
  } else {
    if (isChecked) {
      selectedWallpaperIds.add(id);
    } else {
      selectedWallpaperIds.delete(id);
    }
    const row = document.getElementById(`row-wp-${id}`);
    if (row) {
      row.classList.toggle('table-row-selected', isChecked);
    }
  }

  lastCheckedWallpaperIndex = currentIndex;
  updateSelectAllCheckboxState();
  updateWallpaperBatchBar();
}

// Master "Select All" checkbox toggle
function toggleSelectAllWallpapers(isChecked) {
  if (isChecked) {
    currentFilteredWallpapers.forEach(w => selectedWallpaperIds.add(w.id));
  } else {
    currentFilteredWallpapers.forEach(w => selectedWallpaperIds.delete(w.id));
  }
  renderWallpapersTable();
}

// Preset batch selection (20, 50, 100)
function selectWallpaperBatch(count) {
  const target = currentFilteredWallpapers.slice(0, count);
  target.forEach(w => selectedWallpaperIds.add(w.id));
  renderWallpapersTable();
  showToast(`📦 Selected ${Math.min(count, target.length)} wallpapers!`);
}

// Select all filtered wallpapers
function selectAllFilteredWallpapers() {
  currentFilteredWallpapers.forEach(w => selectedWallpaperIds.add(w.id));
  renderWallpapersTable();
  showToast(`📦 Selected all ${currentFilteredWallpapers.length} wallpapers!`);
}

// Clear selection
function clearWallpaperSelection() {
  selectedWallpaperIds.clear();
  lastCheckedWallpaperIndex = null;
  renderWallpapersTable();
  showToast('Selection cleared');
}

// Update Master Checkbox indeterminate/checked state
function updateSelectAllCheckboxState() {
  const masterCheck = document.getElementById('selectAllWallpapersCheck');
  if (!masterCheck) return;

  if (currentFilteredWallpapers.length === 0) {
    masterCheck.checked = false;
    masterCheck.indeterminate = false;
    return;
  }

  let selectedInFiltered = 0;
  currentFilteredWallpapers.forEach(w => {
    if (selectedWallpaperIds.has(w.id)) selectedInFiltered++;
  });

  if (selectedInFiltered === currentFilteredWallpapers.length) {
    masterCheck.checked = true;
    masterCheck.indeterminate = false;
  } else if (selectedInFiltered > 0) {
    masterCheck.checked = false;
    masterCheck.indeterminate = true;
  } else {
    masterCheck.checked = false;
    masterCheck.indeterminate = false;
  }
}

// Floating Batch Bar Visibility & Content
function updateWallpaperBatchBar() {
  const bar = document.getElementById('wallpaperBatchBar');
  const countBadge = document.getElementById('batchSelectedCountBadge');
  const clearTopBtn = document.getElementById('clearSelectionTopBtn');
  const count = selectedWallpaperIds.size;

  if (!bar) return;

  if (count > 0) {
    bar.style.display = 'flex';
    if (countBadge) countBadge.textContent = count;
    if (clearTopBtn) clearTopBtn.style.display = 'inline-block';
  } else {
    bar.style.display = 'none';
    if (clearTopBtn) clearTopBtn.style.display = 'none';
  }
}

// 1. Bulk Delete Handlers
function confirmBulkDelete() {
  if (selectedWallpaperIds.size === 0) {
    showToast('Please select at least one wallpaper.');
    return;
  }
  const countText = document.getElementById('bulkDeleteCountText');
  if (countText) countText.textContent = selectedWallpaperIds.size;
  const modal = document.getElementById('bulkDeleteConfirmModal');
  if (modal) modal.classList.add('active');
}

function closeBulkDeleteModal() {
  const modal = document.getElementById('bulkDeleteConfirmModal');
  if (modal) modal.classList.remove('active');
}

async function executeBulkDelete() {
  if (selectedWallpaperIds.size === 0) return;
  const btn = document.getElementById('confirmBulkDeleteBtn');
  if (btn) {
    btn.disabled = true;
    btn.textContent = 'Deleting...';
  }

  try {
    const ids = Array.from(selectedWallpaperIds);
    const res = await authFetch('/api/admin/wallpapers/bulk-delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ids })
    });
    const json = await res.json();
    if (json.success) {
      showToast(`🗑️ ${json.message || 'Wallpapers deleted successfully!'}`);
      selectedWallpaperIds.clear();
      closeBulkDeleteModal();
      await loadWallpapers();
      await loadCategories();
    } else {
      showToast(`Delete failed: ${json.error || 'Server error'}`);
    }
  } catch (err) {
    console.error('Bulk delete error:', err);
    showToast(`Delete error: ${err.message}`);
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.textContent = 'Yes, Delete Wallpapers';
    }
  }
}

// 2. Bulk Category Move Handler
async function executeBulkCategoryMove() {
  if (selectedWallpaperIds.size === 0) {
    showToast('Please select at least one wallpaper.');
    return;
  }
  const select = document.getElementById('batchCategorySelect');
  const targetCategory = select ? select.value : '';

  if (!targetCategory) {
    showToast('⚠️ Please select a target category from the dropdown.');
    if (select) select.focus();
    return;
  }

  const applyBtn = document.getElementById('applyBatchCategoryBtn');
  if (applyBtn) {
    applyBtn.disabled = true;
    applyBtn.textContent = 'Moving...';
  }

  try {
    const ids = Array.from(selectedWallpaperIds);
    const res = await authFetch('/api/admin/wallpapers/bulk-category', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ids, category: targetCategory })
    });
    const json = await res.json();
    if (json.success) {
      showToast(`📁 ${json.message || 'Wallpapers moved successfully!'}`);
      selectedWallpaperIds.clear();
      await loadWallpapers();
      await loadCategories();
    } else {
      showToast(`Move failed: ${json.error || 'Server error'}`);
    }
  } catch (err) {
    console.error('Bulk category error:', err);
    showToast(`Move error: ${err.message}`);
  } finally {
    if (applyBtn) {
      applyBtn.disabled = false;
      applyBtn.textContent = '📁 Move';
    }
  }
}

// 3. Bulk VIP / Free Toggle Handler
async function executeBulkPremiumToggle(isPremium) {
  if (selectedWallpaperIds.size === 0) {
    showToast('Please select at least one wallpaper.');
    return;
  }

  try {
    const ids = Array.from(selectedWallpaperIds);
    const res = await authFetch('/api/admin/wallpapers/bulk-premium', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ids, isPremium })
    });
    const json = await res.json();
    if (json.success) {
      showToast(`💎 ${json.message || 'Updated status successfully!'}`);
      selectedWallpaperIds.clear();
      await loadWallpapers();
    } else {
      showToast(`Update failed: ${json.error || 'Server error'}`);
    }
  } catch (err) {
    console.error('Bulk premium error:', err);
    showToast(`Update error: ${err.message}`);
  }
}

// 4. Bulk Hero Banner Add Handler
async function executeBulkAddToBanners() {
  if (selectedWallpaperIds.size === 0) {
    showToast('Please select at least one wallpaper.');
    return;
  }

  try {
    const ids = Array.from(selectedWallpaperIds);
    const res = await authFetch('/api/admin/wallpapers/bulk-banner', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ids, badgeText: 'FEATURED DROP' })
    });
    const json = await res.json();
    if (json.success) {
      showToast(`🖼️ ${json.message || 'Added to Hero Banners successfully!'}`);
      selectedWallpaperIds.clear();
      updateWallpaperBatchBar();
      if (typeof loadBanners === 'function') {
        loadBanners();
      }
    } else {
      showToast(`Banner addition failed: ${json.error || 'Server error'}`);
    }
  } catch (err) {
    console.error('Bulk banner error:', err);
    showToast(`Error: ${err.message}`);
  }
}

// Global window exposures for bulk actions
window.handleWallpaperCheckClick = handleWallpaperCheckClick;
window.toggleSelectAllWallpapers = toggleSelectAllWallpapers;
window.selectWallpaperBatch = selectWallpaperBatch;
window.selectAllFilteredWallpapers = selectAllFilteredWallpapers;
window.clearWallpaperSelection = clearWallpaperSelection;
window.confirmBulkDelete = confirmBulkDelete;
window.closeBulkDeleteModal = closeBulkDeleteModal;
window.executeBulkDelete = executeBulkDelete;
window.executeBulkCategoryMove = executeBulkCategoryMove;
window.executeBulkPremiumToggle = executeBulkPremiumToggle;
window.executeBulkAddToBanners = executeBulkAddToBanners;

// Search & Filter listeners
document.getElementById('wallSearchInput').addEventListener('input', renderWallpapersTable);
document.getElementById('wallCategoryFilter').addEventListener('change', renderWallpapersTable);
document.getElementById('wallTypeFilter').addEventListener('change', renderWallpapersTable);

// 4. RINGTONES
async function loadRingtones() {
  try {
    const res = await fetch('/api/ringtones');
    const json = await res.json();
    if (json.success) {
      allRingtones = json.data;
      renderRingtones();
    }
  } catch (e) {
    console.error('Ringtones load error', e);
  }
}

function renderRingtones() {
  const grid = document.getElementById('ringtonesGrid');
  grid.innerHTML = allRingtones.map(r => `
    <div class="ringtone-card">
      <div class="ringtone-header">
        <div>
          <div class="ringtone-title">${r.title}</div>
          <div class="ringtone-artist">${r.artist} &bull; ${r.category}</div>
        </div>
        <span class="badge badge-cyan">${r.durationSeconds}s</span>
      </div>
      <div class="ringtone-controls">
        <button class="btn btn-sm btn-secondary" onclick="toggleAudioPreview('${r.id}', '${r.audioUrl}')" id="audioBtn-${r.id}">
          ▶️ Play Preview
        </button>
        <button class="btn btn-sm btn-danger" onclick="deleteRingtone('${r.id}')">Delete</button>
      </div>
    </div>
  `).join('');
}

function toggleAudioPreview(id, url) {
  const btn = document.getElementById(`audioBtn-${id}`);
  if (currentPlayingId === id && currentAudio) {
    currentAudio.pause();
    currentAudio = null;
    currentPlayingId = null;
    btn.textContent = '▶️ Play Preview';
    return;
  }

  if (currentAudio) {
    currentAudio.pause();
    if (currentPlayingId) {
      const prevBtn = document.getElementById(`audioBtn-${currentPlayingId}`);
      if (prevBtn) prevBtn.textContent = '▶️ Play Preview';
    }
  }

  currentAudio = new Audio(url);
  currentPlayingId = id;
  btn.textContent = '⏸️ Stop';
  currentAudio.play();
  currentAudio.onended = () => {
    btn.textContent = '▶️ Play Preview';
    currentAudio = null;
    currentPlayingId = null;
  };
}

// 5. LIVE FEATURE FLAGS, DYNAMIC WEATHER & REMOTE CONFIG
async function loadConfig() {
  try {
    const res = await authFetch('/api/admin/config');
    const json = await res.json();
    if (json.success) {
      const cfg = json.data;

      // 1. Weather Overlays Flags
      document.getElementById('cfgWeatherEnabled').checked = cfg.weather_enabled === 'true';
      const activeEffect = cfg.weather_effect || 'rain';
      document.getElementById('cfgWeatherEffect').value = activeEffect;
      document.querySelectorAll('.weather-effect-card').forEach(card => {
        card.classList.toggle('active', card.getAttribute('data-effect') === activeEffect);
      });
      document.getElementById('cfgWeatherIntensity').value = cfg.weather_intensity || 'medium';
      document.getElementById('cfgWeatherCategories').value = cfg.weather_categories || 'all';
      document.getElementById('cfgWeatherSfx').checked = cfg.weather_sfx_enabled !== 'false';

      // 2. Announcement Banner Flags
      document.getElementById('cfgAnnouncementEnabled').checked = cfg.announcement_enabled === 'true';
      document.getElementById('cfgAnnouncementText').value = cfg.announcement_text || '🎉 દિવાળી સ્પેશિયલ 3D વૉલપેપર્સ લાઈવ થયા છે! 4K માં ડાઉનલોડ કરો.';
      document.getElementById('cfgAnnouncementType').value = cfg.announcement_type || 'festival';
      document.getElementById('cfgAnnouncementTheme').value = cfg.announcement_theme || 'diwali_gold';
      document.getElementById('cfgAnnouncementAction').value = cfg.announcement_action_url || 'category:Space';
      document.getElementById('cfgAnnouncementDismissable').checked = cfg.announcement_dismissable !== 'false';

      // 3. Force Update Engine
      document.getElementById('cfgForceUpdateEnabled').checked = cfg.force_update_enabled === 'true';
      document.getElementById('cfgAppVersion').value = cfg.app_version || '1.3.0';
      document.getElementById('cfgMinSupportedVersion').value = cfg.min_supported_version || '1.2.0';
      document.getElementById('cfgUpdateTitle').value = cfg.update_dialog_title || '🚀 New 3D Engine Update Available!';
      document.getElementById('cfgUpdateMessage').value = cfg.update_dialog_message || 'We have updated ReWall with smoother 60FPS gyroscope 3D parallax tracking and fresh 4K festival drops. Please update to continue.';
      document.getElementById('cfgUpdateUrl').value = cfg.update_url || 'https://play.google.com/store/apps/details?id=com.rewall.parallax3d';
      document.getElementById('cfgUpdateBtnText').value = cfg.update_btn_text || 'Update to Latest Version';

      // 4. AdMob & Monetization
      document.getElementById('cfgBanner').checked = cfg.admob_banner_enabled === 'true';
      document.getElementById('cfgInterstitial').checked = cfg.admob_interstitial_enabled === 'true';
      document.getElementById('cfgRewardedInterval').value = cfg.admob_rewarded_interval || 3;
      document.getElementById('cfgMaintenance').checked = cfg.maintenance_mode === 'true';
      if (cfg.daily_pick_id) {
        document.getElementById('cfgDailyPickSelect').value = cfg.daily_pick_id;
      }

      // Update Live Phone Simulation Stage
      updateSimulatedApp();
    }
  } catch (e) {
    console.error('Config load error', e);
  }
}

function populateDailyPickSelect() {
  const select = document.getElementById('cfgDailyPickSelect');
  if (!select) return;
  select.innerHTML = allWallpapers.map(w => `
    <option value="${w.id}">${w.title} (${w.category})</option>
  `).join('');
}

// Interactive Live Remote Simulator Synchronization
function updateSimulatedApp() {
  const weatherEnabled = document.getElementById('cfgWeatherEnabled')?.checked || false;
  const weatherEffect = document.getElementById('cfgWeatherEffect')?.value || 'rain';
  const weatherIntensity = document.getElementById('cfgWeatherIntensity')?.value || 'medium';

  // 1. Sync Weather Engine
  if (typeof window.setWeatherSimulatorState === 'function') {
    window.setWeatherSimulatorState(weatherEnabled, weatherEffect, weatherIntensity);
  }

  const effectNames = {
    rain: '🌧️ Monsoon Rain',
    snow: '❄️ Winter Snow',
    thunderstorm: '⚡ Thunderstorm',
    clouds: '☁️ Drifting Fog',
    sakura: '🌸 Sakura Petals'
  };

  const weatherBadge = document.getElementById('simWeatherBadge');
  const effectInfo = document.getElementById('simEffectInfo');
  if (weatherBadge) {
    weatherBadge.textContent = weatherEnabled ? `${effectNames[weatherEffect] || 'Weather'} Active` : 'Weather Off';
    weatherBadge.style.display = weatherEnabled ? 'inline-block' : 'none';
  }
  if (effectInfo) {
    effectInfo.textContent = weatherEnabled 
      ? `${effectNames[weatherEffect] || 'Weather'} • ${weatherIntensity.toUpperCase()} Intensity`
      : 'Weather Particles Disabled';
  }

  // 2. Sync Announcement Banner
  const bannerEnabled = document.getElementById('cfgAnnouncementEnabled')?.checked || false;
  const bannerText = document.getElementById('cfgAnnouncementText')?.value || '';
  const bannerType = document.getElementById('cfgAnnouncementType')?.value || 'festival';
  const bannerTheme = document.getElementById('cfgAnnouncementTheme')?.value || 'diwali_gold';

  const bannerEl = document.getElementById('simAnnouncementBanner');
  const bannerTextEl = document.getElementById('simBannerText');
  const bannerIconEl = document.getElementById('simBannerIcon');

  if (bannerEl) {
    bannerEl.style.display = bannerEnabled && bannerText.trim() ? 'flex' : 'none';
    bannerEl.className = `sim-announcement-banner ${bannerTheme}`;
    if (bannerTextEl) bannerTextEl.textContent = bannerText;
    if (bannerIconEl) {
      const typeIcons = { festival: '🪔', promo: '🎁', new_feature: '⚡', alert: '⚠️' };
      bannerIconEl.textContent = typeIcons[bannerType] || '📢';
    }
  }

  // 3. Sync Force Update Modal
  const forceUpdateEnabled = document.getElementById('cfgForceUpdateEnabled')?.checked || false;
  const updateTitle = document.getElementById('cfgUpdateTitle')?.value || '';
  const updateMsg = document.getElementById('cfgUpdateMessage')?.value || '';
  const updateVer = document.getElementById('cfgAppVersion')?.value || '1.3.0';
  const updateBtnText = document.getElementById('cfgUpdateBtnText')?.value || 'Update Now';

  const updateModal = document.getElementById('simForceUpdateModal');
  const updateTitleEl = document.getElementById('simUpdateTitle');
  const updateMsgEl = document.getElementById('simUpdateMsg');
  const updateVerEl = document.getElementById('simUpdateVer');
  const updateBtnEl = document.getElementById('simUpdateBtn');

  if (updateModal) {
    updateModal.style.display = forceUpdateEnabled ? 'flex' : 'none';
    if (updateTitleEl) updateTitleEl.textContent = updateTitle;
    if (updateMsgEl) updateMsgEl.textContent = updateMsg;
    if (updateVerEl) updateVerEl.textContent = `v${updateVer} (Required)`;
    if (updateBtnEl) updateBtnEl.textContent = updateBtnText;
  }
}

// Remote Config Studio Input Listeners
function initRemoteConfigStudio() {
  // Weather Effect card buttons
  const effectCards = document.querySelectorAll('.weather-effect-card');
  effectCards.forEach(card => {
    card.addEventListener('click', () => {
      effectCards.forEach(c => c.classList.remove('active'));
      card.classList.add('active');
      const effect = card.getAttribute('data-effect');
      document.getElementById('cfgWeatherEffect').value = effect;
      updateSimulatedApp();
    });
  });

  // Dynamic live sync listeners on all toggles and inputs
  const liveInputIds = [
    'cfgWeatherEnabled', 'cfgWeatherIntensity', 'cfgWeatherCategories', 'cfgWeatherSfx',
    'cfgAnnouncementEnabled', 'cfgAnnouncementText', 'cfgAnnouncementType', 'cfgAnnouncementTheme',
    'cfgForceUpdateEnabled', 'cfgAppVersion', 'cfgMinSupportedVersion', 'cfgUpdateTitle', 'cfgUpdateMessage', 'cfgUpdateBtnText'
  ];

  liveInputIds.forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      el.addEventListener('input', updateSimulatedApp);
      el.addEventListener('change', updateSimulatedApp);
    }
  });

  // Config Form Submit -> saves to SQLite cloud
  const configForm = document.getElementById('configForm');
  if (configForm) {
    configForm.addEventListener('submit', async (e) => {
      e.preventDefault();

      const body = {
        // Weather
        weather_enabled: document.getElementById('cfgWeatherEnabled').checked,
        weather_effect: document.getElementById('cfgWeatherEffect').value,
        weather_intensity: document.getElementById('cfgWeatherIntensity').value,
        weather_categories: document.getElementById('cfgWeatherCategories').value,
        weather_sfx_enabled: document.getElementById('cfgWeatherSfx').checked,

        // Announcement
        announcement_enabled: document.getElementById('cfgAnnouncementEnabled').checked,
        announcement_text: document.getElementById('cfgAnnouncementText').value,
        announcement_type: document.getElementById('cfgAnnouncementType').value,
        announcement_theme: document.getElementById('cfgAnnouncementTheme').value,
        announcement_action_url: document.getElementById('cfgAnnouncementAction').value,
        announcement_dismissable: document.getElementById('cfgAnnouncementDismissable').checked,

        // Force Update
        force_update_enabled: document.getElementById('cfgForceUpdateEnabled').checked,
        app_version: document.getElementById('cfgAppVersion').value,
        min_supported_version: document.getElementById('cfgMinSupportedVersion').value,
        update_dialog_title: document.getElementById('cfgUpdateTitle').value,
        update_dialog_message: document.getElementById('cfgUpdateMessage').value,
        update_url: document.getElementById('cfgUpdateUrl').value,
        update_btn_text: document.getElementById('cfgUpdateBtnText').value,

        // Monetization & System
        admob_banner_enabled: document.getElementById('cfgBanner').checked,
        admob_interstitial_enabled: document.getElementById('cfgInterstitial').checked,
        admob_rewarded_interval: document.getElementById('cfgRewardedInterval').value,
        daily_pick_id: document.getElementById('cfgDailyPickSelect')?.value || 'w1',
        maintenance_mode: document.getElementById('cfgMaintenance').checked
      };

      try {
        const res = await authFetch('/api/admin/config', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(body)
        });
        const json = await res.json();
        if (json.success) {
          showToast('🎉 Live Remote Flags & Weather Config saved to Cloud!');
        } else {
          alert('Failed to save configuration: ' + (json.error || 'Server error'));
        }
      } catch (err) {
        alert('Failed to save config: ' + err.message);
      }
    });
  }
}

// 60FPS Ambient Weather Particles Simulator Canvas
let weatherAnimFrame = null;
let weatherParticles = [];
let weatherEffectType = 'rain';
let weatherIntensity = 'medium';
let weatherActive = false;

function initWeatherSimulator() {
  const canvas = document.getElementById('weatherCanvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');

  function resizeCanvas() {
    canvas.width = canvas.parentElement?.clientWidth || 250;
    canvas.height = canvas.parentElement?.clientHeight || 500;
  }
  resizeCanvas();

  function spawnParticles() {
    weatherParticles = [];
    let count = 60;
    if (weatherIntensity === 'light') count = 25;
    else if (weatherIntensity === 'heavy') count = 120;

    for (let i = 0; i < count; i++) {
      weatherParticles.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,
        len: Math.random() * 14 + 8,
        speed: Math.random() * 4 + 3.5,
        size: Math.random() * 2.8 + 1,
        angle: (Math.random() * 10 + 75) * (Math.PI / 180),
        alpha: Math.random() * 0.5 + 0.35,
        swaySpeed: Math.random() * 0.03 + 0.015,
        swayOffset: Math.random() * Math.PI * 2,
        rot: Math.random() * 360,
        rotSpeed: Math.random() * 2 - 1
      });
    }
  }

  let lightningTimer = 0;
  let isFlashing = false;

  function loop() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    if (weatherActive) {
      if (weatherEffectType === 'rain' || weatherEffectType === 'thunderstorm') {
        ctx.strokeStyle = weatherEffectType === 'thunderstorm' ? '#a5f3fc' : '#67e8f9';
        ctx.lineWidth = 1.4;

        weatherParticles.forEach(p => {
          ctx.beginPath();
          ctx.globalAlpha = p.alpha;
          ctx.moveTo(p.x, p.y);
          const dx = Math.cos(p.angle) * p.len;
          const dy = Math.sin(p.angle) * p.len;
          ctx.lineTo(p.x + dx, p.y + dy);
          ctx.stroke();

          p.x += Math.cos(p.angle) * p.speed;
          p.y += Math.sin(p.angle) * p.speed;

          if (p.y > canvas.height) {
            p.y = -p.len;
            p.x = Math.random() * canvas.width;
          }
        });

        if (weatherEffectType === 'thunderstorm') {
          lightningTimer++;
          if (lightningTimer > 120 && Math.random() < 0.04) {
            isFlashing = true;
            lightningTimer = 0;
            setTimeout(() => { isFlashing = false; }, 80);
          }
          if (isFlashing) {
            ctx.fillStyle = 'rgba(255, 255, 255, 0.4)';
            ctx.fillRect(0, 0, canvas.width, canvas.height);
          }
        }
      } else if (weatherEffectType === 'snow') {
        ctx.fillStyle = '#ffffff';
        weatherParticles.forEach(p => {
          p.swayOffset += p.swaySpeed;
          p.x += Math.sin(p.swayOffset) * 0.7;
          p.y += p.speed * 0.45;

          ctx.beginPath();
          ctx.globalAlpha = p.alpha;
          ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
          ctx.fill();

          if (p.y > canvas.height) {
            p.y = -5;
            p.x = Math.random() * canvas.width;
          }
        });
      } else if (weatherEffectType === 'sakura') {
        ctx.fillStyle = '#f472b6';
        weatherParticles.forEach(p => {
          p.swayOffset += p.swaySpeed;
          p.x += Math.sin(p.swayOffset) * 1.1;
          p.y += p.speed * 0.45;
          p.rot += p.rotSpeed;

          ctx.save();
          ctx.translate(p.x, p.y);
          ctx.rotate((p.rot * Math.PI) / 180);
          ctx.globalAlpha = p.alpha;

          ctx.beginPath();
          ctx.ellipse(0, 0, p.size * 2, p.size * 1.2, 0, 0, Math.PI * 2);
          ctx.fill();
          ctx.restore();

          if (p.y > canvas.height) {
            p.y = -10;
            p.x = Math.random() * canvas.width;
          }
        });
      } else if (weatherEffectType === 'clouds') {
        ctx.fillStyle = 'rgba(203, 213, 225, 0.1)';
        weatherParticles.forEach(p => {
          p.x += p.speed * 0.15;
          ctx.beginPath();
          ctx.globalAlpha = p.alpha * 0.3;
          ctx.arc(p.x, p.y, p.size * 16, 0, Math.PI * 2);
          ctx.fill();

          if (p.x - p.size * 16 > canvas.width) {
            p.x = -p.size * 16;
            p.y = Math.random() * canvas.height;
          }
        });
      }
    }

    ctx.globalAlpha = 1.0;
    weatherAnimFrame = requestAnimationFrame(loop);
  }

  spawnParticles();
  if (weatherAnimFrame) cancelAnimationFrame(weatherAnimFrame);
  weatherAnimFrame = requestAnimationFrame(loop);

  window.setWeatherSimulatorState = function (active, effect, intensity) {
    weatherActive = active;
    weatherEffectType = effect || 'rain';
    weatherIntensity = intensity || 'medium';
    spawnParticles();
  };
}

// Category form submit
document.getElementById('addCategoryForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = document.getElementById('newCatId').value.trim();
  const name = document.getElementById('newCatName').value.trim();
  const iconUrl = document.getElementById('newCatIcon').value.trim();

  try {
    const res = await authFetch('/api/admin/categories', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id, name, iconUrl })
    });
    const json = await res.json();
    if (json.success) {
      showToast(`Category "${name}" added!`);
      document.getElementById('addCategoryForm').reset();
      await loadCategories();
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
});

async function deleteCategory(id) {
  if (!confirm(`Are you sure you want to delete category "${id}"?`)) return;
  try {
    const res = await authFetch(`/api/admin/categories/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Category deleted');
      await loadCategories();
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
}

// 6. MODALS & 3D LAYER BUILDER
function initModals() {
  const wpModal = document.getElementById('wallpaperModal');
  const rtModal = document.getElementById('ringtoneModal');

  document.getElementById('openUploadModalBtn').addEventListener('click', () => {
    openCreateWallpaperModal();
  });

  document.getElementById('closeWallpaperModal').addEventListener('click', () => {
    wpModal.classList.remove('active');
  });

  document.getElementById('cancelWallpaperBtn').addEventListener('click', () => {
    wpModal.classList.remove('active');
  });

  document.getElementById('openRingtoneModalBtn').addEventListener('click', () => {
    document.getElementById('ringtoneForm').reset();
    rtModal.classList.add('active');
  });

  document.getElementById('closeRingtoneModal').addEventListener('click', () => {
    rtModal.classList.remove('active');
  });

  document.getElementById('cancelRingtoneBtn').addEventListener('click', () => {
    rtModal.classList.remove('active');
  });

  document.getElementById('addLayerBtn').addEventListener('click', () => {
    addLayerRow();
  });

  document.getElementById('wpIsParallax').addEventListener('change', (e) => {
    document.getElementById('layerBuilderSection').style.display = e.target.checked ? 'block' : 'none';
    updatePreviewLayers();
  });

  document.getElementById('wpPreviewUrl').addEventListener('input', () => {
    updatePreviewLayers();
  });

  // Universal click outside on modal backdrop to close
  document.querySelectorAll('.modal-backdrop').forEach(modal => {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        modal.classList.remove('active');
      }
    });
  });

  // Universal Escape key to close any active modal
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-backdrop.active').forEach(m => m.classList.remove('active'));
    }
  });
}

function openCreateWallpaperModal() {
  document.getElementById('wallpaperForm').reset();
  document.getElementById('editWallpaperId').value = '';
  document.getElementById('modalWallpaperTitle').textContent = 'Create 3D Parallax Wallpaper';
  document.getElementById('layersContainer').innerHTML = '';
  document.getElementById('wpIsParallax').checked = true;
  document.getElementById('layerBuilderSection').style.display = 'block';

  // Add 2 default layers (Background & Foreground)
  addLayerRow('', 0.2);
  addLayerRow('', 0.7);

  document.getElementById('wallpaperModal').classList.add('active');
  updatePreviewLayers();
}

function openEditWallpaperModal(id) {
  const wp = allWallpapers.find(w => w.id === id);
  if (!wp) return;

  document.getElementById('editWallpaperId').value = wp.id;
  document.getElementById('modalWallpaperTitle').textContent = `Edit Wallpaper: ${wp.title}`;
  document.getElementById('wpTitle').value = wp.title;
  document.getElementById('wpCategory').value = wp.category;
  document.getElementById('wpIsPremium').value = String(wp.isPremium);
  document.getElementById('wpPreviewUrl').value = wp.previewUrl;
  document.getElementById('wpFullUrl').value = wp.fullUrl;
  document.getElementById('wpIsParallax').checked = wp.isParallax;
  document.getElementById('layerBuilderSection').style.display = wp.isParallax ? 'block' : 'none';

  const container = document.getElementById('layersContainer');
  container.innerHTML = '';
  if (wp.layers && wp.layers.length > 0) {
    wp.layers.forEach(l => {
      addLayerRow(l.imageUrl, l.depth);
    });
  } else {
    addLayerRow('', 0.2);
    addLayerRow('', 0.7);
  }

  document.getElementById('wallpaperModal').classList.add('active');
  updatePreviewLayers();
}

function addLayerRow(url = '', depth = 0.5) {
  const container = document.getElementById('layersContainer');
  const index = container.children.length + 1;
  const div = document.createElement('div');
  div.className = 'layer-card';
  div.innerHTML = `
    <div class="layer-order-badge">${index}</div>
    <input type="text" class="layer-url-input" placeholder="Layer image URL or upload..." value="${url}">
    <button type="button" class="btn btn-sm btn-secondary layer-crop-btn" style="padding:4px 7px; font-size:11px; color:#00E5FF;" title="Crop layer to 9:16">✂️</button>
    <label class="upload-btn" style="padding:4px 8px; font-size:11px;">
      <input type="file" accept="image/*" style="display:none;" class="layer-file-input">
      📁
    </label>
    <div class="depth-slider-box">
      <span>Depth:</span>
      <input type="range" class="layer-depth-slider" min="0.1" max="1.0" step="0.05" value="${depth}">
      <span class="depth-val">${parseFloat(depth).toFixed(2)}</span>
    </div>
    <button type="button" class="btn btn-sm btn-danger" style="padding:4px 8px;" onclick="this.parentElement.remove(); updatePreviewLayers();">&times;</button>
  `;

  // Attach listeners
  const urlInput = div.querySelector('.layer-url-input');
  const depthSlider = div.querySelector('.layer-depth-slider');
  const depthVal = div.querySelector('.depth-val');
  const fileInput = div.querySelector('.layer-file-input');
  const cropBtn = div.querySelector('.layer-crop-btn');

  urlInput.addEventListener('input', updatePreviewLayers);
  depthSlider.addEventListener('input', (e) => {
    depthVal.textContent = parseFloat(e.target.value).toFixed(2);
    updatePreviewLayers();
  });

  if (cropBtn) {
    cropBtn.addEventListener('click', () => {
      const val = urlInput.value.trim();
      if (!val) {
        fileInput.click();
        return;
      }
      openImageCropper({
        imageUrl: val,
        onComplete: (uploadedUrl) => {
          urlInput.value = uploadedUrl;
          updatePreviewLayers();
        }
      });
    });
  }

  fileInput.addEventListener('change', async (e) => {
    if (e.target.files.length > 0) {
      const file = e.target.files[0];
      openImageCropper({
        file,
        onComplete: (uploadedUrl) => {
          urlInput.value = uploadedUrl;
          updatePreviewLayers();
        }
      });
      e.target.value = '';
    }
  });

  container.appendChild(div);
  updatePreviewLayers();
}

// 7. FILE UPLOAD HANDLING (WITH MOBILE CROPPER HOOKS)
function initFileUploads() {
  // Preview image upload -> opens 9:16 Cropper
  document.getElementById('wpPreviewFileInput').addEventListener('change', async (e) => {
    if (e.target.files.length > 0) {
      const file = e.target.files[0];
      openImageCropper({
        file,
        onComplete: (url) => {
          document.getElementById('wpPreviewUrl').value = url;
          updatePreviewLayers();
        }
      });
      e.target.value = '';
    }
  });

  // Full 4K image upload -> opens 9:16 Cropper
  document.getElementById('wpFullFileInput').addEventListener('change', async (e) => {
    if (e.target.files.length > 0) {
      const file = e.target.files[0];
      openImageCropper({
        file,
        onComplete: (url) => {
          document.getElementById('wpFullUrl').value = url;
        }
      });
      e.target.value = '';
    }
  });

  // Ringtone audio upload
  document.getElementById('rtAudioFileInput').addEventListener('change', async (e) => {
    if (e.target.files.length > 0) {
      const url = await uploadFile(e.target.files[0]);
      if (url) document.getElementById('rtAudioUrl').value = url;
      e.target.value = '';
    }
  });
}

async function uploadFile(file) {
  const formData = new FormData();
  formData.append('file', file);
  showToast('Uploading file...');
  try {
    const res = await authFetch('/api/admin/upload', {
      method: 'POST',
      body: formData
    });
    const json = await res.json();
    if (json.success) {
      showToast('File uploaded successfully!');
      return json.url;
    } else {
      alert('Upload failed: ' + json.error);
      return null;
    }
  } catch (err) {
    alert('Upload error: ' + err.message);
    return null;
  }
}

// 8. LIVE 3D TILT PREVIEW
function updatePreviewLayers() {
  const stage = document.getElementById('previewStage');
  const isParallax = document.getElementById('wpIsParallax').checked;
  const previewUrl = document.getElementById('wpPreviewUrl').value.trim();

  if (!isParallax) {
    if (previewUrl) {
      stage.innerHTML = `<img src="${previewUrl}" class="preview-layer-img" style="transform:none;">`;
    } else {
      stage.innerHTML = '<div class="preview-placeholder">Enter cover image URL to preview</div>';
    }
    return;
  }

  const layerInputs = document.querySelectorAll('.layer-card');
  const layersData = [];

  layerInputs.forEach(card => {
    const url = card.querySelector('.layer-url-input').value.trim();
    const depth = parseFloat(card.querySelector('.layer-depth-slider').value) || 0.5;
    if (url) layersData.push({ url, depth });
  });

  if (layersData.length === 0) {
    if (previewUrl) {
      stage.innerHTML = `<img src="${previewUrl}" class="preview-layer-img" data-depth="0.3">`;
    } else {
      stage.innerHTML = '<div class="preview-placeholder">Add layer images to test 3D parallax tilt</div>';
    }
    return;
  }

  stage.innerHTML = layersData.map(l => `
    <img src="${l.url}" class="preview-layer-img" data-depth="${l.depth}">
  `).join('');
}

function initParallaxPreview() {
  const stage = document.getElementById('previewStage');

  stage.addEventListener('mousemove', (e) => {
    const rect = stage.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    // Center offset from -1 to 1
    const xNorm = (x / rect.width - 0.5) * 2;
    const yNorm = (y / rect.height - 0.5) * 2;

    const layerImgs = stage.querySelectorAll('.preview-layer-img');
    layerImgs.forEach(img => {
      const depth = parseFloat(img.dataset.depth || '0.5');
      const moveX = -xNorm * depth * 28;
      const moveY = -yNorm * depth * 28;
      img.style.transform = `translate(${moveX}px, ${moveY}px) scale(1.15)`;
    });
  });

  stage.addEventListener('mouseleave', () => {
    const layerImgs = stage.querySelectorAll('.preview-layer-img');
    layerImgs.forEach(img => {
      img.style.transform = 'translate(0px, 0px) scale(1.15)';
    });
  });
}

// 9. WALLPAPER SUBMIT
document.getElementById('wallpaperForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = document.getElementById('editWallpaperId').value;
  const title = document.getElementById('wpTitle').value.trim();
  const category = document.getElementById('wpCategory').value;
  const isPremium = document.getElementById('wpIsPremium').value === 'true';
  const previewUrl = document.getElementById('wpPreviewUrl').value.trim();
  const fullUrl = document.getElementById('wpFullUrl').value.trim() || previewUrl;
  const isParallax = document.getElementById('wpIsParallax').checked;

  const layers = [];
  if (isParallax) {
    const layerCards = document.querySelectorAll('.layer-card');
    layerCards.forEach((card, idx) => {
      const url = card.querySelector('.layer-url-input').value.trim();
      const depth = parseFloat(card.querySelector('.layer-depth-slider').value) || 0.5;
      if (url) {
        layers.push({ imageUrl: url, depth, sortOrder: idx });
      }
    });
  }

  const payload = {
    title,
    category,
    isPremium,
    previewUrl,
    fullUrl,
    isParallax,
    layers
  };

  try {
    let res;
    if (id) {
      // Update
      res = await authFetch(`/api/admin/wallpapers/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    } else {
      // Create
      res = await authFetch('/api/admin/wallpapers', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    }

    const json = await res.json();
    if (json.success) {
      showToast(id ? 'Wallpaper updated!' : '3D Wallpaper created!');
      document.getElementById('wallpaperModal').classList.remove('active');
      await loadWallpapers();
      await loadStats();
    } else {
      alert('Error: ' + json.error);
    }
  } catch (err) {
    alert('Request failed: ' + err.message);
  }
});

async function deleteWallpaper(id) {
  if (!confirm(`Are you sure you want to delete wallpaper ${id}?`)) return;
  try {
    const res = await authFetch(`/api/admin/wallpapers/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Wallpaper deleted');
      await loadWallpapers();
      await loadStats();
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
}

// 10. RINGTONE SUBMIT & DELETE
document.getElementById('ringtoneForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const payload = {
    title: document.getElementById('rtTitle').value.trim(),
    artist: document.getElementById('rtArtist').value.trim(),
    category: document.getElementById('rtCategory').value.trim(),
    audioUrl: document.getElementById('rtAudioUrl').value.trim(),
    durationSeconds: parseInt(document.getElementById('rtDuration').value, 10) || 30
  };

  try {
    const res = await authFetch('/api/admin/ringtones', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (json.success) {
      showToast('Ringtone added to catalog!');
      document.getElementById('ringtoneModal').classList.remove('active');
      await loadRingtones();
      await loadStats();
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
});

async function deleteRingtone(id) {
  if (!confirm('Are you sure you want to delete this ringtone?')) return;
  try {
    const res = await authFetch(`/api/admin/ringtones/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Ringtone deleted');
      await loadRingtones();
      await loadStats();
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
}

// 11. BULK UPLOAD & AUTO-WEBP PROCESSING
let selectedBulkFiles = [];

function initBulkUpload() {
  const modal = document.getElementById('bulkUploadModal');
  const openBtn = document.getElementById('openBulkUploadModalBtn');
  const closeBtn = document.getElementById('closeBulkModal');
  const cancelBtn = document.getElementById('cancelBulkBtn');
  const dropzone = document.getElementById('bulkDropzone');
  const fileInput = document.getElementById('bulkFileInput');
  const browseBtn = document.getElementById('browseBulkBtn');
  const badge = document.getElementById('selectedFilesBadge');
  const form = document.getElementById('bulkUploadForm');
  const progressBox = document.getElementById('bulkProgressBox');
  const progressBarFill = document.getElementById('bulkProgressBarFill');
  const progressStatus = document.getElementById('bulkProgressStatus');
  const progressPercent = document.getElementById('bulkProgressPercent');
  const resultBox = document.getElementById('bulkResultBox');
  const startBtn = document.getElementById('startBulkBtn');
  const bulkCategory = document.getElementById('bulkCategory');

  openBtn.addEventListener('click', () => {
    selectedBulkFiles = [];
    badge.style.display = 'none';
    progressBox.style.display = 'none';
    resultBox.style.display = 'none';
    startBtn.disabled = false;
    form.reset();

    // Populate category dropdown
    bulkCategory.innerHTML = allCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');

    modal.classList.add('active');
  });

  closeBtn.addEventListener('click', () => modal.classList.remove('active'));
  cancelBtn.addEventListener('click', () => modal.classList.remove('active'));

  browseBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    fileInput.click();
  });

  dropzone.addEventListener('click', () => fileInput.click());

  dropzone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropzone.classList.add('dragover');
  });

  dropzone.addEventListener('dragleave', () => {
    dropzone.classList.remove('dragover');
  });

  dropzone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropzone.classList.remove('dragover');
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFilesSelected(Array.from(e.dataTransfer.files));
    }
  });

  fileInput.addEventListener('change', (e) => {
    if (e.target.files && e.target.files.length > 0) {
      handleFilesSelected(Array.from(e.target.files));
    }
  });

  function handleFilesSelected(files) {
    selectedBulkFiles = files;
    badge.style.display = 'inline-block';
    const isZip = files.length === 1 && files[0].name.toLowerCase().endsWith('.zip');
    if (isZip) {
      badge.textContent = `📦 ZIP Archive: ${files[0].name} (${(files[0].size / (1024*1024)).toFixed(1)} MB)`;
    } else {
      const totalMB = (files.reduce((acc, f) => acc + f.size, 0) / (1024 * 1024)).toFixed(1);
      badge.textContent = `🖼️ ${files.length} Wallpapers Selected (${totalMB} MB Total)`;
    }
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!selectedBulkFiles || selectedBulkFiles.length === 0) {
      alert('Please select files or a .ZIP archive first!');
      return;
    }

    startBtn.disabled = true;
    progressBox.style.display = 'block';
    resultBox.style.display = 'none';
    progressBarFill.style.width = '20%';
    progressPercent.textContent = '20%';
    progressStatus.textContent = 'Uploading files to server...';

    const formData = new FormData();
    for (const f of selectedBulkFiles) {
      formData.append('files', f);
    }
    formData.append('category', document.getElementById('bulkCategory').value);
    formData.append('isPremium', document.getElementById('bulkIsPremium').value);
    formData.append('isParallax', document.getElementById('bulkIsParallax').value);

    // Dynamic progress ticker while Sharp compresses in Node.js
    let simProgress = 25;
    const progressTimer = setInterval(() => {
      if (simProgress < 88) {
        simProgress += Math.floor(Math.random() * 7) + 3;
        progressBarFill.style.width = `${simProgress}%`;
        progressPercent.textContent = `${simProgress}%`;
        progressStatus.textContent = 'Auto-converting to 4K WebP & generating 600px mobile thumbs...';
      }
    }, 350);

    try {
      const res = await authFetch('/api/admin/bulk-upload', {
        method: 'POST',
        body: formData
      });
      clearInterval(progressTimer);

      progressBarFill.style.width = '100%';
      progressPercent.textContent = '100%';
      progressStatus.textContent = 'Processing Completed!';

      const json = await res.json();
      if (json.success) {
        showToast(`🎉 Imported ${json.count} Wallpapers!`);
        resultBox.style.display = 'block';
        const origMB = (json.totalOriginalBytes / (1024 * 1024)).toFixed(2);
        const compMB = (json.totalCompressedBytes / (1024 * 1024)).toFixed(2);
        resultBox.innerHTML = `
          <strong>✅ Bulk Import Completed Successfully!</strong>
          <div style="margin-top:6px; line-height:1.6; color:#D1FAE5;">
            • Total Wallpapers Added: <strong>${json.count}</strong><br>
            • Bandwidth & Storage Saved: <strong>${json.savedPercent}%</strong> (${origMB} MB &rarr; ${compMB} MB WebP)<br>
            • Mobile Thumbnails: Generated at 600x900 WebP for instant 0ms app grid scrolling!
          </div>
        `;

        await loadWallpapers();
        await loadStats();

        setTimeout(() => {
          modal.classList.remove('active');
        }, 3500);
      } else {
        alert('Bulk upload error: ' + json.error);
        startBtn.disabled = false;
      }
    } catch (err) {
      clearInterval(progressTimer);
      alert('Upload failed: ' + err.message);
      startBtn.disabled = false;
    }
  });
}

// ========================================================
// PUSH NOTIFICATION STUDIO
// ========================================================
function populateNotifWallpaperSelect() {
  const quickSelect = document.getElementById('notifWallpaperQuickSelect');
  const deepLinkSelect = document.getElementById('notifDeepLinkSelect');
  if (!quickSelect || !deepLinkSelect) return;

  quickSelect.innerHTML = '<option value="">-- Choose from Catalog --</option>' +
    allWallpapers.map(w => `<option value="${w.previewUrl}" data-id="${w.id}" data-title="${w.title}">${w.title} (${w.category})</option>`).join('');

  deepLinkSelect.innerHTML = '<option value="">📱 Open App Home Screen</option>' +
    allWallpapers.map(w => `<option value="${w.id}">🖼️ Direct to Wallpaper: ${w.title}</option>`).join('');
}

function initPushNotifications() {
  const titleInput = document.getElementById('notifTitleInput');
  const bodyInput = document.getElementById('notifBodyInput');
  const imgInput = document.getElementById('notifImageUrlInput');
  const fileInput = document.getElementById('notifImageFileInput');
  const quickSelect = document.getElementById('notifWallpaperQuickSelect');
  const deepLinkSelect = document.getElementById('notifDeepLinkSelect');
  const sampleBtn = document.getElementById('loadSampleNotifBtn');
  const form = document.getElementById('notifComposerForm');
  const refreshBtn = document.getElementById('refreshNotifHistoryBtn');

  // Live Phone Mockup Elements
  const mockupTitle = document.getElementById('mockupTitle');
  const mockupBody = document.getElementById('mockupBody');
  const mockupImg = document.getElementById('mockupBannerImg');
  const mockupTime = document.getElementById('mockupTimeDisplay');

  // Update clock
  if (mockupTime) {
    const updateMockupClock = () => {
      const now = new Date();
      mockupTime.textContent = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };
    updateMockupClock();
    setInterval(updateMockupClock, 30000);
  }

  // Live Title Sync
  if (titleInput && mockupTitle) {
    titleInput.addEventListener('input', () => {
      mockupTitle.textContent = titleInput.value.trim() || '🔥 New 3D Parallax Drop: Cyber Samurai';
    });
  }

  // Live Body Sync
  if (bodyInput && mockupBody) {
    bodyInput.addEventListener('input', () => {
      mockupBody.textContent = bodyInput.value.trim() || 'Feel the 4K depth in your hand! Open now to preview and set as your live wallpaper.';
    });
  }

  // Live Image Banner Sync
  if (imgInput && mockupImg) {
    imgInput.addEventListener('input', () => {
      const val = imgInput.value.trim();
      mockupImg.src = val || 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=75';
    });
  }

  // Image File Upload for Banner
  if (fileInput) {
    fileInput.addEventListener('change', async (e) => {
      if (e.target.files && e.target.files.length > 0) {
        const file = e.target.files[0];
        const uploadedUrl = await uploadFile(file);
        if (uploadedUrl) {
          imgInput.value = uploadedUrl;
          mockupImg.src = uploadedUrl;
        }
      }
    });
  }

  // Quick Wallpaper Select
  if (quickSelect) {
    quickSelect.addEventListener('change', () => {
      const selectedOption = quickSelect.options[quickSelect.selectedIndex];
      if (selectedOption && selectedOption.value) {
        imgInput.value = selectedOption.value;
        mockupImg.src = selectedOption.value;

        const wallId = selectedOption.dataset.id;
        const wallTitle = selectedOption.dataset.title;

        if (deepLinkSelect && wallId) {
          deepLinkSelect.value = wallId;
        }

        if (!titleInput.value) {
          titleInput.value = `⭐ Featured Pick: ${wallTitle}`;
          mockupTitle.textContent = titleInput.value;
        }
        if (!bodyInput.value) {
          bodyInput.value = `Experience high-depth 3D parallax layers on your phone. Tap to preview & apply!`;
          mockupBody.textContent = bodyInput.value;
        }
      }
    });
  }

  // Sample Preset Loader
  if (sampleBtn) {
    sampleBtn.addEventListener('click', () => {
      const sample = allWallpapers.find(w => w.isParallax) || allWallpapers[0];
      if (sample) {
        titleInput.value = `🔥 4K 3D Drop: ${sample.title}`;
        bodyInput.value = `Dive into ultra-deep gyro motion on your home screen! Tap to unlock and apply now.`;
        imgInput.value = sample.previewUrl;
        mockupTitle.textContent = titleInput.value;
        mockupBody.textContent = bodyInput.value;
        mockupImg.src = sample.previewUrl;
        if (deepLinkSelect) deepLinkSelect.value = sample.id;
        showToast('Sample notification preset loaded!');
      } else {
        titleInput.value = '🚀 New Weekend 3D Parallax Wallpapers!';
        bodyInput.value = 'Over 10 new high-definition motion wallpapers added. Refresh your setup today!';
        imgInput.value = 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=75';
        mockupTitle.textContent = titleInput.value;
        mockupBody.textContent = bodyInput.value;
        mockupImg.src = imgInput.value;
        showToast('Sample notification preset loaded!');
      }
    });
  }

  // Send Notification Form
  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const sendBtn = document.getElementById('sendNotifBtn');
      const title = titleInput.value.trim();
      const body = bodyInput.value.trim();
      const imageUrl = imgInput.value.trim();
      const wallpaperId = deepLinkSelect ? deepLinkSelect.value : '';

      if (!confirm(`Are you sure you want to broadcast this notification to all active devices?\n\n"${title}"`)) {
        return;
      }

      sendBtn.disabled = true;
      sendBtn.innerHTML = 'Broadcasting to devices...';

      try {
        const res = await authFetch('/api/admin/notifications/send', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ title, body, imageUrl, wallpaperId })
        });
        const json = await res.json();
        if (json.success) {
          showToast(`🔔 Notification successfully broadcasted! (${json.data.recipientCount} devices)`);
          form.reset();
          mockupTitle.textContent = '🔥 New 3D Parallax Drop: Cyber Samurai';
          mockupBody.textContent = 'Feel the 4K depth in your hand! Open now to preview and set as your live wallpaper.';
          mockupImg.src = 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600&auto=format&fit=crop&q=75';
          await loadNotifications();
        } else {
          alert('Notification error: ' + json.error);
        }
      } catch (err) {
        alert('Failed to send notification: ' + err.message);
      } finally {
        sendBtn.disabled = false;
        sendBtn.innerHTML = `
          <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          Broadcast Push Notification Now
        `;
      }
    });
  }

  // Refresh History
  if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
      loadNotifications();
      showToast('Notification history refreshed!');
    });
  }
}

async function loadNotifications() {
  try {
    const res = await authFetch('/api/admin/notifications');
    const json = await res.json();
    if (json.success) {
      const items = json.data || [];
      document.getElementById('notifActiveDevices').textContent = (json.activeDevices || 1).toLocaleString();
      document.getElementById('notifTotalSent').textContent = items.length.toLocaleString();

      const tbody = document.getElementById('notifHistoryTableBody');
      if (!tbody) return;

      if (items.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="7" style="text-align:center; padding:32px; color:var(--text-muted);">
              No push notifications broadcasted yet. Use the composer above to send your first campaign!
            </td>
          </tr>
        `;
        return;
      }

      tbody.innerHTML = items.map(n => {
        const dateStr = new Date(n.sentAt).toLocaleString([], {
          month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });
        const targetLabel = n.wallpaperId
          ? `<span class="badge badge-cyan">Wallpaper #${n.wallpaperId}</span>`
          : `<span class="badge" style="background:rgba(255,255,255,0.06); color:#fff;">App Home</span>`;
        const thumb = n.imageUrl
          ? `<img src="${n.imageUrl}" class="notif-thumb-img" alt="">`
          : `<div class="notif-thumb-img" style="display:flex;align-items:center;justify-content:center;background:rgba(255,255,255,0.05); font-size:18px;">🔔</div>`;

        return `
          <tr>
            <td>${thumb}</td>
            <td>
              <strong style="color:#fff; font-size:13.5px;">${escapeHtml(n.title)}</strong>
              <div style="font-size:12px; color:var(--text-muted); margin-top:2px; max-width:320px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                ${escapeHtml(n.body)}
              </div>
            </td>
            <td>${targetLabel}</td>
            <td style="font-size:12px; color:var(--text-muted);">${dateStr}</td>
            <td><span class="badge badge-purple">📱 ${n.recipientCount || 1} Device${(n.recipientCount || 1) > 1 ? 's' : ''}</span></td>
            <td><span class="badge badge-green">✓ ${n.status || 'Delivered'}</span></td>
            <td>
              <button class="btn btn-sm btn-danger" onclick="deleteNotification('${n.id}')">Delete</button>
            </td>
          </tr>
        `;
      }).join('');
    }
  } catch (err) {
    console.error('Error loading notification history', err);
  }
}

async function deleteNotification(id) {
  if (!confirm('Delete this broadcast log?')) return;
  try {
    const res = await authFetch(`/api/admin/notifications/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Notification log deleted');
      await loadNotifications();
    }
  } catch (err) {
    alert('Error: ' + err.message);
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ==========================================
// 1-CLICK BACKUP & DISASTER RECOVERY STUDIO
// ==========================================

function initBackupRestore() {
  const openBtn = document.getElementById('openBackupModalBtn');
  const modal = document.getElementById('backupRestoreModal');
  const closeBtn = document.getElementById('closeBackupModal');
  const cancelBtn = document.getElementById('cancelBackupModalBtn');
  const downloadFullBtn = document.getElementById('downloadFullBackupBtn');
  const downloadDbBtn = document.getElementById('downloadDbOnlyBtn');
  const createSnapshotBtn = document.getElementById('createServerSnapshotBtn');
  const refreshSnapshotsBtn = document.getElementById('refreshSnapshotsBtn');
  const saveAutoConfigBtn = document.getElementById('saveAutoBackupConfigBtn');
  const dropzone = document.getElementById('restoreDropzone');
  const fileInput = document.getElementById('restoreFileInput');
  const selectedName = document.getElementById('selectedBackupName');
  const restoreForm = document.getElementById('restoreBackupForm');
  const startRestoreBtn = document.getElementById('startRestoreBtn');
  const restoreProgress = document.getElementById('restoreProgressBox');

  let selectedBackupFile = null;

  async function loadBackupStatsAndSnapshots() {
    try {
      // 1. Load system backup metrics & config
      const res = await authFetch('/api/admin/backup/stats');
      const json = await res.json();
      if (json.success && json.data) {
        const d = json.data;
        const dbKb = (d.databaseSizeBytes / 1024).toFixed(1);
        const uploadsMb = (d.uploadsSizeBytes / (1024 * 1024)).toFixed(2);

        const dbEl = document.getElementById('bkDbSize');
        const wallEl = document.getElementById('bkWallCount');
        const uploadsEl = document.getElementById('bkUploadsInfo');
        const autoStatusEl = document.getElementById('bkAutoStatus');

        if (dbEl) dbEl.textContent = `${dbKb} KB`;
        if (wallEl) wallEl.textContent = `${d.wallpapersCount} wallpapers`;
        if (uploadsEl) uploadsEl.textContent = `${d.uploadsCount} files (${uploadsMb} MB)`;

        const cfg = d.autoConfig || {};
        if (autoStatusEl) {
          if (cfg.enabled) {
            autoStatusEl.style.color = '#10B981';
            autoStatusEl.textContent = `🟢 Active (${cfg.frequency || 'Daily'})`;
          } else {
            autoStatusEl.style.color = '#EF4444';
            autoStatusEl.textContent = '⏸️ Disabled';
          }
        }

        const enabledCheck = document.getElementById('autoBackupEnabledCheck');
        const freqSelect = document.getElementById('autoBackupFrequencySelect');
        const retInput = document.getElementById('autoBackupRetentionInput');

        if (enabledCheck) enabledCheck.checked = Boolean(cfg.enabled);
        if (freqSelect && cfg.frequency) freqSelect.value = cfg.frequency;
        if (retInput && cfg.retentionMaxSnapshots) retInput.value = cfg.retentionMaxSnapshots;
      }

      // 2. Load server snapshots history
      await loadSnapshotsList();
    } catch (err) {
      console.error('Failed to load backup stats & snapshots:', err);
    }
  }

  async function loadSnapshotsList() {
    const tbody = document.getElementById('backupSnapshotsTableBody');
    const badge = document.getElementById('snapshotsCountBadge');
    if (!tbody) return;

    try {
      const res = await authFetch('/api/admin/backup/snapshots');
      const json = await res.json();

      if (json.success && Array.isArray(json.data)) {
        const list = json.data;
        if (badge) badge.textContent = `${list.length} Snapshots`;

        if (list.length === 0) {
          tbody.innerHTML = `
            <tr>
              <td colspan="6" style="text-align:center; padding:24px; color:#94A3B8;">
                <div style="font-size:20px; margin-bottom:4px;">📦</div>
                <div>No server snapshots found. Click "⚡ Create Snapshot Now" to create your first backup.</div>
              </td>
            </tr>
          `;
          return;
        }

        tbody.innerHTML = list.map(snap => {
          const isAuto = snap.type === 'auto';
          const sizeStr = snap.sizeBytes ? (snap.sizeBytes > 1024 * 1024 ? `${(snap.sizeBytes / (1024 * 1024)).toFixed(2)} MB` : `${(snap.sizeBytes / 1024).toFixed(1)} KB`) : '--';
          const timeStr = snap.createdAt ? new Date(snap.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : 'N/A';
          const totalEntries = snap.manifestStats?.totalEntries || snap.manifestStats?.metrics?.wallpapers || '--';

          return `
            <tr>
              <td>
                <div style="font-weight:700; color:#F8FAFC; display:flex; align-items:center; gap:6px;">
                  <span>${escapeHtml(snap.label || snap.filename)}</span>
                </div>
                <div style="font-size:11px; color:#94A3B8; font-family:monospace; margin-top:2px;">
                  ${escapeHtml(snap.filename)}
                </div>
              </td>
              <td style="color:#CBD5E1;">${timeStr}</td>
              <td>
                <span class="badge-pill" style="font-size:10.5px; ${isAuto ? 'background:rgba(16, 185, 129, 0.15); color:#10B981;' : 'background:rgba(0, 229, 255, 0.15); color:var(--accent-cyan);'}">
                  ${isAuto ? '⏰ Auto Daily' : '👤 Manual'}
                </span>
              </td>
              <td style="font-weight:600; color:#F8FAFC;">${sizeStr}</td>
              <td style="color:#38BDF8; font-weight:600;">${totalEntries} items</td>
              <td style="text-align:right;">
                <div style="display:flex; justify-content:flex-end; gap:6px; align-items:center;">
                  <button 
                    type="button" 
                    class="btn-icon-tiny" 
                    onclick="restoreServerSnapshot('${escapeHtml(snap.filename)}')" 
                    title="⚡ 1-Second Instant Restore from this snapshot"
                    style="background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(0, 229, 255, 0.2)); border-color: #10B981; color: #10B981; padding: 3px 8px; width: auto; font-size: 11px; font-weight: 700;"
                  >
                    ⚡ 1-Sec Restore
                  </button>
                  <button 
                    type="button" 
                    class="btn-icon-tiny" 
                    onclick="downloadServerSnapshot('${escapeHtml(snap.filename)}')" 
                    title="⬇️ Download Snapshot file"
                  >
                    <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                  </button>
                  <button 
                    type="button" 
                    class="btn-icon-tiny text-danger" 
                    onclick="deleteServerSnapshot('${escapeHtml(snap.filename)}')" 
                    title="Delete Snapshot"
                  >
                    <svg width="13" height="13" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                  </button>
                </div>
              </td>
            </tr>
          `;
        }).join('');
      }
    } catch (err) {
      console.error('Error fetching snapshots list:', err);
    }
  }

  if (openBtn) {
    openBtn.addEventListener('click', () => {
      selectedBackupFile = null;
      if (fileInput) fileInput.value = '';
      if (selectedName) selectedName.style.display = 'none';
      if (startRestoreBtn) startRestoreBtn.disabled = true;
      if (restoreProgress) restoreProgress.style.display = 'none';

      loadBackupStatsAndSnapshots();
      if (modal) modal.classList.add('active');
    });
  }

  [closeBtn, cancelBtn].forEach(btn => {
    if (btn) {
      btn.addEventListener('click', () => {
        if (modal) modal.classList.remove('active');
      });
    }
  });

  // 1-Click Instant Snapshot Creation on Server
  if (createSnapshotBtn) {
    createSnapshotBtn.addEventListener('click', async () => {
      const labelInput = document.getElementById('manualSnapshotLabelInput');
      const label = labelInput ? labelInput.value.trim() : '';

      createSnapshotBtn.disabled = true;
      createSnapshotBtn.textContent = '⏳ Creating Snapshot...';

      try {
        const res = await authFetch('/api/admin/backup/snapshots', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            type: 'manual',
            label: label || `Manual Backup (${new Date().toLocaleDateString()})`
          })
        });

        const json = await res.json();
        if (json.success) {
          showToast(`🎉 Snapshot created: ${json.data?.filename || 'Success'}!`);
          if (labelInput) labelInput.value = '';
          await loadBackupStatsAndSnapshots();
        } else {
          showToast(`⚠️ Error: ${json.error || 'Failed to create snapshot'}`);
        }
      } catch (err) {
        showToast(`⚠️ Error: ${err.message}`);
      } finally {
        createSnapshotBtn.disabled = false;
        createSnapshotBtn.innerHTML = '⚡ Create Snapshot Now';
      }
    });
  }

  // Refresh Snapshots list button
  if (refreshSnapshotsBtn) {
    refreshSnapshotsBtn.addEventListener('click', () => {
      loadSnapshotsList();
      showToast('Snapshots history refreshed!');
    });
  }

  // Save Auto-Backup Config
  if (saveAutoConfigBtn) {
    saveAutoConfigBtn.addEventListener('click', async () => {
      const enabled = document.getElementById('autoBackupEnabledCheck')?.checked || false;
      const frequency = document.getElementById('autoBackupFrequencySelect')?.value || 'daily';
      const retentionMaxSnapshots = Number(document.getElementById('autoBackupRetentionInput')?.value) || 14;

      try {
        const res = await authFetch('/api/admin/backup/config', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ enabled, frequency, retentionMaxSnapshots })
        });
        const json = await res.json();
        if (json.success) {
          showToast('✅ Auto-backup schedule settings saved!');
          loadBackupStatsAndSnapshots();
        } else {
          showToast(`⚠️ Error: ${json.error}`);
        }
      } catch (err) {
        showToast(`⚠️ Save error: ${err.message}`);
      }
    });
  }

  // Export 1-Click Full Backup (.ZIP)
  if (downloadFullBtn) {
    downloadFullBtn.addEventListener('click', () => {
      if (!adminToken) {
        showToast('Please sign in to download cloud backup');
        return;
      }
      showToast('📦 Preparing full cloud backup archive (.ZIP)...');
      window.location.href = `/api/admin/backup/download?token=${encodeURIComponent(adminToken)}`;
    });
  }

  // Export Database Only (.sqlite)
  if (downloadDbBtn) {
    downloadDbBtn.addEventListener('click', () => {
      if (!adminToken) {
        showToast('Please sign in to download cloud backup');
        return;
      }
      showToast('📄 Downloading rewall.sqlite database...');
      window.location.href = `/api/admin/backup/download?type=db-only&token=${encodeURIComponent(adminToken)}`;
    });
  }

  // Dropzone file handling
  if (dropzone && fileInput) {
    dropzone.addEventListener('click', () => fileInput.click());

    ['dragenter', 'dragover'].forEach(name => {
      dropzone.addEventListener(name, (e) => {
        e.preventDefault();
        dropzone.classList.add('dragover');
      });
    });

    ['dragleave', 'drop'].forEach(name => {
      dropzone.addEventListener(name, (e) => {
        e.preventDefault();
        dropzone.classList.remove('dragover');
      });
    });

    dropzone.addEventListener('drop', (e) => {
      if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
        handleSelectedBackup(e.dataTransfer.files[0]);
      }
    });

    fileInput.addEventListener('change', (e) => {
      if (e.target.files && e.target.files.length > 0) {
        handleSelectedBackup(e.target.files[0]);
      }
    });
  }

  function handleSelectedBackup(file) {
    const ext = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
    if (!['.zip', '.sqlite', '.db'].includes(ext)) {
      alert('Please upload a valid .zip or .sqlite file.');
      return;
    }
    selectedBackupFile = file;
    if (selectedName) {
      selectedName.textContent = `Selected: ${file.name} (${(file.size / 1024).toFixed(1)} KB)`;
      selectedName.style.display = 'block';
    }
    if (startRestoreBtn) {
      startRestoreBtn.disabled = false;
    }
  }

  // Restore Form Submission
  if (restoreForm) {
    restoreForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      if (!selectedBackupFile) {
        alert('Please select a backup .zip or .sqlite file first.');
        return;
      }

      const confirmed = confirm(
        '⚠️ ARE YOU SURE YOU WANT TO RESTORE THIS BACKUP?\n\n' +
        'Your current database will be archived to a .bak safety file and replaced with this backup in 1 second.\n' +
        'Server data will reload immediately.'
      );
      if (!confirmed) return;

      const formData = new FormData();
      formData.append('backupFile', selectedBackupFile);

      if (startRestoreBtn) startRestoreBtn.disabled = true;
      if (restoreProgress) restoreProgress.style.display = 'block';

      try {
        const res = await authFetch('/api/admin/backup/restore', {
          method: 'POST',
          body: formData
        });
        const json = await res.json();

        if (json.success) {
          showToast('⚡ 1-Second Instant Restore completed successfully!');
          if (modal) modal.classList.remove('active');
          // Reload all application data live
          await loadAllData();
        } else {
          alert('Restore error: ' + (json.error || 'Failed to restore backup'));
        }
      } catch (err) {
        alert('Server error while restoring backup: ' + err.message);
      } finally {
        if (startRestoreBtn) startRestoreBtn.disabled = false;
        if (restoreProgress) restoreProgress.style.display = 'none';
      }
    });
  }
}

// Global Instant 1-Second Server Snapshot Restore
async function restoreServerSnapshot(filename) {
  const confirmed = confirm(
    `⚡ 1-SECOND INSTANT RESTORE\n\n` +
    `Are you sure you want to restore snapshot "${filename}"?\n\n` +
    `• Instant hot-swap in <1 second\n` +
    `• Automatic .bak safety rollback created\n` +
    `• Database & all assets reloaded immediately`
  );
  if (!confirmed) return;

  try {
    showToast(`⚡ Restoring snapshot "${filename}"...`);
    const res = await authFetch(`/api/admin/backup/snapshots/${encodeURIComponent(filename)}/restore`, {
      method: 'POST'
    });
    const json = await res.json();

    if (json.success) {
      showToast(`🎉 ${json.message || 'Restored in 1 second!'}`);
      const modal = document.getElementById('backupRestoreModal');
      if (modal) modal.classList.remove('active');
      await loadAllData();
    } else {
      alert(`⚠️ Restore error: ${json.error || 'Failed to restore snapshot'}`);
    }
  } catch (err) {
    alert(`Server error while restoring snapshot: ${err.message}`);
  }
}

// Global Download Server Snapshot
function downloadServerSnapshot(filename) {
  if (!adminToken) {
    showToast('Please sign in to download backup');
    return;
  }
  showToast(`⬇️ Downloading ${filename}...`);
  window.location.href = `/api/admin/backup/snapshots/${encodeURIComponent(filename)}/download?token=${encodeURIComponent(adminToken)}`;
}

// Global Delete Server Snapshot
async function deleteServerSnapshot(filename) {
  if (!confirm(`Are you sure you want to delete snapshot "${filename}"?`)) return;

  try {
    const res = await authFetch(`/api/admin/backup/snapshots/${encodeURIComponent(filename)}`, {
      method: 'DELETE'
    });
    const json = await res.json();

    if (json.success) {
      showToast('🗑️ Snapshot deleted successfully.');
      initBackupRestore(); // Re-trigger load
      const refreshBtn = document.getElementById('refreshSnapshotsBtn');
      if (refreshBtn) refreshBtn.click();
    } else {
      showToast(`⚠️ Delete error: ${json.error}`);
    }
  } catch (err) {
    showToast(`⚠️ Delete error: ${err.message}`);
  }
}

window.restoreServerSnapshot = restoreServerSnapshot;
window.downloadServerSnapshot = downloadServerSnapshot;
window.deleteServerSnapshot = deleteServerSnapshot;

/* ==========================================================================
   ✂️ IN-BROWSER MOBILE CROP STUDIO (9:16 / 19.5:9 / WebP)
   ========================================================================== */
let currentCropper = null;
let currentCropCallback = null;
let currentOriginalFile = null;

function initImageCropper() {
  const modal = document.getElementById('imageCropModal');
  const closeBtn = document.getElementById('closeCropModal');
  const cancelBtn = document.getElementById('cancelCropBtn');
  const skipBtn = document.getElementById('skipCropBtn');
  const applyBtn = document.getElementById('applyCropBtn');

  const ratioPills = document.querySelectorAll('.aspect-pill');
  const ratioLabel = document.getElementById('cropRatioLabel');

  // Shortcut crop buttons inside wallpaper modal
  const cropPrevBtn = document.getElementById('cropExistingPreviewBtn');
  if (cropPrevBtn) {
    cropPrevBtn.addEventListener('click', () => {
      const url = document.getElementById('wpPreviewUrl').value.trim();
      if (!url) {
        document.getElementById('wpPreviewFileInput').click();
        return;
      }
      openImageCropper({
        imageUrl: url,
        onComplete: (croppedUrl) => {
          document.getElementById('wpPreviewUrl').value = croppedUrl;
          updatePreviewLayers();
        }
      });
    });
  }

  const cropFullBtn = document.getElementById('cropExistingFullBtn');
  if (cropFullBtn) {
    cropFullBtn.addEventListener('click', () => {
      const url = document.getElementById('wpFullUrl').value.trim();
      if (!url) {
        document.getElementById('wpFullFileInput').click();
        return;
      }
      openImageCropper({
        imageUrl: url,
        onComplete: (croppedUrl) => {
          document.getElementById('wpFullUrl').value = croppedUrl;
        }
      });
    });
  }

  // Close / Cancel modal
  const closeModal = () => {
    if (currentCropper) {
      currentCropper.destroy();
      currentCropper = null;
    }
    currentCropCallback = null;
    currentOriginalFile = null;
    if (modal) modal.classList.remove('active');
  };

  if (closeBtn) closeBtn.addEventListener('click', closeModal);
  if (cancelBtn) cancelBtn.addEventListener('click', closeModal);

  // Skip Crop -> upload original file directly as-is
  if (skipBtn) {
    skipBtn.addEventListener('click', async () => {
      const fileToUpload = currentOriginalFile;
      const callback = currentCropCallback;
      closeModal();

      if (fileToUpload && callback) {
        const uploadedUrl = await uploadFile(fileToUpload);
        if (uploadedUrl) {
          callback(uploadedUrl);
        }
      }
    });
  }

  // Aspect ratio presets (9:16 standard, 19.5:9 modern AMOLED, 1:1, 3:4, Free)
  ratioPills.forEach(pill => {
    pill.addEventListener('click', () => {
      ratioPills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');

      const ratioStr = pill.getAttribute('data-ratio');
      let ratio = NaN;
      if (ratioStr === '9/16') ratio = 9 / 16;
      else if (ratioStr === '19.5/9') ratio = 9 / 19.5;
      else if (ratioStr === '1/1') ratio = 1;
      else if (ratioStr === '3/4') ratio = 3 / 4;
      else ratio = NaN;

      if (ratioLabel) {
        ratioLabel.textContent = pill.textContent.trim().split(' ')[1] || 'Custom';
      }

      if (currentCropper) {
        currentCropper.setAspectRatio(ratio);
        updateLivePhonePreview();
      }
    });
  });

  // Transformation Tools (Zoom, Rotate, Flip, Reset)
  document.getElementById('cropZoomInBtn')?.addEventListener('click', () => currentCropper && currentCropper.zoom(0.1));
  document.getElementById('cropZoomOutBtn')?.addEventListener('click', () => currentCropper && currentCropper.zoom(-0.1));
  document.getElementById('cropRotateLeftBtn')?.addEventListener('click', () => currentCropper && currentCropper.rotate(-90));
  document.getElementById('cropRotateRightBtn')?.addEventListener('click', () => currentCropper && currentCropper.rotate(90));

  let scaleX = 1;
  let scaleY = 1;
  document.getElementById('cropFlipXBtn')?.addEventListener('click', () => {
    if (!currentCropper) return;
    scaleX = -scaleX;
    currentCropper.scaleX(scaleX);
  });
  document.getElementById('cropFlipYBtn')?.addEventListener('click', () => {
    if (!currentCropper) return;
    scaleY = -scaleY;
    currentCropper.scaleY(scaleY);
  });

  document.getElementById('cropResetBtn')?.addEventListener('click', () => {
    if (!currentCropper) return;
    scaleX = 1;
    scaleY = 1;
    currentCropper.reset();
  });

  // Apply Crop & Upload as WebP
  if (applyBtn) {
    applyBtn.addEventListener('click', async () => {
      if (!currentCropper) return;

      showToast('✂️ Processing high-resolution mobile crop...');
      applyBtn.disabled = true;

      try {
        const croppedCanvas = currentCropper.getCroppedCanvas({
          maxWidth: 2160,
          maxHeight: 3840,
          imageSmoothingEnabled: true,
          imageSmoothingQuality: 'high'
        });

        if (!croppedCanvas) {
          alert('Could not render cropped canvas.');
          applyBtn.disabled = false;
          return;
        }

        // Convert to lossless WebP blob
        croppedCanvas.toBlob(async (blob) => {
          if (!blob) {
            alert('Failed to generate image blob.');
            applyBtn.disabled = false;
            return;
          }

          const fileName = `wallpaper_mobile_crop_${Date.now()}.webp`;
          const croppedFile = new File([blob], fileName, { type: 'image/webp' });

          const callback = currentCropCallback;
          closeModal();

          const uploadedUrl = await uploadFile(croppedFile);
          if (uploadedUrl && callback) {
            callback(uploadedUrl);
            showToast('🎉 Mobile crop applied & uploaded as WebP!');
          }
        }, 'image/webp', 0.92);
      } catch (err) {
        console.error('Crop export error:', err);
        alert('Error applying crop: ' + err.message);
      } finally {
        applyBtn.disabled = false;
      }
    });
  }
}

function updateLivePhonePreview() {
  if (!currentCropper) return;
  const previewCanvas = document.getElementById('cropPreviewCanvas');
  const dimensionsText = document.getElementById('cropDimensionsText');
  if (!previewCanvas) return;

  try {
    const croppedCanvas = currentCropper.getCroppedCanvas({
      width: 440,
      height: 880,
      imageSmoothingEnabled: true,
      imageSmoothingQuality: 'medium'
    });

    if (croppedCanvas) {
      previewCanvas.width = croppedCanvas.width;
      previewCanvas.height = croppedCanvas.height;
      const ctx = previewCanvas.getContext('2d');
      ctx.clearRect(0, 0, previewCanvas.width, previewCanvas.height);
      ctx.drawImage(croppedCanvas, 0, 0);
    }

    const data = currentCropper.getData();
    if (dimensionsText && data) {
      dimensionsText.textContent = `${Math.round(data.width)} × ${Math.round(data.height)} px`;
    }
  } catch (_) {}
}

function openImageCropper({ file, imageUrl, onComplete }) {
  const modal = document.getElementById('imageCropModal');
  const targetImg = document.getElementById('cropperTargetImage');
  const skipBtn = document.getElementById('skipCropBtn');
  if (!modal || !targetImg) return;

  currentCropCallback = onComplete;
  currentOriginalFile = file || null;

  if (skipBtn) {
    skipBtn.style.display = file ? 'inline-flex' : 'none';
  }

  const loadAndInitCropper = (source) => {
    targetImg.src = source;
    modal.classList.add('active');

    if (currentCropper) {
      currentCropper.destroy();
      currentCropper = null;
    }

    // Reset aspect ratio buttons to 9:16 default
    document.querySelectorAll('.aspect-pill').forEach(p => {
      p.classList.toggle('active', p.getAttribute('data-ratio') === '9/16');
    });
    const ratioLabel = document.getElementById('cropRatioLabel');
    if (ratioLabel) ratioLabel.textContent = '9:16';

    targetImg.onload = () => {
      if (typeof Cropper === 'undefined') {
        console.error('Cropper.js is not loaded');
        alert('Cropper library is loading, please try again.');
        return;
      }

      currentCropper = new Cropper(targetImg, {
        aspectRatio: 9 / 16,
        viewMode: 1,
        dragMode: 'move',
        autoCropArea: 1,
        restore: false,
        guides: true,
        center: true,
        highlight: false,
        cropBoxMovable: true,
        cropBoxResizable: true,
        toggleDragModeOnDblclick: false,
        ready() {
          updateLivePhonePreview();
        },
        crop() {
          updateLivePhonePreview();
        }
      });
    };
  };

  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => loadAndInitCropper(e.target.result);
    reader.readAsDataURL(file);
  } else if (imageUrl) {
    loadAndInitCropper(imageUrl);
  }
}

// ===================================================
// 🎮 3D LIVE PARALLAX GYROSCOPE STUDIO ENGINE
// ===================================================

let studioActiveWp = null;
let studioLayers = []; // [{ id, imageUrl, depth, muted: false }]
let studioTargetTiltX = 0; // Deg
let studioTargetTiltY = 0;
let studioCurrentTiltX = 0;
let studioCurrentTiltY = 0;
let studioSensitivity = 1.2;
let studioZDist = 120;
let studioInvertX = false;
let studioInvertY = false;
let studioEnableGlare = true;
let studioExploded = false;
let studioAutoOrbit = false;
let studioWeatherEffect = 'none';
let studioRafId = null;
let studioOrbitAngle = 0;
let studioFpsTimer = performance.now();
let studioFramesCount = 0;
let studioWeatherParticles = [];
let studioInitialized = false;

function initParallaxStudio() {
  if (studioInitialized) return;
  studioInitialized = true;

  const modal = document.getElementById('parallaxStudioModal');
  const closeBtn = document.getElementById('closeParallaxStudioModal');
  const phoneFrame = document.getElementById('studioPhoneFrame');
  const wpSelect = document.getElementById('studioWallpaperSelect');
  const explodedBtn = document.getElementById('studioExplodedBtn');
  const autoOrbitBtn = document.getElementById('studioAutoOrbitBtn');
  const sensSlider = document.getElementById('studioSensSlider');
  const sensValue = document.getElementById('studioSensValue');
  const zDistSlider = document.getElementById('studioZDistSlider');
  const zDistValue = document.getElementById('studioZDistValue');
  const invertXCheck = document.getElementById('studioInvertX');
  const invertYCheck = document.getElementById('studioInvertY');
  const glareCheck = document.getElementById('studioEnableGlare');
  const recenterBtn = document.getElementById('studioResetTiltBtn');
  const saveDepthsBtn = document.getElementById('studioSaveDepthsBtn');

  // Close modal
  if (closeBtn) {
    closeBtn.addEventListener('click', closeParallaxStudio);
  }
  if (modal) {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) closeParallaxStudio();
    });
  }

  // Interactive Gyro Tilt on Phone Frame (Mouse Move)
  if (phoneFrame) {
    phoneFrame.addEventListener('mousemove', (e) => {
      if (studioAutoOrbit) return;
      const rect = phoneFrame.getBoundingClientRect();
      const normX = (e.clientX - rect.left) / rect.width - 0.5;
      const normY = (e.clientY - rect.top) / rect.height - 0.5;

      const multX = studioInvertX ? -1 : 1;
      const multY = studioInvertY ? -1 : 1;

      studioTargetTiltX = normX * 36 * studioSensitivity * multX;
      studioTargetTiltY = normY * 36 * studioSensitivity * multY;
    });

    phoneFrame.addEventListener('mouseleave', () => {
      if (!studioAutoOrbit) {
        studioTargetTiltX = 0;
        studioTargetTiltY = 0;
      }
    });

    // Touch support for mobile / tablets
    phoneFrame.addEventListener('touchmove', (e) => {
      if (e.touches.length > 0 && !studioAutoOrbit) {
        const touch = e.touches[0];
        const rect = phoneFrame.getBoundingClientRect();
        const normX = (touch.clientX - rect.left) / rect.width - 0.5;
        const normY = (touch.clientY - rect.top) / rect.height - 0.5;
        const multX = studioInvertX ? -1 : 1;
        const multY = studioInvertY ? -1 : 1;
        studioTargetTiltX = normX * 36 * studioSensitivity * multX;
        studioTargetTiltY = normY * 36 * studioSensitivity * multY;
        e.preventDefault();
      }
    }, { passive: false });

    phoneFrame.addEventListener('touchend', () => {
      if (!studioAutoOrbit) {
        studioTargetTiltX = 0;
        studioTargetTiltY = 0;
      }
    });
  }

  // Wallpaper Switcher dropdown
  if (wpSelect) {
    wpSelect.addEventListener('change', (e) => {
      const selectedId = e.target.value;
      const found = allWallpapers.find(w => w.id === selectedId);
      if (found) {
        loadWallpaperIntoStudio(found);
      }
    });
  }

  // Exploded View Slicer
  if (explodedBtn) {
    explodedBtn.addEventListener('click', () => {
      studioExploded = !studioExploded;
      explodedBtn.classList.toggle('active', studioExploded);
      const stack = document.getElementById('studioLayersContainer');
      const hudMode = document.getElementById('hudModeText');
      if (stack) stack.classList.toggle('exploded-mode', studioExploded);
      if (hudMode) hudMode.textContent = studioExploded ? 'Exploded 3D Slicer' : '3D Parallax';
    });
  }

  // Auto-Orbit Continuous Gyro
  if (autoOrbitBtn) {
    autoOrbitBtn.addEventListener('click', () => {
      studioAutoOrbit = !studioAutoOrbit;
      autoOrbitBtn.classList.toggle('active', studioAutoOrbit);
      if (!studioAutoOrbit) {
        studioTargetTiltX = 0;
        studioTargetTiltY = 0;
      }
    });
  }

  // Sensitivity Slider
  if (sensSlider) {
    sensSlider.addEventListener('input', (e) => {
      studioSensitivity = parseFloat(e.target.value);
      if (sensValue) sensValue.textContent = `${studioSensitivity.toFixed(1)}x`;
    });
  }

  // Z-Spread Slider
  if (zDistSlider) {
    zDistSlider.addEventListener('input', (e) => {
      studioZDist = parseInt(e.target.value, 10);
      if (zDistValue) zDistValue.textContent = `${studioZDist}px`;
    });
  }

  // Invert Toggles
  if (invertXCheck) {
    invertXCheck.addEventListener('change', (e) => {
      studioInvertX = e.target.checked;
    });
  }
  if (invertYCheck) {
    invertYCheck.addEventListener('change', (e) => {
      studioInvertY = e.target.checked;
    });
  }
  if (glareCheck) {
    glareCheck.addEventListener('change', (e) => {
      studioEnableGlare = e.target.checked;
    });
  }

  // Recenter Tilt
  if (recenterBtn) {
    recenterBtn.addEventListener('click', () => {
      studioTargetTiltX = 0;
      studioTargetTiltY = 0;
      studioCurrentTiltX = 0;
      studioCurrentTiltY = 0;
      if (studioAutoOrbit) {
        studioAutoOrbit = false;
        if (autoOrbitBtn) autoOrbitBtn.classList.remove('active');
      }
    });
  }

  // Save Layer Depths to Backend
  if (saveDepthsBtn) {
    saveDepthsBtn.addEventListener('click', saveStudioLayerDepths);
  }

  // Weather pill selector
  document.querySelectorAll('.weather-pill-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.weather-pill-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      studioWeatherEffect = btn.getAttribute('data-weather') || 'none';
      initStudioWeatherParticles();
    });
  });
}

function openParallaxStudio(wallpaperId) {
  initParallaxStudio();

  const modal = document.getElementById('parallaxStudioModal');
  const wpSelect = document.getElementById('studioWallpaperSelect');

  // Populate wallpaper dropdown
  if (wpSelect && allWallpapers.length > 0) {
    wpSelect.innerHTML = allWallpapers.map(w => {
      const is3D = w.isParallax && w.layers && w.layers.length > 0;
      const tag = is3D ? `[3D - ${w.layers.length} Layers]` : '[2D Static]';
      return `<option value="${w.id}">${tag} ${w.title} (${w.category})</option>`;
    }).join('');
  }

  let targetWp = null;
  if (wallpaperId) {
    targetWp = allWallpapers.find(w => w.id === wallpaperId);
  }
  if (!targetWp) {
    targetWp = allWallpapers.find(w => w.isParallax && w.layers && w.layers.length > 0) || allWallpapers[0];
  }

  if (targetWp) {
    if (wpSelect) wpSelect.value = targetWp.id;
    loadWallpaperIntoStudio(targetWp);
  }

  if (modal) {
    modal.classList.add('active');
  }

  // Start 60 FPS RAF Animation Loop
  if (!studioRafId) {
    studioFpsTimer = performance.now();
    studioFramesCount = 0;
    studioRafId = requestAnimationFrame(studioAnimationLoop);
  }
}

function closeParallaxStudio() {
  const modal = document.getElementById('parallaxStudioModal');
  if (modal) modal.classList.remove('active');

  if (studioRafId) {
    cancelAnimationFrame(studioRafId);
    studioRafId = null;
  }
}

function loadWallpaperIntoStudio(wp) {
  studioActiveWp = wp;
  const layersContainer = document.getElementById('studioLayersContainer');
  const layersList = document.getElementById('studioLayersList');
  const hudLayersCount = document.getElementById('hudLayersCount');

  // Prepare layers data
  if (wp.layers && wp.layers.length > 0) {
    studioLayers = wp.layers.map((l, i) => ({
      id: l.id || `layer_${i}`,
      imageUrl: l.imageUrl,
      depth: parseFloat(l.depth || 0.5),
      sortOrder: i,
      muted: false
    }));
  } else {
    studioLayers = [
      { id: 'l_bg', imageUrl: wp.fullUrl || wp.previewUrl, depth: 0.25, sortOrder: 0, muted: false }
    ];
  }

  if (hudLayersCount) {
    hudLayersCount.textContent = `${studioLayers.length} Active`;
  }

  // Render 3D phone stage layers
  if (layersContainer) {
    layersContainer.innerHTML = studioLayers.map((l, idx) => `
      <img src="${l.imageUrl}" 
           class="studio-parallax-layer" 
           id="studioLayerEl_${idx}"
           data-idx="${idx}"
           data-depth="${l.depth}" 
           alt="Layer ${idx + 1}">
    `).join('');
  }

  // Render Layer Depth Inspector cards
  if (layersList) {
    layersList.innerHTML = studioLayers.map((l, idx) => {
      const layerName = idx === 0 
        ? 'Background (Base)' 
        : idx === studioLayers.length - 1 
          ? 'Foreground (Near)' 
          : `Midground (Layer ${idx + 1})`;
      return `
        <div class="studio-layer-item" id="studioLayerCard_${idx}">
          <img src="${l.imageUrl}" class="studio-layer-thumb" alt="">
          <div class="studio-layer-info">
            <div class="studio-layer-name">
              <span>${layerName}</span>
              <strong id="studioDepthVal_${idx}" class="text-cyan">${l.depth.toFixed(2)}</strong>
            </div>
            <input type="range" 
                   class="studio-slider studio-depth-input" 
                   data-idx="${idx}" 
                   min="0.05" 
                   max="1.0" 
                   step="0.05" 
                   value="${l.depth}" 
                   oninput="updateStudioLayerDepth(${idx}, this.value)">
          </div>
          <button type="button" 
                  class="studio-layer-eye-btn" 
                  id="studioEyeBtn_${idx}" 
                  onclick="toggleStudioLayerMute(${idx})" 
                  title="Toggle Layer Visibility">👁️</button>
        </div>
      `;
    }).join('');
  }

  initStudioWeatherParticles();
}

function updateStudioLayerDepth(idx, newDepthVal) {
  const depth = parseFloat(newDepthVal);
  if (studioLayers[idx]) {
    studioLayers[idx].depth = depth;
  }
  const valLabel = document.getElementById(`studioDepthVal_${idx}`);
  if (valLabel) valLabel.textContent = depth.toFixed(2);

  const layerEl = document.getElementById(`studioLayerEl_${idx}`);
  if (layerEl) layerEl.dataset.depth = depth;
}

function toggleStudioLayerMute(idx) {
  if (!studioLayers[idx]) return;
  studioLayers[idx].muted = !studioLayers[idx].muted;
  const layerEl = document.getElementById(`studioLayerEl_${idx}`);
  const eyeBtn = document.getElementById(`studioEyeBtn_${idx}`);

  if (layerEl) {
    layerEl.classList.toggle('muted', studioLayers[idx].muted);
  }
  if (eyeBtn) {
    eyeBtn.textContent = studioLayers[idx].muted ? '👁️‍🗨️' : '👁️';
    eyeBtn.style.opacity = studioLayers[idx].muted ? '0.4' : '1';
  }
}

async function saveStudioLayerDepths() {
  if (!studioActiveWp) return;
  const token = localStorage.getItem('rewall_token');
  if (!token) {
    alert('Session expired. Please log in again.');
    return;
  }

  try {
    const payload = {
      layers: studioLayers.map((l, i) => ({
        id: l.id,
        imageUrl: l.imageUrl,
        depth: l.depth,
        sortOrder: i
      }))
    };

    const res = await fetch(`/api/admin/wallpapers/${studioActiveWp.id}/layers`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    });

    const json = await res.json();
    if (json.success) {
      showToast('✅ 3D Parallax layer depths successfully saved to Cloud!');
      studioActiveWp.layers = payload.layers;
      studioActiveWp.isParallax = true;
      renderWallpapersTable();
    } else {
      alert('Error saving layers: ' + (json.error || 'Unknown error'));
    }
  } catch (err) {
    console.error('Error saving studio layer depths:', err);
    alert('Failed to save layer depths: ' + err.message);
  }
}

function studioAnimationLoop(timestamp) {
  studioRafId = requestAnimationFrame(studioAnimationLoop);

  // 1. Auto-Orbit Continuous Gyro Simulation
  if (studioAutoOrbit) {
    studioOrbitAngle += 0.024;
    studioTargetTiltX = Math.sin(studioOrbitAngle) * 15;
    studioTargetTiltY = Math.cos(studioOrbitAngle * 0.75) * 12;
  }

  // 2. Spring Inertia Damping (Lerp factor 0.12)
  const lerp = 0.12;
  studioCurrentTiltX += (studioTargetTiltX - studioCurrentTiltX) * lerp;
  studioCurrentTiltY += (studioTargetTiltY - studioCurrentTiltY) * lerp;

  // 3. Chassis 3D Rotation
  const chassis = document.getElementById('studioParallaxChassis');
  if (chassis && !studioExploded) {
    chassis.style.transform = `rotateX(${-studioCurrentTiltY}deg) rotateY(${studioCurrentTiltX}deg)`;
  } else if (chassis && studioExploded) {
    chassis.style.transform = 'none';
  }

  // 4. Update Each Layer along Z and X/Y with Depth
  const layerEls = document.querySelectorAll('.studio-parallax-layer');
  layerEls.forEach((el, idx) => {
    const depth = parseFloat(el.dataset.depth || '0.5');
    if (studioExploded) {
      const zOffset = (idx + 1) * 70;
      el.style.transform = `translateZ(${zOffset}px) scale(0.92)`;
    } else {
      const shiftX = studioCurrentTiltX * depth * 2.2;
      const shiftY = studioCurrentTiltY * depth * 2.2;
      const zDistance = (depth - 0.4) * studioZDist;
      el.style.transform = `translate3d(${shiftX}px, ${shiftY}px, ${zDistance}px) scale(1.15)`;
    }
  });

  // 5. Specular Glare / Dynamic Lighting Sheen
  const glareEl = document.getElementById('studioSpecularGlare');
  if (glareEl && studioEnableGlare && !studioExploded) {
    const glareX = 50 - studioCurrentTiltX * 1.8;
    const glareY = 50 - studioCurrentTiltY * 1.8;
    glareEl.style.background = `radial-gradient(circle at ${glareX}% ${glareY}%, rgba(255,255,255,0.32) 0%, rgba(255,255,255,0.06) 42%, transparent 70%)`;
    glareEl.style.display = 'block';
  } else if (glareEl) {
    glareEl.style.display = 'none';
  }

  // 6. Real-time Weather Canvas rendering on 3D stage
  renderStudioWeather();

  // 7. Telemetry HUD
  const hudCoords = document.getElementById('hudGyroCoords');
  if (hudCoords) {
    const xSign = studioCurrentTiltX >= 0 ? '+' : '';
    const ySign = studioCurrentTiltY >= 0 ? '+' : '';
    hudCoords.textContent = `X: ${xSign}${studioCurrentTiltX.toFixed(1)}°  Y: ${ySign}${studioCurrentTiltY.toFixed(1)}°`;
  }

  // 8. FPS Counter
  studioFramesCount++;
  if (timestamp - studioFpsTimer >= 1000) {
    const fpsText = document.getElementById('hudFpsText');
    if (fpsText) fpsText.textContent = `${studioFramesCount} FPS`;
    studioFramesCount = 0;
    studioFpsTimer = timestamp;
  }
}

function initStudioWeatherParticles() {
  studioWeatherParticles = [];
  const canvas = document.getElementById('studioWeatherOverlayCanvas');
  if (!canvas) return;

  canvas.width = 310;
  canvas.height = 580;

  if (studioWeatherEffect === 'none') return;

  const count = studioWeatherEffect === 'rain' ? 80
              : studioWeatherEffect === 'snow' ? 50
              : studioWeatherEffect === 'thunder' ? 100
              : studioWeatherEffect === 'sakura' ? 35
              : 20;

  for (let i = 0; i < count; i++) {
    studioWeatherParticles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * 1.5,
      vy: studioWeatherEffect === 'rain' || studioWeatherEffect === 'thunder' ? 10 + Math.random() * 8
        : studioWeatherEffect === 'snow' ? 1.2 + Math.random() * 2
        : studioWeatherEffect === 'sakura' ? 1.5 + Math.random() * 2
        : 0.4 + Math.random() * 0.6,
      size: studioWeatherEffect === 'snow' ? 2 + Math.random() * 3
          : studioWeatherEffect === 'sakura' ? 7 + Math.random() * 6
          : studioWeatherEffect === 'fog' ? 40 + Math.random() * 50
          : 15 + Math.random() * 12,
      opacity: 0.3 + Math.random() * 0.6,
      angle: Math.random() * Math.PI * 2,
      spin: (Math.random() - 0.5) * 0.04
    });
  }
}

function renderStudioWeather() {
  const canvas = document.getElementById('studioWeatherOverlayCanvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  if (studioWeatherEffect === 'none' || studioWeatherParticles.length === 0) return;

  if (studioWeatherEffect === 'thunder' && Math.random() < 0.015) {
    ctx.fillStyle = 'rgba(216, 180, 254, 0.4)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
  }

  studioWeatherParticles.forEach(p => {
    p.y += p.vy;
    p.x += p.vx;
    p.angle += p.spin;

    if (p.y > canvas.height + 20) {
      p.y = -20;
      p.x = Math.random() * canvas.width;
    }
    if (p.x < -30) p.x = canvas.width + 30;
    if (p.x > canvas.width + 30) p.x = -30;

    ctx.save();
    if (studioWeatherEffect === 'rain' || studioWeatherEffect === 'thunder') {
      ctx.strokeStyle = studioWeatherEffect === 'thunder' ? 'rgba(0, 229, 255, 0.65)' : 'rgba(255, 255, 255, 0.55)';
      ctx.lineWidth = 1.2;
      ctx.beginPath();
      ctx.moveTo(p.x, p.y);
      ctx.lineTo(p.x - 2, p.y + p.size);
      ctx.stroke();
    } else if (studioWeatherEffect === 'snow') {
      ctx.fillStyle = `rgba(255, 255, 255, ${p.opacity})`;
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      ctx.fill();
    } else if (studioWeatherEffect === 'sakura') {
      ctx.translate(p.x, p.y);
      ctx.rotate(p.angle);
      ctx.fillStyle = `rgba(244, 114, 182, ${p.opacity})`;
      ctx.beginPath();
      ctx.ellipse(0, 0, p.size, p.size * 0.5, 0, 0, Math.PI * 2);
      ctx.fill();
    } else if (studioWeatherEffect === 'fog') {
      const grad = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.size);
      grad.addColorStop(0, 'rgba(200, 210, 230, 0.12)');
      grad.addColorStop(1, 'rgba(200, 210, 230, 0)');
      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
      ctx.fill();
    }
    ctx.restore();
  });
}

// ==========================================================================
// CONNECTED DEVICES & GEO MAP TELEMETRY MODULE
// ==========================================================================

let telemetryDevices = [];
let telemetryBrandChart = null;
let telemetryOsChart = null;
let currentMapRegion = 'global';

function initDeviceTelemetry() {
  const refreshBtn = document.getElementById('refreshTelemetryBtn');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', async () => {
      await loadTelemetryData();
      showToast('Device fleet telemetry refreshed!');
    });
  }

  const broadcastBtn = document.getElementById('broadcastPingBtn');
  if (broadcastBtn) {
    broadcastBtn.addEventListener('click', async () => {
      showToast('📡 Broadcasting heartbeat ping to all devices...');
      setTimeout(() => {
        showToast('✓ Heartbeat received from ' + telemetryDevices.length + ' active devices.');
      }, 700);
    });
  }

  // Filter controls
  const searchInput = document.getElementById('deviceSearchInput');
  if (searchInput) {
    searchInput.addEventListener('input', () => {
      renderDevicesTable();
    });
  }

  const brandFilter = document.getElementById('deviceBrandFilter');
  if (brandFilter) {
    brandFilter.addEventListener('change', () => {
      renderDevicesTable();
    });
  }

  const statusFilter = document.getElementById('deviceStatusFilter');
  if (statusFilter) {
    statusFilter.addEventListener('change', () => {
      renderDevicesTable();
    });
  }

  // Map Region Pills
  const mapPills = document.querySelectorAll('.map-pill');
  mapPills.forEach(pill => {
    pill.addEventListener('click', () => {
      mapPills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      const region = pill.dataset.region;
      setMapRegion(region);
    });
  });
}

function setMapRegion(region) {
  currentMapRegion = region;
  const svg = document.getElementById('worldMapSvg');
  if (!svg) return;

  if (region === 'india') {
    // Zoom focus on India subcontinent
    svg.setAttribute('viewBox', '590 140 240 180');
  } else if (region === 'west') {
    // Zoom focus on North America and Europe
    svg.setAttribute('viewBox', '60 40 560 280');
  } else {
    // Global standard
    svg.setAttribute('viewBox', '0 0 1000 500');
  }
}

async function loadTelemetryData() {
  try {
    const res = await authFetch('/api/admin/telemetry');
    const json = await res.json();
    if (!json.success || !json.data) return;

    const data = json.data;
    telemetryDevices = (data.devices || []).map(d => ({
      ...d,
      online: d.online !== undefined ? d.online : (d.isOnline !== undefined ? d.isOnline : true)
    }));

    // 1. Update KPI Summary Cards
    const totalEl = document.getElementById('telemetryTotalDevices');
    if (totalEl) totalEl.textContent = data.summary.totalDevices || 0;

    const onlineEl = document.getElementById('telemetryOnlineDevices');
    if (onlineEl) onlineEl.textContent = data.summary.onlineDevices || 0;

    const brandEl = document.getElementById('telemetryTopBrand');
    if (brandEl) brandEl.textContent = data.summary.topBrand || '--';

    const osEl = document.getElementById('telemetryTopOs');
    if (osEl) osEl.textContent = data.summary.topOs || data.summary.topAndroidVersion || '--';

    const liveBadge = document.getElementById('geoLiveCountBadge');
    if (liveBadge) {
      liveBadge.textContent = `● ${data.summary.onlineDevices} Online / ${data.summary.totalDevices} Total`;
    }

    // 2. Populate Brand Filter options if empty
    populateBrandFilterOptions();

    // 3. Render Geo Map
    renderGeoMap(data.geoPings || []);

    // 4. Render Telemetry Charts
    renderTelemetryCharts(data.brandStats || [], data.osStats || []);

    // 5. Render Devices Ledger Table
    renderDevicesTable();

  } catch (err) {
    console.error('Failed to load telemetry data:', err);
  }
}

function populateBrandFilterOptions() {
  const brandFilter = document.getElementById('deviceBrandFilter');
  if (!brandFilter) return;

  const currentVal = brandFilter.value;
  const brands = [...new Set(telemetryDevices.map(d => d.brand || d.manufacturer).filter(Boolean))].sort();

  brandFilter.innerHTML = '<option value="ALL">All Brands</option>';
  brands.forEach(b => {
    const opt = document.createElement('option');
    opt.value = b;
    opt.textContent = b;
    if (b === currentVal) opt.selected = true;
    brandFilter.appendChild(opt);
  });
}

function getCountryFlag(code) {
  if (!code) return '🌐';
  const flags = {
    'IN': '🇮🇳', 'US': '🇺🇸', 'GB': '🇬🇧', 'DE': '🇩🇪',
    'AE': '🇦🇪', 'JP': '🇯🇵', 'FR': '🇫🇷', 'SG': '🇸🇬',
    'CA': '🇨🇦', 'AU': '🇦🇺', 'BR': '🇧🇷', 'KR': '🇰🇷'
  };
  return flags[code.toUpperCase()] || '🌐';
}

function renderGeoMap(pings) {
  const nodesLayer = document.getElementById('geoNodesLayer');
  const tooltip = document.getElementById('geoMapTooltip');
  const viewport = document.getElementById('geoMapViewport');
  if (!nodesLayer) return;

  nodesLayer.innerHTML = '';

  pings.forEach((ping, idx) => {
    const lat = Number(ping.latitude) || 0;
    const lon = Number(ping.longitude) || 0;

    // Equirectangular projection onto 1000x500 SVG canvas
    const x = Math.max(15, Math.min(985, ((lon + 180) / 360) * 1000));
    const y = Math.max(15, Math.min(485, ((90 - lat) / 180) * 500));

    const g = document.createElementNS('http://www.w3.org/2000/svg', 'g');
    g.setAttribute('class', `geo-node ${ping.online ? 'online' : 'offline'}`);
    g.setAttribute('data-device-id', ping.deviceId);

    // Pulse wave ring
    if (ping.online) {
      const pulse = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
      pulse.setAttribute('class', 'geo-node-pulse');
      pulse.setAttribute('cx', x);
      pulse.setAttribute('cy', y);
      pulse.setAttribute('r', '4');
      pulse.style.animationDelay = `${(idx * 0.18) % 2.4}s`;
      g.appendChild(pulse);
    }

    // Core circle
    const core = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    core.setAttribute('class', 'geo-node-core');
    core.setAttribute('cx', x);
    core.setAttribute('cy', y);
    core.setAttribute('r', ping.online ? '5.5' : '4');
    g.appendChild(core);

    // Tooltip interaction
    g.addEventListener('mouseenter', (e) => {
      if (!tooltip || !viewport) return;
      const flag = getCountryFlag(ping.countryCode);
      const onlineTag = ping.online ? '<span style="color:#10b981;">🟢 Online (Active)</span>' : '<span style="color:#94a3b8;">⚪ Offline</span>';

      tooltip.innerHTML = `
        <div class="tooltip-title">${flag} ${ping.city || 'Tech Node'}, ${ping.country || 'Global'}</div>
        <div class="tooltip-row"><span>Device:</span><span class="tooltip-val">${ping.manufacturer || ''} ${ping.deviceModel}</span></div>
        <div class="tooltip-row"><span>OS Fleet:</span><span class="tooltip-val">${ping.androidVersion || 'Android'} (API ${ping.apiLevel || '-'})</span></div>
        <div class="tooltip-row"><span>Display:</span><span class="tooltip-val">${ping.screenResolution || '-'} • ${ping.screenDpi || '-'} DPI</span></div>
        <div class="tooltip-row"><span>Status:</span><span class="tooltip-val">${onlineTag}</span></div>
        <div class="tooltip-row"><span>Battery:</span><span class="tooltip-val">🔋 ${ping.batteryLevel || '--'}%</span></div>
      `;
      tooltip.style.display = 'block';
      tooltip.style.opacity = '1';
    });

    g.addEventListener('mousemove', (e) => {
      if (!tooltip || !viewport) return;
      const rect = viewport.getBoundingClientRect();
      const mouseX = e.clientX - rect.left;
      const mouseY = e.clientY - rect.top;

      let posX = mouseX + 14;
      let posY = mouseY + 14;

      if (posX + 220 > rect.width) {
        posX = mouseX - 220;
      }
      if (posY + 160 > rect.height) {
        posY = mouseY - 160;
      }

      tooltip.style.left = `${posX}px`;
      tooltip.style.top = `${posY}px`;
    });

    g.addEventListener('mouseleave', () => {
      if (!tooltip) return;
      tooltip.style.display = 'none';
      tooltip.style.opacity = '0';
    });

    g.addEventListener('click', () => {
      // Highlight in search input
      const searchInput = document.getElementById('deviceSearchInput');
      if (searchInput) {
        searchInput.value = ping.deviceModel;
        renderDevicesTable();
        showToast(`Filtered ledger by ${ping.deviceModel}`);
      }
    });

    nodesLayer.appendChild(g);
  });
}

function renderTelemetryCharts(brandData, osData) {
  if (typeof Chart === 'undefined') return;

  // 1. Device Brand Distribution Doughnut Chart
  const brandCanvas = document.getElementById('deviceBrandChart');
  if (brandCanvas) {
    if (telemetryBrandChart) {
      telemetryBrandChart.destroy();
    }

    const palette = ['#00e5ff', '#a855f7', '#10b981', '#f59e0b', '#ec4899', '#3b82f6', '#6366f1', '#14b8a6'];
    const labels = brandData.map(b => b.brand || 'Other');
    const values = brandData.map(b => b.count);

    telemetryBrandChart = new Chart(brandCanvas, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: values,
          backgroundColor: palette.slice(0, labels.length),
          borderColor: '#0a101d',
          borderWidth: 2,
          hoverOffset: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '62%',
        plugins: {
          legend: {
            position: 'right',
            labels: {
              color: '#94a3b8',
              font: { family: 'Plus Jakarta Sans', size: 12, weight: 600 },
              padding: 14,
              boxWidth: 12,
              usePointStyle: true
            }
          },
          tooltip: {
            backgroundColor: 'rgba(10, 18, 32, 0.95)',
            borderColor: 'rgba(0, 229, 255, 0.3)',
            borderWidth: 1,
            titleFont: { family: 'Plus Jakarta Sans', weight: 'bold' },
            bodyFont: { family: 'Plus Jakarta Sans' },
            padding: 10,
            cornerRadius: 8
          }
        }
      }
    });
  }

  // 2. Android OS Version Adoption Bar Chart
  const osCanvas = document.getElementById('deviceOsChart');
  if (osCanvas) {
    if (telemetryOsChart) {
      telemetryOsChart.destroy();
    }

    const osLabels = osData.map(o => o.os || 'Android');
    const osValues = osData.map(o => o.count);

    telemetryOsChart = new Chart(osCanvas, {
      type: 'bar',
      data: {
        labels: osLabels,
        datasets: [{
          label: 'Active Devices',
          data: osValues,
          backgroundColor: 'rgba(0, 229, 255, 0.35)',
          borderColor: '#00e5ff',
          borderWidth: 1.5,
          borderRadius: 6,
          hoverBackgroundColor: 'rgba(0, 229, 255, 0.65)'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: 'rgba(10, 18, 32, 0.95)',
            borderColor: 'rgba(0, 229, 255, 0.3)',
            borderWidth: 1,
            titleFont: { family: 'Plus Jakarta Sans', weight: 'bold' },
            bodyFont: { family: 'Plus Jakarta Sans' },
            padding: 10,
            cornerRadius: 8
          }
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: {
              color: '#94a3b8',
              font: { family: 'Plus Jakarta Sans', size: 11.5, weight: 600 }
            }
          },
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1,
              color: '#64748b',
              font: { family: 'Plus Jakarta Sans', size: 11 }
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.05)'
            }
          }
        }
      }
    });
  }
}

function formatRelativeTime(ts) {
  if (!ts) return 'Unknown';
  const diff = Date.now() - Number(ts);
  if (isNaN(diff)) return 'Recently';
  const seconds = Math.floor(diff / 1000);
  if (seconds < 60) return 'Just now';
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

function renderDevicesTable() {
  const tbody = document.getElementById('devicesTableBody');
  if (!tbody) return;

  const searchQuery = (document.getElementById('deviceSearchInput')?.value || '').toLowerCase().trim();
  const brandFilter = document.getElementById('deviceBrandFilter')?.value || 'ALL';
  const statusFilter = document.getElementById('deviceStatusFilter')?.value || 'ALL';

  const filtered = telemetryDevices.filter(d => {
    // Brand filter
    if (brandFilter !== 'ALL' && (d.brand || d.manufacturer) !== brandFilter) return false;

    // Status filter
    if (statusFilter === 'ONLINE' && !d.online) return false;
    if (statusFilter === 'OFFLINE' && d.online) return false;

    // Search query
    if (searchQuery) {
      const match = (
        (d.deviceModel || '').toLowerCase().includes(searchQuery) ||
        (d.manufacturer || '').toLowerCase().includes(searchQuery) ||
        (d.city || '').toLowerCase().includes(searchQuery) ||
        (d.country || '').toLowerCase().includes(searchQuery) ||
        (d.ipAddress || '').toLowerCase().includes(searchQuery) ||
        (d.deviceId || '').toLowerCase().includes(searchQuery) ||
        (d.androidVersion || '').toLowerCase().includes(searchQuery)
      );
      if (!match) return false;
    }

    return true;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" style="text-align:center; padding:32px; color:#94a3b8;">
          <div style="font-size:24px; margin-bottom:6px;">🔍</div>
          No connected devices match your filters.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = filtered.map(dev => {
    const statusBadge = dev.online
      ? `<span class="device-online-badge"><span class="status-dot online pulsing"></span> Online</span>`
      : `<span class="device-offline-badge"><span class="status-dot"></span> Offline</span>`;

    const flag = getCountryFlag(dev.countryCode);
    const batt = dev.batteryLevel != null ? Number(dev.batteryLevel) : 100;
    let battClass = 'battery-high';
    if (batt < 25) battClass = 'battery-low';
    else if (batt < 50) battClass = 'battery-mid';

    const lastPingText = formatRelativeTime(dev.lastPingAt);

    return `
      <tr>
        <td>${statusBadge}</td>
        <td>
          <div style="font-weight:700; color:#fff; font-size:13.5px;">${dev.deviceModel}</div>
          <div class="text-muted" style="font-size:11px; margin-top:2px;">
            ${dev.manufacturer} • <span style="font-family:monospace; opacity:0.8;">${dev.deviceId.substring(0, 8)}...</span>
          </div>
        </td>
        <td>
          <span class="os-pill">${dev.androidVersion}</span>
          <span class="text-muted" style="font-size:11px; margin-left:4px;">API ${dev.apiLevel}</span>
        </td>
        <td>
          <div style="font-size:12px; font-weight:600; color:#e2e8f0;">${dev.screenResolution || '-'}</div>
          <div class="text-muted" style="font-size:11px;">${dev.screenDpi || '-'} DPI</div>
        </td>
        <td>
          <div style="font-size:12.5px; font-weight:600; color:#fff;">${flag} ${dev.city || 'Unknown'}</div>
          <div class="text-muted" style="font-size:11px;">${dev.country || 'Global'} (${dev.ipAddress || 'LAN'})</div>
        </td>
        <td>
          <span class="battery-pill ${battClass}">
            🔋 ${batt}%
          </span>
        </td>
        <td>
          <div style="font-size:12px; color:#e2e8f0;">${lastPingText}</div>
        </td>
        <td>
          <button class="btn-ping" onclick="sendDeviceTestPing('${dev.deviceId}')" title="Send test ping / heartbeat to this phone">
            <svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>
            Ping
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

window.sendDeviceTestPing = async function(deviceId) {
  try {
    const res = await authFetch(`/api/admin/devices/${encodeURIComponent(deviceId)}/ping`, {
      method: 'POST'
    });
    const json = await res.json();
    if (json.success) {
      showToast(`⚡ Heartbeat ping sent to ${json.device ? json.device.deviceModel : 'device'}!`);
      await loadTelemetryData();
    } else {
      showToast(json.error || 'Failed to ping device');
    }
  } catch (err) {
    showToast('Ping error: ' + err.message);
  }
};

// ========================================================
// 🖼️ HOME SCREEN HERO BANNERS & CAROUSEL SIMULATOR
// ========================================================
let allHeroBanners = [];
let simCarouselIndex = 0;
let simCarouselTimer = null;

async function loadBanners() {
  try {
    const res = await authFetch('/api/admin/banners');
    const json = await res.json();
    if (json.success) {
      allHeroBanners = json.data || [];
      renderBannersTable();
      renderSimCarousel();
      populateBannerWallpaperQuickSelect();
    }
  } catch (err) {
    console.error('Error loading hero banners:', err);
  }
}

function renderBannersTable() {
  const tbody = document.getElementById('bannersTableBody');
  if (!tbody) return;

  const filter = document.getElementById('bannerStatusFilter')?.value || 'ALL';
  let filtered = allHeroBanners;
  if (filter === 'ACTIVE') {
    filtered = allHeroBanners.filter(b => b.isActive);
  } else if (filter === 'PAUSED') {
    filtered = allHeroBanners.filter(b => !b.isActive);
  }

  if (filtered.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6" class="text-center text-muted" style="padding: 32px 16px;">
          <div style="font-size: 32px; margin-bottom: 8px;">🖼️</div>
          <div>No hero banners found</div>
          <div style="font-size: 12px; margin-top: 4px;">Click "+ Add Hero Banner" above to publish your first home screen promotional slide.</div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = filtered.map(b => {
    const actionLabel = getActionDisplayLabel(b.actionType, b.actionTarget);
    const badgeHtml = b.badgeText
      ? `<span style="font-size:10px; font-weight:800; background:linear-gradient(135deg,#f59e0b,#ef4444); color:#fff; padding:2px 7px; border-radius:10px; display:inline-block; margin-bottom:4px; text-transform:uppercase; letter-spacing:0.5px;">${escapeHtml(b.badgeText)}</span>`
      : '';

    return `
      <tr>
        <td>
          <img src="${b.imageUrl}" class="banner-thumb-img" alt="${escapeHtml(b.title)}" onerror="this.src='https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200'">
        </td>
        <td>
          ${badgeHtml}
          <div style="font-weight:700; color:#fff; font-size:13.5px;">${escapeHtml(b.title)}</div>
          ${b.subtitle ? `<div class="text-muted" style="font-size:11.5px; margin-top:2px;">${escapeHtml(b.subtitle)}</div>` : ''}
        </td>
        <td>
          <span style="font-size:12px; background:rgba(255,255,255,0.06); padding:4px 9px; border-radius:6px; border:1px solid rgba(255,255,255,0.08); display:inline-block; max-width:200px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
            ${actionLabel}
          </span>
        </td>
        <td>
          <span class="badge-pill" style="background:rgba(0,229,255,0.12); color:#00E5FF; font-weight:700; font-size:12px;">#${b.sortOrder}</span>
        </td>
        <td>
          <label class="switch">
            <input type="checkbox" ${b.isActive ? 'checked' : ''} onchange="toggleBannerActive('${b.id}')">
            <span class="slider"></span>
          </label>
        </td>
        <td>
          <div style="display:flex; gap:6px;">
            <button class="btn btn-secondary btn-sm" onclick="editBanner('${b.id}')" title="Edit Banner" style="padding:4px 8px; font-size:11px;">
              ✏️ Edit
            </button>
            <button class="btn btn-danger btn-sm" onclick="deleteBanner('${b.id}')" title="Delete Banner" style="padding:4px 8px; font-size:11px;">
              🗑️
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function getActionDisplayLabel(type, target) {
  if (type === 'category') {
    return `📁 Category: <strong>${escapeHtml(target || 'All')}</strong>`;
  } else if (type === 'wallpaper') {
    const wp = allWallpapers.find(w => w.id === target);
    return `🎯 Wallpaper: <strong>${escapeHtml(wp ? wp.title : target)}</strong>`;
  } else if (type === 'screen') {
    const screens = {
      custom_3d: '3D Maker Studio',
      ai_generator: 'AI 4K Generator',
      daily: 'Daily Wallpaper'
    };
    return `📱 Screen: <strong>${screens[target] || target}</strong>`;
  } else if (type === 'url') {
    return `🌐 Link: <a href="${escapeHtml(target)}" target="_blank" style="color:var(--accent-cyan); text-decoration:underline;">${escapeHtml(target)}</a>`;
  }
  return `Action: ${escapeHtml(target || '-')}`;
}

function renderSimCarousel() {
  const track = document.getElementById('simCarouselTrack');
  const dotsContainer = document.getElementById('simCarouselDots');
  const statusBadge = document.getElementById('carouselStatusBadge');
  if (!track || !dotsContainer) return;

  const activeBanners = allHeroBanners.filter(b => b.isActive).sort((a, b) => a.sortOrder - b.sortOrder);

  if (statusBadge) {
    statusBadge.textContent = `● ${activeBanners.length} Active Banners`;
    statusBadge.className = activeBanners.length > 0 ? 'badge-pill badge-green' : 'badge-pill badge-danger';
  }

  if (activeBanners.length === 0) {
    track.innerHTML = `
      <div class="sim-slide" style="display:flex; align-items:center; justify-content:center; flex-direction:column; background:#0f172a; color:#94a3b8; padding:20px; text-align:center;">
        <span style="font-size:28px; margin-bottom:6px;">⏸️</span>
        <strong style="font-size:13px; color:#e2e8f0;">No Active Banners</strong>
        <p style="font-size:11px; margin-top:4px;">Enable or create a banner to preview the slider.</p>
      </div>
    `;
    dotsContainer.innerHTML = '';
    return;
  }

  if (simCarouselIndex >= activeBanners.length) {
    simCarouselIndex = 0;
  }

  track.innerHTML = activeBanners.map((b, idx) => {
    const badgeMarkup = b.badgeText
      ? `<div class="sim-slide-badge">${escapeHtml(b.badgeText)}</div>`
      : '';

    return `
      <div class="sim-slide" style="background-image: url('${b.imageUrl}');" onclick="handleSimSlideClick(${idx})">
        <div class="sim-slide-overlay">
          ${badgeMarkup}
          <div class="sim-slide-title">${escapeHtml(b.title)}</div>
          ${b.subtitle ? `<div class="sim-slide-subtitle">${escapeHtml(b.subtitle)}</div>` : ''}
          <button type="button" class="sim-slide-btn">EXPLORE NOW ➔</button>
        </div>
      </div>
    `;
  }).join('');

  dotsContainer.innerHTML = activeBanners.map((_, idx) => `
    <button type="button" class="sim-dot ${idx === simCarouselIndex ? 'active' : ''}" onclick="goToSimSlide(${idx})"></button>
  `).join('');

  updateSimCarouselPosition();
  startSimCarouselTimer(activeBanners.length);
}

function updateSimCarouselPosition() {
  const track = document.getElementById('simCarouselTrack');
  const dots = document.querySelectorAll('#simCarouselDots .sim-dot');
  if (!track) return;

  track.style.transform = `translateX(-${simCarouselIndex * 100}%)`;
  dots.forEach((d, i) => {
    d.classList.toggle('active', i === simCarouselIndex);
  });
}

function startSimCarouselTimer(itemCount) {
  if (simCarouselTimer) clearInterval(simCarouselTimer);
  if (itemCount <= 1) return;

  simCarouselTimer = setInterval(() => {
    const activeCount = allHeroBanners.filter(b => b.isActive).length;
    if (activeCount > 1) {
      simCarouselIndex = (simCarouselIndex + 1) % activeCount;
      updateSimCarouselPosition();
    }
  }, 4000);
}

window.goToSimSlide = function(idx) {
  simCarouselIndex = idx;
  updateSimCarouselPosition();
};

window.handleSimSlideClick = function(idx) {
  const activeBanners = allHeroBanners.filter(b => b.isActive).sort((a, b) => a.sortOrder - b.sortOrder);
  const b = activeBanners[idx];
  if (!b) return;

  const actionMsg = describeAction(b.actionType, b.actionTarget);
  showToast(`📱 Simulated Tap: ${actionMsg}`);
};

function describeAction(type, target) {
  if (type === 'category') return `Filter category "${target}" on Home Screen`;
  if (type === 'wallpaper') {
    const wp = allWallpapers.find(w => w.id === target);
    return `Open Wallpaper Details for "${wp ? wp.title : target}"`;
  }
  if (type === 'screen') {
    if (target === 'custom_3d') return 'Navigate to 3D Depth Layer Studio';
    if (target === 'ai_generator') return 'Open AI 4K Wallpaper Generator';
    return `Open Screen: ${target}`;
  }
  if (type === 'url') return `Launch External Browser: ${target}`;
  return `Target: ${target}`;
}

window.toggleBannerActive = async function(id) {
  try {
    const res = await authFetch(`/api/admin/banners/${id}/toggle`, { method: 'PATCH' });
    const json = await res.json();
    if (json.success) {
      showToast(json.message || 'Banner status updated');
      await loadBanners();
    } else {
      showToast(json.error || 'Failed to update banner');
    }
  } catch (err) {
    showToast('Error: ' + err.message);
  }
};

window.deleteBanner = async function(id) {
  const banner = allHeroBanners.find(b => b.id === id);
  const name = banner ? banner.title : 'this banner';
  if (!confirm(`Are you sure you want to delete "${name}"?`)) return;

  try {
    const res = await authFetch(`/api/admin/banners/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Hero banner deleted successfully');
      await loadBanners();
    } else {
      showToast(json.error || 'Failed to delete banner');
    }
  } catch (err) {
    showToast('Error: ' + err.message);
  }
};

window.editBanner = function(id) {
  const banner = allHeroBanners.find(b => b.id === id);
  if (!banner) return;

  document.getElementById('modalBannerTitle').textContent = 'Edit Hero Banner';
  document.getElementById('bannerIdInput').value = banner.id;
  document.getElementById('bannerTitleInput').value = banner.title;
  document.getElementById('bannerSubtitleInput').value = banner.subtitle || '';
  document.getElementById('bannerBadgeInput').value = banner.badgeText || '';
  document.getElementById('bannerSortOrderInput').value = banner.sortOrder || 1;
  document.getElementById('bannerImageUrlInput').value = banner.imageUrl || '';
  document.getElementById('bannerIsActiveCheck').checked = banner.isActive;

  // Show image preview
  const prevBox = document.getElementById('bannerImagePreviewBox');
  const prevImg = document.getElementById('bannerImagePreviewImg');
  if (banner.imageUrl) {
    prevImg.src = banner.imageUrl;
    prevBox.style.display = 'block';
  } else {
    prevBox.style.display = 'none';
  }

  // Action type & target
  const typeSelect = document.getElementById('bannerActionTypeSelect');
  typeSelect.value = banner.actionType || 'category';
  updateBannerActionTargetOptions(banner.actionType || 'category', banner.actionTarget || '');

  document.getElementById('bannerModal').classList.add('active');
};

function populateBannerWallpaperQuickSelect() {
  const select = document.getElementById('bannerWallpaperQuickSelect');
  if (!select) return;

  select.innerHTML = '<option value="">-- Choose Existing Wallpaper --</option>' +
    allWallpapers.map(w => `<option value="${w.previewUrl}" data-id="${w.id}" data-title="${escapeHtml(w.title)}">${escapeHtml(w.title)} (${w.category})</option>`).join('');
}

function updateBannerActionTargetOptions(actionType, selectedValue = '') {
  const targetSelect = document.getElementById('bannerActionTargetSelect');
  const customInput = document.getElementById('bannerActionTargetCustomInput');
  if (!targetSelect || !customInput) return;

  if (actionType === 'url') {
    targetSelect.style.display = 'none';
    customInput.style.display = 'block';
    customInput.placeholder = 'https://example.com/promo';
    customInput.value = selectedValue;
  } else if (actionType === 'category') {
    targetSelect.style.display = 'block';
    customInput.style.display = 'none';
    targetSelect.innerHTML = allCategories.map(c => `
      <option value="${c.name}" ${c.name === selectedValue ? 'selected' : ''}>📁 ${escapeHtml(c.name)}</option>
    `).join('');
  } else if (actionType === 'wallpaper') {
    targetSelect.style.display = 'block';
    customInput.style.display = 'none';
    targetSelect.innerHTML = allWallpapers.map(w => `
      <option value="${w.id}" ${w.id === selectedValue ? 'selected' : ''}>🎯 ${escapeHtml(w.title)}</option>
    `).join('');
  } else if (actionType === 'screen') {
    targetSelect.style.display = 'block';
    customInput.style.display = 'none';
    targetSelect.innerHTML = `
      <option value="custom_3d" ${selectedValue === 'custom_3d' ? 'selected' : ''}>📱 3D Layer Studio (DIY Maker)</option>
      <option value="ai_generator" ${selectedValue === 'ai_generator' ? 'selected' : ''}>✨ AI 4K Wallpaper Generator</option>
      <option value="daily" ${selectedValue === 'daily' ? 'selected' : ''}>⭐ Daily Wallpaper Pick</option>
    `;
  }
}

function initHeroBanners() {
  const addBtn = document.getElementById('openAddBannerModalBtn');
  const refreshBtn = document.getElementById('refreshBannersBtn');
  const closeBtn = document.getElementById('closeBannerModal');
  const cancelBtn = document.getElementById('cancelBannerModalBtn');
  const modal = document.getElementById('bannerModal');
  const form = document.getElementById('bannerForm');
  const actionTypeSelect = document.getElementById('bannerActionTypeSelect');
  const quickSelect = document.getElementById('bannerWallpaperQuickSelect');
  const imgUrlInput = document.getElementById('bannerImageUrlInput');
  const fileInput = document.getElementById('bannerImageFileInput');
  const statusFilter = document.getElementById('bannerStatusFilter');
  const prevArrow = document.getElementById('simCarouselPrevBtn');
  const nextArrow = document.getElementById('simCarouselNextBtn');

  if (addBtn) {
    addBtn.addEventListener('click', () => {
      try {
        const titleEl = document.getElementById('modalBannerTitle');
        if (titleEl) titleEl.textContent = 'Add Hero Banner';
        if (form) form.reset();
        const idInput = document.getElementById('bannerIdInput');
        if (idInput) idInput.value = '';
        const sortInput = document.getElementById('bannerSortOrderInput');
        if (sortInput) sortInput.value = (allHeroBanners.length + 1);
        const badgeInput = document.getElementById('bannerBadgeInput');
        if (badgeInput) badgeInput.value = '🔥 FESTIVAL SPECIAL';
        const activeCheck = document.getElementById('bannerIsActiveCheck');
        if (activeCheck) activeCheck.checked = true;
        const prevBox = document.getElementById('bannerImagePreviewBox');
        if (prevBox) prevBox.style.display = 'none';
        updateBannerActionTargetOptions('category', allCategories[0]?.name || '');
      } catch (err) {
        console.error('Error preparing banner modal:', err);
      }
      if (modal) {
        modal.classList.add('active');
      }
    });
  }

  [closeBtn, cancelBtn].forEach(btn => {
    if (btn) {
      btn.addEventListener('click', () => {
        modal.classList.remove('active');
      });
    }
  });

  if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
      loadBanners();
      showToast('Hero banners refreshed!');
    });
  }

  if (statusFilter) {
    statusFilter.addEventListener('change', () => {
      renderBannersTable();
    });
  }

  if (actionTypeSelect) {
    actionTypeSelect.addEventListener('change', () => {
      updateBannerActionTargetOptions(actionTypeSelect.value);
    });
  }

  if (quickSelect) {
    quickSelect.addEventListener('change', () => {
      const val = quickSelect.value;
      if (!val) return;
      imgUrlInput.value = val;
      const prevBox = document.getElementById('bannerImagePreviewBox');
      const prevImg = document.getElementById('bannerImagePreviewImg');
      prevImg.src = val;
      prevBox.style.display = 'block';

      const selectedOpt = quickSelect.options[quickSelect.selectedIndex];
      const titleInput = document.getElementById('bannerTitleInput');
      if (!titleInput.value.trim() && selectedOpt.dataset.title) {
        titleInput.value = `🔥 ${selectedOpt.dataset.title}`;
      }
    });
  }

  if (fileInput) {
    fileInput.addEventListener('change', () => {
      if (fileInput.files && fileInput.files[0]) {
        const reader = new FileReader();
        reader.onload = (e) => {
          const prevBox = document.getElementById('bannerImagePreviewBox');
          const prevImg = document.getElementById('bannerImagePreviewImg');
          prevImg.src = e.target.result;
          prevBox.style.display = 'block';
        };
        reader.readAsDataURL(fileInput.files[0]);
      }
    });
  }

  if (imgUrlInput) {
    imgUrlInput.addEventListener('input', () => {
      const val = imgUrlInput.value.trim();
      const prevBox = document.getElementById('bannerImagePreviewBox');
      const prevImg = document.getElementById('bannerImagePreviewImg');
      if (val) {
        prevImg.src = val;
        prevBox.style.display = 'block';
      } else {
        prevBox.style.display = 'none';
      }
    });
  }

  if (prevArrow) {
    prevArrow.addEventListener('click', () => {
      const activeCount = allHeroBanners.filter(b => b.isActive).length;
      if (activeCount <= 1) return;
      simCarouselIndex = (simCarouselIndex - 1 + activeCount) % activeCount;
      updateSimCarouselPosition();
    });
  }

  if (nextArrow) {
    nextArrow.addEventListener('click', () => {
      const activeCount = allHeroBanners.filter(b => b.isActive).length;
      if (activeCount <= 1) return;
      simCarouselIndex = (simCarouselIndex + 1) % activeCount;
      updateSimCarouselPosition();
    });
  }

  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const bannerId = document.getElementById('bannerIdInput').value;
      const title = document.getElementById('bannerTitleInput').value.trim();
      const subtitle = document.getElementById('bannerSubtitleInput').value.trim();
      const badgeText = document.getElementById('bannerBadgeInput').value.trim();
      const sortOrder = document.getElementById('bannerSortOrderInput').value;
      const imageUrl = imgUrlInput.value.trim();
      const actionType = actionTypeSelect.value;
      const isActive = document.getElementById('bannerIsActiveCheck').checked;

      let actionTarget = '';
      if (actionType === 'url') {
        actionTarget = document.getElementById('bannerActionTargetCustomInput').value.trim();
      } else {
        actionTarget = document.getElementById('bannerActionTargetSelect').value;
      }

      const submitBtn = document.getElementById('saveBannerSubmitBtn');
      submitBtn.disabled = true;
      submitBtn.innerHTML = 'Saving Hero Banner...';

      try {
        const formData = new FormData();
        formData.append('title', title);
        formData.append('subtitle', subtitle);
        formData.append('badgeText', badgeText);
        formData.append('sortOrder', sortOrder);
        formData.append('imageUrl', imageUrl);
        formData.append('actionType', actionType);
        formData.append('actionTarget', actionTarget);
        formData.append('isActive', isActive);

        if (fileInput.files && fileInput.files[0]) {
          formData.append('bannerImage', fileInput.files[0]);
        }

        const url = bannerId ? `/api/admin/banners/${bannerId}` : '/api/admin/banners';
        const method = bannerId ? 'PUT' : 'POST';

        const res = await authFetch(url, {
          method,
          body: formData
        });

        const json = await res.json();
        if (json.success) {
          showToast(bannerId ? 'Hero banner updated!' : 'Hero banner created!');
          modal.classList.remove('active');
          await loadBanners();
        } else {
          showToast(json.error || 'Failed to save banner');
        }
      } catch (err) {
        showToast('Save error: ' + err.message);
      } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '💾 Save Hero Banner';
      }
    });
  }
}

// ========================================================
// 🔥 LIVE TRENDING SEARCH KEYWORDS / TAGS STUDIO
// ========================================================
let allTrendingTags = [];

async function loadTrendingTags() {
  try {
    const res = await authFetch('/api/admin/trending-tags');
    const json = await res.json();
    if (json.success) {
      allTrendingTags = json.data || [];
      renderTrendingTagsCloud();
      renderTrendingTagsTable();
    }
  } catch (err) {
    console.error('Error loading trending tags:', err);
  }
}

function renderTrendingTagsCloud() {
  const container = document.getElementById('trendingTagsCloud');
  const badge = document.getElementById('activeTagsCountBadge');
  if (!container) return;

  const activeCount = allTrendingTags.filter(t => t.isActive).length;
  if (badge) {
    badge.textContent = `● ${activeCount} Active Tags`;
    badge.className = activeCount > 0 ? 'badge-pill badge-green' : 'badge-pill badge-danger';
  }

  if (allTrendingTags.length === 0) {
    container.innerHTML = `
      <div class="text-muted" style="font-size:12px; padding:8px 0;">
        No trending tags created yet. Use the form below to add promoted keywords!
      </div>
    `;
    return;
  }

  container.innerHTML = allTrendingTags.map(t => {
    const pausedClass = t.isActive ? '' : 'paused';
    const clickLabel = t.clickCount > 0 ? `<span class="tag-click-badge">${t.clickCount} clicks</span>` : '';
    return `
      <div class="trending-tag-pill ${pausedClass}" onclick="handleTrendingTagPillClick('${escapeHtml(t.tag)}')" title="${t.isActive ? 'Simulate app search tap' : 'Tag is paused'}">
        <span>${t.icon || '🔥'}</span>
        <span>${escapeHtml(t.tag)}</span>
        ${clickLabel}
      </div>
    `;
  }).join('');
}

window.handleTrendingTagPillClick = function(tagName) {
  showToast(`📱 Simulated App Search Tap: "${tagName}"`);
};

function renderTrendingTagsTable() {
  const tbody = document.getElementById('trendingTagsTableBody');
  if (!tbody) return;

  if (allTrendingTags.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6" class="text-center text-muted" style="padding: 24px;">
          No trending tags configured yet
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = allTrendingTags.map(t => {
    return `
      <tr>
        <td style="font-size:18px; text-align:center;">${t.icon || '🔥'}</td>
        <td>
          <span style="font-weight:700; color:#fff; font-size:13.5px;">${escapeHtml(t.tag)}</span>
        </td>
        <td>
          <span class="badge-pill" style="background:rgba(0,229,255,0.12); color:#00E5FF; font-weight:700; font-size:12px;">#${t.sortOrder}</span>
        </td>
        <td>
          <span class="tag-click-badge" style="font-size:11px; padding:2px 8px;">🔥 ${t.clickCount.toLocaleString()} clicks</span>
        </td>
        <td>
          <label class="switch">
            <input type="checkbox" ${t.isActive ? 'checked' : ''} onchange="toggleTrendingTagActive('${t.id}')">
            <span class="slider"></span>
          </label>
        </td>
        <td style="text-align:right;">
          <button class="btn btn-danger btn-sm" onclick="deleteTrendingTag('${t.id}')" title="Delete Tag" style="padding:3px 8px; font-size:11px;">
            🗑️
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

window.toggleTrendingTagActive = async function(id) {
  try {
    const res = await authFetch(`/api/admin/trending-tags/${id}/toggle`, { method: 'PATCH' });
    const json = await res.json();
    if (json.success) {
      showToast(json.message || 'Tag status updated');
      await loadTrendingTags();
    } else {
      showToast(json.error || 'Failed to update tag');
    }
  } catch (err) {
    showToast('Error: ' + err.message);
  }
};

window.deleteTrendingTag = async function(id) {
  const tagObj = allTrendingTags.find(t => t.id === id);
  const name = tagObj ? tagObj.tag : 'this tag';
  if (!confirm(`Are you sure you want to delete "${name}"?`)) return;

  try {
    const res = await authFetch(`/api/admin/trending-tags/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Trending tag deleted');
      await loadTrendingTags();
    } else {
      showToast(json.error || 'Failed to delete tag');
    }
  } catch (err) {
    showToast('Error: ' + err.message);
  }
};

function initTrendingTagsStudio() {
  const addForm = document.getElementById('addTrendingTagForm');
  if (!addForm) return;

  addForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const tagInput = document.getElementById('newTagInput');
    const iconSelect = document.getElementById('newTagIconSelect');
    const sortInput = document.getElementById('newTagSortOrder');
    const submitBtn = document.getElementById('submitAddTagBtn');

    const tag = tagInput.value.trim();
    const icon = iconSelect.value;
    const sortOrder = sortInput.value;

    if (!tag) return;

    submitBtn.disabled = true;
    submitBtn.textContent = 'Adding...';

    try {
      const res = await authFetch('/api/admin/trending-tags', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ tag, icon, sortOrder, isActive: true })
      });

      const json = await res.json();
      if (json.success) {
        showToast(`Added trending tag "${json.data ? json.data.tag : tag}"!`);
        tagInput.value = '';
        sortInput.value = (allTrendingTags.length + 2);
        await loadTrendingTags();
      } else {
        showToast(json.error || 'Failed to add tag');
      }
    } catch (err) {
      showToast('Error: ' + err.message);
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = '+ Add Tag';
    }
  });
}

// ========================================================
// 🎛️ APP FEATURES MASTER SWITCHBOARD (DYNAMIC FEATURE FLAGS)
// ========================================================

const featureKeyMap = {
  feature_status_saver: { id: 'flagStatusSaver', name: 'WhatsApp Status Saver', icon: '💬' },
  feature_ai_studio: { id: 'flagAiStudio', name: 'AI Wallpaper Studio', icon: '🤖' },
  feature_custom_3d: { id: 'flagCustom3d', name: 'DIY 3D Parallax Maker', icon: '🎨' },
  feature_music_visualizer: { id: 'flagMusicVisualizer', name: 'Music Beat Visualizer', icon: '🎵' },
  feature_ringtones: { id: 'flagRingtones', name: 'Ringtones & Audio Cutter', icon: '🔔' },
  feature_weather_sync: { id: 'flagWeatherSync', name: 'Weather Live Sync', icon: '🌧️' },
  feature_double_wallpaper: { id: 'flagDoubleWallpaper', name: 'Double Wallpaper', icon: '🖼️' },
  feature_wallpaper_changer: { id: 'flagWallpaperChanger', name: 'Auto Wallpaper Changer', icon: '⏱️' }
};

let currentFeatureFlags = {};

async function loadFeatureFlags() {
  try {
    const res = await fetch('/api/app-config');
    const json = await res.json();
    if (!json.success || !json.data) return;

    const features = json.data.features || {};
    currentFeatureFlags = features;

    const propToKey = {
      statusSaver: 'feature_status_saver',
      aiStudio: 'feature_ai_studio',
      custom3dMaker: 'feature_custom_3d',
      musicVisualizer: 'feature_music_visualizer',
      ringtones: 'feature_ringtones',
      weatherSync: 'feature_weather_sync',
      doubleWallpaper: 'feature_double_wallpaper',
      wallpaperChanger: 'feature_wallpaper_changer'
    };

    let activeCount = 0;
    const totalCount = Object.keys(featureKeyMap).length;

    for (const [prop, val] of Object.entries(features)) {
      const flagKey = propToKey[prop];
      if (flagKey && featureKeyMap[flagKey]) {
        const el = document.getElementById(featureKeyMap[flagKey].id);
        const card = document.querySelector(`.feature-flag-card[data-key="${flagKey}"]`);
        if (el) {
          el.checked = Boolean(val);
        }
        if (card) {
          card.classList.toggle('disabled', !val);
        }
        if (val) activeCount++;
      }
    }

    const badge = document.getElementById('activeFeaturesBadge');
    if (badge) {
      badge.textContent = `● ${activeCount}/${totalCount} Active`;
      badge.className = activeCount === totalCount ? 'badge-pill badge-green' : 'badge-pill badge-orange';
    }
  } catch (err) {
    console.error('Failed to load feature flags:', err);
  }
}

async function toggleFeatureFlag(featureKey, enabled) {
  try {
    const res = await authFetch('/api/admin/features/toggle', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ featureKey, enabled })
    });
    const json = await res.json();
    if (json.success) {
      const meta = featureKeyMap[featureKey];
      const name = meta ? meta.name : featureKey;
      const statusText = enabled ? 'ENABLED (Visible in App)' : 'DISABLED (Hidden from App)';
      showToast(`${meta ? meta.icon : '🎛️'} ${name}: ${statusText}`);
      await loadFeatureFlags();
    } else {
      showToast('Error: ' + (json.error || 'Failed to toggle feature'));
    }
  } catch (err) {
    showToast('Network error: ' + err.message);
  }
}

async function applyFeaturePreset(mode) {
  try {
    const res = await authFetch('/api/admin/features/preset', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mode })
    });
    const json = await res.json();
    if (json.success) {
      showToast(json.message);
      await loadFeatureFlags();
    } else {
      showToast('Error: ' + (json.error || 'Failed to apply preset'));
    }
  } catch (err) {
    showToast('Network error: ' + err.message);
  }
}

function initFeatureFlagsSwitchboard() {
  // Individual switches
  document.querySelectorAll('input[data-flag]').forEach(input => {
    input.addEventListener('change', async (e) => {
      const key = e.target.getAttribute('data-flag');
      const enabled = e.target.checked;
      await toggleFeatureFlag(key, enabled);
    });
  });

  // Preset 1: Google Play Review Safe Mode
  const safeModeBtn = document.getElementById('btnGooglePlaySafeMode');
  if (safeModeBtn) {
    safeModeBtn.addEventListener('click', async () => {
      if (confirm('Activate "Google Play Review Safe Mode"?\\n\\nThis will instantly hide WhatsApp Status Saver from the Android drawer so your app passes Google Play Store policy review without permission warnings.')) {
        await applyFeaturePreset('google_play_safe');
      }
    });
  }

  // Preset 2: Enable All Features
  const enableAllBtn = document.getElementById('btnEnableAllFeatures');
  if (enableAllBtn) {
    enableAllBtn.addEventListener('click', async () => {
      await applyFeaturePreset('all_enabled');
    });
  }
}

// ========================================================
// 🌐 SOCIAL MEDIA, LEGAL POLICY & SUPPORT HUB
// ========================================================

async function loadSocialHubConfig() {
  try {
    const res = await fetch('/api/app-config');
    const json = await res.json();
    if (!json.success || !json.data) return;

    const legal = json.data.legalAndSupport || {};

    const privacyEl = document.getElementById('hubPrivacyPolicyUrl');
    const termsEl = document.getElementById('hubTermsUrl');
    const instaEl = document.getElementById('hubInstagramUrl');
    const teleEl = document.getElementById('hubTelegramUrl');
    const waEl = document.getElementById('hubWhatsappNumber');
    const emailEl = document.getElementById('hubSupportEmail');

    if (privacyEl) privacyEl.value = legal.privacyPolicyUrl || '';
    if (termsEl) termsEl.value = legal.termsUrl || '';
    if (instaEl) instaEl.value = legal.instagramUrl || '';
    if (teleEl) teleEl.value = legal.telegramUrl || '';
    if (waEl) waEl.value = legal.whatsappNumber || '';
    if (emailEl) emailEl.value = legal.supportEmail || '';

    updateSocialHubTestLinks();
  } catch (err) {
    console.error('Failed to load social hub config:', err);
  }
}

function updateSocialHubTestLinks() {
  const privacyVal = document.getElementById('hubPrivacyPolicyUrl')?.value.trim();
  const termsVal = document.getElementById('hubTermsUrl')?.value.trim();
  const instaVal = document.getElementById('hubInstagramUrl')?.value.trim();
  const teleVal = document.getElementById('hubTelegramUrl')?.value.trim();
  const waVal = document.getElementById('hubWhatsappNumber')?.value.trim();
  const emailVal = document.getElementById('hubSupportEmail')?.value.trim();

  const testPrivacy = document.getElementById('testPrivacyPolicyLink');
  const testTerms = document.getElementById('testTermsLink');
  const testInsta = document.getElementById('testInstagramLink');
  const testTele = document.getElementById('testTelegramLink');
  const testWa = document.getElementById('testWhatsappLink');
  const testEmail = document.getElementById('testEmailLink');

  if (testPrivacy) testPrivacy.href = privacyVal || '#';
  if (testTerms) testTerms.href = termsVal || '#';
  if (testInsta) testInsta.href = instaVal || '#';
  if (testTele) testTele.href = teleVal || '#';

  if (testWa) {
    const cleanNumber = waVal ? waVal.replace(/[^0-9+]/g, '') : '';
    testWa.href = cleanNumber ? `https://wa.me/${cleanNumber.replace('+', '')}?text=Hello%20ReWall%20Support` : '#';
  }

  if (testEmail) {
    testEmail.href = emailVal ? `mailto:${emailVal}?subject=ReWall%20Support%20Inquiry` : '#';
  }
}

async function saveSocialHubConfig() {
  const privacyPolicyUrl = document.getElementById('hubPrivacyPolicyUrl')?.value.trim() || '';
  const termsUrl = document.getElementById('hubTermsUrl')?.value.trim() || '';
  const instagramUrl = document.getElementById('hubInstagramUrl')?.value.trim() || '';
  const telegramUrl = document.getElementById('hubTelegramUrl')?.value.trim() || '';
  const whatsappNumber = document.getElementById('hubWhatsappNumber')?.value.trim() || '';
  const supportEmail = document.getElementById('hubSupportEmail')?.value.trim() || '';

  const saveBtn = document.getElementById('btnSaveSocialHub');
  if (saveBtn) {
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';
  }

  try {
    const res = await authFetch('/api/admin/social-hub', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        privacyPolicyUrl,
        termsUrl,
        instagramUrl,
        telegramUrl,
        whatsappNumber,
        supportEmail
      })
    });

    const json = await res.json();
    if (json.success) {
      showToast('🌐 Social & Legal Hub links saved successfully!');
      updateSocialHubTestLinks();
    } else {
      showToast('Error: ' + (json.error || 'Failed to save hub links'));
    }
  } catch (err) {
    showToast('Network error: ' + err.message);
  } finally {
    if (saveBtn) {
      saveBtn.disabled = false;
      saveBtn.textContent = '💾 Save Hub Links';
    }
  }
}

function initSocialHub() {
  const saveBtn = document.getElementById('btnSaveSocialHub');
  if (saveBtn) {
    saveBtn.addEventListener('click', saveSocialHubConfig);
  }

  const hubInputs = [
    'hubPrivacyPolicyUrl',
    'hubTermsUrl',
    'hubInstagramUrl',
    'hubTelegramUrl',
    'hubWhatsappNumber',
    'hubSupportEmail'
  ];

  hubInputs.forEach(id => {
    const input = document.getElementById(id);
    if (input) {
      input.addEventListener('input', updateSocialHubTestLinks);
    }
  });
}

// ========================================================
// ⭐ SMART IN-APP RATING & REVIEW TRIGGER (PLAY STORE GROWTH)
// ========================================================

async function loadRatingPromptConfig() {
  try {
    const res = await fetch('/api/app-config');
    const json = await res.json();
    if (!json.success || !json.data) return;

    const rating = json.data.ratingPrompt || {};

    const enabledEl = document.getElementById('cfgRatingEnabled');
    const triggerEl = document.getElementById('cfgRatingTriggerDownloads');
    const titleEl = document.getElementById('cfgRatingTitle');
    const msgEl = document.getElementById('cfgRatingMessage');
    const posBtnEl = document.getElementById('cfgRatingPositiveBtn');
    const disBtnEl = document.getElementById('cfgRatingDismissBtn');

    if (enabledEl) enabledEl.checked = rating.enabled !== false;
    if (triggerEl) triggerEl.value = String(rating.triggerDownloads || 3);
    if (titleEl) titleEl.value = rating.title || 'Enjoying ReWall 3D Wallpapers?';
    if (msgEl) msgEl.value = rating.message || 'You have downloaded awesome 4K 3D wallpapers! A quick 5-star rating on Google Play Store helps our team keep adding free wallpapers.';
    if (posBtnEl) posBtnEl.value = rating.positiveBtn || '⭐ Rate 5 Stars on Google Play';
    if (disBtnEl) disBtnEl.value = rating.dismissBtn || 'Maybe Later';

    updateRatingSimulatorPreview();
  } catch (err) {
    console.error('Failed to load rating prompt config:', err);
  }
}

function updateRatingSimulatorPreview() {
  const title = document.getElementById('cfgRatingTitle')?.value || 'Enjoying ReWall 3D?';
  const msg = document.getElementById('cfgRatingMessage')?.value || 'Rate us 5 stars on Google Play!';
  const posBtn = document.getElementById('cfgRatingPositiveBtn')?.value || '⭐ Rate 5 Stars on Google Play';
  const disBtn = document.getElementById('cfgRatingDismissBtn')?.value || 'Maybe Later';

  const simTitle = document.getElementById('simRatingTitle');
  const simMsg = document.getElementById('simRatingMsg');
  const simBtn = document.getElementById('simRatingBtn');
  const simDismiss = document.getElementById('simRatingDismissBtn');

  if (simTitle) simTitle.textContent = title;
  if (simMsg) simMsg.textContent = msg;
  if (simBtn) simBtn.textContent = posBtn;
  if (simDismiss) simDismiss.textContent = disBtn;
}

async function saveRatingPromptConfig() {
  const enabled = document.getElementById('cfgRatingEnabled')?.checked ?? true;
  const triggerDownloads = parseInt(document.getElementById('cfgRatingTriggerDownloads')?.value || '3', 10);
  const title = document.getElementById('cfgRatingTitle')?.value.trim() || 'Enjoying ReWall 3D Wallpapers?';
  const message = document.getElementById('cfgRatingMessage')?.value.trim() || '';
  const positiveBtn = document.getElementById('cfgRatingPositiveBtn')?.value.trim() || '⭐ Rate 5 Stars on Google Play';
  const dismissBtn = document.getElementById('cfgRatingDismissBtn')?.value.trim() || 'Maybe Later';

  const saveBtn = document.getElementById('btnSaveRatingPrompt');
  if (saveBtn) {
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';
  }

  try {
    const res = await authFetch('/api/admin/rating-prompt', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        enabled,
        triggerDownloads,
        title,
        message,
        positiveBtn,
        dismissBtn
      })
    });

    const json = await res.json();
    if (json.success) {
      showToast('⭐ Smart In-App Rating Trigger saved successfully!');
      updateRatingSimulatorPreview();
    } else {
      showToast('Error: ' + (json.error || 'Failed to save rating trigger'));
    }
  } catch (err) {
    showToast('Network error: ' + err.message);
  } finally {
    if (saveBtn) {
      saveBtn.disabled = false;
      saveBtn.textContent = '💾 Save Rating Trigger';
    }
  }
}

function initRatingPromptStudio() {
  const saveBtn = document.getElementById('btnSaveRatingPrompt');
  if (saveBtn) {
    saveBtn.addEventListener('click', saveRatingPromptConfig);
  }

  const previewBtn = document.getElementById('btnPreviewRatingPopup');
  const simModal = document.getElementById('simRatingModal');
  const simDismiss = document.getElementById('simRatingDismissBtn');

  if (previewBtn && simModal) {
    previewBtn.addEventListener('click', () => {
      const isVisible = simModal.style.display !== 'none';
      simModal.style.display = isVisible ? 'none' : 'flex';
      previewBtn.classList.toggle('active', !isVisible);
      previewBtn.textContent = isVisible ? '📲 Preview on Phone Simulator' : '❌ Hide Phone Preview';
      updateRatingSimulatorPreview();
      if (!isVisible) {
        simModal.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    });
  }

  if (simDismiss && simModal) {
    simDismiss.addEventListener('click', () => {
      simModal.style.display = 'none';
      if (previewBtn) {
        previewBtn.classList.remove('active');
        previewBtn.textContent = '📲 Preview on Phone Simulator';
      }
    });
  }

  const inputs = ['cfgRatingTitle', 'cfgRatingMessage', 'cfgRatingPositiveBtn', 'cfgRatingDismissBtn'];
  inputs.forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      el.addEventListener('input', updateRatingSimulatorPreview);
    }
  });
}

// ========================================================
// 💰 ADMOB LIVE MONETIZATION & AD UNITS MANAGER
// ========================================================

const GOOGLE_ADMOB_TEST_IDS = {
  appId: 'ca-app-pub-3940256099942544~3347511713',
  bannerId: 'ca-app-pub-3940256099942544/6300978111',
  interstitialId: 'ca-app-pub-3940256099942544/1033173712',
  rewardedId: 'ca-app-pub-3940256099942544/5224354917',
  appOpenId: 'ca-app-pub-3940256099942544/9257395921',
  nativeId: 'ca-app-pub-3940256099942544/2247696110'
};

async function loadAdmobConfig() {
  try {
    const res = await fetch('/api/app-config');
    const json = await res.json();
    if (!json.success || !json.data) return;

    const admob = json.data.admob || {};

    const cfgBanner = document.getElementById('cfgBanner');
    const cfgInterstitial = document.getElementById('cfgInterstitial');
    const cfgNative = document.getElementById('cfgNative');
    const cfgRewardedInterval = document.getElementById('cfgRewardedInterval');
    const cfgInterstitialInterval = document.getElementById('cfgInterstitialInterval');
    const cfgNativeInterval = document.getElementById('cfgNativeInterval');
    const cfgAdmobAppId = document.getElementById('cfgAdmobAppId');
    const cfgAdmobBannerId = document.getElementById('cfgAdmobBannerId');
    const cfgAdmobInterstitialId = document.getElementById('cfgAdmobInterstitialId');
    const cfgAdmobRewardedId = document.getElementById('cfgAdmobRewardedId');
    const cfgAdmobAppOpenId = document.getElementById('cfgAdmobAppOpenId');
    const cfgAdmobNativeId = document.getElementById('cfgAdmobNativeId');
    const cfgAdmobTestMode = document.getElementById('cfgAdmobTestMode');

    if (cfgBanner) cfgBanner.checked = admob.bannerEnabled !== false;
    if (cfgInterstitial) cfgInterstitial.checked = admob.interstitialEnabled !== false;
    if (cfgNative) cfgNative.checked = admob.nativeEnabled !== false;
    if (cfgRewardedInterval) cfgRewardedInterval.value = admob.rewardedInterval || 3;
    if (cfgInterstitialInterval) cfgInterstitialInterval.value = admob.interstitialInterval || 3;
    if (cfgNativeInterval) cfgNativeInterval.value = admob.nativeInterval || 6;

    if (cfgAdmobAppId) cfgAdmobAppId.value = admob.appId || GOOGLE_ADMOB_TEST_IDS.appId;
    if (cfgAdmobBannerId) cfgAdmobBannerId.value = admob.bannerId || GOOGLE_ADMOB_TEST_IDS.bannerId;
    if (cfgAdmobInterstitialId) cfgAdmobInterstitialId.value = admob.interstitialId || GOOGLE_ADMOB_TEST_IDS.interstitialId;
    if (cfgAdmobRewardedId) cfgAdmobRewardedId.value = admob.rewardedId || GOOGLE_ADMOB_TEST_IDS.rewardedId;
    if (cfgAdmobAppOpenId) cfgAdmobAppOpenId.value = admob.appOpenId || GOOGLE_ADMOB_TEST_IDS.appOpenId;
    if (cfgAdmobNativeId) cfgAdmobNativeId.value = admob.nativeId || GOOGLE_ADMOB_TEST_IDS.nativeId;
    if (cfgAdmobTestMode) cfgAdmobTestMode.checked = admob.isTestMode !== false;

    updateAdmobBadge();
  } catch (err) {
    console.error('Failed to load AdMob config:', err);
  }
}

function updateAdmobBadge() {
  const badge = document.getElementById('admobModeBadge');
  const isTestMode = document.getElementById('cfgAdmobTestMode')?.checked ?? true;
  const appId = document.getElementById('cfgAdmobAppId')?.value || '';
  const isGoogleSample = appId.includes('3940256099942544');

  if (badge) {
    if (isTestMode || isGoogleSample) {
      badge.textContent = '🧪 Google Test Mode';
      badge.style.borderColor = 'var(--accent-cyan)';
      badge.style.color = 'var(--accent-cyan)';
      badge.style.background = 'rgba(0, 229, 255, 0.1)';
    } else {
      badge.textContent = '🚀 Production Live';
      badge.style.borderColor = '#22c55e';
      badge.style.color = '#22c55e';
      badge.style.background = 'rgba(34, 197, 94, 0.15)';
    }
  }
}

function loadGoogleTestAdmobIds() {
  const cfgAdmobAppId = document.getElementById('cfgAdmobAppId');
  const cfgAdmobBannerId = document.getElementById('cfgAdmobBannerId');
  const cfgAdmobInterstitialId = document.getElementById('cfgAdmobInterstitialId');
  const cfgAdmobRewardedId = document.getElementById('cfgAdmobRewardedId');
  const cfgAdmobAppOpenId = document.getElementById('cfgAdmobAppOpenId');
  const cfgAdmobNativeId = document.getElementById('cfgAdmobNativeId');
  const cfgAdmobTestMode = document.getElementById('cfgAdmobTestMode');

  if (cfgAdmobAppId) cfgAdmobAppId.value = GOOGLE_ADMOB_TEST_IDS.appId;
  if (cfgAdmobBannerId) cfgAdmobBannerId.value = GOOGLE_ADMOB_TEST_IDS.bannerId;
  if (cfgAdmobInterstitialId) cfgAdmobInterstitialId.value = GOOGLE_ADMOB_TEST_IDS.interstitialId;
  if (cfgAdmobRewardedId) cfgAdmobRewardedId.value = GOOGLE_ADMOB_TEST_IDS.rewardedId;
  if (cfgAdmobAppOpenId) cfgAdmobAppOpenId.value = GOOGLE_ADMOB_TEST_IDS.appOpenId;
  if (cfgAdmobNativeId) cfgAdmobNativeId.value = GOOGLE_ADMOB_TEST_IDS.nativeId;
  if (cfgAdmobTestMode) cfgAdmobTestMode.checked = true;

  updateAdmobBadge();
  showToast('🧪 Loaded Google official sample AdMob test IDs! Safe from policy bans.');
}

window.copyAdmobUnit = function(elementId) {
  const input = document.getElementById(elementId);
  if (!input || !input.value) return;
  navigator.clipboard.writeText(input.value).then(() => {
    showToast(`📋 Copied: ${input.value}`);
  }).catch(() => {
    input.select();
    document.execCommand('copy');
    showToast(`📋 Copied: ${input.value}`);
  });
};

async function saveAdmobConfig() {
  const saveBtn = document.getElementById('btnSaveAdmobConfig');
  if (saveBtn) {
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';
  }

  const bannerEnabled = document.getElementById('cfgBanner')?.checked ?? true;
  const interstitialEnabled = document.getElementById('cfgInterstitial')?.checked ?? true;
  const nativeEnabled = document.getElementById('cfgNative')?.checked ?? true;
  const rewardedInterval = parseInt(document.getElementById('cfgRewardedInterval')?.value || '3', 10);
  const interstitialInterval = parseInt(document.getElementById('cfgInterstitialInterval')?.value || '3', 10);
  const nativeInterval = parseInt(document.getElementById('cfgNativeInterval')?.value || '6', 10);
  const appId = document.getElementById('cfgAdmobAppId')?.value.trim() || GOOGLE_ADMOB_TEST_IDS.appId;
  const bannerId = document.getElementById('cfgAdmobBannerId')?.value.trim() || GOOGLE_ADMOB_TEST_IDS.bannerId;
  const interstitialId = document.getElementById('cfgAdmobInterstitialId')?.value.trim() || GOOGLE_ADMOB_TEST_IDS.interstitialId;
  const rewardedId = document.getElementById('cfgAdmobRewardedId')?.value.trim() || GOOGLE_ADMOB_TEST_IDS.rewardedId;
  const appOpenId = document.getElementById('cfgAdmobAppOpenId')?.value.trim() || GOOGLE_ADMOB_TEST_IDS.appOpenId;
  const nativeId = document.getElementById('cfgAdmobNativeId')?.value.trim() || GOOGLE_ADMOB_TEST_IDS.nativeId;
  const isTestMode = document.getElementById('cfgAdmobTestMode')?.checked ?? true;

  try {
    const res = await authFetch('/api/admin/admob', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        bannerEnabled,
        interstitialEnabled,
        nativeEnabled,
        rewardedInterval,
        interstitialInterval,
        nativeInterval,
        appId,
        bannerId,
        interstitialId,
        rewardedId,
        appOpenId,
        nativeId,
        isTestMode
      })
    });

    const json = await res.json();
    if (json.success) {
      showToast('💰 AdMob Monetization & Ad Unit IDs saved successfully!');
      updateAdmobBadge();

      // Also save Daily Pick and Maintenance Mode if present
      const dailyPickId = document.getElementById('cfgDailyPickSelect')?.value;
      const maintenance = document.getElementById('cfgMaintenance')?.checked;
      if (dailyPickId !== undefined || maintenance !== undefined) {
        await authFetch('/api/admin/config', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            daily_pick_id: dailyPickId || '',
            maintenance_mode: maintenance ? 'true' : 'false'
          })
        });
      }
    } else {
      showToast('Error: ' + (json.error || 'Failed to save AdMob config'));
    }
  } catch (err) {
    showToast('Network error: ' + err.message);
  } finally {
    if (saveBtn) {
      saveBtn.disabled = false;
      saveBtn.textContent = '💾 Save Monetization';
    }
  }
}

function initAdmobStudio() {
  const saveBtn = document.getElementById('btnSaveAdmobConfig');
  if (saveBtn) {
    saveBtn.addEventListener('click', saveAdmobConfig);
  }

  const loadTestBtn = document.getElementById('btnLoadTestAdmob');
  if (loadTestBtn) {
    loadTestBtn.addEventListener('click', loadGoogleTestAdmobIds);
  }

  const testModeCheck = document.getElementById('cfgAdmobTestMode');
  if (testModeCheck) {
    testModeCheck.addEventListener('change', updateAdmobBadge);
  }

  const appIdInput = document.getElementById('cfgAdmobAppId');
  if (appIdInput) {
    appIdInput.addEventListener('input', updateAdmobBadge);
  }
}

// ========================================================
// ⚡ DYNAMIC CHARGING ANIMATIONS STUDIO & SIMULATOR LOGIC
// ========================================================

let allChargingAnimations = [];
let currentSimulatedAnim = null;
let currentLottieInstance = null;
let simBatteryPercent = 85;
let simSoundMuted = false;
let simSpeed = 1.0;

function initChargingStudio() {
  const addBtn = document.getElementById('openAddChargingModalBtn');
  const refreshBtn = document.getElementById('refreshChargingBtn');
  const closeBtn = document.getElementById('closeChargingModal');
  const cancelBtn = document.getElementById('cancelChargingModalBtn');
  const modal = document.getElementById('chargingModal');
  const form = document.getElementById('chargingForm');

  const categoryFilter = document.getElementById('chargingCategoryFilter');
  const statusFilter = document.getElementById('chargingStatusFilter');
  const searchInput = document.getElementById('chargingSearchInput');

  const colorPicker = document.getElementById('chargingColorPicker');
  const textColorInput = document.getElementById('chargingTextColorInput');

  const dropArea = document.getElementById('chargingAnimDropArea');
  const fileInput = document.getElementById('chargingAnimFileInput');
  const fileNameDisplay = document.getElementById('chargingAnimFileName');

  // Open Add Modal
  window.openAddChargingModal = function() {
    const modalEl = document.getElementById('chargingModal');
    const formEl = document.getElementById('chargingForm');
    const titleEl = document.getElementById('modalChargingTitle');
    const idInput = document.getElementById('chargingIdInput');
    const sortInput = document.getElementById('chargingSortOrderInput');
    const textColInput = document.getElementById('chargingTextColorInput');
    const colorPickerInput = document.getElementById('chargingColorPicker');
    const activeCheck = document.getElementById('chargingIsActiveCheck');
    const premiumCheck = document.getElementById('chargingIsPremiumCheck');
    const fileDisplay = document.getElementById('chargingAnimFileName');

    if (titleEl) titleEl.textContent = '⚡ Add Charging Animation';
    if (formEl) formEl.reset();
    if (idInput) idInput.value = '';
    if (sortInput) sortInput.value = (allChargingAnimations.length + 1);
    if (textColInput) textColInput.value = '#00E5FF';
    if (colorPickerInput) colorPickerInput.value = '#00E5FF';
    if (activeCheck) activeCheck.checked = true;
    if (premiumCheck) premiumCheck.checked = false;
    if (fileDisplay) fileDisplay.textContent = 'Supports .json, .mp4, .webm (Max 50MB)';
    if (modalEl) modalEl.classList.add('active');
  };

  if (addBtn) {
    addBtn.addEventListener('click', window.openAddChargingModal);
  }

  // Close Modal
  [closeBtn, cancelBtn].forEach(btn => {
    if (btn) {
      btn.addEventListener('click', () => {
        modal.classList.remove('active');
      });
    }
  });

  // Color picker sync
  if (colorPicker && textColorInput) {
    colorPicker.addEventListener('input', (e) => {
      textColorInput.value = e.target.value.toUpperCase();
    });
    textColorInput.addEventListener('input', (e) => {
      const val = e.target.value;
      if (/^#[0-9A-F]{6}$/i.test(val)) {
        colorPicker.value = val;
      }
    });
  }

  // Color preset buttons
  document.querySelectorAll('.color-preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const color = btn.dataset.color;
      if (color && colorPicker && textColorInput) {
        colorPicker.value = color;
        textColorInput.value = color.toUpperCase();
      }
    });
  });

  // Drag & drop file upload area
  if (dropArea && fileInput) {
    dropArea.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', () => {
      if (fileInput.files && fileInput.files[0]) {
        fileNameDisplay.textContent = `Selected: ${fileInput.files[0].name} (${(fileInput.files[0].size / 1024).toFixed(1)} KB)`;
        const name = fileInput.files[0].name.toLowerCase();
        const typeSelect = document.getElementById('chargingTypeSelect');
        if (typeSelect) {
          if (name.endsWith('.mp4') || name.endsWith('.webm')) {
            typeSelect.value = 'video';
          } else if (name.endsWith('.json')) {
            typeSelect.value = 'lottie';
          }
        }
      }
    });

    ['dragenter', 'dragover'].forEach(eventName => {
      dropArea.addEventListener(eventName, (e) => {
        e.preventDefault();
        dropArea.style.borderColor = 'var(--accent-cyan)';
        dropArea.style.background = 'rgba(0, 229, 255, 0.1)';
      });
    });

    ['dragleave', 'drop'].forEach(eventName => {
      dropArea.addEventListener(eventName, (e) => {
        e.preventDefault();
        dropArea.style.borderColor = 'rgba(0, 229, 255, 0.3)';
        dropArea.style.background = 'rgba(0, 229, 255, 0.03)';
      });
    });

    dropArea.addEventListener('drop', (e) => {
      if (e.dataTransfer.files && e.dataTransfer.files[0]) {
        fileInput.files = e.dataTransfer.files;
        fileNameDisplay.textContent = `Selected: ${fileInput.files[0].name} (${(fileInput.files[0].size / 1024).toFixed(1)} KB)`;
      }
    });
  }

  // Refresh
  if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
      loadChargingAnimations();
      showToast('Charging animations refreshed!');
    });
  }

  // Filters
  if (categoryFilter) {
    categoryFilter.addEventListener('change', renderChargingCards);
  }
  if (statusFilter) {
    statusFilter.addEventListener('change', renderChargingCards);
  }
  if (searchInput) {
    searchInput.addEventListener('input', renderChargingCards);
  }

  // Form submit (Add or Edit)
  if (form) {
    form.addEventListener('submit', handleChargingFormSubmit);
  }

  // Simulator controls
  initSimulatorControls();
}

// Load animations from server
async function loadChargingAnimations() {
  try {
    const res = await authFetch('/api/admin/charging-animations');
    const json = await res.json();
    if (json.success) {
      allChargingAnimations = json.data || [];

      // Update counters
      const totalEl = document.getElementById('totalChargingCount');
      const activeEl = document.getElementById('activeChargingCount');
      const appliedEl = document.getElementById('totalChargingApplied');
      const catCountEl = document.getElementById('chargingCategoriesCount');

      if (totalEl) totalEl.textContent = allChargingAnimations.length;
      if (activeEl) activeEl.textContent = allChargingAnimations.filter(a => a.isActive).length;
      if (appliedEl) {
        const total = allChargingAnimations.reduce((acc, cur) => acc + (cur.downloads || 0), 0);
        appliedEl.textContent = total.toLocaleString();
      }
      if (catCountEl) {
        const distinct = new Set(allChargingAnimations.map(a => a.category));
        catCountEl.textContent = distinct.size;
      }

      renderChargingCards();

      // If nothing simulated yet, load the first item
      if (!currentSimulatedAnim && allChargingAnimations.length > 0) {
        loadInSimulator(allChargingAnimations[0]);
      }
    }
  } catch (err) {
    console.error('Error loading charging animations:', err);
  }
}

// Render cards
function renderChargingCards() {
  const container = document.getElementById('chargingCardsGrid');
  if (!container) return;

  const catFilter = document.getElementById('chargingCategoryFilter')?.value || 'ALL';
  const statusFilter = document.getElementById('chargingStatusFilter')?.value || 'ALL';
  const search = (document.getElementById('chargingSearchInput')?.value || '').trim().toLowerCase();

  const filtered = allChargingAnimations.filter(item => {
    if (catFilter !== 'ALL' && item.category !== catFilter) return false;
    if (statusFilter === 'ACTIVE' && !item.isActive) return false;
    if (statusFilter === 'INACTIVE' && item.isActive) return false;
    if (search && !item.title.toLowerCase().includes(search)) return false;
    return true;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align:center; padding: 48px 20px; background: rgba(255,255,255,0.02); border-radius: 16px; border: 1px dashed rgba(255,255,255,0.1);">
        <div style="font-size:36px; margin-bottom:12px;">⚡</div>
        <div style="font-size:16px; font-weight:700; color:#fff;">No Charging Animations Found</div>
        <div class="text-muted" style="font-size:13px; margin-top:4px;">Try changing your search or filter, or click "+ Add Charging Animation"</div>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(anim => {
    const isSelected = currentSimulatedAnim && currentSimulatedAnim.id === anim.id;
    const formatBadge = anim.animationType === 'video'
      ? `<span class="badge-format video">🎬 Video MP4</span>`
      : `<span class="badge-format lottie">⚡ Lottie JSON</span>`;
    const vipBadge = anim.isPremium ? `<span class="badge-format" style="border-color:#eab308; color:#facc15;">⭐ VIP</span>` : '';
    const soundIndicator = anim.soundUrl
      ? `<div class="badge-sound-indicator" title="Custom plug-in sound enabled">🔊</div>`
      : '';

    return `
      <div class="charging-card ${isSelected ? 'active-selected' : ''}" id="card-${anim.id}" onclick="selectAndSimulate('${anim.id}')" style="cursor:pointer;">
        <div class="charging-card-preview">
          <img src="${anim.previewUrl || '/uploads/charging/thumb_neon_arc.svg'}" alt="${anim.title}" onerror="this.src='/uploads/charging/thumb_neon_arc.svg'">
          <div class="charging-card-badges">
            ${formatBadge}
            ${vipBadge}
          </div>
          ${soundIndicator}
        </div>
        <div class="charging-card-body">
          <div class="charging-card-title" title="${anim.title}">${anim.title}</div>
          <div class="charging-card-meta">
            <span style="display:flex; align-items:center;">
              <span class="charging-color-dot" style="background:${anim.textColor}; color:${anim.textColor};"></span>
              <span style="font-weight:700; color:#cbd5e1;">${anim.category}</span>
            </span>
            <span>⚡ ${anim.downloads || 0} applied</span>
          </div>
          <div class="charging-card-actions">
            <button class="btn-preview-sim" onclick="event.stopPropagation(); selectAndSimulate('${anim.id}')">
              👁️ Simulate
            </button>
            <div style="display:flex; align-items:center; gap:8px;">
              <label class="switch" title="Toggle active status in app" onclick="event.stopPropagation()">
                <input type="checkbox" ${anim.isActive ? 'checked' : ''} onchange="toggleChargingActive('${anim.id}', this.checked)">
                <span class="slider"></span>
              </label>
              <button class="btn-icon-tiny" title="Edit Animation" onclick="event.stopPropagation(); openEditChargingModal('${anim.id}')">
                <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
              <button class="btn-icon-tiny text-danger" title="Delete Animation" onclick="event.stopPropagation(); deleteChargingAnimation('${anim.id}')">
                <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/></svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

// Select item to preview in phone simulator
window.selectAndSimulate = function(id) {
  const anim = allChargingAnimations.find(a => a.id === id);
  if (anim) {
    loadInSimulator(anim);
    document.querySelectorAll('.charging-card').forEach(c => c.classList.remove('active-selected'));
    const card = document.getElementById(`card-${id}`);
    if (card) card.classList.add('active-selected');
  }
};

// Load in Simulator
function loadInSimulator(anim) {
  currentSimulatedAnim = anim;

  const titleBadge = document.getElementById('simCurrentAnimTitle');
  if (titleBadge) titleBadge.textContent = anim.title;

  const lottieContainer = document.getElementById('simLottieContainer');
  const videoPlayer = document.getElementById('simVideoPlayer');

  // Apply colors to HUD
  const glowColor = anim.textColor || '#00E5FF';
  const bolt = document.getElementById('hudBolt');
  const hudRingFg = document.getElementById('hudRingFg');
  const chargingType = document.getElementById('hudChargingType');
  const hudWattage = document.getElementById('hudWattage');

  if (bolt) {
    bolt.style.color = glowColor;
    bolt.style.filter = `drop-shadow(0 0 10px ${glowColor})`;
  }
  if (hudRingFg) {
    hudRingFg.style.stroke = glowColor;
    hudRingFg.style.filter = `drop-shadow(0 0 10px ${glowColor})`;
  }
  if (chargingType) {
    chargingType.style.color = glowColor;
    chargingType.style.textShadow = `0 0 10px ${glowColor}`;
  }
  if (hudWattage) {
    hudWattage.style.color = glowColor;
  }

  // Load animation
  if (anim.animationType === 'video') {
    if (currentLottieInstance) {
      currentLottieInstance.destroy();
      currentLottieInstance = null;
    }
    if (lottieContainer) lottieContainer.style.display = 'none';
    if (videoPlayer) {
      videoPlayer.style.display = 'block';
      videoPlayer.src = anim.animationUrl;
      videoPlayer.playbackRate = simSpeed;
      videoPlayer.play().catch(() => {});
    }
  } else {
    // Lottie
    if (videoPlayer) {
      videoPlayer.pause();
      videoPlayer.style.display = 'none';
    }
    if (lottieContainer) {
      lottieContainer.style.display = 'block';
      lottieContainer.innerHTML = '';
      if (currentLottieInstance) {
        currentLottieInstance.destroy();
        currentLottieInstance = null;
      }
      try {
        if (typeof lottie !== 'undefined') {
          currentLottieInstance = lottie.loadAnimation({
            container: lottieContainer,
            renderer: 'svg',
            loop: true,
            autoplay: true,
            path: anim.animationUrl
          });
          currentLottieInstance.setSpeed(simSpeed);
        }
      } catch (e) {
        console.warn('Lottie load error in simulator:', e);
      }
    }
  }

  updateSimulatorBattery(simBatteryPercent);
}

// Update battery level in simulator
function updateSimulatorBattery(percent) {
  simBatteryPercent = Math.min(100, Math.max(1, percent));

  const hudPercentDisplay = document.getElementById('hudPercentDisplay');
  const simBatteryMini = document.getElementById('simBatteryMini');
  const simSliderVal = document.getElementById('simSliderVal');
  const hudRingFg = document.getElementById('hudRingFg');

  if (hudPercentDisplay) hudPercentDisplay.textContent = `${simBatteryPercent}%`;
  if (simBatteryMini) simBatteryMini.textContent = `${simBatteryPercent}%`;
  if (simSliderVal) simSliderVal.textContent = `${simBatteryPercent}%`;

  if (hudRingFg) {
    const offset = 440 * (1 - (simBatteryPercent / 100));
    hudRingFg.style.strokeDashoffset = offset;
  }
}

// Simulator interactive controls
function initSimulatorControls() {
  const slider = document.getElementById('simBatterySlider');
  const plugInBtn = document.getElementById('simPlugInBtn');
  const soundBtn = document.getElementById('simToggleSoundBtn');
  const speedBtn = document.getElementById('simCycleSpeedBtn');

  if (slider) {
    slider.addEventListener('input', (e) => {
      updateSimulatorBattery(parseInt(e.target.value, 10));
    });
  }

  if (plugInBtn) {
    plugInBtn.addEventListener('click', triggerSimulatorPlugIn);
  }

  if (soundBtn) {
    soundBtn.addEventListener('click', () => {
      simSoundMuted = !simSoundMuted;
      soundBtn.textContent = simSoundMuted ? '🔇' : '🔊';
      showToast(simSoundMuted ? 'Simulator audio muted' : 'Simulator audio enabled');
    });
  }

  if (speedBtn) {
    const speeds = [1.0, 1.5, 2.0];
    let idx = 0;
    speedBtn.addEventListener('click', () => {
      idx = (idx + 1) % speeds.length;
      simSpeed = speeds[idx];
      speedBtn.textContent = `${simSpeed.toFixed(1)}x`;
      if (currentLottieInstance) currentLottieInstance.setSpeed(simSpeed);
      const vid = document.getElementById('simVideoPlayer');
      if (vid) vid.playbackRate = simSpeed;
      showToast(`Playback speed: ${simSpeed}x`);
    });
  }

  setInterval(() => {
    const timeEl = document.getElementById('simStatusTime');
    if (timeEl) {
      const now = new Date();
      timeEl.textContent = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    }
  }, 1000);
}

// Trigger plug-in effect in simulator
function triggerSimulatorPlugIn() {
  const screen = document.getElementById('simPhoneScreen');
  if (screen) {
    screen.style.transition = 'filter 0.2s, transform 0.2s';
    screen.style.filter = 'brightness(1.6) drop-shadow(0 0 25px var(--accent-cyan))';
    screen.style.transform = 'scale(1.02)';
    setTimeout(() => {
      screen.style.filter = 'none';
      screen.style.transform = 'scale(1)';
    }, 450);
  }

  if (!simSoundMuted) {
    if (currentSimulatedAnim && currentSimulatedAnim.soundUrl) {
      try {
        const audio = new Audio(currentSimulatedAnim.soundUrl);
        audio.play().catch(() => {});
      } catch (e) {}
    } else {
      playSynthChime();
    }
  }
  showToast('⚡ Charger connected! Animation triggered.');
}

// Web Audio API Synthesizer for high-tech plug-in chime
function playSynthChime() {
  try {
    const AudioCtx = window.AudioContext || window.webkitAudioContext;
    if (!AudioCtx) return;
    const ctx = new AudioCtx();
    const osc1 = ctx.createOscillator();
    const osc2 = ctx.createOscillator();
    const gain = ctx.createGain();

    osc1.type = 'sine';
    osc2.type = 'triangle';

    osc1.frequency.setValueAtTime(440, ctx.currentTime);
    osc1.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.15);
    osc1.frequency.exponentialRampToValueAtTime(1320, ctx.currentTime + 0.35);

    osc2.frequency.setValueAtTime(220, ctx.currentTime);
    osc2.frequency.exponentialRampToValueAtTime(660, ctx.currentTime + 0.25);

    gain.gain.setValueAtTime(0.15, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.45);

    osc1.connect(gain);
    osc2.connect(gain);
    gain.connect(ctx.destination);

    osc1.start();
    osc2.start();
    osc1.stop(ctx.currentTime + 0.45);
    osc2.stop(ctx.currentTime + 0.45);
  } catch (e) {}
}

// Toggle Active
window.toggleChargingActive = async function(id, isActive) {
  try {
    const res = await authFetch(`/api/admin/charging-animations/${id}/toggle`, {
      method: 'PATCH'
    });
    const json = await res.json();
    if (json.success) {
      showToast(json.message);
      const item = allChargingAnimations.find(a => a.id === id);
      if (item) item.isActive = isActive;
      const activeEl = document.getElementById('activeChargingCount');
      if (activeEl) activeEl.textContent = allChargingAnimations.filter(a => a.isActive).length;
    } else {
      showToast('Error: ' + json.error);
    }
  } catch (err) {
    showToast('Failed to toggle status');
  }
};

// Edit Modal
window.openEditChargingModal = function(id) {
  const item = allChargingAnimations.find(a => a.id === id);
  if (!item) return;

  const modal = document.getElementById('chargingModal');
  document.getElementById('modalChargingTitle').textContent = '✏️ Edit Charging Animation';
  document.getElementById('chargingIdInput').value = item.id;
  document.getElementById('chargingTitleInput').value = item.title;
  document.getElementById('chargingCategorySelect').value = item.category || 'NEON';
  document.getElementById('chargingTypeSelect').value = item.animationType || 'lottie';
  document.getElementById('chargingDirectAnimUrlInput').value = item.animationUrl || '';
  document.getElementById('chargingDirectPreviewUrlInput').value = item.previewUrl || '';
  document.getElementById('chargingDirectSoundUrlInput').value = item.soundUrl || '';
  document.getElementById('chargingTextColorInput').value = item.textColor || '#00E5FF';
  document.getElementById('chargingColorPicker').value = item.textColor || '#00E5FF';
  document.getElementById('chargingSortOrderInput').value = item.sortOrder || 1;
  document.getElementById('chargingIsPremiumCheck').checked = Boolean(item.isPremium);
  document.getElementById('chargingIsActiveCheck').checked = Boolean(item.isActive);
  document.getElementById('chargingAnimFileName').textContent = `Current source: ${item.animationUrl}`;

  modal.classList.add('active');
};

// Delete Animation
window.deleteChargingAnimation = async function(id) {
  if (!confirm('Are you sure you want to delete this charging animation?')) return;
  try {
    const res = await authFetch(`/api/admin/charging-animations/${id}`, {
      method: 'DELETE'
    });
    const json = await res.json();
    if (json.success) {
      showToast('Charging animation deleted');
      allChargingAnimations = allChargingAnimations.filter(a => a.id !== id);
      renderChargingCards();
      if (currentSimulatedAnim && currentSimulatedAnim.id === id && allChargingAnimations.length > 0) {
        loadInSimulator(allChargingAnimations[0]);
      }
    } else {
      showToast('Error: ' + json.error);
    }
  } catch (err) {
    showToast('Failed to delete animation');
  }
};

// Form submit handler (Multipart FormData)
async function handleChargingFormSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('chargingIdInput').value;
  const isEditing = Boolean(id);

  const title = document.getElementById('chargingTitleInput').value;
  const category = document.getElementById('chargingCategorySelect').value;
  const animationType = document.getElementById('chargingTypeSelect').value;
  const directAnimationUrl = document.getElementById('chargingDirectAnimUrlInput').value;
  const directPreviewUrl = document.getElementById('chargingDirectPreviewUrlInput').value;
  const directSoundUrl = document.getElementById('chargingDirectSoundUrlInput').value;
  const textColor = document.getElementById('chargingTextColorInput').value;
  const sortOrder = document.getElementById('chargingSortOrderInput').value;
  const isPremium = document.getElementById('chargingIsPremiumCheck').checked;
  const isActive = document.getElementById('chargingIsActiveCheck').checked;

  const animFile = document.getElementById('chargingAnimFileInput').files[0];
  const previewFile = document.getElementById('chargingPreviewFileInput').files[0];
  const soundFile = document.getElementById('chargingSoundFileInput').files[0];

  if (!isEditing && !animFile && !directAnimationUrl) {
    showToast('Please provide an animation file or direct URL');
    return;
  }

  const formData = new FormData();
  formData.append('title', title);
  formData.append('category', category);
  formData.append('animationType', animationType);
  formData.append('textColor', textColor);
  formData.append('sortOrder', sortOrder);
  formData.append('isPremium', isPremium);
  formData.append('isActive', isActive);
  if (directAnimationUrl) formData.append('directAnimationUrl', directAnimationUrl);
  if (directPreviewUrl) formData.append('directPreviewUrl', directPreviewUrl);
  if (directSoundUrl) formData.append('directSoundUrl', directSoundUrl);

  if (animFile) formData.append('animationFile', animFile);
  if (previewFile) formData.append('previewFile', previewFile);
  if (soundFile) formData.append('soundFile', soundFile);

  const submitBtn = document.getElementById('saveChargingSubmitBtn');
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.textContent = '⏳ Uploading & Saving...';
  }

  try {
    const url = isEditing
      ? `/api/admin/charging-animations/${id}`
      : '/api/admin/charging-animations';
    const method = isEditing ? 'PUT' : 'POST';

    const res = await authFetch(url, {
      method,
      body: formData
    });
    const json = await res.json();
    if (json.success) {
      showToast(json.message || 'Charging animation saved!');
      document.getElementById('chargingModal').classList.remove('active');
      await loadChargingAnimations();
      if (json.data) {
        loadInSimulator(json.data);
      }
    } else {
      showToast('Error: ' + json.error);
    }
  } catch (err) {
    showToast('Network error: ' + err.message);
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = '💾 Save Charging Animation';
    }
  }
}

// ========================================================
// 🌈 DYNAMIC EDGE LIGHTING STUDIO IMPLEMENTATION
// ========================================================

let allEdgePresets = [];
let activeEdgeSimPreset = null;
let modalEdgeColors = ['#FF0055', '#FF7700', '#FFE600', '#00FF66', '#00E5FF', '#7000FF'];
let edgeSimTriggerMode = 'always';

function initEdgeLightingStudio() {
  const closeBtn = document.getElementById('closeEdgeModal');
  const cancelBtn = document.getElementById('cancelEdgeModalBtn');
  const modal = document.getElementById('edgeLightingModal');
  const form = document.getElementById('edgeForm');
  const refreshBtn = document.getElementById('refreshEdgeBtn');
  const categoryFilter = document.getElementById('edgeCategoryFilter');
  const statusFilter = document.getElementById('edgeStatusFilter');
  const searchInput = document.getElementById('edgeSearchInput');

  if (closeBtn && modal) {
    closeBtn.addEventListener('click', () => modal.classList.remove('active'));
  }
  if (cancelBtn && modal) {
    cancelBtn.addEventListener('click', () => modal.classList.remove('active'));
  }
  if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
      loadEdgeLightingPresets();
      showToast('Edge lighting presets refreshed!');
    });
  }
  if (categoryFilter) categoryFilter.addEventListener('change', renderEdgeLightingCatalog);
  if (statusFilter) statusFilter.addEventListener('change', renderEdgeLightingCatalog);
  if (searchInput) searchInput.addEventListener('input', renderEdgeLightingCatalog);
  if (form) form.addEventListener('submit', handleEdgeFormSubmit);

  // Simulator Sliders
  const speedSlider = document.getElementById('edgeSimSpeedSlider');
  const thicknessSlider = document.getElementById('edgeSimThicknessSlider');
  const radiusSlider = document.getElementById('edgeSimRadiusSlider');
  const punchCheck = document.getElementById('edgeSimPunchCheck');

  if (speedSlider) {
    speedSlider.addEventListener('input', (e) => {
      document.getElementById('edgeSimSpeedVal').textContent = `${parseFloat(e.target.value).toFixed(1)}x`;
      updateEdgeSimulator();
    });
  }
  if (thicknessSlider) {
    thicknessSlider.addEventListener('input', (e) => {
      document.getElementById('edgeSimThicknessVal').textContent = `${e.target.value} px`;
      updateEdgeSimulator();
    });
  }
  if (radiusSlider) {
    radiusSlider.addEventListener('input', (e) => {
      document.getElementById('edgeSimRadiusVal').textContent = `${e.target.value} px`;
      updateEdgeSimulator();
    });
  }
  if (punchCheck) {
    punchCheck.addEventListener('change', updateEdgeSimulator);
  }

  // Modal sliders live text
  const mSpeed = document.getElementById('edgeSpeedInput');
  const mBorder = document.getElementById('edgeBorderInput');
  const mRadius = document.getElementById('edgeRadiusInput');
  const mPunch = document.getElementById('edgePunchInput');

  if (mSpeed) mSpeed.addEventListener('input', (e) => {
    document.getElementById('modalEdgeSpeedText').textContent = `${parseFloat(e.target.value).toFixed(1)}x`;
  });
  if (mBorder) mBorder.addEventListener('input', (e) => {
    document.getElementById('modalEdgeThicknessText').textContent = `${e.target.value}px`;
  });
  if (mRadius) mRadius.addEventListener('input', (e) => {
    document.getElementById('modalEdgeRadiusText').textContent = `${e.target.value}px`;
  });
  if (mPunch) mPunch.addEventListener('input', (e) => {
    document.getElementById('modalEdgePunchText').textContent = `${e.target.value}px`;
  });
}

async function loadEdgeLightingPresets() {
  try {
    const res = await authFetch('/api/admin/edge-lighting');
    const json = await res.json();
    if (json.success) {
      allEdgePresets = json.data || [];

      // Update counters
      const totalEl = document.getElementById('totalEdgeCount');
      const activeEl = document.getElementById('activeEdgeCount');
      const appliedEl = document.getElementById('totalEdgeApplied');
      const catCountEl = document.getElementById('edgeCategoriesCount');

      if (totalEl) totalEl.textContent = allEdgePresets.length;
      if (activeEl) activeEl.textContent = allEdgePresets.filter(p => p.isActive).length;
      if (appliedEl) {
        const total = allEdgePresets.reduce((acc, cur) => acc + (cur.downloads || 0), 0);
        appliedEl.textContent = total.toLocaleString();
      }
      if (catCountEl) {
        const cats = new Set(allEdgePresets.map(p => p.category));
        catCountEl.textContent = cats.size;
      }

      renderEdgeLightingCatalog();

      if (!activeEdgeSimPreset && allEdgePresets.length > 0) {
        selectEdgePresetForSim(allEdgePresets[0]);
      }
    }
  } catch (err) {
    console.error('Failed to load edge lighting presets:', err);
  }
}

function renderEdgeLightingCatalog() {
  const container = document.getElementById('edgeCardsGrid');
  if (!container) return;

  const catFilter = document.getElementById('edgeCategoryFilter')?.value || 'ALL';
  const statusFilter = document.getElementById('edgeStatusFilter')?.value || 'ALL';
  const search = (document.getElementById('edgeSearchInput')?.value || '').toLowerCase().trim();

  const filtered = allEdgePresets.filter(p => {
    if (catFilter !== 'ALL' && p.category !== catFilter) return false;
    if (statusFilter === 'ACTIVE' && !p.isActive) return false;
    if (statusFilter === 'INACTIVE' && p.isActive) return false;
    if (search) {
      const matchTitle = (p.title || '').toLowerCase().includes(search);
      const matchCat = (p.category || '').toLowerCase().includes(search);
      const matchType = (p.animationType || '').toLowerCase().includes(search);
      if (!matchTitle && !matchCat && !matchType) return false;
    }
    return true;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 40px; text-align: center; color: var(--text-secondary); background: rgba(255,255,255,0.02); border-radius: 12px; border: 1px dashed rgba(255,255,255,0.08);">
        <div style="font-size: 32px; margin-bottom: 8px;">🌈</div>
        <div style="font-size: 15px; font-weight: 600; color: #fff;">No Edge Lighting Presets Found</div>
        <div style="font-size: 12px; margin-top: 4px;">Click "+ Add Edge Lighting Preset" above to create your first dynamic border animation.</div>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(p => {
    const isSelected = activeEdgeSimPreset && activeEdgeSimPreset.id === p.id;
    const colors = Array.isArray(p.colors) ? p.colors : ['#00E5FF', '#7000FF'];
    const gradientCss = colors.length > 1 ? `conic-gradient(from 0deg, ${colors.join(', ')}, ${colors[0]})` : colors[0];
    
    const colorChipsHtml = colors.slice(0, 5).map(c => `
      <div class="edge-color-circle" style="background:${c}; box-shadow: 0 0 6px ${c};"></div>
    `).join('');

    return `
      <div class="edge-card ${isSelected ? 'active-preview' : ''}" id="edge-card-${p.id}">
        <div class="edge-card-preview-box" onclick="selectEdgePresetById('${p.id}')" style="cursor:pointer;" title="Click to preview on live phone simulator">
          <div class="edge-mini-phone-outline">
            <div class="edge-mini-glow-border" style="background: ${gradientCss};"></div>
            <div style="font-size: 18px; z-index: 2;">📱</div>
          </div>
          ${p.isPremium ? '<span class="badge" style="position:absolute; top:8px; left:8px; background:linear-gradient(135deg, #FFD700, #FF8800); color:#000; font-weight:700; font-size:10px;">💎 PRO</span>' : ''}
          <span class="badge" style="position:absolute; top:8px; right:8px; background:rgba(0,0,0,0.6); backdrop-filter:blur(4px); font-size:10px; border:1px solid rgba(255,255,255,0.1);">${(p.animationType || '').replace('_', ' ').toUpperCase()}</span>
        </div>

        <div class="edge-card-body">
          <div class="edge-card-title" title="${p.title}">${p.title}</div>
          <div class="edge-card-meta">
            <span>🏷️ ${p.category}</span>
            <span>•</span>
            <span>⚡ ${p.speed}x</span>
            <span>•</span>
            <span>📏 ${p.borderSize}px</span>
            <span>•</span>
            <span>📥 ${(p.downloads || 0).toLocaleString()}</span>
          </div>

          <div class="edge-palette-strip" title="Gradient Palette">
            ${colorChipsHtml}
            ${colors.length > 5 ? `<span style="font-size:10px; color:rgba(255,255,255,0.4);">+${colors.length - 5}</span>` : ''}
          </div>

          <div class="edge-card-footer">
            <div style="display:flex; align-items:center; gap:8px;">
              <button class="btn btn-secondary btn-sm" onclick="selectEdgePresetById('${p.id}')" style="padding:4px 10px; font-size:11px;">
                ${isSelected ? '📱 Active' : '▶️ Preview'}
              </button>
              <button class="btn btn-secondary btn-sm" onclick="openEditEdgeModal('${p.id}')" style="padding:4px 8px;" title="Edit Preset">
                ✏️
              </button>
              <button class="btn btn-secondary btn-sm" onclick="deleteEdgePreset('${p.id}')" style="padding:4px 8px; color:#ff4466;" title="Delete Preset">
                🗑️
              </button>
            </div>

            <label class="switch" title="Toggle active status" style="margin:0;">
              <input type="checkbox" ${p.isActive ? 'checked' : ''} onchange="toggleEdgeActive('${p.id}')">
              <span class="slider"></span>
            </label>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function selectEdgePresetById(id) {
  const found = allEdgePresets.find(p => p.id === id);
  if (found) {
    selectEdgePresetForSim(found);
  }
}

function selectEdgePresetForSim(preset) {
  activeEdgeSimPreset = preset;

  // Highlight card
  document.querySelectorAll('.edge-card').forEach(c => c.classList.remove('active-preview'));
  const card = document.getElementById(`edge-card-${preset.id}`);
  if (card) card.classList.add('active-preview');

  // Update simulator UI texts
  const titleEl = document.getElementById('edgeSimActiveTitle');
  const subEl = document.getElementById('edgeSimActiveSub');
  const badgeEl = document.getElementById('simEdgeStyleBadge');
  const pillsEl = document.getElementById('edgeSimColorPills');

  if (titleEl) titleEl.textContent = preset.title;
  if (subEl) subEl.textContent = `Speed: ${preset.speed}x | Thickness: ${preset.borderSize}px | Radius: ${preset.cornerRadius}px`;
  if (badgeEl) badgeEl.textContent = (preset.animationType || 'RAINBOW_WAVE').replace('_', ' ').toUpperCase();

  // Color pills
  if (pillsEl) {
    const colors = Array.isArray(preset.colors) ? preset.colors : ['#00E5FF', '#7000FF'];
    pillsEl.innerHTML = colors.map(c => `
      <div style="display:flex; align-items:center; gap:4px; background:rgba(0,0,0,0.5); padding:3px 7px; border-radius:12px; border:1px solid rgba(255,255,255,0.1); font-size:9.5px; font-family:monospace; color:#fff;">
        <span style="width:8px; height:8px; border-radius:50%; background:${c}; display:inline-block;"></span>
        ${c}
      </div>
    `).join('');
  }

  // Update sliders
  const speedSlider = document.getElementById('edgeSimSpeedSlider');
  const thicknessSlider = document.getElementById('edgeSimThicknessSlider');
  const radiusSlider = document.getElementById('edgeSimRadiusSlider');
  const punchCheck = document.getElementById('edgeSimPunchCheck');

  if (speedSlider) {
    speedSlider.value = preset.speed || 1.0;
    document.getElementById('edgeSimSpeedVal').textContent = `${(preset.speed || 1.0).toFixed(1)}x`;
  }
  if (thicknessSlider) {
    thicknessSlider.value = preset.borderSize || 6;
    document.getElementById('edgeSimThicknessVal').textContent = `${preset.borderSize || 6} px`;
  }
  if (radiusSlider) {
    radiusSlider.value = preset.cornerRadius || 32;
    document.getElementById('edgeSimRadiusVal').textContent = `${preset.cornerRadius || 32} px`;
  }
  if (punchCheck) {
    punchCheck.checked = (preset.punchHoleRadius || 0) > 0;
  }

  updateEdgeSimulator();
}

function updateEdgeSimulator() {
  if (!activeEdgeSimPreset) return;

  const glowBorder = document.getElementById('edgeGlowBorder');
  const phoneFrame = document.getElementById('edgePhoneFrame');
  const phoneScreen = document.querySelector('.edge-phone-screen');
  const punchAura = document.getElementById('edgePunchHoleAura');

  if (!glowBorder) return;

  const speed = parseFloat(document.getElementById('edgeSimSpeedSlider')?.value || activeEdgeSimPreset.speed || 1.2);
  const thickness = parseInt(document.getElementById('edgeSimThicknessSlider')?.value || activeEdgeSimPreset.borderSize || 6, 10);
  const radius = parseInt(document.getElementById('edgeSimRadiusSlider')?.value || activeEdgeSimPreset.cornerRadius || 32, 10);
  const punchOn = document.getElementById('edgeSimPunchCheck')?.checked ?? false;

  const colors = Array.isArray(activeEdgeSimPreset.colors) ? activeEdgeSimPreset.colors : ['#00E5FF', '#7000FF'];
  const gradientStops = colors.join(', ') + (colors.length > 1 ? `, ${colors[0]}` : '');

  // Frame corner radius
  if (phoneFrame) phoneFrame.style.borderRadius = `${radius + 4}px`;
  if (phoneScreen) phoneScreen.style.borderRadius = `${radius}px`;
  glowBorder.style.borderRadius = `${radius + 4}px`;

  // Compute animation duration inversely proportional to speed: 1.0x = 3s
  const duration = Math.max(0.6, (3.0 / Math.max(0.2, speed))).toFixed(2);

  const animType = activeEdgeSimPreset.animationType || 'rainbow_wave';

  if (animType === 'pulse_glow') {
    glowBorder.style.background = `radial-gradient(ellipse at center, transparent 65%, ${colors[0]} 92%, ${colors[1] || colors[0]} 100%)`;
    glowBorder.style.boxShadow = `inset 0 0 ${thickness * 2}px ${colors[0]}, 0 0 ${thickness * 3}px ${colors[1] || colors[0]}`;
    glowBorder.style.border = `${thickness}px solid ${colors[0]}`;
    glowBorder.style.animation = `pulseEdgeNeon ${duration}s ease-in-out infinite`;
  } else if (animType === 'snake_comet') {
    glowBorder.style.background = `conic-gradient(from 0deg, transparent 0deg, ${colors[0]} 40deg, ${colors[1] || colors[0]} 70deg, transparent 100deg, transparent 180deg, ${colors[0]} 220deg, ${colors[1] || colors[0]} 250deg, transparent 280deg, transparent 360deg)`;
    glowBorder.style.boxShadow = `0 0 ${thickness * 2.5}px ${colors[0]}`;
    glowBorder.style.border = `${thickness}px solid transparent`;
    glowBorder.style.animation = `rotateEdgeRainbow ${duration}s linear infinite`;
  } else {
    // 360 Sweep Rainbow Wave (Default)
    glowBorder.style.background = `conic-gradient(from 0deg, ${gradientStops})`;
    glowBorder.style.boxShadow = `0 0 ${thickness * 3}px ${colors[0]}`;
    glowBorder.style.border = `${thickness}px solid transparent`;
    glowBorder.style.animation = `rotateEdgeRainbow ${duration}s linear infinite`;
  }

  // Punch hole camera aura
  if (punchAura) {
    if (punchOn || (activeEdgeSimPreset.punchHoleRadius || 0) > 0) {
      punchAura.style.opacity = '1';
      punchAura.style.background = `radial-gradient(circle, ${colors[0]} 30%, ${colors[1] || colors[0]} 70%, transparent 100%)`;
      punchAura.style.boxShadow = `0 0 10px ${colors[0]}`;
      punchAura.style.animation = `pulseEdgeNeon ${duration}s ease-in-out infinite`;
    } else {
      punchAura.style.opacity = '0';
    }
  }
}

function setEdgeSimTrigger(mode) {
  edgeSimTriggerMode = mode;
  document.querySelectorAll('.edge-btn-trigger').forEach(b => b.classList.remove('active'));

  if (mode === 'always') {
    document.getElementById('triggerBtnAlways')?.classList.add('active');
    updateEdgeSimulator();
    showToast('Trigger Mode: Always On (Continuous glowing borders)');
  } else if (mode === 'notification') {
    document.getElementById('triggerBtnNotif')?.classList.add('active');
    showToast('Simulating Notification Alert Pulse!');
    const border = document.getElementById('edgeGlowBorder');
    if (border) {
      border.style.animation = 'pulseEdgeNeon 0.5s ease-in-out 4';
    }
  } else if (mode === 'call') {
    document.getElementById('triggerBtnCall')?.classList.add('active');
    showToast('Simulating Incoming Call Alert!');
    const border = document.getElementById('edgeGlowBorder');
    if (border) {
      border.style.animation = 'rotateEdgeRainbow 0.8s linear infinite';
    }
  }
}

function openAddEdgeModal(presetToEdit = null) {
  const modal = document.getElementById('edgeLightingModal');
  const titleEl = document.getElementById('modalEdgeTitle');
  const idInput = document.getElementById('edgeIdInput');
  const titleInput = document.getElementById('edgeTitleInput');
  const catSelect = document.getElementById('edgeCategorySelect');
  const typeSelect = document.getElementById('edgeTypeSelect');
  const speedInput = document.getElementById('edgeSpeedInput');
  const borderInput = document.getElementById('edgeBorderInput');
  const radiusInput = document.getElementById('edgeRadiusInput');
  const punchInput = document.getElementById('edgePunchInput');
  const sortInput = document.getElementById('edgeSortOrderInput');
  const premiumCheck = document.getElementById('edgeIsPremiumCheck');
  const activeCheck = document.getElementById('edgeIsActiveCheck');

  if (presetToEdit) {
    if (titleEl) titleEl.textContent = '✏️ Edit Edge Lighting Preset';
    if (idInput) idInput.value = presetToEdit.id;
    if (titleInput) titleInput.value = presetToEdit.title;
    if (catSelect) catSelect.value = presetToEdit.category;
    if (typeSelect) typeSelect.value = presetToEdit.animationType;
    if (speedInput) speedInput.value = presetToEdit.speed;
    if (borderInput) borderInput.value = presetToEdit.borderSize;
    if (radiusInput) radiusInput.value = presetToEdit.cornerRadius;
    if (punchInput) punchInput.value = presetToEdit.punchHoleRadius || 0;
    if (sortInput) sortInput.value = presetToEdit.sortOrder || 1;
    if (premiumCheck) premiumCheck.checked = Boolean(presetToEdit.isPremium);
    if (activeCheck) activeCheck.checked = Boolean(presetToEdit.isActive);

    modalEdgeColors = Array.isArray(presetToEdit.colors) ? [...presetToEdit.colors] : ['#00E5FF', '#7000FF'];
  } else {
    if (titleEl) titleEl.textContent = '🌈 Add Edge Lighting Preset';
    if (idInput) idInput.value = '';
    if (titleInput) titleInput.value = '';
    if (catSelect) catSelect.value = 'RAINBOW';
    if (typeSelect) typeSelect.value = 'rainbow_wave';
    if (speedInput) speedInput.value = 1.2;
    if (borderInput) borderInput.value = 6;
    if (radiusInput) radiusInput.value = 32;
    if (punchInput) punchInput.value = 0;
    if (sortInput) sortInput.value = allEdgePresets.length + 1;
    if (premiumCheck) premiumCheck.checked = false;
    if (activeCheck) activeCheck.checked = true;

    modalEdgeColors = ['#FF0055', '#FF7700', '#FFE600', '#00FF66', '#00E5FF', '#7000FF'];
  }

  // Update slider preview text
  document.getElementById('modalEdgeSpeedText').textContent = `${parseFloat(speedInput.value).toFixed(1)}x`;
  document.getElementById('modalEdgeThicknessText').textContent = `${borderInput.value}px`;
  document.getElementById('modalEdgeRadiusText').textContent = `${radiusInput.value}px`;
  document.getElementById('modalEdgePunchText').textContent = `${punchInput.value}px`;

  renderModalEdgeColors();

  if (modal) modal.classList.add('active');
}

function openEditEdgeModal(id) {
  const found = allEdgePresets.find(p => p.id === id);
  if (found) {
    openAddEdgeModal(found);
  }
}

function renderModalEdgeColors() {
  const container = document.getElementById('modalEdgeColorList');
  if (!container) return;

  container.innerHTML = modalEdgeColors.map((c, idx) => `
    <div class="modal-color-chip">
      <input type="color" value="${c}" onchange="updateModalEdgeColor(${idx}, this.value)">
      <span>${c}</span>
      ${modalEdgeColors.length > 2 ? `<button type="button" onclick="removeModalEdgeColor(${idx})">&times;</button>` : ''}
    </div>
  `).join('');
}

function updateModalEdgeColor(index, newHex) {
  modalEdgeColors[index] = newHex;
  renderModalEdgeColors();
}

function addModalEdgeColor(defaultHex = '#00E5FF') {
  if (modalEdgeColors.length >= 8) {
    showToast('Maximum 8 gradient colors allowed');
    return;
  }
  modalEdgeColors.push(defaultHex);
  renderModalEdgeColors();
}

function removeModalEdgeColor(index) {
  if (modalEdgeColors.length <= 2) {
    showToast('At least 2 colors required for gradient');
    return;
  }
  modalEdgeColors.splice(index, 1);
  renderModalEdgeColors();
}

async function handleEdgeFormSubmit(e) {
  e.preventDefault();

  const id = document.getElementById('edgeIdInput')?.value;
  const title = document.getElementById('edgeTitleInput')?.value;
  const category = document.getElementById('edgeCategorySelect')?.value;
  const animationType = document.getElementById('edgeTypeSelect')?.value;
  const speed = parseFloat(document.getElementById('edgeSpeedInput')?.value || 1.0);
  const borderSize = parseInt(document.getElementById('edgeBorderInput')?.value || 6, 10);
  const cornerRadius = parseInt(document.getElementById('edgeRadiusInput')?.value || 32, 10);
  const punchHoleRadius = parseInt(document.getElementById('edgePunchInput')?.value || 0, 10);
  const sortOrder = parseInt(document.getElementById('edgeSortOrderInput')?.value || 1, 10);
  const isPremium = document.getElementById('edgeIsPremiumCheck')?.checked ? 1 : 0;
  const isActive = document.getElementById('edgeIsActiveCheck')?.checked ? 1 : 0;

  const submitBtn = document.getElementById('saveEdgeSubmitBtn');
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';
  }

  const payload = {
    title,
    category,
    animationType,
    colors: JSON.stringify(modalEdgeColors),
    speed,
    borderSize,
    cornerRadius,
    punchHoleRadius,
    glowSpread: borderSize * 2,
    sortOrder,
    isPremium,
    isActive
  };

  try {
    const url = id ? `/api/admin/edge-lighting/${id}` : '/api/admin/edge-lighting';
    const method = id ? 'PUT' : 'POST';

    const res = await authFetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();

    if (json.success) {
      showToast(json.message || 'Edge lighting preset saved successfully!');
      document.getElementById('edgeLightingModal')?.classList.remove('active');
      await loadEdgeLightingPresets();
      if (json.data) {
        selectEdgePresetForSim(json.data);
      }
    } else {
      showToast('Error: ' + json.error);
    }
  } catch (err) {
    showToast('Network error: ' + err.message);
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = '💾 Save Edge Lighting Preset';
    }
  }
}

async function toggleEdgeActive(id) {
  try {
    const res = await authFetch(`/api/admin/edge-lighting/${id}/toggle`, { method: 'PATCH' });
    const json = await res.json();
    if (json.success) {
      showToast(json.message);
      const found = allEdgePresets.find(p => p.id === id);
      if (found) found.isActive = json.isActive;
      renderEdgeLightingCatalog();
    } else {
      showToast('Error: ' + json.error);
    }
  } catch (err) {
    showToast('Failed to toggle preset status: ' + err.message);
  }
}

// ========================================================
// 🏝️ DYNAMIC ISLAND STUDIO & CAPSULE SIMULATOR CONTROLLER
// ========================================================
let allIslandThemes = [];
let activeIslandSimTheme = null;
let currentIslandSimEvent = 'music';
let islandPunchXOffset = 0;
let islandPunchYOffset = 18;

async function loadDynamicIslandThemes() {
  try {
    const res = await authFetch('/api/admin/dynamic-island/themes');
    const json = await res.json();
    if (json.success) {
      allIslandThemes = json.data || [];

      // Update counters
      const totalEl = document.getElementById('totalIslandThemes');
      const activeEl = document.getElementById('activeIslandThemes');
      const appliedEl = document.getElementById('totalIslandApplied');

      if (totalEl) totalEl.textContent = allIslandThemes.length;
      if (activeEl) activeEl.textContent = allIslandThemes.filter(t => t.isActive).length;
      if (appliedEl) appliedEl.textContent = allIslandThemes.reduce((acc, t) => acc + (t.appliedCount || 0), 0).toLocaleString();

      renderDynamicIslandCatalog();

      // Pick first or previously selected theme
      if (allIslandThemes.length > 0) {
        const themeToSelect = activeIslandSimTheme
          ? (allIslandThemes.find(t => t.id === activeIslandSimTheme.id) || allIslandThemes[0])
          : allIslandThemes[0];
        selectIslandThemeForSim(themeToSelect);
      }
    }
  } catch (err) {
    console.error('Failed to load Dynamic Island themes:', err);
    showToast('Failed to load Dynamic Island themes: ' + err.message);
  }
}

function renderDynamicIslandCatalog() {
  const container = document.getElementById('islandCardsGrid');
  if (!container) return;

  const searchVal = document.getElementById('islandSearchInput')?.value.toLowerCase().trim() || '';
  const styleFilter = document.getElementById('islandStyleFilter')?.value || 'ALL';
  const statusFilter = document.getElementById('islandStatusFilter')?.value || 'ALL';

  const filtered = allIslandThemes.filter(t => {
    const titleStr = t.title || t.name || '';
    const matchesSearch = !searchVal || titleStr.toLowerCase().includes(searchVal) || (t.styleType || '').toLowerCase().includes(searchVal);
    const matchesStyle = styleFilter === 'ALL' || t.styleType === styleFilter;
    const matchesStatus = statusFilter === 'ALL' || (statusFilter === 'ACTIVE' ? t.isActive : !t.isActive);
    return matchesSearch && matchesStyle && matchesStatus;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--text-secondary); background: rgba(255,255,255,0.02); border-radius: 12px;">
        <span style="font-size: 32px; display: block; margin-bottom: 8px;">🏝️</span>
        <strong>No Dynamic Island Themes Found</strong>
        <p style="font-size: 12px; margin-top: 4px;">Try adjusting filters or click "+ Add Island Theme".</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(t => {
    const isSel = activeIslandSimTheme && activeIslandSimTheme.id === t.id;
    const accent = t.accentColor || '#00E5FF';
    const bg = t.backgroundColor || '#0B0E14';
    const glow = t.glowColor || accent;
    const glowSpread = t.glowIntensity || 10;
    const isPrem = Boolean(t.isPremium);
    const title = t.title || t.name || 'Island Theme';
    const downloads = t.downloads || t.appliedCount || 0;
    const radius = t.cornerRadius || t.borderRadius || 20;

    return `
      <div class="island-card ${isSel ? 'active-preview' : ''}" id="island-card-${t.id}" onclick="selectIslandThemeById('${t.id}')" style="cursor:pointer; ${isSel ? `border-color:${accent}; box-shadow: 0 0 24px ${accent}60, 0 8px 24px rgba(0,0,0,0.5);` : ''}">
        <!-- Preview Box -->
        <div class="island-card-preview-box" title="Click to preview on phone simulator">
          ${t.previewUrl ? `
            <img src="${t.previewUrl}" alt="${escapeHtml(title)}" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
            <div class="island-preview-fallback" style="display:none; width: 140px; height: 32px; border-radius: ${radius}px; background: ${bg}; border: 1.5px solid ${accent}; box-shadow: 0 0 ${glowSpread}px ${glow}40; align-items:center; justify-content:space-between; padding:4px 10px; color:#fff;">
              <div style="display:flex; align-items:center; gap:6px;">
                <span style="width:7px; height:7px; border-radius:50%; background:${accent}; box-shadow:0 0 6px ${accent};"></span>
                <span style="font-size:10px; font-weight:700;">${escapeHtml(title)}</span>
              </div>
              <span style="font-size:11px;">⚡</span>
            </div>
          ` : `
            <div style="width: 140px; height: 32px; border-radius: ${radius}px; background: ${bg}; border: 1.5px solid ${accent}; box-shadow: 0 0 ${glowSpread}px ${glow}40; display:flex; align-items:center; justify-content:space-between; padding:4px 10px; color:#fff;">
              <div style="display:flex; align-items:center; gap:6px;">
                <span style="width:7px; height:7px; border-radius:50%; background:${accent}; box-shadow:0 0 6px ${accent};"></span>
                <span style="font-size:10px; font-weight:700;">${escapeHtml(title)}</span>
              </div>
              <span style="font-size:11px;">⚡</span>
            </div>
          `}
          ${isPrem ? '<span style="position:absolute; top:8px; right:8px; font-size:10px; font-weight:800; background:rgba(255,215,0,0.2); color:#FFD700; border:1px solid rgba(255,215,0,0.4); padding:2px 6px; border-radius:4px; z-index:2;">PRO VIP</span>' : ''}
        </div>

        <!-- Body -->
        <div class="island-card-body">
          <div class="island-card-title">${escapeHtml(title)}</div>
          <div class="island-card-meta">
            <span>🎨 ${(t.styleType || 'obsidian').replace('_', ' ').toUpperCase()}</span>
            <span>•</span>
            <span>🔥 ${downloads.toLocaleString()} applied</span>
          </div>

          <div style="display:flex; align-items:center; gap:8px; margin-bottom:12px;">
            <span style="font-size:11px; color:var(--text-secondary);">Accent:</span>
            <span style="width:14px; height:14px; border-radius:50%; background:${accent}; border:1px solid rgba(255,255,255,0.2); box-shadow:0 0 6px ${accent};"></span>
            <span style="font-family:monospace; font-size:11px; color:#fff;">${accent}</span>
          </div>

          <!-- Footer Actions -->
          <div class="island-card-footer">
            <div style="display:flex; gap:6px;">
              <button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); selectIslandThemeById('${t.id}')" title="Preview on phone">
                👁️ Preview
              </button>
              <button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); openEditIslandModal('${t.id}')" title="Edit theme">
                ✏️
              </button>
              <button class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); deleteIslandTheme('${t.id}')" title="Delete theme" style="color:var(--accent-crimson);">
                🗑️
              </button>
            </div>

            <label class="switch" title="Toggle active status" style="margin:0;" onclick="event.stopPropagation()">
              <input type="checkbox" ${t.isActive ? 'checked' : ''} onchange="toggleIslandActive('${t.id}')">
              <span class="slider"></span>
            </label>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function selectIslandThemeById(id) {
  const found = allIslandThemes.find(t => t.id === id);
  if (found) {
    selectIslandThemeForSim(found);
  }
}

function selectIslandThemeForSim(theme) {
  activeIslandSimTheme = theme;

  // Highlight active card
  document.querySelectorAll('.island-card').forEach(c => {
    c.classList.remove('active-preview');
    c.style.borderColor = '';
    c.style.boxShadow = '';
  });
  const card = document.getElementById(`island-card-${theme.id}`);
  if (card) {
    card.classList.add('active-preview');
    const cardAccent = theme.accentColor || '#00E5FF';
    card.style.borderColor = cardAccent;
    card.style.boxShadow = `0 0 24px ${cardAccent}60, 0 8px 24px rgba(0,0,0,0.5)`;
  }

  // Update simulator UI texts
  const titleEl = document.getElementById('simIslandThemeTitle');
  const badgeEl = document.getElementById('simIslandStyleBadge');
  if (titleEl) titleEl.textContent = theme.title || theme.name || 'Dynamic Island';
  if (badgeEl) badgeEl.textContent = (theme.styleType || 'obsidian').replace('_', ' ').toUpperCase();

  // Set default event type based on theme personality
  if (theme.styleType === 'amoled') {
    currentIslandSimEvent = 'charging';
  } else if (theme.styleType === 'glassmorphic') {
    currentIslandSimEvent = 'earbuds';
  } else if (theme.styleType === 'cyberpunk') {
    currentIslandSimEvent = 'music';
  } else if (theme.styleType === 'luxury') {
    currentIslandSimEvent = 'music';
  } else if (theme.styleType === 'gradient') {
    currentIslandSimEvent = 'music';
  } else {
    currentIslandSimEvent = 'music';
  }

  // Update active trigger button highlight
  document.querySelectorAll('.island-event-btn').forEach(b => b.classList.remove('active'));
  const btnMap = {
    compact: 'btnEventCompact',
    charging: 'btnEventCharging',
    music: 'btnEventMusic',
    earbuds: 'btnEventEarbuds',
    call: 'btnEventCall',
    notif: 'btnEventNotif'
  };
  const activeBtn = document.getElementById(btnMap[currentIslandSimEvent]);
  if (activeBtn) activeBtn.classList.add('active');

  // Update wallpaper ambient glow
  const phoneScreen = document.querySelector('.island-phone-screen');
  if (phoneScreen) {
    const accent = theme.accentColor || '#00E5FF';
    const glow = theme.glowColor || accent;
    phoneScreen.style.background = `radial-gradient(ellipse at top, ${glow}40 0%, #06080e 70%)`;
  }

  updateIslandSimulatorCapsule();
}

function updateIslandSimulatorCapsule() {
  if (!activeIslandSimTheme) return;

  const capsule = document.getElementById('simIslandCapsule');
  if (!capsule) return;

  const t = activeIslandSimTheme;
  const accent = t.accentColor || '#00E5FF';
  const bg = t.backgroundColor || '#0B0E14';
  const textColor = t.textColor || '#FFFFFF';
  const glow = t.glowColor || accent;
  const glowSpread = t.glowIntensity || 16;
  const radius = t.cornerRadius || t.borderRadius || 22;
  const styleType = t.styleType || 'minimal';

  // Base Reset
  capsule.style.backdropFilter = 'none';
  capsule.style.webkitBackdropFilter = 'none';

  if (styleType === 'minimal') {
    capsule.style.background = '#000000';
    capsule.style.borderColor = 'rgba(255, 255, 255, 0.22)';
    capsule.style.boxShadow = '0 6px 24px rgba(0, 0, 0, 0.95), 0 0 10px rgba(0, 229, 255, 0.2)';
  } else if (styleType === 'cyberpunk') {
    capsule.style.background = '#080C16';
    capsule.style.borderColor = '#00E5FF';
    capsule.style.boxShadow = `0 4px 20px rgba(0, 0, 0, 0.7), 0 0 22px rgba(0, 229, 255, 0.8), 0 0 35px rgba(112, 0, 255, 0.4)`;
  } else if (styleType === 'glassmorphic') {
    capsule.style.background = 'rgba(20, 30, 45, 0.65)';
    capsule.style.backdropFilter = 'blur(20px)';
    capsule.style.webkitBackdropFilter = 'blur(20px)';
    capsule.style.borderColor = '#00FF88';
    capsule.style.boxShadow = `0 8px 32px rgba(0, 0, 0, 0.5), 0 0 20px rgba(0, 255, 136, 0.6)`;
  } else if (styleType === 'amoled') {
    capsule.style.background = '#000000';
    capsule.style.borderColor = '#FFFFFF';
    capsule.style.boxShadow = '0 6px 24px rgba(0, 0, 0, 0.98), 0 0 15px rgba(255, 255, 255, 0.35)';
  } else if (styleType === 'gradient') {
    capsule.style.background = '#1A1428';
    capsule.style.borderColor = '#FF007F';
    capsule.style.boxShadow = `0 4px 20px rgba(0, 0, 0, 0.7), 0 0 22px rgba(255, 0, 127, 0.7), 0 0 35px rgba(0, 229, 255, 0.3)`;
  } else if (styleType === 'luxury') {
    capsule.style.background = '#120F08';
    capsule.style.borderColor = '#FFD700';
    capsule.style.boxShadow = `0 4px 22px rgba(0, 0, 0, 0.85), 0 0 24px rgba(255, 215, 0, 0.7), 0 0 40px rgba(255, 165, 0, 0.35)`;
  } else {
    capsule.style.background = bg;
    capsule.style.borderColor = accent;
    capsule.style.boxShadow = `0 4px 20px rgba(0, 0, 0, 0.6), 0 0 ${glowSpread}px ${glow}`;
  }

  capsule.style.color = textColor;
  capsule.style.borderRadius = `${radius}px`;

  // Update status bar opacity
  const statusBar = document.querySelector('.island-phone-screen .sim-status-bar');
  if (statusBar) {
    statusBar.style.transition = 'opacity 0.25s ease';
    statusBar.style.opacity = currentIslandSimEvent === 'compact' ? '1' : '0';
  }

  // Render internal event state with theme context
  renderCapsuleContent(capsule, currentIslandSimEvent, accent, textColor, styleType, t.title);
}

function renderCapsuleContent(capsule, eventType, accent, textColor, styleType, themeTitle) {
  capsule.className = `dynamic-island-capsule state-${eventType}`;
  styleType = styleType || (activeIslandSimTheme ? activeIslandSimTheme.styleType : 'minimal');
  accent = accent || (activeIslandSimTheme ? activeIslandSimTheme.accentColor : '#00E5FF');

  if (eventType === 'compact') {
    let icon = '⚡';
    let badge = '88%';
    if (styleType === 'luxury') { icon = '👑'; badge = '100%'; }
    else if (styleType === 'cyberpunk') { icon = '⚡'; badge = '99%'; }
    else if (styleType === 'glassmorphic') { icon = '💎'; badge = '92%'; }
    else if (styleType === 'amoled') { icon = '🔋'; badge = '100%'; }
    else if (styleType === 'gradient') { icon = '🌸'; badge = '95%'; }

    capsule.innerHTML = `
      <div style="display:flex; align-items:center; gap:5px; padding-left:2px;">
        <span style="font-size:10px;">${icon}</span>
        <span style="font-size:10px; font-weight:800; color:${accent};">${badge}</span>
      </div>
      <div class="island-eq-bars" style="margin-right:2px;">
        <div class="island-wave-bar" style="background:${accent};"></div>
        <div class="island-wave-bar" style="background:${accent};"></div>
        <div class="island-wave-bar" style="background:${accent};"></div>
      </div>
    `;
  } else if (eventType === 'charging') {
    let tag = '66W SuperDart';
    let sub = 'Fast Charging Active';
    let icon = '⚡';
    if (styleType === 'amoled') { tag = 'Pure Void Zero-Drain'; sub = 'AMOLED Battery Void'; icon = '🔋'; }
    else if (styleType === 'cyberpunk') { tag = '120W Cyber Arc Core'; sub = 'Hyper-Drive Matrix'; icon = '⚡'; }
    else if (styleType === 'luxury') { tag = '80W VIP Sovereign'; sub = 'Imperial Gold Charging'; icon = '👑'; }
    else if (styleType === 'glassmorphic') { tag = '50W Aero Glass'; sub = 'Qi2 Wireless Float'; icon = '💎'; }
    else if (styleType === 'gradient') { tag = '66W Aurora Turbo'; sub = 'Sunset Aurora Wave'; icon = '🌸'; }

    capsule.innerHTML = `
      <div style="display:flex; align-items:center; gap:8px;">
        <div style="width:28px; height:28px; border-radius:50%; background:${accent}25; display:flex; align-items:center; justify-content:center; color:${accent}; font-size:14px; box-shadow:0 0 10px ${accent}40;">
          ${icon}
        </div>
        <div style="display:flex; flex-direction:column; gap:1px;">
          <span style="font-size:11px; font-weight:800; color:#fff;">${tag}</span>
          <span style="font-size:9px; color:${accent}; font-weight:600;">${sub}</span>
        </div>
      </div>
      <div style="display:flex; align-items:center; gap:6px;">
        <div style="width:46px; height:6px; background:rgba(255,255,255,0.12); border-radius:4px; overflow:hidden;">
          <div style="width:88%; height:100%; background:${accent}; border-radius:4px; box-shadow:0 0 8px ${accent};"></div>
        </div>
        <span style="font-size:12px; font-weight:900; color:${accent};">88%</span>
      </div>
    `;
  } else if (eventType === 'music') {
    let song = 'Starboy';
    let artist = 'The Weeknd';
    let icon = '🎵';
    let bgGradient = 'linear-gradient(135deg, #1e1b4b, #3b0764)';

    if (styleType === 'luxury') {
      song = 'Sovereign Master';
      artist = 'Lossless 24-bit Gold';
      icon = '👑';
      bgGradient = 'linear-gradient(135deg, #78350f, #f59e0b)';
    } else if (styleType === 'cyberpunk') {
      song = 'Cyber Arc Matrix';
      artist = 'Hyper • Spoiler';
      icon = '⚡';
      bgGradient = 'linear-gradient(135deg, #0e7490, #6d28d9)';
    } else if (styleType === 'glassmorphic') {
      song = 'Aero Spatial Sound';
      artist = 'Dolby Atmos Spatial';
      icon = '💎';
      bgGradient = 'linear-gradient(135deg, #064e3b, #059669)';
    } else if (styleType === 'amoled') {
      song = 'Pure Void Audio';
      artist = 'OLED High Fidelity';
      icon = '🖤';
      bgGradient = 'linear-gradient(135deg, #18181b, #27272a)';
    } else if (styleType === 'gradient') {
      song = 'Aurora Cloud Sunset';
      artist = 'Lofi Dreams & Waves';
      icon = '🌸';
      bgGradient = 'linear-gradient(135deg, #831843, #be185d)';
    }

    capsule.innerHTML = `
      <div style="display:flex; align-items:center; gap:8px;">
        <div style="width:34px; height:34px; border-radius:8px; background:${bgGradient}; display:flex; align-items:center; justify-content:center; font-size:16px; box-shadow:0 2px 10px rgba(0,0,0,0.5);">
          ${icon}
        </div>
        <div style="display:flex; flex-direction:column; gap:1px; max-width:98px; overflow:hidden;">
          <span style="font-size:11px; font-weight:800; color:#fff; white-space:nowrap; text-overflow:ellipsis; overflow:hidden;">${song}</span>
          <span style="font-size:9px; color:rgba(255,255,255,0.7); white-space:nowrap; text-overflow:ellipsis; overflow:hidden;">${artist}</span>
        </div>
      </div>
      <div style="display:flex; align-items:center; gap:8px;">
        <div class="island-eq-bars">
          <div class="island-wave-bar" style="background:${accent};"></div>
          <div class="island-wave-bar" style="background:${accent};"></div>
          <div class="island-wave-bar" style="background:${accent};"></div>
          <div class="island-wave-bar" style="background:${accent};"></div>
        </div>
        <span style="font-size:13px; cursor:pointer; color:#fff;" title="Play/Pause">⏸️</span>
      </div>
    `;
  } else if (eventType === 'earbuds') {
    let budsName = 'Buds Pro 3';
    let icon = '🎧';
    if (styleType === 'luxury') { budsName = 'AirPods Max Gold'; icon = '👑'; }
    else if (styleType === 'cyberpunk') { budsName = 'Cyber Arc Buds'; icon = '⚡'; }
    else if (styleType === 'glassmorphic') { budsName = 'Glass Aero Pods'; icon = '💎'; }
    else if (styleType === 'amoled') { budsName = 'Void Zero Buds'; icon = '🖤'; }
    else if (styleType === 'gradient') { budsName = 'Aurora Pastel Buds'; icon = '🌸'; }

    capsule.innerHTML = `
      <div style="display:flex; align-items:center; gap:8px;">
        <span style="font-size:18px;">${icon}</span>
        <div style="display:flex; flex-direction:column; gap:1px;">
          <span style="font-size:11px; font-weight:800; color:#fff;">${budsName}</span>
          <span style="font-size:9px; color:${accent}; font-weight:600;">Spatial Connected</span>
        </div>
      </div>
      <div style="display:flex; gap:4px; font-size:9.5px; font-weight:700; font-family:monospace;">
        <span style="background:rgba(255,255,255,0.08); padding:2px 5px; border-radius:4px; color:#fff;">L 95%</span>
        <span style="background:rgba(255,255,255,0.08); padding:2px 5px; border-radius:4px; color:#fff;">R 92%</span>
      </div>
    `;
  } else if (eventType === 'call') {
    capsule.innerHTML = `
      <div style="display:flex; align-items:center; gap:8px;">
        <div style="width:28px; height:28px; border-radius:50%; background:${accent}; display:flex; align-items:center; justify-content:center; color:#000; font-size:11px; font-weight:800;">
          EM
        </div>
        <div style="display:flex; flex-direction:column; gap:1px;">
          <span style="font-size:11px; font-weight:800; color:#fff;">Elon Musk</span>
          <span style="font-size:9px; color:#22c55e; font-weight:600;">Incoming Call...</span>
        </div>
      </div>
      <div style="display:flex; align-items:center; gap:6px;">
        <button type="button" style="width:24px; height:24px; border-radius:50%; background:#ef4444; border:none; color:#fff; font-size:10px; cursor:pointer; display:flex; align-items:center; justify-content:center;" title="Decline">✕</button>
        <button type="button" style="width:24px; height:24px; border-radius:50%; background:#22c55e; border:none; color:#fff; font-size:10px; cursor:pointer; display:flex; align-items:center; justify-content:center;" title="Accept">📞</button>
      </div>
    `;
  } else if (eventType === 'notif') {
    capsule.innerHTML = `
      <div style="display:flex; align-items:center; gap:8px;">
        <div style="width:28px; height:28px; border-radius:8px; background:#25D366; display:flex; align-items:center; justify-content:center; color:#fff; font-size:14px; box-shadow:0 2px 8px rgba(37,211,102,0.4);">
          💬
        </div>
        <div style="display:flex; flex-direction:column; gap:1px; max-width:135px; overflow:hidden;">
          <span style="font-size:11px; font-weight:800; color:#fff; white-space:nowrap; text-overflow:ellipsis; overflow:hidden;">WhatsApp Alert</span>
          <span style="font-size:9px; color:rgba(255,255,255,0.7); white-space:nowrap; text-overflow:ellipsis; overflow:hidden;">Hey, check out this 3D wallpaper!</span>
        </div>
      </div>
      <span style="font-size:10px; color:${accent}; font-weight:700;">now</span>
    `;
  }
}

function setIslandSimEvent(mode) {
  currentIslandSimEvent = mode;
  document.querySelectorAll('.island-event-btn').forEach(b => b.classList.remove('active'));

  const btnMap = {
    compact: 'btnEventCompact',
    charging: 'btnEventCharging',
    music: 'btnEventMusic',
    earbuds: 'btnEventEarbuds',
    call: 'btnEventCall',
    notif: 'btnEventNotif'
  };

  const activeBtn = document.getElementById(btnMap[mode]);
  if (activeBtn) activeBtn.classList.add('active');

  updateIslandSimulatorCapsule();
}

function setIslandAlignment(align) {
  const xSlider = document.getElementById('simIslandPunchXSlider');
  let x = 0;
  if (align === 'left') x = -65;
  else if (align === 'center') x = 0;
  else if (align === 'right') x = 65;

  if (xSlider) xSlider.value = x;
  updateIslandPosition(x, islandPunchYOffset);

  ['btnIslandAlignLeft', 'btnIslandAlignCenter', 'btnIslandAlignRight'].forEach(id => {
    const b = document.getElementById(id);
    if (b) {
      b.classList.remove('active');
      b.style.background = '';
      b.style.color = '';
      b.style.fontWeight = '';
    }
  });

  const activeId = align === 'left' ? 'btnIslandAlignLeft' : (align === 'right' ? 'btnIslandAlignRight' : 'btnIslandAlignCenter');
  const activeBtn = document.getElementById(activeId);
  if (activeBtn) {
    activeBtn.classList.add('active');
    activeBtn.style.background = 'var(--accent-cyan)';
    activeBtn.style.color = '#000';
    activeBtn.style.fontWeight = '700';
  }
}

function updateIslandPosition(xOffsetPx, yOffsetPx) {
  islandPunchXOffset = xOffsetPx;
  islandPunchYOffset = yOffsetPx;

  const punchHole = document.getElementById('simIslandPunchHole');
  const capsule = document.getElementById('simIslandCapsule');

  if (punchHole) {
    punchHole.style.left = `calc(50% + ${xOffsetPx}px)`;
    punchHole.style.top = `${yOffsetPx}px`;
  }

  if (capsule) {
    capsule.style.left = `calc(50% + ${xOffsetPx}px)`;
    capsule.style.top = `${yOffsetPx - 4}px`;
  }

  // Update slider displays
  const xVal = document.getElementById('simIslandPunchXVal');
  const yVal = document.getElementById('simIslandPunchYVal');
  if (xVal) xVal.textContent = `${xOffsetPx} px`;
  if (yVal) yVal.textContent = `${yOffsetPx} px`;
}

function openAddIslandModal(themeToEdit = null) {
  const modal = document.getElementById('islandThemeModal');
  const titleEl = document.getElementById('modalIslandTitle');
  const idInput = document.getElementById('islandThemeId');
  const nameInput = document.getElementById('islandThemeName');
  const styleSelect = document.getElementById('islandStyleType');
  const accentInput = document.getElementById('islandAccentColor');
  const accentPicker = document.getElementById('islandAccentColorPicker');
  const bgInput = document.getElementById('islandBgColor');
  const bgPicker = document.getElementById('islandBgColorPicker');
  const textInput = document.getElementById('islandTextColor');
  const textPicker = document.getElementById('islandTextColorPicker');
  const glowInput = document.getElementById('islandGlowColor');
  const glowPicker = document.getElementById('islandGlowColorPicker');
  const glowSpreadInput = document.getElementById('islandGlowIntensity');
  const radiusInput = document.getElementById('islandBorderRadius');
  const sortInput = document.getElementById('islandSortOrder');
  const premiumCheck = document.getElementById('islandIsPremium');
  const activeCheck = document.getElementById('islandIsActive');

  if (themeToEdit) {
    if (titleEl) titleEl.textContent = '✏️ Edit Dynamic Island Theme';
    if (idInput) idInput.value = themeToEdit.id;
    if (nameInput) nameInput.value = themeToEdit.title || themeToEdit.name || '';
    if (styleSelect) styleSelect.value = themeToEdit.styleType || 'obsidian_black';

    const accent = themeToEdit.accentColor || '#00E5FF';
    const bg = themeToEdit.backgroundColor || '#0B0E14';
    const textColor = themeToEdit.textColor || '#FFFFFF';
    const glow = themeToEdit.glowColor || accent;

    if (accentInput) accentInput.value = accent;
    if (accentPicker) accentPicker.value = accent;
    if (bgInput) bgInput.value = bg;
    if (bgPicker) bgPicker.value = bg;
    if (textInput) textInput.value = textColor;
    if (textPicker) textPicker.value = textColor;
    if (glowInput) glowInput.value = glow;
    if (glowPicker) glowPicker.value = glow;

    if (glowSpreadInput) glowSpreadInput.value = themeToEdit.glowIntensity || 12;
    if (radiusInput) radiusInput.value = themeToEdit.cornerRadius || themeToEdit.borderRadius || 28;
    if (sortInput) sortInput.value = themeToEdit.sortOrder || 1;
    if (premiumCheck) premiumCheck.checked = Boolean(themeToEdit.isPremium);
    if (activeCheck) activeCheck.checked = Boolean(themeToEdit.isActive);
  } else {
    if (titleEl) titleEl.textContent = '🏝️ Add Dynamic Island Theme';
    if (idInput) idInput.value = '';
    if (nameInput) nameInput.value = 'Cyberpunk Neon Capsule';
    if (styleSelect) styleSelect.value = 'obsidian_black';

    if (accentInput) accentInput.value = '#00E5FF';
    if (accentPicker) accentPicker.value = '#00E5FF';
    if (bgInput) bgInput.value = '#0B0E14';
    if (bgPicker) bgPicker.value = '#0B0E14';
    if (textInput) textInput.value = '#FFFFFF';
    if (textPicker) textPicker.value = '#FFFFFF';
    if (glowInput) glowInput.value = '#00E5FF';
    if (glowPicker) glowPicker.value = '#00E5FF';

    if (glowSpreadInput) glowSpreadInput.value = 12;
    if (radiusInput) radiusInput.value = 28;
    if (sortInput) sortInput.value = allIslandThemes.length + 1;
    if (premiumCheck) premiumCheck.checked = false;
    if (activeCheck) activeCheck.checked = true;
  }

  // Update slider preview numbers
  if (glowSpreadInput && document.getElementById('modalIslandGlowText')) {
    document.getElementById('modalIslandGlowText').textContent = `${glowSpreadInput.value}px`;
  }
  if (radiusInput && document.getElementById('modalIslandRadiusText')) {
    document.getElementById('modalIslandRadiusText').textContent = `${radiusInput.value}px`;
  }

  if (modal) modal.classList.add('active');
}

function openEditIslandModal(id) {
  const found = allIslandThemes.find(t => t.id === id);
  if (found) {
    openAddIslandModal(found);
  }
}

async function handleIslandFormSubmit(e) {
  e.preventDefault();

  const id = document.getElementById('islandThemeId')?.value;
  const name = document.getElementById('islandThemeName')?.value;
  const styleType = document.getElementById('islandStyleType')?.value;
  const accentColor = document.getElementById('islandAccentColor')?.value;
  const backgroundColor = document.getElementById('islandBgColor')?.value;
  const textColor = document.getElementById('islandTextColor')?.value;
  const glowColor = document.getElementById('islandGlowColor')?.value;
  const glowIntensity = parseInt(document.getElementById('islandGlowIntensity')?.value || '12', 10);
  const cornerRadius = parseInt(document.getElementById('islandBorderRadius')?.value || '28', 10);
  const sortOrder = parseInt(document.getElementById('islandSortOrder')?.value || '1', 10);
  const isPremium = document.getElementById('islandIsPremium')?.checked ? 1 : 0;
  const isActive = document.getElementById('islandIsActive')?.checked ? 1 : 0;

  if (!name) {
    showToast('Theme title is required');
    return;
  }

  const submitBtn = document.getElementById('saveIslandSubmitBtn');
  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';
  }

  const payload = {
    title: name,
    name,
    styleType,
    accentColor,
    backgroundColor,
    textColor,
    glowColor,
    glowIntensity,
    cornerRadius,
    sortOrder,
    isPremium,
    isActive
  };

  const isEdit = Boolean(id);
  const url = isEdit ? `/api/admin/dynamic-island/themes/${id}` : '/api/admin/dynamic-island/themes';
  const method = isEdit ? 'PUT' : 'POST';

  try {
    const res = await authFetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();

    if (json.success) {
      showToast(json.message || 'Dynamic Island theme saved successfully!');
      document.getElementById('islandThemeModal')?.classList.remove('active');
      await loadDynamicIslandThemes();
      if (json.data) {
        selectIslandThemeForSim(json.data);
      }
    } else {
      showToast('Error: ' + (json.error || 'Failed to save theme'));
    }
  } catch (err) {
    showToast('Server error: ' + err.message);
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = '💾 Save Dynamic Island Theme';
    }
  }
}

async function toggleIslandActive(id) {
  try {
    const res = await authFetch(`/api/admin/dynamic-island/themes/${id}/toggle`, { method: 'PATCH' });
    const json = await res.json();
    if (json.success) {
      showToast('Theme status updated');
      const theme = allIslandThemes.find(t => t.id === id);
      if (theme) theme.isActive = json.isActive;
      renderDynamicIslandCatalog();
    } else {
      showToast('Failed to toggle status: ' + json.error);
    }
  } catch (err) {
    showToast('Failed to toggle status: ' + err.message);
  }
}

async function deleteIslandTheme(id) {
  if (!confirm('Are you sure you want to delete this Dynamic Island theme?')) return;
  try {
    const res = await authFetch(`/api/admin/dynamic-island/themes/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Theme deleted successfully');
      allIslandThemes = allIslandThemes.filter(t => t.id !== id);
      renderDynamicIslandCatalog();
      if (activeIslandSimTheme && activeIslandSimTheme.id === id) {
        activeIslandSimTheme = allIslandThemes[0] || null;
        if (activeIslandSimTheme) selectIslandThemeForSim(activeIslandSimTheme);
      }
    } else {
      showToast('Error: ' + json.error);
    }
  } catch (err) {
    showToast('Failed to delete theme: ' + err.message);
  }
}

function initDynamicIslandStudio() {
  const addBtn = document.getElementById('openAddIslandModalBtn');
  const refreshBtn = document.getElementById('refreshIslandBtn');
  const closeBtn = document.getElementById('closeIslandModal');
  const cancelBtn = document.getElementById('cancelIslandModalBtn');
  const form = document.getElementById('islandThemeForm');

  if (addBtn) addBtn.addEventListener('click', () => openAddIslandModal());
  if (refreshBtn) refreshBtn.addEventListener('click', () => {
    loadDynamicIslandThemes();
    showToast('Dynamic Island themes refreshed');
  });

  [closeBtn, cancelBtn].forEach(btn => {
    if (btn) btn.addEventListener('click', () => {
      document.getElementById('islandThemeModal')?.classList.remove('active');
    });
  });

  if (form) form.addEventListener('submit', handleIslandFormSubmit);

  // Search and Filter Listeners
  document.getElementById('islandSearchInput')?.addEventListener('input', renderDynamicIslandCatalog);
  document.getElementById('islandStyleFilter')?.addEventListener('change', renderDynamicIslandCatalog);
  document.getElementById('islandStatusFilter')?.addEventListener('change', renderDynamicIslandCatalog);

  // Event simulator buttons
  document.getElementById('btnEventCompact')?.addEventListener('click', () => setIslandSimEvent('compact'));
  document.getElementById('btnEventCharging')?.addEventListener('click', () => setIslandSimEvent('charging'));
  document.getElementById('btnEventMusic')?.addEventListener('click', () => setIslandSimEvent('music'));
  document.getElementById('btnEventEarbuds')?.addEventListener('click', () => setIslandSimEvent('earbuds'));
  document.getElementById('btnEventCall')?.addEventListener('click', () => setIslandSimEvent('call'));
  document.getElementById('btnEventNotif')?.addEventListener('click', () => setIslandSimEvent('notif'));

  // Interactive tap on capsule toggles between compact & expanded
  document.getElementById('simIslandCapsule')?.addEventListener('click', () => {
    if (currentIslandSimEvent === 'compact') {
      setIslandSimEvent('music');
    } else {
      setIslandSimEvent('compact');
    }
  });

  // Punch Hole Calibration Sliders
  const xSlider = document.getElementById('simIslandPunchXSlider');
  const ySlider = document.getElementById('simIslandPunchYSlider');
  const radiusSlider = document.getElementById('simIslandRadiusSlider');

  if (xSlider) {
    xSlider.addEventListener('input', (e) => {
      updateIslandPosition(parseInt(e.target.value, 10), islandPunchYOffset);
    });
  }

  if (ySlider) {
    ySlider.addEventListener('input', (e) => {
      updateIslandPosition(islandPunchXOffset, parseInt(e.target.value, 10));
    });
  }

  if (radiusSlider) {
    radiusSlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      const radiusVal = document.getElementById('simIslandRadiusVal');
      if (radiusVal) radiusVal.textContent = `${val} px`;
      const capsule = document.getElementById('simIslandCapsule');
      if (capsule) capsule.style.borderRadius = `${val}px`;
    });
  }

  // Preset alignment buttons (Left hole, Center hole, Right hole)
  document.getElementById('btnIslandAlignLeft')?.addEventListener('click', () => setIslandAlignment('left'));
  document.getElementById('btnIslandAlignCenter')?.addEventListener('click', () => setIslandAlignment('center'));
  document.getElementById('btnIslandAlignRight')?.addEventListener('click', () => setIslandAlignment('right'));

  // Modal color picker syncs
  const syncPicker = (pickerId, textId) => {
    const p = document.getElementById(pickerId);
    const t = document.getElementById(textId);
    if (p && t) {
      p.addEventListener('input', () => { t.value = p.value.toUpperCase(); });
      t.addEventListener('input', () => {
        if (/^#[0-9A-F]{6}$/i.test(t.value)) p.value = t.value;
      });
    }
  };
  syncPicker('islandAccentColorPicker', 'islandAccentColor');
  syncPicker('islandBgColorPicker', 'islandBgColor');
  syncPicker('islandTextColorPicker', 'islandTextColor');
  syncPicker('islandGlowColorPicker', 'islandGlowColor');

  // Modal slider number displays
  document.getElementById('islandGlowIntensity')?.addEventListener('input', (e) => {
    document.getElementById('modalIslandGlowText').textContent = `${e.target.value}px`;
  });
  document.getElementById('islandBorderRadius')?.addEventListener('input', (e) => {
    document.getElementById('modalIslandRadiusText').textContent = `${e.target.value}px`;
  });

  // Initial position for simulator
  updateIslandPosition(0, 14);
}

// Global window exposure for HTML event handlers
window.openAddIslandModal = openAddIslandModal;
window.openEditIslandModal = openEditIslandModal;
window.deleteIslandTheme = deleteIslandTheme;
window.toggleIslandActive = toggleIslandActive;
window.selectIslandThemeById = selectIslandThemeById;
window.setIslandSimEvent = setIslandSimEvent;
window.setIslandAlignment = setIslandAlignment;
window.updateIslandPosition = updateIslandPosition;
window.loadDynamicIslandThemes = loadDynamicIslandThemes;

// ========================================================
// 🕒 ALWAYS-ON DISPLAY (AOD) CLOCKS & WIDGETS CONTROLLER
// ========================================================
let allAodClocks = [];
let activeAodSimClock = null;
let aodBurnInShiftStep = 0;
let aodBrightness = 80;
let aodSimWidgets = { battery: true, date: true, steps: true, weather: true };
let aodClockTimer = null;

async function loadAODClocks() {
  try {
    const res = await authFetch('/api/admin/aod/clocks');
    const json = await res.json();
    if (json.success) {
      allAodClocks = json.data || [];

      // Update counters
      const totalEl = document.getElementById('totalAodClocks');
      const activeEl = document.getElementById('activeAodClocks');
      const appliedEl = document.getElementById('totalAodApplied');

      if (totalEl) totalEl.textContent = allAodClocks.length;
      if (activeEl) activeEl.textContent = allAodClocks.filter(c => c.isActive).length;
      if (appliedEl) appliedEl.textContent = allAodClocks.reduce((acc, c) => acc + (c.downloads || 0), 0).toLocaleString();

      renderAODCatalog();

      // Pick first or previously selected clock face
      if (allAodClocks.length > 0) {
        const clockToSelect = activeAodSimClock
          ? (allAodClocks.find(c => c.id === activeAodSimClock.id) || allAodClocks[0])
          : allAodClocks[0];
        selectAODClockForSim(clockToSelect);
      }
    }
  } catch (err) {
    console.error('Failed to load AOD clocks:', err);
    showToast('Failed to load AOD clocks: ' + err.message);
  }
}

function renderAODCatalog() {
  const container = document.getElementById('aodCardsGrid');
  if (!container) return;

  const searchVal = document.getElementById('aodSearchInput')?.value.toLowerCase().trim() || '';
  const styleFilter = document.getElementById('aodStyleFilter')?.value || 'ALL';
  const statusFilter = document.getElementById('aodStatusFilter')?.value || 'ALL';

  const filtered = allAodClocks.filter(c => {
    const titleStr = c.title || '';
    const matchesSearch = !searchVal || titleStr.toLowerCase().includes(searchVal) || (c.clockType || '').toLowerCase().includes(searchVal) || (c.dialStyle || '').toLowerCase().includes(searchVal);
    const matchesStyle = styleFilter === 'ALL' || c.clockType === styleFilter;
    const matchesStatus = statusFilter === 'ALL' || (statusFilter === 'ACTIVE' ? c.isActive : !c.isActive);
    return matchesSearch && matchesStyle && matchesStatus;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--text-secondary); background: rgba(255,255,255,0.02); border-radius: 12px;">
        <span style="font-size: 32px; display: block; margin-bottom: 8px;">🕒</span>
        <strong>No AOD Clock Faces Found</strong>
        <p style="font-size: 12px; margin-top: 4px;">Try adjusting filters or click "+ Add AOD Clock".</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(c => {
    const isSel = activeAodSimClock && activeAodSimClock.id === c.id;
    const accent = c.accentColor || '#00E5FF';
    const glow = c.glowColor || accent;
    const isPrem = Boolean(c.isPremium);
    const title = c.title || 'AOD Clock Face';
    const downloads = c.downloads || 0;
    const preview = c.previewUrl || '/uploads/aod/aod_cyberpunk_2077.svg';

    return `
      <div class="aod-card ${isSel ? 'active-preview' : ''}" id="aod-card-${c.id}">
        <!-- Preview Box -->
        <div class="aod-card-preview-box" onclick="selectAODClockById('${c.id}')" style="cursor:pointer;" title="Click to preview on AMOLED simulator">
          <img src="${preview}" alt="${escapeHtml(title)}" loading="lazy" style="box-shadow: 0 0 16px ${glow}40;">
          ${isPrem ? '<span style="position:absolute; top:8px; right:8px; font-size:10px; font-weight:800; background:rgba(255,215,0,0.2); color:#FFD700; border:1px solid rgba(255,215,0,0.4); padding:2px 6px; border-radius:4px;">PRO VIP</span>' : ''}
          <span style="position:absolute; bottom:8px; left:8px; font-size:9.5px; font-weight:700; background:rgba(0,0,0,0.7); color:#22C55E; padding:2px 6px; border-radius:4px; border:1px solid rgba(34,197,94,0.3);">0W AMOLED</span>
        </div>

        <!-- Body -->
        <div class="aod-card-body">
          <div class="aod-card-title">${escapeHtml(title)}</div>
          <div class="aod-card-meta">
            <span>🎨 ${(c.clockType || 'digital').replace('_', ' ').toUpperCase()}</span>
            <span>•</span>
            <span>🔥 ${downloads.toLocaleString()} applied</span>
          </div>

          <div style="display:flex; align-items:center; gap:8px; margin-bottom:12px;">
            <span style="font-size:11px; color:var(--text-secondary);">Accent:</span>
            <span style="width:14px; height:14px; border-radius:50%; background:${accent}; border:1px solid rgba(255,255,255,0.2); box-shadow:0 0 6px ${accent};"></span>
            <span style="font-family:monospace; font-size:11px; color:#fff;">${accent}</span>
          </div>

          <!-- Footer Actions -->
          <div class="aod-card-footer">
            <div style="display:flex; gap:6px; align-items:center;">
              <button class="btn btn-sm btn-adb-push" onclick="pushToDevice('aod', '${c.id}', event)" title="1-Click Live Test on connected Phone (I2212)" style="font-size:11px;">
                📲 Push
              </button>
              <button class="btn btn-secondary btn-sm" onclick="selectAODClockById('${c.id}')" title="Preview on AMOLED phone">
                👁️ Preview
              </button>
              <button class="btn btn-secondary btn-sm" onclick="openEditAodModal('${c.id}')" title="Edit clock face">
                ✏️
              </button>
              <button class="btn btn-secondary btn-sm" onclick="deleteAodClock('${c.id}')" title="Delete clock face" style="color:var(--accent-crimson);">
                🗑️
              </button>
            </div>

            <label class="switch" title="Toggle active status" style="margin:0;">
              <input type="checkbox" ${c.isActive ? 'checked' : ''} onchange="toggleAodActive('${c.id}')">
              <span class="slider"></span>
            </label>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function selectAODClockById(id) {
  const found = allAodClocks.find(c => c.id === id);
  if (found) {
    selectAODClockForSim(found);
  }
}

function selectAODClockForSim(clock) {
  activeAodSimClock = clock;

  // Highlight active card
  document.querySelectorAll('.aod-card').forEach(c => c.classList.remove('active-preview'));
  const card = document.getElementById(`aod-card-${clock.id}`);
  if (card) card.classList.add('active-preview');

  // Update simulator badge
  const badgeEl = document.getElementById('simAodStyleBadge');
  if (badgeEl) {
    badgeEl.textContent = (clock.clockType || 'digital').replace('_', ' ').toUpperCase();
    badgeEl.style.background = clock.accentColor || '#FFD700';
    badgeEl.style.color = '#000';
  }

  // Set initial widget checkboxes according to clock defaults
  if (document.getElementById('chkSimBattery')) document.getElementById('chkSimBattery').checked = Boolean(clock.hasBatteryWidget);
  if (document.getElementById('chkSimDate')) document.getElementById('chkSimDate').checked = Boolean(clock.hasDateWidget);
  if (document.getElementById('chkSimSteps')) document.getElementById('chkSimSteps').checked = Boolean(clock.hasStepsWidget);
  if (document.getElementById('chkSimWeather')) document.getElementById('chkSimWeather').checked = Boolean(clock.hasWeatherWidget);

  aodSimWidgets.battery = Boolean(clock.hasBatteryWidget);
  aodSimWidgets.date = Boolean(clock.hasDateWidget);
  aodSimWidgets.steps = Boolean(clock.hasStepsWidget);
  aodSimWidgets.weather = Boolean(clock.hasWeatherWidget);

  updateAODSimulatorClock();
}

function updateAODSimulatorClock() {
  if (!activeAodSimClock) return;

  const stage = document.getElementById('simAodClockStage');
  if (!stage) return;

  const c = activeAodSimClock;
  const accent = c.accentColor || '#00E5FF';
  const glow = c.glowColor || accent;
  const text = c.textColor || '#FFFFFF';
  const type = c.clockType || 'cyberpunk_digital';

  const now = new Date();
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const seconds = String(now.getSeconds()).padStart(2, '0');

  if (type === 'cyberpunk_digital') {
    stage.innerHTML = `
      <div style="position:relative; padding:10px 20px;">
        <div style="position:absolute; top:0; left:0; width:16px; height:16px; border-top:2px solid ${accent}; border-left:2px solid ${accent};"></div>
        <div style="position:absolute; top:0; right:0; width:16px; height:16px; border-top:2px solid ${accent}; border-right:2px solid ${accent};"></div>
        <div style="position:absolute; bottom:0; left:0; width:16px; height:16px; border-bottom:2px solid ${glow}; border-left:2px solid ${glow};"></div>
        <div style="position:absolute; bottom:0; right:0; width:16px; height:16px; border-bottom:2px solid ${glow}; border-right:2px solid ${glow};"></div>

        <div class="aod-digital-time" style="color:${text}; text-shadow:0 0 16px ${glow};">
          ${hours}:${minutes}
        </div>
        <div class="aod-digital-seconds" style="color:${accent}; text-align:center;">
          SEC : ${seconds} // HUD 2077
        </div>
      </div>
    `;
  } else if (type === 'minimalist_analog' || type === 'celestial') {
    const secDeg = (now.getSeconds() / 60) * 360;
    const minDeg = ((now.getMinutes() + now.getSeconds() / 60) / 60) * 360;
    const hrDeg = (((now.getHours() % 12) + now.getMinutes() / 60) / 12) * 360;

    stage.innerHTML = `
      <div class="aod-analog-container" style="border: 1.5px solid rgba(255,255,255,0.12); box-shadow: 0 0 20px ${glow}30;">
        <!-- Dial ticks -->
        <span style="position:absolute; top:8px; width:3px; height:10px; background:${accent}; border-radius:2px;"></span>
        <span style="position:absolute; bottom:8px; width:3px; height:10px; background:${text}; border-radius:2px;"></span>
        <span style="position:absolute; left:8px; height:3px; width:10px; background:${text}; border-radius:2px;"></span>
        <span style="position:absolute; right:8px; height:3px; width:10px; background:${text}; border-radius:2px;"></span>

        <!-- Hour hand -->
        <div style="position:absolute; width:4px; height:45px; background:${text}; border-radius:3px; transform-origin:bottom center; bottom:50%; transform: rotate(${hrDeg}deg); box-shadow:0 0 6px ${glow};"></div>

        <!-- Minute hand -->
        <div style="position:absolute; width:3px; height:65px; background:${accent}; border-radius:3px; transform-origin:bottom center; bottom:50%; transform: rotate(${minDeg}deg); box-shadow:0 0 8px ${accent};"></div>

        <!-- Second needle -->
        <div style="position:absolute; width:1.5px; height:75px; background:${glow}; border-radius:1px; transform-origin:bottom center; bottom:50%; transform: rotate(${secDeg}deg);"></div>

        <!-- Center pin -->
        <div style="width:10px; height:10px; border-radius:50%; background:#000; border:2px solid ${accent}; z-index:10;"></div>
      </div>
    `;
  } else if (type === 'typography_word') {
    stage.innerHTML = `
      <div class="aod-word-grid">
        <div>
          <span style="color:${accent}; text-shadow:0 0 8px ${accent};">IT</span> 
          <span style="color:${accent}; text-shadow:0 0 8px ${accent};">IS</span> 
          <span style="color:rgba(255,255,255,0.2);">HALF</span> 
          <span style="color:rgba(255,255,255,0.2);">A</span>
        </div>
        <div>
          <span style="color:rgba(255,255,255,0.2);">QUARTER</span> 
          <span style="color:rgba(255,255,255,0.2);">TWENTY</span>
        </div>
        <div>
          <span style="color:rgba(255,255,255,0.2);">TO</span> 
          <span style="color:${accent}; text-shadow:0 0 8px ${accent};">PAST</span> 
          <span style="color:rgba(255,255,255,0.2);">SIX</span>
        </div>
        <div>
          <span style="color:${text}; text-shadow:0 0 10px ${glow};">TEN</span> 
          <span style="color:rgba(255,255,255,0.2);">ELEVEN</span>
        </div>
        <div>
          <span style="color:rgba(255,255,255,0.2);">IN</span> 
          <span style="color:${accent};">THE</span> 
          <span style="color:${accent}; text-shadow:0 0 8px ${accent};">NIGHT</span>
        </div>
      </div>
    `;
  } else if (type === 'neon_animal') {
    stage.innerHTML = `
      <div style="display:flex; flex-direction:column; align-items:center;">
        <!-- Glowing Fox Icon Silhouette -->
        <div style="font-size:52px; filter:drop-shadow(0 0 14px ${accent}); margin-bottom:4px;">
          🦊
        </div>
        <div class="aod-digital-time" style="color:${text}; text-shadow:0 0 18px ${glow}; font-size:44px;">
          ${hours}:${minutes}
        </div>
        <div style="font-size:11px; font-weight:800; color:${accent}; letter-spacing:2px; margin-top:2px;">
          CYBER KITSUNE
        </div>
      </div>
    `;
  } else if (type === 'gaming_hud') {
    stage.innerHTML = `
      <div style="position:relative; width:190px; height:190px; border-radius:50%; border:3px solid rgba(255,255,255,0.08); display:flex; flex-direction:column; align-items:center; justify-content:center; box-shadow:inset 0 0 25px rgba(245,158,11,0.15);">
        <!-- Reticle corners -->
        <div style="position:absolute; top:8px; font-size:10px; color:${accent}; font-family:monospace; font-weight:900;">LVL 99 // HUD</div>
        <div class="aod-digital-time" style="color:${text}; text-shadow:0 0 14px ${glow}; font-size:42px;">
          ${hours}:${minutes}
        </div>
        <!-- Health & Mana Bars in Simulator -->
        <div style="width:120px; display:flex; flex-direction:column; gap:3px; margin-top:6px;">
          <div style="height:5px; background:rgba(255,255,255,0.1); border-radius:3px; overflow:hidden;">
            <div style="width:84%; height:100%; background:#EF4444;"></div>
          </div>
          <div style="height:5px; background:rgba(255,255,255,0.1); border-radius:3px; overflow:hidden;">
            <div style="width:68%; height:100%; background:${accent};"></div>
          </div>
        </div>
      </div>
    `;
  }

  // Update Widget visibility
  const wBattery = document.getElementById('simAodWidgetBattery');
  const wDate = document.getElementById('simAodWidgetDate');
  const wSteps = document.getElementById('simAodWidgetSteps');
  const wWeather = document.getElementById('simAodWidgetWeather');

  if (wBattery) wBattery.style.display = aodSimWidgets.battery ? 'inline-flex' : 'none';
  if (wDate) wDate.style.display = aodSimWidgets.date ? 'inline-flex' : 'none';
  if (wSteps) wSteps.style.display = aodSimWidgets.steps ? 'inline-flex' : 'none';
  if (wWeather) wWeather.style.display = aodSimWidgets.weather ? 'inline-flex' : 'none';
}

function startAODRealTimeClock() {
  if (aodClockTimer) clearInterval(aodClockTimer);
  aodClockTimer = setInterval(() => {
    if (activeAodSimClock) {
      updateAODSimulatorClock();
    }
  }, 1000);
}

function toggleSimWidget(widgetKey) {
  const chk = document.getElementById(`chkSim${widgetKey.charAt(0).toUpperCase() + widgetKey.slice(1)}`);
  if (chk) {
    aodSimWidgets[widgetKey] = chk.checked;
    updateAODSimulatorClock();
  }
}

function triggerSimPixelShift() {
  const shifts = [
    { x: 0, y: 0 },
    { x: 6, y: 5 },
    { x: -5, y: 7 },
    { x: 7, y: -4 },
    { x: -6, y: -6 }
  ];

  aodBurnInShiftStep = (aodBurnInShiftStep + 1) % shifts.length;
  const current = shifts[aodBurnInShiftStep];

  const container = document.getElementById('simAodShiftContainer');
  const badge = document.getElementById('burnInShiftBadge');

  if (container) {
    container.style.transform = `translate(${current.x}px, ${current.y}px)`;
  }
  if (badge) {
    badge.textContent = `(${current.x > 0 ? '+' : ''}${current.x}px, ${current.y > 0 ? '+' : ''}${current.y}px)`;
  }

  showToast(`Anti-Burn-In Pixel Shift: Offset to (${current.x}px, ${current.y}px)`);
}

function openAddAodModal(clockToEdit = null) {
  const modal = document.getElementById('aodClockModal');
  const titleEl = document.getElementById('modalAodTitle');
  const idInput = document.getElementById('aodClockId');
  const titleInput = document.getElementById('aodClockTitle');
  const typeSelect = document.getElementById('aodClockType');
  const dialSelect = document.getElementById('aodDialStyle');
  const accentInput = document.getElementById('aodAccentColor');
  const accentPicker = document.getElementById('aodAccentColorPicker');
  const glowInput = document.getElementById('aodGlowColor');
  const glowPicker = document.getElementById('aodGlowColorPicker');
  const textInput = document.getElementById('aodTextColor');
  const textPicker = document.getElementById('aodTextColorPicker');
  const sortInput = document.getElementById('aodSortOrder');
  const batteryCheck = document.getElementById('aodHasBattery');
  const dateCheck = document.getElementById('aodHasDate');
  const stepsCheck = document.getElementById('aodHasSteps');
  const weatherCheck = document.getElementById('aodHasWeather');
  const premiumCheck = document.getElementById('aodIsPremium');
  const activeCheck = document.getElementById('aodIsActive');

  if (clockToEdit) {
    if (titleEl) titleEl.textContent = '✏️ Edit AOD Clock Face';
    if (idInput) idInput.value = clockToEdit.id;
    if (titleInput) titleInput.value = clockToEdit.title || '';
    if (typeSelect) typeSelect.value = clockToEdit.clockType || 'cyberpunk_digital';
    if (dialSelect) dialSelect.value = clockToEdit.dialStyle || 'futuristic_hud';

    const accent = clockToEdit.accentColor || '#00E5FF';
    const glow = clockToEdit.glowColor || accent;
    const text = clockToEdit.textColor || '#FFFFFF';

    if (accentInput) accentInput.value = accent;
    if (accentPicker) accentPicker.value = accent;
    if (glowInput) glowInput.value = glow;
    if (glowPicker) glowPicker.value = glow;
    if (textInput) textInput.value = text;
    if (textPicker) textPicker.value = text;

    if (sortInput) sortInput.value = clockToEdit.sortOrder || 1;
    if (batteryCheck) batteryCheck.checked = Boolean(clockToEdit.hasBatteryWidget);
    if (dateCheck) dateCheck.checked = Boolean(clockToEdit.hasDateWidget);
    if (stepsCheck) stepsCheck.checked = Boolean(clockToEdit.hasStepsWidget);
    if (weatherCheck) weatherCheck.checked = Boolean(clockToEdit.hasWeatherWidget);
    if (premiumCheck) premiumCheck.checked = Boolean(clockToEdit.isPremium);
    if (activeCheck) activeCheck.checked = Boolean(clockToEdit.isActive);
  } else {
    if (titleEl) titleEl.textContent = '🕒 Add AOD Clock Face';
    if (idInput) idInput.value = '';
    if (titleInput) titleInput.value = 'Cyberpunk Neon HUD 2077';
    if (typeSelect) typeSelect.value = 'cyberpunk_digital';
    if (dialSelect) dialSelect.value = 'futuristic_hud';

    if (accentInput) accentInput.value = '#00E5FF';
    if (accentPicker) accentPicker.value = '#00E5FF';
    if (glowInput) glowInput.value = '#7000FF';
    if (glowPicker) glowPicker.value = '#7000FF';
    if (textInput) textInput.value = '#FFFFFF';
    if (textPicker) textPicker.value = '#FFFFFF';

    if (sortInput) sortInput.value = allAodClocks.length + 1;
    if (batteryCheck) batteryCheck.checked = true;
    if (dateCheck) dateCheck.checked = true;
    if (stepsCheck) stepsCheck.checked = true;
    if (weatherCheck) weatherCheck.checked = true;
    if (premiumCheck) premiumCheck.checked = false;
    if (activeCheck) activeCheck.checked = true;
  }

  if (modal) modal.classList.add('active');
}

function openEditAodModal(id) {
  const found = allAodClocks.find(c => c.id === id);
  if (found) {
    openAddAodModal(found);
  }
}

async function handleAodFormSubmit(e) {
  e.preventDefault();

  const id = document.getElementById('aodClockId')?.value;
  const title = document.getElementById('aodClockTitle')?.value;
  const clockType = document.getElementById('aodClockType')?.value;
  const dialStyle = document.getElementById('aodDialStyle')?.value;
  const accentColor = document.getElementById('aodAccentColor')?.value;
  const glowColor = document.getElementById('aodGlowColor')?.value;
  const textColor = document.getElementById('aodTextColor')?.value;
  const sortOrder = parseInt(document.getElementById('aodSortOrder')?.value || '1', 10);
  const hasBatteryWidget = document.getElementById('aodHasBattery')?.checked ? 1 : 0;
  const hasDateWidget = document.getElementById('aodHasDate')?.checked ? 1 : 0;
  const hasStepsWidget = document.getElementById('aodHasSteps')?.checked ? 1 : 0;
  const hasWeatherWidget = document.getElementById('aodHasWeather')?.checked ? 1 : 0;
  const isPremium = document.getElementById('aodIsPremium')?.checked ? 1 : 0;
  const isActive = document.getElementById('aodIsActive')?.checked ? 1 : 0;

  if (!title) {
    showToast('Clock face title is required');
    return;
  }

  const payload = {
    title,
    clockType,
    dialStyle,
    accentColor,
    glowColor,
    textColor,
    backgroundColor: '#000000',
    hasBatteryWidget,
    hasDateWidget,
    hasStepsWidget,
    hasWeatherWidget,
    sortOrder,
    isPremium,
    isActive
  };

  const isEdit = Boolean(id);
  const url = isEdit ? `/api/admin/aod/clocks/${id}` : '/api/admin/aod/clocks';
  const method = isEdit ? 'PUT' : 'POST';

  try {
    const res = await authFetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (json.success) {
      showToast(json.message || 'AOD Clock face saved successfully!');
      document.getElementById('aodClockModal')?.classList.remove('active');
      await loadAODClocks();
      if (json.data) {
        selectAODClockForSim(json.data);
      }
    } else {
      showToast('Error: ' + (json.error || 'Failed to save clock face'));
    }
  } catch (err) {
    showToast('Server error: ' + err.message);
  }
}

async function toggleAodActive(id) {
  try {
    const res = await authFetch(`/api/admin/aod/clocks/${id}/toggle`, { method: 'PATCH' });
    const json = await res.json();
    if (json.success) {
      showToast('AOD Clock status updated');
      const clock = allAodClocks.find(c => c.id === id);
      if (clock) clock.isActive = json.isActive;
      renderAODCatalog();
    } else {
      showToast('Failed to toggle status: ' + json.error);
    }
  } catch (err) {
    showToast('Failed to toggle status: ' + err.message);
  }
}

async function deleteAodClock(id) {
  if (!confirm('Are you sure you want to delete this AOD clock face?')) return;
  try {
    const res = await authFetch(`/api/admin/aod/clocks/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('AOD Clock Face deleted successfully');
      allAodClocks = allAodClocks.filter(c => c.id !== id);
      renderAODCatalog();
      if (activeAodSimClock && activeAodSimClock.id === id) {
        activeAodSimClock = allAodClocks[0] || null;
        if (activeAodSimClock) selectAODClockForSim(activeAodSimClock);
      }
    } else {
      showToast('Error: ' + json.error);
    }
  } catch (err) {
    showToast('Failed to delete clock face: ' + err.message);
  }
}

function initAODStudio() {
  const addBtn = document.getElementById('openAddAodModalBtn');
  const refreshBtn = document.getElementById('refreshAodBtn');
  const closeBtn = document.getElementById('closeAodModal');
  const cancelBtn = document.getElementById('cancelAodModalBtn');
  const form = document.getElementById('aodClockForm');

  if (addBtn) addBtn.addEventListener('click', () => openAddAodModal());
  if (refreshBtn) refreshBtn.addEventListener('click', () => {
    loadAODClocks();
    showToast('AOD clock faces refreshed');
  });

  [closeBtn, cancelBtn].forEach(btn => {
    if (btn) btn.addEventListener('click', () => {
      document.getElementById('aodClockModal')?.classList.remove('active');
    });
  });

  if (form) form.addEventListener('submit', handleAodFormSubmit);

  // Search and Filter Listeners
  document.getElementById('aodSearchInput')?.addEventListener('input', renderAODCatalog);
  document.getElementById('aodStyleFilter')?.addEventListener('change', renderAODCatalog);
  document.getElementById('aodStatusFilter')?.addEventListener('change', renderAODCatalog);

  // Color picker syncs
  const syncPicker = (pickerId, textId) => {
    const p = document.getElementById(pickerId);
    const t = document.getElementById(textId);
    if (p && t) {
      p.addEventListener('input', () => { t.value = p.value.toUpperCase(); });
      t.addEventListener('input', () => {
        if (/^#[0-9A-F]{6}$/i.test(t.value)) p.value = t.value;
      });
    }
  };
  syncPicker('aodAccentColorPicker', 'aodAccentColor');
  syncPicker('aodGlowColorPicker', 'aodGlowColor');
  syncPicker('aodTextColorPicker', 'aodTextColor');

  // Brightness slider
  const brightnessSlider = document.getElementById('simAodBrightnessSlider');
  if (brightnessSlider) {
    brightnessSlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      aodBrightness = val;
      const displayVal = document.getElementById('simAodBrightnessVal');
      if (displayVal) displayVal.textContent = `${val}%`;
      const screen = document.getElementById('aodPhoneScreen');
      if (screen) screen.style.opacity = (val / 100).toFixed(2);
    });
  }

  // Start real-time ticking clock loop
  startAODRealTimeClock();
}

// Global window exposure for AOD Studio
window.openAddAodModal = openAddAodModal;
window.openEditAodModal = openEditAodModal;
window.deleteAodClock = deleteAodClock;
window.toggleAodActive = toggleAodActive;
window.selectAODClockById = selectAODClockById;
window.toggleSimWidget = toggleSimWidget;
window.triggerSimPixelShift = triggerSimPixelShift;
window.loadAODClocks = loadAODClocks;

// =========================================================================
// 📞 3D COLOR CALL SCREEN & FLASH THEMES STUDIO LOGIC
// =========================================================================

let allCallThemes = [];
let activeCallSimTheme = null;
let simCallFlashActive = true;
let simCallSoundActive = true;
let simCallFlashSpeed = 'normal';
let simCallButtonStyle = 'neon_glow';
let simCallIsRinging = false;

async function loadCallScreenThemes() {
  try {
    const res = await authFetch('/api/admin/call-screen/themes');
    const json = await res.json();
    if (json.success) {
      allCallThemes = json.data || [];
      const stats = json.stats || {};
      const totalEl = document.getElementById('totalCallThemes');
      const activeEl = document.getElementById('activeCallThemes');
      const appliedEl = document.getElementById('totalCallApplied');
      const flashEl = document.getElementById('callFlashCount');

      if (totalEl) totalEl.textContent = (stats.total || allCallThemes.length).toLocaleString();
      if (activeEl) activeEl.textContent = (stats.active || allCallThemes.filter(t => t.isActive).length).toLocaleString();
      if (appliedEl) appliedEl.textContent = (stats.totalDownloads || 0).toLocaleString();
      if (flashEl) flashEl.textContent = (stats.flashCount || allCallThemes.filter(t => t.flashAlertEnabled).length).toLocaleString();

      renderCallScreenCatalog();

      if (!activeCallSimTheme && allCallThemes.length > 0) {
        selectCallThemeForSim(allCallThemes[0]);
      }
    }
  } catch (err) {
    console.error('Error loading call screen themes:', err);
  }
}

function renderCallScreenCatalog() {
  const container = document.getElementById('callThemesGrid');
  if (!container) return;

  const searchQuery = (document.getElementById('callSearchInput')?.value || '').trim().toLowerCase();
  const catFilter = document.getElementById('callCategoryFilter')?.value || 'ALL';
  const statusFilter = document.getElementById('callStatusFilter')?.value || 'ALL';

  const filtered = allCallThemes.filter(theme => {
    const matchesSearch = !searchQuery ||
      theme.title.toLowerCase().includes(searchQuery) ||
      theme.category.toLowerCase().includes(searchQuery) ||
      theme.buttonStyle.toLowerCase().includes(searchQuery);

    const matchesCategory = catFilter === 'ALL' || theme.category === catFilter;
    const matchesStatus = statusFilter === 'ALL' ||
      (statusFilter === 'ACTIVE' && theme.isActive) ||
      (statusFilter === 'INACTIVE' && !theme.isActive);

    return matchesSearch && matchesCategory && matchesStatus;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding: 48px 20px; background: rgba(15, 23, 42, 0.4); border-radius: 12px; border: 1px dashed rgba(255,255,255,0.1);">
        <span style="font-size:38px;">📞</span>
        <h4 style="margin:10px 0 4px; color:#fff;">No Call Themes Found</h4>
        <p class="text-muted" style="font-size:12px;">Try clearing filters or click "+ Add Call Theme" to create one.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = filtered.map(theme => {
    const isSelected = activeCallSimTheme && activeCallSimTheme.id === theme.id;
    const categoryColors = {
      NEON: '#00E676',
      LUXURY: '#FFD700',
      '3D_PARALLAX': '#B388FF',
      ANIME: '#FF1744',
      CYBERPUNK: '#00E5FF'
    };
    const catColor = categoryColors[theme.category] || '#00E5FF';

    return `
      <div class="callscreen-card ${isSelected ? 'active-sim' : ''}" id="card-call-${theme.id}">
        <div class="callscreen-card-thumb" onclick="selectCallThemeById('${theme.id}')">
          <img src="${theme.previewUrl || '/uploads/callscreen/call_cyber_matrix_2077.svg'}" alt="${theme.title}">
          <span class="callscreen-card-badge" style="background:${catColor}; color:#000;">${theme.category}</span>
          ${theme.flashAlertEnabled ? `<span class="callscreen-flash-badge">⚡ Flash Alert</span>` : ''}
        </div>
        <div class="callscreen-card-body">
          <div class="callscreen-card-title">
            <span>${theme.title}</span>
            ${theme.isPremium ? '<span style="font-size:11px; color:#FFD700;">💎 VIP</span>' : ''}
          </div>
          <div class="callscreen-meta-row">
            <span>🔘 ${theme.buttonStyle.replace('_', ' ')}</span>
            <span>⬇️ ${(theme.downloads || 0).toLocaleString()}</span>
          </div>
          <div class="callscreen-card-actions">
            <button type="button" class="btn btn-sm btn-adb-push" onclick="pushToDevice('call_screen', '${theme.id}', event)" title="1-Click Live Test on connected Phone (I2212)" style="font-size:11px;">
              📲 Push
            </button>
            <button type="button" class="btn btn-secondary btn-sm flex-1" onclick="selectCallThemeById('${theme.id}')" style="font-size:11.5px;">
              👁️ Preview
            </button>
            <button type="button" class="btn btn-secondary btn-sm" onclick="openEditCallModal('${theme.id}')" title="Edit Theme" style="padding:4px 8px;">
              ✏️
            </button>
            <button type="button" class="btn btn-sm ${theme.isActive ? 'btn-secondary' : 'btn-primary'}" onclick="toggleCallThemeActive('${theme.id}')" title="Toggle Live" style="padding:4px 8px; font-size:11px;">
              ${theme.isActive ? 'Active' : 'Off'}
            </button>
            <button type="button" class="btn btn-secondary btn-sm" onclick="deleteCallTheme('${theme.id}')" title="Delete Theme" style="padding:4px 8px; color:#FF5252;">
              🗑️
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function selectCallThemeById(id) {
  const theme = allCallThemes.find(t => t.id === id);
  if (theme) selectCallThemeForSim(theme);
}

function selectCallThemeForSim(theme) {
  activeCallSimTheme = theme;
  simCallButtonStyle = theme.buttonStyle || 'neon_glow';
  simCallFlashSpeed = theme.flashSpeed || 'normal';

  document.querySelectorAll('.callscreen-card').forEach(c => c.classList.remove('active-sim'));
  const card = document.getElementById(`card-call-${theme.id}`);
  if (card) card.classList.add('active-sim');

  const bgImg = document.getElementById('simCallBgImg');
  if (bgImg) bgImg.src = theme.backgroundUrl || theme.previewUrl;

  const badge = document.getElementById('simCallCategoryBadge');
  if (badge) {
    badge.textContent = theme.category;
    const catColors = { NEON: '#00E676', LUXURY: '#FFD700', '3D_PARALLAX': '#B388FF', ANIME: '#FF1744', CYBERPUNK: '#00E5FF', RETRO: '#FF007F' };
    badge.style.background = catColors[theme.category] || '#00E5FF';
    badge.style.color = (theme.category === 'LUXURY' || theme.category === 'NEON' || theme.category === 'CYBERPUNK') ? '#000' : '#fff';
  }

  // Dynamic Caller Personas based on Theme
  const personas = {
    CYBERPUNK: { name: 'SARAH CONNOR', number: '+1 (555) 019-2834', emoji: '🤖', status: 'INCOMING CYBER CALL...' },
    LUXURY: { name: 'ALEXANDER PIERCE', number: '+44 20 7946 0958', emoji: '👑', status: 'VIP GOLD CALL...' },
    '3D_PARALLAX': { name: 'ASTRONAUT ZERO', number: '+1 (800) 555-0881', emoji: '👨‍🚀', status: 'ORBITAL FREQUENCY...' },
    ANIME: { name: 'KENSHIN RAIJIN', number: '+81 3 5555 0192', emoji: '⚡', status: 'THUNDER CLAN CALLING...' },
    NEON: { name: 'FREYA NORDIC', number: '+47 21 00 00 00', emoji: '❄️', status: 'AURORA INCOMING...' },
    RETRO: { name: 'MIAMI VICE 84', number: '+1 (305) 555-1984', emoji: '🌴', status: 'RETRO 80S CALLING...' }
  };
  const profile = personas[theme.category] || { name: 'SARAH CONNOR', number: '+1 (555) 019-2834', emoji: '👤', status: 'INCOMING CALL...' };

  const nameEl = document.getElementById('simCallerName');
  if (nameEl) nameEl.textContent = profile.name;

  const numberEl = document.getElementById('simCallerNumber');
  if (numberEl) numberEl.textContent = profile.number;

  const emojiEl = document.getElementById('simCallAvatarEmoji');
  if (emojiEl) emojiEl.textContent = profile.emoji;

  const statusText = document.getElementById('simCallStatusText');
  if (statusText) statusText.textContent = profile.status;

  const avatarCircle = document.getElementById('simCallAvatarCircle');
  if (avatarCircle) {
    avatarCircle.style.borderColor = theme.accentColor || '#00E5FF';
    avatarCircle.style.boxShadow = `0 0 25px ${theme.accentColor || '#00E5FF'}88`;
  }

  const rings = document.querySelectorAll('.call-ripple-ring');
  rings.forEach(r => {
    r.style.borderColor = theme.accentColor || '#00E5FF';
  });

  const styleSelect = document.getElementById('simButtonStyleSelect');
  if (styleSelect) styleSelect.value = simCallButtonStyle;

  const speedSelect = document.getElementById('simFlashSpeedSelect');
  if (speedSelect) speedSelect.value = simCallFlashSpeed;

  updateCallButtonsUI(simCallButtonStyle, theme.accentColor || '#00E5FF');
  updateFlashLedClass();
}

function updateCallButtonsUI(style, accentColor) {
  const stage = document.getElementById('simCallButtonsStage');
  if (!stage) return;

  if (style === 'glassmorphism') {
    stage.innerHTML = `
      <div class="call-btn-glass decline" onclick="handleDeclineSimCall()" title="Decline Call">
        <svg width="24" height="24" fill="none" stroke="#FFFFFF" stroke-width="2.5" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </div>
      <div class="call-btn-glass accept" onclick="handleAcceptSimCall()" title="Accept Call">
        <svg width="24" height="24" fill="none" stroke="#FFFFFF" stroke-width="2.5" viewBox="0 0 24 24"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
      </div>
    `;
  } else if (style === 'retro_cyber') {
    stage.innerHTML = `
      <div class="call-btn-retro decline" onclick="handleDeclineSimCall()" title="Decline Call">
        <span style="color:#fff; font-weight:900; font-size:12px; letter-spacing:1px;">END</span>
      </div>
      <div class="call-btn-retro accept" onclick="handleAcceptSimCall()" title="Accept Call">
        <span style="color:#fff; font-weight:900; font-size:12px; letter-spacing:1px;">ANS</span>
      </div>
    `;
  } else if (style === 'minimal_flat') {
    stage.innerHTML = `
      <div class="call-btn-minimal decline" onclick="handleDeclineSimCall()" title="Decline Call">
        <svg width="22" height="22" fill="none" stroke="#FFFFFF" stroke-width="2.5" viewBox="0 0 24 24"><path d="M10.68 13.31a16 16 0 0 0 3.41 2.6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91"/></svg>
      </div>
      <div class="call-btn-minimal accept" onclick="handleAcceptSimCall()" title="Accept Call">
        <svg width="22" height="22" fill="none" stroke="#FFFFFF" stroke-width="2.5" viewBox="0 0 24 24"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
      </div>
    `;
  } else {
    // Default: neon_glow
    stage.innerHTML = `
      <div class="call-btn-neon decline" onclick="handleDeclineSimCall()" title="Decline Call">
        <svg width="26" height="26" fill="none" stroke="#FFFFFF" stroke-width="2.5" viewBox="0 0 24 24"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
      </div>
      <div class="call-btn-neon accept" onclick="handleAcceptSimCall()" title="Accept Call">
        <svg width="26" height="26" fill="none" stroke="#FFFFFF" stroke-width="2.5" viewBox="0 0 24 24"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
      </div>
    `;
  }
}

function setSimButtonStyle(style) {
  simCallButtonStyle = style;
  updateCallButtonsUI(style, activeCallSimTheme?.accentColor || '#00E5FF');
}

function setSimFlashSpeed(speed) {
  simCallFlashSpeed = speed;
  updateFlashLedClass();
}

function updateFlashLedClass() {
  const led = document.getElementById('simCallFlashLed');
  if (!led) return;
  led.className = 'call-flash-led';
  if (simCallFlashActive) {
    if (simCallFlashSpeed === 'strobe') led.classList.add('strobing-fast');
    else if (simCallFlashSpeed === 'slow') led.classList.add('strobing-slow');
    else led.classList.add('strobing-normal');
  }
}

function toggleSimFlashlight() {
  simCallFlashActive = !simCallFlashActive;
  const btnText = document.getElementById('simFlashBtnText');
  if (btnText) btnText.textContent = `Flashlight: ${simCallFlashActive ? 'ON' : 'OFF'}`;
  updateFlashLedClass();
  showToast(`Camera Flashlight Alert ${simCallFlashActive ? 'Enabled' : 'Disabled'}`);
}

function toggleSimCallSound() {
  simCallSoundActive = !simCallSoundActive;
  const btnText = document.getElementById('simSoundBtnText');
  if (btnText) btnText.textContent = `Audio: ${simCallSoundActive ? 'ON' : 'OFF'}`;
  showToast(`Ringtone Audio ${simCallSoundActive ? 'Enabled' : 'Muted'}`);
}

function simulateIncomingCall() {
  simCallIsRinging = true;
  const statusText = document.getElementById('simCallStatusText');
  if (statusText) statusText.textContent = 'RINGING... (INCOMING)';

  const frame = document.getElementById('callPhoneFrame');
  if (frame) {
    frame.style.transform = 'scale(1.02)';
    setTimeout(() => { frame.style.transform = 'scale(1)'; }, 300);
  }

  if (simCallSoundActive) {
    try {
      const ctx = new (window.AudioContext || window.webkitAudioContext)();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(440, ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(880, ctx.currentTime + 0.3);
      gain.gain.setValueAtTime(0.1, ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.5);
      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();
      osc.stop(ctx.currentTime + 0.5);
    } catch (_) {}
  }

  showToast('📲 Incoming call ringing! Press Accept or Decline.');
}

function handleAcceptSimCall() {
  simCallIsRinging = false;
  const statusText = document.getElementById('simCallStatusText');
  if (statusText) statusText.textContent = 'CALL CONNECTED 00:01';
  showToast('📞 Call Connected! (Microphone Active)');
}

function handleDeclineSimCall() {
  simCallIsRinging = false;
  const statusText = document.getElementById('simCallStatusText');
  if (statusText) statusText.textContent = 'CALL ENDED';
  setTimeout(() => {
    if (statusText) statusText.textContent = 'INCOMING CALL...';
  }, 2000);
  showToast('❌ Call Rejected');
}

function openAddCallModal() {
  const modal = document.getElementById('callThemeModal');
  const form = document.getElementById('callThemeForm');
  if (!modal || !form) return;

  document.getElementById('modalCallThemeTitle').textContent = '📞 Add Call Screen Theme';
  document.getElementById('callThemeId').value = '';
  document.getElementById('callThemeTitle').value = 'Cyberpunk Matrix 2077';
  document.getElementById('callThemeCategory').value = 'NEON';
  document.getElementById('callButtonStyle').value = 'neon_glow';
  document.getElementById('callPreviewUrl').value = '';
  document.getElementById('callAccentColor').value = '#00E5FF';
  document.getElementById('callAccentColorPicker').value = '#00E5FF';
  document.getElementById('callGlowColor').value = '#7000FF';
  document.getElementById('callGlowColorPicker').value = '#7000FF';
  document.getElementById('callFlashAlert').checked = true;
  document.getElementById('callFlashSpeed').value = 'normal';
  document.getElementById('callSortOrder').value = '1';
  document.getElementById('callIsPremium').checked = false;
  document.getElementById('callIsActive').checked = true;

  modal.classList.add('active');
}

function openEditCallModal(id) {
  const theme = allCallThemes.find(t => t.id === id);
  if (!theme) return;

  const modal = document.getElementById('callThemeModal');
  if (!modal) return;

  document.getElementById('modalCallThemeTitle').textContent = '✏️ Edit Call Screen Theme';
  document.getElementById('callThemeId').value = theme.id;
  document.getElementById('callThemeTitle').value = theme.title;
  document.getElementById('callThemeCategory').value = theme.category;
  document.getElementById('callButtonStyle').value = theme.buttonStyle;
  document.getElementById('callPreviewUrl').value = theme.previewUrl || '';
  document.getElementById('callAccentColor').value = theme.accentColor || '#00E5FF';
  document.getElementById('callAccentColorPicker').value = theme.accentColor || '#00E5FF';
  document.getElementById('callGlowColor').value = theme.glowColor || '#7000FF';
  document.getElementById('callGlowColorPicker').value = theme.glowColor || '#7000FF';
  document.getElementById('callFlashAlert').checked = Boolean(theme.flashAlertEnabled);
  document.getElementById('callFlashSpeed').value = theme.flashSpeed || 'normal';
  document.getElementById('callSortOrder').value = theme.sortOrder || 1;
  document.getElementById('callIsPremium').checked = Boolean(theme.isPremium);
  document.getElementById('callIsActive').checked = Boolean(theme.isActive);

  modal.classList.add('active');
}

async function handleCallThemeFormSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('callThemeId').value;
  const isEdit = Boolean(id);

  const title = document.getElementById('callThemeTitle').value.trim();
  if (!title) {
    showToast('Theme title is required');
    return;
  }

  const formData = new FormData();
  formData.append('title', title);
  formData.append('category', document.getElementById('callThemeCategory').value);
  formData.append('buttonStyle', document.getElementById('callButtonStyle').value);
  formData.append('accentColor', document.getElementById('callAccentColor').value);
  formData.append('glowColor', document.getElementById('callGlowColor').value);
  formData.append('flashAlertEnabled', document.getElementById('callFlashAlert').checked ? '1' : '0');
  formData.append('flashSpeed', document.getElementById('callFlashSpeed').value);
  formData.append('sortOrder', document.getElementById('callSortOrder').value);
  formData.append('isPremium', document.getElementById('callIsPremium').checked ? '1' : '0');
  formData.append('previewUrl', document.getElementById('callPreviewUrl').value.trim());

  const fileInput = document.getElementById('callPreviewFile');
  if (fileInput && fileInput.files[0]) {
    formData.append('preview', fileInput.files[0]);
  }

  try {
    const url = isEdit ? `/api/admin/call-screen/themes/${id}` : '/api/admin/call-screen/themes';
    const method = isEdit ? 'PUT' : 'POST';

    const res = await authFetch(url, {
      method,
      body: formData
    });
    const json = await res.json();
    if (json.success) {
      showToast(isEdit ? 'Theme updated successfully!' : 'New Call Screen theme created!');
      document.getElementById('callThemeModal')?.classList.remove('active');
      await loadCallScreenThemes();
    } else {
      showToast('Error: ' + (json.error || 'Failed to save theme'));
    }
  } catch (err) {
    showToast('Failed to save call theme: ' + err.message);
  }
}

async function toggleCallThemeActive(id) {
  try {
    const res = await authFetch(`/api/admin/call-screen/themes/${id}/toggle`, { method: 'PATCH' });
    const json = await res.json();
    if (json.success) {
      showToast(json.message);
      const theme = allCallThemes.find(t => t.id === id);
      if (theme) theme.isActive = json.isActive;
      renderCallScreenCatalog();
    }
  } catch (err) {
    showToast('Failed to toggle status: ' + err.message);
  }
}

async function deleteCallTheme(id) {
  if (!confirm('Are you sure you want to delete this Call Screen theme?')) return;
  try {
    const res = await authFetch(`/api/admin/call-screen/themes/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Theme deleted successfully');
      allCallThemes = allCallThemes.filter(t => t.id !== id);
      renderCallScreenCatalog();
      if (activeCallSimTheme && activeCallSimTheme.id === id) {
        activeCallSimTheme = allCallThemes[0] || null;
        if (activeCallSimTheme) selectCallThemeForSim(activeCallSimTheme);
      }
    }
  } catch (err) {
    showToast('Failed to delete theme: ' + err.message);
  }
}

function initCallScreenStudio() {
  const addBtn = document.getElementById('openAddCallModalBtn');
  const refreshBtn = document.getElementById('refreshCallBtn');
  const closeBtn = document.getElementById('closeCallModal');
  const cancelBtn = document.getElementById('cancelCallModalBtn');
  const form = document.getElementById('callThemeForm');

  if (addBtn) addBtn.addEventListener('click', openAddCallModal);
  if (refreshBtn) refreshBtn.addEventListener('click', () => {
    loadCallScreenThemes();
    showToast('Call themes refreshed');
  });

  [closeBtn, cancelBtn].forEach(btn => {
    if (btn) btn.addEventListener('click', () => {
      document.getElementById('callThemeModal')?.classList.remove('active');
    });
  });

  if (form) form.addEventListener('submit', handleCallThemeFormSubmit);

  document.getElementById('callSearchInput')?.addEventListener('input', renderCallScreenCatalog);
  document.getElementById('callCategoryFilter')?.addEventListener('change', renderCallScreenCatalog);
  document.getElementById('callStatusFilter')?.addEventListener('change', renderCallScreenCatalog);

  const syncPicker = (pickerId, textId) => {
    const p = document.getElementById(pickerId);
    const t = document.getElementById(textId);
    if (p && t) {
      p.addEventListener('input', () => { t.value = p.value.toUpperCase(); });
      t.addEventListener('input', () => {
        if (/^#[0-9A-F]{6}$/i.test(t.value)) p.value = t.value;
      });
    }
  };
  syncPicker('callAccentColorPicker', 'callAccentColor');
  syncPicker('callGlowColorPicker', 'callGlowColor');
}

// Global window exposure
window.openAddCallModal = openAddCallModal;
window.openEditCallModal = openEditCallModal;
window.deleteCallTheme = deleteCallTheme;
window.toggleCallThemeActive = toggleCallThemeActive;
window.selectCallThemeById = selectCallThemeById;
window.setSimButtonStyle = setSimButtonStyle;
window.setSimFlashSpeed = setSimFlashSpeed;
window.toggleSimFlashlight = toggleSimFlashlight;
window.toggleSimCallSound = toggleSimCallSound;
window.simulateIncomingCall = simulateIncomingCall;
window.handleAcceptSimCall = handleAcceptSimCall;
window.handleDeclineSimCall = handleDeclineSimCall;
window.loadCallScreenThemes = loadCallScreenThemes;

// =========================================================================
// 👥 DUO / DOUBLE WALLPAPERS STUDIO LOGIC
// =========================================================================

let allDuoPairs = [];
let activeDuoSimPair = null;
let duoSimView = 'dual'; // 'dual' | 'unlock'
let duoUnlockAnimTimer = null;

async function loadDuoWallpapers() {
  try {
    const res = await authFetch('/api/admin/duo/wallpapers');
    const json = await res.json();
    if (json.success) {
      allDuoPairs = json.data || [];
      const stats = json.stats || {};
      const totalEl = document.getElementById('totalDuoPairs');
      const activeEl = document.getElementById('activeDuoPairs');
      const appliedEl = document.getElementById('totalDuoApplied');
      const vipEl = document.getElementById('vipDuoPairs');

      if (totalEl) totalEl.textContent = (stats.total || allDuoPairs.length).toLocaleString();
      if (activeEl) activeEl.textContent = (stats.active || allDuoPairs.filter(p => p.isActive).length).toLocaleString();
      if (appliedEl) appliedEl.textContent = (stats.totalDownloads || 0).toLocaleString();
      if (vipEl) vipEl.textContent = (stats.vipCount || allDuoPairs.filter(p => p.isPremium).length).toLocaleString();

      renderDuoWallpapers();

      if (!activeDuoSimPair && allDuoPairs.length > 0) {
        selectDuoPairForSim(allDuoPairs[0]);
      }
    }
  } catch (err) {
    console.error('Error loading duo wallpapers:', err);
  }
}

function renderDuoWallpapers() {
  const container = document.getElementById('duoPairsGrid');
  if (!container) return;

  const searchQuery = (document.getElementById('duoSearchInput')?.value || '').trim().toLowerCase();
  const catFilter = document.getElementById('duoCategoryFilter')?.value || 'ALL';
  const statusFilter = document.getElementById('duoStatusFilter')?.value || 'ALL';

  const filtered = allDuoPairs.filter(pair => {
    const matchesSearch = !searchQuery ||
      pair.title.toLowerCase().includes(searchQuery) ||
      (pair.description && pair.description.toLowerCase().includes(searchQuery)) ||
      pair.category.toLowerCase().includes(searchQuery);

    const matchesCategory = catFilter === 'ALL' || pair.category === catFilter;
    const matchesStatus = statusFilter === 'ALL' ||
      (statusFilter === 'ACTIVE' && pair.isActive) ||
      (statusFilter === 'INACTIVE' && !pair.isActive);

    return matchesSearch && matchesCategory && matchesStatus;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding: 48px 20px; background: rgba(15, 23, 42, 0.4); border-radius: 12px; border: 1px dashed rgba(255,255,255,0.1);">
        <span style="font-size:38px;">👥</span>
        <h4 style="margin:10px 0 4px; color:#fff;">No Duo Pairs Found</h4>
        <p class="text-muted" style="font-size:12px;">Try clearing filters or click "+ Add Duo Pair" to create one.</p>
      </div>
    `;
    return;
  }

  const categoryColors = {
    CYBERPUNK: '#00E5FF',
    LANDSCAPE: '#00E676',
    SPACE: '#B388FF',
    ANIME: '#FF1744',
    COUPLE: '#FF4081',
    NATURE: '#FF9100'
  };

  container.innerHTML = filtered.map(pair => {
    const isSelected = activeDuoSimPair && activeDuoSimPair.id === pair.id;
    const catColor = categoryColors[pair.category] || '#00E5FF';

    return `
      <div class="duo-card ${isSelected ? 'active-sim' : ''}" id="card-duo-${pair.id}">
        <div class="duo-split-thumb" onclick="selectDuoPairById('${pair.id}')" title="Preview Magic Pair">
          <div class="duo-split-half lock-half">
            <img src="${pair.lockImageUrl}" alt="${pair.title} Lock">
            <span class="duo-thumb-label">🔒 LOCK</span>
          </div>
          <div class="duo-split-divider"></div>
          <div class="duo-split-half home-half">
            <img src="${pair.homeImageUrl}" alt="${pair.title} Home">
            <span class="duo-thumb-label">📱 HOME</span>
          </div>
          <span class="duo-card-badge" style="background:${catColor}; color:#000;">${pair.category}</span>
          ${pair.isPremium ? `<span class="duo-vip-badge">💎 VIP</span>` : ''}
        </div>
        <div class="duo-card-body">
          <div class="duo-card-title-row">
            <h4 class="duo-card-title" onclick="selectDuoPairById('${pair.id}')">${pair.title}</h4>
          </div>
          ${pair.description ? `<p class="duo-card-desc">${pair.description}</p>` : ''}
          <div class="duo-meta-row">
            <span>🎨 ${pair.accentColor || '#00E5FF'}</span>
            <span>⬇️ ${(pair.downloads || 0).toLocaleString()} applied</span>
          </div>
          <div class="duo-card-actions">
            <button type="button" class="btn btn-secondary btn-sm flex-1" onclick="selectDuoPairById('${pair.id}')" style="font-size:11.5px;">
              👁️ Simulator
            </button>
            <button type="button" class="btn btn-secondary btn-sm" onclick="openEditDuoModal('${pair.id}')" title="Edit Pair" style="padding:4px 8px;">
              ✏️
            </button>
            <button type="button" class="btn btn-sm ${pair.isActive ? 'btn-secondary' : 'btn-primary'}" onclick="toggleDuoActive('${pair.id}')" title="Toggle Live" style="padding:4px 8px; font-size:11px;">
              ${pair.isActive ? 'Active' : 'Off'}
            </button>
            <button type="button" class="btn btn-secondary btn-sm" onclick="deleteDuoPair('${pair.id}')" title="Delete Pair" style="padding:4px 8px; color:#FF5252;">
              🗑️
            </button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function selectDuoPairById(id) {
  const pair = allDuoPairs.find(p => p.id === id);
  if (pair) selectDuoPairForSim(pair);
}

function selectDuoPairForSim(pair) {
  activeDuoSimPair = pair;

  document.querySelectorAll('.duo-card').forEach(c => c.classList.remove('active-sim'));
  const card = document.getElementById(`card-duo-${pair.id}`);
  if (card) card.classList.add('active-sim');

  const titleEl = document.getElementById('simDuoPairTitle');
  if (titleEl) titleEl.textContent = pair.title + (pair.description ? ` — ${pair.description}` : '');

  const badge = document.getElementById('simDuoCategoryBadge');
  if (badge) {
    badge.textContent = pair.category;
    const catColors = { CYBERPUNK: '#00E5FF', LANDSCAPE: '#00E676', SPACE: '#B388FF', ANIME: '#FF1744', COUPLE: '#FF4081', NATURE: '#FF9100' };
    badge.style.background = catColors[pair.category] || '#00E5FF';
    badge.style.color = (pair.category === 'ANIME' || pair.category === 'SPACE') ? '#fff' : '#000';
  }

  const lockImg = document.getElementById('simDuoLockImg');
  if (lockImg) lockImg.src = pair.lockImageUrl;

  const homeImg = document.getElementById('simDuoHomeImg');
  if (homeImg) homeImg.src = pair.homeImageUrl;

  const slider = document.getElementById('duoUnlockSlider');
  if (slider) slider.value = 0;
  onDuoUnlockSliderChange(0);
}

function setDuoSimView(mode) {
  duoSimView = mode;
  const btnDual = document.getElementById('btnModeDual');
  const btnUnlock = document.getElementById('btnModeUnlock');
  const container = document.getElementById('duoPhoneContainer');

  if (btnDual) btnDual.classList.toggle('active-mode', mode === 'dual');
  if (btnUnlock) btnUnlock.classList.toggle('active-mode', mode === 'unlock');

  if (container) {
    container.classList.toggle('unlock-test-mode', mode === 'unlock');
  }

  if (mode === 'unlock') {
    simulateMagicUnlock();
  } else {
    onDuoUnlockSliderChange(0);
  }
}

function onDuoUnlockSliderChange(val) {
  const percent = Number(val);
  const lockFrame = document.getElementById('duoLockPhoneFrame');
  const homeFrame = document.getElementById('duoHomePhoneFrame');
  const container = document.getElementById('duoPhoneContainer');

  if (container && container.classList.contains('unlock-test-mode')) {
    if (lockFrame) {
      lockFrame.style.opacity = (1 - percent / 100).toFixed(2);
      lockFrame.style.transform = `translateY(-${(percent * 0.9).toFixed(1)}px) scale(${(1 - percent * 0.001).toFixed(2)})`;
    }
    if (homeFrame) {
      homeFrame.style.transform = `scale(${(0.92 + (percent / 100) * 0.08).toFixed(2)})`;
      homeFrame.style.opacity = '1';
    }
  } else {
    if (lockFrame) {
      lockFrame.style.opacity = (1 - (percent * 0.35) / 100).toFixed(2);
      lockFrame.style.transform = `scale(${(1 - (percent * 0.05) / 100).toFixed(2)})`;
    }
    if (homeFrame) {
      homeFrame.style.opacity = (0.7 + (percent * 0.3) / 100).toFixed(2);
      homeFrame.style.transform = `scale(${(0.95 + (percent * 0.05) / 100).toFixed(2)})`;
    }
  }
}

function simulateMagicUnlock() {
  if (duoUnlockAnimTimer) {
    clearInterval(duoUnlockAnimTimer);
    duoUnlockAnimTimer = null;
  }

  const slider = document.getElementById('duoUnlockSlider');
  let currentVal = 0;
  let direction = 1;

  showToast('✨ Magic Transitioning: Lock -> Unlock Home Screen');

  duoUnlockAnimTimer = setInterval(() => {
    currentVal += direction * 4;
    if (currentVal >= 100) {
      currentVal = 100;
      direction = -1;
    } else if (currentVal <= 0) {
      currentVal = 0;
      clearInterval(duoUnlockAnimTimer);
      duoUnlockAnimTimer = null;
    }
    if (slider) slider.value = currentVal;
    onDuoUnlockSliderChange(currentVal);
  }, 35);
}

function openAddDuoModal() {
  const modal = document.getElementById('duoPairModal');
  const form = document.getElementById('duoPairForm');
  if (!modal || !form) return;

  document.getElementById('modalDuoTitle').textContent = '👥 Add Duo Wallpaper Pair';
  document.getElementById('duoPairId').value = '';
  document.getElementById('duoTitle').value = '';
  document.getElementById('duoDescription').value = '';
  document.getElementById('duoCategory').value = 'CYBERPUNK';
  document.getElementById('duoAccentColor').value = '#00E5FF';
  document.getElementById('duoAccentColorPicker').value = '#00E5FF';
  document.getElementById('duoLockUrl').value = '';
  document.getElementById('duoHomeUrl').value = '';
  document.getElementById('duoLockPreviewImg').src = '/uploads/duo/duo_cyber_samurai_lock.svg';
  document.getElementById('duoHomePreviewImg').src = '/uploads/duo/duo_cyber_samurai_home.svg';
  document.getElementById('duoSortOrder').value = '1';
  document.getElementById('duoIsPremium').checked = false;
  document.getElementById('duoIsActive').checked = true;

  modal.classList.add('active');
}

function openEditDuoModal(id) {
  const pair = allDuoPairs.find(p => p.id === id);
  if (!pair) return;

  const modal = document.getElementById('duoPairModal');
  if (!modal) return;

  document.getElementById('modalDuoTitle').textContent = '✏️ Edit Duo Wallpaper Pair';
  document.getElementById('duoPairId').value = pair.id;
  document.getElementById('duoTitle').value = pair.title;
  document.getElementById('duoDescription').value = pair.description || '';
  document.getElementById('duoCategory').value = pair.category;
  document.getElementById('duoAccentColor').value = pair.accentColor || '#00E5FF';
  document.getElementById('duoAccentColorPicker').value = pair.accentColor || '#00E5FF';
  document.getElementById('duoLockUrl').value = pair.lockImageUrl || '';
  document.getElementById('duoHomeUrl').value = pair.homeImageUrl || '';
  document.getElementById('duoLockPreviewImg').src = pair.lockImageUrl || '';
  document.getElementById('duoHomePreviewImg').src = pair.homeImageUrl || '';
  document.getElementById('duoSortOrder').value = pair.sortOrder || 1;
  document.getElementById('duoIsPremium').checked = Boolean(pair.isPremium);
  document.getElementById('duoIsActive').checked = Boolean(pair.isActive);

  modal.classList.add('active');
}

async function handleDuoPairFormSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('duoPairId').value;
  const isEdit = Boolean(id);

  const title = document.getElementById('duoTitle').value.trim();
  if (!title) {
    showToast('Pair title is required');
    return;
  }

  const formData = new FormData();
  formData.append('title', title);
  formData.append('description', document.getElementById('duoDescription').value.trim());
  formData.append('category', document.getElementById('duoCategory').value);
  formData.append('accentColor', document.getElementById('duoAccentColor').value);
  formData.append('sortOrder', document.getElementById('duoSortOrder').value);
  formData.append('isPremium', document.getElementById('duoIsPremium').checked ? '1' : '0');
  formData.append('lockImageUrl', document.getElementById('duoLockUrl').value.trim());
  formData.append('homeImageUrl', document.getElementById('duoHomeUrl').value.trim());

  const lockFile = document.getElementById('duoLockFile');
  if (lockFile && lockFile.files[0]) {
    formData.append('lockImage', lockFile.files[0]);
  }

  const homeFile = document.getElementById('duoHomeFile');
  if (homeFile && homeFile.files[0]) {
    formData.append('homeImage', homeFile.files[0]);
  }

  try {
    const url = isEdit ? `/api/admin/duo/wallpapers/${id}` : '/api/admin/duo/wallpapers';
    const method = isEdit ? 'PUT' : 'POST';

    const res = await authFetch(url, {
      method,
      body: formData
    });
    const json = await res.json();
    if (json.success) {
      showToast(isEdit ? 'Duo pair updated successfully!' : 'New Duo Wallpaper pair created!');
      document.getElementById('duoPairModal')?.classList.remove('active');
      await loadDuoWallpapers();
    } else {
      showToast('Error: ' + (json.error || 'Failed to save pair'));
    }
  } catch (err) {
    showToast('Failed to save duo pair: ' + err.message);
  }
}

async function toggleDuoActive(id) {
  try {
    const res = await authFetch(`/api/admin/duo/wallpapers/${id}/toggle`, { method: 'PATCH' });
    const json = await res.json();
    if (json.success) {
      showToast(json.message);
      const pair = allDuoPairs.find(p => p.id === id);
      if (pair) pair.isActive = json.isActive;
      renderDuoWallpapers();
    }
  } catch (err) {
    showToast('Failed to toggle status: ' + err.message);
  }
}

async function deleteDuoPair(id) {
  if (!confirm('Are you sure you want to delete this Duo Wallpaper pair?')) return;
  try {
    const res = await authFetch(`/api/admin/duo/wallpapers/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Duo pair deleted successfully');
      allDuoPairs = allDuoPairs.filter(p => p.id !== id);
      renderDuoWallpapers();
      if (activeDuoSimPair && activeDuoSimPair.id === id) {
        activeDuoSimPair = allDuoPairs[0] || null;
        if (activeDuoSimPair) selectDuoPairForSim(activeDuoSimPair);
      }
    }
  } catch (err) {
    showToast('Failed to delete duo pair: ' + err.message);
  }
}

function startDuoSimulatorClock() {
  function tick() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const clockEl = document.getElementById('simDuoLockClock');
    if (clockEl) clockEl.textContent = `${hours}:${minutes}`;

    const dateEl = document.getElementById('simDuoLockDate');
    if (dateEl) {
      const options = { weekday: 'long', month: 'long', day: 'numeric' };
      dateEl.textContent = now.toLocaleDateString('en-US', options);
    }
  }
  tick();
  setInterval(tick, 30000);
}

function initDuoStudio() {
  const addBtn = document.getElementById('openAddDuoModalBtn');
  const refreshBtn = document.getElementById('refreshDuoBtn');
  const closeBtn = document.getElementById('closeDuoModal');
  const cancelBtn = document.getElementById('cancelDuoModalBtn');
  const form = document.getElementById('duoPairForm');

  if (addBtn) addBtn.addEventListener('click', openAddDuoModal);
  if (refreshBtn) refreshBtn.addEventListener('click', () => {
    loadDuoWallpapers();
    showToast('Duo pairs refreshed');
  });

  [closeBtn, cancelBtn].forEach(btn => {
    if (btn) btn.addEventListener('click', () => {
      document.getElementById('duoPairModal')?.classList.remove('active');
    });
  });

  if (form) form.addEventListener('submit', handleDuoPairFormSubmit);

  document.getElementById('duoSearchInput')?.addEventListener('input', renderDuoWallpapers);
  document.getElementById('duoCategoryFilter')?.addEventListener('change', renderDuoWallpapers);
  document.getElementById('duoStatusFilter')?.addEventListener('change', renderDuoWallpapers);

  // Sync Color Picker
  const p = document.getElementById('duoAccentColorPicker');
  const t = document.getElementById('duoAccentColor');
  if (p && t) {
    p.addEventListener('input', () => { t.value = p.value.toUpperCase(); });
    t.addEventListener('input', () => {
      if (/^#[0-9A-F]{6}$/i.test(t.value)) p.value = t.value;
    });
  }

  // Preview uploads
  const lockFileInput = document.getElementById('duoLockFile');
  if (lockFileInput) {
    lockFileInput.addEventListener('change', e => {
      if (e.target.files && e.target.files[0]) {
        const url = URL.createObjectURL(e.target.files[0]);
        const prev = document.getElementById('duoLockPreviewImg');
        if (prev) prev.src = url;
      }
    });
  }

  const homeFileInput = document.getElementById('duoHomeFile');
  if (homeFileInput) {
    homeFileInput.addEventListener('change', e => {
      if (e.target.files && e.target.files[0]) {
        const url = URL.createObjectURL(e.target.files[0]);
        const prev = document.getElementById('duoHomePreviewImg');
        if (prev) prev.src = url;
      }
    });
  }

  startDuoSimulatorClock();
}

// Global window exposures for Duo Studio
window.openAddDuoModal = openAddDuoModal;
window.openEditDuoModal = openEditDuoModal;
window.deleteDuoPair = deleteDuoPair;
window.toggleDuoActive = toggleDuoActive;
window.selectDuoPairById = selectDuoPairById;
window.setDuoSimView = setDuoSimView;
window.simulateMagicUnlock = simulateMagicUnlock;
window.onDuoUnlockSliderChange = onDuoUnlockSliderChange;
window.loadDuoWallpapers = loadDuoWallpapers;

// =========================================================================
// 👆 INTERACTIVE TOUCH FLUID & RIPPLE STUDIO LOGIC
// =========================================================================

let allTouchPresets = [];
let activeTouchSimPreset = null;
let simTouchEffectType = 'WATER_RIPPLE';
let simTouchRadius = 75;
let simTouchSpeed = 1.2;
let simTouchColor = '#00E5FF';
let simTouchSecondaryColor = '#7000FF';
let isTouchInteracting = false;

// HTML5 Canvas Ripple & Particle Physics Engine
let touchCanvas = null;
let touchCtx = null;
let touchRipples = [];
let touchParticles = [];
let touchLightningArcs = [];
let touchAnimFrameId = null;

async function loadTouchPresets() {
  try {
    const res = await authFetch('/api/admin/touch-effects/presets');
    const json = await res.json();
    if (json.success) {
      allTouchPresets = json.data || [];
      const stats = json.stats || {};
      const totalEl = document.getElementById('totalTouchPresets');
      const activeEl = document.getElementById('activeTouchPresets');
      const appliedEl = document.getElementById('totalTouchApplied');
      const vipEl = document.getElementById('vipTouchPresets');

      if (totalEl) totalEl.textContent = (stats.total || allTouchPresets.length).toLocaleString();
      if (activeEl) activeEl.textContent = (stats.active || allTouchPresets.filter(p => p.isActive).length).toLocaleString();
      if (appliedEl) appliedEl.textContent = (stats.totalDownloads || 0).toLocaleString();
      if (vipEl) vipEl.textContent = (stats.vipCount || allTouchPresets.filter(p => p.isPremium).length).toLocaleString();

      renderTouchPresets();

      if (!activeTouchSimPreset && allTouchPresets.length > 0) {
        selectTouchPresetForSim(allTouchPresets[0]);
      }
    }
  } catch (err) {
    console.error('Error loading touch presets:', err);
  }
}

function renderTouchPresets() {
  const container = document.getElementById('touchPresetsGrid');
  if (!container) return;

  const searchQuery = (document.getElementById('touchSearchInput')?.value || '').trim().toLowerCase();
  const typeFilter = document.getElementById('touchTypeFilter')?.value || 'ALL';
  const statusFilter = document.getElementById('touchStatusFilter')?.value || 'ALL';

  const filtered = allTouchPresets.filter(preset => {
    const matchesSearch = !searchQuery ||
      preset.title.toLowerCase().includes(searchQuery) ||
      (preset.description && preset.description.toLowerCase().includes(searchQuery)) ||
      preset.effectType.toLowerCase().includes(searchQuery);

    const matchesType = typeFilter === 'ALL' || preset.effectType === typeFilter;
    const matchesStatus = statusFilter === 'ALL' ||
      (statusFilter === 'ACTIVE' && preset.isActive) ||
      (statusFilter === 'INACTIVE' && !preset.isActive);

    return matchesSearch && matchesType && matchesStatus;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding: 48px 20px; background: rgba(15, 23, 42, 0.4); border-radius: 12px; border: 1px dashed rgba(255,255,255,0.1);">
        <span style="font-size:38px;">👆</span>
        <h4 style="margin:10px 0 4px; color:#fff;">No Touch Presets Found</h4>
        <p class="text-muted" style="font-size:12px;">Try clearing filters or click "+ Add Touch Preset" to create one.</p>
      </div>
    `;
    return;
  }

  const typeIcons = {
    WATER_RIPPLE: '🌊',
    NEON_FLUID: '🌌',
    ELECTRIC_SPARKS: '⚡',
    MAGIC_STARDUST: '✨',
    MAGMA_BURST: '🔥',
    GRAVITY_VORTEX: '🌀'
  };

  container.innerHTML = filtered.map(preset => {
    const isSelected = activeTouchSimPreset && activeTouchSimPreset.id === preset.id;
    const icon = typeIcons[preset.effectType] || '👆';

    return `
      <div class="touch-card ${isSelected ? 'active-sim' : ''}" id="card-touch-${preset.id}" onclick="selectTouchPresetById('${preset.id}')" title="Click to simulate ${preset.title}">
        <div class="touch-card-header">
          <div class="touch-type-icon-badge" style="background:${preset.primaryColor}22; border:1px solid ${preset.primaryColor}55;">
            ${icon}
          </div>
          <div style="display:flex; align-items:center; gap:6px;">
            ${isSelected ? `<span class="touch-tag-pill" style="color:#00E5FF; border-color:#00E5FF; background:rgba(0,229,255,0.15); font-weight:700;">🎯 ACTIVE</span>` : ''}
            <span class="touch-tag-pill" style="color:${preset.primaryColor}; border-color:${preset.primaryColor}44;">${preset.effectType}</span>
            ${preset.isPremium ? `<span class="touch-tag-pill" style="color:#FFD700; border-color:#FFD70066;">💎 VIP</span>` : ''}
          </div>
        </div>
        <div class="touch-card-body">
          <h4 style="color:${isSelected ? '#00E5FF' : '#fff'}; transition:color 0.2s;">${preset.title}</h4>
          ${preset.description ? `<p class="touch-card-desc">${preset.description}</p>` : ''}
          <div class="touch-meta-tags">
            <span class="touch-tag-pill">⚡ Speed: ${preset.waveSpeed}x</span>
            <span class="touch-tag-pill">⭕ Radius: ${preset.waveRadius}px</span>
            <span class="touch-tag-pill">✨ Particles: ${preset.particleCount}</span>
            <span class="touch-tag-pill">⬇️ ${(preset.downloads || 0).toLocaleString()} applied</span>
          </div>
        </div>
        <div class="touch-card-actions" onclick="event.stopPropagation()">
          <button type="button" class="btn ${isSelected ? 'btn-primary' : 'btn-secondary'} btn-sm flex-1" onclick="selectTouchPresetById('${preset.id}')" style="font-size:11.5px; font-weight:${isSelected ? '700' : '500'};">
            ${isSelected ? '🎯 Simulating' : '👁️ Simulator'}
          </button>
          <button type="button" class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); openEditTouchModal('${preset.id}')" title="Edit Preset" style="padding:4px 8px;">
            ✏️
          </button>
          <button type="button" class="btn btn-sm ${preset.isActive ? 'btn-secondary' : 'btn-primary'}" onclick="event.stopPropagation(); toggleTouchPresetActive('${preset.id}')" title="Toggle Live" style="padding:4px 8px; font-size:11px;">
            ${preset.isActive ? 'Active' : 'Off'}
          </button>
          <button type="button" class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); deleteTouchPreset('${preset.id}')" title="Delete Preset" style="padding:4px 8px; color:#FF5252;">
            🗑️
          </button>
        </div>
      </div>
    `;
  }).join('');
}

function selectTouchPresetById(id) {
  const preset = allTouchPresets.find(p => p.id === id);
  if (preset) selectTouchPresetForSim(preset);
}

function selectTouchPresetForSim(preset) {
  if (!preset) return;
  activeTouchSimPreset = preset;
  simTouchEffectType = preset.effectType;
  simTouchRadius = preset.waveRadius || 75;
  simTouchSpeed = preset.waveSpeed || 1.2;
  simTouchColor = preset.primaryColor || '#00E5FF';
  simTouchSecondaryColor = preset.secondaryColor || '#7000FF';

  // Update card active classes and active tags
  document.querySelectorAll('.touch-card').forEach(c => {
    const isThisCard = c.id === `card-touch-${preset.id}`;
    c.classList.toggle('active-sim', isThisCard);
    const title = c.querySelector('h4');
    if (title) title.style.color = isThisCard ? '#00E5FF' : '#fff';
    const simBtn = c.querySelector('.touch-card-actions button:first-child');
    if (simBtn) {
      simBtn.className = `btn ${isThisCard ? 'btn-primary' : 'btn-secondary'} btn-sm flex-1`;
      simBtn.innerHTML = isThisCard ? '🎯 Simulating' : '👁️ Simulator';
    }
  });

  const titleEl = document.getElementById('simTouchPresetTitle');
  if (titleEl) titleEl.textContent = preset.title + (preset.description ? ` — ${preset.description}` : '');

  const badge = document.getElementById('simTouchTypeBadge');
  if (badge) {
    badge.textContent = preset.effectType;
    badge.style.background = preset.primaryColor || '#00E5FF';
    badge.style.color = (preset.primaryColor === '#FFE600' || preset.primaryColor === '#FFD700') ? '#000' : '#000';
  }

  // Update chip buttons
  document.querySelectorAll('.touch-chip').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('onclick')?.includes(preset.effectType));
  });

  // Update controls
  const rSlider = document.getElementById('simTouchRadiusSlider');
  if (rSlider) rSlider.value = simTouchRadius;
  const rVal = document.getElementById('simRadiusVal');
  if (rVal) rVal.textContent = `${simTouchRadius}px`;

  const sSlider = document.getElementById('simTouchSpeedSlider');
  if (sSlider) sSlider.value = simTouchSpeed;
  const sVal = document.getElementById('simSpeedVal');
  if (sVal) sVal.textContent = `${simTouchSpeed}x`;

  const cPicker = document.getElementById('simTouchColorPicker');
  if (cPicker) cPicker.value = simTouchColor;

  // Clear previous canvas objects and trigger preview shockwave with new physics
  clearSimTouchCanvas();
  triggerSimShockwave();
}

function setSimTouchEffectType(type) {
  // Find matching preset from allTouchPresets
  const matched = allTouchPresets.find(p => p.effectType === type);
  if (matched) {
    selectTouchPresetForSim(matched);
    return;
  }

  simTouchEffectType = type;
  const badge = document.getElementById('simTouchTypeBadge');
  if (badge) badge.textContent = type;

  document.querySelectorAll('.touch-chip').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('onclick')?.includes(type));
  });

  clearSimTouchCanvas();
  triggerSimShockwave();
}

function updateSimTouchParam(param, val) {
  if (param === 'radius') {
    simTouchRadius = parseInt(val) || 75;
    const el = document.getElementById('simRadiusVal');
    if (el) el.textContent = `${simTouchRadius}px`;
  } else if (param === 'speed') {
    simTouchSpeed = parseFloat(val) || 1.2;
    const el = document.getElementById('simSpeedVal');
    if (el) el.textContent = `${simTouchSpeed}x`;
  } else if (param === 'color') {
    simTouchColor = val;
  }
}

function triggerSimShockwave() {
  if (!touchCanvas) return;
  const cx = touchCanvas.width / 2;
  const cy = touchCanvas.height / 2;
  addTouchInteractionPoint(cx, cy, true);
}

function clearSimTouchCanvas() {
  touchRipples = [];
  touchParticles = [];
  touchLightningArcs = [];
  if (touchCtx && touchCanvas) {
    touchCtx.clearRect(0, 0, touchCanvas.width, touchCanvas.height);
  }
}

// =========================================================================
// HTML5 Canvas Physics Engine (Ripples, Fluid Dye, Sparks, Stardust)
// =========================================================================

function addTouchInteractionPoint(x, y, isBlast = false) {
  const prompt = document.getElementById('touchPromptPill');
  if (prompt) prompt.style.display = 'none';

  if (simTouchEffectType === 'WATER_RIPPLE') {
    touchRipples.push({
      x,
      y,
      radius: 5,
      maxRadius: simTouchRadius * (isBlast ? 2.0 : 1.2),
      alpha: 1.0,
      speed: 3.5 * simTouchSpeed,
      color: simTouchColor
    });
  } else if (simTouchEffectType === 'NEON_FLUID') {
    const count = isBlast ? 35 : 12;
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = (Math.random() * 2.5 + 1.0) * simTouchSpeed;
      touchParticles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        radius: Math.random() * 8 + 4,
        alpha: 0.9,
        decay: Math.random() * 0.02 + 0.015,
        color: Math.random() > 0.5 ? simTouchColor : simTouchSecondaryColor,
        isFluid: true
      });
    }
  } else if (simTouchEffectType === 'ELECTRIC_SPARKS') {
    const arcCount = isBlast ? 8 : 4;
    for (let i = 0; i < arcCount; i++) {
      const targetX = x + (Math.random() - 0.5) * simTouchRadius * 2;
      const targetY = y + (Math.random() - 0.5) * simTouchRadius * 2;
      touchLightningArcs.push({
        startX: x,
        startY: y,
        endX: targetX,
        endY: targetY,
        life: 1.0,
        decay: 0.08 * simTouchSpeed,
        color: Math.random() > 0.3 ? simTouchColor : '#FFFFFF'
      });
    }
  } else if (simTouchEffectType === 'MAGIC_STARDUST') {
    const count = isBlast ? 40 : 15;
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = (Math.random() * 2.0 + 0.5) * simTouchSpeed;
      touchParticles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        radius: Math.random() * 3 + 1.5,
        alpha: 1.0,
        decay: Math.random() * 0.02 + 0.01,
        color: Math.random() > 0.4 ? '#FFD700' : '#FF4081',
        isSparkle: true
      });
    }
  } else if (simTouchEffectType === 'MAGMA_BURST') {
    const count = isBlast ? 45 : 18;
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = (Math.random() * 3.5 + 1.0) * simTouchSpeed;
      touchParticles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed - 1.0,
        radius: Math.random() * 5 + 2,
        alpha: 1.0,
        decay: Math.random() * 0.03 + 0.015,
        color: Math.random() > 0.5 ? '#FF3D00' : '#FFD700'
      });
    }
  } else if (simTouchEffectType === 'GRAVITY_VORTEX') {
    touchRipples.push({
      x,
      y,
      radius: simTouchRadius * 1.5,
      maxRadius: simTouchRadius * 0.1,
      alpha: 1.0,
      speed: -2.0 * simTouchSpeed,
      color: '#B388FF',
      isVortex: true
    });
  }
}

function renderTouchCanvasFrame() {
  if (!touchCanvas || !touchCtx) {
    touchAnimFrameId = requestAnimationFrame(renderTouchCanvasFrame);
    return;
  }

  touchCtx.clearRect(0, 0, touchCanvas.width, touchCanvas.height);

  // Render Ripples
  for (let i = touchRipples.length - 1; i >= 0; i--) {
    const rip = touchRipples[i];
    rip.radius += rip.speed;
    rip.alpha -= 0.018 * simTouchSpeed;

    if (rip.alpha <= 0 || rip.radius >= rip.maxRadius || (rip.isVortex && rip.radius <= 0)) {
      touchRipples.splice(i, 1);
      continue;
    }

    touchCtx.save();
    touchCtx.beginPath();
    touchCtx.arc(rip.x, rip.y, Math.max(1, rip.radius), 0, Math.PI * 2);
    touchCtx.strokeStyle = rip.color;
    touchCtx.globalAlpha = Math.max(0, rip.alpha);
    touchCtx.lineWidth = 3.5;
    touchCtx.shadowColor = rip.color;
    touchCtx.shadowBlur = 12;
    touchCtx.stroke();
    touchCtx.restore();
  }

  // Render Particles & Fluid Drops
  for (let i = touchParticles.length - 1; i >= 0; i--) {
    const p = touchParticles[i];
    p.x += p.vx;
    p.y += p.vy;
    p.alpha -= p.decay;

    if (p.isFluid) {
      p.radius += 0.3;
    }

    if (p.alpha <= 0) {
      touchParticles.splice(i, 1);
      continue;
    }

    touchCtx.save();
    touchCtx.beginPath();
    touchCtx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
    touchCtx.fillStyle = p.color;
    touchCtx.globalAlpha = Math.max(0, p.alpha);
    touchCtx.shadowColor = p.color;
    touchCtx.shadowBlur = p.isSparkle ? 10 : 6;
    touchCtx.fill();
    touchCtx.restore();
  }

  // Render Lightning Arcs
  for (let i = touchLightningArcs.length - 1; i >= 0; i--) {
    const arc = touchLightningArcs[i];
    arc.life -= arc.decay;

    if (arc.life <= 0) {
      touchLightningArcs.splice(i, 1);
      continue;
    }

    touchCtx.save();
    touchCtx.beginPath();
    touchCtx.moveTo(arc.startX, arc.startY);

    const midX = (arc.startX + arc.endX) / 2 + (Math.random() - 0.5) * 16;
    const midY = (arc.startY + arc.endY) / 2 + (Math.random() - 0.5) * 16;
    touchCtx.lineTo(midX, midY);
    touchCtx.lineTo(arc.endX, arc.endY);

    touchCtx.strokeStyle = arc.color;
    touchCtx.globalAlpha = Math.max(0, arc.life);
    touchCtx.lineWidth = 2.5;
    touchCtx.shadowColor = arc.color;
    touchCtx.shadowBlur = 14;
    touchCtx.stroke();
    touchCtx.restore();
  }

  touchAnimFrameId = requestAnimationFrame(renderTouchCanvasFrame);
}

function openAddTouchModal() {
  const modal = document.getElementById('touchPresetModal');
  const form = document.getElementById('touchPresetForm');
  if (!modal || !form) return;

  document.getElementById('modalTouchTitle').textContent = '👆 Add Touch Effect Preset';
  document.getElementById('touchPresetId').value = '';
  document.getElementById('touchTitle').value = '';
  document.getElementById('touchEffectType').value = 'WATER_RIPPLE';
  document.getElementById('touchDescription').value = '';
  document.getElementById('touchPrimaryColor').value = '#00E5FF';
  document.getElementById('touchPrimaryColorPicker').value = '#00E5FF';
  document.getElementById('touchSecondaryColor').value = '#7000FF';
  document.getElementById('touchSecondaryColorPicker').value = '#7000FF';
  document.getElementById('touchAccentColor').value = '#FF007F';
  document.getElementById('touchAccentColorPicker').value = '#FF007F';
  document.getElementById('touchWaveSpeed').value = '1.2';
  document.getElementById('modalTouchSpeedText').textContent = '1.2x';
  document.getElementById('touchWaveRadius').value = '75';
  document.getElementById('modalTouchRadiusText').textContent = '75px';
  document.getElementById('touchParticleCount').value = '80';
  document.getElementById('modalTouchParticlesText').textContent = '80';
  document.getElementById('touchViscosity').value = '0.8';
  document.getElementById('modalTouchViscosityText').textContent = '0.8';
  document.getElementById('touchHapticEnabled').checked = true;
  document.getElementById('touchIsPremium').checked = false;
  document.getElementById('touchSortOrder').value = '1';
  document.getElementById('touchIsActive').checked = true;

  modal.classList.add('active');
}

function openEditTouchModal(id) {
  const preset = allTouchPresets.find(p => p.id === id);
  if (!preset) return;

  const modal = document.getElementById('touchPresetModal');
  if (!modal) return;

  document.getElementById('modalTouchTitle').textContent = '✏️ Edit Touch Effect Preset';
  document.getElementById('touchPresetId').value = preset.id;
  document.getElementById('touchTitle').value = preset.title;
  document.getElementById('touchEffectType').value = preset.effectType;
  document.getElementById('touchDescription').value = preset.description || '';
  document.getElementById('touchPrimaryColor').value = preset.primaryColor || '#00E5FF';
  document.getElementById('touchPrimaryColorPicker').value = preset.primaryColor || '#00E5FF';
  document.getElementById('touchSecondaryColor').value = preset.secondaryColor || '#7000FF';
  document.getElementById('touchSecondaryColorPicker').value = preset.secondaryColor || '#7000FF';
  document.getElementById('touchAccentColor').value = preset.accentColor || '#FF007F';
  document.getElementById('touchAccentColorPicker').value = preset.accentColor || '#FF007F';
  document.getElementById('touchWaveSpeed').value = preset.waveSpeed || 1.2;
  document.getElementById('modalTouchSpeedText').textContent = `${preset.waveSpeed || 1.2}x`;
  document.getElementById('touchWaveRadius').value = preset.waveRadius || 75;
  document.getElementById('modalTouchRadiusText').textContent = `${preset.waveRadius || 75}px`;
  document.getElementById('touchParticleCount').value = preset.particleCount || 80;
  document.getElementById('modalTouchParticlesText').textContent = `${preset.particleCount || 80}`;
  document.getElementById('touchViscosity').value = preset.viscosity || 0.8;
  document.getElementById('modalTouchViscosityText').textContent = `${preset.viscosity || 0.8}`;
  document.getElementById('touchHapticEnabled').checked = Boolean(preset.hapticEnabled);
  document.getElementById('touchIsPremium').checked = Boolean(preset.isPremium);
  document.getElementById('touchSortOrder').value = preset.sortOrder || 1;
  document.getElementById('touchIsActive').checked = Boolean(preset.isActive);

  modal.classList.add('active');
}

async function handleTouchPresetFormSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('touchPresetId').value;
  const isEdit = Boolean(id);

  const title = document.getElementById('touchTitle').value.trim();
  if (!title) {
    showToast('Preset title is required');
    return;
  }

  const payload = {
    title,
    effectType: document.getElementById('touchEffectType').value,
    description: document.getElementById('touchDescription').value.trim(),
    primaryColor: document.getElementById('touchPrimaryColor').value,
    secondaryColor: document.getElementById('touchSecondaryColor').value,
    accentColor: document.getElementById('touchAccentColor').value,
    waveSpeed: parseFloat(document.getElementById('touchWaveSpeed').value),
    waveRadius: parseInt(document.getElementById('touchWaveRadius').value),
    particleCount: parseInt(document.getElementById('touchParticleCount').value),
    viscosity: parseFloat(document.getElementById('touchViscosity').value),
    hapticEnabled: document.getElementById('touchHapticEnabled').checked ? 1 : 0,
    isPremium: document.getElementById('touchIsPremium').checked ? 1 : 0,
    sortOrder: parseInt(document.getElementById('touchSortOrder').value) || 1,
    isActive: document.getElementById('touchIsActive').checked ? 1 : 0
  };

  try {
    const url = isEdit ? `/api/admin/touch-effects/presets/${id}` : '/api/admin/touch-effects/presets';
    const method = isEdit ? 'PUT' : 'POST';

    const res = await authFetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (json.success) {
      showToast(isEdit ? 'Touch preset updated successfully!' : 'New Touch preset created!');
      document.getElementById('touchPresetModal')?.classList.remove('active');
      await loadTouchPresets();
    } else {
      showToast('Error: ' + (json.error || 'Failed to save preset'));
    }
  } catch (err) {
    showToast('Failed to save touch preset: ' + err.message);
  }
}

async function toggleTouchPresetActive(id) {
  try {
    const res = await authFetch(`/api/admin/touch-effects/presets/${id}/toggle`, { method: 'PATCH' });
    const json = await res.json();
    if (json.success) {
      showToast(json.message);
      const preset = allTouchPresets.find(p => p.id === id);
      if (preset) preset.isActive = json.isActive;
      renderTouchPresets();
    }
  } catch (err) {
    showToast('Failed to toggle status: ' + err.message);
  }
}

async function deleteTouchPreset(id) {
  if (!confirm('Are you sure you want to delete this Touch Effect preset?')) return;
  try {
    const res = await authFetch(`/api/admin/touch-effects/presets/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Touch preset deleted successfully');
      allTouchPresets = allTouchPresets.filter(p => p.id !== id);
      renderTouchPresets();
      if (activeTouchSimPreset && activeTouchSimPreset.id === id) {
        activeTouchSimPreset = allTouchPresets[0] || null;
        if (activeTouchSimPreset) selectTouchPresetForSim(activeTouchSimPreset);
      }
    }
  } catch (err) {
    showToast('Failed to delete touch preset: ' + err.message);
  }
}

function initTouchStudio() {
  const addBtn = document.getElementById('openAddTouchModalBtn');
  const refreshBtn = document.getElementById('refreshTouchBtn');
  const closeBtn = document.getElementById('closeTouchModal');
  const cancelBtn = document.getElementById('cancelTouchModalBtn');
  const form = document.getElementById('touchPresetForm');

  if (addBtn) addBtn.addEventListener('click', openAddTouchModal);
  if (refreshBtn) refreshBtn.addEventListener('click', () => {
    loadTouchPresets();
    showToast('Touch presets refreshed');
  });

  [closeBtn, cancelBtn].forEach(btn => {
    if (btn) btn.addEventListener('click', () => {
      document.getElementById('touchPresetModal')?.classList.remove('active');
    });
  });

  if (form) form.addEventListener('submit', handleTouchPresetFormSubmit);

  document.getElementById('touchSearchInput')?.addEventListener('input', renderTouchPresets);
  document.getElementById('touchTypeFilter')?.addEventListener('change', renderTouchPresets);
  document.getElementById('touchStatusFilter')?.addEventListener('change', renderTouchPresets);

  // Sync color pickers in modal
  const syncPicker = (pickerId, textId) => {
    const p = document.getElementById(pickerId);
    const t = document.getElementById(textId);
    if (p && t) {
      p.addEventListener('input', () => { t.value = p.value.toUpperCase(); });
      t.addEventListener('input', () => {
        if (/^#[0-9A-F]{6}$/i.test(t.value)) p.value = t.value;
      });
    }
  };
  syncPicker('touchPrimaryColorPicker', 'touchPrimaryColor');
  syncPicker('touchSecondaryColorPicker', 'touchSecondaryColor');
  syncPicker('touchAccentColorPicker', 'touchAccentColor');

  // Slider value labels in modal
  const syncSliderText = (sliderId, textId, suffix = '') => {
    const s = document.getElementById(sliderId);
    const t = document.getElementById(textId);
    if (s && t) {
      s.addEventListener('input', () => { t.textContent = s.value + suffix; });
    }
  };
  syncSliderText('touchWaveSpeed', 'modalTouchSpeedText', 'x');
  syncSliderText('touchWaveRadius', 'modalTouchRadiusText', 'px');
  syncSliderText('touchParticleCount', 'modalTouchParticlesText');
  syncSliderText('touchViscosity', 'modalTouchViscosityText');

  // Initialize Canvas
  touchCanvas = document.getElementById('simTouchCanvas');
  if (touchCanvas) {
    touchCtx = touchCanvas.getContext('2d');

    const getCanvasPos = (e) => {
      const rect = touchCanvas.getBoundingClientRect();
      const scaleX = touchCanvas.width / rect.width;
      const scaleY = touchCanvas.height / rect.height;
      const clientX = e.touches ? e.touches[0].clientX : e.clientX;
      const clientY = e.touches ? e.touches[0].clientY : e.clientY;
      return {
        x: (clientX - rect.left) * scaleX,
        y: (clientY - rect.top) * scaleY
      };
    };

    touchCanvas.addEventListener('mousedown', (e) => {
      isTouchInteracting = true;
      const pos = getCanvasPos(e);
      addTouchInteractionPoint(pos.x, pos.y);
    });

    touchCanvas.addEventListener('mousemove', (e) => {
      if (!isTouchInteracting) return;
      const pos = getCanvasPos(e);
      addTouchInteractionPoint(pos.x, pos.y);
    });

    window.addEventListener('mouseup', () => {
      isTouchInteracting = false;
    });

    touchCanvas.addEventListener('touchstart', (e) => {
      isTouchInteracting = true;
      const pos = getCanvasPos(e);
      addTouchInteractionPoint(pos.x, pos.y);
    }, { passive: true });

    touchCanvas.addEventListener('touchmove', (e) => {
      if (!isTouchInteracting) return;
      const pos = getCanvasPos(e);
      addTouchInteractionPoint(pos.x, pos.y);
    }, { passive: true });

    window.addEventListener('touchend', () => {
      isTouchInteracting = false;
    });

    // Start render loop
    if (!touchAnimFrameId) {
      touchAnimFrameId = requestAnimationFrame(renderTouchCanvasFrame);
    }
  }
}

// Global window exposures for Touch Studio
window.openAddTouchModal = openAddTouchModal;
window.openEditTouchModal = openEditTouchModal;
window.deleteTouchPreset = deleteTouchPreset;
window.toggleTouchPresetActive = toggleTouchPresetActive;
window.selectTouchPresetById = selectTouchPresetById;
window.setSimTouchEffectType = setSimTouchEffectType;
window.updateSimTouchParam = updateSimTouchParam;
window.triggerSimShockwave = triggerSimShockwave;
window.clearSimTouchCanvas = clearSimTouchCanvas;
window.loadTouchPresets = loadTouchPresets;

// =========================================================================
// 🔓 IN-DISPLAY FINGERPRINT ANIMATIONS STUDIO LOGIC
// =========================================================================

let allFingerprintPresets = [];
let activeFingerprintSimPreset = null;
let fpSimYPos = 78;
let fpSimScale = 1.0;
let fpSimSpeed = 1.2;
let fpSimPrimaryColor = '#00E5FF';
let fpSimSecondaryColor = '#7000FF';
let fpSimAccentGlow = '#00FFAA';
let fpSimAnimType = 'CYBER_MATRIX';
let fpCanvas = null;
let fpCtx = null;
let fpParticles = [];
let fpRings = [];
let fpAnimFrameId = null;
let fpIsScanning = false;
let fpScanTimer = null;

async function loadFingerprintPresets() {
  try {
    const res = await authFetch('/api/admin/fingerprint/presets');
    const json = await res.json();
    if (json.success) {
      allFingerprintPresets = json.data || [];
      const stats = json.stats || {};

      const totalEl = document.getElementById('totalFingerprintPresets');
      const activeEl = document.getElementById('activeFingerprintPresets');
      const appliedEl = document.getElementById('totalFingerprintApplied');
      const vipEl = document.getElementById('vipFingerprintPresets');

      if (totalEl) totalEl.textContent = (stats.total || allFingerprintPresets.length).toLocaleString();
      if (activeEl) activeEl.textContent = (stats.active || allFingerprintPresets.filter(p => p.isActive).length).toLocaleString();
      if (appliedEl) appliedEl.textContent = (stats.totalDownloads || 0).toLocaleString();
      if (vipEl) vipEl.textContent = (stats.vipCount || allFingerprintPresets.filter(p => p.isPremium).length).toLocaleString();

      renderFingerprintPresets();

      if (!activeFingerprintSimPreset && allFingerprintPresets.length > 0) {
        selectFingerprintPresetForSim(allFingerprintPresets[0]);
      }
    }
  } catch (err) {
    console.error('Error loading fingerprint presets:', err);
  }
}

function renderFingerprintPresets() {
  const container = document.getElementById('fingerprintPresetsGrid');
  if (!container) return;

  const searchQuery = (document.getElementById('fpSearchInput')?.value || '').trim().toLowerCase();
  const catFilter = document.getElementById('fpCategoryFilter')?.value || 'ALL';
  const statusFilter = document.getElementById('fpStatusFilter')?.value || 'ALL';

  const filtered = allFingerprintPresets.filter(p => {
    const matchesSearch = !searchQuery ||
      p.title.toLowerCase().includes(searchQuery) ||
      p.animationType.toLowerCase().includes(searchQuery) ||
      (p.description && p.description.toLowerCase().includes(searchQuery)) ||
      (p.category && p.category.toLowerCase().includes(searchQuery));

    const matchesCat = catFilter === 'ALL' || p.category === catFilter;
    const matchesStatus = statusFilter === 'ALL' ||
      (statusFilter === 'ACTIVE' && p.isActive) ||
      (statusFilter === 'INACTIVE' && !p.isActive);

    return matchesSearch && matchesCat && matchesStatus;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align:center; padding: 48px 20px; background: rgba(15, 23, 42, 0.4); border-radius: 12px; border: 1px dashed rgba(255,255,255,0.1);">
        <span style="font-size:38px;">🔓</span>
        <h4 style="margin:10px 0 4px; color:#fff;">No Fingerprint Presets Found</h4>
        <p class="text-muted" style="font-size:12px;">Try adjusting search terms or click "+ Add Fingerprint Preset" to create one.</p>
      </div>
    `;
    return;
  }

  const animIcons = {
    CYBER_MATRIX: '⚡',
    SUPERNOVA_FLARE: '🌌',
    NEON_PORTAL: '🌀',
    MYSTIC_RUNES: '✨',
    CIRCUIT_OVERLOAD: '🔌',
    SOLAR_FLARE: '🔥'
  };

  container.innerHTML = filtered.map(preset => {
    const isSelected = activeFingerprintSimPreset && activeFingerprintSimPreset.id === preset.id;
    const icon = animIcons[preset.animationType] || '🔓';

    return `
      <div class="touch-card ${isSelected ? 'active-sim' : ''}" id="card-fp-${preset.id}" onclick="selectFingerprintPresetById('${preset.id}')">
        <div class="touch-card-preview" style="background: radial-gradient(circle at center, ${preset.primaryColor || '#00E5FF'}22 0%, rgba(10,14,26,0.95) 75%); border-bottom: 1px solid rgba(255,255,255,0.06); height: 110px; display: flex; align-items: center; justify-content: center; position: relative;">
          <div style="position: relative; width: 62px; height: 62px; border-radius: 50%; border: 2px dashed ${preset.primaryColor || '#00E5FF'}; box-shadow: 0 0 16px ${preset.primaryColor || '#00E5FF'}66; display: flex; align-items: center; justify-content: center;">
            <div style="width: 44px; height: 44px; border-radius: 50%; border: 1.5px solid ${preset.secondaryColor || '#7000FF'}; display: flex; align-items: center; justify-content: center; color: ${preset.primaryColor || '#00E5FF'}; font-size: 20px;">
              ${icon}
            </div>
          </div>
          <span class="touch-card-badge" style="background:${preset.primaryColor || '#00E5FF'}; color:#000; font-weight:800; font-size:10px; position:absolute; top:8px; left:8px; padding:2px 7px; border-radius:4px;">
            ${preset.category || 'CYBER'}
          </span>
          ${preset.isPremium ? `<span class="touch-card-badge" style="background:#FFD700; color:#000; font-weight:800; font-size:10px; position:absolute; top:8px; right:8px; padding:2px 7px; border-radius:4px;">💎 PRO</span>` : ''}
        </div>
        <div class="touch-card-body" style="padding: 12px 14px;">
          <h4 style="color:${isSelected ? '#00E5FF' : '#fff'}; transition:color 0.2s; font-size: 14px; margin: 0 0 4px;">${preset.title}</h4>
          ${preset.description ? `<p class="touch-card-desc" style="font-size: 11.5px; color: var(--text-secondary); margin: 0 0 8px; line-height: 1.4;">${preset.description}</p>` : ''}
          <div class="touch-meta-tags" style="display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 10px;">
            <span class="touch-tag-pill">⚡ Speed: ${preset.animationSpeed || 1.2}x</span>
            <span class="touch-tag-pill">📏 Scale: ${preset.sensorScale || 1.0}x</span>
            <span class="touch-tag-pill">📍 Y-Pos: ${preset.yPositionPercent || 78}%</span>
            <span class="touch-tag-pill">⬇️ ${(preset.downloads || 0).toLocaleString()} applied</span>
          </div>
        </div>
        <div class="touch-card-actions" onclick="event.stopPropagation()" style="padding: 8px 14px 12px; display: flex; gap: 6px;">
          <button type="button" class="btn ${isSelected ? 'btn-primary' : 'btn-secondary'} btn-sm flex-1" onclick="selectFingerprintPresetById('${preset.id}')" style="font-size:11.5px; font-weight:${isSelected ? '700' : '500'};">
            ${isSelected ? '🎯 Simulating' : '👁️ Simulator'}
          </button>
          <button type="button" class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); openEditFingerprintModal('${preset.id}')" title="Edit Preset" style="padding:4px 8px;">
            ✏️
          </button>
          <button type="button" class="btn btn-sm ${preset.isActive ? 'btn-secondary' : 'btn-primary'}" onclick="event.stopPropagation(); toggleFingerprintPresetActive('${preset.id}')" title="Toggle Live" style="padding:4px 8px; font-size:11px;">
            ${preset.isActive ? 'Active' : 'Off'}
          </button>
          <button type="button" class="btn btn-secondary btn-sm" onclick="event.stopPropagation(); deleteFingerprintPreset('${preset.id}')" title="Delete Preset" style="padding:4px 8px; color:#FF5252;">
            🗑️
          </button>
        </div>
      </div>
    `;
  }).join('');
}

function selectFingerprintPresetById(id) {
  const preset = allFingerprintPresets.find(p => p.id === id);
  if (preset) selectFingerprintPresetForSim(preset);
}

function selectFingerprintPresetForSim(preset) {
  if (!preset) return;
  activeFingerprintSimPreset = preset;
  fpSimAnimType = preset.animationType;
  fpSimPrimaryColor = preset.primaryColor || '#00E5FF';
  fpSimSecondaryColor = preset.secondaryColor || '#7000FF';
  fpSimAccentGlow = preset.accentGlow || '#00FFAA';
  fpSimSpeed = preset.animationSpeed || 1.2;
  fpSimScale = preset.sensorScale || 1.0;
  fpSimYPos = preset.yPositionPercent || 78;

  // Update card active classes
  document.querySelectorAll('.touch-card').forEach(c => {
    const isThisCard = c.id === `card-fp-${preset.id}`;
    c.classList.toggle('active-sim', isThisCard);
    const title = c.querySelector('h4');
    if (title) title.style.color = isThisCard ? '#00E5FF' : '#fff';
    const simBtn = c.querySelector('.touch-card-actions button:first-child');
    if (simBtn) {
      simBtn.className = `btn ${isThisCard ? 'btn-primary' : 'btn-secondary'} btn-sm flex-1`;
      simBtn.innerHTML = isThisCard ? '🎯 Simulating' : '👁️ Simulator';
    }
  });

  // Simulator Header Titles & Badges
  const titleEl = document.getElementById('simFingerprintPresetTitle');
  if (titleEl) titleEl.textContent = preset.title;

  const badge = document.getElementById('simFpTypeBadge');
  if (badge) {
    badge.textContent = preset.animationType;
    badge.style.background = fpSimPrimaryColor;
    badge.style.color = (fpSimPrimaryColor === '#FFE600' || fpSimPrimaryColor === '#FFD700') ? '#000' : '#000';
  }

  // Effect Switcher Chips
  document.querySelectorAll('.touch-effect-chips .touch-chip').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('onclick')?.includes(preset.animationType));
  });

  // Sliders
  const ySlider = document.getElementById('simFpYSlider');
  if (ySlider) ySlider.value = fpSimYPos;
  const yVal = document.getElementById('simFpYVal');
  if (yVal) yVal.textContent = `${fpSimYPos}%`;

  const scaleSlider = document.getElementById('simFpScaleSlider');
  if (scaleSlider) scaleSlider.value = fpSimScale;
  const scaleVal = document.getElementById('simFpScaleVal');
  if (scaleVal) scaleVal.textContent = `${fpSimScale}x`;

  const colorPicker = document.getElementById('simFpColorPicker');
  if (colorPicker) colorPicker.value = fpSimPrimaryColor;

  // Hotspot Sensor Element Updates
  updateFpSensorElementStyles();

  // Trigger quick unlock burst for immediate visual feedback
  triggerSimFingerprintUnlock();
}

function updateFpSensorElementStyles() {
  const hotspot = document.getElementById('simFpSensorHotspot');
  if (!hotspot) return;

  hotspot.style.top = `${fpSimYPos}%`;
  hotspot.style.left = '50%';
  hotspot.style.transform = `translate(-50%, -50%) scale(${fpSimScale})`;

  const ringOuter = document.getElementById('fpRingOuter');
  if (ringOuter) {
    ringOuter.style.borderColor = fpSimPrimaryColor;
    ringOuter.style.boxShadow = `0 0 16px ${fpSimPrimaryColor}88`;
    ringOuter.style.animationDuration = `${1.8 / fpSimSpeed}s`;
  }

  const ringInner = document.getElementById('fpRingInner');
  if (ringInner) {
    ringInner.style.borderColor = fpSimSecondaryColor;
    ringInner.style.boxShadow = `0 0 12px ${fpSimSecondaryColor}66`;
    ringInner.style.animationDuration = `${1.2 / fpSimSpeed}s`;
  }

  const laser = document.getElementById('fpLaserSweep');
  if (laser) {
    laser.style.background = `linear-gradient(90deg, transparent, ${fpSimPrimaryColor}, transparent)`;
    laser.style.boxShadow = `0 0 10px ${fpSimPrimaryColor}`;
    laser.style.animationDuration = `${1.0 / fpSimSpeed}s`;
  }

  const thumb = document.getElementById('fpThumbIcon');
  if (thumb) {
    thumb.style.color = fpSimPrimaryColor;
    thumb.style.filter = `drop-shadow(0 0 8px ${fpSimPrimaryColor})`;
  }
}

function setFingerprintAnimType(type) {
  const matched = allFingerprintPresets.find(p => p.animationType === type);
  if (matched) {
    selectFingerprintPresetForSim(matched);
    return;
  }

  fpSimAnimType = type;
  const badge = document.getElementById('simFpTypeBadge');
  if (badge) badge.textContent = type;

  document.querySelectorAll('.touch-effect-chips .touch-chip').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('onclick')?.includes(type));
  });

  triggerSimFingerprintUnlock();
}

function onFpYPositionChange(val) {
  fpSimYPos = parseInt(val, 10) || 78;
  const el = document.getElementById('simFpYVal');
  if (el) el.textContent = `${fpSimYPos}%`;
  updateFpSensorElementStyles();
}

function onFpScaleChange(val) {
  fpSimScale = parseFloat(val) || 1.0;
  const el = document.getElementById('simFpScaleVal');
  if (el) el.textContent = `${fpSimScale}x`;
  updateFpSensorElementStyles();
}

function onFpColorChange(color) {
  fpSimPrimaryColor = color;
  updateFpSensorElementStyles();
}

function triggerSimFingerprintUnlock() {
  const hotspot = document.getElementById('simFpSensorHotspot');
  const badge = document.getElementById('fpUnlockBadge');
  const prompt = document.getElementById('fpPromptPill');

  if (hotspot) hotspot.classList.add('unlocking');
  if (badge) badge.classList.add('show');
  if (prompt) prompt.style.display = 'none';

  // Get coordinates for burst on canvas
  let cx = 160;
  let cy = 360;
  if (hotspot && fpCanvas) {
    const rect = hotspot.getBoundingClientRect();
    const cRect = fpCanvas.getBoundingClientRect();
    if (cRect.width > 0 && cRect.height > 0) {
      cx = ((rect.left + rect.width / 2) - cRect.left) * (fpCanvas.width / cRect.width);
      cy = ((rect.top + rect.height / 2) - cRect.top) * (fpCanvas.height / cRect.height);
    }
  }

  spawnFpUnlockBurst(cx, cy, fpSimPrimaryColor, fpSimSecondaryColor, fpSimAnimType);

  // Play synthetic unlock audio chime
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(587.33, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(1174.66, ctx.currentTime + 0.2);
    gain.gain.setValueAtTime(0.18, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, ctx.currentTime + 0.35);
    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.35);
  } catch (_) {}

  // Auto reset after 2.4 seconds
  setTimeout(() => {
    resetSimFingerprintSensor();
  }, 2400);
}

function resetSimFingerprintSensor() {
  const hotspot = document.getElementById('simFpSensorHotspot');
  const badge = document.getElementById('fpUnlockBadge');
  const prompt = document.getElementById('fpPromptPill');

  if (hotspot) {
    hotspot.classList.remove('unlocking');
    hotspot.classList.remove('scanning');
  }
  if (badge) badge.classList.remove('show');
  if (prompt) prompt.style.display = 'block';
}

function spawnFpUnlockBurst(x, y, primaryColor, secondaryColor, type) {
  // Shockwave Rings
  fpRings.push({
    x,
    y,
    radius: 12,
    maxRadius: 150 * fpSimScale,
    alpha: 1.0,
    speed: 5.5 * fpSimSpeed,
    color: primaryColor,
    lineWidth: 4
  });

  fpRings.push({
    x,
    y,
    radius: 4,
    maxRadius: 110 * fpSimScale,
    alpha: 0.9,
    speed: 3.8 * fpSimSpeed,
    color: secondaryColor,
    lineWidth: 2.5
  });

  // Outward particles based on type
  const count = 42;
  for (let i = 0; i < count; i++) {
    const angle = (Math.PI * 2 * i) / count + (Math.random() - 0.5) * 0.4;
    const speed = (Math.random() * 4.0 + 1.8) * fpSimSpeed;
    const isPrimary = Math.random() > 0.4;

    fpParticles.push({
      x,
      y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed,
      radius: Math.random() * 4 + 2,
      alpha: 1.0,
      decay: Math.random() * 0.022 + 0.014,
      color: isPrimary ? primaryColor : secondaryColor,
      isSparkle: type === 'MYSTIC_RUNES' || type === 'SUPERNOVA_FLARE',
      isSquare: type === 'CYBER_MATRIX' || type === 'CIRCUIT_OVERLOAD'
    });
  }
}

function renderFpCanvasFrame() {
  if (!fpCanvas || !fpCtx) {
    fpAnimFrameId = requestAnimationFrame(renderFpCanvasFrame);
    return;
  }

  fpCtx.clearRect(0, 0, fpCanvas.width, fpCanvas.height);

  // Render Expanding Shockwave Rings
  for (let i = fpRings.length - 1; i >= 0; i--) {
    const ring = fpRings[i];
    ring.radius += ring.speed;
    ring.alpha -= 0.022 * fpSimSpeed;

    if (ring.alpha <= 0 || ring.radius >= ring.maxRadius) {
      fpRings.splice(i, 1);
      continue;
    }

    fpCtx.save();
    fpCtx.beginPath();
    fpCtx.arc(ring.x, ring.y, Math.max(1, ring.radius), 0, Math.PI * 2);
    fpCtx.strokeStyle = ring.color;
    fpCtx.globalAlpha = Math.max(0, ring.alpha);
    fpCtx.lineWidth = ring.lineWidth;
    fpCtx.shadowColor = ring.color;
    fpCtx.shadowBlur = 14;
    fpCtx.stroke();
    fpCtx.restore();
  }

  // Render Particles
  for (let i = fpParticles.length - 1; i >= 0; i--) {
    const p = fpParticles[i];
    p.x += p.vx;
    p.y += p.vy;
    p.alpha -= p.decay;

    if (p.alpha <= 0) {
      fpParticles.splice(i, 1);
      continue;
    }

    fpCtx.save();
    fpCtx.globalAlpha = Math.max(0, p.alpha);
    fpCtx.fillStyle = p.color;
    fpCtx.shadowColor = p.color;
    fpCtx.shadowBlur = p.isSparkle ? 12 : 6;

    if (p.isSquare) {
      fpCtx.fillRect(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2);
    } else {
      fpCtx.beginPath();
      fpCtx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      fpCtx.fill();
    }
    fpCtx.restore();
  }

  fpAnimFrameId = requestAnimationFrame(renderFpCanvasFrame);
}

function openAddFingerprintModal(presetToEdit = null) {
  const modal = document.getElementById('fingerprintPresetModal');
  const titleEl = document.getElementById('fingerprintModalTitle');
  const idInput = document.getElementById('editFingerprintId');

  if (presetToEdit) {
    if (titleEl) titleEl.textContent = '✏️ Edit Fingerprint Preset';
    if (idInput) idInput.value = presetToEdit.id;
    document.getElementById('fpModalTitle').value = presetToEdit.title || '';
    document.getElementById('fpModalCategory').value = presetToEdit.category || 'CYBERPUNK';
    document.getElementById('fpModalAnimationType').value = presetToEdit.animationType || 'CYBER_MATRIX';
    document.getElementById('fpModalSortOrder').value = presetToEdit.sortOrder || 1;
    document.getElementById('fpModalDesc').value = presetToEdit.description || '';

    const pColor = presetToEdit.primaryColor || '#00E5FF';
    document.getElementById('fpModalPrimaryColor').value = pColor;
    document.getElementById('fpModalPrimaryColorPicker').value = pColor;

    const sColor = presetToEdit.secondaryColor || '#7000FF';
    document.getElementById('fpModalSecondaryColor').value = sColor;
    document.getElementById('fpModalSecondaryColorPicker').value = sColor;

    const aGlow = presetToEdit.accentGlow || '#00FFAA';
    document.getElementById('fpModalAccentGlow').value = aGlow;
    document.getElementById('fpModalAccentGlowPicker').value = aGlow;

    const speed = presetToEdit.animationSpeed || 1.2;
    document.getElementById('fpModalSpeed').value = speed;
    document.getElementById('fpModalSpeedVal').textContent = `${speed}x`;

    const scale = presetToEdit.sensorScale || 1.0;
    document.getElementById('fpModalScale').value = scale;
    document.getElementById('fpModalScaleVal').textContent = `${scale}x`;

    const pos = presetToEdit.yPositionPercent || 78;
    document.getElementById('fpModalPos').value = pos;
    document.getElementById('fpModalPosVal').textContent = `${pos}%`;

    document.getElementById('fpModalHaptic').checked = Boolean(presetToEdit.hapticEnabled);
    document.getElementById('fpModalSound').checked = Boolean(presetToEdit.soundEnabled);
    document.getElementById('fpModalPremium').checked = Boolean(presetToEdit.isPremium);
  } else {
    if (titleEl) titleEl.textContent = '🔓 Add Fingerprint Animation Preset';
    if (idInput) idInput.value = '';
    document.getElementById('fpModalTitle').value = '';
    document.getElementById('fpModalCategory').value = 'CYBERPUNK';
    document.getElementById('fpModalAnimationType').value = 'CYBER_MATRIX';
    document.getElementById('fpModalSortOrder').value = allFingerprintPresets.length + 1;
    document.getElementById('fpModalDesc').value = '';

    document.getElementById('fpModalPrimaryColor').value = '#00E5FF';
    document.getElementById('fpModalPrimaryColorPicker').value = '#00E5FF';
    document.getElementById('fpModalSecondaryColor').value = '#7000FF';
    document.getElementById('fpModalSecondaryColorPicker').value = '#7000FF';
    document.getElementById('fpModalAccentGlow').value = '#00FFAA';
    document.getElementById('fpModalAccentGlowPicker').value = '#00FFAA';

    document.getElementById('fpModalSpeed').value = 1.2;
    document.getElementById('fpModalSpeedVal').textContent = '1.2x';
    document.getElementById('fpModalScale').value = 1.0;
    document.getElementById('fpModalScaleVal').textContent = '1.0x';
    document.getElementById('fpModalPos').value = 78;
    document.getElementById('fpModalPosVal').textContent = '78%';

    document.getElementById('fpModalHaptic').checked = true;
    document.getElementById('fpModalSound').checked = true;
    document.getElementById('fpModalPremium').checked = false;
  }

  if (modal) modal.classList.add('active');
}

function openEditFingerprintModal(id) {
  const found = allFingerprintPresets.find(p => p.id === id);
  if (found) openAddFingerprintModal(found);
}

function closeFingerprintModal() {
  document.getElementById('fingerprintPresetModal')?.classList.remove('active');
}

async function handleFingerprintPresetFormSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('editFingerprintId')?.value;
  const isEdit = Boolean(id);

  const title = document.getElementById('fpModalTitle')?.value.trim();
  if (!title) {
    showToast('Preset title is required');
    return;
  }

  const payload = {
    title,
    category: document.getElementById('fpModalCategory')?.value || 'CYBERPUNK',
    animationType: document.getElementById('fpModalAnimationType')?.value || 'CYBER_MATRIX',
    description: document.getElementById('fpModalDesc')?.value.trim() || '',
    primaryColor: document.getElementById('fpModalPrimaryColor')?.value || '#00E5FF',
    secondaryColor: document.getElementById('fpModalSecondaryColor')?.value || '#7000FF',
    accentGlow: document.getElementById('fpModalAccentGlow')?.value || '#00FFAA',
    animationSpeed: parseFloat(document.getElementById('fpModalSpeed')?.value) || 1.2,
    sensorScale: parseFloat(document.getElementById('fpModalScale')?.value) || 1.0,
    yPositionPercent: parseInt(document.getElementById('fpModalPos')?.value, 10) || 78,
    hapticEnabled: document.getElementById('fpModalHaptic')?.checked ? 1 : 0,
    soundEnabled: document.getElementById('fpModalSound')?.checked ? 1 : 0,
    isPremium: document.getElementById('fpModalPremium')?.checked ? 1 : 0,
    sortOrder: parseInt(document.getElementById('fpModalSortOrder')?.value, 10) || 1,
    isActive: 1
  };

  try {
    const url = isEdit ? `/api/admin/fingerprint/presets/${id}` : '/api/admin/fingerprint/presets';
    const method = isEdit ? 'PUT' : 'POST';

    const res = await authFetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const json = await res.json();
    if (json.success) {
      showToast(isEdit ? 'Fingerprint preset updated!' : 'New Fingerprint preset created!');
      closeFingerprintModal();
      await loadFingerprintPresets();
    } else {
      showToast('Error: ' + (json.error || 'Failed to save preset'));
    }
  } catch (err) {
    showToast('Failed to save preset: ' + err.message);
  }
}

async function toggleFingerprintPresetActive(id) {
  try {
    const res = await authFetch(`/api/admin/fingerprint/presets/${id}/toggle`, { method: 'PATCH' });
    const json = await res.json();
    if (json.success) {
      showToast(json.message);
      const preset = allFingerprintPresets.find(p => p.id === id);
      if (preset) preset.isActive = json.isActive;
      renderFingerprintPresets();
    } else {
      showToast('Failed to toggle: ' + json.error);
    }
  } catch (err) {
    showToast('Failed to toggle status: ' + err.message);
  }
}

async function deleteFingerprintPreset(id) {
  if (!confirm('Are you sure you want to delete this Fingerprint Animation preset?')) return;
  try {
    const res = await authFetch(`/api/admin/fingerprint/presets/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (json.success) {
      showToast('Fingerprint preset deleted successfully');
      allFingerprintPresets = allFingerprintPresets.filter(p => p.id !== id);
      renderFingerprintPresets();
      if (activeFingerprintSimPreset && activeFingerprintSimPreset.id === id) {
        activeFingerprintSimPreset = allFingerprintPresets[0] || null;
        if (activeFingerprintSimPreset) selectFingerprintPresetForSim(activeFingerprintSimPreset);
      }
    } else {
      showToast('Error: ' + json.error);
    }
  } catch (err) {
    showToast('Failed to delete preset: ' + err.message);
  }
}

function initFingerprintStudio() {
  const form = document.getElementById('fingerprintPresetForm');
  if (form) form.addEventListener('submit', handleFingerprintPresetFormSubmit);

  // Canvas Initialization
  fpCanvas = document.getElementById('simFingerprintCanvas');
  if (fpCanvas) {
    fpCtx = fpCanvas.getContext('2d');
    if (!fpAnimFrameId) {
      fpAnimFrameId = requestAnimationFrame(renderFpCanvasFrame);
    }
  }

  // Interactive Press & Hold on Sensor Hotspot
  const hotspot = document.getElementById('simFpSensorHotspot');
  if (hotspot) {
    const startScan = (e) => {
      e.preventDefault();
      fpIsScanning = true;
      hotspot.classList.add('scanning');
      const prompt = document.getElementById('fpPromptPill');
      if (prompt) prompt.style.display = 'none';

      fpScanTimer = setTimeout(() => {
        if (fpIsScanning) {
          triggerSimFingerprintUnlock();
        }
      }, 550);
    };

    const cancelScan = () => {
      fpIsScanning = false;
      if (fpScanTimer) {
        clearTimeout(fpScanTimer);
        fpScanTimer = null;
      }
      if (!hotspot.classList.contains('unlocking')) {
        hotspot.classList.remove('scanning');
        const prompt = document.getElementById('fpPromptPill');
        if (prompt) prompt.style.display = 'block';
      }
    };

    hotspot.addEventListener('mousedown', startScan);
    hotspot.addEventListener('touchstart', startScan, { passive: false });

    hotspot.addEventListener('mouseup', cancelScan);
    hotspot.addEventListener('mouseleave', cancelScan);
    hotspot.addEventListener('touchend', cancelScan);
  }
}

// Window Exposures for HTML attributes
window.openAddFingerprintModal = openAddFingerprintModal;
window.openEditFingerprintModal = openEditFingerprintModal;
window.closeFingerprintModal = closeFingerprintModal;
window.deleteFingerprintPreset = deleteFingerprintPreset;
window.toggleFingerprintPresetActive = toggleFingerprintPresetActive;
window.selectFingerprintPresetById = selectFingerprintPresetById;
window.setFingerprintAnimType = setFingerprintAnimType;
window.onFpYPositionChange = onFpYPositionChange;
window.onFpScaleChange = onFpScaleChange;
window.onFpColorChange = onFpColorChange;
window.triggerSimFingerprintUnlock = triggerSimFingerprintUnlock;
window.resetSimFingerprintSensor = resetSimFingerprintSensor;
window.loadFingerprintPresets = loadFingerprintPresets;

// ========================================================
// ⚡ FLAGSHIP PERSONALIZATION SUITE MANAGER
// ========================================================
let suiteItems = [];
let suiteDragSrcIndex = null;

async function loadPersonalizationSuite() {
  try {
    const res = await authFetch('/api/admin/personalization-suite');
    const json = await res.json();
    const items = json.data || json.items;
    if (json.success && Array.isArray(items)) {
      suiteItems = items.sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
      renderSuiteCards();
      renderSuiteSimulator();
    } else {
      showToast('Failed to load personalization suite: ' + (json.error || 'Unknown error'));
    }
  } catch (err) {
    console.error('Error loading personalization suite:', err);
    showToast('Network error loading suite data');
  }
}

function renderSuiteCards() {
  const container = document.getElementById('suiteCardsList');
  if (!container) return;

  const activeCount = suiteItems.filter(i => i.enabled).length;
  const countBadge = document.getElementById('suiteActiveCountBadge');
  if (countBadge) {
    countBadge.textContent = `${activeCount} / ${suiteItems.length} Modules Live`;
  }

  container.innerHTML = suiteItems.map((item, index) => {
    const isFirst = index === 0;
    const isLast = index === suiteItems.length - 1;
    const isDisabled = !item.enabled;

    return `
      <div class="suite-card-item ${isDisabled ? 'disabled-module' : ''}" 
           draggable="true" 
           data-index="${index}" 
           data-id="${item.id}">
        <div class="suite-drag-handle" title="Drag to reorder">⠿</div>
        <div class="suite-order-arrows">
          <button type="button" class="btn-arrow-tiny btn-suite-up" data-index="${index}" ${isFirst ? 'disabled' : ''} title="Move Up">▲</button>
          <button type="button" class="btn-arrow-tiny btn-suite-down" data-index="${index}" ${isLast ? 'disabled' : ''} title="Move Down">▼</button>
        </div>
        <div class="suite-order-num">#${index + 1}</div>
        <div class="suite-icon-box">${item.iconEmoji || '⚡'}</div>
        <div class="suite-details">
          <div class="suite-title-row">
            <span class="suite-title-text">${escapeHtml(item.title)}</span>
          </div>
          <div class="suite-inputs-row">
            <div class="suite-input-wrap" style="flex: 1.5; min-width: 130px;">
              <label class="suite-input-label">Title</label>
              <input type="text" class="suite-input suite-input-title" data-index="${index}" value="${escapeHtml(item.title)}">
            </div>
            <div class="suite-input-wrap" style="flex: 1; min-width: 100px;">
              <label class="suite-input-label">Badge Text</label>
              <input type="text" class="suite-input suite-input-badge" data-index="${index}" value="${escapeHtml(item.badge || '')}" placeholder="None">
            </div>
            <div class="suite-input-wrap" style="flex: 2; min-width: 160px;">
              <label class="suite-input-label">Subtitle</label>
              <input type="text" class="suite-input suite-input-sub" data-index="${index}" value="${escapeHtml(item.subtitle || '')}">
            </div>
          </div>
          <div class="suite-badge-presets">
            <span style="font-size: 9.5px; color: var(--text-muted); margin-right: 4px;">Presets:</span>
            <button type="button" class="preset-chip" data-index="${index}" data-badge="HOT">HOT</button>
            <button type="button" class="preset-chip" data-index="${index}" data-badge="NEW">NEW</button>
            <button type="button" class="preset-chip" data-index="${index}" data-badge="50% OFF">50% OFF</button>
            <button type="button" class="preset-chip" data-index="${index}" data-badge="FESTIVAL">FESTIVAL</button>
            <button type="button" class="preset-chip" data-index="${index}" data-badge="VIP">VIP</button>
            <button type="button" class="preset-chip" data-index="${index}" data-badge="GEMINI AI">GEMINI AI</button>
            <button type="button" class="preset-chip" data-index="${index}" data-badge="PRO">PRO</button>
            <button type="button" class="preset-chip" data-index="${index}" data-badge="">None</button>
          </div>
        </div>
        <div class="suite-toggle-col">
          <label class="switch">
            <input type="checkbox" class="suite-toggle-checkbox" data-index="${index}" ${item.enabled ? 'checked' : ''}>
            <span class="slider"></span>
          </label>
          <span class="switch-label-status ${item.enabled ? '' : 'disabled'}">${item.enabled ? 'Active' : 'Hidden'}</span>
        </div>
      </div>
    `;
  }).join('');

  attachSuiteCardListeners();
}

function renderSuiteSimulator() {
  const simCarousel = document.getElementById('simCarouselList');
  const simCount = document.getElementById('simCountBadge');
  if (!simCarousel) return;

  const activeItems = suiteItems.filter(i => i.enabled);
  if (simCount) {
    simCount.textContent = `${activeItems.length} TOOLS`;
  }

  if (activeItems.length === 0) {
    simCarousel.innerHTML = `<div style="font-size: 11px; color: var(--text-muted); padding: 16px; text-align: center; width: 100%;">All modules are currently hidden.</div>`;
    return;
  }

  simCarousel.innerHTML = activeItems.map(item => `
    <div class="mini-sim-card">
      <div class="mini-sim-top">
        <span class="mini-sim-emoji">${item.iconEmoji || '⚡'}</span>
        ${item.badge ? `<span class="mini-sim-badge">${escapeHtml(item.badge)}</span>` : ''}
      </div>
      <div>
        <div class="mini-sim-title">${escapeHtml(item.title)}</div>
        <div class="mini-sim-sub">${escapeHtml(item.subtitle || '')}</div>
      </div>
    </div>
  `).join('');
}

function attachSuiteCardListeners() {
  const container = document.getElementById('suiteCardsList');
  if (!container) return;

  // Title input change
  container.querySelectorAll('.suite-input-title').forEach(input => {
    input.addEventListener('input', (e) => {
      const idx = parseInt(e.target.dataset.index, 10);
      suiteItems[idx].title = e.target.value;
      const titleSpan = e.target.closest('.suite-card-item')?.querySelector('.suite-title-text');
      if (titleSpan) titleSpan.textContent = e.target.value;
      renderSuiteSimulator();
    });
  });

  // Badge input change
  container.querySelectorAll('.suite-input-badge').forEach(input => {
    input.addEventListener('input', (e) => {
      const idx = parseInt(e.target.dataset.index, 10);
      suiteItems[idx].badge = e.target.value.trim();
      renderSuiteSimulator();
    });
  });

  // Subtitle input change
  container.querySelectorAll('.suite-input-sub').forEach(input => {
    input.addEventListener('input', (e) => {
      const idx = parseInt(e.target.dataset.index, 10);
      suiteItems[idx].subtitle = e.target.value;
      renderSuiteSimulator();
    });
  });

  // Preset badge buttons
  container.querySelectorAll('.preset-chip').forEach(chip => {
    chip.addEventListener('click', (e) => {
      const idx = parseInt(e.target.dataset.index, 10);
      const badgeVal = e.target.dataset.badge || '';
      suiteItems[idx].badge = badgeVal;
      const badgeInput = container.querySelector(`.suite-input-badge[data-index="${idx}"]`);
      if (badgeInput) badgeInput.value = badgeVal;
      renderSuiteSimulator();
    });
  });

  // Toggle Checkboxes
  container.querySelectorAll('.suite-toggle-checkbox').forEach(chk => {
    chk.addEventListener('change', (e) => {
      const idx = parseInt(e.target.dataset.index, 10);
      const isChecked = e.target.checked;
      suiteItems[idx].enabled = isChecked;
      
      const card = e.target.closest('.suite-card-item');
      if (card) {
        card.classList.toggle('disabled-module', !isChecked);
        const statusSpan = card.querySelector('.switch-label-status');
        if (statusSpan) {
          statusSpan.textContent = isChecked ? 'Active' : 'Hidden';
          statusSpan.classList.toggle('disabled', !isChecked);
        }
      }

      const activeCount = suiteItems.filter(i => i.enabled).length;
      const countBadge = document.getElementById('suiteActiveCountBadge');
      if (countBadge) {
        countBadge.textContent = `${activeCount} / ${suiteItems.length} Modules Live`;
      }
      renderSuiteSimulator();
    });
  });

  // Reorder Up
  container.querySelectorAll('.btn-suite-up').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const idx = parseInt(btn.dataset.index, 10);
      if (idx > 0) {
        const temp = suiteItems[idx];
        suiteItems[idx] = suiteItems[idx - 1];
        suiteItems[idx - 1] = temp;
        updateSuiteOrders();
        renderSuiteCards();
        renderSuiteSimulator();
      }
    });
  });

  // Reorder Down
  container.querySelectorAll('.btn-suite-down').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const idx = parseInt(btn.dataset.index, 10);
      if (idx < suiteItems.length - 1) {
        const temp = suiteItems[idx];
        suiteItems[idx] = suiteItems[idx + 1];
        suiteItems[idx + 1] = temp;
        updateSuiteOrders();
        renderSuiteCards();
        renderSuiteSimulator();
      }
    });
  });

  // HTML5 Drag and Drop
  const cards = container.querySelectorAll('.suite-card-item');
  cards.forEach(card => {
    card.addEventListener('dragstart', (e) => {
      suiteDragSrcIndex = parseInt(card.dataset.index, 10);
      card.classList.add('dragging');
      e.dataTransfer.effectAllowed = 'move';
      e.dataTransfer.setData('text/plain', suiteDragSrcIndex);
    });

    card.addEventListener('dragover', (e) => {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'move';
      card.classList.add('drag-over');
    });

    card.addEventListener('dragleave', () => {
      card.classList.remove('drag-over');
    });

    card.addEventListener('drop', (e) => {
      e.preventDefault();
      card.classList.remove('drag-over');
      const targetIndex = parseInt(card.dataset.index, 10);
      if (suiteDragSrcIndex !== null && suiteDragSrcIndex !== targetIndex) {
        const movedItem = suiteItems.splice(suiteDragSrcIndex, 1)[0];
        suiteItems.splice(targetIndex, 0, movedItem);
        updateSuiteOrders();
        renderSuiteCards();
        renderSuiteSimulator();
      }
    });

    card.addEventListener('dragend', () => {
      card.classList.remove('dragging');
      cards.forEach(c => c.classList.remove('drag-over'));
      suiteDragSrcIndex = null;
    });
  });
}

function updateSuiteOrders() {
  suiteItems.forEach((item, index) => {
    item.order = index;
  });
}

async function savePersonalizationSuite() {
  const saveBtn = document.getElementById('saveSuiteBtn');
  if (saveBtn) {
    saveBtn.disabled = true;
    saveBtn.innerHTML = `
      <svg class="spinner" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10" stroke-opacity="0.25"/>
        <path d="M12 2a10 10 0 0 1 10 10"/>
      </svg>
      Saving...
    `;
  }

  try {
    updateSuiteOrders();
    const res = await authFetch('/api/admin/personalization-suite', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ items: suiteItems })
    });
    const json = await res.json();
    if (json.success) {
      showToast('⚡ Flagship Suite saved & applied live OTA!');
      const items = json.data || json.items;
      if (Array.isArray(items)) {
        suiteItems = items.sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
        renderSuiteCards();
        renderSuiteSimulator();
      }
    } else {
      showToast('Failed to save suite: ' + (json.error || 'Server error'));
    }
  } catch (err) {
    console.error('Error saving suite:', err);
    showToast('Failed to save suite: ' + err.message);
  } finally {
    if (saveBtn) {
      saveBtn.disabled = false;
      saveBtn.innerHTML = `
        <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
        Save & Apply OTA
      `;
    }
  }
}

async function resetPersonalizationSuite() {
  if (!confirm('Are you sure you want to reset all 8 Flagship Suite modules to factory defaults?')) {
    return;
  }

  const resetBtn = document.getElementById('resetSuiteBtn');
  if (resetBtn) resetBtn.disabled = true;

  try {
    const res = await authFetch('/api/admin/personalization-suite/reset', { method: 'POST' });
    const json = await res.json();
    if (json.success) {
      showToast('⚡ Suite reset to factory defaults!');
      const items = json.data || json.items;
      if (Array.isArray(items)) {
        suiteItems = items.sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
        renderSuiteCards();
        renderSuiteSimulator();
      } else {
        loadPersonalizationSuite();
      }
    } else {
      showToast('Reset failed: ' + (json.error || 'Server error'));
    }
  } catch (err) {
    console.error('Error resetting suite:', err);
    showToast('Reset failed: ' + err.message);
  } finally {
    if (resetBtn) resetBtn.disabled = false;
  }
}

function initPersonalizationSuite() {
  const saveBtn = document.getElementById('saveSuiteBtn');
  if (saveBtn) {
    saveBtn.addEventListener('click', savePersonalizationSuite);
  }

  const resetBtn = document.getElementById('resetSuiteBtn');
  if (resetBtn) {
    resetBtn.addEventListener('click', resetPersonalizationSuite);
  }
}

// Window exposures
window.loadPersonalizationSuite = loadPersonalizationSuite;
window.savePersonalizationSuite = savePersonalizationSuite;
window.resetPersonalizationSuite = resetPersonalizationSuite;

// ========================================================
// 🔍 UNIVERSAL QUICK SEARCH & COMMAND PALETTE (Ctrl + K)
// ========================================================
let isCommandPaletteOpen = false;
let paletteSelectedIndex = 0;
let paletteRenderedItems = [];
let paletteActiveFilter = 'all';
let paletteSearchDebounce = null;
let paletteAbortController = null;

const paletteNavTabs = [
  { id: 'overview', title: 'Dashboard Overview', subtitle: 'Live statistics and connected client management', icon: '📊', type: 'navigation' },
  { id: 'wallpapers', title: 'Wallpapers & 3D Studio', subtitle: 'Manage cloud wallpapers, 3D multi-layers, and 4K assets', icon: '🖼️', type: 'navigation' },
  { id: 'banners', title: 'Hero Banners', subtitle: 'Publish auto-sliding promotional banners & festival drops', icon: '🎨', type: 'navigation' },
  { id: 'suite-manager', title: 'Flagship Suite Manager', subtitle: 'OTA control for 8 flagship modules: toggle ON/OFF & order', icon: '⚡', type: 'navigation' },
  { id: 'charging', title: 'Charging Animation Studio', subtitle: 'Upload, customize & simulate battery charging effects', icon: '⚡', type: 'navigation' },
  { id: 'edge-lighting', title: 'Edge Lighting Studio', subtitle: 'Create RGB border neon & camera punch-hole lighting', icon: '🌈', type: 'navigation' },
  { id: 'dynamic-island', title: 'Dynamic Island Studio', subtitle: 'Smart camera notch capsule, music equalizer & alerts', icon: '🏝️', type: 'navigation' },
  { id: 'aod', title: 'Always-On Display (AOD)', subtitle: 'Pitch-black AMOLED battery-saving clocks & widgets', icon: '🕒', type: 'navigation' },
  { id: 'callscreen', title: 'Color Call Screen & Flash', subtitle: '3D animated incoming call themes & flash strobe', icon: '📞', type: 'navigation' },
  { id: 'duo', title: 'Duo Wallpapers Studio', subtitle: 'Curate matching 4K/3D Lock and Home Screen pairs', icon: '👥', type: 'navigation' },
  { id: 'touch-effects', title: 'Touch Fluid & Ripple Effects', subtitle: 'Touch-reactive fluid dye swirls, water ripples & arcs', icon: '👆', type: 'navigation' },
  { id: 'fingerprint', title: 'In-Display Fingerprint FX', subtitle: 'Biometric sensor animations & holographic HUD rings', icon: '🔓', type: 'navigation' },
  { id: 'categories', title: 'Category Management', subtitle: 'Create and organize wallpaper categories & icons', icon: '📁', type: 'navigation' },
  { id: 'ringtones', title: 'Ringtone Catalog', subtitle: 'Upload and preview high-fidelity audio ringtones', icon: '🎵', type: 'navigation' },
  { id: 'notifications', title: 'Push Notification Studio', subtitle: 'Broadcast alerts, wallpaper highlights & daily picks', icon: '🔔', type: 'navigation' },
  { id: 'config', title: 'Remote Config & Weather', subtitle: 'Weather overlays, announcement banners & monetization', icon: '⚡', type: 'navigation' },
  { id: 'devices', title: 'Connected Devices & Radar', subtitle: 'Real-time telemetry, Android OS stats & live radar map', icon: '📱', type: 'navigation' }
];

const paletteQuickActions = [
  {
    id: 'act_upload_wallpaper',
    title: 'Upload Wallpaper',
    subtitle: 'Create a new 4K, static, or 3D Parallax wallpaper',
    icon: '➕',
    badge: 'Upload',
    type: 'action',
    handler: () => openCreateWallpaperModal()
  },
  {
    id: 'act_bulk_upload',
    title: 'Bulk Upload Wallpapers',
    subtitle: 'Upload multiple wallpapers with auto category tagging',
    icon: '⚡',
    badge: 'Bulk',
    type: 'action',
    handler: () => {
      const m = document.getElementById('bulkUploadModal');
      if (m) m.classList.add('active');
    }
  },
  {
    id: 'act_push_notification',
    title: 'Push Notification',
    subtitle: 'Broadcast instant alert or daily highlight to all devices',
    icon: '🔔',
    badge: 'FCM',
    type: 'action',
    handler: () => {
      switchTab('notifications');
      setTimeout(() => {
        const input = document.getElementById('notifTitle');
        if (input) {
          input.focus();
          input.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }, 250);
    }
  },
  {
    id: 'act_export_backup',
    title: 'Export Backup',
    subtitle: 'Download full SQLite database archive snapshot',
    icon: '💾',
    badge: 'Backup',
    type: 'action',
    handler: () => {
      const m = document.getElementById('backupRestoreModal');
      if (m) {
        m.classList.add('active');
        if (typeof loadBackupStats === 'function') loadBackupStats();
      }
    }
  },
  {
    id: 'act_parallax_studio',
    title: '3D Parallax Studio',
    subtitle: 'Open 3D gyroscope depth visualizer & layer composer',
    icon: '🌀',
    badge: '3D Studio',
    type: 'action',
    handler: () => {
      const m = document.getElementById('parallaxStudioModal');
      if (m) m.classList.add('active');
    }
  },
  {
    id: 'act_refresh_all',
    title: 'Refresh All Data',
    subtitle: 'Reload all wallpapers, categories, suite items & stats',
    icon: '🔄',
    badge: 'Sync',
    type: 'action',
    handler: () => {
      loadAllData();
      showToast('⚡ All data refreshed!');
    }
  },
  {
    id: 'act_change_password',
    title: 'Change Admin Password',
    subtitle: 'Update superadmin security credentials',
    icon: '🔑',
    badge: 'Security',
    type: 'action',
    handler: () => {
      const m = document.getElementById('changePasswordModal');
      if (m) m.classList.add('active');
    }
  },
  {
    id: 'act_weather_sim',
    title: 'Dynamic Weather Simulator',
    subtitle: 'Test Rain, Snow, Cyberpunk & Lightning overlays',
    icon: '🌦️',
    badge: 'Weather',
    type: 'action',
    handler: () => switchTab('config')
  },
  {
    id: 'act_devices_radar',
    title: 'Live Devices Telemetry Radar',
    subtitle: 'Inspect connected Android clients & geographic radar',
    icon: '📱',
    badge: 'Radar',
    type: 'action',
    handler: () => switchTab('devices')
  },
  {
    id: 'act_signout',
    title: 'Sign Out of Admin Panel',
    subtitle: 'Terminate current admin session & clear tokens',
    icon: '🚪',
    badge: 'Auth',
    type: 'action',
    handler: (e) => handleSignOut(e || new Event('click'))
  }
];

function initCommandPalette() {
  const modal = document.getElementById('commandPaletteModal');
  const searchInput = document.getElementById('paletteSearchInput');
  const clearBtn = document.getElementById('paletteClearBtn');
  const filterBar = document.getElementById('paletteFilterBar');
  const openBtn = document.getElementById('openCommandPaletteBtn');

  if (!modal || !searchInput) return;

  // Global Keyboard Shortcut: Ctrl + K or Cmd + K
  document.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && (e.key.toLowerCase() === 'k' || e.code === 'KeyK')) {
      e.preventDefault();
      toggleCommandPalette();
    }
  });

  // Top header search button trigger
  if (openBtn) {
    openBtn.addEventListener('click', (e) => {
      e.preventDefault();
      openCommandPalette();
    });
  }

  // Clear button click
  if (clearBtn) {
    clearBtn.addEventListener('click', () => {
      searchInput.value = '';
      clearBtn.style.display = 'none';
      handlePaletteSearch('');
      searchInput.focus();
    });
  }

  // Backdrop click to close
  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      closeCommandPalette();
    }
  });

  // Search input typing & keyboard navigation
  searchInput.addEventListener('input', (e) => {
    const q = e.target.value;
    clearBtn.style.display = q.trim().length > 0 ? 'flex' : 'none';
    clearTimeout(paletteSearchDebounce);
    paletteSearchDebounce = setTimeout(() => {
      handlePaletteSearch(q);
    }, 90);
  });

  searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      movePaletteSelection(1);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      movePaletteSelection(-1);
    } else if (e.key === 'Enter') {
      e.preventDefault();
      executeSelectedPaletteItem();
    } else if (e.key === 'Escape') {
      e.preventDefault();
      closeCommandPalette();
    } else if (e.key === 'Tab') {
      e.preventDefault();
      cyclePaletteFilter(e.shiftKey ? -1 : 1);
    }
  });

  // Category filter pills click
  if (filterBar) {
    filterBar.querySelectorAll('.palette-filter-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        filterBar.querySelectorAll('.palette-filter-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        paletteActiveFilter = pill.dataset.filter || 'all';
        handlePaletteSearch(searchInput.value);
      });
    });
  }
}

function openCommandPalette() {
  const modal = document.getElementById('commandPaletteModal');
  const searchInput = document.getElementById('paletteSearchInput');
  const clearBtn = document.getElementById('paletteClearBtn');
  if (!modal || !searchInput) return;

  isCommandPaletteOpen = true;
  modal.classList.add('active');
  searchInput.value = '';
  if (clearBtn) clearBtn.style.display = 'none';

  // Render initial default suggestions
  handlePaletteSearch('');

  setTimeout(() => {
    searchInput.focus();
    searchInput.select();
  }, 50);
}

function closeCommandPalette() {
  const modal = document.getElementById('commandPaletteModal');
  if (!modal) return;
  modal.classList.remove('active');
  isCommandPaletteOpen = false;
  if (paletteAbortController) {
    paletteAbortController.abort();
    paletteAbortController = null;
  }
}

function toggleCommandPalette() {
  if (isCommandPaletteOpen) {
    closeCommandPalette();
  } else {
    openCommandPalette();
  }
}

function cyclePaletteFilter(direction = 1) {
  const filterBar = document.getElementById('paletteFilterBar');
  if (!filterBar) return;
  const pills = Array.from(filterBar.querySelectorAll('.palette-filter-pill'));
  if (!pills.length) return;

  let currentIndex = pills.findIndex(p => p.classList.contains('active'));
  if (currentIndex === -1) currentIndex = 0;

  let nextIndex = (currentIndex + direction + pills.length) % pills.length;
  pills[nextIndex].click();
}

// Highlight helper
function highlightPaletteMatch(text, query) {
  if (!query || !text) return escapeHtml(text || '');
  const escapedText = escapeHtml(text);
  const escapedQuery = escapeHtml(query);
  const regex = new RegExp(`(${escapedQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
  return escapedText.replace(regex, '<span class="palette-highlight">$1</span>');
}

async function handlePaletteSearch(rawQuery) {
  const container = document.getElementById('paletteResultsContainer');
  if (!container) return;

  const query = (rawQuery || '').trim();
  const lowerQuery = query.toLowerCase();

  // If query is empty: show quick actions + navigation tabs + top wallpapers
  if (!query) {
    renderDefaultPaletteSuggestions();
    return;
  }

  // Cancel any ongoing search request
  if (paletteAbortController) {
    paletteAbortController.abort();
  }
  paletteAbortController = new AbortController();

  // 1. Instantly filter local Quick Actions & Navigation tabs (zero latency)
  const matchedActions = (paletteActiveFilter === 'all' || paletteActiveFilter === 'actions')
    ? paletteQuickActions.filter(a => a.title.toLowerCase().includes(lowerQuery) || a.subtitle.toLowerCase().includes(lowerQuery) || a.id.toLowerCase().includes(lowerQuery))
    : [];

  const matchedTabs = (paletteActiveFilter === 'all' || paletteActiveFilter === 'navigation')
    ? paletteNavTabs.filter(t => t.title.toLowerCase().includes(lowerQuery) || t.subtitle.toLowerCase().includes(lowerQuery) || t.id.toLowerCase().includes(lowerQuery))
    : [];

  // Show immediate local results while backend runs
  renderPaletteSearchResults({
    query,
    actions: matchedActions,
    navigation: matchedTabs,
    serverResults: null,
    isLoading: true
  });

  // 2. Query backend omni-search for database items (wallpapers, themes, AOD, ringtones, etc.)
  try {
    const res = await authFetch(`/api/admin/omni-search?q=${encodeURIComponent(query)}&type=${encodeURIComponent(paletteActiveFilter)}`, {
      signal: paletteAbortController.signal
    });

    if (res.ok) {
      const json = await res.json();
      if (json.success) {
        renderPaletteSearchResults({
          query,
          actions: matchedActions,
          navigation: matchedTabs,
          serverResults: json.results || {},
          isLoading: false
        });
      }
    }
  } catch (err) {
    if (err.name !== 'AbortError') {
      console.warn('Omni search fetch error:', err);
      // Still show local results
      renderPaletteSearchResults({
        query,
        actions: matchedActions,
        navigation: matchedTabs,
        serverResults: {},
        isLoading: false
      });
    }
  }
}

function renderDefaultPaletteSuggestions() {
  const container = document.getElementById('paletteResultsContainer');
  if (!container) return;

  paletteRenderedItems = [];
  let html = '';

  // Quick Actions Section
  if (paletteActiveFilter === 'all' || paletteActiveFilter === 'actions') {
    html += `<div class="palette-section-header">⚡ Quick Actions <span class="badge-count">${paletteQuickActions.length}</span></div>`;
    paletteQuickActions.forEach(act => {
      const itemIndex = paletteRenderedItems.length;
      paletteRenderedItems.push({
        id: act.id,
        type: 'action',
        handler: act.handler
      });
      html += `
        <div class="palette-item ${itemIndex === 0 ? 'active' : ''}" data-index="${itemIndex}" onclick="triggerPaletteItem(${itemIndex})">
          <div class="palette-item-icon action">${act.icon}</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${escapeHtml(act.title)}</span>
              <span class="badge badge-cyan" style="font-size:10px; padding:1px 6px;">${act.badge}</span>
            </div>
            <div class="palette-item-sub">${escapeHtml(act.subtitle)}</div>
          </div>
          <span class="palette-item-action">Run ↵</span>
        </div>
      `;
    });
  }

  // Navigation Tabs Section
  if (paletteActiveFilter === 'all' || paletteActiveFilter === 'navigation') {
    html += `<div class="palette-section-header" style="margin-top:8px;">🧭 Jump to Tab <span class="badge-count">${paletteNavTabs.length}</span></div>`;
    paletteNavTabs.forEach(tab => {
      const itemIndex = paletteRenderedItems.length;
      paletteRenderedItems.push({
        id: tab.id,
        type: 'navigation',
        handler: () => switchTab(tab.id)
      });
      html += `
        <div class="palette-item" data-index="${itemIndex}" onclick="triggerPaletteItem(${itemIndex})">
          <div class="palette-item-icon nav">${tab.icon}</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${escapeHtml(tab.title)}</span>
            </div>
            <div class="palette-item-sub">${escapeHtml(tab.subtitle)}</div>
          </div>
          <span class="palette-item-action">Jump ↵</span>
        </div>
      `;
    });
  }

  // Recent Curated Wallpapers from memory
  if ((paletteActiveFilter === 'all' || paletteActiveFilter === 'wallpapers') && allWallpapers.length > 0) {
    const recent = allWallpapers.slice(0, 6);
    html += `<div class="palette-section-header" style="margin-top:8px;">🖼️ Wallpapers & 3D <span class="badge-count">${allWallpapers.length}</span></div>`;
    recent.forEach(wp => {
      const itemIndex = paletteRenderedItems.length;
      paletteRenderedItems.push({
        id: wp.id,
        type: 'wallpaper',
        handler: () => openEditWallpaperModal(wp.id)
      });
      html += `
        <div class="palette-item" data-index="${itemIndex}" onclick="triggerPaletteItem(${itemIndex})">
          <img src="${wp.previewUrl}" class="palette-item-thumb" onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'38\\' height=\\'38\\' fill=\\'%23334155\\'><rect width=\\'38\\' height=\\'38\\'/></svg>'">
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${escapeHtml(wp.title)}</span>
              ${wp.isParallax ? '<span class="badge badge-purple" style="font-size:9.5px; padding:0 5px;">3D</span>' : ''}
              ${wp.isPremium ? '<span class="badge badge-orange" style="font-size:9.5px; padding:0 5px;">PRO</span>' : ''}
            </div>
            <div class="palette-item-sub">
              <span>${escapeHtml(wp.category)}</span> &bull; 
              <span class="palette-item-id-badge">ID: ${escapeHtml(wp.id)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ↵</span>
        </div>
      `;
    });
  }

  container.innerHTML = html;
  paletteSelectedIndex = 0;
  updatePaletteActiveHighlight();
}

function renderPaletteSearchResults({ query, actions, navigation, serverResults, isLoading }) {
  const container = document.getElementById('paletteResultsContainer');
  if (!container) return;

  paletteRenderedItems = [];
  let html = '';

  const results = serverResults || {};
  const wallpapers = results.wallpapers || [];
  const island = results.island || [];
  const aod = results.aod || [];
  const charging = results.charging || [];
  const edge = results.edge || [];
  const callscreen = results.callscreen || [];
  const duo = results.duo || [];
  const touch = results.touch || [];
  const fingerprint = results.fingerprint || [];
  const ringtones = results.ringtones || [];
  const categories = results.categories || [];
  const banners = results.banners || [];

  const totalResultsCount = actions.length + navigation.length + wallpapers.length + island.length + aod.length + charging.length + edge.length + callscreen.length + duo.length + touch.length + fingerprint.length + ringtones.length + categories.length + banners.length;

  if (totalResultsCount === 0 && !isLoading) {
    container.innerHTML = `
      <div class="palette-empty-state">
        <div class="palette-empty-icon">🔍</div>
        <div class="palette-empty-title">No matching results found</div>
        <div class="palette-empty-sub">No wallpaper, theme, AOD clock, ringtone or command matches "<strong>${escapeHtml(query)}</strong>"</div>
      </div>
    `;
    return;
  }

  // 1. Quick Actions
  if (actions.length > 0) {
    html += `<div class="palette-section-header">⚡ Quick Actions <span class="badge-count">${actions.length}</span></div>`;
    actions.forEach(act => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: act.id, type: 'action', handler: act.handler });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon action">${act.icon}</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(act.title, query)}</span>
              <span class="badge badge-cyan" style="font-size:10px; padding:1px 6px;">${act.badge}</span>
            </div>
            <div class="palette-item-sub">${highlightPaletteMatch(act.subtitle, query)}</div>
          </div>
          <span class="palette-item-action">Run ↵</span>
        </div>
      `;
    });
  }

  // 2. Navigation Tabs
  if (navigation.length > 0) {
    html += `<div class="palette-section-header">🧭 Navigation Tabs <span class="badge-count">${navigation.length}</span></div>`;
    navigation.forEach(tab => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: tab.id, type: 'navigation', handler: () => switchTab(tab.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon nav">${tab.icon}</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(tab.title, query)}</span>
            </div>
            <div class="palette-item-sub">${highlightPaletteMatch(tab.subtitle, query)}</div>
          </div>
          <span class="palette-item-action">Jump ↵</span>
        </div>
      `;
    });
  }

  // 3. Wallpapers & 3D Parallax
  if (wallpapers.length > 0) {
    html += `<div class="palette-section-header">🖼️ Wallpapers & 3D <span class="badge-count">${wallpapers.length}</span></div>`;
    wallpapers.forEach(wp => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: wp.id, type: 'wallpaper', handler: () => openEditWallpaperModal(wp.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <img src="${wp.previewUrl}" class="palette-item-thumb" onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'38\\' height=\\'38\\' fill=\\'%23334155\\'><rect width=\\'38\\' height=\\'38\\'/></svg>'">
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(wp.title, query)}</span>
              ${wp.isParallax ? '<span class="badge badge-purple" style="font-size:9.5px; padding:0 5px;">3D</span>' : ''}
              ${wp.isPremium ? '<span class="badge badge-orange" style="font-size:9.5px; padding:0 5px;">PRO</span>' : ''}
            </div>
            <div class="palette-item-sub">
              <span>${highlightPaletteMatch(wp.category, query)}</span> &bull; 
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(wp.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // 4. Dynamic Island Themes
  if (island.length > 0) {
    html += `<div class="palette-section-header">🏝️ Dynamic Island Themes <span class="badge-count">${island.length}</span></div>`;
    island.forEach(item => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: item.id, type: 'island', handler: () => openEditIslandModal(item.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon action" style="background:${item.accentColor ? item.accentColor + '25' : 'rgba(0,229,255,0.15)'};">🏝️</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(item.title, query)}</span>
              <span class="badge badge-cyan" style="font-size:9.5px; padding:0 5px;">${item.styleType || 'island'}</span>
            </div>
            <div class="palette-item-sub">
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(item.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // 5. Always-On Display (AOD) Clocks
  if (aod.length > 0) {
    html += `<div class="palette-section-header">🕒 Always-On Display Clocks <span class="badge-count">${aod.length}</span></div>`;
    aod.forEach(clock => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: clock.id, type: 'aod', handler: () => openEditAodModal(clock.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon action">🕒</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(clock.title, query)}</span>
              <span class="badge badge-purple" style="font-size:9.5px; padding:0 5px;">${clock.clockType || 'AOD'}</span>
            </div>
            <div class="palette-item-sub">
              <span>Dial: ${clock.dialStyle || 'Default'}</span> &bull;
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(clock.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // 6. Charging Animations
  if (charging.length > 0) {
    html += `<div class="palette-section-header">⚡ Charging Animations <span class="badge-count">${charging.length}</span></div>`;
    charging.forEach(anim => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: anim.id, type: 'charging', handler: () => openEditChargingModal(anim.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon action">⚡</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(anim.title, query)}</span>
              <span class="badge badge-cyan" style="font-size:9.5px; padding:0 5px;">${anim.category || 'NEON'}</span>
            </div>
            <div class="palette-item-sub">
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(anim.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // 7. Edge Lighting Presets
  if (edge.length > 0) {
    html += `<div class="palette-section-header">🌈 Edge Lighting Presets <span class="badge-count">${edge.length}</span></div>`;
    edge.forEach(p => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: p.id, type: 'edge', handler: () => openEditEdgeModal(p.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon action">🌈</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(p.title, query)}</span>
              <span class="badge badge-cyan" style="font-size:9.5px; padding:0 5px;">${p.category || 'RGB'}</span>
            </div>
            <div class="palette-item-sub">
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(p.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // 8. Color Call Screen Themes
  if (callscreen.length > 0) {
    html += `<div class="palette-section-header">📞 Color Call Screen Themes <span class="badge-count">${callscreen.length}</span></div>`;
    callscreen.forEach(t => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: t.id, type: 'callscreen', handler: () => openEditCallModal(t.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon action">📞</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(t.title, query)}</span>
              <span class="badge badge-purple" style="font-size:9.5px; padding:0 5px;">${t.category || 'CALL'}</span>
            </div>
            <div class="palette-item-sub">
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(t.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // 9. Duo Wallpapers
  if (duo.length > 0) {
    html += `<div class="palette-section-header">👥 Duo Wallpapers <span class="badge-count">${duo.length}</span></div>`;
    duo.forEach(pair => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: pair.id, type: 'duo', handler: () => openEditDuoModal(pair.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon action">👥</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(pair.title, query)}</span>
              <span class="badge badge-cyan" style="font-size:9.5px; padding:0 5px;">${pair.category || 'DUO'}</span>
            </div>
            <div class="palette-item-sub">
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(pair.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // 10. Touch Effects & Fluid
  if (touch.length > 0) {
    html += `<div class="palette-section-header">👆 Touch Effects Presets <span class="badge-count">${touch.length}</span></div>`;
    touch.forEach(tp => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: tp.id, type: 'touch', handler: () => openEditTouchModal(tp.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon action">👆</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(tp.title, query)}</span>
              <span class="badge badge-purple" style="font-size:9.5px; padding:0 5px;">${tp.effectType || 'Fluid'}</span>
            </div>
            <div class="palette-item-sub">
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(tp.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // 11. Fingerprint Presets
  if (fingerprint.length > 0) {
    html += `<div class="palette-section-header">🔓 Fingerprint Presets <span class="badge-count">${fingerprint.length}</span></div>`;
    fingerprint.forEach(fp => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({ id: fp.id, type: 'fingerprint', handler: () => openEditFingerprintModal(fp.id) });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon action">🔓</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(fp.title, query)}</span>
              <span class="badge badge-cyan" style="font-size:9.5px; padding:0 5px;">${fp.animationType || 'Fingerprint'}</span>
            </div>
            <div class="palette-item-sub">
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(fp.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // 12. Ringtones
  if (ringtones.length > 0) {
    html += `<div class="palette-section-header">🎵 Audio Ringtones <span class="badge-count">${ringtones.length}</span></div>`;
    ringtones.forEach(rt => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({
        id: rt.id,
        type: 'ringtone',
        handler: () => {
          switchTab('ringtones');
          setTimeout(() => {
            toggleAudioPreview(rt.id, rt.audioUrl);
          }, 200);
        }
      });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <div class="palette-item-icon audio">🎵</div>
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(rt.title, query)}</span>
              <span class="badge badge-cyan" style="font-size:9.5px; padding:0 5px;">${rt.durationSeconds || 30}s</span>
            </div>
            <div class="palette-item-sub">
              <span>${highlightPaletteMatch(rt.artist, query)} &bull; ${highlightPaletteMatch(rt.category, query)}</span> &bull; 
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(rt.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Play ▶️</span>
        </div>
      `;
    });
  }

  // 13. Categories
  if (categories.length > 0) {
    html += `<div class="palette-section-header">📁 Categories <span class="badge-count">${categories.length}</span></div>`;
    categories.forEach(cat => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({
        id: cat.id,
        type: 'category',
        handler: () => {
          switchTab('categories');
          setTimeout(() => {
            const catRow = Array.from(document.querySelectorAll('.category-row')).find(r => r.textContent.includes(cat.id));
            if (catRow) {
              catRow.scrollIntoView({ behavior: 'smooth', block: 'center' });
              catRow.style.outline = '2px solid var(--accent-cyan)';
              setTimeout(() => { catRow.style.outline = ''; }, 2000);
            }
          }, 200);
        }
      });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <img src="${cat.iconUrl || 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png'}" class="palette-item-thumb" onerror="this.src='https://cdn-icons-png.flaticon.com/512/3135/3135715.png'">
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(cat.name, query)}</span>
            </div>
            <div class="palette-item-sub">
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(cat.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">View 📁</span>
        </div>
      `;
    });
  }

  // 14. Hero Banners
  if (banners.length > 0) {
    html += `<div class="palette-section-header">🎨 Hero Banners <span class="badge-count">${banners.length}</span></div>`;
    banners.forEach(b => {
      const idx = paletteRenderedItems.length;
      paletteRenderedItems.push({
        id: b.id,
        type: 'banner',
        handler: () => {
          if (typeof window.editBanner === 'function') {
            window.editBanner(b.id);
          }
        }
      });
      html += `
        <div class="palette-item" data-index="${idx}" onclick="triggerPaletteItem(${idx})">
          <img src="${b.imageUrl}" class="palette-item-thumb" onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'38\\' height=\\'38\\' fill=\\'%23334155\\'><rect width=\\'38\\' height=\\'38\\'/></svg>'">
          <div class="palette-item-info">
            <div class="palette-item-title-row">
              <span class="palette-item-title">${highlightPaletteMatch(b.title, query)}</span>
              ${b.badgeText ? `<span class="badge badge-pink" style="font-size:9.5px; padding:0 5px;">${escapeHtml(b.badgeText)}</span>` : ''}
            </div>
            <div class="palette-item-sub">
              <span>${highlightPaletteMatch(b.subtitle || '', query)}</span> &bull;
              <span class="palette-item-id-badge">ID: ${highlightPaletteMatch(b.id, query)}</span>
            </div>
          </div>
          <span class="palette-item-action">Edit ✏️</span>
        </div>
      `;
    });
  }

  // If loading spinner still active in background
  if (isLoading) {
    html += `
      <div class="palette-loading-state">
        <div class="palette-spinner"></div>
        <span>Searching database across 16 modules...</span>
      </div>
    `;
  }

  container.innerHTML = html;
  paletteSelectedIndex = 0;
  updatePaletteActiveHighlight();
}

function movePaletteSelection(delta) {
  if (paletteRenderedItems.length === 0) return;
  paletteSelectedIndex = (paletteSelectedIndex + delta + paletteRenderedItems.length) % paletteRenderedItems.length;
  updatePaletteActiveHighlight();
}

function updatePaletteActiveHighlight() {
  const container = document.getElementById('paletteResultsContainer');
  if (!container) return;

  const items = container.querySelectorAll('.palette-item');
  items.forEach((item, index) => {
    const isSelected = index === paletteSelectedIndex;
    item.classList.toggle('active', isSelected);
    if (isSelected) {
      item.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  });
}

function triggerPaletteItem(index) {
  paletteSelectedIndex = index;
  executeSelectedPaletteItem();
}

function executeSelectedPaletteItem() {
  if (paletteSelectedIndex < 0 || paletteSelectedIndex >= paletteRenderedItems.length) return;
  const item = paletteRenderedItems[paletteSelectedIndex];
  if (!item) return;

  closeCommandPalette();

  if (typeof item.handler === 'function') {
    try {
      item.handler();
    } catch (err) {
      console.error('Error executing palette item handler:', err);
    }
  }
}

// Global exposure
window.openCommandPalette = openCommandPalette;
window.closeCommandPalette = closeCommandPalette;
window.toggleCommandPalette = toggleCommandPalette;
window.triggerPaletteItem = triggerPaletteItem;

// =========================================================================
// 📲 ADB 1-CLICK LIVE TEST TO DEVICE (I2212)
// =========================================================================
let currentAdbStatus = { connected: false, devices: [], primaryDevice: null };

async function checkAdbDeviceStatus(showToastOnCheck = false) {
  const badge = document.getElementById('adbDeviceStatusBadge');
  const dot = document.getElementById('adbStatusDot');
  const label = document.getElementById('adbDeviceLabel');

  try {
    const res = await authFetch('/api/admin/adb/status');
    const json = await res.json();
    if (json.success) {
      currentAdbStatus = json;
      const isConnected = Boolean(json.connected && json.primaryDevice);
      const dev = json.primaryDevice;

      if (badge) {
        badge.classList.toggle('disconnected', !isConnected);
        badge.title = isConnected
          ? `🟢 Connected: ${dev.displayName} (${dev.id})\nClick to refresh device status.`
          : '🔴 No ADB device connected.\nConnect vivo I2212 via USB with USB Debugging enabled.';
      }
      if (dot) {
        dot.classList.toggle('disconnected', !isConnected);
        dot.classList.toggle('pulsing', isConnected);
      }
      if (label) {
        label.textContent = isConnected
          ? `📱 ${dev.displayName || 'I2212 Online'}`
          : '📱 No ADB Device';
      }

      if (showToastOnCheck) {
        if (isConnected) {
          showToast(`🟢 Connected to ${dev.displayName} (${dev.id})! Ready for Live Test.`);
        } else {
          showToast('⚠️ No ADB Device detected. Please check USB debugging on your phone.');
        }
      }
    }
  } catch (err) {
    console.warn('[ADB] Failed to check device status:', err);
    if (badge) badge.classList.add('disconnected');
    if (dot) dot.classList.add('disconnected');
    if (label) label.textContent = '📱 ADB Offline';
  }
}

async function pushToDevice(type, id, event = null) {
  if (event && event.stopPropagation) {
    event.stopPropagation();
  }

  const btn = event ? event.currentTarget : null;
  const originalHtml = btn ? btn.innerHTML : '';

  if (btn) {
    btn.classList.add('pushing');
    btn.innerHTML = '📲 Pushing...';
  }

  try {
    const res = await authFetch('/api/admin/adb/push-live-test', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ type, id })
    });

    const json = await res.json();

    if (json.success) {
      if (btn) {
        btn.classList.remove('pushing');
        btn.classList.add('success');
        btn.innerHTML = '✅ Live on Phone!';
        setTimeout(() => {
          btn.classList.remove('success');
          btn.innerHTML = originalHtml;
        }, 2500);
      }
      showToast(json.message || `🚀 Live Test pushed to ${json.device?.displayName || 'Phone'}!`);
    } else {
      if (btn) {
        btn.classList.remove('pushing');
        btn.innerHTML = originalHtml;
      }
      showToast(`⚠️ Live Test error: ${json.error || 'Failed to push'}`);
    }
  } catch (err) {
    console.error('Push live test error:', err);
    if (btn) {
      btn.classList.remove('pushing');
      btn.innerHTML = originalHtml;
    }
    showToast(`⚠️ Push error: ${err.message}`);
  }
}

function pushActiveStudioWallpaperToDevice(event) {
  const wpId = (typeof studioActiveWp !== 'undefined' && studioActiveWp) ? studioActiveWp.id : (document.getElementById('studioWallpaperSelect')?.value || null);
  if (!wpId) {
    showToast('⚠️ Please select a wallpaper in the studio first.');
    return;
  }
  pushToDevice('wallpaper', wpId, event);
}

function pushActiveCallThemeToDevice(event) {
  const themeId = (typeof activeCallSimTheme !== 'undefined' && activeCallSimTheme) ? activeCallSimTheme.id : (allCallThemes[0]?.id || null);
  if (!themeId) {
    showToast('⚠️ Please select a Call Theme first.');
    return;
  }
  pushToDevice('call_screen', themeId, event);
}

function pushActiveAodClockToDevice(event) {
  const clockId = (typeof activeAodSimClock !== 'undefined' && activeAodSimClock) ? activeAodSimClock.id : (allAodClocks[0]?.id || null);
  if (!clockId) {
    showToast('⚠️ Please select an AOD Clock Face first.');
    return;
  }
  pushToDevice('aod', clockId, event);
}

// Global exposure
window.checkAdbDeviceStatus = checkAdbDeviceStatus;
window.pushToDevice = pushToDevice;
window.pushActiveStudioWallpaperToDevice = pushActiveStudioWallpaperToDevice;
window.pushActiveCallThemeToDevice = pushActiveCallThemeToDevice;
window.pushActiveAodClockToDevice = pushActiveAodClockToDevice;

// ========================================================
// 🏷️ FESTIVAL & EVENT BANNER SCHEDULER STUDIO
// ========================================================

const FESTIVAL_PRESETS = {
  diwali: {
    name: 'Diwali Festival of Lights 2026',
    festivalKey: 'diwali',
    bannerBadge: '🪔 DIWALI SPECIAL 2026',
    bannerTitle: 'Shubh Deepavali 3D Lights',
    bannerSubtitle: 'Exclusive Parallax, Live Diya & Golden Fireworks Themes',
    bannerImageUrl: 'https://images.unsplash.com/photo-1605705658649-6f17666249be?auto=format&fit=crop&w=1200&q=80',
    targetCategory: 'Festivals & Celebrations',
    targetTags: '#DiwaliSpecial, #3DFireworks, #HappyDiwali, #DiyaLive, #GoldenSparkles',
    startDateOffsetDays: 0,
    durationDays: 7,
    priority: 10
  },
  navratri: {
    name: 'Navratri Garba Mahotsav 2026',
    festivalKey: 'navratri',
    bannerBadge: '💃 DANDIYA & GARBA NIGHTS',
    bannerTitle: 'Navratri Special 3D Beats',
    bannerSubtitle: 'Vibrant Dandiya, Maa Durga Blessings & Glowing Chaniya Choli FX',
    bannerImageUrl: 'https://images.unsplash.com/photo-1598387993441-a364f854c3e1?auto=format&fit=crop&w=1200&q=80',
    targetCategory: 'Festivals & Celebrations',
    targetTags: '#NavratriSpecial, #GarbaLive, #DandiyaNight, #MaaDurga, #Chogada',
    startDateOffsetDays: 14,
    durationDays: 10,
    priority: 10
  },
  newyear: {
    name: 'Happy New Year 2027 Celebration',
    festivalKey: 'newyear',
    bannerBadge: '🎆 HAPPY NEW YEAR 2027',
    bannerTitle: 'Midnight Countdown & Neon Sparks',
    bannerSubtitle: 'Futuristic 2027 Neon Wallpapers, Sparkling City Skylines & Clocks',
    bannerImageUrl: 'https://images.unsplash.com/photo-1467810563316-b5476525c0f9?auto=format&fit=crop&w=1200&q=80',
    targetCategory: 'Festivals & Celebrations',
    targetTags: '#NewYear2027, #MidnightSpark, #HappyNewYear, #Neon2027, #Celebration',
    startDateOffsetDays: 95,
    durationDays: 5,
    priority: 10
  },
  uttarayan: {
    name: 'Makar Sankranti & Uttarayan 2027',
    festivalKey: 'uttarayan',
    bannerBadge: '🪁 KITE FESTIVAL 2027',
    bannerTitle: 'Kai Po Che! Sky High 3D Kites',
    bannerSubtitle: 'Sky Full of Colorful Kites, Tukkal Night Lanterns & Parallax Clouds',
    bannerImageUrl: 'https://images.unsplash.com/photo-1516483638261-f4dbaf036963?auto=format&fit=crop&w=1200&q=80',
    targetCategory: 'Festivals & Celebrations',
    targetTags: '#UttarayanSpecial, #KaiPoChe, #KiteFestival, #MakarSankranti, #Sky3D',
    startDateOffsetDays: 110,
    durationDays: 4,
    priority: 10
  },
  holi: {
    name: 'Holi & Dhuleti Color Splash 2027',
    festivalKey: 'holi',
    bannerBadge: '🎨 COLOR SPLASH FESTIVAL',
    bannerTitle: 'Rang Barse 3D Fluid Gulal',
    bannerSubtitle: 'Interactive Touch Powder Bursts, Vibrant Color Clouds & Fluid Water FX',
    bannerImageUrl: 'https://images.unsplash.com/photo-1551818255-e6e10975bc17?auto=format&fit=crop&w=1200&q=80',
    targetCategory: 'Festivals & Celebrations',
    targetTags: '#HoliSpecial, #ColorSplash, #RangBarse, #DhuletiLive, #GulalBurst',
    startDateOffsetDays: 165,
    durationDays: 3,
    priority: 10
  },
  christmas: {
    name: 'Merry Christmas & Winter Wonderland',
    festivalKey: 'christmas',
    bannerBadge: '🎄 MERRY CHRISTMAS',
    bannerTitle: 'Winter Wonderland & Snowy Nights',
    bannerSubtitle: 'Gentle Snowfall, Glowing Christmas Trees & Santa Parallax Magic',
    bannerImageUrl: 'https://images.unsplash.com/photo-1543589077-47d81606c1bf?auto=format&fit=crop&w=1200&q=80',
    targetCategory: 'Festivals & Celebrations',
    targetTags: '#MerryChristmas, #Snowfall3D, #XmasMagic, #WinterWonderland, #SantaLive',
    startDateOffsetDays: 85,
    durationDays: 7,
    priority: 10
  }
};

function initFestivalScheduler() {
  const addBtn = document.getElementById('openAddFestivalModalBtn');
  const refreshBtn = document.getElementById('refreshFestivalsBtn');
  const closeBtn = document.getElementById('closeFestivalModalBtn');
  const cancelBtn = document.getElementById('cancelFestivalModalBtn');
  const modal = document.getElementById('festivalEventModal');
  const form = document.getElementById('festivalEventForm');
  const searchInput = document.getElementById('festivalSearchInput');
  const statusFilter = document.getElementById('festivalStatusFilter');
  const imgUrlInput = document.getElementById('festivalBannerImageUrlInput');
  const fileInput = document.getElementById('festivalBannerImageFileInput');

  if (addBtn) {
    addBtn.addEventListener('click', () => {
      openAddFestivalModal();
    });
  }

  [closeBtn, cancelBtn].forEach(btn => {
    if (btn) {
      btn.addEventListener('click', () => {
        closeFestivalModal();
      });
    }
  });

  if (refreshBtn) {
    refreshBtn.addEventListener('click', () => {
      loadFestivalEvents();
      showToast('Festival events refreshed!');
    });
  }

  if (searchInput) {
    searchInput.addEventListener('input', () => {
      renderFestivalEventsTable();
    });
  }

  if (statusFilter) {
    statusFilter.addEventListener('change', () => {
      renderFestivalEventsTable();
    });
  }

  if (imgUrlInput) {
    imgUrlInput.addEventListener('input', () => {
      const val = imgUrlInput.value.trim();
      const prevBox = document.getElementById('festivalBannerImagePreviewBox');
      const prevImg = document.getElementById('festivalBannerImagePreviewImg');
      if (val && prevBox && prevImg) {
        prevImg.src = val;
        prevBox.style.display = 'block';
      } else if (prevBox) {
        prevBox.style.display = 'none';
      }
    });
  }

  if (fileInput) {
    fileInput.addEventListener('change', () => {
      if (fileInput.files && fileInput.files[0]) {
        const reader = new FileReader();
        reader.onload = (e) => {
          const prevBox = document.getElementById('festivalBannerImagePreviewBox');
          const prevImg = document.getElementById('festivalBannerImagePreviewImg');
          if (prevImg && prevBox) {
            prevImg.src = e.target.result;
            prevBox.style.display = 'block';
          }
        };
        reader.readAsDataURL(fileInput.files[0]);
      }
    });
  }

  if (form) {
    form.addEventListener('submit', handleFestivalFormSubmit);
  }
}

async function loadFestivalEvents() {
  try {
    const res = await authFetch('/api/admin/events');
    const json = await res.json();
    if (json.success && Array.isArray(json.data)) {
      allFestivalEvents = json.data;
      updateFestivalStatsCounters(json.summary, json.stats);
      renderFestivalEventsTable();
    } else {
      console.warn('Failed to load festival events:', json);
    }
  } catch (err) {
    console.error('Error fetching festival events:', err);
    showToast(`⚠️ Failed to load festival events: ${err.message}`);
  }
}

function updateFestivalStatsCounters(summary, stats) {
  const s = stats || summary || {};
  const liveEl = document.getElementById('statFestivalLiveCount');
  const upEl = document.getElementById('statFestivalUpcomingCount');
  const expEl = document.getElementById('statFestivalExpiredCount');
  const totEl = document.getElementById('statFestivalTotalCount');

  const live = s.active !== undefined ? s.active : (s.liveCount !== undefined ? s.liveCount : allFestivalEvents.filter(e => e.liveStatus === 'LIVE_NOW').length);
  const upcoming = s.upcoming !== undefined ? s.upcoming : (s.upcomingCount !== undefined ? s.upcomingCount : allFestivalEvents.filter(e => e.liveStatus === 'UPCOMING').length);
  const expired = s.expired !== undefined ? s.expired : (s.expiredCount !== undefined ? s.expiredCount : allFestivalEvents.filter(e => e.liveStatus === 'EXPIRED').length);
  const total = s.total !== undefined ? s.total : allFestivalEvents.length;

  if (liveEl) liveEl.textContent = live;
  if (upEl) upEl.textContent = upcoming;
  if (expEl) expEl.textContent = expired;
  if (totEl) totEl.textContent = total;
}

function renderFestivalEventsTable() {
  const tbody = document.getElementById('festivalEventsTableBody');
  if (!tbody) return;

  const searchKeyword = (document.getElementById('festivalSearchInput')?.value || '').toLowerCase().trim();
  const statusFilter = document.getElementById('festivalStatusFilter')?.value || 'ALL';

  let filtered = allFestivalEvents.filter(item => {
    if (statusFilter !== 'ALL' && item.liveStatus !== statusFilter) {
      return false;
    }
    if (searchKeyword) {
      const matchName = (item.name || '').toLowerCase().includes(searchKeyword);
      const matchKey = (item.festivalKey || '').toLowerCase().includes(searchKeyword);
      const matchTitle = (item.bannerTitle || '').toLowerCase().includes(searchKeyword);
      const matchCat = (item.targetCategory || '').toLowerCase().includes(searchKeyword);
      const matchTags = (Array.isArray(item.targetTags) ? item.targetTags.join(' ') : (item.targetTags || '')).toLowerCase().includes(searchKeyword);
      return matchName || matchKey || matchTitle || matchCat || matchTags;
    }
    return true;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 36px 16px; color: #94a3b8;">
          <div style="font-size: 28px; margin-bottom: 8px;">🏷️</div>
          <div style="font-weight: 600; color: #cbd5e1;">No festival events found</div>
          <div style="font-size: 12px; margin-top: 4px;">Click "+ Create Festival Event" or select a 1-Click Preset to schedule an event.</div>
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = filtered.map(ev => {
    // Status Badge
    let statusBadgeHtml = '';
    let countdownHtml = '';

    if (!ev.isActive) {
      statusBadgeHtml = `<span class="status-badge-paused">⏸️ Paused</span>`;
      countdownHtml = `<span class="event-countdown-pill" style="color:#f87171;">Event Disabled</span>`;
    } else if (ev.forceLive) {
      statusBadgeHtml = `<span class="status-badge-live"><span class="countdown-live-pulse"></span> 🟢 FORCE LIVE</span>`;
      countdownHtml = `<span class="event-countdown-pill" style="color:#fbbf24;">⚡ Test Override Active</span>`;
    } else if (ev.liveStatus === 'LIVE_NOW') {
      statusBadgeHtml = `<span class="status-badge-live"><span class="countdown-live-pulse"></span> 🟢 LIVE NOW</span>`;
      countdownHtml = `<span class="event-countdown-pill" style="color:#10b981;">${escapeHtml(ev.countdown || 'Live Now')}</span>`;
    } else if (ev.liveStatus === 'UPCOMING') {
      statusBadgeHtml = `<span class="status-badge-upcoming">⏰ Upcoming</span>`;
      countdownHtml = `<span class="event-countdown-pill" style="color:#60a5fa;">${escapeHtml(ev.countdown || 'Upcoming')}</span>`;
    } else {
      statusBadgeHtml = `<span class="status-badge-expired">⏳ Expired</span>`;
      countdownHtml = `<span class="event-countdown-pill" style="color:#94a3b8;">${escapeHtml(ev.countdown || 'Expired')}</span>`;
    }

    // Tags rendering
    let tagsArr = [];
    if (Array.isArray(ev.targetTags)) {
      tagsArr = ev.targetTags;
    } else if (typeof ev.targetTags === 'string') {
      try {
        const parsed = JSON.parse(ev.targetTags);
        tagsArr = Array.isArray(parsed) ? parsed : [ev.targetTags];
      } catch (_) {
        tagsArr = ev.targetTags.split(',').map(t => t.trim()).filter(Boolean);
      }
    }
    const tagsHtml = tagsArr.map(t => `<span class="tag-pill-festival">${escapeHtml(t)}</span>`).join('');

    // Format Dates
    const startStr = ev.startDate ? new Date(ev.startDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : 'N/A';
    const endStr = ev.endDate ? new Date(ev.endDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : 'N/A';

    const bannerImg = ev.bannerImageUrl || 'https://images.unsplash.com/photo-1605705658649-6f17666249be?auto=format&fit=crop&w=300&q=80';

    return `
      <tr class="event-row" data-id="${ev.id}">
        <td>
          <img src="${escapeHtml(bannerImg)}" class="festival-banner-thumb" alt="Banner" onerror="this.src='https://images.unsplash.com/photo-1605705658649-6f17666249be?auto=format&fit=crop&w=300&q=80'">
        </td>
        <td>
          <div style="font-weight: 700; color: #f8fafc; font-size: 13.5px;">${escapeHtml(ev.name)}</div>
          <div style="font-size: 11.5px; color: #94a3b8; display:flex; align-items:center; gap:6px; margin-top:2px;">
            <span style="background:rgba(255,255,255,0.06); padding:1px 6px; border-radius:4px; font-family:monospace; color:var(--accent-cyan);">key: ${escapeHtml(ev.festivalKey)}</span>
            <span>•</span>
            <span>${escapeHtml(ev.bannerBadge || '')}</span>
          </div>
          <div style="font-size: 12px; color: #cbd5e1; margin-top: 3px; font-weight: 500;">
            "${escapeHtml(ev.bannerTitle || '')}"
          </div>
        </td>
        <td>
          <div class="event-date-range">
            <div><strong>From:</strong> ${startStr}</div>
            <div><strong>To:</strong> ${endStr}</div>
          </div>
          ${countdownHtml}
        </td>
        <td>
          <div style="font-size: 12px; font-weight: 600; color: #38bdf8;">📁 ${escapeHtml(ev.targetCategory || 'Festivals & Celebrations')}</div>
          <div style="margin-top: 4px; max-width: 220px; display:flex; flex-wrap:wrap;">
            ${tagsHtml || '<span style="color:#64748b; font-size:11px;">No tags</span>'}
          </div>
        </td>
        <td>
          ${statusBadgeHtml}
        </td>
        <td>
          <button 
            type="button" 
            class="force-live-toggle-btn ${ev.forceLive ? 'active' : 'inactive'}" 
            onclick="toggleFestivalForceLive('${ev.id}')"
            title="Toggle instant Force Live testing override"
          >
            ${ev.forceLive ? '⚡ FORCE ON' : '○ Disabled'}
          </button>
        </td>
        <td style="text-align: right;">
          <div style="display:flex; justify-content:flex-end; gap:6px; align-items:center;">
            <button 
              type="button" 
              class="btn-icon-tiny btn-adb-push" 
              onclick="pushFestivalToDevice('${ev.id}', event)" 
              title="📲 Push Festival Live Test directly to connected phone (vivo I2212)"
              style="padding: 4px 8px; font-size: 11.5px; height: 28px; width: auto;"
            >
              📲 Test Phone
            </button>
            <button 
              type="button" 
              class="btn-icon-tiny" 
              onclick="openEditFestivalModal('${ev.id}')" 
              title="Edit Event"
            >
              <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
            </button>
            <button 
              type="button" 
              class="btn-icon-tiny ${ev.isActive ? 'text-danger' : 'text-success'}" 
              onclick="toggleFestivalActive('${ev.id}')" 
              title="${ev.isActive ? 'Pause Event' : 'Activate Event'}"
            >
              ${ev.isActive ? '⏸️' : '▶️'}
            </button>
            <button 
              type="button" 
              class="btn-icon-tiny text-danger" 
              onclick="deleteFestivalEvent('${ev.id}')" 
              title="Delete Event"
            >
              <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function openAddFestivalModal() {
  const modal = document.getElementById('festivalEventModal');
  const form = document.getElementById('festivalEventForm');
  if (!modal || !form) return;

  form.reset();
  document.getElementById('modalFestivalTitle').textContent = '🏷️ Create Festival Event';
  document.getElementById('festivalEventIdInput').value = '';
  document.getElementById('festivalIsActiveCheck').checked = true;
  document.getElementById('festivalForceLiveCheck').checked = false;
  document.getElementById('festivalPriorityInput').value = 10;
  document.getElementById('festivalSortOrderInput').value = 0;

  // Set default dates: Start today, End in 7 days
  const now = new Date();
  const nextWeek = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);
  document.getElementById('festivalStartDateInput').value = formatDateForInput(now);
  document.getElementById('festivalEndDateInput').value = formatDateForInput(nextWeek);

  document.getElementById('festivalBannerImagePreviewBox').style.display = 'none';
  modal.classList.add('active');
}

function openPresetFestivalModal(presetKey) {
  openAddFestivalModal();
  fillFestivalFormPreset(presetKey);
}

function fillFestivalFormPreset(presetKey) {
  const preset = FESTIVAL_PRESETS[presetKey];
  if (!preset) return;

  document.getElementById('festivalNameInput').value = preset.name;
  document.getElementById('festivalKeyInput').value = preset.festivalKey;
  document.getElementById('festivalBannerBadgeInput').value = preset.bannerBadge;
  document.getElementById('festivalBannerTitleInput').value = preset.bannerTitle;
  document.getElementById('festivalBannerSubtitleInput').value = preset.bannerSubtitle;
  document.getElementById('festivalBannerImageUrlInput').value = preset.bannerImageUrl;
  document.getElementById('festivalTargetCategoryInput').value = preset.targetCategory;
  document.getElementById('festivalTargetTagsInput').value = preset.targetTags;
  document.getElementById('festivalPriorityInput').value = preset.priority || 10;

  const start = new Date(Date.now() + (preset.startDateOffsetDays || 0) * 24 * 60 * 60 * 1000);
  const end = new Date(start.getTime() + (preset.durationDays || 7) * 24 * 60 * 60 * 1000);
  document.getElementById('festivalStartDateInput').value = formatDateForInput(start);
  document.getElementById('festivalEndDateInput').value = formatDateForInput(end);

  const prevBox = document.getElementById('festivalBannerImagePreviewBox');
  const prevImg = document.getElementById('festivalBannerImagePreviewImg');
  if (prevBox && prevImg && preset.bannerImageUrl) {
    prevImg.src = preset.bannerImageUrl;
    prevBox.style.display = 'block';
  }

  showToast(`⚡ Preset "${preset.name}" applied!`);
}

function openEditFestivalModal(id) {
  const ev = allFestivalEvents.find(e => String(e.id) === String(id));
  if (!ev) return;

  const modal = document.getElementById('festivalEventModal');
  const form = document.getElementById('festivalEventForm');
  if (!modal || !form) return;

  form.reset();
  document.getElementById('modalFestivalTitle').textContent = `🏷️ Edit Festival: ${ev.name}`;
  document.getElementById('festivalEventIdInput').value = ev.id;
  document.getElementById('festivalNameInput').value = ev.name || '';
  document.getElementById('festivalKeyInput').value = ev.festivalKey || '';
  document.getElementById('festivalStartDateInput').value = formatDateForInput(new Date(ev.startDate));
  document.getElementById('festivalEndDateInput').value = formatDateForInput(new Date(ev.endDate));
  document.getElementById('festivalBannerBadgeInput').value = ev.bannerBadge || '';
  document.getElementById('festivalPriorityInput').value = ev.priority || 10;
  document.getElementById('festivalBannerTitleInput').value = ev.bannerTitle || '';
  document.getElementById('festivalBannerSubtitleInput').value = ev.bannerSubtitle || '';
  document.getElementById('festivalBannerImageUrlInput').value = ev.bannerImageUrl || '';
  document.getElementById('festivalTargetCategoryInput').value = ev.targetCategory || '';
  document.getElementById('festivalSortOrderInput').value = ev.sortOrder || 0;

  let tagsStr = '';
  if (Array.isArray(ev.targetTags)) {
    tagsStr = ev.targetTags.join(', ');
  } else if (typeof ev.targetTags === 'string') {
    try {
      const parsed = JSON.parse(ev.targetTags);
      tagsStr = Array.isArray(parsed) ? parsed.join(', ') : ev.targetTags;
    } catch (_) {
      tagsStr = ev.targetTags;
    }
  }
  document.getElementById('festivalTargetTagsInput').value = tagsStr || '';
  document.getElementById('festivalIsActiveCheck').checked = Boolean(ev.isActive);
  document.getElementById('festivalForceLiveCheck').checked = Boolean(ev.forceLive);

  const prevBox = document.getElementById('festivalBannerImagePreviewBox');
  const prevImg = document.getElementById('festivalBannerImagePreviewImg');
  if (prevBox && prevImg && ev.bannerImageUrl) {
    prevImg.src = ev.bannerImageUrl;
    prevBox.style.display = 'block';
  } else if (prevBox) {
    prevBox.style.display = 'none';
  }

  modal.classList.add('active');
}

function closeFestivalModal() {
  const modal = document.getElementById('festivalEventModal');
  if (modal) modal.classList.remove('active');
}

function formatDateForInput(d) {
  if (!d || isNaN(d.getTime())) return '';
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

async function handleFestivalFormSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('festivalEventIdInput').value.trim();
  const isEdit = Boolean(id);

  const fileInput = document.getElementById('festivalBannerImageFileInput');
  const hasFile = fileInput && fileInput.files && fileInput.files[0];

  const payload = {
    name: document.getElementById('festivalNameInput').value.trim(),
    festivalKey: document.getElementById('festivalKeyInput').value.trim(),
    startDate: document.getElementById('festivalStartDateInput').value,
    endDate: document.getElementById('festivalEndDateInput').value,
    bannerBadge: document.getElementById('festivalBannerBadgeInput').value.trim(),
    bannerTitle: document.getElementById('festivalBannerTitleInput').value.trim(),
    bannerSubtitle: document.getElementById('festivalBannerSubtitleInput').value.trim(),
    bannerImageUrl: document.getElementById('festivalBannerImageUrlInput').value.trim(),
    targetCategory: document.getElementById('festivalTargetCategoryInput').value.trim(),
    targetTags: document.getElementById('festivalTargetTagsInput').value.trim(),
    priority: Number(document.getElementById('festivalPriorityInput').value) || 10,
    sortOrder: Number(document.getElementById('festivalSortOrderInput').value) || 0,
    isActive: document.getElementById('festivalIsActiveCheck').checked ? 1 : 0,
    forceLive: document.getElementById('festivalForceLiveCheck').checked ? 1 : 0
  };

  try {
    let res;
    if (hasFile) {
      const formData = new FormData();
      Object.keys(payload).forEach(key => formData.append(key, payload[key]));
      formData.append('bannerImage', fileInput.files[0]);

      res = await authFetch(isEdit ? `/api/admin/events/${id}` : '/api/admin/events', {
        method: isEdit ? 'PUT' : 'POST',
        body: formData
      });
    } else {
      res = await authFetch(isEdit ? `/api/admin/events/${id}` : '/api/admin/events', {
        method: isEdit ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    }

    const json = await res.json();
    if (json.success) {
      showToast(isEdit ? '✅ Festival Event updated successfully!' : '🎉 Festival Event created!');
      closeFestivalModal();
      loadFestivalEvents();
    } else {
      showToast(`⚠️ Error: ${json.error || 'Failed to save event'}`);
    }
  } catch (err) {
    console.error('Save festival event error:', err);
    showToast(`⚠️ Save error: ${err.message}`);
  }
}

async function toggleFestivalForceLive(id) {
  try {
    const res = await authFetch(`/api/admin/events/${id}/force-live`, {
      method: 'POST'
    });
    const json = await res.json();
    if (json.success) {
      showToast(json.data?.forceLive ? '⚡ Force Live ON: Banners are now LIVE in the app!' : '○ Force Live OFF: Reverted to scheduled dates');
      loadFestivalEvents();
    } else {
      showToast(`⚠️ ${json.error || 'Failed to toggle force live'}`);
    }
  } catch (err) {
    showToast(`⚠️ Toggle error: ${err.message}`);
  }
}

async function toggleFestivalActive(id) {
  try {
    const res = await authFetch(`/api/admin/events/${id}/toggle-active`, {
      method: 'POST'
    });
    const json = await res.json();
    if (json.success) {
      showToast(json.data?.isActive ? '🟢 Festival Event activated!' : '⏸️ Festival Event paused.');
      loadFestivalEvents();
    } else {
      showToast(`⚠️ ${json.error || 'Failed to toggle status'}`);
    }
  } catch (err) {
    showToast(`⚠️ Toggle error: ${err.message}`);
  }
}

async function deleteFestivalEvent(id) {
  if (!confirm('Are you sure you want to delete this scheduled festival event?')) return;
  try {
    const res = await authFetch(`/api/admin/events/${id}`, {
      method: 'DELETE'
    });
    const json = await res.json();
    if (json.success) {
      showToast('🗑️ Festival Event deleted.');
      loadFestivalEvents();
    } else {
      showToast(`⚠️ Delete error: ${json.error}`);
    }
  } catch (err) {
    showToast(`⚠️ Delete error: ${err.message}`);
  }
}

async function pushFestivalToDevice(id, event) {
  if (event && event.stopPropagation) {
    event.stopPropagation();
  }

  const btn = event ? event.currentTarget : null;
  const originalHtml = btn ? btn.innerHTML : '';

  if (btn) {
    btn.classList.add('pushing');
    btn.innerHTML = '📲 Pushing...';
  }

  try {
    const res = await authFetch(`/api/admin/events/${id}/push-device`, {
      method: 'POST'
    });
    const json = await res.json();
    if (json.success) {
      if (btn) {
        btn.classList.remove('pushing');
        btn.classList.add('success');
        btn.innerHTML = '✅ Live on Phone!';
        setTimeout(() => {
          btn.classList.remove('success');
          btn.innerHTML = originalHtml;
        }, 2500);
      }
      showToast(json.message || `🚀 Festival live preview pushed to ${json.device?.displayName || 'Phone'}!`);
    } else {
      if (btn) {
        btn.classList.remove('pushing');
        btn.innerHTML = originalHtml;
      }
      showToast(`⚠️ Live Test error: ${json.error || 'Failed to push'}`);
    }
  } catch (err) {
    console.error('Push festival error:', err);
    if (btn) {
      btn.classList.remove('pushing');
      btn.innerHTML = originalHtml;
    }
    showToast(`⚠️ Push error: ${err.message}`);
  }
}

// Global exposure for event handlers
window.initFestivalScheduler = initFestivalScheduler;
window.loadFestivalEvents = loadFestivalEvents;
window.renderFestivalEventsTable = renderFestivalEventsTable;
window.openAddFestivalModal = openAddFestivalModal;
window.openPresetFestivalModal = openPresetFestivalModal;
window.fillFestivalFormPreset = fillFestivalFormPreset;
window.openEditFestivalModal = openEditFestivalModal;
window.closeFestivalModal = closeFestivalModal;
window.toggleFestivalForceLive = toggleFestivalForceLive;
window.toggleFestivalActive = toggleFestivalActive;
window.deleteFestivalEvent = deleteFestivalEvent;
window.pushFestivalToDevice = pushFestivalToDevice;



