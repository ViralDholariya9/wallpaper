const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const fs = require('fs');

const dataDir = path.join(__dirname, '..', 'data');
if (!fs.existsSync(dataDir)) {
  fs.mkdirSync(dataDir, { recursive: true });
}

const dbPath = path.join(dataDir, 'rewall.sqlite');
let currentDb = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    console.error('❌ Failed to connect to SQLite database:', err.message);
  } else {
    console.log('✅ Connected to SQLite database:', dbPath);
  }
});
currentDb.run('PRAGMA foreign_keys = ON');

// Stable DB Proxy wrapper: allows safe dynamic reconnects and disaster restores
// without invalidating route handlers or closed C++ handles.
const db = {
  run(sql, params, callback) {
    return currentDb.run(sql, params, callback);
  },
  get(sql, params, callback) {
    return currentDb.get(sql, params, callback);
  },
  all(sql, params, callback) {
    return currentDb.all(sql, params, callback);
  },
  exec(sql, callback) {
    return currentDb.exec(sql, callback);
  },
  serialize(callback) {
    return currentDb.serialize(callback);
  },
  close(callback) {
    return currentDb.close(callback);
  },
  allAsync(sql, params = []) {
    return new Promise((resolve, reject) => {
      currentDb.all(sql, params, (err, rows) => {
        if (err) reject(err);
        else resolve(rows);
      });
    });
  },
  getAsync(sql, params = []) {
    return new Promise((resolve, reject) => {
      currentDb.get(sql, params, (err, row) => {
        if (err) reject(err);
        else resolve(row);
      });
    });
  },
  runAsync(sql, params = []) {
    return new Promise((resolve, reject) => {
      currentDb.run(sql, params, function (err) {
        if (err) reject(err);
        else resolve({ lastID: this.lastID, changes: this.changes });
      });
    });
  }
};

const defaultPersonalizationSuite = [
  {
    id: "ai_studio",
    title: "AI Studio",
    gujaratiTitle: "AI સ્ટુડિયો",
    subtitle: "Prompt to 4K 3D Art",
    badge: "GEMINI AI",
    iconEmoji: "🎨",
    enabled: true,
    order: 0
  },
  {
    id: "edge_lighting",
    title: "Edge Lighting",
    gujaratiTitle: "એજ લાઈટિંગ",
    subtitle: "RGB Screen & Notch Glow",
    badge: "RGB NEON",
    iconEmoji: "⚡",
    enabled: true,
    order: 1
  },
  {
    id: "dynamic_island",
    title: "Dynamic Island",
    gujaratiTitle: "ડાયનેમિક આઈલેન્ડ",
    subtitle: "Camera Notch Smart Capsule",
    badge: "SMART HUD",
    iconEmoji: "🏝️",
    enabled: true,
    order: 2
  },
  {
    id: "aod",
    title: "Always-On Display",
    gujaratiTitle: "AOD ક્લોક્સ",
    subtitle: "AMOLED Clocks & Battery HUD",
    badge: "AMOLED",
    iconEmoji: "🕒",
    enabled: true,
    order: 3
  },
  {
    id: "call_screen",
    title: "Color Call Screen",
    gujaratiTitle: "કલર કોલ સ્ક્રીન",
    subtitle: "3D Video Themes & Flash",
    badge: "3D CALL",
    iconEmoji: "📞",
    enabled: true,
    order: 4
  },
  {
    id: "duo_wallpapers",
    title: "Duo Wallpapers",
    gujaratiTitle: "ડ્યૂઓ જોડી",
    subtitle: "Matching Lock & Home Pairs",
    badge: "MAGIC PAIR",
    iconEmoji: "👥",
    enabled: true,
    order: 5
  },
  {
    id: "touch_fluid",
    title: "Touch Fluid FX",
    gujaratiTitle: "ટચ ફ્લુઈડ",
    subtitle: "Water Ripples & Smoke Swirls",
    badge: "4D LIVE",
    iconEmoji: "👆",
    enabled: true,
    order: 6
  },
  {
    id: "fingerprint_fx",
    title: "Fingerprint FX",
    gujaratiTitle: "ફિંગરપ્રિન્ટ FX",
    subtitle: "Biometric HUD & Supernova",
    badge: "BIOMETRIC",
    iconEmoji: "🔓",
    enabled: true,
    order: 7
  }
];

// Initialize schema
async function initSchema() {
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS wallpapers (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      category TEXT NOT NULL,
      previewUrl TEXT NOT NULL,
      fullUrl TEXT NOT NULL,
      isParallax INTEGER DEFAULT 0,
      downloads INTEGER DEFAULT 0,
      likes INTEGER DEFAULT 0,
      isPremium INTEGER DEFAULT 0,
      isUnlocked INTEGER DEFAULT 1,
      createdAt INTEGER NOT NULL
    )
  `);

  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS wallpaper_layers (
      id TEXT PRIMARY KEY,
      wallpaperId TEXT NOT NULL,
      imageUrl TEXT NOT NULL,
      depth REAL NOT NULL,
      sortOrder INTEGER DEFAULT 0,
      FOREIGN KEY(wallpaperId) REFERENCES wallpapers(id) ON DELETE CASCADE
    )
  `);

  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS categories (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      iconUrl TEXT
    )
  `);

  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS ringtones (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      artist TEXT NOT NULL,
      audioUrl TEXT NOT NULL,
      durationSeconds INTEGER DEFAULT 30,
      category TEXT NOT NULL,
      downloads INTEGER DEFAULT 0
    )
  `);

  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS app_config (
      key TEXT PRIMARY KEY,
      value TEXT NOT NULL
    )
  `);

  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS hero_banners (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      subtitle TEXT,
      badgeText TEXT,
      imageUrl TEXT NOT NULL,
      actionType TEXT DEFAULT 'category',
      actionTarget TEXT,
      sortOrder INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default sample hero banners if empty
  const bannerCountRow = await db.getAsync('SELECT COUNT(*) as count FROM hero_banners');
  if (bannerCountRow && bannerCountRow.count === 0) {
    const now = Date.now();
    const defaultBanners = [
      [
        'b_diwali_3d',
        '🔥 Diwali 3D Festival Drop!',
        'Handcrafted 4K multi-layer festival parallax wallpapers',
        'FESTIVAL SPECIAL',
        'https://images.unsplash.com/photo-1514565131-fce0801e5785?w=1200&auto=format&fit=crop&q=80',
        'category',
        'Space',
        1,
        1,
        now
      ],
      [
        'b_cyber_samurai',
        '⚡ Cyber Samurai 3D',
        'Ultra-fluid 60FPS gyroscope parallax action',
        'POPULAR 3D',
        'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=1200&auto=format&fit=crop&q=80',
        'wallpaper',
        'w1',
        2,
        1,
        now - 1000
      ],
      [
        'b_cosmic_nebula',
        '🌌 Cosmic Nebula 4K',
        'Deep space live stars & intergalactic depth',
        'TRENDING',
        'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200&auto=format&fit=crop&q=80',
        'category',
        'Space',
        3,
        1,
        now - 2000
      ]
    ];

    for (const b of defaultBanners) {
      await db.runAsync(`
        INSERT INTO hero_banners (id, title, subtitle, badgeText, imageUrl, actionType, actionTarget, sortOrder, isActive, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, b);
    }
  }

  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS trending_tags (
      id TEXT PRIMARY KEY,
      tag TEXT NOT NULL,
      icon TEXT DEFAULT '🔥',
      sortOrder INTEGER DEFAULT 0,
      clickCount INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default sample trending search tags if empty
  const tagCountRow = await db.getAsync('SELECT COUNT(*) as count FROM trending_tags');
  if (tagCountRow && tagCountRow.count === 0) {
    const now = Date.now();
    const defaultTags = [
      ['tag_1', '#3D Parallax', '✨', 1, 1420, 1, now],
      ['tag_2', '#Diwali Special', '🪔', 2, 1890, 1, now - 1000],
      ['tag_3', '#Cyberpunk', '⚡', 3, 950, 1, now - 2000],
      ['tag_4', '#Anime', '🌸', 4, 1210, 1, now - 3000],
      ['tag_5', '#Mahadev', '🕉️', 5, 1650, 1, now - 4000],
      ['tag_6', '#Cars 4K', '🏎️', 6, 880, 1, now - 5000],
      ['tag_7', '#AMOLED Dark', '🖤', 7, 760, 1, now - 6000],
      ['tag_8', '#Deep Space', '🌌', 8, 640, 1, now - 7000]
    ];

    for (const t of defaultTags) {
      await db.runAsync(`
        INSERT INTO trending_tags (id, tag, icon, sortOrder, clickCount, isActive, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?)
      `, t);
    }
  }

  // 🏷️ FESTIVAL & EVENT BANNER SCHEDULER TABLE
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS festival_events (
      id TEXT PRIMARY KEY,
      name TEXT NOT NULL,
      festivalKey TEXT NOT NULL,
      bannerTitle TEXT NOT NULL,
      bannerSubtitle TEXT,
      bannerBadge TEXT DEFAULT 'FESTIVAL SPECIAL',
      bannerImageUrl TEXT NOT NULL,
      targetCategory TEXT DEFAULT 'Festival',
      targetTags TEXT DEFAULT '[]',
      startDate TEXT NOT NULL,
      endDate TEXT NOT NULL,
      isActive INTEGER DEFAULT 1,
      forceLive INTEGER DEFAULT 0,
      priority INTEGER DEFAULT 10,
      sortOrder INTEGER DEFAULT 0,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default curated festival campaigns if empty
  const eventCountRow = await db.getAsync('SELECT COUNT(*) as count FROM festival_events');
  if (eventCountRow && eventCountRow.count === 0) {
    const now = Date.now();
    const defaultEvents = [
      [
        'event_diwali_2026',
        '🪔 Diwali Grand Festival 2026',
        'diwali',
        '🔥 Shubh Deepavali 3D Dhamaka Drop!',
        'Handcrafted 4K festive multi-layer live diyas & firework parallax wallpapers',
        '🪔 DIWALI SPECIAL',
        'https://images.unsplash.com/photo-1514565131-fce0801e5785?w=1200&auto=format&fit=crop&q=80',
        'Festival',
        JSON.stringify(['#DiwaliSpecial', '#ShubhDiwali', '#3DFireworks', '#Diyas4K']),
        '2026-10-25T00:00:00',
        '2026-11-05T23:59:59',
        1,
        1, // Force live for immediate preview
        10,
        1,
        now
      ],
      [
        'event_navratri_2026',
        '💃 Navratri Garba Nights 2026',
        'navratri',
        '⚡ Navratri 3D Dandiya Nights',
        'Divine Maa Durga & vibrant illuminated garba live gyro themes',
        '💃 NAVRATRI SPECIAL',
        'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop&q=80',
        'Festival',
        JSON.stringify(['#NavratriGarba', '#Dandiya3D', '#MaaDurga', '#GarbaVibes']),
        '2026-10-10T00:00:00',
        '2026-10-20T23:59:59',
        1,
        0,
        8,
        2,
        now - 1000
      ],
      [
        'event_newyear_2027',
        '🎆 New Year 2027 Countdown',
        'new_year',
        '✨ Welcome 2027 Cyber Parallax',
        'Neon countdowns, champagne sparks & cyber celebration 3D drops',
        '🎆 NEW YEAR 2027',
        'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80',
        'Cyberpunk',
        JSON.stringify(['#NewYear2027', '#HappyNewYear', '#MidnightCountdown', '#2027Vibes']),
        '2026-12-28T00:00:00',
        '2027-01-05T23:59:59',
        1,
        0,
        9,
        3,
        now - 2000
      ],
      [
        'event_uttarayan_2027',
        '🪁 Uttarayan Kite Festival 2027',
        'uttarayan',
        '🪁 Kai Po Che! 3D Sky Parallax',
        'Colorful kites soaring in vivid dynamic azure skies with depth motion',
        '🪁 KITE FESTIVAL',
        'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=1200&auto=format&fit=crop&q=80',
        'Nature',
        JSON.stringify(['#Uttarayan2027', '#KaiPoChe', '#MakarSankranti', '#KiteFlying']),
        '2027-01-12T00:00:00',
        '2027-01-16T23:59:59',
        1,
        0,
        7,
        4,
        now - 3000
      ],
      [
        'event_holi_2027',
        '🎨 Holi Festival of Colors 2027',
        'holi',
        '🌈 Rang Barse 3D Fluid Splash',
        'Gulal color powder explosions and liquid fluid touch interactions',
        '🎨 HOLI SPECIAL',
        'https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1200&auto=format&fit=crop&q=80',
        'Festival',
        JSON.stringify(['#Holi2027', '#RangBarse', '#ColorSplash3D', '#HappyHoli']),
        '2027-03-20T00:00:00',
        '2027-03-25T23:59:59',
        1,
        0,
        8,
        5,
        now - 4000
      ]
    ];

    for (const e of defaultEvents) {
      await db.runAsync(`
        INSERT INTO festival_events (id, name, festivalKey, bannerTitle, bannerSubtitle, bannerBadge, bannerImageUrl, targetCategory, targetTags, startDate, endDate, isActive, forceLive, priority, sortOrder, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, e);
    }
  }

  // ⚡ DYNAMIC CHARGING ANIMATIONS TABLE
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS charging_animations (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      category TEXT DEFAULT 'NEON',
      previewUrl TEXT NOT NULL,
      animationUrl TEXT NOT NULL,
      animationType TEXT DEFAULT 'lottie',
      soundUrl TEXT,
      textColor TEXT DEFAULT '#00E5FF',
      isPremium INTEGER DEFAULT 0,
      downloads INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      sortOrder INTEGER DEFAULT 0,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default curated charging animations if empty
  const chargingCountRow = await db.getAsync('SELECT COUNT(*) as count FROM charging_animations');
  if (chargingCountRow && chargingCountRow.count === 0) {
    const now = Date.now();
    const defaultAnimations = [
      [
        'ca_neon_arc',
        '⚡ Neon Cyber Arc Reactor',
        'NEON',
        '/uploads/charging/thumb_neon_arc.svg',
        '/uploads/charging/neon_cyber_arc.json',
        'lottie',
        null,
        '#00E5FF',
        0,
        1850,
        1,
        1,
        now
      ],
      [
        'ca_quantum_vortex',
        '🌌 Quantum Cosmic Vortex',
        '3D_PARTICLE',
        '/uploads/charging/thumb_quantum_vortex.svg',
        '/uploads/charging/quantum_vortex.json',
        'lottie',
        null,
        '#B026FF',
        0,
        2420,
        1,
        2,
        now - 1000
      ],
      [
        'ca_amoled_liquid',
        '🧪 AMOLED Toxic Green Liquid',
        'LIQUID',
        '/uploads/charging/thumb_liquid_bubble.svg',
        '/uploads/charging/liquid_bubble_flow.json',
        'lottie',
        null,
        '#00FF88',
        0,
        3100,
        1,
        3,
        now - 2000
      ],
      [
        'ca_speed_nitro',
        '🔥 High-Voltage Lightning Bolt',
        'CYBERPUNK',
        '/uploads/charging/thumb_lightning.svg',
        '/uploads/charging/lightning_turbo.json',
        'lottie',
        null,
        '#FFB800',
        0,
        1670,
        1,
        4,
        now - 3000
      ],
      [
        'ca_minimal_zen',
        '⚪ Minimalist Pure Zen Ring',
        'MINIMAL',
        '/uploads/charging/thumb_zen_ring.svg',
        '/uploads/charging/minimal_zen.json',
        'lottie',
        null,
        '#FFFFFF',
        0,
        920,
        1,
        5,
        now - 4000
      ]
    ];

    for (const ca of defaultAnimations) {
      await db.runAsync(`
        INSERT INTO charging_animations (id, title, category, previewUrl, animationUrl, animationType, soundUrl, textColor, isPremium, downloads, isActive, sortOrder, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, ca);
    }
    console.log('⚡ Initialized 5 curated default charging animations in SQLite');
  }

  // 🌈 DYNAMIC EDGE LIGHTING & BORDER ANIMATIONS TABLE
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS edge_lighting_presets (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      category TEXT DEFAULT 'RAINBOW',
      animationType TEXT DEFAULT 'rainbow_wave',
      colors TEXT NOT NULL,
      speed REAL DEFAULT 1.0,
      borderSize INTEGER DEFAULT 5,
      cornerRadius INTEGER DEFAULT 28,
      punchHoleRadius INTEGER DEFAULT 0,
      glowSpread INTEGER DEFAULT 12,
      previewUrl TEXT,
      isPremium INTEGER DEFAULT 0,
      downloads INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      sortOrder INTEGER DEFAULT 0,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default curated Edge Lighting presets if empty
  const edgeCountRow = await db.getAsync('SELECT COUNT(*) as count FROM edge_lighting_presets');
  if (edgeCountRow && edgeCountRow.count === 0) {
    const now = Date.now();
    const defaultEdgePresets = [
      [
        'el_rgb_rainbow',
        '🌈 RGB 360° Rainbow Wave',
        'RAINBOW',
        'rainbow_wave',
        JSON.stringify(['#FF0055', '#FF7700', '#FFE600', '#00FF66', '#00E5FF', '#7000FF', '#FF0055']),
        1.2,
        6,
        32,
        0,
        14,
        '/uploads/edge/thumb_rgb_rainbow.svg',
        0,
        3820,
        1,
        1,
        now
      ],
      [
        'el_cyber_snake',
        '⚡ Cyber Snake Comet Dual-Head',
        'CYBER',
        'snake_comet',
        JSON.stringify(['#00E5FF', '#7000FF', '#00E5FF']),
        1.5,
        5,
        30,
        0,
        16,
        '/uploads/edge/thumb_cyber_snake.svg',
        0,
        2910,
        1,
        2,
        now - 1000
      ],
      [
        'el_neon_pulse',
        '💖 Electric Neon Breathing Pulse',
        'NEON',
        'pulse_glow',
        JSON.stringify(['#FF007F', '#9B00FF', '#00FFFF']),
        0.9,
        7,
        32,
        0,
        20,
        '/uploads/edge/thumb_neon_pulse.svg',
        0,
        2450,
        1,
        3,
        now - 2000
      ],
      [
        'el_galaxy_glow',
        '🌌 Deep Cosmic Galaxy Drift',
        'GALAXY',
        'galaxy_flow',
        JSON.stringify(['#4A00E0', '#8E2DE2', '#00C9FF', '#92FE9D']),
        0.8,
        5,
        28,
        0,
        15,
        '/uploads/edge/thumb_galaxy_glow.svg',
        0,
        1980,
        1,
        4,
        now - 3000
      ],
      [
        'el_solar_flare',
        '🔥 Solar Flare & Golden Flame',
        'FIRE',
        'rainbow_wave',
        JSON.stringify(['#FF3E00', '#FF8500', '#FFD200', '#FF1E56']),
        1.3,
        6,
        30,
        0,
        18,
        '/uploads/edge/thumb_solar_flare.svg',
        0,
        3120,
        1,
        5,
        now - 4000
      ],
      [
        'el_punch_aura',
        '🎯 Camera Punch-Hole Aura + Dual Edge',
        'NOTCH',
        'punch_hole',
        JSON.stringify(['#00FFCC', '#0072FF', '#00FFCC']),
        1.1,
        4,
        34,
        18,
        16,
        '/uploads/edge/thumb_punch_aura.svg',
        0,
        1650,
        1,
        6,
        now - 5000
      ]
    ];

    for (const ep of defaultEdgePresets) {
      await db.runAsync(`
        INSERT INTO edge_lighting_presets (id, title, category, animationType, colors, speed, borderSize, cornerRadius, punchHoleRadius, glowSpread, previewUrl, isPremium, downloads, isActive, sortOrder, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, ep);
    }
    console.log('🌈 Initialized 6 curated default Edge Lighting presets in SQLite');
  }

  // 🏝️ DYNAMIC ISLAND & NOTIFICATION CAPSULE THEMES TABLE
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS dynamic_island_themes (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      styleType TEXT DEFAULT 'minimal',
      backgroundColor TEXT DEFAULT '#000000',
      textColor TEXT DEFAULT '#FFFFFF',
      accentColor TEXT DEFAULT '#00E5FF',
      glowColor TEXT DEFAULT 'rgba(0,229,255,0.4)',
      cornerRadius INTEGER DEFAULT 24,
      compactWidth INTEGER DEFAULT 120,
      compactHeight INTEGER DEFAULT 36,
      expandedWidth INTEGER DEFAULT 320,
      expandedHeight INTEGER DEFAULT 84,
      previewUrl TEXT,
      isPremium INTEGER DEFAULT 0,
      downloads INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      sortOrder INTEGER DEFAULT 0,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default curated Dynamic Island themes if empty
  const islandCountRow = await db.getAsync('SELECT COUNT(*) as count FROM dynamic_island_themes');
  if (islandCountRow && islandCountRow.count === 0) {
    const now = Date.now();
    const defaultIslandThemes = [
      [
        'di_apple_obsidian',
        '🍎 Apple Minimalist Obsidian',
        'minimal',
        '#000000',
        '#FFFFFF',
        '#00E5FF',
        'rgba(255,255,255,0.15)',
        24,
        120,
        36,
        320,
        84,
        '/uploads/island/thumb_apple_obsidian.svg',
        0,
        4520,
        1,
        1,
        now
      ],
      [
        'di_cyberpunk_neon',
        '⚡ Cyberpunk Neon Arc Reactor',
        'cyberpunk',
        '#080C16',
        '#00E5FF',
        '#7000FF',
        'rgba(0,229,255,0.5)',
        26,
        128,
        38,
        330,
        88,
        '/uploads/island/thumb_cyberpunk_neon.svg',
        0,
        3890,
        1,
        2,
        now - 1000
      ],
      [
        'di_frosted_glass',
        '💎 Frosted Glassmorphism Aero',
        'glassmorphic',
        'rgba(25,30,45,0.75)',
        '#FFFFFF',
        '#00FF88',
        'rgba(0,255,136,0.3)',
        28,
        124,
        36,
        324,
        86,
        '/uploads/island/thumb_frosted_glass.svg',
        0,
        3140,
        1,
        3,
        now - 2000
      ],
      [
        'di_amoled_void',
        '🖤 AMOLED Pure Zero-Battery Void',
        'amoled',
        '#000000',
        '#E0E0E0',
        '#FFFFFF',
        'rgba(255,255,255,0.08)',
        22,
        115,
        34,
        310,
        80,
        '/uploads/island/thumb_amoled_void.svg',
        0,
        2780,
        1,
        4,
        now - 3000
      ],
      [
        'di_pastel_aurora',
        '🌸 Pastel Aurora Cloud',
        'gradient',
        '#1A1428',
        '#FFD6E8',
        '#FF007F',
        'rgba(255,0,127,0.4)',
        28,
        126,
        38,
        326,
        88,
        '/uploads/island/thumb_pastel_aurora.svg',
        0,
        2340,
        1,
        5,
        now - 4000
      ],
      [
        'di_golden_vip',
        '👑 Golden VIP Sovereign Crown',
        'luxury',
        '#120F08',
        '#FFD700',
        '#FF9900',
        'rgba(255,215,0,0.45)',
        25,
        130,
        38,
        330,
        90,
        '/uploads/island/thumb_golden_vip.svg',
        1,
        1920,
        1,
        6,
        now - 5000
      ]
    ];

    for (const it of defaultIslandThemes) {
      await db.runAsync(`
        INSERT INTO dynamic_island_themes (id, title, styleType, backgroundColor, textColor, accentColor, glowColor, cornerRadius, compactWidth, compactHeight, expandedWidth, expandedHeight, previewUrl, isPremium, downloads, isActive, sortOrder, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, it);
    }
    console.log('🏝️ Initialized 6 curated default Dynamic Island themes in SQLite');
  }

  // 🕒 ALWAYS-ON DISPLAY (AOD) CLOCKS & WIDGETS TABLE
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS aod_clocks (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      clockType TEXT DEFAULT 'cyberpunk_digital',
      accentColor TEXT DEFAULT '#00E5FF',
      glowColor TEXT DEFAULT '#00E5FF',
      textColor TEXT DEFAULT '#FFFFFF',
      backgroundColor TEXT DEFAULT '#000000',
      dialStyle TEXT DEFAULT 'futuristic_hud',
      hasBatteryWidget INTEGER DEFAULT 1,
      hasDateWidget INTEGER DEFAULT 1,
      hasStepsWidget INTEGER DEFAULT 1,
      hasWeatherWidget INTEGER DEFAULT 1,
      previewUrl TEXT,
      assetUrl TEXT,
      isPremium INTEGER DEFAULT 0,
      downloads INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      sortOrder INTEGER DEFAULT 0,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default curated AOD clock faces if empty
  const aodCountRow = await db.getAsync('SELECT COUNT(*) as count FROM aod_clocks');
  if (aodCountRow && aodCountRow.count === 0) {
    const now = Date.now();
    const defaultAodClocks = [
      [
        'aod_cyberpunk_2077',
        '⚡ Cyberpunk Neon HUD 2077',
        'cyberpunk_digital',
        '#00E5FF',
        '#7000FF',
        '#FFFFFF',
        '#000000',
        'futuristic_hud',
        1, 1, 1, 1,
        '/uploads/aod/aod_cyberpunk_2077.svg',
        '/uploads/aod/aod_cyberpunk_2077.svg',
        0,
        6840,
        1,
        1,
        now
      ],
      [
        'aod_zenith_analog',
        '⌚ Zenith Minimalist Luxury Analog',
        'minimalist_analog',
        '#E2E8F0',
        '#38BDF8',
        '#F8FAFC',
        '#000000',
        'minimalist_ticks',
        1, 1, 1, 1,
        '/uploads/aod/aod_zenith_analog.svg',
        '/uploads/aod/aod_zenith_analog.svg',
        0,
        5420,
        1,
        2,
        now - 1000
      ],
      [
        'aod_matrix_typography',
        '🔤 Matrix Typography Word Clock',
        'typography_word',
        '#22C55E',
        '#16A34A',
        '#DCFCE7',
        '#000000',
        'word_matrix',
        1, 1, 0, 1,
        '/uploads/aod/aod_matrix_typography.svg',
        '/uploads/aod/aod_matrix_typography.svg',
        0,
        4190,
        1,
        3,
        now - 2000
      ],
      [
        'aod_neon_kitsune',
        '🦊 Neon Cyber Kitsune',
        'neon_animal',
        '#FF2A85',
        '#FF7170',
        '#FFFFFF',
        '#000000',
        'polygonal_fox',
        1, 1, 1, 0,
        '/uploads/aod/aod_neon_kitsune.svg',
        '/uploads/aod/aod_neon_kitsune.svg',
        0,
        7250,
        1,
        4,
        now - 3000
      ],
      [
        'aod_gamer_stamina',
        '🎮 Gamer HUD Stamina Dial',
        'gaming_hud',
        '#F59E0B',
        '#EF4444',
        '#FEF3C7',
        '#000000',
        'sci_fi_stamina',
        1, 1, 1, 1,
        '/uploads/aod/aod_gamer_stamina.svg',
        '/uploads/aod/aod_gamer_stamina.svg',
        0,
        6110,
        1,
        5,
        now - 4000
      ],
      [
        'aod_celestial_void',
        '👑 Celestial VIP Gold Constellation',
        'minimalist_analog',
        '#FFD700',
        '#F59E0B',
        '#FFFBEB',
        '#000000',
        'celestial_ring',
        1, 1, 0, 1,
        '/uploads/aod/aod_celestial_void.svg',
        '/uploads/aod/aod_celestial_void.svg',
        1,
        8900,
        1,
        6,
        now - 5000
      ]
    ];

    for (const c of defaultAodClocks) {
      await db.runAsync(`
        INSERT INTO aod_clocks (id, title, clockType, accentColor, glowColor, textColor, backgroundColor, dialStyle, hasBatteryWidget, hasDateWidget, hasStepsWidget, hasWeatherWidget, previewUrl, assetUrl, isPremium, downloads, isActive, sortOrder, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, c);
    }
    console.log('🕒 Initialized 6 curated default AOD clock faces in SQLite');
  }

  // 📞 3D COLOR CALL SCREEN & FLASH THEMES TABLE
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS call_themes (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      category TEXT DEFAULT 'NEON',
      backgroundUrl TEXT NOT NULL,
      previewUrl TEXT NOT NULL,
      buttonStyle TEXT DEFAULT 'neon_glow',
      accentColor TEXT DEFAULT '#00E5FF',
      glowColor TEXT DEFAULT '#7000FF',
      flashAlertEnabled INTEGER DEFAULT 1,
      flashSpeed TEXT DEFAULT 'normal',
      ringtoneUrl TEXT,
      isPremium INTEGER DEFAULT 0,
      downloads INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      sortOrder INTEGER DEFAULT 0,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default curated Call Screen themes if empty
  const callCountRow = await db.getAsync('SELECT COUNT(*) as count FROM call_themes');
  if (callCountRow && callCountRow.count === 0) {
    const now = Date.now();
    const defaultCallThemes = [
      [
        'call_cyber_matrix_2077',
        '⚡ Cyberpunk Matrix 2077',
        'CYBERPUNK',
        '/uploads/callscreen/call_cyber_matrix_2077.svg',
        '/uploads/callscreen/call_cyber_matrix_2077.svg',
        'neon_glow',
        '#00E5FF',
        '#7000FF',
        1,
        'strobe',
        null,
        0,
        14280,
        1,
        1,
        now
      ],
      [
        'call_royal_gold_wave',
        '✨ Royal Gold Silk Wave',
        'LUXURY',
        '/uploads/callscreen/call_royal_gold_wave.svg',
        '/uploads/callscreen/call_royal_gold_wave.svg',
        'glassmorphism',
        '#FFD700',
        '#FFA000',
        1,
        'normal',
        null,
        1,
        19850,
        1,
        2,
        now - 1000
      ],
      [
        'call_galaxy_supernova',
        '🌌 Deep Galaxy Supernova',
        '3D_PARALLAX',
        '/uploads/callscreen/call_galaxy_supernova.svg',
        '/uploads/callscreen/call_galaxy_supernova.svg',
        'neon_glow',
        '#B388FF',
        '#7C4DFF',
        1,
        'normal',
        null,
        0,
        11340,
        1,
        3,
        now - 2000
      ],
      [
        'call_anime_thunder_god',
        '⚡ Anime Thunder God',
        'ANIME',
        '/uploads/callscreen/call_anime_thunder_god.svg',
        '/uploads/callscreen/call_anime_thunder_god.svg',
        'retro_cyber',
        '#FF1744',
        '#FF5252',
        1,
        'strobe',
        null,
        0,
        16920,
        1,
        4,
        now - 3000
      ],
      [
        'call_aurora_borealis',
        '🌈 Mystic Northern Lights',
        'NEON',
        '/uploads/callscreen/call_aurora_borealis.svg',
        '/uploads/callscreen/call_aurora_borealis.svg',
        'minimal_flat',
        '#00E676',
        '#1DE9B6',
        1,
        'slow',
        null,
        0,
        8750,
        1,
        5,
        now - 4000
      ],
      [
        'call_synthwave_sunset_80s',
        '🌴 Retro 80s Synthwave Grid',
        'NEON',
        '/uploads/callscreen/call_synthwave_sunset_80s.svg',
        '/uploads/callscreen/call_synthwave_sunset_80s.svg',
        'retro_cyber',
        '#FF007F',
        '#7928CA',
        1,
        'strobe',
        null,
        0,
        13410,
        1,
        6,
        now - 5000
      ]
    ];

    for (const t of defaultCallThemes) {
      await db.runAsync(`
        INSERT INTO call_themes (id, title, category, backgroundUrl, previewUrl, buttonStyle, accentColor, glowColor, flashAlertEnabled, flashSpeed, ringtoneUrl, isPremium, downloads, isActive, sortOrder, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, t);
    }
    console.log('📞 Initialized 6 curated default 3D Color Call Screen themes in SQLite');
  }

  // 👥 DUO / DOUBLE WALLPAPERS TABLE (Lock & Home Magic Pairs)
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS duo_wallpapers (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      description TEXT,
      category TEXT DEFAULT 'CYBERPUNK',
      lockImageUrl TEXT NOT NULL,
      homeImageUrl TEXT NOT NULL,
      previewUrl TEXT,
      accentColor TEXT DEFAULT '#00E5FF',
      isPremium INTEGER DEFAULT 0,
      downloads INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      sortOrder INTEGER DEFAULT 0,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default curated Duo Wallpaper pairs if empty
  const duoCountRow = await db.getAsync('SELECT COUNT(*) as count FROM duo_wallpapers');
  if (duoCountRow && duoCountRow.count === 0) {
    const now = Date.now();
    const defaultDuoPairs = [
      [
        'duo_cyber_samurai',
        '⚡ Cyber Samurai: Dormant to Awakened',
        'Meditating cyber warrior on lock screen awakens with glowing neon eyes and blazing energy katana on home screen.',
        'CYBERPUNK',
        '/uploads/duo/duo_cyber_samurai_lock.svg',
        '/uploads/duo/duo_cyber_samurai_home.svg',
        '/uploads/duo/duo_cyber_samurai_home.svg',
        '#00E5FF',
        0,
        15420,
        1,
        1,
        now
      ],
      [
        'duo_skyline_timelapse',
        '🌆 Metropolis 4K: Sunset to Cyberpunk',
        'Golden hour warm sunset skyline transforms into vibrant midnight cyberpunk with glowing neon billboards.',
        'LANDSCAPE',
        '/uploads/duo/duo_skyline_timelapse_lock.svg',
        '/uploads/duo/duo_skyline_timelapse_home.svg',
        '/uploads/duo/duo_skyline_timelapse_home.svg',
        '#FF9800',
        0,
        22150,
        1,
        2,
        now - 1000
      ],
      [
        'duo_celestial_portal',
        '🌀 Stargate: Cosmic Ring to Wormhole',
        'Ancient silent stargate ring unlocks into an active hyperspace wormhole vortex on your home screen.',
        'SPACE',
        '/uploads/duo/duo_celestial_portal_lock.svg',
        '/uploads/duo/duo_celestial_portal_home.svg',
        '/uploads/duo/duo_celestial_portal_home.svg',
        '#7000FF',
        0,
        18900,
        1,
        3,
        now - 2000
      ],
      [
        'duo_super_saiyan',
        '🐉 Dragon Aura: Base to Super Saiyan God',
        'Focused warrior in deep meditation explodes into blinding golden ki and cyan plasma lightning.',
        'ANIME',
        '/uploads/duo/duo_super_saiyan_lock.svg',
        '/uploads/duo/duo_super_saiyan_home.svg',
        '/uploads/duo/duo_super_saiyan_home.svg',
        '#FFD600',
        1,
        31200,
        1,
        4,
        now - 3000
      ],
      [
        'duo_soulmate_connection',
        '💞 Soulmates: Moon Maiden & Sun King',
        'Crescent moon maiden on lock screen connects across the cosmos with radiant sun king on home screen.',
        'COUPLE',
        '/uploads/duo/duo_soulmate_connection_lock.svg',
        '/uploads/duo/duo_soulmate_connection_home.svg',
        '/uploads/duo/duo_soulmate_connection_home.svg',
        '#FF1744',
        0,
        19800,
        1,
        5,
        now - 4000
      ],
      [
        'duo_neon_wildlife',
        '🐅 Apex Predator: Shadow to Cyber Tiger',
        'Stealth shadow obsidian panther blends in dark lock screen, bursting into electric geometric cyber tiger.',
        'NATURE',
        '/uploads/duo/duo_neon_wildlife_lock.svg',
        '/uploads/duo/duo_neon_wildlife_home.svg',
        '/uploads/duo/duo_neon_wildlife_home.svg',
        '#00E5FF',
        0,
        14750,
        1,
        6,
        now - 5000
      ]
    ];

    for (const p of defaultDuoPairs) {
      await db.runAsync(`
        INSERT INTO duo_wallpapers (id, title, description, category, lockImageUrl, homeImageUrl, previewUrl, accentColor, isPremium, downloads, isActive, sortOrder, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, p);
    }
    console.log('👥 Initialized 6 curated default Duo / Double Wallpaper pairs in SQLite');
  }

  // 👆 INTERACTIVE TOUCH FLUID & RIPPLE PRESETS TABLE
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS touch_presets (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      effectType TEXT NOT NULL,
      description TEXT,
      primaryColor TEXT DEFAULT '#00E5FF',
      secondaryColor TEXT DEFAULT '#7000FF',
      accentColor TEXT DEFAULT '#FF007F',
      waveSpeed REAL DEFAULT 1.0,
      waveRadius INTEGER DEFAULT 70,
      particleCount INTEGER DEFAULT 80,
      viscosity REAL DEFAULT 0.8,
      hapticEnabled INTEGER DEFAULT 1,
      isPremium INTEGER DEFAULT 0,
      downloads INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      sortOrder INTEGER DEFAULT 1,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default curated Touch Presets if empty
  const touchCountRow = await db.getAsync('SELECT COUNT(*) as count FROM touch_presets');
  if (touchCountRow && touchCountRow.count === 0) {
    const now = Date.now();
    const defaultTouchPresets = [
      [
        'touch_water_ripple',
        '🌊 Crystal Water Ripples',
        'WATER_RIPPLE',
        'Realistic fluid water distortion echoing outwards with refraction highlights.',
        '#00E5FF',
        '#0088FF',
        '#E0F7FA',
        1.2,
        75,
        50,
        0.85,
        1,
        0,
        18450,
        1,
        1,
        now
      ],
      [
        'touch_neon_fluid',
        '🌌 Cyber Neon Fluid Dye',
        'NEON_FLUID',
        'Luminous cyan and magenta fluid dye that curls, swirls and mixes on finger drags.',
        '#00E5FF',
        '#7000FF',
        '#FF007F',
        1.0,
        90,
        130,
        0.70,
        1,
        0,
        24680,
        1,
        2,
        now - 1000
      ],
      [
        'touch_electric_sparks',
        '⚡ Hyper Electric Arcs',
        'ELECTRIC_SPARKS',
        'High-voltage electric plasma branches and lightning sparks leaping toward touches.',
        '#00E5FF',
        '#FF1744',
        '#FFFF00',
        1.8,
        80,
        95,
        0.95,
        1,
        1,
        21340,
        1,
        3,
        now - 2000
      ],
      [
        'touch_magic_stardust',
        '✨ Celestial Stardust Aura',
        'MAGIC_STARDUST',
        'Glimmering golden stardust and fairy dust trailing touch motions with soft shimmer.',
        '#FFD700',
        '#FF4081',
        '#FFFFFF',
        0.85,
        65,
        140,
        0.60,
        1,
        0,
        29800,
        1,
        4,
        now - 3000
      ],
      [
        'touch_solar_magma',
        '🔥 Solar Magma Flare',
        'MAGMA_BURST',
        'Explosive volcanic embers and intense magma heat bursts from touch points.',
        '#FF3D00',
        '#FFD700',
        '#FF9100',
        1.5,
        85,
        110,
        0.80,
        1,
        1,
        17290,
        1,
        5,
        now - 4000
      ],
      [
        'touch_gravity_vortex',
        '🌀 Quantum Gravity Vortex',
        'GRAVITY_VORTEX',
        'Spacetime gravitational warping pulling particles and luminous trails inward.',
        '#B388FF',
        '#7C4DFF',
        '#00E5FF',
        1.1,
        100,
        160,
        0.90,
        1,
        0,
        15420,
        1,
        6,
        now - 5000
      ]
    ];

    for (const tp of defaultTouchPresets) {
      await db.runAsync(`
        INSERT INTO touch_presets (id, title, effectType, description, primaryColor, secondaryColor, accentColor, waveSpeed, waveRadius, particleCount, viscosity, hapticEnabled, isPremium, downloads, isActive, sortOrder, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, tp);
    }
    console.log('👆 Initialized 6 curated default Touch Fluid & Ripple presets in SQLite');
  }

  // 🔓 IN-DISPLAY FINGERPRINT ANIMATIONS TABLE
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS fingerprint_presets (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      category TEXT DEFAULT 'CYBERPUNK',
      animationType TEXT NOT NULL,
      description TEXT,
      primaryColor TEXT DEFAULT '#00E5FF',
      secondaryColor TEXT DEFAULT '#7000FF',
      accentGlow TEXT DEFAULT '#00FFAA',
      animationSpeed REAL DEFAULT 1.2,
      scale REAL DEFAULT 1.0,
      verticalPosition INTEGER DEFAULT 78,
      hapticEnabled INTEGER DEFAULT 1,
      soundEnabled INTEGER DEFAULT 1,
      isPremium INTEGER DEFAULT 0,
      downloads INTEGER DEFAULT 0,
      isActive INTEGER DEFAULT 1,
      sortOrder INTEGER DEFAULT 1,
      createdAt INTEGER NOT NULL
    )
  `);

  // Seed default curated Fingerprint Presets if empty
  const fpCountRow = await db.getAsync('SELECT COUNT(*) as count FROM fingerprint_presets');
  if (fpCountRow && fpCountRow.count === 0) {
    const now = Date.now();
    const defaultFingerprintPresets = [
      [
        'fp_cyber_matrix',
        '⚡ Cyber Matrix Biometric Scanner',
        'CYBERPUNK',
        'CYBER_MATRIX',
        'Concentric rotating holographic HUD matrix rings with laser scan sweep and binary lock feedback.',
        '#00E5FF',
        '#7000FF',
        '#00FFAA',
        1.2,
        1.0,
        78,
        1,
        1,
        0,
        38400,
        1,
        1,
        now
      ],
      [
        'fp_supernova_flare',
        '🌌 Supernova Cosmic Flare',
        'COSMIC',
        'SUPERNOVA_FLARE',
        'Concentrated stellar core ignition exploding into radiant golden cosmic ray bursts upon touch.',
        '#FFD700',
        '#FF007F',
        '#FFE600',
        1.4,
        1.1,
        78,
        1,
        1,
        0,
        45200,
        1,
        2,
        now - 1000
      ],
      [
        'fp_neon_portal',
        '🌀 Neon Hyper Portal',
        'NEON',
        'NEON_PORTAL',
        'Spinning quantum wormhole tunnel with pulsing chromatic ring vortex and neon particle trails.',
        '#00FFCC',
        '#9D00FF',
        '#FF0099',
        1.3,
        1.0,
        78,
        1,
        1,
        0,
        29800,
        1,
        3,
        now - 2000
      ],
      [
        'fp_mystic_runes',
        '✨ Mystic Arcane Runes',
        'MAGIC',
        'MYSTIC_RUNES',
        'Ancient alchemical summoning circle rotating with glowing ancient glyphs and mystic stardust aura.',
        '#FF9100',
        '#FFD700',
        '#FF3D00',
        1.0,
        1.15,
        78,
        1,
        1,
        1,
        31500,
        1,
        4,
        now - 3000
      ],
      [
        'fp_quantum_circuit',
        '⚡ Quantum Circuit Overload',
        'CYBERPUNK',
        'CIRCUIT_OVERLOAD',
        'High-voltage neon PCB circuit traces branching outward across the screen with electric spark nodes.',
        '#00E676',
        '#00E5FF',
        '#76FF03',
        1.6,
        1.05,
        78,
        1,
        1,
        0,
        22400,
        1,
        5,
        now - 4000
      ],
      [
        'fp_solar_fusion',
        '🔥 Solar Fusion Flare',
        'ENERGY',
        'SOLAR_FLARE',
        'Intense nuclear fusion plasma arcs erupting outward with fiery solar coronal mass emissions.',
        '#FF3D00',
        '#FFD600',
        '#FF1744',
        1.5,
        1.1,
        78,
        1,
        1,
        1,
        36700,
        1,
        6,
        now - 5000
      ]
    ];

    for (const fp of defaultFingerprintPresets) {
      await db.runAsync(`
        INSERT INTO fingerprint_presets (id, title, category, animationType, description, primaryColor, secondaryColor, accentGlow, animationSpeed, scale, verticalPosition, hapticEnabled, soundEnabled, isPremium, downloads, isActive, sortOrder, createdAt)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `, fp);
    }
    console.log('🔓 Initialized 6 curated default In-Display Fingerprint presets in SQLite');
  }

  // Initialize default feature flags and remote configs if missing
  const defaultConfigs = [
    ['weather_enabled', 'false'],
    ['weather_effect', 'rain'],
    ['weather_intensity', 'medium'],
    ['weather_categories', 'all'],
    ['weather_sfx_enabled', 'true'],
    ['force_update_enabled', 'false'],
    ['min_supported_version', '1.0.0'],
    ['app_version', '1.0.0'],
    ['update_dialog_title', '🚀 New 3D Engine Update Available!'],
    ['update_dialog_message', 'We have updated ReWall with smoother 60FPS gyroscope 3D parallax tracking and fresh 4K festival drops. Please update to continue.'],
    ['update_url', 'https://play.google.com/store/apps/details?id=com.rewall.parallax3d'],
    ['update_btn_text', 'Update to Latest Version'],
    ['announcement_enabled', 'false'],
    ['announcement_text', '🎉 દિવાળી સ્પેશિયલ 3D વૉલપેપર્સ લાઈવ થયા છે! 4K માં ડાઉનલોડ કરો.'],
    ['announcement_type', 'festival'],
    ['announcement_theme', 'diwali_gold'],
    ['announcement_action_url', 'category:Space'],
    ['announcement_dismissable', 'true'],
    ['admob_banner_enabled', 'true'],
    ['admob_interstitial_enabled', 'true'],
    ['admob_rewarded_interval', '3'],
    ['admob_interstitial_interval', '3'],
    ['admob_app_id', 'ca-app-pub-3940256099942544~3347511713'],
    ['admob_banner_id', 'ca-app-pub-3940256099942544/6300978111'],
    ['admob_interstitial_id', 'ca-app-pub-3940256099942544/1033173712'],
    ['admob_rewarded_id', 'ca-app-pub-3940256099942544/5224354917'],
    ['admob_app_open_id', 'ca-app-pub-3940256099942544/9257395921'],
    ['admob_is_test_mode', 'true'],
    ['maintenance_mode', 'false'],
    ['daily_pick_id', 'w1'],
    ['feature_status_saver', 'true'],
    ['feature_ai_studio', 'true'],
    ['feature_music_visualizer', 'true'],
    ['feature_ringtones', 'true'],
    ['feature_custom_3d', 'true'],
    ['feature_weather_sync', 'true'],
    ['feature_double_wallpaper', 'true'],
    ['feature_wallpaper_changer', 'true'],
    ['feature_edge_lighting', 'true'],
    ['feature_dynamic_island', 'true'],
    ['feature_always_on_display', 'true'],
    ['feature_call_screen', 'true'],
    ['feature_touch_effects', 'true'],
    ['feature_fingerprint_animations', 'true'],
    ['privacy_policy_url', 'https://rewall-3d.web.app/privacy-policy'],
    ['terms_of_service_url', 'https://rewall-3d.web.app/terms'],
    ['instagram_url', 'https://instagram.com/rewall.3d'],
    ['telegram_url', 'https://t.me/rewall_wallpapers'],
    ['whatsapp_support_number', '+919876543210'],
    ['support_email', 'support@rewall.app'],
    ['rating_prompt_enabled', 'true'],
    ['rating_prompt_downloads_trigger', '3'],
    ['rating_prompt_title', 'Enjoying ReWall 3D Wallpapers?'],
    ['rating_prompt_message', 'You have downloaded awesome 4K 3D wallpapers! A quick 5-star rating on Google Play Store helps our team keep adding free wallpapers.'],
    ['rating_prompt_positive_btn', '⭐ Rate 5 Stars on Google Play'],
    ['rating_prompt_dismiss_btn', 'Maybe Later'],
    ['personalization_suite_items', JSON.stringify(defaultPersonalizationSuite)]
  ];
  for (const [k, v] of defaultConfigs) {
    await db.runAsync('INSERT OR IGNORE INTO app_config (key, value) VALUES (?, ?)', [k, v]);
  }

  // Secure Admin Authentication table
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS admins (
      id TEXT PRIMARY KEY,
      username TEXT UNIQUE NOT NULL,
      passwordHash TEXT NOT NULL,
      salt TEXT NOT NULL,
      role TEXT DEFAULT 'superadmin',
      createdAt INTEGER NOT NULL
    )
  `);

  // FCM Registered Devices table
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS fcm_tokens (
      token TEXT PRIMARY KEY,
      deviceModel TEXT,
      appVersion TEXT,
      updatedAt INTEGER NOT NULL
    )
  `);

  // Device Telemetry & Connected Devices table
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS device_telemetry (
      deviceId TEXT PRIMARY KEY,
      deviceModel TEXT NOT NULL,
      manufacturer TEXT NOT NULL,
      brand TEXT NOT NULL,
      androidVersion TEXT NOT NULL,
      apiLevel INTEGER NOT NULL,
      appVersion TEXT NOT NULL,
      screenResolution TEXT,
      screenDpi INTEGER DEFAULT 420,
      country TEXT DEFAULT 'India',
      countryCode TEXT DEFAULT 'IN',
      city TEXT DEFAULT 'Mumbai',
      latitude REAL DEFAULT 19.0760,
      longitude REAL DEFAULT 72.8777,
      ipAddress TEXT,
      fcmToken TEXT,
      batteryLevel INTEGER DEFAULT 85,
      firstSeenAt INTEGER NOT NULL,
      lastPingAt INTEGER NOT NULL
    )
  `);

  // Push Notifications Broadcast History table
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS notifications (
      id TEXT PRIMARY KEY,
      title TEXT NOT NULL,
      body TEXT NOT NULL,
      imageUrl TEXT,
      wallpaperId TEXT,
      sentAt INTEGER NOT NULL,
      recipientCount INTEGER DEFAULT 0,
      status TEXT DEFAULT 'sent'
    )
  `);

  // Daily Analytics & Growth Trend table
  await db.runAsync(`
    CREATE TABLE IF NOT EXISTS daily_analytics (
      date TEXT PRIMARY KEY,
      newLikes INTEGER DEFAULT 0,
      newDownloads INTEGER DEFAULT 0,
      activeUsers INTEGER DEFAULT 0
    )
  `);

  // Seed past 14 days of daily analytics if empty
  const existingAnalytics = await db.getAsync('SELECT date FROM daily_analytics LIMIT 1');
  if (!existingAnalytics) {
    const today = new Date();
    for (let i = 13; i >= 0; i--) {
      const d = new Date(today);
      d.setDate(d.getDate() - i);
      const dateStr = d.toISOString().split('T')[0];
      const baseGrowth = (14 - i) * 120;
      const randomLikes = Math.floor(2200 + baseGrowth + Math.sin(i) * 400 + Math.random() * 300);
      const randomDownloads = Math.floor(4500 + baseGrowth * 1.8 + Math.cos(i) * 700 + Math.random() * 500);
      const randomUsers = Math.floor(1200 + baseGrowth * 0.9 + Math.random() * 200);

      await db.runAsync(
        'INSERT OR IGNORE INTO daily_analytics (date, newLikes, newDownloads, activeUsers) VALUES (?, ?, ?, ?)',
        [dateStr, randomLikes, randomDownloads, randomUsers]
      );
    }
    console.log('📊 Initialized 14-day daily analytics baseline dataset');
  }

  // Seed default admin account if none exists (admin / admin123)
  const existingAdmin = await db.getAsync('SELECT id FROM admins LIMIT 1');
  if (!existingAdmin) {
    const salt = generateSalt();
    const hash = hashPassword('admin123', salt);
    await db.runAsync(
      'INSERT INTO admins (id, username, passwordHash, salt, role, createdAt) VALUES (?, ?, ?, ?, ?, ?)',
      ['admin_default', 'admin', hash, salt, 'superadmin', Date.now()]
    );
    console.log('🔒 Default Admin user initialized: [username: admin | password: admin123]');
  }
}

const crypto = require('crypto');

function generateSalt() {
  return crypto.randomBytes(16).toString('hex');
}

function hashPassword(password, salt) {
  return crypto.pbkdf2Sync(password, salt, 10000, 64, 'sha512').toString('hex');
}

function verifyPassword(password, hash, salt) {
  const checkHash = hashPassword(password, salt);
  return checkHash === hash;
}

// Replace and restore database file safely
async function replaceDatabaseFile(newDbBuffer) {
  // 1. Close current connection cleanly
  await new Promise((resolve, reject) => {
    currentDb.close((err) => {
      if (err) reject(err);
      else resolve();
    });
  });

  // 2. Backup current db to .bak fallback before overwriting
  const backupFile = path.join(dataDir, `rewall.sqlite.bak_${Date.now()}`);
  if (fs.existsSync(dbPath)) {
    try { fs.copyFileSync(dbPath, backupFile); } catch (_) {}
  }

  // 3. Write new SQLite database file
  fs.writeFileSync(dbPath, newDbBuffer);

  // 4. Re-open connection onto currentDb
  currentDb = new sqlite3.Database(dbPath);
  currentDb.run('PRAGMA foreign_keys = ON');

  // 5. Ensure schema tables & defaults exist
  await initSchema();
  console.log('✅ SQLite database successfully re-opened and re-initialized from backup');
  return true;
}

// Get database & uploads stats for backup dialog
async function getBackupStats() {
  const dbStat = fs.existsSync(dbPath) ? fs.statSync(dbPath) : { size: 0 };
  const uploadDir = path.join(__dirname, '..', 'public', 'uploads');
  
  let uploadsCount = 0;
  let uploadsSizeBytes = 0;
  if (fs.existsSync(uploadDir)) {
    const files = fs.readdirSync(uploadDir);
    uploadsCount = files.length;
    files.forEach(f => {
      try {
        uploadsSizeBytes += fs.statSync(path.join(uploadDir, f)).size;
      } catch (_) {}
    });
  }

  const wallsRow = await db.getAsync('SELECT COUNT(*) as count FROM wallpapers');
  const catsRow = await db.getAsync('SELECT COUNT(*) as count FROM categories');
  const ringRow = await db.getAsync('SELECT COUNT(*) as count FROM ringtones');

  return {
    databaseSizeBytes: dbStat.size,
    uploadsCount,
    uploadsSizeBytes,
    wallpapersCount: wallsRow ? wallsRow.count : 0,
    categoriesCount: catsRow ? catsRow.count : 0,
    ringtonesCount: ringRow ? ringRow.count : 0,
    dbPath
  };
}

module.exports = {
  db,
  dbPath,
  initSchema,
  generateSalt,
  hashPassword,
  verifyPassword,
  replaceDatabaseFile,
  getBackupStats,
  defaultPersonalizationSuite
};
