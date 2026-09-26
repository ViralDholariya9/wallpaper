require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');
const os = require('os');
const { initSchema } = require('./db/database');
const apiRoutes = require('./routes/api');
const adminRoutes = require('./routes/admin');
const { initBackupScheduler } = require('./services/backupScheduler');

const app = express();
const PORT = process.env.PORT || 3000;

// Enable CORS for all incoming client requests
app.use(cors());

// Middleware for body parsing
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// Static file hosting
app.use('/uploads', express.static(path.join(__dirname, 'public', 'uploads')));
app.use('/admin', express.static(path.join(__dirname, 'public', 'admin')));

// Root redirect to Admin Panel
app.get('/', (req, res) => {
  res.redirect('/admin');
});

// API Routes
app.use('/api', apiRoutes);
app.use('/api/admin', adminRoutes);

// Health & Ping check (ultra-lightweight for cron pingers like cron-job.org)
app.all(['/health', '/ping'], (req, res) => {
  if (req.method === 'HEAD') {
    return res.status(200).set('Content-Length', '0').end();
  }
  res.status(200).set('Content-Type', 'text/plain').send('OK');
});

// Helper to find local IPv4 address
function getLocalIP() {
  const interfaces = os.networkInterfaces();
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        return iface.address;
      }
    }
  }
  return 'localhost';
}

// Start Server
async function startServer() {
  await initSchema();
  initBackupScheduler();
  app.listen(PORT, '0.0.0.0', () => {
    const ip = getLocalIP();
    console.log('========================================================');
    console.log('🚀 ReWall 3D Parallax REST API & Admin Panel is Running!');
    console.log(`🌐 Local URL:     http://localhost:${PORT}/admin`);
    console.log(`📱 LAN / Phone:   http://${ip}:${PORT}/admin`);
    console.log(`📡 API Base:      http://${ip}:${PORT}/api/wallpapers`);
    console.log('========================================================');
  });
}

startServer().catch((err) => {
  console.error('❌ Server startup error:', err);
});

