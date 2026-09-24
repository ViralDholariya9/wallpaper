const express = require('express');
const router = express.Router();
const { db, defaultPersonalizationSuite } = require('../db/database');

// Helper to attach layers to wallpapers
async function attachLayers(wallpapers) {
  if (!wallpapers || wallpapers.length === 0) return [];
  const ids = wallpapers.map(w => `'${w.id}'`).join(',');
  const layers = await db.allAsync(
    `SELECT id, wallpaperId, imageUrl, depth, sortOrder FROM wallpaper_layers WHERE wallpaperId IN (${ids}) ORDER BY sortOrder ASC`
  );

  const layersByWallpaper = {};
  for (const l of layers) {
    if (!layersByWallpaper[l.wallpaperId]) {
      layersByWallpaper[l.wallpaperId] = [];
    }
    layersByWallpaper[l.wallpaperId].push({
      id: l.id,
      imageUrl: l.imageUrl,
      depth: parseFloat(l.depth)
    });
  }

function optimizeThumbnail(url) {
  if (!url) return url;
  if (url.includes('images.unsplash.com') && !url.includes('&w=')) {
    return `${url}&w=600&auto=format&fit=crop&q=75`;
  }
  return url;
}

  return wallpapers.map(w => ({
    id: w.id,
    title: w.title,
    category: w.category,
    previewUrl: optimizeThumbnail(w.previewUrl),
    fullUrl: w.fullUrl || w.previewUrl,
    isParallax: Boolean(w.isParallax),
    layers: layersByWallpaper[w.id] || [],
    downloads: w.downloads,
    likes: w.likes,
    isFavorite: false,
    isPremium: Boolean(w.isPremium),
    isUnlocked: Boolean(w.isUnlocked),
    createdAt: Number(w.createdAt)
  }));
}

// GET /api/wallpapers
// Query params: page, limit, category, tab, search
router.get('/wallpapers', async (req, res) => {
  try {
    const page = parseInt(req.query.page, 10) || 1;
    const limit = parseInt(req.query.limit, 10) || 12;
    const offset = (page - 1) * limit;

    const { category, tab, search } = req.query;
    const whereClauses = [];
    const params = [];

    if (category && category !== 'ALL') {
      whereClauses.push('category = ?');
      params.push(category);
    }

    if (search && search.trim() !== '') {
      whereClauses.push('title LIKE ?');
      params.push(`%${search.trim()}%`);
    }

    if (tab === 'PARALLAX' || tab === 'THREE_D') {
      whereClauses.push('isParallax = 1');
    } else if (tab === 'TWO_D') {
      whereClauses.push('isParallax = 0');
    }

    let orderBy = 'createdAt DESC';
    if (tab === 'POPULAR') {
      orderBy = 'likes DESC, downloads DESC';
    } else if (tab === 'RECENT') {
      orderBy = 'createdAt DESC';
    }

    const whereSql = whereClauses.length > 0 ? `WHERE ${whereClauses.join(' AND ')}` : '';

    const countRow = await db.getAsync(`SELECT COUNT(*) as total FROM wallpapers ${whereSql}`, params);
    const total = countRow ? countRow.total : 0;

    const wallpapers = await db.allAsync(
      `SELECT * FROM wallpapers ${whereSql} ORDER BY ${orderBy} LIMIT ? OFFSET ?`,
      [...params, limit, offset]
    );

    const formatted = await attachLayers(wallpapers);

    res.json({
      success: true,
      data: formatted,
      pagination: {
        page,
        limit,
        total,
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (err) {
    console.error('Error fetching wallpapers:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/wallpapers/:id
router.get('/wallpapers/:id', async (req, res) => {
  try {
    const wallpaper = await db.getAsync('SELECT * FROM wallpapers WHERE id = ?', [req.params.id]);
    if (!wallpaper) {
      return res.status(404).json({ success: false, error: 'Wallpaper not found' });
    }
    const [formatted] = await attachLayers([wallpaper]);
    res.json({ success: true, data: formatted });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/wallpapers/:id/download
router.post('/wallpapers/:id/download', async (req, res) => {
  try {
    await db.runAsync('UPDATE wallpapers SET downloads = downloads + 1 WHERE id = ?', [req.params.id]);
    res.json({ success: true, message: 'Download count incremented' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/wallpapers/:id/like
router.post('/wallpapers/:id/like', async (req, res) => {
  try {
    await db.runAsync('UPDATE wallpapers SET likes = likes + 1 WHERE id = ?', [req.params.id]);
    res.json({ success: true, message: 'Like count incremented' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/categories
router.get('/categories', async (req, res) => {
  try {
    const categories = await db.allAsync(`
      SELECT c.*, COUNT(w.id) as count 
      FROM categories c 
      LEFT JOIN wallpapers w ON w.category = c.id 
      GROUP BY c.id 
      ORDER BY c.name ASC
    `);
    res.json({ success: true, data: categories });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/ringtones
router.get('/ringtones', async (req, res) => {
  try {
    const { category, search } = req.query;
    const whereClauses = [];
    const params = [];

    if (category && category !== 'ALL') {
      whereClauses.push('category = ?');
      params.push(category);
    }
    if (search && search.trim() !== '') {
      whereClauses.push('(title LIKE ? OR artist LIKE ?)');
      params.push(`%${search.trim()}%`, `%${search.trim()}%`);
    }

    const whereSql = whereClauses.length > 0 ? `WHERE ${whereClauses.join(' AND ')}` : '';
    const ringtones = await db.allAsync(`SELECT * FROM ringtones ${whereSql} ORDER BY downloads DESC`, params);

    res.json({
      success: true,
      data: ringtones.map(r => ({
        id: r.id,
        title: r.title,
        artist: r.artist,
        audioUrl: r.audioUrl,
        durationSeconds: r.durationSeconds,
        category: r.category,
        downloads: r.downloads,
        isFavorite: false,
        isPlaying: false
      }))
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/daily-pick
router.get('/daily-pick', async (req, res) => {
  try {
    const pickConfig = await db.getAsync("SELECT value FROM app_config WHERE key = 'daily_pick_id'");
    let wallpaper = null;

    if (pickConfig && pickConfig.value) {
      wallpaper = await db.getAsync('SELECT * FROM wallpapers WHERE id = ?', [pickConfig.value]);
    }

    if (!wallpaper) {
      // Fallback to top liked 3D parallax wallpaper
      wallpaper = await db.getAsync('SELECT * FROM wallpapers WHERE isParallax = 1 ORDER BY likes DESC LIMIT 1');
    }

    if (!wallpaper) {
      wallpaper = await db.getAsync('SELECT * FROM wallpapers ORDER BY id ASC LIMIT 1');
    }

    if (!wallpaper) {
      return res.status(404).json({ success: false, error: 'No daily pick available' });
    }

    const [formatted] = await attachLayers([wallpaper]);
    formatted.title = `⭐ Daily Pick: ${formatted.title}`;
    res.json({ success: true, data: formatted });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/app-config
router.get('/app-config', async (req, res) => {
  try {
    const rows = await db.allAsync('SELECT key, value FROM app_config');
    const configMap = {};
    for (const r of rows) {
      configMap[r.key] = r.value;
    }

    const weatherCategoriesRaw = configMap['weather_categories'] || 'all';
    const weatherCategories = weatherCategoriesRaw === 'all' 
      ? ['all'] 
      : weatherCategoriesRaw.split(',').map(c => c.trim()).filter(Boolean);

    let suiteItems = defaultPersonalizationSuite;
    if (configMap['personalization_suite_items']) {
      try {
        suiteItems = JSON.parse(configMap['personalization_suite_items']);
      } catch (e) {
        suiteItems = defaultPersonalizationSuite;
      }
    }

    res.json({
      success: true,
      data: {
        // Flagship Personalization Suite Over-The-Air Config
        personalizationSuite: suiteItems,

        // Dynamic Weather Overlays Feature Flag
        weather: {
          enabled: configMap['weather_enabled'] === 'true',
          effect: configMap['weather_effect'] || 'rain',
          intensity: configMap['weather_intensity'] || 'medium',
          categories: weatherCategories,
          sfxEnabled: configMap['weather_sfx_enabled'] === 'true'
        },

        // Force Update Enforcement Engine
        forceUpdate: {
          enabled: configMap['force_update_enabled'] === 'true',
          latestVersion: configMap['app_version'] || '1.0.0',
          minSupportedVersion: configMap['min_supported_version'] || '1.0.0',
          title: configMap['update_dialog_title'] || '🚀 New 3D Engine Update Available!',
          message: configMap['update_dialog_message'] || 'Please update to enjoy the latest 3D wallpapers and smooth performance.',
          storeUrl: configMap['update_url'] || 'https://play.google.com/store/apps',
          buttonText: configMap['update_btn_text'] || 'Update to Latest Version'
        },

        // In-App Announcement & Promo Banner
        announcement: {
          enabled: configMap['announcement_enabled'] === 'true',
          text: configMap['announcement_text'] || '',
          type: configMap['announcement_type'] || 'festival',
          theme: configMap['announcement_theme'] || 'diwali_gold',
          actionTarget: configMap['announcement_action_url'] || '',
          dismissable: configMap['announcement_dismissable'] !== 'false'
        },

        // Dynamic App Feature Flags (Drawer & Master Switchboard)
        features: {
          statusSaver: configMap['feature_status_saver'] !== 'false',
          aiStudio: configMap['feature_ai_studio'] !== 'false',
          musicVisualizer: configMap['feature_music_visualizer'] !== 'false',
          ringtones: configMap['feature_ringtones'] !== 'false',
          custom3dMaker: configMap['feature_custom_3d'] !== 'false',
          weatherSync: configMap['feature_weather_sync'] !== 'false',
          doubleWallpaper: configMap['feature_double_wallpaper'] !== 'false',
          wallpaperChanger: configMap['feature_wallpaper_changer'] !== 'false',
          chargingAnimations: configMap['feature_charging_animations'] !== 'false',
          edgeLighting: configMap['feature_edge_lighting'] !== 'false',
          dynamicIsland: configMap['feature_dynamic_island'] !== 'false',
          alwaysOnDisplay: configMap['feature_always_on_display'] !== 'false',
          callScreen: configMap['feature_call_screen'] !== 'false',
          touchEffects: configMap['feature_touch_effects'] !== 'false',
          fingerprintAnimations: configMap['feature_fingerprint_animations'] !== 'false'
        },

        // Social Media, Legal Policy & Support Hub
        legalAndSupport: {
          privacyPolicyUrl: configMap['privacy_policy_url'] || 'https://rewall-3d.web.app/privacy-policy',
          termsUrl: configMap['terms_of_service_url'] || 'https://rewall-3d.web.app/terms',
          instagramUrl: configMap['instagram_url'] || 'https://instagram.com/rewall.3d',
          telegramUrl: configMap['telegram_url'] || 'https://t.me/rewall_wallpapers',
          whatsappNumber: configMap['whatsapp_support_number'] || '+919876543210',
          supportEmail: configMap['support_email'] || 'support@rewall.app'
        },

        // Smart In-App Rating & Review Trigger (Play Store Growth)
        ratingPrompt: {
          enabled: configMap['rating_prompt_enabled'] !== 'false',
          triggerDownloads: parseInt(configMap['rating_prompt_downloads_trigger'] || '3', 10),
          title: configMap['rating_prompt_title'] || 'Enjoying ReWall 3D Wallpapers?',
          message: configMap['rating_prompt_message'] || 'You have downloaded awesome 4K 3D wallpapers! A quick 5-star rating on Google Play Store helps our team keep adding free wallpapers.',
          positiveBtn: configMap['rating_prompt_positive_btn'] || '⭐ Rate 5 Stars on Google Play',
          dismissBtn: configMap['rating_prompt_dismiss_btn'] || 'Maybe Later'
        },

        // Monetization & AdMob Live Manager
        admob: {
          bannerEnabled: configMap['admob_banner_enabled'] !== 'false',
          interstitialEnabled: configMap['admob_interstitial_enabled'] !== 'false',
          rewardedInterval: parseInt(configMap['admob_rewarded_interval'] || '3', 10),
          interstitialInterval: parseInt(configMap['admob_interstitial_interval'] || '3', 10),
          appId: configMap['admob_app_id'] || 'ca-app-pub-3940256099942544~3347511713',
          bannerId: configMap['admob_banner_id'] || 'ca-app-pub-3940256099942544/6300978111',
          interstitialId: configMap['admob_interstitial_id'] || 'ca-app-pub-3940256099942544/1033173712',
          rewardedId: configMap['admob_rewarded_id'] || 'ca-app-pub-3940256099942544/5224354917',
          appOpenId: configMap['admob_app_open_id'] || 'ca-app-pub-3940256099942544/9257395921',
          isTestMode: configMap['admob_is_test_mode'] !== 'false'
        },

        // System state (backward-compatible top-level properties)
        maintenanceMode: configMap['maintenance_mode'] === 'true',
        dailyPickId: configMap['daily_pick_id'] || '',
        appVersion: configMap['app_version'] || '1.0.0',
        updateUrl: configMap['update_url'] || ''
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/banners (Public endpoint for active home hero carousel banners & scheduled festivals)
router.get('/banners', async (req, res) => {
  try {
    const nowIso = new Date().toISOString();

    // 1. Fetch currently active scheduled festival events (or forceLive test override)
    let festivalBanners = [];
    try {
      const activeEvents = await db.allAsync(
        `SELECT id, name, festivalKey, bannerTitle, bannerSubtitle, bannerBadge, bannerImageUrl, targetCategory, startDate, endDate, priority, sortOrder
         FROM festival_events
         WHERE isActive = 1 AND (forceLive = 1 OR (startDate <= ? AND ? <= endDate))
         ORDER BY priority DESC, sortOrder ASC`,
        [nowIso, nowIso]
      );

      festivalBanners = (activeEvents || []).map((e, idx) => ({
        id: e.id,
        title: e.bannerTitle,
        subtitle: e.bannerSubtitle || '',
        badgeText: e.bannerBadge || 'FESTIVAL SPECIAL',
        imageUrl: e.bannerImageUrl,
        actionType: 'category',
        actionTarget: e.targetCategory || 'Festival',
        sortOrder: idx,
        isFestival: true,
        festivalKey: e.festivalKey
      }));
    } catch (e) {
      console.warn('Festival events query fallback:', e.message);
    }

    // 2. Fetch standard hero banners
    const standardBanners = await db.allAsync(
      'SELECT id, title, subtitle, badgeText, imageUrl, actionType, actionTarget, sortOrder FROM hero_banners WHERE isActive = 1 ORDER BY sortOrder ASC, createdAt DESC'
    );

    // 3. Combine with festival banners on top
    const combined = [...festivalBanners, ...(standardBanners || [])];

    res.json({
      success: true,
      count: combined.length,
      data: combined,
      hasActiveFestival: festivalBanners.length > 0
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// Known Geo Presets for local / private dev IPs
const GEO_PRESETS = [
  { city: 'Mumbai', country: 'India', countryCode: 'IN', lat: 19.0760, lng: 72.8777 },
  { city: 'Ahmedabad', country: 'India', countryCode: 'IN', lat: 23.0225, lng: 72.5714 },
  { city: 'Surat', country: 'India', countryCode: 'IN', lat: 21.1702, lng: 72.8311 },
  { city: 'Bengaluru', country: 'India', countryCode: 'IN', lat: 12.9716, lng: 77.5946 },
  { city: 'New Delhi', country: 'India', countryCode: 'IN', lat: 28.6139, lng: 77.2090 },
  { city: 'Pune', country: 'India', countryCode: 'IN', lat: 18.5204, lng: 73.8567 },
  { city: 'Hyderabad', country: 'India', countryCode: 'IN', lat: 17.3850, lng: 78.4867 }
];

// POST /api/device/telemetry (Rich Device Telemetry & Heartbeat)
router.post('/device/telemetry', async (req, res) => {
  try {
    const {
      deviceId,
      deviceModel,
      manufacturer,
      brand,
      androidVersion,
      apiLevel,
      appVersion,
      screenResolution,
      screenDpi,
      fcmToken,
      batteryLevel,
      city: clientCity,
      country: clientCountry
    } = req.body;

    const rawIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress || '127.0.0.1';
    const clientIp = rawIp.replace('::ffff:', '');

    // Default to Ahmedabad / Gujarat / India if local
    const defaultGeo = GEO_PRESETS[Math.floor(Math.random() * GEO_PRESETS.length)];
    const finalCity = clientCity || defaultGeo.city;
    const finalCountry = clientCountry || defaultGeo.country;
    const finalCountryCode = defaultGeo.countryCode;
    const finalLat = defaultGeo.lat;
    const finalLng = defaultGeo.lng;

    const finalDeviceId = deviceId || (fcmToken ? `dev_${fcmToken.slice(0, 16)}` : `dev_${Date.now()}`);
    const finalModel = deviceModel || `${manufacturer || 'Android'} Device`;
    const finalMfr = manufacturer || (deviceModel ? deviceModel.split(' ')[0] : 'Android');
    const finalBrand = brand || finalMfr.toLowerCase();
    const finalOs = androidVersion || 'Android 14';
    const finalApi = parseInt(apiLevel, 10) || 34;
    const finalAppVer = appVersion || '1.0.0';
    const now = Date.now();

    await db.runAsync(
      `INSERT INTO device_telemetry (
         deviceId, deviceModel, manufacturer, brand, androidVersion, apiLevel,
         appVersion, screenResolution, screenDpi, country, countryCode, city,
         latitude, longitude, ipAddress, fcmToken, batteryLevel, firstSeenAt, lastPingAt
       ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
       ON CONFLICT(deviceId) DO UPDATE SET
         deviceModel = excluded.deviceModel,
         manufacturer = excluded.manufacturer,
         brand = excluded.brand,
         androidVersion = excluded.androidVersion,
         apiLevel = excluded.apiLevel,
         appVersion = excluded.appVersion,
         screenResolution = excluded.screenResolution,
         screenDpi = excluded.screenDpi,
         ipAddress = excluded.ipAddress,
         fcmToken = COALESCE(excluded.fcmToken, device_telemetry.fcmToken),
         batteryLevel = excluded.batteryLevel,
         lastPingAt = excluded.lastPingAt`,
      [
        finalDeviceId,
        finalModel,
        finalMfr,
        finalBrand,
        finalOs,
        finalApi,
        finalAppVer,
        screenResolution || '1080x2400',
        parseInt(screenDpi, 10) || 420,
        finalCountry,
        finalCountryCode,
        finalCity,
        finalLat,
        finalLng,
        clientIp,
        fcmToken || null,
        parseInt(batteryLevel, 10) || 85,
        now,
        now
      ]
    );

    // Keep fcm_tokens in sync if push token is supplied
    if (fcmToken) {
      await db.runAsync(
        `INSERT INTO fcm_tokens (token, deviceModel, appVersion, updatedAt)
         VALUES (?, ?, ?, ?)
         ON CONFLICT(token) DO UPDATE SET
           deviceModel = excluded.deviceModel,
           appVersion = excluded.appVersion,
           updatedAt = excluded.updatedAt`,
        [fcmToken.trim(), finalModel, finalAppVer, now]
      );
    }

    console.log(`📱 Telemetry heartbeat received: [${finalModel}] (${finalOs}, ${finalCity})`);
    res.json({
      success: true,
      message: 'Telemetry logged successfully',
      data: { deviceId: finalDeviceId, status: 'online' }
    });
  } catch (err) {
    console.error('Error logging telemetry:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/device/register-token (Legacy token registration)
router.post('/device/register-token', async (req, res) => {
  try {
    const { token, deviceModel, appVersion } = req.body;
    if (!token || !token.trim()) {
      return res.status(400).json({ success: false, error: 'Token is required' });
    }
    await db.runAsync(
      `INSERT INTO fcm_tokens (token, deviceModel, appVersion, updatedAt)
       VALUES (?, ?, ?, ?)
       ON CONFLICT(token) DO UPDATE SET
         deviceModel = excluded.deviceModel,
         appVersion = excluded.appVersion,
         updatedAt = excluded.updatedAt`,
      [token.trim(), deviceModel || 'Android Device', appVersion || '1.0.0', Date.now()]
    );
    console.log(`📱 Device registered for push notifications: [${(deviceModel || 'Android').trim()}]`);
    res.json({ success: true, message: 'Device token registered successfully' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// ==========================================
// 🖼️ HERO BANNERS CAROUSEL (Public API)
// ==========================================
// GET /api/banners - returns active banners ordered by sortOrder ASC
router.get('/banners', async (req, res) => {
  try {
    const banners = await db.allAsync(
      `SELECT id, title, subtitle, badgeText, imageUrl, actionType, actionTarget, sortOrder, isActive, createdAt
       FROM hero_banners
       WHERE isActive = 1
       ORDER BY sortOrder ASC, createdAt DESC`
    );

    const formatted = (banners || []).map(b => ({
      id: b.id,
      title: b.title,
      subtitle: b.subtitle || '',
      badgeText: b.badgeText || '',
      imageUrl: b.imageUrl,
      actionType: b.actionType || 'category',
      actionTarget: b.actionTarget || '',
      sortOrder: Number(b.sortOrder || 0),
      isActive: Boolean(b.isActive),
      createdAt: Number(b.createdAt)
    }));

    res.json({
      success: true,
      data: formatted
    });
  } catch (err) {
    console.error('Error fetching hero banners:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ==========================================
// 🔥 TRENDING SEARCH KEYWORDS / TAGS (Public API)
// ==========================================

// GET /api/trending-tags - returns active trending tags ordered by sortOrder ASC (with scheduled festival tags injected on top)
router.get('/trending-tags', async (req, res) => {
  try {
    const nowIso = new Date().toISOString();

    // 1. Fetch tags from active festival events
    const festivalTags = [];
    try {
      const activeEvents = await db.allAsync(
        `SELECT id, name, festivalKey, targetTags, startDate, endDate
         FROM festival_events
         WHERE isActive = 1 AND (forceLive = 1 OR (startDate <= ? AND ? <= endDate))
         ORDER BY priority DESC, sortOrder ASC`,
        [nowIso, nowIso]
      );

      const iconMap = {
        diwali: '🪔',
        navratri: '💃',
        new_year: '🎆',
        uttarayan: '🪁',
        holi: '🎨',
        christmas: '🎄',
        custom: '✨'
      };

      for (const ev of (activeEvents || [])) {
        try {
          const tagsArr = typeof ev.targetTags === 'string' ? JSON.parse(ev.targetTags) : (ev.targetTags || []);
          tagsArr.forEach((t, i) => {
            const cleanTag = t.startsWith('#') ? t : `#${t}`;
            festivalTags.push({
              id: `fest_${ev.id}_${i}`,
              tag: cleanTag,
              icon: iconMap[ev.festivalKey] || '🔥',
              sortOrder: i - 50, // Top priority
              clickCount: 8800 - i * 100,
              isActive: true,
              isFestival: true,
              createdAt: Date.now()
            });
          });
        } catch (_) {}
      }
    } catch (e) {
      console.warn('Festival tags query fallback:', e.message);
    }

    // 2. Fetch standard trending tags
    const tags = await db.allAsync(
      `SELECT id, tag, icon, sortOrder, clickCount, isActive, createdAt
       FROM trending_tags
       WHERE isActive = 1
       ORDER BY sortOrder ASC, clickCount DESC`
    );

    const standardTags = (tags || []).map(t => ({
      id: t.id,
      tag: t.tag,
      icon: t.icon || '🔥',
      sortOrder: Number(t.sortOrder || 0),
      clickCount: Number(t.clickCount || 0),
      isActive: Boolean(t.isActive),
      createdAt: Number(t.createdAt)
    }));

    // Combine with festival tags on top, avoiding exact tag duplicates
    const seenTags = new Set();
    const combinedTags = [];

    for (const t of [...festivalTags, ...standardTags]) {
      const lower = t.tag.toLowerCase();
      if (!seenTags.has(lower)) {
        seenTags.add(lower);
        combinedTags.push(t);
      }
    }

    res.json({
      success: true,
      count: combinedTags.length,
      data: combinedTags
    });
  } catch (err) {
    console.error('Error fetching trending tags:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/events/active - Returns currently active festival campaign details
router.get('/events/active', async (req, res) => {
  try {
    const nowIso = new Date().toISOString();
    const activeEvents = await db.allAsync(
      `SELECT * FROM festival_events
       WHERE isActive = 1 AND (forceLive = 1 OR (startDate <= ? AND ? <= endDate))
       ORDER BY priority DESC, sortOrder ASC`,
      [nowIso, nowIso]
    );

    res.json({
      success: true,
      count: (activeEvents || []).length,
      data: activeEvents || []
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/trending-tags/:id/click - increment tag click analytics
router.post('/trending-tags/:id/click', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync(
      `UPDATE trending_tags SET clickCount = clickCount + 1 WHERE id = ?`,
      [id]
    );
    res.json({ success: true, message: 'Tag click recorded' });
  } catch (err) {
    console.error('Error recording tag click:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// ⚡ DYNAMIC CHARGING ANIMATIONS (Public Endpoints for App)
// ========================================================

// GET /api/charging-animations
router.get('/charging-animations', async (req, res) => {
  try {
    const { category, search } = req.query;
    const where = ['isActive = 1'];
    const params = [];

    if (category && category.toUpperCase() !== 'ALL') {
      where.push('category = ?');
      params.push(category.toUpperCase());
    }

    if (search && search.trim() !== '') {
      where.push('title LIKE ?');
      params.push(`%${search.trim()}%`);
    }

    const whereSql = `WHERE ${where.join(' AND ')}`;
    const items = await db.allAsync(
      `SELECT * FROM charging_animations ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`,
      params
    );

    res.json({
      success: true,
      count: items.length,
      data: items.map(item => ({
        id: item.id,
        title: item.title,
        category: item.category,
        previewUrl: item.previewUrl,
        animationUrl: item.animationUrl,
        animationType: item.animationType || 'lottie',
        soundUrl: item.soundUrl || null,
        textColor: item.textColor || '#00E5FF',
        isPremium: Boolean(item.isPremium),
        downloads: Number(item.downloads || 0),
        sortOrder: Number(item.sortOrder || 0),
        createdAt: Number(item.createdAt)
      }))
    });
  } catch (err) {
    console.error('Error fetching charging animations:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/charging-animations/categories
router.get('/charging-animations/categories', async (req, res) => {
  try {
    const categories = await db.allAsync(
      `SELECT category as name, COUNT(*) as count 
       FROM charging_animations 
       WHERE isActive = 1 
       GROUP BY category 
       ORDER BY count DESC`
    );
    res.json({ success: true, data: categories });
  } catch (err) {
    console.error('Error fetching charging categories:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/charging-animations/:id
router.get('/charging-animations/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync(
      `SELECT * FROM charging_animations WHERE id = ?`,
      [id]
    );

    if (!item) {
      return res.status(404).json({ success: false, error: 'Charging animation not found' });
    }

    res.json({
      success: true,
      data: {
        id: item.id,
        title: item.title,
        category: item.category,
        previewUrl: item.previewUrl,
        animationUrl: item.animationUrl,
        animationType: item.animationType || 'lottie',
        soundUrl: item.soundUrl || null,
        textColor: item.textColor || '#00E5FF',
        isPremium: Boolean(item.isPremium),
        downloads: Number(item.downloads || 0),
        isActive: Boolean(item.isActive),
        sortOrder: Number(item.sortOrder || 0),
        createdAt: Number(item.createdAt)
      }
    });
  } catch (err) {
    console.error('Error fetching charging animation details:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/charging-animations/:id/apply
router.post('/charging-animations/:id/apply', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync(
      `UPDATE charging_animations SET downloads = downloads + 1 WHERE id = ?`,
      [id]
    );
    res.json({ success: true, message: 'Charging animation applied successfully' });
  } catch (err) {
    console.error('Error applying charging animation:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// 🌈 DYNAMIC EDGE LIGHTING & BORDER ANIMATIONS (Public Endpoints for App)

// GET /api/edge-lighting
router.get('/edge-lighting', async (req, res) => {
  try {
    const { category, search } = req.query;
    let whereClause = ['isActive = 1'];
    let params = [];

    if (category && category !== 'ALL') {
      whereClause.push('category = ?');
      params.push(category);
    }

    if (search && search.trim() !== '') {
      whereClause.push('(title LIKE ? OR category LIKE ? OR animationType LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q, q);
    }

    const whereSql = `WHERE ${whereClause.join(' AND ')}`;
    const rows = await db.allAsync(
      `SELECT * FROM edge_lighting_presets ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`,
      params
    );

    const data = rows.map((r) => {
      let parsedColors = [];
      try {
        parsedColors = JSON.parse(r.colors);
      } catch (_) {
        parsedColors = ['#00E5FF', '#7000FF'];
      }
      return {
        id: r.id,
        title: r.title,
        category: r.category,
        animationType: r.animationType,
        colors: parsedColors,
        speed: Number(r.speed || 1.0),
        borderSize: Number(r.borderSize || 5),
        cornerRadius: Number(r.cornerRadius || 28),
        punchHoleRadius: Number(r.punchHoleRadius || 0),
        glowSpread: Number(r.glowSpread || 12),
        previewUrl: r.previewUrl,
        isPremium: Boolean(r.isPremium),
        downloads: Number(r.downloads || 0),
        isActive: Boolean(r.isActive),
        sortOrder: Number(r.sortOrder || 0),
        createdAt: Number(r.createdAt)
      };
    });

    res.json({
      success: true,
      count: data.length,
      data
    });
  } catch (err) {
    console.error('Error fetching edge lighting presets:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/edge-lighting/categories
router.get('/edge-lighting/categories', async (req, res) => {
  try {
    const rows = await db.allAsync(
      `SELECT category, COUNT(*) as count 
       FROM edge_lighting_presets 
       WHERE isActive = 1 
       GROUP BY category 
       ORDER BY count DESC`
    );
    res.json({ success: true, data: rows });
  } catch (err) {
    console.error('Error fetching edge lighting categories:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/edge-lighting/:id
router.get('/edge-lighting/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync(
      `SELECT * FROM edge_lighting_presets WHERE id = ?`,
      [id]
    );

    if (!item) {
      return res.status(404).json({ success: false, error: 'Edge lighting preset not found' });
    }

    let parsedColors = [];
    try {
      parsedColors = JSON.parse(item.colors);
    } catch (_) {
      parsedColors = ['#00E5FF', '#7000FF'];
    }

    res.json({
      success: true,
      data: {
        id: item.id,
        title: item.title,
        category: item.category,
        animationType: item.animationType,
        colors: parsedColors,
        speed: Number(item.speed || 1.0),
        borderSize: Number(item.borderSize || 5),
        cornerRadius: Number(item.cornerRadius || 28),
        punchHoleRadius: Number(item.punchHoleRadius || 0),
        glowSpread: Number(item.glowSpread || 12),
        previewUrl: item.previewUrl,
        isPremium: Boolean(item.isPremium),
        downloads: Number(item.downloads || 0),
        isActive: Boolean(item.isActive),
        sortOrder: Number(item.sortOrder || 0),
        createdAt: Number(item.createdAt)
      }
    });
  } catch (err) {
    console.error('Error fetching edge lighting details:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/edge-lighting/:id/apply
router.post('/edge-lighting/:id/apply', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync(
      `UPDATE edge_lighting_presets SET downloads = downloads + 1 WHERE id = ?`,
      [id]
    );
    res.json({ success: true, message: 'Edge lighting preset applied successfully' });
  } catch (err) {
    console.error('Error applying edge lighting preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// 🏝️ DYNAMIC ISLAND & NOTIFICATION CAPSULE (Public Endpoints for App)

// GET /api/dynamic-island/themes
router.get('/dynamic-island/themes', async (req, res) => {
  try {
    const { search } = req.query;
    let whereClause = ['isActive = 1'];
    let params = [];

    if (search && search.trim() !== '') {
      whereClause.push('(title LIKE ? OR styleType LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q);
    }

    const whereSql = `WHERE ${whereClause.join(' AND ')}`;
    const rows = await db.allAsync(
      `SELECT * FROM dynamic_island_themes ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`,
      params
    );

    const data = rows.map((r) => ({
      id: r.id,
      title: r.title,
      styleType: r.styleType,
      backgroundColor: r.backgroundColor,
      textColor: r.textColor,
      accentColor: r.accentColor,
      glowColor: r.glowColor,
      cornerRadius: Number(r.cornerRadius || 24),
      compactWidth: Number(r.compactWidth || 120),
      compactHeight: Number(r.compactHeight || 36),
      expandedWidth: Number(r.expandedWidth || 320),
      expandedHeight: Number(r.expandedHeight || 84),
      previewUrl: r.previewUrl,
      isPremium: Boolean(r.isPremium),
      downloads: Number(r.downloads || 0),
      isActive: Boolean(r.isActive),
      sortOrder: Number(r.sortOrder || 0),
      createdAt: Number(r.createdAt)
    }));

    res.json({ success: true, count: data.length, data });
  } catch (err) {
    console.error('Error fetching dynamic island themes:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/dynamic-island/themes/:id
router.get('/dynamic-island/themes/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const r = await db.getAsync('SELECT * FROM dynamic_island_themes WHERE id = ?', [id]);
    if (!r) {
      return res.status(404).json({ success: false, error: 'Dynamic Island theme not found' });
    }
    res.json({
      success: true,
      data: {
        id: r.id,
        title: r.title,
        styleType: r.styleType,
        backgroundColor: r.backgroundColor,
        textColor: r.textColor,
        accentColor: r.accentColor,
        glowColor: r.glowColor,
        cornerRadius: Number(r.cornerRadius || 24),
        compactWidth: Number(r.compactWidth || 120),
        compactHeight: Number(r.compactHeight || 36),
        expandedWidth: Number(r.expandedWidth || 320),
        expandedHeight: Number(r.expandedHeight || 84),
        previewUrl: r.previewUrl,
        isPremium: Boolean(r.isPremium),
        downloads: Number(r.downloads || 0),
        isActive: Boolean(r.isActive),
        sortOrder: Number(r.sortOrder || 0),
        createdAt: Number(r.createdAt)
      }
    });
  } catch (err) {
    console.error('Error fetching dynamic island theme details:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/dynamic-island/themes/:id/apply
router.post('/dynamic-island/themes/:id/apply', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync('UPDATE dynamic_island_themes SET downloads = downloads + 1 WHERE id = ?', [id]);
    res.json({ success: true, message: 'Dynamic island theme applied successfully' });
  } catch (err) {
    console.error('Error applying dynamic island theme:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// 🕒 ALWAYS-ON DISPLAY (AOD) CLOCKS & WIDGETS (Public App Endpoints)
// ========================================================

// GET /api/aod/clocks
router.get('/aod/clocks', async (req, res) => {
  try {
    const { clockType, search } = req.query;
    let whereClause = ['isActive = 1'];
    let params = [];

    if (clockType && clockType !== 'ALL') {
      whereClause.push('clockType = ?');
      params.push(clockType);
    }

    if (search && search.trim()) {
      whereClause.push('(title LIKE ? OR dialStyle LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q);
    }

    const sql = `
      SELECT * FROM aod_clocks 
      WHERE ${whereClause.join(' AND ')} 
      ORDER BY sortOrder ASC, createdAt DESC
    `;
    const rows = await db.allAsync(sql, params);

    const formatted = rows.map(r => ({
      id: r.id,
      title: r.title,
      clockType: r.clockType,
      accentColor: r.accentColor,
      glowColor: r.glowColor,
      textColor: r.textColor,
      backgroundColor: r.backgroundColor || '#000000',
      dialStyle: r.dialStyle,
      hasBatteryWidget: Boolean(r.hasBatteryWidget),
      hasDateWidget: Boolean(r.hasDateWidget),
      hasStepsWidget: Boolean(r.hasStepsWidget),
      hasWeatherWidget: Boolean(r.hasWeatherWidget),
      previewUrl: r.previewUrl,
      assetUrl: r.assetUrl,
      isPremium: Boolean(r.isPremium),
      downloads: r.downloads,
      isActive: Boolean(r.isActive),
      sortOrder: r.sortOrder
    }));

    res.json({
      success: true,
      total: formatted.length,
      data: formatted
    });
  } catch (err) {
    console.error('Error fetching AOD clocks:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/aod/clocks/:id
router.get('/aod/clocks/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const r = await db.getAsync('SELECT * FROM aod_clocks WHERE id = ?', [id]);
    if (!r) {
      return res.status(404).json({ success: false, error: 'AOD Clock Face not found' });
    }
    res.json({
      success: true,
      data: {
        id: r.id,
        title: r.title,
        clockType: r.clockType,
        accentColor: r.accentColor,
        glowColor: r.glowColor,
        textColor: r.textColor,
        backgroundColor: r.backgroundColor || '#000000',
        dialStyle: r.dialStyle,
        hasBatteryWidget: Boolean(r.hasBatteryWidget),
        hasDateWidget: Boolean(r.hasDateWidget),
        hasStepsWidget: Boolean(r.hasStepsWidget),
        hasWeatherWidget: Boolean(r.hasWeatherWidget),
        previewUrl: r.previewUrl,
        assetUrl: r.assetUrl,
        isPremium: Boolean(r.isPremium),
        downloads: r.downloads,
        isActive: Boolean(r.isActive)
      }
    });
  } catch (err) {
    console.error('Error fetching AOD clock details:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/aod/clocks/:id/apply
router.post('/aod/clocks/:id/apply', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync('UPDATE aod_clocks SET downloads = downloads + 1 WHERE id = ?', [id]);
    res.json({ success: true, message: 'AOD clock applied successfully' });
  } catch (err) {
    console.error('Error applying AOD clock:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ==========================================
// 📞 3D COLOR CALL SCREEN & FLASH THEMES
// ==========================================

// GET /api/call-screen/themes
router.get('/call-screen/themes', async (req, res) => {
  try {
    const { category, search } = req.query;
    let whereConditions = ['isActive = 1'];
    let params = [];

    if (category && category !== 'ALL') {
      whereConditions.push('category = ?');
      params.push(category.toUpperCase());
    }

    if (search && search.trim()) {
      whereConditions.push('(title LIKE ? OR category LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q);
    }

    const whereSql = `WHERE ${whereConditions.join(' AND ')}`;
    const rows = await db.allAsync(
      `SELECT * FROM call_themes ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`,
      params
    );

    res.json({
      success: true,
      count: rows.length,
      data: rows.map(r => ({
        id: r.id,
        title: r.title,
        category: r.category,
        backgroundUrl: r.backgroundUrl,
        previewUrl: r.previewUrl,
        buttonStyle: r.buttonStyle,
        accentColor: r.accentColor,
        glowColor: r.glowColor,
        flashAlertEnabled: Boolean(r.flashAlertEnabled),
        flashSpeed: r.flashSpeed || 'normal',
        ringtoneUrl: r.ringtoneUrl,
        isPremium: Boolean(r.isPremium),
        downloads: r.downloads,
        isActive: Boolean(r.isActive)
      }))
    });
  } catch (err) {
    console.error('Error fetching call screen themes:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/call-screen/themes/:id
router.get('/call-screen/themes/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const r = await db.getAsync('SELECT * FROM call_themes WHERE id = ?', [id]);
    if (!r) {
      return res.status(404).json({ success: false, error: 'Call Screen theme not found' });
    }
    res.json({
      success: true,
      data: {
        id: r.id,
        title: r.title,
        category: r.category,
        backgroundUrl: r.backgroundUrl,
        previewUrl: r.previewUrl,
        buttonStyle: r.buttonStyle,
        accentColor: r.accentColor,
        glowColor: r.glowColor,
        flashAlertEnabled: Boolean(r.flashAlertEnabled),
        flashSpeed: r.flashSpeed || 'normal',
        ringtoneUrl: r.ringtoneUrl,
        isPremium: Boolean(r.isPremium),
        downloads: r.downloads,
        isActive: Boolean(r.isActive)
      }
    });
  } catch (err) {
    console.error('Error fetching call theme details:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/call-screen/themes/:id/apply
router.post('/call-screen/themes/:id/apply', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync('UPDATE call_themes SET downloads = downloads + 1 WHERE id = ?', [id]);
    res.json({ success: true, message: 'Call Screen theme applied successfully' });
  } catch (err) {
    console.error('Error applying call theme:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// 👥 DUO / DOUBLE WALLPAPERS (Lock & Home Magic Pairs)
// ========================================================

// GET /api/duo/wallpapers
router.get('/duo/wallpapers', async (req, res) => {
  try {
    const { category, search } = req.query;
    let where = ['isActive = 1'];
    let params = [];

    if (category && category.toUpperCase() !== 'ALL') {
      where.push('category = ?');
      params.push(category.toUpperCase());
    }

    if (search && search.trim() !== '') {
      where.push('(title LIKE ? OR description LIKE ? OR category LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q, q);
    }

    const whereSql = `WHERE ${where.join(' AND ')}`;
    const rows = await db.allAsync(
      `SELECT * FROM duo_wallpapers ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`,
      params
    );

    res.json({
      success: true,
      count: rows.length,
      data: rows.map(r => ({
        id: r.id,
        title: r.title,
        description: r.description || '',
        category: r.category,
        lockImageUrl: r.lockImageUrl,
        homeImageUrl: r.homeImageUrl,
        previewUrl: r.previewUrl || r.homeImageUrl,
        accentColor: r.accentColor || '#00E5FF',
        isPremium: Boolean(r.isPremium),
        downloads: r.downloads,
        isActive: Boolean(r.isActive),
        sortOrder: r.sortOrder || 0,
        createdAt: r.createdAt
      }))
    });
  } catch (err) {
    console.error('Error fetching duo wallpapers:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/duo/wallpapers/:id
router.get('/duo/wallpapers/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const r = await db.getAsync('SELECT * FROM duo_wallpapers WHERE id = ?', [id]);
    if (!r) {
      return res.status(404).json({ success: false, error: 'Duo wallpaper pair not found' });
    }
    res.json({
      success: true,
      data: {
        id: r.id,
        title: r.title,
        description: r.description || '',
        category: r.category,
        lockImageUrl: r.lockImageUrl,
        homeImageUrl: r.homeImageUrl,
        previewUrl: r.previewUrl || r.homeImageUrl,
        accentColor: r.accentColor || '#00E5FF',
        isPremium: Boolean(r.isPremium),
        downloads: r.downloads,
        isActive: Boolean(r.isActive),
        sortOrder: r.sortOrder || 0,
        createdAt: r.createdAt
      }
    });
  } catch (err) {
    console.error('Error fetching duo wallpaper details:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/duo/wallpapers/:id/apply
router.post('/duo/wallpapers/:id/apply', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync('UPDATE duo_wallpapers SET downloads = downloads + 1 WHERE id = ?', [id]);
    res.json({ success: true, message: 'Duo wallpaper magic pair applied successfully' });
  } catch (err) {
    console.error('Error applying duo wallpaper:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// =========================================================================
// 👆 INTERACTIVE TOUCH FLUID & RIPPLE PRESETS APIS
// =========================================================================

// GET /api/touch-effects/presets
router.get('/touch-effects/presets', async (req, res) => {
  try {
    const { effectType, search } = req.query;
    let whereClauses = ['isActive = 1'];
    let params = [];

    if (effectType && effectType !== 'ALL') {
      whereClauses.push('effectType = ?');
      params.push(effectType);
    }

    if (search && search.trim()) {
      whereClauses.push('(title LIKE ? OR description LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q);
    }

    const whereSql = whereClauses.length > 0 ? `WHERE ${whereClauses.join(' AND ')}` : '';
    const rows = await db.allAsync(
      `SELECT * FROM touch_presets ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`,
      params
    );

    res.json({
      success: true,
      data: (rows || []).map(r => ({
        id: r.id,
        title: r.title,
        effectType: r.effectType,
        description: r.description || '',
        primaryColor: r.primaryColor || '#00E5FF',
        secondaryColor: r.secondaryColor || '#7000FF',
        accentColor: r.accentColor || '#FF007F',
        waveSpeed: r.waveSpeed || 1.0,
        waveRadius: r.waveRadius || 70,
        particleCount: r.particleCount || 80,
        viscosity: r.viscosity || 0.8,
        hapticEnabled: Boolean(r.hapticEnabled),
        isPremium: Boolean(r.isPremium),
        downloads: r.downloads || 0,
        isActive: Boolean(r.isActive),
        sortOrder: r.sortOrder || 1,
        createdAt: r.createdAt
      }))
    });
  } catch (err) {
    console.error('Error fetching touch presets:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/touch-effects/presets/:id
router.get('/touch-effects/presets/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const r = await db.getAsync('SELECT * FROM touch_presets WHERE id = ?', [id]);
    if (!r) {
      return res.status(404).json({ success: false, error: 'Touch preset not found' });
    }

    res.json({
      success: true,
      data: {
        id: r.id,
        title: r.title,
        effectType: r.effectType,
        description: r.description || '',
        primaryColor: r.primaryColor || '#00E5FF',
        secondaryColor: r.secondaryColor || '#7000FF',
        accentColor: r.accentColor || '#FF007F',
        waveSpeed: r.waveSpeed || 1.0,
        waveRadius: r.waveRadius || 70,
        particleCount: r.particleCount || 80,
        viscosity: r.viscosity || 0.8,
        hapticEnabled: Boolean(r.hapticEnabled),
        isPremium: Boolean(r.isPremium),
        downloads: r.downloads || 0,
        isActive: Boolean(r.isActive),
        sortOrder: r.sortOrder || 1,
        createdAt: r.createdAt
      }
    });
  } catch (err) {
    console.error('Error fetching touch preset details:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/touch-effects/presets/:id/apply
router.post('/touch-effects/presets/:id/apply', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync('UPDATE touch_presets SET downloads = downloads + 1 WHERE id = ?', [id]);
    res.json({ success: true, message: 'Touch preset applied successfully' });
  } catch (err) {
    console.error('Error applying touch preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ============================================================================
// 🔓 IN-DISPLAY FINGERPRINT ANIMATION EFFECTS PUBLIC APIS
// ============================================================================

// GET /api/fingerprint/presets
router.get('/fingerprint/presets', async (req, res) => {
  try {
    const { category, animationType, search } = req.query;
    let whereConditions = ['isActive = 1'];
    let params = [];

    if (category && category !== 'ALL') {
      whereConditions.push('category = ?');
      params.push(category.toUpperCase());
    }

    if (animationType && animationType !== 'ALL') {
      whereConditions.push('animationType = ?');
      params.push(animationType);
    }

    if (search && search.trim()) {
      whereConditions.push('(title LIKE ? OR description LIKE ?)');
      params.push(`%${search.trim()}%`, `%${search.trim()}%`);
    }

    const whereSql = whereConditions.length ? `WHERE ${whereConditions.join(' AND ')}` : '';
    const rows = await db.allAsync(
      `SELECT * FROM fingerprint_presets ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`,
      params
    );

    const data = rows.map(r => ({
      id: r.id,
      title: r.title,
      category: r.category,
      animationType: r.animationType,
      description: r.description || '',
      primaryColor: r.primaryColor || '#00E5FF',
      secondaryColor: r.secondaryColor || '#7000FF',
      accentGlow: r.accentGlow || '#00FFAA',
      animationSpeed: r.animationSpeed || 1.2,
      scale: r.scale || 1.0,
      verticalPosition: r.verticalPosition || 78,
      hapticEnabled: Boolean(r.hapticEnabled),
      soundEnabled: Boolean(r.soundEnabled),
      isPremium: Boolean(r.isPremium),
      downloads: r.downloads || 0,
      isActive: Boolean(r.isActive),
      sortOrder: r.sortOrder || 1,
      createdAt: r.createdAt
    }));

    res.json({ success: true, data });
  } catch (err) {
    console.error('Error fetching fingerprint presets:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/fingerprint/presets/:id
router.get('/fingerprint/presets/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const r = await db.getAsync('SELECT * FROM fingerprint_presets WHERE id = ?', [id]);
    if (!r) {
      return res.status(404).json({ success: false, error: 'Fingerprint preset not found' });
    }

    res.json({
      success: true,
      data: {
        id: r.id,
        title: r.title,
        category: r.category,
        animationType: r.animationType,
        description: r.description || '',
        primaryColor: r.primaryColor || '#00E5FF',
        secondaryColor: r.secondaryColor || '#7000FF',
        accentGlow: r.accentGlow || '#00FFAA',
        animationSpeed: r.animationSpeed || 1.2,
        scale: r.scale || 1.0,
        verticalPosition: r.verticalPosition || 78,
        hapticEnabled: Boolean(r.hapticEnabled),
        soundEnabled: Boolean(r.soundEnabled),
        isPremium: Boolean(r.isPremium),
        downloads: r.downloads || 0,
        isActive: Boolean(r.isActive),
        sortOrder: r.sortOrder || 1,
        createdAt: r.createdAt
      }
    });
  } catch (err) {
    console.error('Error fetching fingerprint preset details:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/fingerprint/presets/:id/apply
router.post('/fingerprint/presets/:id/apply', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync('UPDATE fingerprint_presets SET downloads = downloads + 1 WHERE id = ?', [id]);
    res.json({ success: true, message: 'Fingerprint preset applied successfully' });
  } catch (err) {
    console.error('Error applying fingerprint preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
