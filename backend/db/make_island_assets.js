const fs = require('fs');
const path = require('path');

const islandDir = path.join(__dirname, '..', 'public', 'uploads', 'island');
if (!fs.existsSync(islandDir)) {
  fs.mkdirSync(islandDir, { recursive: true });
}

function makeSvg(name, title, bgColor, accentColor, glowColor, subtitle) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 240 140" width="240" height="140">
  <defs>
    <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="5" result="blur" />
      <feMerge>
        <feMergeNode in="blur" />
        <feMergeNode in="SourceGraphic" />
      </feMerge>
    </filter>
  </defs>
  <rect width="240" height="140" rx="16" fill="#0b0d14" />
  
  <!-- Outer Glow -->
  <rect x="25" y="25" width="190" height="56" rx="26" fill="none" stroke="${glowColor}" stroke-width="4" filter="url(#glow)" opacity="0.7"/>
  
  <!-- Capsule Body -->
  <rect x="28" y="28" width="184" height="50" rx="25" fill="${bgColor}" stroke="${accentColor}" stroke-width="1.5" />
  
  <!-- Inner Punch Hole Camera -->
  <circle cx="56" cy="53" r="6.5" fill="#000" stroke="rgba(255,255,255,0.2)" stroke-width="1"/>
  
  <!-- Dynamic Island Content (Music Waveform or Battery) -->
  <rect x="74" y="47" width="4" height="12" rx="2" fill="${accentColor}"/>
  <rect x="81" y="43" width="4" height="20" rx="2" fill="${accentColor}"/>
  <rect x="88" y="49" width="4" height="8" rx="2" fill="${accentColor}"/>
  
  <text x="100" y="58" fill="#ffffff" font-size="11.5" font-weight="bold" font-family="sans-serif">${title}</text>
  <text x="120" y="112" fill="rgba(255,255,255,0.6)" font-size="9.5" font-family="sans-serif" text-anchor="middle">${subtitle}</text>
</svg>`;

  fs.writeFileSync(path.join(islandDir, name), svg, 'utf8');
  console.log(`Generated ${name}`);
}

makeSvg('thumb_apple_obsidian.svg', 'Minimalist', '#000000', '#00E5FF', 'rgba(255,255,255,0.2)', 'APPLE MINIMAL OBSIDIAN');
makeSvg('thumb_cyberpunk_neon.svg', 'Cyber Arc', '#080C16', '#00E5FF', '#00E5FF', 'CYBERPUNK NEON ARC');
makeSvg('thumb_frosted_glass.svg', 'Frosted Aero', '#161B2E', '#00FF88', '#00FF88', 'FROSTED GLASSMORPHIC');
makeSvg('thumb_amoled_void.svg', 'Pure Void', '#000000', '#E0E0E0', 'rgba(255,255,255,0.1)', 'AMOLED ZERO-BATTERY');
makeSvg('thumb_pastel_aurora.svg', 'Pastel Cloud', '#1A1428', '#FF007F', '#FF007F', 'PASTEL AURORA SUNSET');
makeSvg('thumb_golden_vip.svg', 'VIP Sovereign', '#120F08', '#FFD700', '#FFD700', 'GOLDEN VIP SOVEREIGN');
