const fs = require('fs');
const path = require('path');

const targetDir = path.join(__dirname, '..', 'public', 'uploads', 'aod');
if (!fs.existsSync(targetDir)) {
  fs.mkdirSync(targetDir, { recursive: true });
}

// 1. Cyberpunk Neon HUD 2077
const svgCyberpunk = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 320" width="320" height="320">
  <defs>
    <radialGradient id="cyberGlow" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#00E5FF" stop-opacity="0.3"/>
      <stop offset="100%" stop-color="#000000" stop-opacity="0"/>
    </radialGradient>
    <filter id="neonBlur" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="3" result="blur"/>
      <feMerge>
        <feMergeNode in="blur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
  </defs>
  <rect width="320" height="320" fill="#000000" rx="36"/>
  <circle cx="160" cy="160" r="140" fill="url(#cyberGlow)"/>
  
  <!-- Cyberpunk outer radar rings -->
  <circle cx="160" cy="160" r="130" fill="none" stroke="rgba(0, 229, 255, 0.2)" stroke-width="1.5" stroke-dasharray="6, 8"/>
  <circle cx="160" cy="160" r="115" fill="none" stroke="rgba(112, 0, 255, 0.3)" stroke-width="2"/>
  <path d="M 160 30 A 130 130 0 0 1 290 160" fill="none" stroke="#00E5FF" stroke-width="3" stroke-linecap="round" filter="url(#neonBlur)"/>
  <path d="M 30 160 A 130 130 0 0 1 160 290" fill="none" stroke="#7000FF" stroke-width="3" stroke-linecap="round" filter="url(#neonBlur)"/>

  <!-- Futuristic corner brackets -->
  <path d="M 60 80 L 60 60 L 80 60" fill="none" stroke="#00E5FF" stroke-width="2"/>
  <path d="M 260 80 L 260 60 L 240 60" fill="none" stroke="#00E5FF" stroke-width="2"/>
  <path d="M 60 240 L 60 260 L 80 260" fill="none" stroke="#7000FF" stroke-width="2"/>
  <path d="M 260 240 L 260 260 L 240 260" fill="none" stroke="#7000FF" stroke-width="2"/>

  <!-- Glowing Digital Time -->
  <text x="160" y="150" font-family="-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif" font-size="52" font-weight="900" fill="#FFFFFF" text-anchor="middle" letter-spacing="2" filter="url(#neonBlur)">10:45</text>
  <text x="160" y="180" font-family="monospace" font-size="14" font-weight="800" fill="#00E5FF" text-anchor="middle" letter-spacing="4">SEC : 28 // HUD</text>

  <!-- Telemetry Sub-Widgets -->
  <rect x="90" y="210" width="140" height="24" rx="12" fill="rgba(0, 229, 255, 0.08)" stroke="rgba(0, 229, 255, 0.3)" stroke-width="1"/>
  <text x="160" y="226" font-family="sans-serif" font-size="11" font-weight="700" fill="#00E5FF" text-anchor="middle">⚡ 88%  •  👣 7,240</text>
</svg>`;

// 2. Zenith Minimalist Luxury Analog
const svgZenith = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 320" width="320" height="320">
  <defs>
    <linearGradient id="goldHand" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#E2E8F0"/>
      <stop offset="100%" stop-color="#38BDF8"/>
    </linearGradient>
  </defs>
  <rect width="320" height="320" fill="#000000" rx="36"/>
  
  <!-- Outer Minimalist Dial Ring -->
  <circle cx="160" cy="160" r="130" fill="none" stroke="rgba(255, 255, 255, 0.08)" stroke-width="1.5"/>
  <circle cx="160" cy="160" r="126" fill="none" stroke="rgba(56, 189, 248, 0.25)" stroke-width="1" stroke-dasharray="2, 10"/>

  <!-- 12 Hour Minimalist Tick Marks -->
  <circle cx="160" cy="45" r="3.5" fill="#38BDF8"/>
  <circle cx="275" cy="160" r="3.5" fill="#E2E8F0"/>
  <circle cx="160" cy="275" r="3.5" fill="#E2E8F0"/>
  <circle cx="45" cy="160" r="3.5" fill="#E2E8F0"/>

  <circle cx="217.5" cy="60.4" r="2" fill="rgba(255,255,255,0.4)"/>
  <circle cx="259.6" cy="102.5" r="2" fill="rgba(255,255,255,0.4)"/>
  <circle cx="259.6" cy="217.5" r="2" fill="rgba(255,255,255,0.4)"/>
  <circle cx="217.5" cy="259.6" r="2" fill="rgba(255,255,255,0.4)"/>
  <circle cx="102.5" cy="259.6" r="2" fill="rgba(255,255,255,0.4)"/>
  <circle cx="60.4" cy="217.5" r="2" fill="rgba(255,255,255,0.4)"/>
  <circle cx="60.4" cy="102.5" r="2" fill="rgba(255,255,255,0.4)"/>
  <circle cx="102.5" cy="60.4" r="2" fill="rgba(255,255,255,0.4)"/>

  <!-- Sleek Hands -->
  <line x1="160" y1="160" x2="115" y2="105" stroke="#FFFFFF" stroke-width="4.5" stroke-linecap="round"/>
  <line x1="160" y1="160" x2="225" y2="120" stroke="#38BDF8" stroke-width="3" stroke-linecap="round"/>
  <line x1="160" y1="180" x2="160" y2="65" stroke="#00E5FF" stroke-width="1.2" stroke-linecap="round"/>

  <!-- Center Pin -->
  <circle cx="160" cy="160" r="6" fill="#000000" stroke="#00E5FF" stroke-width="2"/>
  <circle cx="160" cy="160" r="2" fill="#FFFFFF"/>

  <!-- Subdial Date Pill -->
  <rect x="135" y="195" width="50" height="20" rx="6" fill="rgba(255,255,255,0.06)" stroke="rgba(255,255,255,0.12)" stroke-width="1"/>
  <text x="160" y="209" font-family="sans-serif" font-size="10" font-weight="700" fill="#38BDF8" text-anchor="middle">MON 14</text>
</svg>`;

// 3. Matrix Typography Word Clock
const svgMatrix = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 320" width="320" height="320">
  <rect width="320" height="320" fill="#000000" rx="36"/>
  
  <g font-family="monospace" font-size="15" font-weight="900" letter-spacing="7" text-anchor="middle">
    <text x="160" y="80">
      <tspan fill="#22C55E">IT</tspan> <tspan fill="#22C55E">IS</tspan> <tspan fill="rgba(255,255,255,0.15)">HALF</tspan> <tspan fill="rgba(255,255,255,0.15)">A</tspan>
    </text>
    <text x="160" y="115">
      <tspan fill="rgba(255,255,255,0.15)">QUARTER</tspan> <tspan fill="rgba(255,255,255,0.15)">TWENTY</tspan>
    </text>
    <text x="160" y="150">
      <tspan fill="rgba(255,255,255,0.15)">TO</tspan> <tspan fill="#22C55E">PAST</tspan> <tspan fill="rgba(255,255,255,0.15)">SIX</tspan>
    </text>
    <text x="160" y="185">
      <tspan fill="#22C55E">TEN</tspan> <tspan fill="rgba(255,255,255,0.15)">ELEVEN</tspan> <tspan fill="rgba(255,255,255,0.15)">FIVE</tspan>
    </text>
    <text x="160" y="220">
      <tspan fill="rgba(255,255,255,0.15)">IN</tspan> <tspan fill="#22C55E">THE</tspan> <tspan fill="#22C55E">NIGHT</tspan>
    </text>
  </g>

  <line x1="60" y1="255" x2="260" y2="255" stroke="rgba(34, 197, 94, 0.3)" stroke-width="1"/>
  <text x="160" y="278" font-family="monospace" font-size="11" font-weight="700" fill="#22C55E" text-anchor="middle">🔋 92%  |  🌙 24°C  |  14 SEP</text>
</svg>`;

// 4. Neon Cyber Kitsune
const svgKitsune = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 320" width="320" height="320">
  <defs>
    <filter id="pinkGlow" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="4" result="blur"/>
      <feMerge>
        <feMergeNode in="blur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
  </defs>
  <rect width="320" height="320" fill="#000000" rx="36"/>

  <g fill="none" stroke="#FF2A85" stroke-width="2" stroke-linejoin="round" stroke-linecap="round" filter="url(#pinkGlow)">
    <polygon points="160,80 110,40 125,100" stroke="#FF2A85" fill="rgba(255,42,133,0.1)"/>
    <polygon points="160,80 210,40 195,100" stroke="#FF2A85" fill="rgba(255,42,133,0.1)"/>
    <polygon points="160,80 125,100 160,125 195,100" stroke="#FF7170"/>
    <polygon points="125,100 80,140 135,150 160,125" stroke="#FF2A85"/>
    <polygon points="195,100 240,140 185,150 160,125" stroke="#FF2A85"/>
    <polygon points="135,150 160,195 185,150 160,125" stroke="#FF7170" fill="rgba(255,113,112,0.15)"/>
    <line x1="130" y1="125" x2="145" y2="130" stroke="#00E5FF" stroke-width="2.5"/>
    <line x1="190" y1="125" x2="175" y2="130" stroke="#00E5FF" stroke-width="2.5"/>
  </g>

  <text x="160" y="238" font-family="-apple-system, sans-serif" font-size="34" font-weight="900" fill="#FFFFFF" text-anchor="middle" letter-spacing="2" filter="url(#pinkGlow)">09:41</text>
  <text x="160" y="265" font-family="sans-serif" font-size="11" font-weight="700" fill="#FF7170" text-anchor="middle">CYBER KITSUNE  •  84%</text>
</svg>`;

// 5. Gamer HUD Stamina Dial
const svgGamer = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 320" width="320" height="320">
  <defs>
    <linearGradient id="amberFire" x1="0%" y1="0%" x2="100%" y2="0%">
      <stop offset="0%" stop-color="#F59E0B"/>
      <stop offset="100%" stop-color="#EF4444"/>
    </linearGradient>
  </defs>
  <rect width="320" height="320" fill="#000000" rx="36"/>

  <circle cx="160" cy="160" r="120" fill="none" stroke="rgba(255,255,255,0.08)" stroke-width="8"/>
  <path d="M 75 245 A 120 120 0 1 1 245 245" fill="none" stroke="url(#amberFire)" stroke-width="8" stroke-linecap="round"/>

  <line x1="160" y1="20" x2="160" y2="35" stroke="#F59E0B" stroke-width="2"/>
  <line x1="160" y1="285" x2="160" y2="300" stroke="#F59E0B" stroke-width="2"/>
  <line x1="20" y1="160" x2="35" y2="160" stroke="#F59E0B" stroke-width="2"/>
  <line x1="285" y1="160" x2="300" y2="160" stroke="#F59E0B" stroke-width="2"/>

  <rect x="125" y="65" width="70" height="20" rx="4" fill="rgba(245, 158, 11, 0.15)" stroke="#F59E0B" stroke-width="1"/>
  <text x="160" y="79" font-family="monospace" font-size="10" font-weight="900" fill="#F59E0B" text-anchor="middle">LVL 99 // MAX</text>

  <text x="160" y="165" font-family="-apple-system, sans-serif" font-size="54" font-weight="900" fill="#FFFFFF" text-anchor="middle" letter-spacing="1">12:30</text>

  <rect x="75" y="195" width="170" height="8" rx="4" fill="rgba(255,255,255,0.1)"/>
  <rect x="75" y="195" width="140" height="8" rx="4" fill="#EF4444"/>
  <text x="65" y="202" font-family="monospace" font-size="9" font-weight="900" fill="#EF4444" text-anchor="end">HP</text>
  <text x="255" y="202" font-family="monospace" font-size="9" font-weight="900" fill="#EF4444">82%</text>

  <rect x="75" y="215" width="170" height="8" rx="4" fill="rgba(255,255,255,0.1)"/>
  <rect x="75" y="215" width="115" height="8" rx="4" fill="#F59E0B"/>
  <text x="65" y="222" font-family="monospace" font-size="9" font-weight="900" fill="#F59E0B" text-anchor="end">STM</text>
  <text x="255" y="222" font-family="monospace" font-size="9" font-weight="900" fill="#F59E0B">6.8K</text>
</svg>`;

// 6. Celestial VIP Gold Constellation
const svgCelestial = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 320" width="320" height="320">
  <defs>
    <radialGradient id="goldGlow" cx="50%" cy="50%" r="50%">
      <stop offset="0%" stop-color="#FFD700" stop-opacity="0.2"/>
      <stop offset="100%" stop-color="#000000" stop-opacity="0"/>
    </radialGradient>
  </defs>
  <rect width="320" height="320" fill="#000000" rx="36"/>
  <circle cx="160" cy="160" r="140" fill="url(#goldGlow)"/>

  <circle cx="160" cy="160" r="130" fill="none" stroke="#FFD700" stroke-width="1" stroke-dasharray="1, 8"/>
  <circle cx="160" cy="160" r="115" fill="none" stroke="rgba(255, 215, 0, 0.4)" stroke-width="1.5"/>
  <circle cx="160" cy="160" r="100" fill="none" stroke="rgba(255, 215, 0, 0.15)" stroke-width="1"/>

  <circle cx="160" cy="45" r="3" fill="#FFD700"/>
  <circle cx="275" cy="160" r="3" fill="#FFD700"/>
  <circle cx="160" cy="275" r="3" fill="#FFD700"/>
  <circle cx="45" cy="160" r="3" fill="#FFD700"/>
  <circle cx="241" cy="79" r="2.5" fill="#FFE066"/>
  <circle cx="79" cy="241" r="2.5" fill="#FFE066"/>

  <path d="M 160 45 L 241 79 L 275 160 L 160 275" fill="none" stroke="rgba(255, 215, 0, 0.25)" stroke-width="0.8"/>

  <line x1="160" y1="160" x2="160" y2="85" stroke="#FFD700" stroke-width="3.5" stroke-linecap="round"/>
  <line x1="160" y1="160" x2="220" y2="160" stroke="#FFE066" stroke-width="2.5" stroke-linecap="round"/>
  <circle cx="160" cy="160" r="5" fill="#000000" stroke="#FFD700" stroke-width="2"/>
  <circle cx="160" cy="160" r="2" fill="#FFD700"/>

  <text x="160" y="215" font-family="sans-serif" font-size="12" font-weight="900" fill="#FFD700" text-anchor="middle" letter-spacing="3">CELESTIAL</text>
  <text x="160" y="235" font-family="sans-serif" font-size="10" font-weight="600" fill="rgba(255,215,0,0.7)" text-anchor="middle">VIP ROYAL AOD</text>
</svg>`;

const files = [
  { name: 'aod_cyberpunk_2077.svg', content: svgCyberpunk },
  { name: 'aod_zenith_analog.svg', content: svgZenith },
  { name: 'aod_matrix_typography.svg', content: svgMatrix },
  { name: 'aod_neon_kitsune.svg', content: svgKitsune },
  { name: 'aod_gamer_stamina.svg', content: svgGamer },
  { name: 'aod_celestial_void.svg', content: svgCelestial }
];

files.forEach(f => {
  const filePath = path.join(targetDir, f.name);
  fs.writeFileSync(filePath, f.content, 'utf8');
  console.log(`✅ Generated AOD Vector Asset: ${f.name}`);
});

console.log('✨ All 6 AOD vector preview assets generated successfully!');
