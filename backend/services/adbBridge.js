const { exec } = require('child_process');
const util = require('util');
const path = require('path');
const fs = require('fs');

const execPromise = util.promisify(exec);

// Candidate paths for adb.exe on Windows
const CANDIDATE_ADB_PATHS = [
  'adb',
  'D:\\AndroidSDK\\platform-tools\\adb.exe',
  'C:\\Android\\platform-tools\\adb.exe',
  'C:\\Users\\' + (process.env.USERNAME || 'VP') + '\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe'
];

let resolvedAdbPath = null;

async function getAdbPath() {
  if (resolvedAdbPath) return resolvedAdbPath;

  for (const p of CANDIDATE_ADB_PATHS) {
    try {
      if (p.includes('\\') && !fs.existsSync(p)) continue;
      const { stdout } = await execPromise(`"${p}" version`, { timeout: 3000 });
      if (stdout && stdout.includes('Android Debug Bridge')) {
        resolvedAdbPath = p;
        return resolvedAdbPath;
      }
    } catch (_) {
      // Try next
    }
  }

  resolvedAdbPath = 'adb';
  return resolvedAdbPath;
}

/**
 * Lists all connected ADB devices with model and status.
 */
async function getConnectedDevices() {
  const adb = await getAdbPath();
  try {
    const { stdout } = await execPromise(`"${adb}" devices -l`, { timeout: 4000 });
    const lines = stdout.split(/\r?\n/).map(l => l.trim()).filter(Boolean);
    const devices = [];

    for (const line of lines) {
      if (line.startsWith('List of devices') || line.startsWith('* daemon')) continue;
      
      const parts = line.split(/\s+/);
      if (parts.length >= 2) {
        const id = parts[0];
        const status = parts[1];
        
        let model = '';
        let product = '';
        let deviceName = '';
        
        for (let i = 2; i < parts.length; i++) {
          if (parts[i].startsWith('model:')) model = parts[i].replace('model:', '');
          if (parts[i].startsWith('product:')) product = parts[i].replace('product:', '');
          if (parts[i].startsWith('device:')) deviceName = parts[i].replace('device:', '');
        }

        let displayName = model || product || id;
        if (displayName === 'I2212' || product === 'I2212') {
          displayName = 'vivo I2212';
        }

        devices.push({
          id,
          status,
          model: model || 'Unknown Model',
          product,
          deviceName,
          displayName,
          isTargetPhone: id === '10BD1303DM00013' || model.includes('I2212') || product.includes('I2212')
        });
      }
    }

    return {
      connected: devices.length > 0,
      devices,
      primaryDevice: devices.find(d => d.isTargetPhone) || devices[0] || null
    };
  } catch (err) {
    console.warn('[ADB Bridge] Error listing devices:', err.message);
    return {
      connected: false,
      devices: [],
      primaryDevice: null,
      error: err.message
    };
  }
}

/**
 * Pushes live test intent directly to the connected phone.
 * @param {string} type - 'wallpaper' | 'call_screen' | 'aod'
 * @param {string} id - Item ID
 * @param {string} [targetDeviceId] - Optional specific serial ID
 */
async function pushLiveTest(type, id, targetDeviceId = null) {
  const adb = await getAdbPath();
  const devInfo = await getConnectedDevices();

  if (!devInfo.connected || devInfo.devices.length === 0) {
    throw new Error('No ADB device connected. Please connect vivo I2212 via USB with USB Debugging enabled.');
  }

  const device = targetDeviceId 
    ? devInfo.devices.find(d => d.id === targetDeviceId) || devInfo.devices[0]
    : devInfo.primaryDevice;

  if (!device) {
    throw new Error('Target device not found among connected devices.');
  }

  const serial = device.id;
  const pkg = 'com.omvagmine.wallpaper';

  // Step 1: Wake up device display and dismiss keyguard
  try {
    await execPromise(`"${adb}" -s ${serial} shell input keyevent KEYCODE_WAKEUP`, { timeout: 3000 });
    await execPromise(`"${adb}" -s ${serial} shell wm dismiss-keyguard`, { timeout: 3000 });
  } catch (e) {
    console.warn('[ADB Bridge] Wakeup warning (non-fatal):', e.message);
  }

  // Step 2: Formulate the AM start command based on type
  let amCmd = '';
  let friendlyType = '';

  if (type === 'wallpaper') {
    friendlyType = '3D Wallpaper';
    amCmd = `"${adb}" -s ${serial} shell am start -n ${pkg}/com.parallax.wallpaper.MainActivity -a com.parallax.wallpaper.ACTION_LIVE_TEST --es preview_type wallpaper --es preview_id "${id}"`;
  } else if (type === 'call_screen' || type === 'callscreen') {
    friendlyType = 'Call Screen Theme';
    amCmd = `"${adb}" -s ${serial} shell am start -n ${pkg}/com.parallax.wallpaper.callscreen.IncomingCallActivity -a com.parallax.wallpaper.ACTION_LIVE_TEST --es preview_type call_screen --es theme_id "${id}"`;
  } else if (type === 'aod') {
    friendlyType = 'AOD Clock Face';
    amCmd = `"${adb}" -s ${serial} shell am start -n ${pkg}/com.parallax.wallpaper.aod.AODActivity -a com.parallax.wallpaper.ACTION_LIVE_TEST --es preview_type aod --es clock_id "${id}"`;
  } else {
    throw new Error(`Unsupported live test preview type: ${type}`);
  }

  console.log(`[ADB Bridge] Dispatching live test: ${friendlyType} (ID: ${id}) to ${device.displayName} (${serial})`);
  const { stdout, stderr } = await execPromise(amCmd, { timeout: 6000 });

  return {
    success: true,
    type,
    id,
    friendlyType,
    device: {
      id: serial,
      displayName: device.displayName,
      model: device.model
    },
    stdout: stdout.trim(),
    message: `🚀 Sent ${friendlyType} "${id}" to ${device.displayName}!`
  };
}

module.exports = {
  getAdbPath,
  getConnectedDevices,
  pushLiveTest
};
