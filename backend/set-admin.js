const { db, hashPassword, generateSalt } = require('./db/database');

async function setAdminCredentials(username, password) {
  if (!username || !password) {
    console.error('❌ Usage: node backend/set-admin.js <username> <password>');
    process.exit(1);
  }

  // Wait briefly for DB connection to initialize
  await new Promise((r) => setTimeout(r, 600));

  try {
    const salt = generateSalt();
    const hash = hashPassword(password, salt);

    const existing = await db.getAsync('SELECT id FROM admins LIMIT 1');
    if (existing) {
      await db.runAsync(
        'UPDATE admins SET username = ?, passwordHash = ?, salt = ? WHERE id = ?',
        [username.trim(), hash, salt, existing.id]
      );
      console.log(`✅ Admin credentials updated successfully!`);
      console.log(`👤 Username: ${username.trim()}`);
      console.log(`🔑 Password: ${password}`);
    } else {
      await db.runAsync(
        'INSERT INTO admins (id, username, passwordHash, salt, role, createdAt) VALUES (?, ?, ?, ?, ?, ?)',
        ['admin_default', username.trim(), hash, salt, 'superadmin', Date.now()]
      );
      console.log(`✅ New Admin created successfully!`);
      console.log(`👤 Username: ${username.trim()}`);
      console.log(`🔑 Password: ${password}`);
    }
    process.exit(0);
  } catch (err) {
    console.error('❌ Error setting admin credentials:', err);
    process.exit(1);
  }
}

const args = process.argv.slice(2);
const newUsername = args[0];
const newPassword = args[1];

setAdminCredentials(newUsername, newPassword);
