const { db, initSchema } = require('./database');

async function seed() {
  await initSchema();

  console.log('🌱 Checking existing database entries...');
  const count = await db.getAsync('SELECT COUNT(*) as cnt FROM wallpapers');
  if (count && count.cnt > 0 && !process.argv.includes('--force')) {
    console.log(`ℹ️ Database already has ${count.cnt} wallpapers. Skipping seed (use --force to reseed).`);
    process.exit(0);
  }

  console.log('🧹 Clearing old seed data...');
  await db.runAsync('DELETE FROM wallpaper_layers');
  await db.runAsync('DELETE FROM wallpapers');
  await db.runAsync('DELETE FROM categories');
  await db.runAsync('DELETE FROM ringtones');
  await db.runAsync('DELETE FROM app_config');

  console.log('📁 Seeding categories...');
  const categories = [
    { id: 'PARALLAX_3D', name: '3D Parallax', iconUrl: 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png' },
    { id: 'CYBERPUNK', name: 'Cyberpunk', iconUrl: 'https://cdn-icons-png.flaticon.com/512/3593/3593452.png' },
    { id: 'SPACE', name: 'Space & Cosmos', iconUrl: 'https://cdn-icons-png.flaticon.com/512/1046/1046857.png' },
    { id: 'AMOLED', name: 'AMOLED', iconUrl: 'https://cdn-icons-png.flaticon.com/512/3135/3135768.png' },
    { id: 'NATURE', name: 'Nature', iconUrl: 'https://cdn-icons-png.flaticon.com/512/2913/2913520.png' },
    { id: 'ANIME', name: 'Anime', iconUrl: 'https://cdn-icons-png.flaticon.com/512/2922/2922510.png' },
    { id: 'MINIMAL', name: 'Minimal', iconUrl: 'https://cdn-icons-png.flaticon.com/512/3135/3135789.png' }
  ];

  for (const cat of categories) {
    await db.runAsync(
      'INSERT INTO categories (id, name, iconUrl) VALUES (?, ?, ?)',
      [cat.id, cat.name, cat.iconUrl]
    );
  }

  console.log('🖼️ Seeding curated wallpapers & 3D layers...');
  const wallpapers = [
    {
      id: 'w1',
      title: 'Cyber Neon Horizon',
      category: 'CYBERPUNK',
      previewUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 34200,
      likes: 18900,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 2,
      layers: [
        { id: 'l1_bg', imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop', depth: 0.2, sortOrder: 0 },
        { id: 'l1_fg', imageUrl: 'https://images.unsplash.com/photo-1579546929518-9e396f3cc809?q=80&w=800&auto=format&fit=crop', depth: 0.7, sortOrder: 1 }
      ]
    },
    {
      id: 'w2',
      title: 'Deep Space Nebula',
      category: 'SPACE',
      previewUrl: 'https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 41500,
      likes: 24500,
      isPremium: 1,
      isUnlocked: 0,
      createdAt: Date.now() - 86400000 * 1,
      layers: [
        { id: 'l2_bg', imageUrl: 'https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=800&auto=format&fit=crop', depth: 0.15, sortOrder: 0 },
        { id: 'l2_fg', imageUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800&auto=format&fit=crop', depth: 0.6, sortOrder: 1 }
      ]
    },
    {
      id: 'w3',
      title: 'Mystic AMOLED Mountain',
      category: 'AMOLED',
      previewUrl: 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 52800,
      likes: 31400,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 5,
      layers: [
        { id: 'l3_bg', imageUrl: 'https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=800&auto=format&fit=crop', depth: 0.25, sortOrder: 0 },
        { id: 'l3_fg', imageUrl: 'https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=800&auto=format&fit=crop', depth: 0.8, sortOrder: 1 }
      ]
    },
    {
      id: 'w4',
      title: 'Tokyo Neon Rain',
      category: 'CYBERPUNK',
      previewUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=1600&auto=format&fit=crop',
      isParallax: 0,
      downloads: 19800,
      likes: 9400,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 3,
      layers: []
    },
    {
      id: 'w5',
      title: 'Emerald Forest Flow',
      category: 'NATURE',
      previewUrl: 'https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 28600,
      likes: 17300,
      isPremium: 1,
      isUnlocked: 0,
      createdAt: Date.now() - 86400000 * 6,
      layers: [
        { id: 'l5_bg', imageUrl: 'https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=800&auto=format&fit=crop', depth: 0.2, sortOrder: 0 },
        { id: 'l5_fg', imageUrl: 'https://images.unsplash.com/photo-1511497584788-87676104235f?q=80&w=800&auto=format&fit=crop', depth: 0.75, sortOrder: 1 }
      ]
    },
    {
      id: 'w6',
      title: 'Dark Horizon Eclipse',
      category: 'AMOLED',
      previewUrl: 'https://images.unsplash.com/photo-1532693322450-2cb5c511067d?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1532693322450-2cb5c511067d?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 47400,
      likes: 29800,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 4,
      layers: [
        { id: 'l6_bg', imageUrl: 'https://images.unsplash.com/photo-1532693322450-2cb5c511067d?q=80&w=800&auto=format&fit=crop', depth: 0.2, sortOrder: 0 },
        { id: 'l6_fg', imageUrl: 'https://images.unsplash.com/photo-1538370965046-79c0d6907d47?q=80&w=800&auto=format&fit=crop', depth: 0.7, sortOrder: 1 }
      ]
    },
    {
      id: 'w7',
      title: 'Golden Sunset Dunes',
      category: 'MINIMAL',
      previewUrl: 'https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?q=80&w=1600&auto=format&fit=crop',
      isParallax: 0,
      downloads: 15200,
      likes: 8700,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 7,
      layers: []
    },
    {
      id: 'w8',
      title: 'Anime Sky Sanctuary',
      category: 'ANIME',
      previewUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 69100,
      likes: 48400,
      isPremium: 1,
      isUnlocked: 0,
      createdAt: Date.now(),
      layers: [
        { id: 'l8_bg', imageUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=800&auto=format&fit=crop', depth: 0.15, sortOrder: 0 },
        { id: 'l8_fg', imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop', depth: 0.8, sortOrder: 1 }
      ]
    },
    {
      id: 'w9',
      title: 'Cybernetic Ronin 4K',
      category: 'CYBERPUNK',
      previewUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 38400,
      likes: 21000,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 8,
      layers: [
        { id: 'l9_bg', imageUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=800&auto=format&fit=crop', depth: 0.2, sortOrder: 0 },
        { id: 'l9_fg', imageUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop', depth: 0.7, sortOrder: 1 }
      ]
    },
    {
      id: 'w10',
      title: 'Quantum Singularity 3D',
      category: 'SPACE',
      previewUrl: 'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 59000,
      likes: 34900,
      isPremium: 1,
      isUnlocked: 0,
      createdAt: Date.now() - 86400000 * 9,
      layers: [
        { id: 'l10_bg', imageUrl: 'https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=800&auto=format&fit=crop', depth: 0.15, sortOrder: 0 },
        { id: 'l10_fg', imageUrl: 'https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=800&auto=format&fit=crop', depth: 0.75, sortOrder: 1 }
      ]
    },
    {
      id: 'w11',
      title: 'Pure Obsidian Geometry',
      category: 'AMOLED',
      previewUrl: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=1600&auto=format&fit=crop',
      isParallax: 0,
      downloads: 22100,
      likes: 12400,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 10,
      layers: []
    },
    {
      id: 'w12',
      title: 'Bioluminescent Valley',
      category: 'NATURE',
      previewUrl: 'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 43200,
      likes: 26500,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 11,
      layers: [
        { id: 'l12_bg', imageUrl: 'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=800&auto=format&fit=crop', depth: 0.2, sortOrder: 0 },
        { id: 'l12_fg', imageUrl: 'https://images.unsplash.com/photo-1511497584788-87676104235f?q=80&w=800&auto=format&fit=crop', depth: 0.65, sortOrder: 1 }
      ]
    },
    {
      id: 'w13',
      title: 'Sakura Cyber Shrine',
      category: 'ANIME',
      previewUrl: 'https://images.unsplash.com/photo-1528164344705-475426879c0d?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1528164344705-475426879c0d?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 74300,
      likes: 51200,
      isPremium: 1,
      isUnlocked: 0,
      createdAt: Date.now() - 86400000 * 12,
      layers: [
        { id: 'l13_bg', imageUrl: 'https://images.unsplash.com/photo-1528164344705-475426879c0d?q=80&w=800&auto=format&fit=crop', depth: 0.25, sortOrder: 0 },
        { id: 'l13_fg', imageUrl: 'https://images.unsplash.com/photo-1579546929518-9e396f3cc809?q=80&w=800&auto=format&fit=crop', depth: 0.8, sortOrder: 1 }
      ]
    },
    {
      id: 'w14',
      title: 'Minimal Aurora Arch',
      category: 'MINIMAL',
      previewUrl: 'https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?q=80&w=1600&auto=format&fit=crop',
      isParallax: 0,
      downloads: 18600,
      likes: 10100,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 13,
      layers: []
    },
    {
      id: 'w15',
      title: 'Supernova Core 4D',
      category: 'SPACE',
      previewUrl: 'https://images.unsplash.com/photo-1447433589675-4aaa569f3e05?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1447433589675-4aaa569f3e05?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 63800,
      likes: 39200,
      isPremium: 0,
      isUnlocked: 1,
      createdAt: Date.now() - 86400000 * 14,
      layers: [
        { id: 'l15_bg', imageUrl: 'https://images.unsplash.com/photo-1447433589675-4aaa569f3e05?q=80&w=800&auto=format&fit=crop', depth: 0.15, sortOrder: 0 },
        { id: 'l15_fg', imageUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800&auto=format&fit=crop', depth: 0.7, sortOrder: 1 }
      ]
    },
    {
      id: 'w16',
      title: 'Neon Highway Drive',
      category: 'CYBERPUNK',
      previewUrl: 'https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop',
      fullUrl: 'https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=1600&auto=format&fit=crop',
      isParallax: 1,
      downloads: 45900,
      likes: 27800,
      isPremium: 1,
      isUnlocked: 0,
      createdAt: Date.now() - 86400000 * 15,
      layers: [
        { id: 'l16_bg', imageUrl: 'https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop', depth: 0.2, sortOrder: 0 },
        { id: 'l16_fg', imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop', depth: 0.75, sortOrder: 1 }
      ]
    }
  ];

  for (const w of wallpapers) {
    await db.runAsync(
      `INSERT INTO wallpapers (id, title, category, previewUrl, fullUrl, isParallax, downloads, likes, isPremium, isUnlocked, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [w.id, w.title, w.category, w.previewUrl, w.fullUrl, w.isParallax, w.downloads, w.likes, w.isPremium, w.isUnlocked, w.createdAt]
    );

    if (w.layers && w.layers.length > 0) {
      for (const layer of w.layers) {
        await db.runAsync(
          `INSERT INTO wallpaper_layers (id, wallpaperId, imageUrl, depth, sortOrder)
           VALUES (?, ?, ?, ?, ?)`,
          [layer.id, w.id, layer.imageUrl, layer.depth, layer.sortOrder]
        );
      }
    }
  }

  console.log('🎵 Seeding ringtones...');
  const ringtones = [
    { id: 'r1', title: 'Cyberpunk Neon Drive', artist: 'ReWall Sounds', audioUrl: 'https://assets.mixkit.co/active_storage/sfx/2874/2874-preview.mp3', durationSeconds: 28, category: 'Cyberpunk', downloads: 48200 },
    { id: 'r2', title: 'Cosmic Starlight Ambient', artist: 'Astro Beats', audioUrl: 'https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3', durationSeconds: 34, category: 'Ambient', downloads: 62100 },
    { id: 'r3', title: 'Marimba Tropical Vibe', artist: 'Summer Vibes', audioUrl: 'https://assets.mixkit.co/active_storage/sfx/2873/2873-preview.mp3', durationSeconds: 24, category: 'Marimba', downloads: 39800 },
    { id: 'r4', title: 'Future Bass Drop', artist: 'EDM Pulse', audioUrl: 'https://assets.mixkit.co/active_storage/sfx/2871/2871-preview.mp3', durationSeconds: 30, category: 'EDM', downloads: 74500 },
    { id: 'r5', title: 'Anime Lo-Fi Sunset', artist: 'Chill Tokyo', audioUrl: 'https://assets.mixkit.co/active_storage/sfx/2872/2872-preview.mp3', durationSeconds: 42, category: 'Lo-Fi', downloads: 83900 },
    { id: 'r6', title: 'Acoustic Forest Whistle', artist: 'Nature Melody', audioUrl: 'https://assets.mixkit.co/active_storage/sfx/2870/2870-preview.mp3', durationSeconds: 20, category: 'Nature', downloads: 29300 }
  ];

  for (const r of ringtones) {
    await db.runAsync(
      `INSERT INTO ringtones (id, title, artist, audioUrl, durationSeconds, category, downloads)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [r.id, r.title, r.artist, r.audioUrl, r.durationSeconds, r.category, r.downloads]
    );
  }

  console.log('⚙️ Seeding remote app configs (AdMob, version)...');
  const configs = [
    { key: 'admob_banner_enabled', value: 'true' },
    { key: 'admob_interstitial_enabled', value: 'true' },
    { key: 'admob_rewarded_interval', value: '3' },
    { key: 'app_version', value: '1.0.0' },
    { key: 'update_url', value: 'https://play.google.com/store' },
    { key: 'maintenance_mode', value: 'false' },
    { key: 'daily_pick_id', value: 'w1' }
  ];

  for (const c of configs) {
    await db.runAsync(
      'INSERT INTO app_config (key, value) VALUES (?, ?)',
      [c.key, c.value]
    );
  }

  console.log('🎉 Seeding successfully completed!');
  process.exit(0);
}

seed().catch((err) => {
  console.error('❌ Seed error:', err);
  process.exit(1);
});
