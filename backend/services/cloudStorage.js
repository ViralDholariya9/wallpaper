const cloudinary = require('cloudinary').v2;
const fs = require('fs');
const path = require('path');
const https = require('https');
const sqlite3 = require('sqlite3').verbose();

const configPath = path.join(__dirname, '..', 'data', 'cloud_config.json');
const syncMetaPath = path.join(__dirname, '..', 'data', 'cloud_sync.json');
const dbPath = path.join(__dirname, '..', 'data', 'rewall.sqlite');

// Helper to load Cloudinary config
function getCloudConfig() {
  let savedConfig = {};
  try {
    if (fs.existsSync(configPath)) {
      savedConfig = JSON.parse(fs.readFileSync(configPath, 'utf8'));
    }
  } catch (err) {
    console.error('Error reading cloud_config.json:', err.message);
  }

  const cloudName = process.env.CLOUDINARY_CLOUD_NAME || savedConfig.cloudName || '';
  const apiKey = process.env.CLOUDINARY_API_KEY || savedConfig.apiKey || '';
  const apiSecret = process.env.CLOUDINARY_API_SECRET || savedConfig.apiSecret || '';
  const cloudinaryUrl = process.env.CLOUDINARY_URL || savedConfig.cloudinaryUrl || '';

  const isEnabled = Boolean((cloudName && apiKey && apiSecret) || cloudinaryUrl);

  if (cloudName && apiKey && apiSecret) {
    cloudinary.config({
      cloud_name: cloudName,
      api_key: apiKey,
      api_secret: apiSecret,
      secure: true
    });
  } else if (cloudinaryUrl) {
    process.env.CLOUDINARY_URL = cloudinaryUrl;
  }

  let lastSyncedAt = null;
  try {
    if (fs.existsSync(syncMetaPath)) {
      const syncData = JSON.parse(fs.readFileSync(syncMetaPath, 'utf8'));
      lastSyncedAt = syncData.lastSyncedAt || null;
    }
  } catch (_) {}

  return {
    isEnabled,
    cloudName,
    apiKey,
    apiSecret: apiSecret ? '••••••••' + apiSecret.slice(-4) : '',
    hasSecret: Boolean(apiSecret),
    cloudinaryUrl: cloudinaryUrl ? 'cloudinary://••••••••' : '',
    lastSyncedAt
  };
}

// Save Cloudinary config
function saveCloudConfig({ cloudName, apiKey, apiSecret, cloudinaryUrl }) {
  let existing = {};
  try {
    if (fs.existsSync(configPath)) {
      existing = JSON.parse(fs.readFileSync(configPath, 'utf8'));
    }
  } catch (_) {}

  const updated = {
    cloudName: cloudName !== undefined ? cloudName.trim() : existing.cloudName || '',
    apiKey: apiKey !== undefined ? apiKey.trim() : existing.apiKey || '',
    apiSecret: (apiSecret && !apiSecret.includes('••••')) ? apiSecret.trim() : existing.apiSecret || '',
    cloudinaryUrl: (cloudinaryUrl && !cloudinaryUrl.includes('••••')) ? cloudinaryUrl.trim() : existing.cloudinaryUrl || '',
    updatedAt: Date.now()
  };

  const dataDir = path.dirname(configPath);
  if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true });
  }

  fs.writeFileSync(configPath, JSON.stringify(updated, null, 2), 'utf8');

  // Re-configure cloudinary singleton
  if (updated.cloudName && updated.apiKey && updated.apiSecret) {
    cloudinary.config({
      cloud_name: updated.cloudName,
      api_key: updated.apiKey,
      api_secret: updated.apiSecret,
      secure: true
    });
  } else if (updated.cloudinaryUrl) {
    process.env.CLOUDINARY_URL = updated.cloudinaryUrl;
  }

  return getCloudConfig();
}

/**
 * Upload a local file to Cloudinary with automatic resource_type detection.
 * Returns { url, thumbUrl, publicId, bytes, format } or null if not configured.
 */
async function uploadToCloud(filePath, folder = 'wallpapers', options = {}) {
  const config = getCloudConfig();
  if (!config.isEnabled) {
    return null;
  }

  try {
    const ext = path.extname(filePath).toLowerCase();
    let resourceType = 'image';
    if (['.mp4', '.webm', '.mov', '.avi'].includes(ext)) {
      resourceType = 'video';
    } else if (['.json', '.svg', '.lottie', '.mp3', '.ogg', '.wav'].includes(ext)) {
      resourceType = 'raw';
    }

    const uploadOptions = {
      folder: `rewall/${folder}`,
      resource_type: resourceType,
      use_filename: true,
      unique_filename: true,
      overwrite: false,
      ...options
    };

    const result = await cloudinary.uploader.upload(filePath, uploadOptions);

    let thumbUrl = result.secure_url;
    if (resourceType === 'image') {
      thumbUrl = cloudinary.url(result.public_id, {
        transformation: [
          { width: 600, height: 900, crop: 'fill', gravity: 'auto' },
          { quality: 'auto', fetch_format: 'webp' }
        ],
        secure: true
      });
    }

    return {
      url: result.secure_url,
      thumbUrl,
      publicId: result.public_id,
      bytes: result.bytes,
      format: result.format || ext.replace('.', '')
    };
  } catch (err) {
    console.error('Cloudinary upload error:', err);
    return null; // Fallback to local url
  }
}

/**
 * Sync the local SQLite database to Cloudinary as a raw resource.
 * This guarantees database records survive any container resets or re-deploys.
 */
async function syncDatabaseToCloud() {
  const config = getCloudConfig();
  if (!config.isEnabled) {
    return { success: false, reason: 'Cloudinary not enabled' };
  }

  if (!fs.existsSync(dbPath)) {
    return { success: false, reason: 'Database file not found' };
  }

  try {
    const result = await cloudinary.uploader.upload(dbPath, {
      folder: 'rewall/db_backups',
      public_id: 'rewall_latest.sqlite',
      resource_type: 'raw',
      overwrite: true,
      invalidate: true
    });

    const syncMeta = {
      lastSyncedAt: Date.now(),
      bytes: result.bytes,
      url: result.secure_url,
      version: result.version
    };

    fs.writeFileSync(syncMetaPath, JSON.stringify(syncMeta, null, 2), 'utf8');
    console.log(`☁️ [Cloud Storage] SQLite database successfully synced to Cloudinary (${result.bytes} bytes)`);
    return { success: true, data: syncMeta };
  } catch (err) {
    console.error('❌ [Cloud Storage] Failed to sync database to Cloudinary:', err.message);
    return { success: false, error: err.message };
  }
}

// Debounced cloud sync
let syncTimer = null;
function triggerCloudDatabaseSync(delayMs = 3000) {
  if (syncTimer) clearTimeout(syncTimer);
  syncTimer = setTimeout(async () => {
    try {
      await syncDatabaseToCloud();
    } catch (err) {
      console.error('Triggered cloud sync failed:', err.message);
    }
  }, delayMs);
}

/**
 * Download database from Cloudinary and restore if valid.
 */
async function autoRestoreDatabaseFromCloud(force = false) {
  const config = getCloudConfig();
  if (!config.isEnabled || !config.cloudName) {
    return { restored: false, reason: 'Cloudinary not configured' };
  }

  const cloudDbUrl = `https://res.cloudinary.com/${config.cloudName}/raw/upload/rewall/db_backups/rewall_latest.sqlite`;

  try {
    console.log(`🔍 [Cloud Storage] Checking Cloudinary for persistent database snapshot...`);
    const buffer = await new Promise((resolve, reject) => {
      const req = https.get(cloudDbUrl, (res) => {
        if (res.statusCode === 404) {
          return resolve(null);
        }
        if (res.statusCode !== 200) {
          return reject(new Error(`HTTP ${res.statusCode} fetching cloud database`));
        }
        const chunks = [];
        res.on('data', chunk => chunks.push(chunk));
        res.on('end', () => resolve(Buffer.concat(chunks)));
      });
      req.on('error', reject);
      req.setTimeout(10000, () => {
        req.destroy();
        reject(new Error('Cloud DB download timeout'));
      });
    });

    if (!buffer || buffer.length < 100) {
      console.log(`ℹ️ [Cloud Storage] No cloud database backup found on Cloudinary yet. Initializing fresh/local.`);
      return { restored: false, reason: 'No cloud database found' };
    }

    // Validate SQLite Header
    const magicHeader = buffer.slice(0, 16).toString('utf8');
    if (!magicHeader.startsWith('SQLite format 3')) {
      console.warn('⚠️ [Cloud Storage] Cloud database header invalid. Skipping auto-restore.');
      return { restored: false, reason: 'Invalid SQLite header' };
    }

    // Count records in downloaded buffer
    const tempCheckPath = path.join(__dirname, '..', 'data', `temp_restore_check_${Date.now()}.sqlite`);
    fs.writeFileSync(tempCheckPath, buffer);

    const tempDb = new sqlite3.Database(tempCheckPath);
    const getCount = (sql) => new Promise((resolve) => {
      tempDb.get(sql, (err, row) => resolve(row ? (row.total || 0) : 0));
    });

    const cloudTotal = await getCount(`
      SELECT (
        (SELECT COUNT(*) FROM wallpapers) +
        (SELECT COUNT(*) FROM duo_wallpapers) +
        (SELECT COUNT(*) FROM charging_animations)
      ) as total
    `);

    await new Promise(r => tempDb.close(r));
    try { fs.unlinkSync(tempCheckPath); } catch (_) {}

    // Count local database records
    const { replaceDatabaseFile, db } = require('../db/database');
    let localTotal = 0;
    try {
      const localRow = await db.getAsync(`
        SELECT (
          (SELECT COUNT(*) FROM wallpapers) +
          (SELECT COUNT(*) FROM duo_wallpapers) +
          (SELECT COUNT(*) FROM charging_animations)
        ) as total
      `);
      localTotal = localRow ? localRow.total : 0;
    } catch (_) {}

    console.log(`📊 [Cloud Storage] Item comparison -> Cloud DB: ${cloudTotal} items, Local DB: ${localTotal} items`);

    // Restore if forced or if cloud has equal/more content
    if (force || cloudTotal >= localTotal) {
      console.log(`📥 [Cloud Storage] Auto-restoring latest SQLite database from Cloudinary...`);
      await replaceDatabaseFile(buffer);
      console.log(`✅ [Cloud Storage] SQLite database successfully restored from Cloudinary! All user uploads preserved.`);
      return { restored: true, cloudTotal, localTotal };
    } else {
      console.log(`ℹ️ [Cloud Storage] Local database has newer/more items (${localTotal} vs ${cloudTotal}). Syncing local to cloud...`);
      triggerCloudDatabaseSync(1000);
      return { restored: false, reason: 'Local database has more items than cloud' };
    }
  } catch (err) {
    console.error('❌ [Cloud Storage] Auto-restore from cloud failed:', err.message);
    return { restored: false, error: err.message };
  }
}

module.exports = {
  getCloudConfig,
  saveCloudConfig,
  uploadToCloud,
  syncDatabaseToCloud,
  triggerCloudDatabaseSync,
  autoRestoreDatabaseFromCloud
};
