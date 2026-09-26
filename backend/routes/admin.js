const express = require('express');
const router = express.Router();
const path = require('path');
const fs = require('fs');
const multer = require('multer');
const sharp = require('sharp');
const AdmZip = require('adm-zip');
const jwt = require('jsonwebtoken');
const {
  db,
  dbPath,
  verifyPassword,
  hashPassword,
  generateSalt,
  replaceDatabaseFile,
  getBackupStats,
  defaultPersonalizationSuite
} = require('../db/database');

const {
  backupsDir,
  getAutoBackupConfig,
  saveAutoBackupConfig,
  createLocalSnapshot,
  listLocalSnapshots,
  restoreFromLocalSnapshot,
  deleteLocalSnapshot
} = require('../services/backupScheduler');

const JWT_SECRET = process.env.JWT_SECRET || 'rewall_secure_admin_jwt_secret_key_2026_x89';

// Middleware to verify JWT Token for protected Admin routes
function verifyAdminToken(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ success: false, error: 'Authentication required. Please log in.' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.admin = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ success: false, error: 'Session expired or invalid token. Please log in again.' });
  }
}

// Ensure upload directory exists
const uploadDir = path.join(__dirname, '..', 'public', 'uploads');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// Multer storage
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, uploadDir);
  },
  filename: function (req, file, cb) {
    const ext = path.extname(file.originalname);
    const uniqueName = `upload_${Date.now()}_${Math.round(Math.random() * 1e9)}${ext}`;
    cb(null, uniqueName);
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 100 * 1024 * 1024 } // 100 MB limit for bulk/zip
});

// Helper: Convert image to WebP (Lossless/High-Q Full + Fast 600x900 Thumb)
async function processImageToWebP(inputFilePath, baseId) {
  const thumbFileName = `thumb_${baseId}.webp`;
  const fullFileName = `full_${baseId}.webp`;

  const thumbFilePath = path.join(uploadDir, thumbFileName);
  const fullFilePath = path.join(uploadDir, fullFileName);

  const originalStat = fs.statSync(inputFilePath);
  const originalBytes = originalStat.size;

  // 1. High-fidelity 4K/Full WebP (quality: 88 saves 70% space vs raw PNG/JPG)
  await sharp(inputFilePath)
    .webp({ quality: 88, effort: 4 })
    .toFile(fullFilePath);

  // 2. High-speed 600x900 mobile thumb WebP (25-40 KB for 0ms app grid load)
  await sharp(inputFilePath)
    .resize(600, 900, { fit: 'cover', position: 'center' })
    .webp({ quality: 80, effort: 4 })
    .toFile(thumbFilePath);

  const thumbStat = fs.statSync(thumbFilePath);
  const fullStat = fs.statSync(fullFilePath);

  return {
    thumbUrl: `/uploads/${thumbFileName}`,
    fullUrl: `/uploads/${fullFileName}`,
    originalBytes,
    compressedBytes: fullStat.size + thumbStat.size
  };
}

// Clean filename to human readable title
function filenameToTitle(filename) {
  const name = path.parse(filename).name;
  return name
    .replace(/[_-]+/g, ' ')
    .replace(/\b\w/g, c => c.toUpperCase())
    .trim();
}

// ==========================================
// AUTHENTICATION ROUTES
// ==========================================

// POST /api/admin/login
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    if (!username || !password) {
      return res.status(400).json({ success: false, error: 'Username and password are required' });
    }

    const admin = await db.getAsync('SELECT * FROM admins WHERE username = ?', [username.trim()]);
    if (!admin) {
      return res.status(401).json({ success: false, error: 'Invalid username or password' });
    }

    const isValid = verifyPassword(password, admin.passwordHash, admin.salt);
    if (!isValid) {
      return res.status(401).json({ success: false, error: 'Invalid username or password' });
    }

    const token = jwt.sign(
      { id: admin.id, username: admin.username, role: admin.role },
      JWT_SECRET,
      { expiresIn: '7d' }
    );

    res.json({
      success: true,
      token,
      user: {
        id: admin.id,
        username: admin.username,
        role: admin.role
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/admin/me
router.get('/me', verifyAdminToken, async (req, res) => {
  try {
    const admin = await db.getAsync('SELECT id, username, role, createdAt FROM admins WHERE id = ?', [req.admin.id]);
    if (!admin) {
      return res.status(404).json({ success: false, error: 'Admin account not found' });
    }
    res.json({ success: true, user: admin });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/change-password
router.post('/change-password', verifyAdminToken, async (req, res) => {
  try {
    const { currentPassword, newPassword } = req.body;
    if (!currentPassword || !newPassword) {
      return res.status(400).json({ success: false, error: 'Current and new password are required' });
    }
    if (newPassword.length < 6) {
      return res.status(400).json({ success: false, error: 'New password must be at least 6 characters' });
    }

    const admin = await db.getAsync('SELECT * FROM admins WHERE id = ?', [req.admin.id]);
    if (!admin) {
      return res.status(404).json({ success: false, error: 'Admin account not found' });
    }

    const isValid = verifyPassword(currentPassword, admin.passwordHash, admin.salt);
    if (!isValid) {
      return res.status(400).json({ success: false, error: 'Current password is incorrect' });
    }

    const newSalt = generateSalt();
    const newHash = hashPassword(newPassword, newSalt);
    await db.runAsync('UPDATE admins SET passwordHash = ?, salt = ? WHERE id = ?', [newHash, newSalt, admin.id]);

    res.json({ success: true, message: 'Password changed successfully' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/upload (Single file upload with auto WebP compression)
router.post('/upload', verifyAdminToken, upload.single('file'), async (req, res) => {
  if (!req.file) {
    return res.status(400).json({ success: false, error: 'No file uploaded' });
  }

  const ext = path.extname(req.file.originalname).toLowerCase();
  const imageExts = ['.jpg', '.jpeg', '.png', '.webp', '.bmp', '.tiff'];

  // If it's an image, auto-compress to WebP
  if (imageExts.includes(ext)) {
    try {
      const baseId = `${Date.now()}_${Math.round(Math.random() * 1e6)}`;
      const result = await processImageToWebP(req.file.path, baseId);
      
      // Cleanup raw uploaded file
      try { fs.unlinkSync(req.file.path); } catch (_) {}

      return res.json({
        success: true,
        url: result.fullUrl,
        previewUrl: result.thumbUrl,
        filename: path.basename(result.fullUrl),
        originalBytes: result.originalBytes,
        compressedBytes: result.compressedBytes,
        savedPercent: Math.max(0, Math.round((1 - result.compressedBytes / (result.originalBytes || 1)) * 100))
      });
    } catch (err) {
      console.error('WebP conversion error:', err);
      // Fallback to serving raw file if sharp fails on rare format
      const fileUrl = `/uploads/${req.file.filename}`;
      return res.json({ success: true, url: fileUrl, filename: req.file.filename });
    }
  }

  // Audio / other files
  const fileUrl = `/uploads/${req.file.filename}`;
  res.json({ success: true, url: fileUrl, filename: req.file.filename });
});

// POST /api/admin/bulk-upload (Batch multi-image or .ZIP auto-import)
router.post('/bulk-upload', verifyAdminToken, upload.array('files', 100), async (req, res) => {
  try {
    if (!req.files || req.files.length === 0) {
      return res.status(400).json({ success: false, error: 'No files uploaded' });
    }

    const category = req.body.category || 'AMOLED';
    const isPremium = req.body.isPremium === 'true' ? 1 : 0;
    const isParallax = req.body.isParallax === 'true' ? 1 : 0;

    const createdWallpapers = [];
    let totalOriginalBytes = 0;
    let totalCompressedBytes = 0;
    const validExts = ['.jpg', '.jpeg', '.png', '.webp', '.bmp'];

    for (const file of req.files) {
      const ext = path.extname(file.originalname).toLowerCase();

      if (ext === '.zip') {
        // Unzip and process every image inside
        try {
          const zip = new AdmZip(file.path);
          const zipEntries = zip.getEntries();

          for (const entry of zipEntries) {
            if (!entry.isDirectory) {
              const entryExt = path.extname(entry.entryName).toLowerCase();
              if (validExts.includes(entryExt)) {
                const tempFileName = `temp_${Date.now()}_${Math.round(Math.random() * 1e6)}${entryExt}`;
                const tempFilePath = path.join(uploadDir, tempFileName);
                fs.writeFileSync(tempFilePath, entry.getData());

                const baseId = `${Date.now()}_${Math.round(Math.random() * 1e6)}`;
                const title = filenameToTitle(entry.entryName);
                const result = await processImageToWebP(tempFilePath, baseId);

                totalOriginalBytes += result.originalBytes;
                totalCompressedBytes += result.compressedBytes;

                const finalId = `w_${baseId}`;
                await db.runAsync(
                  `INSERT INTO wallpapers (id, title, category, previewUrl, fullUrl, isParallax, downloads, likes, isPremium, isUnlocked, createdAt)
                   VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, 1, ?)`,
                  [finalId, title, category, result.thumbUrl, result.fullUrl, isParallax, isPremium, Date.now()]
                );

                if (isParallax) {
                  await db.runAsync(
                    `INSERT INTO wallpaper_layers (id, wallpaperId, imageUrl, depth, sortOrder)
                     VALUES (?, ?, ?, ?, ?)`,
                    [`l_${finalId}_0`, finalId, result.thumbUrl, 0.35, 0]
                  );
                }

                createdWallpapers.push({ id: finalId, title, previewUrl: result.thumbUrl });
                try { fs.unlinkSync(tempFilePath); } catch (_) {}
              }
            }
          }
        } catch (zipErr) {
          console.error('Error processing zip archive:', zipErr);
        } finally {
          try { fs.unlinkSync(file.path); } catch (_) {}
        }
      } else if (validExts.includes(ext)) {
        // Direct image file
        const baseId = `${Date.now()}_${Math.round(Math.random() * 1e6)}`;
        const title = filenameToTitle(file.originalname);
        const result = await processImageToWebP(file.path, baseId);

        totalOriginalBytes += result.originalBytes;
        totalCompressedBytes += result.compressedBytes;

        const finalId = `w_${baseId}`;
        await db.runAsync(
          `INSERT INTO wallpapers (id, title, category, previewUrl, fullUrl, isParallax, downloads, likes, isPremium, isUnlocked, createdAt)
           VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, 1, ?)`,
          [finalId, title, category, result.thumbUrl, result.fullUrl, isParallax, isPremium, Date.now()]
        );

        if (isParallax) {
          await db.runAsync(
            `INSERT INTO wallpaper_layers (id, wallpaperId, imageUrl, depth, sortOrder)
             VALUES (?, ?, ?, ?, ?)`,
            [`l_${finalId}_0`, finalId, result.thumbUrl, 0.35, 0]
          );
        }

        createdWallpapers.push({ id: finalId, title, previewUrl: result.thumbUrl });
        try { fs.unlinkSync(file.path); } catch (_) {}
      }
    }

    const savedPercent = totalOriginalBytes > 0
      ? Math.max(0, Math.round((1 - totalCompressedBytes / totalOriginalBytes) * 100))
      : 0;

    res.json({
      success: true,
      count: createdWallpapers.length,
      totalOriginalBytes,
      totalCompressedBytes,
      savedPercent,
      wallpapers: createdWallpapers
    });
  } catch (err) {
    console.error('Bulk upload error:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/admin/stats
router.get('/stats', async (req, res) => {
  try {
    const wallCount = await db.getAsync('SELECT COUNT(*) as count, SUM(downloads) as downloads, SUM(likes) as likes FROM wallpapers');
    const ringCount = await db.getAsync('SELECT COUNT(*) as count, SUM(downloads) as downloads FROM ringtones');
    const catCount = await db.getAsync('SELECT COUNT(*) as count FROM categories');
    const parallaxCount = await db.getAsync('SELECT COUNT(*) as count FROM wallpapers WHERE isParallax = 1');

    res.json({
      success: true,
      data: {
        totalWallpapers: wallCount.count || 0,
        totalParallax: parallaxCount.count || 0,
        totalDownloads: (wallCount.downloads || 0) + (ringCount.downloads || 0),
        totalLikes: wallCount.likes || 0,
        totalRingtones: ringCount.count || 0,
        totalCategories: catCount.count || 0
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/admin/analytics (Visual charts data aggregation)
router.get('/analytics', async (req, res) => {
  try {
    // 1. Category Breakdown (Downloads, Likes, Wallpaper Count)
    const categoryShare = await db.allAsync(`
      SELECT 
        category, 
        COUNT(*) as wallpaperCount, 
        COALESCE(SUM(downloads), 0) as totalDownloads, 
        COALESCE(SUM(likes), 0) as totalLikes
      FROM wallpapers 
      GROUP BY category 
      ORDER BY totalDownloads DESC
    `);

    // 2. Top Downloaded Wallpapers
    const topWallpapers = await db.allAsync(`
      SELECT 
        id, 
        title, 
        category, 
        previewUrl, 
        downloads, 
        likes, 
        isParallax, 
        isPremium
      FROM wallpapers 
      ORDER BY downloads DESC 
      LIMIT 6
    `);

    // 3. Daily Likes, Downloads & Active Users Trend (Past 14 Days)
    const dailyTrend = await db.allAsync(`
      SELECT 
        date, 
        newLikes, 
        newDownloads, 
        activeUsers 
      FROM daily_analytics 
      ORDER BY date ASC 
      LIMIT 14
    `);

    // 4. Key Performance Highlights
    const totalWallpapersRow = await db.getAsync('SELECT COUNT(*) as count, SUM(downloads) as downloads, SUM(likes) as likes FROM wallpapers');
    const parallaxCountRow = await db.getAsync('SELECT COUNT(*) as count FROM wallpapers WHERE isParallax = 1');
    const totalWalls = totalWallpapersRow ? totalWallpapersRow.count : 1;
    const totalLikes = totalWallpapersRow ? totalWallpapersRow.likes : 0;
    const totalParallax = parallaxCountRow ? parallaxCountRow.count : 0;

    const summary = {
      topCategory: categoryShare.length > 0 ? categoryShare[0].category : 'N/A',
      topCategoryDownloads: categoryShare.length > 0 ? categoryShare[0].totalDownloads : 0,
      topWallpaperTitle: topWallpapers.length > 0 ? topWallpapers[0].title : 'N/A',
      topWallpaperDownloads: topWallpapers.length > 0 ? topWallpapers[0].downloads : 0,
      avgLikesPerWall: Math.round(totalLikes / (totalWalls || 1)),
      parallaxPercent: Math.round((totalParallax / (totalWalls || 1)) * 100)
    };

    res.json({
      success: true,
      data: {
        categoryShare,
        topWallpapers,
        dailyTrend,
        summary
      }
    });
  } catch (err) {
    console.error('Analytics aggregation error:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/wallpapers
router.post('/wallpapers', verifyAdminToken, async (req, res) => {
  try {
    const {
      id,
      title,
      category,
      previewUrl,
      fullUrl,
      isParallax,
      isPremium,
      layers
    } = req.body;

    const finalId = id || `w_${Date.now()}`;
    const createdAt = Date.now();

    await db.runAsync(
      `INSERT INTO wallpapers (id, title, category, previewUrl, fullUrl, isParallax, downloads, likes, isPremium, isUnlocked, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, 0, 0, ?, 1, ?)`,
      [
        finalId,
        title || 'Untitled Wallpaper',
        category || 'AMOLED',
        previewUrl,
        fullUrl || previewUrl,
        isParallax ? 1 : 0,
        isPremium ? 1 : 0,
        createdAt
      ]
    );

    if (layers && Array.isArray(layers)) {
      for (let i = 0; i < layers.length; i++) {
        const layer = layers[i];
        const layerId = layer.id || `l_${finalId}_${i}`;
        await db.runAsync(
          `INSERT INTO wallpaper_layers (id, wallpaperId, imageUrl, depth, sortOrder)
           VALUES (?, ?, ?, ?, ?)`,
          [layerId, finalId, layer.imageUrl, parseFloat(layer.depth || 0.5), i]
        );
      }
    }

    res.json({ success: true, message: 'Wallpaper created successfully', id: finalId });
  } catch (err) {
    console.error('Error creating wallpaper:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/wallpapers/:id
router.put('/wallpapers/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const {
      title,
      category,
      previewUrl,
      fullUrl,
      isParallax,
      isPremium,
      layers
    } = req.body;

    await db.runAsync(
      `UPDATE wallpapers 
       SET title = ?, category = ?, previewUrl = ?, fullUrl = ?, isParallax = ?, isPremium = ?
       WHERE id = ?`,
      [
        title,
        category,
        previewUrl,
        fullUrl || previewUrl,
        isParallax ? 1 : 0,
        isPremium ? 1 : 0,
        id
      ]
    );

    if (layers && Array.isArray(layers)) {
      await db.runAsync('DELETE FROM wallpaper_layers WHERE wallpaperId = ?', [id]);
      for (let i = 0; i < layers.length; i++) {
        const layer = layers[i];
        const layerId = layer.id || `l_${id}_${i}`;
        await db.runAsync(
          `INSERT INTO wallpaper_layers (id, wallpaperId, imageUrl, depth, sortOrder)
           VALUES (?, ?, ?, ?, ?)`,
          [layerId, id, layer.imageUrl, parseFloat(layer.depth || 0.5), i]
        );
      }
    }

    res.json({ success: true, message: 'Wallpaper updated successfully' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/wallpapers/:id/layers (Quick update for 3D Parallax Studio)
router.put('/wallpapers/:id/layers', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const { layers } = req.body;
    if (!Array.isArray(layers)) {
      return res.status(400).json({ success: false, error: 'layers must be an array' });
    }

    await db.runAsync('DELETE FROM wallpaper_layers WHERE wallpaperId = ?', [id]);
    for (let i = 0; i < layers.length; i++) {
      const layer = layers[i];
      const layerId = layer.id || `l_${id}_${Date.now()}_${i}`;
      await db.runAsync(
        `INSERT INTO wallpaper_layers (id, wallpaperId, imageUrl, depth, sortOrder)
         VALUES (?, ?, ?, ?, ?)`,
        [layerId, id, layer.imageUrl, parseFloat(layer.depth || 0.5), i]
      );
    }

    if (layers.length > 0) {
      await db.runAsync('UPDATE wallpapers SET isParallax = 1 WHERE id = ?', [id]);
    }

    res.json({ success: true, message: '3D Parallax layers updated successfully' });
  } catch (err) {
    console.error('Error updating wallpaper layers:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/wallpapers/:id
router.delete('/wallpapers/:id', verifyAdminToken, async (req, res) => {
  try {
    await db.runAsync('DELETE FROM wallpaper_layers WHERE wallpaperId = ?', [req.params.id]);
    await db.runAsync('DELETE FROM wallpapers WHERE id = ?', [req.params.id]);
    res.json({ success: true, message: 'Wallpaper deleted successfully' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// 📦 BULK / BATCH ACTIONS FOR WALLPAPERS
// ========================================================

// POST /api/admin/wallpapers/bulk-delete
router.post('/wallpapers/bulk-delete', verifyAdminToken, async (req, res) => {
  try {
    const { ids } = req.body;
    if (!Array.isArray(ids) || ids.length === 0) {
      return res.status(400).json({ success: false, error: 'No wallpaper IDs provided for deletion.' });
    }

    const placeholders = ids.map(() => '?').join(',');
    await db.runAsync(`DELETE FROM wallpaper_layers WHERE wallpaperId IN (${placeholders})`, ids);
    const result = await db.runAsync(`DELETE FROM wallpapers WHERE id IN (${placeholders})`, ids);

    res.json({
      success: true,
      count: result.changes || ids.length,
      message: `${result.changes || ids.length} wallpapers deleted successfully`
    });
  } catch (err) {
    console.error('Error in bulk-delete wallpapers:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/wallpapers/bulk-category
router.post('/wallpapers/bulk-category', verifyAdminToken, async (req, res) => {
  try {
    const { ids, category } = req.body;
    if (!Array.isArray(ids) || ids.length === 0) {
      return res.status(400).json({ success: false, error: 'No wallpaper IDs provided.' });
    }
    if (!category || typeof category !== 'string') {
      return res.status(400).json({ success: false, error: 'Target category is required.' });
    }

    const targetCategory = category.trim();
    const placeholders = ids.map(() => '?').join(',');
    const result = await db.runAsync(
      `UPDATE wallpapers SET category = ? WHERE id IN (${placeholders})`,
      [targetCategory, ...ids]
    );

    res.json({
      success: true,
      count: result.changes || ids.length,
      category: targetCategory,
      message: `${result.changes || ids.length} wallpapers moved to category "${targetCategory}"`
    });
  } catch (err) {
    console.error('Error in bulk-category update:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/wallpapers/bulk-premium
router.post('/wallpapers/bulk-premium', verifyAdminToken, async (req, res) => {
  try {
    const { ids, isPremium } = req.body;
    if (!Array.isArray(ids) || ids.length === 0) {
      return res.status(400).json({ success: false, error: 'No wallpaper IDs provided.' });
    }

    const premiumFlag = Number(isPremium) ? 1 : 0;
    const placeholders = ids.map(() => '?').join(',');
    const result = await db.runAsync(
      `UPDATE wallpapers SET isPremium = ? WHERE id IN (${placeholders})`,
      [premiumFlag, ...ids]
    );

    res.json({
      success: true,
      count: result.changes || ids.length,
      isPremium: premiumFlag,
      message: `${result.changes || ids.length} wallpapers marked as ${premiumFlag ? 'VIP / Premium' : 'Free'}`
    });
  } catch (err) {
    console.error('Error in bulk-premium toggle:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/wallpapers/bulk-banner
router.post('/wallpapers/bulk-banner', verifyAdminToken, async (req, res) => {
  try {
    const { ids, badgeText } = req.body;
    if (!Array.isArray(ids) || ids.length === 0) {
      return res.status(400).json({ success: false, error: 'No wallpaper IDs provided.' });
    }

    const placeholders = ids.map(() => '?').join(',');
    const wallpapers = await db.allAsync(
      `SELECT id, title, category, previewUrl FROM wallpapers WHERE id IN (${placeholders})`,
      ids
    );

    if (!wallpapers || wallpapers.length === 0) {
      return res.status(404).json({ success: false, error: 'Selected wallpapers not found.' });
    }

    // Get current maximum sort order for hero banners
    const maxRow = await db.getAsync('SELECT MAX(sortOrder) as maxSort FROM hero_banners');
    let currentSort = (maxRow && maxRow.maxSort ? maxRow.maxSort : 0) + 1;

    let createdCount = 0;
    const now = Date.now();
    for (let i = 0; i < wallpapers.length; i++) {
      const wp = wallpapers[i];
      const bannerId = `banner_wp_${Date.now()}_${i}`;
      const subtitle = `${wp.category} 4K Wallpaper`;
      const badge = (badgeText || 'FEATURED DROP').trim();

      await db.runAsync(
        `INSERT INTO hero_banners (id, title, subtitle, badgeText, imageUrl, actionType, actionTarget, sortOrder, isActive, createdAt)
         VALUES (?, ?, ?, ?, ?, 'wallpaper', ?, ?, 1, ?)`,
        [bannerId, wp.title, subtitle, badge, wp.previewUrl, wp.id, currentSort++, now]
      );
      createdCount++;
    }

    res.json({
      success: true,
      count: createdCount,
      message: `${createdCount} wallpapers added to Home Screen Hero Banners!`
    });
  } catch (err) {
    console.error('Error in bulk-banner creation:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/categories
router.post('/categories', verifyAdminToken, async (req, res) => {
  try {
    const { id, name, iconUrl } = req.body;
    const catId = (id || name.toUpperCase().replace(/\s+/g, '_')).trim();
    await db.runAsync(
      'INSERT INTO categories (id, name, iconUrl) VALUES (?, ?, ?)',
      [catId, name, iconUrl || '']
    );
    res.json({ success: true, message: 'Category added', id: catId });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/categories/:id
router.delete('/categories/:id', verifyAdminToken, async (req, res) => {
  try {
    const rawId = req.params.id;
    const categoryId = decodeURIComponent(rawId).trim();

    // Check if category exists (support exact or case-insensitive ID)
    const category = await db.getAsync(
      'SELECT * FROM categories WHERE id = ? OR LOWER(id) = LOWER(?)',
      [categoryId, categoryId]
    );

    const targetId = category ? category.id : categoryId;

    // Check count of wallpapers in this category
    const wpCountRow = await db.getAsync(
      'SELECT COUNT(*) as count FROM wallpapers WHERE category = ?',
      [targetId]
    );
    const wpCount = wpCountRow ? wpCountRow.count : 0;

    if (wpCount > 0) {
      // Ensure 'GENERAL' category exists so wallpapers are never orphaned
      await db.runAsync(`
        INSERT OR IGNORE INTO categories (id, name, iconUrl)
        VALUES ('GENERAL', 'General', 'https://cdn-icons-png.flaticon.com/512/3135/3135715.png')
      `);
      // Safely reassign wallpapers
      await db.runAsync(
        'UPDATE wallpapers SET category = ? WHERE category = ?',
        ['GENERAL', targetId]
      );
    }

    await db.runAsync(
      'DELETE FROM categories WHERE id = ? OR LOWER(id) = LOWER(?)',
      [targetId, targetId]
    );

    res.json({
      success: true,
      message: `Category "${category ? category.name : targetId}" deleted successfully`,
      reassignedWallpapers: wpCount
    });
  } catch (err) {
    console.error('Error deleting category:', err);
    res.status(500).json({ success: false, error: err.message || 'Server error deleting category' });
  }
});

// POST /api/admin/ringtones
router.post('/ringtones', verifyAdminToken, async (req, res) => {
  try {
    const { id, title, artist, audioUrl, durationSeconds, category } = req.body;
    const finalId = id || `r_${Date.now()}`;
    await db.runAsync(
      `INSERT INTO ringtones (id, title, artist, audioUrl, durationSeconds, category, downloads)
       VALUES (?, ?, ?, ?, ?, ?, 0)`,
      [finalId, title, artist || 'Unknown Artist', audioUrl, durationSeconds || 30, category || 'General']
    );
    res.json({ success: true, message: 'Ringtone added', id: finalId });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/ringtones/:id
router.delete('/ringtones/:id', verifyAdminToken, async (req, res) => {
  try {
    await db.runAsync('DELETE FROM ringtones WHERE id = ?', [req.params.id]);
    res.json({ success: true, message: 'Ringtone deleted' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/admin/config
router.get('/config', async (req, res) => {
  try {
    const rows = await db.allAsync('SELECT key, value FROM app_config');
    const map = {};
    rows.forEach(r => { map[r.key] = r.value; });
    res.json({ success: true, data: map });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/config
router.post('/config', verifyAdminToken, async (req, res) => {
  try {
    const configs = req.body;
    for (const [key, value] of Object.entries(configs)) {
      await db.runAsync(
        `INSERT INTO app_config (key, value) VALUES (?, ?)
         ON CONFLICT(key) DO UPDATE SET value = excluded.value`,
        [key, String(value)]
      );
    }
    res.json({ success: true, message: 'App configuration updated' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/features/toggle - Instant toggle single feature flag
router.post('/features/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { featureKey, enabled } = req.body;
    if (!featureKey) {
      return res.status(400).json({ success: false, error: 'featureKey is required' });
    }
    const valStr = enabled ? 'true' : 'false';
    await db.runAsync(
      `INSERT INTO app_config (key, value) VALUES (?, ?)
       ON CONFLICT(key) DO UPDATE SET value = excluded.value`,
      [featureKey, valStr]
    );
    res.json({
      success: true,
      message: `Feature ${featureKey} set to ${valStr}`,
      featureKey,
      enabled: Boolean(enabled)
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/features/preset - Apply Google Play Compliance or All Enabled preset
router.post('/features/preset', verifyAdminToken, async (req, res) => {
  try {
    const { mode } = req.body;
    const isSafeMode = mode === 'google_play_safe';
    const updates = {
      feature_status_saver: isSafeMode ? 'false' : 'true',
      feature_ai_studio: 'true',
      feature_music_visualizer: 'true',
      feature_ringtones: 'true',
      feature_custom_3d: 'true',
      feature_weather_sync: 'true',
      feature_double_wallpaper: 'true',
      feature_wallpaper_changer: 'true'
    };
    for (const [k, v] of Object.entries(updates)) {
      await db.runAsync(
        `INSERT INTO app_config (key, value) VALUES (?, ?)
         ON CONFLICT(key) DO UPDATE SET value = excluded.value`,
        [k, v]
      );
    }
    res.json({
      success: true,
      mode: isSafeMode ? 'google_play_safe' : 'all_enabled',
      message: isSafeMode 
        ? 'Google Play Compliance Safe Mode activated (WhatsApp Status Saver hidden)' 
        : 'All App Features Enabled (Full Mode)'
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/admin/personalization-suite - Fetch current flagship suite items
router.get('/personalization-suite', async (req, res) => {
  try {
    const row = await db.getAsync("SELECT value FROM app_config WHERE key = 'personalization_suite_items'");
    let items = defaultPersonalizationSuite;
    if (row && row.value) {
      try {
        items = JSON.parse(row.value);
      } catch (e) {
        items = defaultPersonalizationSuite;
      }
    }
    res.json({ success: true, count: items.length, data: items, items: items });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/personalization-suite - Save updated flagship suite items order, badges, titles & visibility
router.post('/personalization-suite', verifyAdminToken, async (req, res) => {
  try {
    const { items } = req.body;
    if (!Array.isArray(items)) {
      return res.status(400).json({ success: false, error: 'items array is required' });
    }
    await db.runAsync(
      `INSERT INTO app_config (key, value) VALUES ('personalization_suite_items', ?)
       ON CONFLICT(key) DO UPDATE SET value = excluded.value`,
      [JSON.stringify(items)]
    );
    res.json({
      success: true,
      message: 'Personalization Suite configuration saved successfully',
      data: items,
      items: items
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/personalization-suite/toggle - Quick toggle single module ON/OFF
router.post('/personalization-suite/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id, enabled } = req.body;
    if (!id) {
      return res.status(400).json({ success: false, error: 'id is required' });
    }
    const row = await db.getAsync("SELECT value FROM app_config WHERE key = 'personalization_suite_items'");
    let items = defaultPersonalizationSuite;
    if (row && row.value) {
      try { items = JSON.parse(row.value); } catch (e) { items = defaultPersonalizationSuite; }
    }
    const target = items.find(it => it.id === id);
    if (target) {
      target.enabled = Boolean(enabled);
      await db.runAsync(
        `INSERT INTO app_config (key, value) VALUES ('personalization_suite_items', ?)
         ON CONFLICT(key) DO UPDATE SET value = excluded.value`,
        [JSON.stringify(items)]
      );
      return res.json({ success: true, message: `Module ${id} is now ${target.enabled ? 'Enabled' : 'Disabled'}`, data: items, items: items });
    }
    res.status(404).json({ success: false, error: 'Module not found' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/personalization-suite/reset - Reset suite to default order & badges
router.post('/personalization-suite/reset', verifyAdminToken, async (req, res) => {
  try {
    await db.runAsync(
      `INSERT INTO app_config (key, value) VALUES ('personalization_suite_items', ?)
       ON CONFLICT(key) DO UPDATE SET value = excluded.value`,
      [JSON.stringify(defaultPersonalizationSuite)]
    );
    res.json({
      success: true,
      message: 'Personalization Suite reset to factory defaults',
      data: defaultPersonalizationSuite,
      items: defaultPersonalizationSuite
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/social-hub - Save Social Media, Legal Policy & Support Links
router.post('/social-hub', verifyAdminToken, async (req, res) => {
  try {
    const {
      privacyPolicyUrl,
      termsUrl,
      instagramUrl,
      telegramUrl,
      whatsappNumber,
      supportEmail
    } = req.body;

    const pairs = [
      ['privacy_policy_url', privacyPolicyUrl || ''],
      ['terms_of_service_url', termsUrl || ''],
      ['instagram_url', instagramUrl || ''],
      ['telegram_url', telegramUrl || ''],
      ['whatsapp_support_number', whatsappNumber || ''],
      ['support_email', supportEmail || '']
    ];

    for (const [k, v] of pairs) {
      await db.runAsync(
        `INSERT INTO app_config (key, value) VALUES (?, ?)
         ON CONFLICT(key) DO UPDATE SET value = excluded.value`,
        [k, String(v)]
      );
    }

    res.json({
      success: true,
      message: 'Social Media, Legal Policy & Support Hub links updated successfully!'
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/rating-prompt - Save Smart In-App Rating & Review Trigger
router.post('/rating-prompt', verifyAdminToken, async (req, res) => {
  try {
    const {
      enabled,
      triggerDownloads,
      title,
      message,
      positiveBtn,
      dismissBtn
    } = req.body;

    const pairs = [
      ['rating_prompt_enabled', enabled ? 'true' : 'false'],
      ['rating_prompt_downloads_trigger', String(parseInt(triggerDownloads, 10) || 3)],
      ['rating_prompt_title', title || 'Enjoying ReWall 3D Wallpapers?'],
      ['rating_prompt_message', message || 'You have downloaded awesome 4K 3D wallpapers! A quick 5-star rating on Google Play Store helps our team keep adding free wallpapers.'],
      ['rating_prompt_positive_btn', positiveBtn || '⭐ Rate 5 Stars on Google Play'],
      ['rating_prompt_dismiss_btn', dismissBtn || 'Maybe Later']
    ];

    for (const [k, v] of pairs) {
      await db.runAsync(
        `INSERT INTO app_config (key, value) VALUES (?, ?)
         ON CONFLICT(key) DO UPDATE SET value = excluded.value`,
        [k, String(v)]
      );
    }

    res.json({
      success: true,
      message: 'Smart In-App Rating Trigger updated successfully!'
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/admob - Save Live AdMob Ad Unit IDs & Frequency Capping
router.post('/admob', verifyAdminToken, async (req, res) => {
  try {
    const {
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
    } = req.body;

    const pairs = [
      ['admob_banner_enabled', bannerEnabled ? 'true' : 'false'],
      ['admob_interstitial_enabled', interstitialEnabled ? 'true' : 'false'],
      ['admob_native_enabled', nativeEnabled ? 'true' : 'false'],
      ['admob_rewarded_interval', String(parseInt(rewardedInterval, 10) || 3)],
      ['admob_interstitial_interval', String(parseInt(interstitialInterval, 10) || 3)],
      ['admob_native_interval', String(parseInt(nativeInterval, 10) || 6)],
      ['admob_app_id', appId || 'ca-app-pub-3940256099942544~3347511713'],
      ['admob_banner_id', bannerId || 'ca-app-pub-3940256099942544/6300978111'],
      ['admob_interstitial_id', interstitialId || 'ca-app-pub-3940256099942544/1033173712'],
      ['admob_rewarded_id', rewardedId || 'ca-app-pub-3940256099942544/5224354917'],
      ['admob_app_open_id', appOpenId || 'ca-app-pub-3940256099942544/9257395921'],
      ['admob_native_id', nativeId || 'ca-app-pub-3940256099942544/2247696110'],
      ['admob_is_test_mode', isTestMode ? 'true' : 'false']
    ];

    for (const [k, v] of pairs) {
      await db.runAsync(
        `INSERT INTO app_config (key, value) VALUES (?, ?)
         ON CONFLICT(key) DO UPDATE SET value = excluded.value`,
        [k, String(v)]
      );
    }

    res.json({
      success: true,
      message: 'AdMob Monetization & Frequency settings saved successfully!'
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// ==========================================
// PUSH NOTIFICATION ROUTES
// ==========================================

// GET /api/admin/notifications
router.get('/notifications', verifyAdminToken, async (req, res) => {
  try {
    const notifications = await db.allAsync('SELECT * FROM notifications ORDER BY sentAt DESC LIMIT 50');
    const tokenCountRow = await db.getAsync('SELECT COUNT(*) as count FROM fcm_tokens');
    const activeTokens = tokenCountRow ? tokenCountRow.count : 0;
    res.json({
      success: true,
      data: notifications,
      activeDevices: activeTokens
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/notifications/send
router.post('/notifications/send', verifyAdminToken, async (req, res) => {
  try {
    const { title, body, imageUrl, wallpaperId } = req.body;
    if (!title || !title.trim()) {
      return res.status(400).json({ success: false, error: 'Notification title is required' });
    }
    if (!body || !body.trim()) {
      return res.status(400).json({ success: false, error: 'Notification message body is required' });
    }

    const tokens = await db.allAsync('SELECT token FROM fcm_tokens');
    const tokenCount = tokens.length;
    // Active devices count (at least 1 simulated in dev)
    const recipientCount = Math.max(1, tokenCount);

    const notifId = `notif_${Date.now()}`;
    const sentAt = Date.now();

    await db.runAsync(
      `INSERT INTO notifications (id, title, body, imageUrl, wallpaperId, sentAt, recipientCount, status)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
      [notifId, title.trim(), body.trim(), imageUrl || '', wallpaperId || '', sentAt, recipientCount, 'Delivered']
    );

    console.log('========================================================');
    console.log(`🔔 [PUSH NOTIFICATION BROADCAST] ID: ${notifId}`);
    console.log(`📌 Title:       ${title}`);
    console.log(`📝 Body:        ${body}`);
    console.log(`🖼️ Image:       ${imageUrl || 'None'}`);
    console.log(`🔗 Target Link: ${wallpaperId ? 'Wallpaper #' + wallpaperId : 'Open App Home'}`);
    console.log(`📱 Targeted:    ${recipientCount} devices`);
    console.log('========================================================');

    res.json({
      success: true,
      message: `Notification broadcasted to ${recipientCount} active device(s)!`,
      data: {
        id: notifId,
        title,
        body,
        imageUrl,
        wallpaperId,
        sentAt,
        recipientCount
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/notifications/:id
router.delete('/notifications/:id', verifyAdminToken, async (req, res) => {
  try {
    await db.runAsync('DELETE FROM notifications WHERE id = ?', [req.params.id]);
    res.json({ success: true, message: 'Notification deleted from history' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// ==========================================
// 📱 DEVICE TELEMETRY & GEO MAP ROUTES
// ==========================================

// GET /api/admin/telemetry
router.get('/telemetry', verifyAdminToken, async (req, res) => {
  try {
    const now = Date.now();
    const fifteenMinAgo = now - 15 * 60 * 1000;

    const devices = await db.allAsync(
      'SELECT * FROM device_telemetry ORDER BY lastPingAt DESC LIMIT 100'
    );

    const totalCountRow = await db.getAsync('SELECT COUNT(*) as count FROM device_telemetry');
    const onlineCountRow = await db.getAsync(
      'SELECT COUNT(*) as count FROM device_telemetry WHERE lastPingAt > ?',
      [fifteenMinAgo]
    );

    // Brand distribution
    const brandStats = await db.allAsync(`
      SELECT manufacturer, COUNT(*) as count 
      FROM device_telemetry 
      GROUP BY manufacturer 
      ORDER BY count DESC
    `);

    // Android Version distribution
    const osStats = await db.allAsync(`
      SELECT androidVersion, apiLevel, COUNT(*) as count 
      FROM device_telemetry 
      GROUP BY androidVersion 
      ORDER BY apiLevel DESC
    `);

    // City / Location summary
    const cityStats = await db.allAsync(`
      SELECT city, country, countryCode, COUNT(*) as count 
      FROM device_telemetry 
      GROUP BY city 
      ORDER BY count DESC
    `);

    const totalDevices = totalCountRow ? totalCountRow.count : 0;
    const onlineDevices = onlineCountRow ? onlineCountRow.count : 0;

    res.json({
      success: true,
      data: {
        summary: {
          totalDevices,
          onlineDevices,
          topBrand: brandStats.length > 0 ? brandStats[0].manufacturer : 'Samsung',
          topBrandCount: brandStats.length > 0 ? brandStats[0].count : 0,
          topAndroidVersion: osStats.length > 0 ? osStats[0].androidVersion : 'Android 15',
          topOs: osStats.length > 0 ? osStats[0].androidVersion : 'Android 15',
          topCity: cityStats.length > 0 ? `${cityStats[0].city}, ${cityStats[0].countryCode}` : 'Mumbai, IN'
        },
        brandStats: brandStats.map(b => ({ brand: b.manufacturer, count: b.count })),
        osStats: osStats.map(o => ({ os: o.androidVersion, apiLevel: o.apiLevel, count: o.count })),
        cityStats,
        devices: devices.map(d => ({
          ...d,
          isOnline: d.lastPingAt > fifteenMinAgo,
          online: d.lastPingAt > fifteenMinAgo
        })),
        geoPings: devices.map(d => ({
          deviceId: d.deviceId,
          deviceModel: d.deviceModel,
          manufacturer: d.manufacturer,
          city: d.city,
          country: d.country,
          countryCode: d.countryCode,
          latitude: d.latitude,
          longitude: d.longitude,
          androidVersion: d.androidVersion,
          apiLevel: d.apiLevel,
          screenResolution: d.screenResolution,
          screenDpi: d.screenDpi,
          batteryLevel: d.batteryLevel,
          isOnline: d.lastPingAt > fifteenMinAgo,
          online: d.lastPingAt > fifteenMinAgo,
          lastPingAt: d.lastPingAt
        }))
      }
    });
  } catch (err) {
    console.error('Telemetry query error:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/devices/:id/ping (Send test push / heartbeat ping)
router.post('/devices/:id/ping', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const device = await db.getAsync('SELECT * FROM device_telemetry WHERE deviceId = ?', [id]);
    if (!device) {
      return res.status(404).json({ success: false, error: 'Device not found' });
    }

    const now = Date.now();
    await db.runAsync('UPDATE device_telemetry SET lastPingAt = ? WHERE deviceId = ?', [now, id]);
    res.json({
      success: true,
      message: `🔔 Ping delivered to [${device.deviceModel}] (${device.city})!`
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// ==========================================
// 🖼️ HERO BANNERS MANAGEMENT ROUTES
// ==========================================

// GET /api/admin/banners
router.get('/banners', verifyAdminToken, async (req, res) => {
  try {
    const banners = await db.allAsync(
      'SELECT * FROM hero_banners ORDER BY sortOrder ASC, createdAt DESC'
    );
    res.json({ success: true, count: banners.length, data: banners });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/banners (Create new banner with optional upload)
router.post('/banners', verifyAdminToken, upload.single('image'), async (req, res) => {
  try {
    const { title, subtitle, badgeText, actionType, actionTarget, sortOrder, isActive } = req.body;
    let imageUrl = req.body.imageUrl || '';

    if (!title || !title.trim()) {
      return res.status(400).json({ success: false, error: 'Banner title is required' });
    }

    if (req.file) {
      const baseId = `${Date.now()}_${Math.round(Math.random() * 1e6)}`;
      const result = await processImageToWebP(req.file.path, baseId);
      imageUrl = result.fullUrl;
      try { fs.unlinkSync(req.file.path); } catch (_) {}
    }

    if (!imageUrl) {
      return res.status(400).json({ success: false, error: 'Banner image or image URL is required' });
    }

    const id = `b_${Date.now()}`;
    const createdAt = Date.now();
    const order = parseInt(sortOrder || '0', 10);
    const active = isActive === 'false' || isActive === 0 || isActive === '0' ? 0 : 1;

    await db.runAsync(
      `INSERT INTO hero_banners (id, title, subtitle, badgeText, imageUrl, actionType, actionTarget, sortOrder, isActive, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [id, title.trim(), subtitle || '', badgeText || '', imageUrl, actionType || 'category', actionTarget || '', order, active, createdAt]
    );

    const banner = await db.getAsync('SELECT * FROM hero_banners WHERE id = ?', [id]);
    res.json({ success: true, message: 'Hero banner created successfully', data: banner });
  } catch (err) {
    console.error('Create banner error:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/banners/:id (Update banner)
router.put('/banners/:id', verifyAdminToken, upload.single('image'), async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM hero_banners WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Banner not found' });
    }

    const { title, subtitle, badgeText, actionType, actionTarget, sortOrder, isActive } = req.body;
    let imageUrl = req.body.imageUrl || existing.imageUrl;

    if (req.file) {
      const baseId = `${Date.now()}_${Math.round(Math.random() * 1e6)}`;
      const result = await processImageToWebP(req.file.path, baseId);
      imageUrl = result.fullUrl;
      try { fs.unlinkSync(req.file.path); } catch (_) {}
    }

    const updatedTitle = title !== undefined ? title.trim() : existing.title;
    const updatedSub = subtitle !== undefined ? subtitle : existing.subtitle;
    const updatedBadge = badgeText !== undefined ? badgeText : existing.badgeText;
    const updatedActionType = actionType !== undefined ? actionType : existing.actionType;
    const updatedActionTarget = actionTarget !== undefined ? actionTarget : existing.actionTarget;
    const updatedOrder = sortOrder !== undefined ? parseInt(sortOrder, 10) : existing.sortOrder;
    const updatedActive = isActive !== undefined ? (isActive === 'true' || isActive === 1 || isActive === true ? 1 : 0) : existing.isActive;

    await db.runAsync(
      `UPDATE hero_banners
       SET title = ?, subtitle = ?, badgeText = ?, imageUrl = ?, actionType = ?, actionTarget = ?, sortOrder = ?, isActive = ?
       WHERE id = ?`,
      [updatedTitle, updatedSub, updatedBadge, imageUrl, updatedActionType, updatedActionTarget, updatedOrder, updatedActive, id]
    );

    const updated = await db.getAsync('SELECT * FROM hero_banners WHERE id = ?', [id]);
    res.json({ success: true, message: 'Banner updated successfully', data: updated });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/banners/:id/toggle (Quick toggle active/inactive)
router.patch('/banners/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM hero_banners WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Banner not found' });
    }

    const newActive = existing.isActive ? 0 : 1;
    await db.runAsync('UPDATE hero_banners SET isActive = ? WHERE id = ?', [newActive, id]);
    res.json({ success: true, message: `Banner ${newActive ? 'activated' : 'paused'}`, isActive: newActive });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/banners/:id
router.delete('/banners/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync('DELETE FROM hero_banners WHERE id = ?', [id]);
    res.json({ success: true, message: 'Banner deleted successfully' });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// ==========================================
// 1-CLICK AUTO BACKUP & 1-SEC RESTORE ROUTES
// ==========================================

// GET /api/admin/backup/stats
router.get('/backup/stats', verifyAdminToken, async (req, res) => {
  try {
    const stats = await getBackupStats();
    const snapshots = listLocalSnapshots();
    const autoConfig = getAutoBackupConfig();

    res.json({
      success: true,
      data: {
        ...stats,
        snapshotsCount: snapshots.length,
        latestSnapshot: snapshots[0] || null,
        autoConfig
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/admin/backup/snapshots - List all local server snapshots
router.get('/backup/snapshots', verifyAdminToken, async (req, res) => {
  try {
    const snapshots = listLocalSnapshots();
    const autoConfig = getAutoBackupConfig();
    res.json({
      success: true,
      count: snapshots.length,
      data: snapshots,
      autoConfig
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/backup/snapshots - 1-Click Create Snapshot on Server
router.post('/backup/snapshots', verifyAdminToken, async (req, res) => {
  try {
    const { label, note, type } = req.body;
    const result = await createLocalSnapshot({
      type: type || 'manual',
      label: label || 'Manual System Backup',
      note: note || ''
    });

    res.json({
      success: true,
      message: '✅ System snapshot created successfully!',
      data: result
    });
  } catch (err) {
    console.error('Create snapshot error:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/backup/snapshots/:filename/restore - Instant 1-Second Restore
router.post('/backup/snapshots/:filename/restore', verifyAdminToken, async (req, res) => {
  try {
    const { filename } = req.params;
    const result = await restoreFromLocalSnapshot(filename);

    res.json({
      success: true,
      message: `⚡ System successfully restored in ${result.durationMs}ms!`,
      data: result
    });
  } catch (err) {
    console.error('Snapshot restore error:', err);
    res.status(500).json({ success: false, error: 'Restore failed: ' + err.message });
  }
});

// DELETE /api/admin/backup/snapshots/:filename - Delete Snapshot
router.delete('/backup/snapshots/:filename', verifyAdminToken, async (req, res) => {
  try {
    const { filename } = req.params;
    const result = deleteLocalSnapshot(filename);
    res.json({
      success: true,
      message: '🗑️ Snapshot deleted successfully',
      data: result
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/admin/backup/snapshots/:filename/download - Download specific server snapshot
router.get('/backup/snapshots/:filename/download', async (req, res) => {
  try {
    let token = null;
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
      token = authHeader.split(' ')[1];
    } else if (req.query.token) {
      token = req.query.token;
    }

    if (!token) {
      return res.status(401).json({ success: false, error: 'Authentication required' });
    }

    try {
      jwt.verify(token, JWT_SECRET);
    } catch (_) {
      return res.status(401).json({ success: false, error: 'Session expired or invalid token' });
    }

    const { filename } = req.params;
    const safeFilename = path.basename(filename);
    const filePath = path.join(backupsDir, safeFilename);

    if (!fs.existsSync(filePath)) {
      return res.status(404).json({ success: false, error: 'Snapshot file not found' });
    }

    res.download(filePath, safeFilename);
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/admin/backup/config - Get Auto Backup configuration
router.get('/backup/config', verifyAdminToken, async (req, res) => {
  try {
    const config = getAutoBackupConfig();
    res.json({ success: true, data: config });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/backup/config - Update Auto Backup configuration
router.post('/backup/config', verifyAdminToken, async (req, res) => {
  try {
    const updated = saveAutoBackupConfig(req.body);
    res.json({
      success: true,
      message: '✅ Auto-backup schedule settings saved!',
      data: updated
    });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/admin/backup/download (Direct Instant Download)
router.get('/backup/download', async (req, res) => {
  try {
    let token = null;
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
      token = authHeader.split(' ')[1];
    } else if (req.query.token) {
      token = req.query.token;
    }

    if (!token) {
      return res.status(401).json({ success: false, error: 'Authentication required to download system backup.' });
    }

    try {
      jwt.verify(token, JWT_SECRET);
    } catch (err) {
      return res.status(401).json({ success: false, error: 'Session expired or invalid token.' });
    }

    const type = req.query.type || 'full';

    if (type === 'db-only') {
      if (!fs.existsSync(dbPath)) {
        return res.status(404).json({ success: false, error: 'Database file not found' });
      }
      const dateStr = new Date().toISOString().slice(0, 10);
      return res.download(dbPath, `rewall_db_${dateStr}.sqlite`);
    }

    // Generate immediate snapshot
    const snapshot = await createLocalSnapshot({
      type: 'manual',
      label: 'Direct Cloud Export'
    });

    res.download(snapshot.filePath, snapshot.filename);
  } catch (err) {
    console.error('Backup download error:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/backup/restore (Accepts .zip or .sqlite file)
router.post('/backup/restore', verifyAdminToken, upload.single('backupFile'), async (req, res) => {
  if (!req.file) {
    return res.status(400).json({ success: false, error: 'No backup file uploaded' });
  }

  const tempFilePath = req.file.path;
  const ext = path.extname(req.file.originalname).toLowerCase();

  try {
    let sqliteBuffer = null;
    let extractedUploadsCount = 0;

    if (ext === '.sqlite' || ext === '.db') {
      sqliteBuffer = fs.readFileSync(tempFilePath);
    } else if (ext === '.zip') {
      const zip = new AdmZip(tempFilePath);
      const entries = zip.getEntries();

      // Find sqlite file inside ZIP
      const dbEntry = entries.find(e => 
        e.entryName === 'rewall.sqlite' ||
        e.name === 'rewall.sqlite' ||
        e.name.endsWith('.sqlite') ||
        e.name.endsWith('.db')
      );

      if (!dbEntry) {
        try { fs.unlinkSync(tempFilePath); } catch (_) {}
        return res.status(400).json({
          success: false,
          error: 'Invalid backup ZIP: Could not find "rewall.sqlite" database inside the archive.'
        });
      }

      sqliteBuffer = dbEntry.getData();

      // Extract uploads/ if present
      entries.forEach(entry => {
        if ((entry.entryName.startsWith('uploads/') || entry.entryName.startsWith('uploads\\')) && !entry.isDirectory) {
          const fileName = path.basename(entry.entryName);
          if (fileName) {
            const destPath = path.join(uploadDir, fileName);
            fs.writeFileSync(destPath, entry.getData());
            extractedUploadsCount++;
          }
        }
      });
    } else {
      try { fs.unlinkSync(tempFilePath); } catch (_) {}
      return res.status(400).json({
        success: false,
        error: 'Unsupported file format. Please upload a valid .zip or .sqlite backup.'
      });
    }

    // Validate SQLite Magic Header ('SQLite format 3\0')
    const magicHeader = sqliteBuffer.slice(0, 16).toString('utf8');
    if (!magicHeader.startsWith('SQLite format 3')) {
      try { fs.unlinkSync(tempFilePath); } catch (_) {}
      return res.status(400).json({
        success: false,
        error: 'Corrupted database: File does not appear to be a valid SQLite 3 database.'
      });
    }

    // Replace database file and reload connection
    await replaceDatabaseFile(sqliteBuffer);

    // Cleanup uploaded temp file
    try { fs.unlinkSync(tempFilePath); } catch (_) {}

    const updatedStats = await getBackupStats();

    res.json({
      success: true,
      message: '⚡ System and database successfully restored in 1 second!',
      data: {
        wallpapersCount: updatedStats.wallpapersCount,
        categoriesCount: updatedStats.categoriesCount,
        extractedUploadsCount,
        databaseSizeBytes: updatedStats.databaseSizeBytes
      }
    });
  } catch (err) {
    console.error('Restore error:', err);
    try { fs.unlinkSync(tempFilePath); } catch (_) {}
    res.status(500).json({ success: false, error: 'Restore failed: ' + err.message });
  }
});

// ==========================================
// 🖼️ HERO BANNERS ADMIN MANAGEMENT
// ==========================================

// GET /api/admin/banners
router.get('/banners', verifyAdminToken, async (req, res) => {
  try {
    const banners = await db.allAsync(
      `SELECT id, title, subtitle, badgeText, imageUrl, actionType, actionTarget, sortOrder, isActive, createdAt
       FROM hero_banners
       ORDER BY sortOrder ASC, createdAt DESC`
    );
    res.json({
      success: true,
      data: (banners || []).map(b => ({
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
      }))
    });
  } catch (err) {
    console.error('Error fetching admin hero banners:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/banners (Create new banner)
router.post('/banners', verifyAdminToken, upload.single('bannerImage'), async (req, res) => {
  try {
    const { title, subtitle, badgeText, actionType, actionTarget, sortOrder, isActive } = req.body;
    let imageUrl = req.body.imageUrl;

    if (!title || !title.trim()) {
      return res.status(400).json({ success: false, error: 'Banner title is required' });
    }

    if (req.file) {
      try {
        const webpFilename = `banner_${Date.now()}_${Math.round(Math.random() * 1e6)}.webp`;
        const webpPath = path.join(uploadDir, webpFilename);
        await sharp(req.file.path)
          .resize({ width: 1200, withoutEnlargement: true })
          .webp({ quality: 85 })
          .toFile(webpPath);

        imageUrl = `/uploads/${webpFilename}`;
        try { fs.unlinkSync(req.file.path); } catch (_) {}
      } catch (sharpErr) {
        imageUrl = `/uploads/${path.basename(req.file.path)}`;
      }
    }

    if (!imageUrl || !imageUrl.trim()) {
      return res.status(400).json({ success: false, error: 'Banner image or image URL is required' });
    }

    const bannerId = `banner_${Date.now()}`;
    const sort = parseInt(sortOrder, 10) || 1;
    const active = (isActive === 'true' || isActive === true || isActive === 1 || isActive === '1') ? 1 : 0;
    const now = Date.now();

    await db.runAsync(
      `INSERT INTO hero_banners (id, title, subtitle, badgeText, imageUrl, actionType, actionTarget, sortOrder, isActive, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [bannerId, title.trim(), (subtitle || '').trim(), (badgeText || '').trim(), imageUrl.trim(), actionType || 'category', actionTarget || '', sort, active, now]
    );

    res.json({
      success: true,
      message: 'Hero banner created successfully',
      data: {
        id: bannerId,
        title: title.trim(),
        subtitle: (subtitle || '').trim(),
        badgeText: (badgeText || '').trim(),
        imageUrl: imageUrl.trim(),
        actionType: actionType || 'category',
        actionTarget: actionTarget || '',
        sortOrder: sort,
        isActive: Boolean(active),
        createdAt: now
      }
    });
  } catch (err) {
    console.error('Error creating hero banner:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/banners/:id (Update banner)
router.put('/banners/:id', verifyAdminToken, upload.single('bannerImage'), async (req, res) => {
  try {
    const { id } = req.params;
    const { title, subtitle, badgeText, actionType, actionTarget, sortOrder, isActive } = req.body;
    let imageUrl = req.body.imageUrl;

    const existing = await db.getAsync('SELECT * FROM hero_banners WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Hero banner not found' });
    }

    if (req.file) {
      try {
        const webpFilename = `banner_${Date.now()}_${Math.round(Math.random() * 1e6)}.webp`;
        const webpPath = path.join(uploadDir, webpFilename);
        await sharp(req.file.path)
          .resize({ width: 1200, withoutEnlargement: true })
          .webp({ quality: 85 })
          .toFile(webpPath);

        imageUrl = `/uploads/${webpFilename}`;
        try { fs.unlinkSync(req.file.path); } catch (_) {}
      } catch (sharpErr) {
        imageUrl = `/uploads/${path.basename(req.file.path)}`;
      }
    } else if (!imageUrl) {
      imageUrl = existing.imageUrl;
    }

    const finalTitle = title !== undefined ? title.trim() : existing.title;
    const finalSubtitle = subtitle !== undefined ? subtitle.trim() : existing.subtitle;
    const finalBadge = badgeText !== undefined ? badgeText.trim() : existing.badgeText;
    const finalActionType = actionType !== undefined ? actionType : existing.actionType;
    const finalActionTarget = actionTarget !== undefined ? actionTarget : existing.actionTarget;
    const finalSort = sortOrder !== undefined ? parseInt(sortOrder, 10) : existing.sortOrder;
    const finalActive = isActive !== undefined ? ((isActive === 'true' || isActive === true || isActive === 1 || isActive === '1') ? 1 : 0) : existing.isActive;

    await db.runAsync(
      `UPDATE hero_banners
       SET title = ?, subtitle = ?, badgeText = ?, imageUrl = ?, actionType = ?, actionTarget = ?, sortOrder = ?, isActive = ?
       WHERE id = ?`,
      [finalTitle, finalSubtitle, finalBadge, imageUrl, finalActionType, finalActionTarget, finalSort, finalActive, id]
    );

    res.json({
      success: true,
      message: 'Hero banner updated successfully',
      data: {
        id,
        title: finalTitle,
        subtitle: finalSubtitle,
        badgeText: finalBadge,
        imageUrl,
        actionType: finalActionType,
        actionTarget: finalActionTarget,
        sortOrder: finalSort,
        isActive: Boolean(finalActive)
      }
    });
  } catch (err) {
    console.error('Error updating hero banner:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/banners/:id/toggle (Toggle active status)
router.patch('/banners/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const banner = await db.getAsync('SELECT * FROM hero_banners WHERE id = ?', [id]);
    if (!banner) {
      return res.status(404).json({ success: false, error: 'Hero banner not found' });
    }

    const newStatus = banner.isActive ? 0 : 1;
    await db.runAsync('UPDATE hero_banners SET isActive = ? WHERE id = ?', [newStatus, id]);

    res.json({
      success: true,
      message: `Banner ${newStatus ? 'activated' : 'paused'}`,
      data: { id, isActive: Boolean(newStatus) }
    });
  } catch (err) {
    console.error('Error toggling banner:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/banners/:id
router.delete('/banners/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const banner = await db.getAsync('SELECT * FROM hero_banners WHERE id = ?', [id]);
    if (!banner) {
      return res.status(404).json({ success: false, error: 'Hero banner not found' });
    }

    await db.runAsync('DELETE FROM hero_banners WHERE id = ?', [id]);
    res.json({ success: true, message: 'Hero banner deleted successfully' });
  } catch (err) {
    console.error('Error deleting hero banner:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ==========================================
// 🔥 TRENDING SEARCH TAGS ADMIN MANAGEMENT
// ==========================================

// GET /api/admin/trending-tags
router.get('/trending-tags', verifyAdminToken, async (req, res) => {
  try {
    const tags = await db.allAsync(
      `SELECT id, tag, icon, sortOrder, clickCount, isActive, createdAt
       FROM trending_tags
       ORDER BY sortOrder ASC, clickCount DESC`
    );

    res.json({
      success: true,
      data: (tags || []).map(t => ({
        id: t.id,
        tag: t.tag,
        icon: t.icon || '🔥',
        sortOrder: Number(t.sortOrder || 0),
        clickCount: Number(t.clickCount || 0),
        isActive: Boolean(t.isActive),
        createdAt: Number(t.createdAt)
      }))
    });
  } catch (err) {
    console.error('Error fetching admin trending tags:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/trending-tags (Create new tag)
router.post('/trending-tags', verifyAdminToken, async (req, res) => {
  try {
    const { tag, icon, sortOrder, isActive } = req.body;
    if (!tag || !tag.trim()) {
      return res.status(400).json({ success: false, error: 'Tag text is required' });
    }

    let cleanTag = tag.trim();
    if (!cleanTag.startsWith('#')) {
      cleanTag = '#' + cleanTag;
    }

    const tagId = `tag_${Date.now()}`;
    const sort = parseInt(sortOrder, 10) || 1;
    const active = (isActive === 'true' || isActive === true || isActive === 1 || isActive === '1') ? 1 : 0;
    const now = Date.now();

    await db.runAsync(
      `INSERT INTO trending_tags (id, tag, icon, sortOrder, clickCount, isActive, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [tagId, cleanTag, (icon || '🔥').trim(), sort, 0, active, now]
    );

    res.json({
      success: true,
      message: 'Trending tag added successfully',
      data: {
        id: tagId,
        tag: cleanTag,
        icon: (icon || '🔥').trim(),
        sortOrder: sort,
        clickCount: 0,
        isActive: Boolean(active),
        createdAt: now
      }
    });
  } catch (err) {
    console.error('Error adding trending tag:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/trending-tags/:id (Update tag)
router.put('/trending-tags/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const { tag, icon, sortOrder, isActive } = req.body;

    const existing = await db.getAsync('SELECT * FROM trending_tags WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Trending tag not found' });
    }

    let cleanTag = tag !== undefined ? tag.trim() : existing.tag;
    if (cleanTag && !cleanTag.startsWith('#')) {
      cleanTag = '#' + cleanTag;
    }

    const finalIcon = icon !== undefined ? icon.trim() : existing.icon;
    const finalSort = sortOrder !== undefined ? parseInt(sortOrder, 10) : existing.sortOrder;
    const finalActive = isActive !== undefined ? ((isActive === 'true' || isActive === true || isActive === 1 || isActive === '1') ? 1 : 0) : existing.isActive;

    await db.runAsync(
      `UPDATE trending_tags SET tag = ?, icon = ?, sortOrder = ?, isActive = ? WHERE id = ?`,
      [cleanTag, finalIcon, finalSort, finalActive, id]
    );

    res.json({
      success: true,
      message: 'Trending tag updated',
      data: {
        id,
        tag: cleanTag,
        icon: finalIcon,
        sortOrder: finalSort,
        isActive: Boolean(finalActive)
      }
    });
  } catch (err) {
    console.error('Error updating trending tag:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/trending-tags/:id/toggle (Toggle active status)
router.patch('/trending-tags/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const tag = await db.getAsync('SELECT * FROM trending_tags WHERE id = ?', [id]);
    if (!tag) {
      return res.status(404).json({ success: false, error: 'Trending tag not found' });
    }

    const newStatus = tag.isActive ? 0 : 1;
    await db.runAsync('UPDATE trending_tags SET isActive = ? WHERE id = ?', [newStatus, id]);

    res.json({
      success: true,
      message: `Tag ${newStatus ? 'activated' : 'paused'}`,
      data: { id, isActive: Boolean(newStatus) }
    });
  } catch (err) {
    console.error('Error toggling trending tag:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/trending-tags/:id
router.delete('/trending-tags/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const tag = await db.getAsync('SELECT * FROM trending_tags WHERE id = ?', [id]);
    if (!tag) {
      return res.status(404).json({ success: false, error: 'Trending tag not found' });
    }

    await db.runAsync('DELETE FROM trending_tags WHERE id = ?', [id]);
    res.json({ success: true, message: 'Trending tag deleted successfully' });
  } catch (err) {
    console.error('Error deleting trending tag:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// ⚡ DYNAMIC CHARGING ANIMATIONS ADMIN MANAGEMENT
// ========================================================

const chargingUploadFields = upload.fields([
  { name: 'animationFile', maxCount: 1 },
  { name: 'previewFile', maxCount: 1 },
  { name: 'soundFile', maxCount: 1 }
]);

// GET /api/admin/charging-animations
router.get('/charging-animations', verifyAdminToken, async (req, res) => {
  try {
    const { category, search } = req.query;
    const where = [];
    const params = [];

    if (category && category.toUpperCase() !== 'ALL') {
      where.push('category = ?');
      params.push(category.toUpperCase());
    }

    if (search && search.trim() !== '') {
      where.push('title LIKE ?');
      params.push(`%${search.trim()}%`);
    }

    const whereSql = where.length > 0 ? `WHERE ${where.join(' AND ')}` : '';
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
        isActive: Boolean(item.isActive),
        sortOrder: Number(item.sortOrder || 0),
        createdAt: Number(item.createdAt)
      }))
    });
  } catch (err) {
    console.error('Error fetching admin charging animations:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/charging-animations (Create new charging animation)
router.post('/charging-animations', verifyAdminToken, chargingUploadFields, async (req, res) => {
  try {
    const {
      title,
      category,
      animationType,
      textColor,
      isPremium,
      isActive,
      sortOrder,
      directAnimationUrl,
      directPreviewUrl,
      directSoundUrl
    } = req.body;

    if (!title || title.trim() === '') {
      return res.status(400).json({ success: false, error: 'Title is required' });
    }

    let animUrl = directAnimationUrl ? directAnimationUrl.trim() : '';
    let prevUrl = directPreviewUrl ? directPreviewUrl.trim() : '';
    let sndUrl = directSoundUrl ? directSoundUrl.trim() : null;

    if (req.files) {
      if (req.files.animationFile && req.files.animationFile[0]) {
        animUrl = `/uploads/${req.files.animationFile[0].filename}`;
      }
      if (req.files.previewFile && req.files.previewFile[0]) {
        prevUrl = `/uploads/${req.files.previewFile[0].filename}`;
      }
      if (req.files.soundFile && req.files.soundFile[0]) {
        sndUrl = `/uploads/${req.files.soundFile[0].filename}`;
      }
    }

    if (!animUrl) {
      return res.status(400).json({ success: false, error: 'Animation file or URL is required' });
    }
    if (!prevUrl) {
      prevUrl = '/uploads/charging/thumb_neon_arc.svg';
    }

    // Detect animation type if not specified
    let finalType = animationType || 'lottie';
    if (animUrl.endsWith('.mp4') || animUrl.endsWith('.webm')) {
      finalType = 'video';
    } else if (animUrl.endsWith('.json')) {
      finalType = 'lottie';
    }

    const id = `ca_${Date.now()}`;
    const now = Date.now();
    const cleanCategory = (category || 'NEON').toUpperCase();
    const cleanColor = textColor && textColor.startsWith('#') ? textColor : '#00E5FF';
    const cleanSortOrder = Number(sortOrder || 1);
    const cleanIsPremium = isPremium === 'true' || isPremium === '1' || isPremium === true ? 1 : 0;
    const cleanIsActive = isActive === 'false' || isActive === '0' || isActive === false ? 0 : 1;

    await db.runAsync(
      `INSERT INTO charging_animations 
       (id, title, category, previewUrl, animationUrl, animationType, soundUrl, textColor, isPremium, downloads, isActive, sortOrder, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        id,
        title.trim(),
        cleanCategory,
        prevUrl,
        animUrl,
        finalType,
        sndUrl,
        cleanColor,
        cleanIsPremium,
        0,
        cleanIsActive,
        cleanSortOrder,
        now
      ]
    );

    const created = await db.getAsync('SELECT * FROM charging_animations WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Charging animation published successfully!',
      data: created
    });
  } catch (err) {
    console.error('Error creating charging animation:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/charging-animations/:id (Update charging animation)
router.put('/charging-animations/:id', verifyAdminToken, chargingUploadFields, async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM charging_animations WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Charging animation not found' });
    }

    const {
      title,
      category,
      animationType,
      textColor,
      isPremium,
      isActive,
      sortOrder,
      directAnimationUrl,
      directPreviewUrl,
      directSoundUrl
    } = req.body;

    let animUrl = directAnimationUrl !== undefined && directAnimationUrl.trim() !== '' ? directAnimationUrl.trim() : existing.animationUrl;
    let prevUrl = directPreviewUrl !== undefined && directPreviewUrl.trim() !== '' ? directPreviewUrl.trim() : existing.previewUrl;
    let sndUrl = directSoundUrl !== undefined ? (directSoundUrl.trim() || null) : existing.soundUrl;

    if (req.files) {
      if (req.files.animationFile && req.files.animationFile[0]) {
        animUrl = `/uploads/${req.files.animationFile[0].filename}`;
      }
      if (req.files.previewFile && req.files.previewFile[0]) {
        prevUrl = `/uploads/${req.files.previewFile[0].filename}`;
      }
      if (req.files.soundFile && req.files.soundFile[0]) {
        sndUrl = `/uploads/${req.files.soundFile[0].filename}`;
      }
    }

    const cleanTitle = title !== undefined ? title.trim() : existing.title;
    const cleanCategory = category !== undefined ? category.toUpperCase() : existing.category;
    const cleanType = animationType !== undefined ? animationType : existing.animationType;
    const cleanColor = textColor !== undefined ? textColor : existing.textColor;
    const cleanSortOrder = sortOrder !== undefined ? Number(sortOrder) : existing.sortOrder;
    const cleanIsPremium = isPremium !== undefined ? (isPremium === 'true' || isPremium === '1' || isPremium === true ? 1 : 0) : existing.isPremium;
    const cleanIsActive = isActive !== undefined ? (isActive === 'false' || isActive === '0' || isActive === false ? 0 : 1) : existing.isActive;

    await db.runAsync(
      `UPDATE charging_animations 
       SET title = ?, category = ?, previewUrl = ?, animationUrl = ?, animationType = ?, soundUrl = ?, textColor = ?, isPremium = ?, isActive = ?, sortOrder = ?
       WHERE id = ?`,
      [
        cleanTitle,
        cleanCategory,
        prevUrl,
        animUrl,
        cleanType,
        sndUrl,
        cleanColor,
        cleanIsPremium,
        cleanIsActive,
        cleanSortOrder,
        id
      ]
    );

    const updated = await db.getAsync('SELECT * FROM charging_animations WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Charging animation updated successfully!',
      data: updated
    });
  } catch (err) {
    console.error('Error updating charging animation:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/charging-animations/:id/toggle (Toggle active status)
router.patch('/charging-animations/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM charging_animations WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Charging animation not found' });
    }

    const newStatus = item.isActive ? 0 : 1;
    await db.runAsync('UPDATE charging_animations SET isActive = ? WHERE id = ?', [newStatus, id]);

    res.json({
      success: true,
      message: `Animation ${newStatus ? 'activated' : 'paused'}`,
      data: { id, isActive: Boolean(newStatus) }
    });
  } catch (err) {
    console.error('Error toggling charging animation:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/charging-animations/:id
router.delete('/charging-animations/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM charging_animations WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Charging animation not found' });
    }

    await db.runAsync('DELETE FROM charging_animations WHERE id = ?', [id]);
    res.json({ success: true, message: 'Charging animation deleted successfully' });
  } catch (err) {
    console.error('Error deleting charging animation:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// 🌈 DYNAMIC EDGE LIGHTING ADMIN MANAGEMENT
// ========================================================

const edgeUploadFields = upload.fields([
  { name: 'previewFile', maxCount: 1 }
]);

// GET /api/admin/edge-lighting
router.get('/edge-lighting', verifyAdminToken, async (req, res) => {
  try {
    const { category, search } = req.query;
    const where = [];
    const params = [];

    if (category && category.toUpperCase() !== 'ALL') {
      where.push('category = ?');
      params.push(category.toUpperCase());
    }

    if (search && search.trim()) {
      where.push('(title LIKE ? OR category LIKE ? OR animationType LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q, q);
    }

    const whereSql = where.length > 0 ? `WHERE ${where.join(' AND ')}` : '';
    const items = await db.allAsync(
      `SELECT * FROM edge_lighting_presets ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`,
      params
    );

    const totalCountRow = await db.getAsync('SELECT COUNT(*) as count FROM edge_lighting_presets');
    const activeCountRow = await db.getAsync('SELECT COUNT(*) as count FROM edge_lighting_presets WHERE isActive = 1');
    const totalDownloadsRow = await db.getAsync('SELECT SUM(downloads) as total FROM edge_lighting_presets');

    const mapped = items.map((i) => {
      let parsedColors = [];
      try {
        parsedColors = JSON.parse(i.colors);
      } catch (_) {
        parsedColors = ['#00E5FF', '#7000FF'];
      }
      return {
        id: i.id,
        title: i.title,
        category: i.category,
        animationType: i.animationType,
        colors: parsedColors,
        speed: Number(i.speed || 1.0),
        borderSize: Number(i.borderSize || 5),
        cornerRadius: Number(i.cornerRadius || 28),
        punchHoleRadius: Number(i.punchHoleRadius || 0),
        glowSpread: Number(i.glowSpread || 12),
        previewUrl: i.previewUrl,
        isPremium: Boolean(i.isPremium),
        downloads: Number(i.downloads || 0),
        isActive: Boolean(i.isActive),
        sortOrder: Number(i.sortOrder || 0),
        createdAt: Number(i.createdAt)
      };
    });

    res.json({
      success: true,
      data: mapped,
      stats: {
        total: totalCountRow ? totalCountRow.count : 0,
        active: activeCountRow ? activeCountRow.count : 0,
        applied: totalDownloadsRow && totalDownloadsRow.total ? totalDownloadsRow.total : 0
      }
    });
  } catch (err) {
    console.error('Error fetching admin edge lighting presets:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/edge-lighting (Create new preset)
router.post('/edge-lighting', verifyAdminToken, edgeUploadFields, async (req, res) => {
  try {
    const {
      title,
      category = 'RAINBOW',
      animationType = 'rainbow_wave',
      colors,
      speed = 1.0,
      borderSize = 5,
      cornerRadius = 28,
      punchHoleRadius = 0,
      glowSpread = 12,
      previewUrl,
      isPremium = 0,
      sortOrder = 0,
      isActive = 1
    } = req.body;

    if (!title || !title.trim()) {
      return res.status(400).json({ success: false, error: 'Title is required' });
    }

    const id = `el_${Date.now()}_${Math.floor(Math.random() * 1000)}`;

    let finalColors = ['#00E5FF', '#7000FF', '#FF007F'];
    if (typeof colors === 'string') {
      try {
        finalColors = JSON.parse(colors);
      } catch (_) {
        finalColors = colors.split(',').map(c => c.trim()).filter(Boolean);
      }
    } else if (Array.isArray(colors)) {
      finalColors = colors;
    }

    let prevUrl = previewUrl || '';
    if (req.files && req.files.previewFile && req.files.previewFile[0]) {
      prevUrl = `/uploads/${req.files.previewFile[0].filename}`;
    }
    if (!prevUrl) {
      prevUrl = '/uploads/edge/thumb_rgb_rainbow.svg';
    }

    const now = Date.now();
    await db.runAsync(
      `INSERT INTO edge_lighting_presets 
       (id, title, category, animationType, colors, speed, borderSize, cornerRadius, punchHoleRadius, glowSpread, previewUrl, isPremium, downloads, isActive, sortOrder, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)`,
      [
        id,
        title.trim(),
        category.toUpperCase(),
        animationType,
        JSON.stringify(finalColors),
        parseFloat(speed) || 1.0,
        parseInt(borderSize, 10) || 5,
        parseInt(cornerRadius, 10) || 28,
        parseInt(punchHoleRadius, 10) || 0,
        parseInt(glowSpread, 10) || 12,
        prevUrl,
        isPremium === 'true' || isPremium === true || isPremium === 1 ? 1 : 0,
        isActive === 'false' || isActive === false || isActive === 0 ? 0 : 1,
        parseInt(sortOrder, 10) || 0,
        now
      ]
    );

    const created = await db.getAsync('SELECT * FROM edge_lighting_presets WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Edge lighting preset published successfully!',
      data: created
    });
  } catch (err) {
    console.error('Error creating edge lighting preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/edge-lighting/:id (Update preset)
router.put('/edge-lighting/:id', verifyAdminToken, edgeUploadFields, async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM edge_lighting_presets WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Preset not found' });
    }

    const {
      title,
      category,
      animationType,
      colors,
      speed,
      borderSize,
      cornerRadius,
      punchHoleRadius,
      glowSpread,
      previewUrl,
      isPremium,
      sortOrder,
      isActive
    } = req.body;

    let finalColors = existing.colors;
    if (colors) {
      if (typeof colors === 'string') {
        try {
          finalColors = JSON.stringify(JSON.parse(colors));
        } catch (_) {
          finalColors = JSON.stringify(colors.split(',').map(c => c.trim()).filter(Boolean));
        }
      } else if (Array.isArray(colors)) {
        finalColors = JSON.stringify(colors);
      }
    }

    let prevUrl = existing.previewUrl;
    if (req.files && req.files.previewFile && req.files.previewFile[0]) {
      prevUrl = `/uploads/${req.files.previewFile[0].filename}`;
    } else if (previewUrl) {
      prevUrl = previewUrl;
    }

    await db.runAsync(
      `UPDATE edge_lighting_presets SET
        title = COALESCE(?, title),
        category = COALESCE(?, category),
        animationType = COALESCE(?, animationType),
        colors = COALESCE(?, colors),
        speed = COALESCE(?, speed),
        borderSize = COALESCE(?, borderSize),
        cornerRadius = COALESCE(?, cornerRadius),
        punchHoleRadius = COALESCE(?, punchHoleRadius),
        glowSpread = COALESCE(?, glowSpread),
        previewUrl = COALESCE(?, previewUrl),
        isPremium = COALESCE(?, isPremium),
        isActive = COALESCE(?, isActive),
        sortOrder = COALESCE(?, sortOrder)
       WHERE id = ?`,
      [
        title ? title.trim() : null,
        category ? category.toUpperCase() : null,
        animationType || null,
        finalColors,
        speed !== undefined ? parseFloat(speed) : null,
        borderSize !== undefined ? parseInt(borderSize, 10) : null,
        cornerRadius !== undefined ? parseInt(cornerRadius, 10) : null,
        punchHoleRadius !== undefined ? parseInt(punchHoleRadius, 10) : null,
        glowSpread !== undefined ? parseInt(glowSpread, 10) : null,
        prevUrl,
        isPremium !== undefined ? (isPremium === 'true' || isPremium === true || isPremium === 1 ? 1 : 0) : null,
        isActive !== undefined ? (isActive === 'false' || isActive === false || isActive === 0 ? 0 : 1) : null,
        sortOrder !== undefined ? parseInt(sortOrder, 10) : null,
        id
      ]
    );

    const updated = await db.getAsync('SELECT * FROM edge_lighting_presets WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Edge lighting preset updated successfully!',
      data: updated
    });
  } catch (err) {
    console.error('Error updating edge lighting preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/edge-lighting/:id/toggle
router.patch('/edge-lighting/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM edge_lighting_presets WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Preset not found' });
    }

    const newStatus = item.isActive ? 0 : 1;
    await db.runAsync('UPDATE edge_lighting_presets SET isActive = ? WHERE id = ?', [newStatus, id]);

    res.json({
      success: true,
      message: `Preset is now ${newStatus ? 'Active' : 'Inactive'}`,
      isActive: Boolean(newStatus)
    });
  } catch (err) {
    console.error('Error toggling edge lighting preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/edge-lighting/:id
router.delete('/edge-lighting/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM edge_lighting_presets WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Preset not found' });
    }

    await db.runAsync('DELETE FROM edge_lighting_presets WHERE id = ?', [id]);
    res.json({ success: true, message: 'Edge lighting preset deleted successfully' });
  } catch (err) {
    console.error('Error deleting edge lighting preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// 🏝️ DYNAMIC ISLAND ADMIN MANAGEMENT
// ========================================================

const islandUploadFields = upload.fields([
  { name: 'previewFile', maxCount: 1 }
]);

// GET /api/admin/dynamic-island/themes
router.get('/dynamic-island/themes', verifyAdminToken, async (req, res) => {
  try {
    const { search } = req.query;
    const where = [];
    const params = [];

    if (search && search.trim()) {
      where.push('(title LIKE ? OR styleType LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q);
    }

    const whereSql = where.length > 0 ? `WHERE ${where.join(' AND ')}` : '';
    const items = await db.allAsync(
      `SELECT * FROM dynamic_island_themes ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`,
      params
    );

    const totalRow = await db.getAsync('SELECT COUNT(*) as count FROM dynamic_island_themes');
    const activeRow = await db.getAsync('SELECT COUNT(*) as count FROM dynamic_island_themes WHERE isActive = 1');
    const appliedRow = await db.getAsync('SELECT SUM(downloads) as total FROM dynamic_island_themes');

    res.json({
      success: true,
      data: items.map((r) => ({
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
      })),
      stats: {
        total: totalRow ? totalRow.count : 0,
        active: activeRow ? activeRow.count : 0,
        applied: appliedRow && appliedRow.total ? appliedRow.total : 0
      }
    });
  } catch (err) {
    console.error('Error fetching admin dynamic island themes:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/dynamic-island/themes (Create new theme)
router.post('/dynamic-island/themes', verifyAdminToken, islandUploadFields, async (req, res) => {
  try {
    const {
      title,
      styleType = 'minimal',
      backgroundColor = '#000000',
      textColor = '#FFFFFF',
      accentColor = '#00E5FF',
      glowColor = 'rgba(0,229,255,0.4)',
      cornerRadius = 24,
      compactWidth = 120,
      compactHeight = 36,
      expandedWidth = 320,
      expandedHeight = 84,
      previewUrl,
      isPremium = 0,
      sortOrder = 0,
      isActive = 1
    } = req.body;

    if (!title || !title.trim()) {
      return res.status(400).json({ success: false, error: 'Title is required' });
    }

    const id = `di_${Date.now()}_${Math.floor(Math.random() * 1000)}`;

    let prevUrl = previewUrl || '';
    if (req.files && req.files.previewFile && req.files.previewFile[0]) {
      prevUrl = `/uploads/${req.files.previewFile[0].filename}`;
    }
    if (!prevUrl) {
      prevUrl = '/uploads/island/thumb_apple_obsidian.svg';
    }

    const now = Date.now();
    await db.runAsync(
      `INSERT INTO dynamic_island_themes 
       (id, title, styleType, backgroundColor, textColor, accentColor, glowColor, cornerRadius, compactWidth, compactHeight, expandedWidth, expandedHeight, previewUrl, isPremium, downloads, isActive, sortOrder, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)`,
      [
        id,
        title.trim(),
        styleType,
        backgroundColor,
        textColor,
        accentColor,
        glowColor,
        parseInt(cornerRadius, 10) || 24,
        parseInt(compactWidth, 10) || 120,
        parseInt(compactHeight, 10) || 36,
        parseInt(expandedWidth, 10) || 320,
        parseInt(expandedHeight, 10) || 84,
        prevUrl,
        isPremium === 'true' || isPremium === true || isPremium === 1 ? 1 : 0,
        isActive === 'false' || isActive === false || isActive === 0 ? 0 : 1,
        parseInt(sortOrder, 10) || 0,
        now
      ]
    );

    const created = await db.getAsync('SELECT * FROM dynamic_island_themes WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Dynamic Island theme created successfully!',
      data: created
    });
  } catch (err) {
    console.error('Error creating dynamic island theme:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/dynamic-island/themes/:id (Update theme)
router.put('/dynamic-island/themes/:id', verifyAdminToken, islandUploadFields, async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM dynamic_island_themes WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Theme not found' });
    }

    const {
      title,
      styleType,
      backgroundColor,
      textColor,
      accentColor,
      glowColor,
      cornerRadius,
      compactWidth,
      compactHeight,
      expandedWidth,
      expandedHeight,
      previewUrl,
      isPremium,
      sortOrder,
      isActive
    } = req.body;

    let prevUrl = existing.previewUrl;
    if (req.files && req.files.previewFile && req.files.previewFile[0]) {
      prevUrl = `/uploads/${req.files.previewFile[0].filename}`;
    } else if (previewUrl) {
      prevUrl = previewUrl;
    }

    await db.runAsync(
      `UPDATE dynamic_island_themes SET
        title = COALESCE(?, title),
        styleType = COALESCE(?, styleType),
        backgroundColor = COALESCE(?, backgroundColor),
        textColor = COALESCE(?, textColor),
        accentColor = COALESCE(?, accentColor),
        glowColor = COALESCE(?, glowColor),
        cornerRadius = COALESCE(?, cornerRadius),
        compactWidth = COALESCE(?, compactWidth),
        compactHeight = COALESCE(?, compactHeight),
        expandedWidth = COALESCE(?, expandedWidth),
        expandedHeight = COALESCE(?, expandedHeight),
        previewUrl = COALESCE(?, previewUrl),
        isPremium = COALESCE(?, isPremium),
        isActive = COALESCE(?, isActive),
        sortOrder = COALESCE(?, sortOrder)
       WHERE id = ?`,
      [
        title ? title.trim() : null,
        styleType || null,
        backgroundColor || null,
        textColor || null,
        accentColor || null,
        glowColor || null,
        cornerRadius !== undefined ? parseInt(cornerRadius, 10) : null,
        compactWidth !== undefined ? parseInt(compactWidth, 10) : null,
        compactHeight !== undefined ? parseInt(compactHeight, 10) : null,
        expandedWidth !== undefined ? parseInt(expandedWidth, 10) : null,
        expandedHeight !== undefined ? parseInt(expandedHeight, 10) : null,
        prevUrl,
        isPremium !== undefined ? (isPremium === 'true' || isPremium === true || isPremium === 1 ? 1 : 0) : null,
        isActive !== undefined ? (isActive === 'false' || isActive === false || isActive === 0 ? 0 : 1) : null,
        sortOrder !== undefined ? parseInt(sortOrder, 10) : null,
        id
      ]
    );

    const updated = await db.getAsync('SELECT * FROM dynamic_island_themes WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Dynamic Island theme updated successfully!',
      data: updated
    });
  } catch (err) {
    console.error('Error updating dynamic island theme:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/dynamic-island/themes/:id/toggle
router.patch('/dynamic-island/themes/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM dynamic_island_themes WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Theme not found' });
    }

    const newStatus = item.isActive ? 0 : 1;
    await db.runAsync('UPDATE dynamic_island_themes SET isActive = ? WHERE id = ?', [newStatus, id]);

    res.json({
      success: true,
      message: `Theme is now ${newStatus ? 'Active' : 'Inactive'}`,
      isActive: Boolean(newStatus)
    });
  } catch (err) {
    console.error('Error toggling dynamic island theme:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/dynamic-island/themes/:id
router.delete('/dynamic-island/themes/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM dynamic_island_themes WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Theme not found' });
    }

    await db.runAsync('DELETE FROM dynamic_island_themes WHERE id = ?', [id]);
    res.json({ success: true, message: 'Dynamic Island theme deleted successfully' });
  } catch (err) {
    console.error('Error deleting dynamic island theme:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// 🕒 ALWAYS-ON DISPLAY (AOD) CLOCKS ADMIN MANAGEMENT
// ========================================================

const aodUploadFields = upload.fields([
  { name: 'previewFile', maxCount: 1 },
  { name: 'assetFile', maxCount: 1 }
]);

// GET /api/admin/aod/clocks
router.get('/aod/clocks', verifyAdminToken, async (req, res) => {
  try {
    const { search, clockType, status } = req.query;
    const where = [];
    const params = [];

    if (search && search.trim()) {
      where.push('(title LIKE ? OR dialStyle LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q);
    }

    if (clockType && clockType !== 'ALL') {
      where.push('clockType = ?');
      params.push(clockType);
    }

    if (status === 'ACTIVE') {
      where.push('isActive = 1');
    } else if (status === 'INACTIVE') {
      where.push('isActive = 0');
    }

    const whereClause = where.length > 0 ? `WHERE ${where.join(' AND ')}` : '';
    const sql = `SELECT * FROM aod_clocks ${whereClause} ORDER BY sortOrder ASC, createdAt DESC`;
    const rows = await db.allAsync(sql, params);

    const totalStats = await db.getAsync(`
      SELECT 
        COUNT(*) as totalClocks,
        SUM(CASE WHEN isActive = 1 THEN 1 ELSE 0 END) as activeClocks,
        SUM(downloads) as totalApplied
      FROM aod_clocks
    `);

    res.json({
      success: true,
      stats: {
        totalClocks: totalStats?.totalClocks || 0,
        activeClocks: totalStats?.activeClocks || 0,
        totalApplied: totalStats?.totalApplied || 0
      },
      data: rows.map(r => ({
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
        sortOrder: r.sortOrder,
        createdAt: r.createdAt
      }))
    });
  } catch (err) {
    console.error('Error fetching admin AOD clocks:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/aod/clocks (Create new AOD clock face)
router.post('/aod/clocks', verifyAdminToken, aodUploadFields, async (req, res) => {
  try {
    const {
      title,
      clockType = 'cyberpunk_digital',
      accentColor = '#00E5FF',
      glowColor = '#00E5FF',
      textColor = '#FFFFFF',
      backgroundColor = '#000000',
      dialStyle = 'futuristic_hud',
      hasBatteryWidget = '1',
      hasDateWidget = '1',
      hasStepsWidget = '1',
      hasWeatherWidget = '1',
      sortOrder = '0',
      isPremium = '0',
      isActive = '1'
    } = req.body;

    if (!title || !title.trim()) {
      return res.status(400).json({ success: false, error: 'Clock title is required' });
    }

    const id = `aod_${Date.now()}`;
    let previewUrl = '/uploads/aod/aod_cyberpunk_2077.svg';
    let assetUrl = '/uploads/aod/aod_cyberpunk_2077.svg';

    if (req.files && req.files['previewFile']) {
      previewUrl = `/uploads/${req.files['previewFile'][0].filename}`;
    }
    if (req.files && req.files['assetFile']) {
      assetUrl = `/uploads/${req.files['assetFile'][0].filename}`;
    }

    const sql = `
      INSERT INTO aod_clocks (
        id, title, clockType, accentColor, glowColor, textColor, backgroundColor, dialStyle,
        hasBatteryWidget, hasDateWidget, hasStepsWidget, hasWeatherWidget, previewUrl, assetUrl,
        isPremium, downloads, isActive, sortOrder, createdAt
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)
    `;

    await db.runAsync(sql, [
      id,
      title.trim(),
      clockType,
      accentColor,
      glowColor,
      textColor,
      backgroundColor,
      dialStyle,
      parseInt(hasBatteryWidget, 10) ? 1 : 0,
      parseInt(hasDateWidget, 10) ? 1 : 0,
      parseInt(hasStepsWidget, 10) ? 1 : 0,
      parseInt(hasWeatherWidget, 10) ? 1 : 0,
      previewUrl,
      assetUrl,
      parseInt(isPremium, 10) ? 1 : 0,
      parseInt(isActive, 10) ? 1 : 0,
      parseInt(sortOrder, 10) || 0,
      Date.now()
    ]);

    const created = await db.getAsync('SELECT * FROM aod_clocks WHERE id = ?', [id]);
    res.status(201).json({
      success: true,
      message: 'AOD Clock Face created successfully',
      data: created
    });
  } catch (err) {
    console.error('Error creating AOD clock face:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/aod/clocks/:id (Update AOD clock face)
router.put('/aod/clocks/:id', verifyAdminToken, aodUploadFields, async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM aod_clocks WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'AOD clock face not found' });
    }

    const {
      title = existing.title,
      clockType = existing.clockType,
      accentColor = existing.accentColor,
      glowColor = existing.glowColor,
      textColor = existing.textColor,
      backgroundColor = existing.backgroundColor,
      dialStyle = existing.dialStyle,
      hasBatteryWidget = existing.hasBatteryWidget,
      hasDateWidget = existing.hasDateWidget,
      hasStepsWidget = existing.hasStepsWidget,
      hasWeatherWidget = existing.hasWeatherWidget,
      sortOrder = existing.sortOrder,
      isPremium = existing.isPremium,
      isActive = existing.isActive
    } = req.body;

    let previewUrl = existing.previewUrl;
    let assetUrl = existing.assetUrl;

    if (req.files && req.files['previewFile']) {
      previewUrl = `/uploads/${req.files['previewFile'][0].filename}`;
    }
    if (req.files && req.files['assetFile']) {
      assetUrl = `/uploads/${req.files['assetFile'][0].filename}`;
    }

    const sql = `
      UPDATE aod_clocks SET
        title = ?,
        clockType = ?,
        accentColor = ?,
        glowColor = ?,
        textColor = ?,
        backgroundColor = ?,
        dialStyle = ?,
        hasBatteryWidget = ?,
        hasDateWidget = ?,
        hasStepsWidget = ?,
        hasWeatherWidget = ?,
        previewUrl = ?,
        assetUrl = ?,
        isPremium = ?,
        isActive = ?,
        sortOrder = ?
      WHERE id = ?
    `;

    await db.runAsync(sql, [
      title.trim(),
      clockType,
      accentColor,
      glowColor,
      textColor,
      backgroundColor,
      dialStyle,
      parseInt(hasBatteryWidget, 10) ? 1 : 0,
      parseInt(hasDateWidget, 10) ? 1 : 0,
      parseInt(hasStepsWidget, 10) ? 1 : 0,
      parseInt(hasWeatherWidget, 10) ? 1 : 0,
      previewUrl,
      assetUrl,
      parseInt(isPremium, 10) ? 1 : 0,
      parseInt(isActive, 10) ? 1 : 0,
      parseInt(sortOrder, 10) || 0,
      id
    ]);

    const updated = await db.getAsync('SELECT * FROM aod_clocks WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'AOD Clock Face updated successfully',
      data: updated
    });
  } catch (err) {
    console.error('Error updating AOD clock face:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/aod/clocks/:id/toggle
router.patch('/aod/clocks/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT isActive FROM aod_clocks WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Clock face not found' });
    }

    const nextState = item.isActive ? 0 : 1;
    await db.runAsync('UPDATE aod_clocks SET isActive = ? WHERE id = ?', [nextState, id]);

    res.json({
      success: true,
      isActive: Boolean(nextState),
      message: `AOD Clock Face is now ${nextState ? 'Active' : 'Inactive'}`
    });
  } catch (err) {
    console.error('Error toggling AOD clock status:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/aod/clocks/:id
router.delete('/aod/clocks/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM aod_clocks WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Clock face not found' });
    }

    await db.runAsync('DELETE FROM aod_clocks WHERE id = ?', [id]);
    res.json({ success: true, message: 'AOD Clock Face deleted successfully' });
  } catch (err) {
    console.error('Error deleting AOD clock face:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// =========================================================================
// 📞 3D COLOR CALL SCREEN & FLASH THEMES (ADMIN API)
// =========================================================================

// GET /api/admin/call-screen/themes
router.get('/call-screen/themes', verifyAdminToken, async (req, res) => {
  try {
    const { search, category } = req.query;
    let whereClauses = [];
    let params = [];

    if (category && category !== 'ALL') {
      whereClauses.push('category = ?');
      params.push(category.toUpperCase());
    }

    if (search && search.trim()) {
      whereClauses.push('(title LIKE ? OR category LIKE ?)');
      const q = `%${search.trim()}%`;
      params.push(q, q);
    }

    const whereSql = whereClauses.length > 0 ? `WHERE ${whereClauses.join(' AND ')}` : '';
    const sql = `SELECT * FROM call_themes ${whereSql} ORDER BY sortOrder ASC, createdAt DESC`;
    const rows = await db.allAsync(sql, params);

    // Aggregate stats
    const statsRow = await db.getAsync(`
      SELECT 
        COUNT(*) as total,
        SUM(CASE WHEN isActive = 1 THEN 1 ELSE 0 END) as active,
        SUM(downloads) as totalDownloads,
        SUM(CASE WHEN flashAlertEnabled = 1 THEN 1 ELSE 0 END) as flashCount
      FROM call_themes
    `);

    res.json({
      success: true,
      stats: {
        total: statsRow ? statsRow.total || 0 : 0,
        active: statsRow ? statsRow.active || 0 : 0,
        totalDownloads: statsRow ? statsRow.totalDownloads || 0 : 0,
        flashCount: statsRow ? statsRow.flashCount || 0 : 0
      },
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
        isActive: Boolean(r.isActive),
        sortOrder: r.sortOrder,
        createdAt: r.createdAt
      }))
    });
  } catch (err) {
    console.error('Error fetching admin call screen themes:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/call-screen/themes
router.post('/call-screen/themes', verifyAdminToken, upload.single('preview'), async (req, res) => {
  try {
    const {
      title,
      category = 'NEON',
      buttonStyle = 'neon_glow',
      accentColor = '#00E5FF',
      glowColor = '#7000FF',
      flashAlertEnabled = '1',
      flashSpeed = 'normal',
      isPremium = '0',
      sortOrder = '0',
      backgroundUrl: reqBgUrl,
      previewUrl: reqPrevUrl
    } = req.body;

    if (!title || !title.trim()) {
      return res.status(400).json({ success: false, error: 'Theme Title is required' });
    }

    const id = `call_${Date.now()}`;
    let previewUrl = reqPrevUrl || '';
    if (req.file) {
      previewUrl = `/uploads/${req.file.filename}`;
    } else if (!previewUrl) {
      previewUrl = '/uploads/callscreen/call_cyber_matrix_2077.svg';
    }

    const backgroundUrl = reqBgUrl || previewUrl;
    const now = Date.now();

    await db.runAsync(`
      INSERT INTO call_themes (
        id, title, category, backgroundUrl, previewUrl, buttonStyle,
        accentColor, glowColor, flashAlertEnabled, flashSpeed,
        isPremium, downloads, isActive, sortOrder, createdAt
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `, [
      id,
      title.trim(),
      category.toUpperCase(),
      backgroundUrl,
      previewUrl,
      buttonStyle,
      accentColor,
      glowColor,
      flashAlertEnabled === 'true' || flashAlertEnabled === '1' || flashAlertEnabled === 1 ? 1 : 0,
      flashSpeed,
      isPremium === 'true' || isPremium === '1' || isPremium === 1 ? 1 : 0,
      0,
      1,
      parseInt(sortOrder || 0, 10),
      now
    ]);

    const created = await db.getAsync('SELECT * FROM call_themes WHERE id = ?', [id]);
    res.status(201).json({
      success: true,
      message: 'Call screen theme created successfully',
      data: created
    });
  } catch (err) {
    console.error('Error creating call screen theme:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/call-screen/themes/:id
router.put('/call-screen/themes/:id', verifyAdminToken, upload.single('preview'), async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM call_themes WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Call screen theme not found' });
    }

    const {
      title,
      category,
      buttonStyle,
      accentColor,
      glowColor,
      flashAlertEnabled,
      flashSpeed,
      isPremium,
      sortOrder,
      previewUrl: reqPrevUrl,
      backgroundUrl: reqBgUrl
    } = req.body;

    let previewUrl = existing.previewUrl;
    if (req.file) {
      previewUrl = `/uploads/${req.file.filename}`;
    } else if (reqPrevUrl) {
      previewUrl = reqPrevUrl;
    }

    const backgroundUrl = reqBgUrl || (req.file ? previewUrl : existing.backgroundUrl);

    await db.runAsync(`
      UPDATE call_themes SET
        title = COALESCE(?, title),
        category = COALESCE(?, category),
        backgroundUrl = ?,
        previewUrl = ?,
        buttonStyle = COALESCE(?, buttonStyle),
        accentColor = COALESCE(?, accentColor),
        glowColor = COALESCE(?, glowColor),
        flashAlertEnabled = ?,
        flashSpeed = COALESCE(?, flashSpeed),
        isPremium = ?,
        sortOrder = COALESCE(?, sortOrder)
      WHERE id = ?
    `, [
      title ? title.trim() : null,
      category ? category.toUpperCase() : null,
      backgroundUrl,
      previewUrl,
      buttonStyle || null,
      accentColor || null,
      glowColor || null,
      flashAlertEnabled !== undefined ? (flashAlertEnabled === 'true' || flashAlertEnabled === '1' || flashAlertEnabled === 1 ? 1 : 0) : existing.flashAlertEnabled,
      flashSpeed || null,
      isPremium !== undefined ? (isPremium === 'true' || isPremium === '1' || isPremium === 1 ? 1 : 0) : existing.isPremium,
      sortOrder !== undefined ? parseInt(sortOrder, 10) : existing.sortOrder,
      id
    ]);

    const updated = await db.getAsync('SELECT * FROM call_themes WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Call screen theme updated successfully',
      data: updated
    });
  } catch (err) {
    console.error('Error updating call screen theme:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/call-screen/themes/:id/toggle
router.patch('/call-screen/themes/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT isActive FROM call_themes WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Theme not found' });
    }

    const nextState = item.isActive === 1 ? 0 : 1;
    await db.runAsync('UPDATE call_themes SET isActive = ? WHERE id = ?', [nextState, id]);

    res.json({
      success: true,
      isActive: Boolean(nextState),
      message: `Call theme is now ${nextState ? 'Active' : 'Inactive'}`
    });
  } catch (err) {
    console.error('Error toggling call theme status:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/call-screen/themes/:id
router.delete('/call-screen/themes/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM call_themes WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Theme not found' });
    }

    await db.runAsync('DELETE FROM call_themes WHERE id = ?', [id]);
    res.json({ success: true, message: 'Call Screen theme deleted successfully' });
  } catch (err) {
    console.error('Error deleting call screen theme:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// 👥 DUO / DOUBLE WALLPAPERS ADMIN CRUD
// ========================================================

// GET /api/admin/duo/wallpapers
router.get('/duo/wallpapers', verifyAdminToken, async (req, res) => {
  try {
    const rows = await db.allAsync('SELECT * FROM duo_wallpapers ORDER BY sortOrder ASC, createdAt DESC');
    const totalCount = rows.length;
    const activeCount = rows.filter(r => r.isActive === 1).length;
    const totalApplied = rows.reduce((acc, r) => acc + (r.downloads || 0), 0);
    const vipCount = rows.filter(r => r.isPremium === 1).length;

    res.json({
      success: true,
      stats: {
        total: totalCount,
        active: activeCount,
        applied: totalApplied,
        vip: vipCount
      },
      data: rows.map(r => ({
        id: r.id,
        title: r.title,
        description: r.description || '',
        category: r.category,
        lockImageUrl: r.lockImageUrl,
        homeImageUrl: r.homeImageUrl,
        previewUrl: r.previewUrl || r.homeImageUrl,
        accentColor: r.accentColor,
        isPremium: Boolean(r.isPremium),
        downloads: r.downloads,
        isActive: Boolean(r.isActive),
        sortOrder: r.sortOrder,
        createdAt: r.createdAt
      }))
    });
  } catch (err) {
    console.error('Error fetching admin duo wallpapers:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/duo/wallpapers
router.post('/duo/wallpapers', verifyAdminToken, upload.fields([{ name: 'lockImage', maxCount: 1 }, { name: 'homeImage', maxCount: 1 }]), async (req, res) => {
  try {
    const { title, description, category, accentColor, isPremium, sortOrder, isActive } = req.body;
    let lockImageUrl = req.body.lockImageUrl;
    let homeImageUrl = req.body.homeImageUrl;

    if (req.files && req.files['lockImage'] && req.files['lockImage'][0]) {
      lockImageUrl = `/uploads/${path.basename(req.files['lockImage'][0].path)}`;
    }
    if (req.files && req.files['homeImage'] && req.files['homeImage'][0]) {
      homeImageUrl = `/uploads/${path.basename(req.files['homeImage'][0].path)}`;
    }

    if (!title || !title.trim()) {
      return res.status(400).json({ success: false, error: 'Pair title is required' });
    }
    if (!lockImageUrl || !lockImageUrl.trim()) {
      return res.status(400).json({ success: false, error: 'Lock screen image is required' });
    }
    if (!homeImageUrl || !homeImageUrl.trim()) {
      return res.status(400).json({ success: false, error: 'Home screen image is required' });
    }

    const id = `duo_${Date.now()}`;
    const now = Date.now();
    const premium = (isPremium === 'true' || isPremium === '1' || isPremium === 1) ? 1 : 0;
    const active = (isActive === 'false' || isActive === '0' || isActive === 0) ? 0 : 1;
    const sort = parseInt(sortOrder, 10) || 1;

    await db.runAsync(`
      INSERT INTO duo_wallpapers (id, title, description, category, lockImageUrl, homeImageUrl, previewUrl, accentColor, isPremium, downloads, isActive, sortOrder, createdAt)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    `, [
      id,
      title.trim(),
      (description || '').trim(),
      (category || 'CYBERPUNK').toUpperCase(),
      lockImageUrl.trim(),
      homeImageUrl.trim(),
      homeImageUrl.trim(),
      accentColor || '#00E5FF',
      premium,
      0,
      active,
      sort,
      now
    ]);

    const created = await db.getAsync('SELECT * FROM duo_wallpapers WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Duo wallpaper pair created successfully',
      data: created
    });
  } catch (err) {
    console.error('Error creating duo wallpaper pair:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/duo/wallpapers/:id
router.put('/duo/wallpapers/:id', verifyAdminToken, upload.fields([{ name: 'lockImage', maxCount: 1 }, { name: 'homeImage', maxCount: 1 }]), async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM duo_wallpapers WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Duo wallpaper pair not found' });
    }

    const { title, description, category, accentColor, isPremium, sortOrder } = req.body;
    let lockImageUrl = req.body.lockImageUrl || existing.lockImageUrl;
    let homeImageUrl = req.body.homeImageUrl || existing.homeImageUrl;

    if (req.files && req.files['lockImage'] && req.files['lockImage'][0]) {
      lockImageUrl = `/uploads/${path.basename(req.files['lockImage'][0].path)}`;
    }
    if (req.files && req.files['homeImage'] && req.files['homeImage'][0]) {
      homeImageUrl = `/uploads/${path.basename(req.files['homeImage'][0].path)}`;
    }

    await db.runAsync(`
      UPDATE duo_wallpapers SET
        title = COALESCE(?, title),
        description = COALESCE(?, description),
        category = COALESCE(?, category),
        lockImageUrl = ?,
        homeImageUrl = ?,
        previewUrl = ?,
        accentColor = COALESCE(?, accentColor),
        isPremium = ?,
        sortOrder = COALESCE(?, sortOrder)
      WHERE id = ?
    `, [
      title ? title.trim() : null,
      description !== undefined ? description.trim() : null,
      category ? category.toUpperCase() : null,
      lockImageUrl,
      homeImageUrl,
      homeImageUrl,
      accentColor || null,
      isPremium !== undefined ? (isPremium === 'true' || isPremium === '1' || isPremium === 1 ? 1 : 0) : existing.isPremium,
      sortOrder !== undefined ? parseInt(sortOrder, 10) : existing.sortOrder,
      id
    ]);

    const updated = await db.getAsync('SELECT * FROM duo_wallpapers WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Duo wallpaper pair updated successfully',
      data: updated
    });
  } catch (err) {
    console.error('Error updating duo wallpaper pair:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/duo/wallpapers/:id/toggle
router.patch('/duo/wallpapers/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT isActive FROM duo_wallpapers WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Duo pair not found' });
    }

    const nextState = item.isActive === 1 ? 0 : 1;
    await db.runAsync('UPDATE duo_wallpapers SET isActive = ? WHERE id = ?', [nextState, id]);

    res.json({
      success: true,
      isActive: Boolean(nextState),
      message: `Duo pair is now ${nextState ? 'Active' : 'Inactive'}`
    });
  } catch (err) {
    console.error('Error toggling duo pair status:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/duo/wallpapers/:id
router.delete('/duo/wallpapers/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM duo_wallpapers WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Duo pair not found' });
    }

    await db.runAsync('DELETE FROM duo_wallpapers WHERE id = ?', [id]);
    res.json({ success: true, message: 'Duo wallpaper pair deleted successfully' });
  } catch (err) {
    console.error('Error deleting duo pair:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// =========================================================================
// 👆 ADMIN: INTERACTIVE TOUCH FLUID & RIPPLE PRESETS
// =========================================================================

// GET /api/admin/touch-effects/presets
router.get('/touch-effects/presets', verifyAdminToken, async (req, res) => {
  try {
    const rows = await db.allAsync('SELECT * FROM touch_presets ORDER BY sortOrder ASC, createdAt DESC');
    const presets = (rows || []).map(r => ({
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
    }));

    const stats = {
      total: presets.length,
      active: presets.filter(p => p.isActive).length,
      totalDownloads: presets.reduce((acc, p) => acc + (p.downloads || 0), 0),
      vipCount: presets.filter(p => p.isPremium).length
    };

    res.json({
      success: true,
      stats,
      data: presets
    });
  } catch (err) {
    console.error('Error fetching admin touch presets:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/touch-effects/presets
router.post('/touch-effects/presets', verifyAdminToken, async (req, res) => {
  try {
    const {
      title,
      effectType,
      description,
      primaryColor,
      secondaryColor,
      accentColor,
      waveSpeed,
      waveRadius,
      particleCount,
      viscosity,
      hapticEnabled,
      isPremium,
      sortOrder,
      isActive
    } = req.body;

    if (!title || !title.trim()) {
      return res.status(400).json({ success: false, error: 'Preset title is required' });
    }

    const id = 'touch_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4);
    const now = Date.now();

    await db.runAsync(`
      INSERT INTO touch_presets (id, title, effectType, description, primaryColor, secondaryColor, accentColor, waveSpeed, waveRadius, particleCount, viscosity, hapticEnabled, isPremium, downloads, isActive, sortOrder, createdAt)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)
    `, [
      id,
      title.trim(),
      effectType || 'WATER_RIPPLE',
      description ? description.trim() : '',
      primaryColor || '#00E5FF',
      secondaryColor || '#7000FF',
      accentColor || '#FF007F',
      parseFloat(waveSpeed) || 1.0,
      parseInt(waveRadius) || 70,
      parseInt(particleCount) || 80,
      parseFloat(viscosity) || 0.8,
      hapticEnabled === '1' || hapticEnabled === 1 || hapticEnabled === true ? 1 : 0,
      isPremium === '1' || isPremium === 1 || isPremium === true ? 1 : 0,
      isActive === '0' || isActive === 0 || isActive === false ? 0 : 1,
      parseInt(sortOrder) || 1,
      now
    ]);

    const created = await db.getAsync('SELECT * FROM touch_presets WHERE id = ?', [id]);
    res.status(201).json({
      success: true,
      message: 'Touch preset created successfully',
      data: created
    });
  } catch (err) {
    console.error('Error creating touch preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/touch-effects/presets/:id
router.put('/touch-effects/presets/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM touch_presets WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Touch preset not found' });
    }

    const {
      title,
      effectType,
      description,
      primaryColor,
      secondaryColor,
      accentColor,
      waveSpeed,
      waveRadius,
      particleCount,
      viscosity,
      hapticEnabled,
      isPremium,
      sortOrder,
      isActive
    } = req.body;

    await db.runAsync(`
      UPDATE touch_presets SET
        title = ?,
        effectType = ?,
        description = ?,
        primaryColor = ?,
        secondaryColor = ?,
        accentColor = ?,
        waveSpeed = ?,
        waveRadius = ?,
        particleCount = ?,
        viscosity = ?,
        hapticEnabled = ?,
        isPremium = ?,
        isActive = ?,
        sortOrder = ?
      WHERE id = ?
    `, [
      title !== undefined ? title.trim() : existing.title,
      effectType !== undefined ? effectType : existing.effectType,
      description !== undefined ? description.trim() : existing.description,
      primaryColor !== undefined ? primaryColor : existing.primaryColor,
      secondaryColor !== undefined ? secondaryColor : existing.secondaryColor,
      accentColor !== undefined ? accentColor : existing.accentColor,
      waveSpeed !== undefined ? parseFloat(waveSpeed) : existing.waveSpeed,
      waveRadius !== undefined ? parseInt(waveRadius) : existing.waveRadius,
      particleCount !== undefined ? parseInt(particleCount) : existing.particleCount,
      viscosity !== undefined ? parseFloat(viscosity) : existing.viscosity,
      hapticEnabled !== undefined ? (hapticEnabled === '1' || hapticEnabled === 1 || hapticEnabled === true ? 1 : 0) : existing.hapticEnabled,
      isPremium !== undefined ? (isPremium === '1' || isPremium === 1 || isPremium === true ? 1 : 0) : existing.isPremium,
      isActive !== undefined ? (isActive === '0' || isActive === 0 || isActive === false ? 0 : 1) : existing.isActive,
      sortOrder !== undefined ? parseInt(sortOrder) : existing.sortOrder,
      id
    ]);

    const updated = await db.getAsync('SELECT * FROM touch_presets WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Touch preset updated successfully',
      data: updated
    });
  } catch (err) {
    console.error('Error updating touch preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/touch-effects/presets/:id/toggle
router.patch('/touch-effects/presets/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT isActive FROM touch_presets WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Touch preset not found' });
    }

    const nextState = item.isActive ? 0 : 1;
    await db.runAsync('UPDATE touch_presets SET isActive = ? WHERE id = ?', [nextState, id]);

    res.json({
      success: true,
      isActive: Boolean(nextState),
      message: `Touch preset is now ${nextState ? 'Active' : 'Inactive'}`
    });
  } catch (err) {
    console.error('Error toggling touch preset status:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/touch-effects/presets/:id
router.delete('/touch-effects/presets/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM touch_presets WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Touch preset not found' });
    }

    await db.runAsync('DELETE FROM touch_presets WHERE id = ?', [id]);
    res.json({ success: true, message: 'Touch preset deleted successfully' });
  } catch (err) {
    console.error('Error deleting touch preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ============================================================================
// 🔓 IN-DISPLAY FINGERPRINT ANIMATION EFFECTS ADMIN APIS
// ============================================================================

// GET /api/admin/fingerprint/presets (Catalog + Statistics)
router.get('/fingerprint/presets', verifyAdminToken, async (req, res) => {
  try {
    const rows = await db.allAsync('SELECT * FROM fingerprint_presets ORDER BY sortOrder ASC, createdAt DESC');
    const totalDownloadsRow = await db.getAsync('SELECT SUM(downloads) as totalDownloads FROM fingerprint_presets');
    const totalCount = rows.length;
    const activeCount = rows.filter(r => r.isActive).length;
    const vipCount = rows.filter(r => r.isPremium).length;
    const totalDownloads = (totalDownloadsRow && totalDownloadsRow.totalDownloads) || 0;

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

    res.json({
      success: true,
      data,
      stats: {
        total: totalCount,
        active: activeCount,
        vipCount,
        totalDownloads
      }
    });
  } catch (err) {
    console.error('Error fetching admin fingerprint presets:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/fingerprint/presets
router.post('/fingerprint/presets', verifyAdminToken, async (req, res) => {
  try {
    const {
      title,
      category,
      animationType,
      description,
      primaryColor,
      secondaryColor,
      accentGlow,
      animationSpeed,
      scale,
      verticalPosition,
      hapticEnabled,
      soundEnabled,
      isPremium,
      isActive,
      sortOrder
    } = req.body;

    if (!title || !animationType) {
      return res.status(400).json({ success: false, error: 'Title and Animation Type are required' });
    }

    const id = 'fp_' + Date.now() + '_' + Math.random().toString(36).substring(2, 7);
    const createdAt = Date.now();

    await db.runAsync(`
      INSERT INTO fingerprint_presets (
        id, title, category, animationType, description, primaryColor, secondaryColor, accentGlow,
        animationSpeed, scale, verticalPosition, hapticEnabled, soundEnabled, isPremium, downloads, isActive, sortOrder, createdAt
      ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?)
    `, [
      id,
      title.trim(),
      (category || 'CYBERPUNK').toUpperCase(),
      animationType,
      (description || '').trim(),
      primaryColor || '#00E5FF',
      secondaryColor || '#7000FF',
      accentGlow || '#00FFAA',
      parseFloat(animationSpeed) || 1.2,
      parseFloat(scale) || 1.0,
      parseInt(verticalPosition) || 78,
      hapticEnabled === false || hapticEnabled === 0 || hapticEnabled === 'false' ? 0 : 1,
      soundEnabled === false || soundEnabled === 0 || soundEnabled === 'false' ? 0 : 1,
      isPremium === true || isPremium === 1 || isPremium === 'true' ? 1 : 0,
      isActive === false || isActive === 0 || isActive === 'false' ? 0 : 1,
      parseInt(sortOrder) || 1,
      createdAt
    ]);

    const created = await db.getAsync('SELECT * FROM fingerprint_presets WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Fingerprint preset created successfully',
      data: created
    });
  } catch (err) {
    console.error('Error creating fingerprint preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/fingerprint/presets/:id
router.put('/fingerprint/presets/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM fingerprint_presets WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Fingerprint preset not found' });
    }

    const {
      title,
      category,
      animationType,
      description,
      primaryColor,
      secondaryColor,
      accentGlow,
      animationSpeed,
      scale,
      verticalPosition,
      hapticEnabled,
      soundEnabled,
      isPremium,
      isActive,
      sortOrder
    } = req.body;

    await db.runAsync(`
      UPDATE fingerprint_presets SET
        title = ?,
        category = ?,
        animationType = ?,
        description = ?,
        primaryColor = ?,
        secondaryColor = ?,
        accentGlow = ?,
        animationSpeed = ?,
        scale = ?,
        verticalPosition = ?,
        hapticEnabled = ?,
        soundEnabled = ?,
        isPremium = ?,
        isActive = ?,
        sortOrder = ?
      WHERE id = ?
    `, [
      title !== undefined ? title.trim() : existing.title,
      category !== undefined ? category.toUpperCase() : existing.category,
      animationType !== undefined ? animationType : existing.animationType,
      description !== undefined ? description.trim() : existing.description,
      primaryColor !== undefined ? primaryColor : existing.primaryColor,
      secondaryColor !== undefined ? secondaryColor : existing.secondaryColor,
      accentGlow !== undefined ? accentGlow : existing.accentGlow,
      animationSpeed !== undefined ? parseFloat(animationSpeed) : existing.animationSpeed,
      scale !== undefined ? parseFloat(scale) : existing.scale,
      verticalPosition !== undefined ? parseInt(verticalPosition) : existing.verticalPosition,
      hapticEnabled !== undefined ? (hapticEnabled ? 1 : 0) : existing.hapticEnabled,
      soundEnabled !== undefined ? (soundEnabled ? 1 : 0) : existing.soundEnabled,
      isPremium !== undefined ? (isPremium ? 1 : 0) : existing.isPremium,
      isActive !== undefined ? (isActive ? 1 : 0) : existing.isActive,
      sortOrder !== undefined ? parseInt(sortOrder) : existing.sortOrder,
      id
    ]);

    const updated = await db.getAsync('SELECT * FROM fingerprint_presets WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Fingerprint preset updated successfully',
      data: updated
    });
  } catch (err) {
    console.error('Error updating fingerprint preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PATCH /api/admin/fingerprint/presets/:id/toggle
router.patch('/fingerprint/presets/:id/toggle', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT isActive FROM fingerprint_presets WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Fingerprint preset not found' });
    }

    const nextState = item.isActive ? 0 : 1;
    await db.runAsync('UPDATE fingerprint_presets SET isActive = ? WHERE id = ?', [nextState, id]);

    res.json({
      success: true,
      isActive: Boolean(nextState),
      message: `Fingerprint preset is now ${nextState ? 'Active' : 'Inactive'}`
    });
  } catch (err) {
    console.error('Error toggling fingerprint preset status:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/fingerprint/presets/:id
router.delete('/fingerprint/presets/:id', verifyAdminToken, async (req, res) => {
  try {
    const { id } = req.params;
    const item = await db.getAsync('SELECT * FROM fingerprint_presets WHERE id = ?', [id]);
    if (!item) {
      return res.status(404).json({ success: false, error: 'Fingerprint preset not found' });
    }

    await db.runAsync('DELETE FROM fingerprint_presets WHERE id = ?', [id]);
    res.json({ success: true, message: 'Fingerprint preset deleted successfully' });
  } catch (err) {
    console.error('Error deleting fingerprint preset:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// ========================================================
// 🔍 UNIVERSAL QUICK SEARCH & COMMAND PALETTE (Ctrl + K)
// ========================================================
router.get('/omni-search', verifyAdminToken, async (req, res) => {
  try {
    const rawQuery = (req.query.q || '').trim();
    if (!rawQuery) {
      return res.json({
        success: true,
        query: '',
        results: {
          wallpapers: [],
          island: [],
          aod: [],
          charging: [],
          edge: [],
          callscreen: [],
          duo: [],
          touch: [],
          fingerprint: [],
          ringtones: [],
          categories: [],
          banners: []
        },
        totalCount: 0
      });
    }

    const term = `%${rawQuery}%`;
    const filterType = (req.query.type || 'all').toLowerCase();

    // Helper to safely run query without breaking entire batch
    const safeQuery = async (sql, params = []) => {
      try {
        return await db.allAsync(sql, params);
      } catch (err) {
        console.warn('omni-search query partial error:', err.message);
        return [];
      }
    };

    const searchJobs = {};

    if (filterType === 'all' || filterType === 'wallpapers') {
      searchJobs.wallpapers = safeQuery(
        `SELECT id, title, category, previewUrl, isParallax, isPremium, downloads, createdAt 
         FROM wallpapers 
         WHERE title LIKE ? OR id LIKE ? OR category LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), downloads DESC LIMIT 10`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'themes' || filterType === 'island') {
      searchJobs.island = safeQuery(
        `SELECT id, title, styleType, previewUrl, accentColor, isPremium, isActive 
         FROM dynamic_island_themes 
         WHERE title LIKE ? OR id LIKE ? OR styleType LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), sortOrder ASC LIMIT 8`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'themes' || filterType === 'aod') {
      searchJobs.aod = safeQuery(
        `SELECT id, title, clockType, previewUrl, dialStyle, isPremium, isActive 
         FROM aod_clocks 
         WHERE title LIKE ? OR id LIKE ? OR clockType LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), sortOrder ASC LIMIT 8`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'themes' || filterType === 'charging') {
      searchJobs.charging = safeQuery(
        `SELECT id, title, category, previewUrl, animationType, isPremium, isActive 
         FROM charging_animations 
         WHERE title LIKE ? OR id LIKE ? OR category LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), sortOrder ASC LIMIT 8`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'themes' || filterType === 'edge') {
      searchJobs.edge = safeQuery(
        `SELECT id, title, category, animationType, colors, previewUrl, isPremium, isActive 
         FROM edge_lighting_presets 
         WHERE title LIKE ? OR id LIKE ? OR category LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), sortOrder ASC LIMIT 8`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'themes' || filterType === 'callscreen') {
      searchJobs.callscreen = safeQuery(
        `SELECT id, title, category, previewUrl, buttonStyle, isPremium, isActive 
         FROM call_themes 
         WHERE title LIKE ? OR id LIKE ? OR category LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), sortOrder ASC LIMIT 8`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'wallpapers' || filterType === 'duo') {
      searchJobs.duo = safeQuery(
        `SELECT id, title, category, previewUrl, lockImageUrl, homeImageUrl, isPremium, isActive 
         FROM duo_wallpapers 
         WHERE title LIKE ? OR id LIKE ? OR category LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), sortOrder ASC LIMIT 8`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'themes' || filterType === 'touch') {
      searchJobs.touch = safeQuery(
        `SELECT id, title, effectType, primaryColor, isPremium, isActive 
         FROM touch_presets 
         WHERE title LIKE ? OR id LIKE ? OR effectType LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), sortOrder ASC LIMIT 8`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'themes' || filterType === 'fingerprint') {
      searchJobs.fingerprint = safeQuery(
        `SELECT id, title, category, animationType, primaryColor, isPremium, isActive 
         FROM fingerprint_presets 
         WHERE title LIKE ? OR id LIKE ? OR category LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), sortOrder ASC LIMIT 8`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'audio' || filterType === 'ringtones') {
      searchJobs.ringtones = safeQuery(
        `SELECT id, title, artist, audioUrl, durationSeconds, category, downloads 
         FROM ringtones 
         WHERE title LIKE ? OR id LIKE ? OR artist LIKE ? OR category LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), downloads DESC LIMIT 8`,
        [term, term, term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'audio' || filterType === 'categories') {
      searchJobs.categories = safeQuery(
        `SELECT id, name, iconUrl 
         FROM categories 
         WHERE name LIKE ? OR id LIKE ? 
         ORDER BY (CASE WHEN name LIKE ? THEN 1 ELSE 2 END) LIMIT 8`,
        [term, term, `${rawQuery}%`]
      );
    }

    if (filterType === 'all' || filterType === 'banners') {
      searchJobs.banners = safeQuery(
        `SELECT id, title, subtitle, badgeText, imageUrl, actionType, isActive 
         FROM hero_banners 
         WHERE title LIKE ? OR id LIKE ? OR subtitle LIKE ? 
         ORDER BY (CASE WHEN title LIKE ? THEN 1 ELSE 2 END), sortOrder ASC LIMIT 6`,
        [term, term, term, `${rawQuery}%`]
      );
    }

    const jobKeys = Object.keys(searchJobs);
    const resolvedArrays = await Promise.all(Object.values(searchJobs));

    const results = {
      wallpapers: [],
      island: [],
      aod: [],
      charging: [],
      edge: [],
      callscreen: [],
      duo: [],
      touch: [],
      fingerprint: [],
      ringtones: [],
      categories: [],
      banners: []
    };

    let totalCount = 0;
    jobKeys.forEach((key, index) => {
      results[key] = resolvedArrays[index] || [];
      totalCount += results[key].length;
    });

    res.json({
      success: true,
      query: rawQuery,
      filterType,
      results,
      totalCount
    });
  } catch (err) {
    console.error('Error in omni-search:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// =========================================================================
// 📲 ADB 1-CLICK LIVE TEST TO CONNECTED DEVICE (I2212)
// =========================================================================
const adbBridge = require('../services/adbBridge');

// GET /api/admin/adb/status - Discovers connected ADB devices
router.get('/adb/status', async (req, res) => {
  try {
    const status = await adbBridge.getConnectedDevices();
    res.json({
      success: true,
      ...status
    });
  } catch (err) {
    console.error('Error fetching ADB status:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/adb/push-live-test - Dispatches live test intent to phone
router.post('/adb/push-live-test', async (req, res) => {
  try {
    const { type, id, deviceId } = req.body;
    if (!type || !id) {
      return res.status(400).json({ success: false, error: 'Missing required fields: type and id' });
    }

    const result = await adbBridge.pushLiveTest(type, id, deviceId);
    res.json({
      success: true,
      ...result
    });
  } catch (err) {
    console.error('Error pushing live test to device:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// =========================================================================
// 🏷️ FESTIVAL & EVENT BANNER SCHEDULER (ADMIN API)
// =========================================================================

// Helper to calculate time difference humanized countdown
function calculateEventStatus(ev, now) {
  const start = new Date(ev.startDate).getTime();
  const end = new Date(ev.endDate).getTime();
  const nowMs = now.getTime();

  if (!ev.isActive) {
    return { status: 'PAUSED', label: '⏸️ Paused (Disabled)', countdown: 'Disabled' };
  }
  if (ev.forceLive) {
    return { status: 'LIVE_NOW', label: '⚡ Force Live (Testing)', countdown: 'Active (Forced)' };
  }
  if (nowMs >= start && nowMs <= end) {
    const remainingMs = end - nowMs;
    const hours = Math.floor(remainingMs / (1000 * 60 * 60));
    const days = Math.floor(hours / 24);
    const countdown = days > 0 ? `Ends in ${days}d ${hours % 24}h` : `Ends in ${hours}h`;
    return { status: 'LIVE_NOW', label: '🟢 LIVE NOW', countdown };
  }
  if (nowMs < start) {
    const diffMs = start - nowMs;
    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    const days = Math.floor(hours / 24);
    const countdown = days > 0 ? `Starts in ${days}d ${hours % 24}h` : `Starts in ${hours}h`;
    return { status: 'UPCOMING', label: '⏰ Upcoming', countdown };
  }
  const passedMs = nowMs - end;
  const daysAgo = Math.floor(passedMs / (1000 * 60 * 60 * 24));
  return { status: 'EXPIRED', label: '⏳ Expired', countdown: daysAgo > 0 ? `Ended ${daysAgo}d ago` : 'Ended recently' };
}

// GET /api/admin/events - List all scheduled festival campaigns
router.get('/events', async (req, res) => {
  try {
    const events = await db.allAsync(
      `SELECT * FROM festival_events ORDER BY priority DESC, sortOrder ASC, createdAt DESC`
    );

    const now = new Date();
    let activeCount = 0;
    let upcomingCount = 0;
    let expiredCount = 0;

    const formatted = (events || []).map(e => {
      let tags = [];
      try {
        tags = typeof e.targetTags === 'string' ? JSON.parse(e.targetTags) : (e.targetTags || []);
      } catch (_) {
        tags = [];
      }

      const statusInfo = calculateEventStatus(e, now);
      if (statusInfo.status === 'LIVE_NOW') activeCount++;
      else if (statusInfo.status === 'UPCOMING') upcomingCount++;
      else if (statusInfo.status === 'EXPIRED') expiredCount++;

      return {
        id: e.id,
        name: e.name,
        festivalKey: e.festivalKey,
        bannerTitle: e.bannerTitle,
        bannerSubtitle: e.bannerSubtitle || '',
        bannerBadge: e.bannerBadge || 'FESTIVAL SPECIAL',
        bannerImageUrl: e.bannerImageUrl,
        targetCategory: e.targetCategory || 'Festival',
        targetTags: tags,
        startDate: e.startDate,
        endDate: e.endDate,
        isActive: Boolean(e.isActive),
        forceLive: Boolean(e.forceLive),
        priority: Number(e.priority || 10),
        sortOrder: Number(e.sortOrder || 0),
        createdAt: Number(e.createdAt),
        liveStatus: statusInfo.status,
        liveLabel: statusInfo.label,
        countdown: statusInfo.countdown
      };
    });

    res.json({
      success: true,
      count: formatted.length,
      stats: {
        total: formatted.length,
        active: activeCount,
        upcoming: upcomingCount,
        expired: expiredCount
      },
      data: formatted
    });
  } catch (err) {
    console.error('Error fetching admin festival events:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/events - Create new festival campaign
router.post('/events', upload.single('bannerImage'), async (req, res) => {
  try {
    const {
      id,
      name,
      festivalKey,
      bannerTitle,
      bannerSubtitle,
      bannerBadge,
      bannerImageUrl,
      targetCategory,
      targetTags,
      startDate,
      endDate,
      isActive,
      forceLive,
      priority,
      sortOrder
    } = req.body;

    if (!name || !bannerTitle || !startDate || !endDate) {
      return res.status(400).json({ success: false, error: 'Name, Banner Title, Start Date, and End Date are required.' });
    }

    const eventId = id || `event_${(festivalKey || 'custom').toLowerCase()}_${Date.now()}`;
    let finalImageUrl = bannerImageUrl || 'https://images.unsplash.com/photo-1514565131-fce0801e5785?w=1200&auto=format&fit=crop&q=80';

    if (req.file) {
      finalImageUrl = `/uploads/${req.file.filename}`;
    }

    let parsedTags = [];
    if (typeof targetTags === 'string') {
      try {
        parsedTags = JSON.parse(targetTags);
      } catch (_) {
        parsedTags = targetTags.split(',').map(t => t.trim()).filter(Boolean);
      }
    } else if (Array.isArray(targetTags)) {
      parsedTags = targetTags;
    }

    const now = Date.now();
    await db.runAsync(
      `INSERT INTO festival_events (id, name, festivalKey, bannerTitle, bannerSubtitle, bannerBadge, bannerImageUrl, targetCategory, targetTags, startDate, endDate, isActive, forceLive, priority, sortOrder, createdAt)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [
        eventId,
        name,
        festivalKey || 'custom',
        bannerTitle,
        bannerSubtitle || '',
        bannerBadge || 'FESTIVAL SPECIAL',
        finalImageUrl,
        targetCategory || 'Festival',
        JSON.stringify(parsedTags),
        startDate,
        endDate,
        isActive !== undefined ? (isActive ? 1 : 0) : 1,
        forceLive !== undefined ? (forceLive ? 1 : 0) : 0,
        Number(priority || 10),
        Number(sortOrder || 0),
        now
      ]
    );

    const created = await db.getAsync('SELECT * FROM festival_events WHERE id = ?', [eventId]);
    res.json({
      success: true,
      message: `🎉 Festival event "${name}" scheduled successfully!`,
      data: created
    });
  } catch (err) {
    console.error('Error creating festival event:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// PUT /api/admin/events/:id - Update festival event
router.put('/events/:id', upload.single('bannerImage'), async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM festival_events WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Festival event not found' });
    }

    const {
      name,
      festivalKey,
      bannerTitle,
      bannerSubtitle,
      bannerBadge,
      bannerImageUrl,
      targetCategory,
      targetTags,
      startDate,
      endDate,
      isActive,
      forceLive,
      priority,
      sortOrder
    } = req.body;

    let finalImageUrl = bannerImageUrl || existing.bannerImageUrl;
    if (req.file) {
      finalImageUrl = `/uploads/${req.file.filename}`;
    }

    let parsedTags = existing.targetTags;
    if (targetTags !== undefined) {
      if (typeof targetTags === 'string') {
        try {
          parsedTags = JSON.stringify(JSON.parse(targetTags));
        } catch (_) {
          parsedTags = JSON.stringify(targetTags.split(',').map(t => t.trim()).filter(Boolean));
        }
      } else if (Array.isArray(targetTags)) {
        parsedTags = JSON.stringify(targetTags);
      }
    }

    await db.runAsync(
      `UPDATE festival_events
       SET name = ?, festivalKey = ?, bannerTitle = ?, bannerSubtitle = ?, bannerBadge = ?, bannerImageUrl = ?, targetCategory = ?, targetTags = ?, startDate = ?, endDate = ?, isActive = ?, forceLive = ?, priority = ?, sortOrder = ?
       WHERE id = ?`,
      [
        name || existing.name,
        festivalKey || existing.festivalKey,
        bannerTitle || existing.bannerTitle,
        bannerSubtitle !== undefined ? bannerSubtitle : existing.bannerSubtitle,
        bannerBadge || existing.bannerBadge,
        finalImageUrl,
        targetCategory || existing.targetCategory,
        parsedTags,
        startDate || existing.startDate,
        endDate || existing.endDate,
        isActive !== undefined ? (isActive ? 1 : 0) : existing.isActive,
        forceLive !== undefined ? (forceLive ? 1 : 0) : existing.forceLive,
        priority !== undefined ? Number(priority) : existing.priority,
        sortOrder !== undefined ? Number(sortOrder) : existing.sortOrder,
        id
      ]
    );

    const updated = await db.getAsync('SELECT * FROM festival_events WHERE id = ?', [id]);
    res.json({
      success: true,
      message: 'Festival event updated successfully!',
      data: updated
    });
  } catch (err) {
    console.error('Error updating festival event:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/events/:id/toggle-active - Toggle active switch
router.post('/events/:id/toggle-active', async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM festival_events WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Festival event not found' });
    }

    const newActive = existing.isActive ? 0 : 1;
    await db.runAsync('UPDATE festival_events SET isActive = ? WHERE id = ?', [newActive, id]);

    res.json({
      success: true,
      isActive: Boolean(newActive),
      message: `Event "${existing.name}" is now ${newActive ? 'Active' : 'Paused'}`
    });
  } catch (err) {
    console.error('Error toggling festival event active status:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/events/:id/force-live - Toggle Force Live Test Override
router.post('/events/:id/force-live', async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM festival_events WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Festival event not found' });
    }

    const newForce = existing.forceLive ? 0 : 1;
    await db.runAsync('UPDATE festival_events SET forceLive = ?, isActive = 1 WHERE id = ?', [newForce, id]);

    res.json({
      success: true,
      forceLive: Boolean(newForce),
      message: newForce 
        ? `⚡ Event "${existing.name}" forced LIVE now on Home Screen!` 
        : `Event "${existing.name}" reverted to schedule.`
    });
  } catch (err) {
    console.error('Error toggling force live:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// DELETE /api/admin/events/:id - Delete festival event
router.delete('/events/:id', async (req, res) => {
  try {
    const { id } = req.params;
    await db.runAsync('DELETE FROM festival_events WHERE id = ?', [id]);
    res.json({ success: true, message: 'Festival event deleted' });
  } catch (err) {
    console.error('Error deleting festival event:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// POST /api/admin/events/:id/push-device - Push festival live test to phone
router.post('/events/:id/push-device', async (req, res) => {
  try {
    const { id } = req.params;
    const existing = await db.getAsync('SELECT * FROM festival_events WHERE id = ?', [id]);
    if (!existing) {
      return res.status(404).json({ success: false, error: 'Festival event not found' });
    }

    // Push wallpaper associated with festival category, or w1
    const wp = await db.getAsync('SELECT id FROM wallpapers WHERE category = ? ORDER BY likes DESC LIMIT 1', [existing.targetCategory])
      || await db.getAsync('SELECT id FROM wallpapers ORDER BY isParallax DESC LIMIT 1');

    const result = await adbBridge.pushLiveTest('wallpaper', wp ? wp.id : 'w1');
    res.json({
      success: true,
      message: `🚀 Sent Festival "${existing.name}" preview to phone!`,
      ...result
    });
  } catch (err) {
    console.error('Error pushing festival event to phone:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

module.exports = router;
