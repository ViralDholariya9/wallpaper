const cloudinary = require('cloudinary').v2;
const fs = require('fs');
const path = require('path');

const configPath = path.join(__dirname, '..', 'data', 'cloud_config.json');

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

  if (cloudinaryUrl) {
    cloudinary.config({ cloudinary_url: cloudinaryUrl });
  } else if (isEnabled) {
    cloudinary.config({
      cloud_name: cloudName,
      api_key: apiKey,
      api_secret: apiSecret,
      secure: true
    });
  }

  return {
    isEnabled,
    cloudName,
    apiKey,
    apiSecret: apiSecret ? '••••••••' + apiSecret.slice(-4) : '',
    hasSecret: Boolean(apiSecret),
    cloudinaryUrl: cloudinaryUrl ? 'cloudinary://••••••••' : ''
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
  if (updated.cloudinaryUrl) {
    cloudinary.config({ cloudinary_url: updated.cloudinaryUrl });
  } else if (updated.cloudName && updated.apiKey && updated.apiSecret) {
    cloudinary.config({
      cloud_name: updated.cloudName,
      api_key: updated.apiKey,
      api_secret: updated.apiSecret,
      secure: true
    });
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

module.exports = {
  getCloudConfig,
  saveCloudConfig,
  uploadToCloud
};
