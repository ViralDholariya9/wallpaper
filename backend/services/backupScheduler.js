const fs = require('fs');
const path = require('path');
const AdmZip = require('adm-zip');
const crypto = require('crypto');
const { db, dbPath, replaceDatabaseFile, getBackupStats } = require('../db/database');

const backupsDir = path.join(__dirname, '..', 'backups');
const uploadDir = path.join(__dirname, '..', 'public', 'uploads');
const dataDir = path.join(__dirname, '..', 'data');
const configFile = path.join(dataDir, 'backup_config.json');

// Ensure backups directory exists
if (!fs.existsSync(backupsDir)) {
  fs.mkdirSync(backupsDir, { recursive: true });
}

// Default Configuration
const DEFAULT_CONFIG = {
  enabled: true,
  frequency: 'daily', // 'hourly', 'every_6h', 'every_12h', 'daily', 'weekly'
  retentionMaxSnapshots: 14, // Keep last 14 snapshots
  lastAutoBackupAt: null,
  nextAutoBackupAt: null
};

function getAutoBackupConfig() {
  try {
    if (fs.existsSync(configFile)) {
      const content = fs.readFileSync(configFile, 'utf8');
      return { ...DEFAULT_CONFIG, ...JSON.parse(content) };
    }
  } catch (err) {
    console.error('Error reading backup_config.json:', err);
  }
  return { ...DEFAULT_CONFIG };
}

function saveAutoBackupConfig(newConfig) {
  try {
    if (!fs.existsSync(dataDir)) {
      fs.mkdirSync(dataDir, { recursive: true });
    }
    const merged = { ...getAutoBackupConfig(), ...newConfig };
    fs.writeFileSync(configFile, JSON.stringify(merged, null, 2), 'utf8');
    return merged;
  } catch (err) {
    console.error('Error saving backup_config.json:', err);
    throw err;
  }
}

// Helper: Compute SHA256 of buffer
function computeSha256(buffer) {
  return crypto.createHash('sha256').update(buffer).digest('hex');
}

// Helper: Calculate next scheduled backup timestamp
function calculateNextBackupTime(frequency, lastTime = Date.now()) {
  const intervals = {
    hourly: 60 * 60 * 1000,
    every_6h: 6 * 60 * 60 * 1000,
    every_12h: 12 * 60 * 60 * 1000,
    daily: 24 * 60 * 60 * 1000,
    weekly: 7 * 24 * 60 * 60 * 1000
  };
  const intervalMs = intervals[frequency] || intervals.daily;
  return lastTime + intervalMs;
}

// Collect comprehensive system record counts for manifest
async function getFullSystemMetrics() {
  const counts = {
    wallpapers: 0,
    categories: 0,
    ringtones: 0,
    banners: 0,
    charging: 0,
    edgeLighting: 0,
    dynamicIsland: 0,
    aod: 0,
    callScreen: 0,
    duo: 0,
    touch: 0,
    fingerprint: 0,
    festivalEvents: 0
  };

  try {
    const q = async (sql) => {
      try {
        const row = await db.getAsync(sql);
        return row ? (row.count || 0) : 0;
      } catch (_) {
        return 0;
      }
    };

    counts.wallpapers = await q('SELECT COUNT(*) as count FROM wallpapers');
    counts.categories = await q('SELECT COUNT(*) as count FROM categories');
    counts.ringtones = await q('SELECT COUNT(*) as count FROM ringtones');
    counts.banners = await q('SELECT COUNT(*) as count FROM hero_banners');
    counts.charging = await q('SELECT COUNT(*) as count FROM charging_animations');
    counts.edgeLighting = await q('SELECT COUNT(*) as count FROM edge_lighting_presets');
    counts.dynamicIsland = await q('SELECT COUNT(*) as count FROM dynamic_island_themes');
    counts.aod = await q('SELECT COUNT(*) as count FROM aod_clocks');
    counts.callScreen = await q('SELECT COUNT(*) as count FROM call_screen_themes');
    counts.duo = await q('SELECT COUNT(*) as count FROM duo_wallpapers');
    counts.touch = await q('SELECT COUNT(*) as count FROM touch_presets');
    counts.fingerprint = await q('SELECT COUNT(*) as count FROM fingerprint_presets');
    counts.festivalEvents = await q('SELECT COUNT(*) as count FROM festival_events');
  } catch (err) {
    console.warn('Error reading system table metrics:', err);
  }

  return counts;
}

/**
 * 1-Click Snapshot Creator: Creates a self-contained ZIP archive containing
 * database, uploaded assets, and metadata manifest.
 */
async function createLocalSnapshot(options = {}) {
  const type = options.type || 'manual'; // 'auto' | 'manual'
  const label = options.label || (type === 'auto' ? 'Automated Daily Cloud Backup' : 'Manual System Backup');
  const note = options.note || '';

  if (!fs.existsSync(dbPath)) {
    throw new Error('Database file rewall.sqlite not found');
  }

  const startTime = Date.now();
  const dbBuffer = fs.readFileSync(dbPath);
  const dbHash = computeSha256(dbBuffer);
  const metrics = await getFullSystemMetrics();

  const zip = new AdmZip();

  // 1. Add SQLite database
  zip.addLocalFile(dbPath, '', 'rewall.sqlite');

  // 2. Add uploaded media files recursively/safely
  let uploadsCount = 0;
  let uploadsTotalBytes = 0;

  function addFolderRecursively(currentDir, relativeZipPath) {
    if (!fs.existsSync(currentDir)) return;
    const items = fs.readdirSync(currentDir);
    for (const item of items) {
      const itemPath = path.join(currentDir, item);
      const stat = fs.statSync(itemPath);
      if (stat.isDirectory()) {
        addFolderRecursively(itemPath, path.join(relativeZipPath, item));
      } else if (stat.isFile()) {
        zip.addLocalFile(itemPath, relativeZipPath);
        uploadsCount++;
        uploadsTotalBytes += stat.size;
      }
    }
  }

  if (fs.existsSync(uploadDir)) {
    addFolderRecursively(uploadDir, 'uploads');
  }

  // 3. Build detailed Manifest
  const manifest = {
    product: 'ReWall Studio - 3D Parallax Control Center',
    serverVersion: '1.4.0',
    snapshotType: type,
    label,
    note,
    createdAt: startTime,
    createdAtFormatted: new Date(startTime).toISOString(),
    sha256Database: dbHash,
    stats: {
      databaseSizeBytes: dbBuffer.length,
      uploadsCount,
      uploadsTotalBytes,
      totalEntries: Object.values(metrics).reduce((a, b) => a + b, 0),
      metrics
    }
  };

  zip.addFile('manifest.json', Buffer.from(JSON.stringify(manifest, null, 2), 'utf8'));

  const timestampStr = new Date(startTime).toISOString().replace(/[:.]/g, '-').slice(0, 19);
  const filename = `rewall_snapshot_${type}_${timestampStr}.zip`;
  const targetFilePath = path.join(backupsDir, filename);

  const zipBuffer = zip.toBuffer();
  fs.writeFileSync(targetFilePath, zipBuffer);

  const durationMs = Date.now() - startTime;

  // Prune older snapshots according to retention settings
  const config = getAutoBackupConfig();
  await pruneOldSnapshots(config.retentionMaxSnapshots || 14);

  // Update last auto backup timestamp if auto
  if (type === 'auto') {
    const nextTime = calculateNextBackupTime(config.frequency);
    saveAutoBackupConfig({
      lastAutoBackupAt: startTime,
      nextAutoBackupAt: nextTime
    });
  }

  return {
    success: true,
    filename,
    filePath: targetFilePath,
    sizeBytes: zipBuffer.length,
    durationMs,
    manifest
  };
}

/**
 * List all saved server snapshots with parsed manifests and file sizes.
 */
function listLocalSnapshots() {
  if (!fs.existsSync(backupsDir)) {
    return [];
  }

  const files = fs.readdirSync(backupsDir);
  const snapshots = [];

  for (const file of files) {
    const filePath = path.join(backupsDir, file);
    try {
      const stat = fs.statSync(filePath);
      if (!stat.isFile()) continue;

      let manifest = null;
      let isZip = file.endsWith('.zip');
      let isSqlite = file.endsWith('.sqlite') || file.endsWith('.db');

      if (isZip) {
        try {
          const zip = new AdmZip(filePath);
          const manifestEntry = zip.getEntry('manifest.json');
          if (manifestEntry) {
            manifest = JSON.parse(manifestEntry.getData().toString('utf8'));
          }
        } catch (_) {}
      }

      const isAuto = file.includes('_auto_') || (manifest && manifest.snapshotType === 'auto');

      snapshots.push({
        filename: file,
        sizeBytes: stat.size,
        createdAt: manifest ? manifest.createdAt : stat.mtimeMs,
        createdAtFormatted: manifest ? manifest.createdAtFormatted : new Date(stat.mtimeMs).toISOString(),
        type: isAuto ? 'auto' : 'manual',
        label: manifest ? manifest.label : (isAuto ? 'Daily Auto-Backup' : 'System Snapshot'),
        note: manifest ? manifest.note : '',
        format: isZip ? 'zip' : (isSqlite ? 'sqlite' : 'unknown'),
        manifestStats: manifest ? manifest.stats : null
      });
    } catch (err) {
      console.warn(`Error reading snapshot file ${file}:`, err);
    }
  }

  // Sort newest first
  snapshots.sort((a, b) => b.createdAt - a.createdAt);
  return snapshots;
}

/**
 * 1-Second Instant Restore from a Local Snapshot:
 * Atomically extracts SQLite DB & uploads and swaps into current live system.
 */
async function restoreFromLocalSnapshot(filename) {
  const safeFilename = path.basename(filename);
  const filePath = path.join(backupsDir, safeFilename);

  if (!fs.existsSync(filePath)) {
    throw new Error(`Snapshot file "${safeFilename}" does not exist`);
  }

  const startTime = Date.now();
  let sqliteBuffer = null;
  let extractedUploadsCount = 0;
  let manifest = null;

  if (safeFilename.endsWith('.sqlite') || safeFilename.endsWith('.db')) {
    sqliteBuffer = fs.readFileSync(filePath);
  } else if (safeFilename.endsWith('.zip')) {
    const zip = new AdmZip(filePath);
    const entries = zip.getEntries();

    // 1. Read manifest if present
    const manifestEntry = entries.find(e => e.entryName === 'manifest.json');
    if (manifestEntry) {
      try {
        manifest = JSON.parse(manifestEntry.getData().toString('utf8'));
      } catch (_) {}
    }

    // 2. Locate sqlite database
    const dbEntry = entries.find(e => 
      e.entryName === 'rewall.sqlite' ||
      e.name === 'rewall.sqlite' ||
      e.name.endsWith('.sqlite') ||
      e.name.endsWith('.db')
    );

    if (!dbEntry) {
      throw new Error('Invalid snapshot archive: rewall.sqlite not found inside ZIP');
    }

    sqliteBuffer = dbEntry.getData();

    // 3. Extract uploads
    entries.forEach(entry => {
      if ((entry.entryName.startsWith('uploads/') || entry.entryName.startsWith('uploads\\')) && !entry.isDirectory) {
        const relPath = entry.entryName.replace(/^uploads[\\/]/, '');
        const targetPath = path.join(uploadDir, relPath);
        const parentDir = path.dirname(targetPath);
        if (!fs.existsSync(parentDir)) {
          fs.mkdirSync(parentDir, { recursive: true });
        }
        fs.writeFileSync(targetPath, entry.getData());
        extractedUploadsCount++;
      }
    });
  } else {
    throw new Error('Unsupported snapshot file format');
  }

  // Verify SQLite Magic Header
  const magicHeader = sqliteBuffer.slice(0, 16).toString('utf8');
  if (!magicHeader.startsWith('SQLite format 3')) {
    throw new Error('Corrupted SQLite header: Not a valid SQLite 3 database');
  }

  // Perform atomic hot-swap
  await replaceDatabaseFile(sqliteBuffer);

  const durationMs = Date.now() - startTime;
  const updatedStats = await getBackupStats();

  return {
    success: true,
    message: 'System restored in 1 second!',
    durationMs,
    extractedUploadsCount,
    updatedStats,
    manifest
  };
}

/**
 * Delete a local snapshot
 */
function deleteLocalSnapshot(filename) {
  const safeFilename = path.basename(filename);
  const filePath = path.join(backupsDir, safeFilename);

  if (!fs.existsSync(filePath)) {
    throw new Error(`Snapshot "${safeFilename}" not found`);
  }

  fs.unlinkSync(filePath);
  return { success: true, filename: safeFilename };
}

/**
 * Prune old snapshots exceeding retention limit
 */
async function pruneOldSnapshots(maxSnapshots = 14) {
  try {
    const snapshots = listLocalSnapshots();
    if (snapshots.length > maxSnapshots) {
      const toDelete = snapshots.slice(maxSnapshots);
      for (const snap of toDelete) {
        try {
          const p = path.join(backupsDir, snap.filename);
          if (fs.existsSync(p)) fs.unlinkSync(p);
          console.log(`🧹 Auto-pruned old backup snapshot: ${snap.filename}`);
        } catch (_) {}
      }
    }
  } catch (err) {
    console.warn('Error during snapshot pruning:', err);
  }
}

/**
 * Initialize Automated Periodic Daily Scheduler (Timer / Cron equivalent)
 */
let schedulerTimer = null;

function initBackupScheduler() {
  if (schedulerTimer) clearInterval(schedulerTimer);

  console.log('📦 ReWall Auto Cloud Backup Engine initialized');

  // Check every 5 minutes if a backup is due
  schedulerTimer = setInterval(async () => {
    try {
      const config = getAutoBackupConfig();
      if (!config.enabled) return;

      const now = Date.now();
      const lastBackup = config.lastAutoBackupAt || 0;
      const nextDue = config.nextAutoBackupAt || calculateNextBackupTime(config.frequency, lastBackup);

      if (now >= nextDue || lastBackup === 0) {
        console.log('⏰ Auto Backup trigger time arrived. Creating daily snapshot...');
        const result = await createLocalSnapshot({
          type: 'auto',
          label: `Daily Auto-Backup (${new Date().toLocaleDateString()})`
        });
        console.log(`✅ Daily Auto-Backup created successfully: ${result.filename} (${(result.sizeBytes / 1024 / 1024).toFixed(2)} MB)`);
      }
    } catch (err) {
      console.error('Error in auto backup scheduler tick:', err);
    }
  }, 5 * 60 * 1000); // Check every 5 minutes

  // Perform an immediate initial check / snapshot creation if no backup exists yet
  setTimeout(async () => {
    try {
      const existing = listLocalSnapshots();
      if (existing.length === 0) {
        console.log('📦 Initializing first baseline auto-backup snapshot...');
        await createLocalSnapshot({
          type: 'auto',
          label: 'Initial Baseline System Backup'
        });
      }
    } catch (err) {
      console.warn('Initial baseline snapshot warning:', err.message);
    }
  }, 3000);
}

module.exports = {
  backupsDir,
  getAutoBackupConfig,
  saveAutoBackupConfig,
  createLocalSnapshot,
  listLocalSnapshots,
  restoreFromLocalSnapshot,
  deleteLocalSnapshot,
  pruneOldSnapshots,
  initBackupScheduler
};
