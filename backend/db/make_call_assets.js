const fs = require('fs');
const path = require('path');

const outDir = path.join(__dirname, '..', 'public', 'uploads', 'callscreen');
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

// Clean 3D Vector Art Wallpapers for Call Screen Themes (NO hardcoded text or buttons)
const themes = [
  {
    id: 'call_cyber_matrix_2077',
    title: '⚡ Cyberpunk Matrix 2077',
    accent: '#00E5FF',
    glow: '#7000FF',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="cyberBg" cx="50%" cy="38%" r="75%">
          <stop offset="0%" stop-color="#190d38"/>
          <stop offset="45%" stop-color="#090317"/>
          <stop offset="100%" stop-color="#020005"/>
        </radialGradient>
        <linearGradient id="cyberGrid" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#00E5FF" stop-opacity="0.35"/>
          <stop offset="100%" stop-color="#7000FF" stop-opacity="0.08"/>
        </linearGradient>
        <linearGradient id="cyberBeam" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#00E5FF" stop-opacity="0.8"/>
          <stop offset="50%" stop-color="#7000FF" stop-opacity="0.3"/>
          <stop offset="100%" stop-color="#000" stop-opacity="0"/>
        </linearGradient>
      </defs>
      <!-- Background -->
      <rect width="360" height="640" fill="url(#cyberBg)"/>
      <!-- Perspective Cyber Grid -->
      <g stroke="url(#cyberGrid)" stroke-width="1">
        <line x1="0" y1="120" x2="360" y2="120"/>
        <line x1="0" y1="240" x2="360" y2="240"/>
        <line x1="0" y1="360" x2="360" y2="360"/>
        <line x1="0" y1="480" x2="360" y2="480"/>
        <line x1="60" y1="0" x2="60" y2="640"/>
        <line x1="120" y1="0" x2="120" y2="640"/>
        <line x1="180" y1="0" x2="180" y2="640"/>
        <line x1="240" y1="0" x2="240" y2="640"/>
        <line x1="300" y1="0" x2="300" y2="640"/>
      </g>
      <!-- Holographic Hex Core Reticle -->
      <circle cx="180" cy="200" r="115" fill="none" stroke="#7000FF" stroke-width="1.5" stroke-dasharray="8 6" opacity="0.45"/>
      <circle cx="180" cy="200" r="95" fill="none" stroke="#00E5FF" stroke-width="2" stroke-dasharray="16 8" opacity="0.6"/>
      <circle cx="180" cy="200" r="75" fill="none" stroke="#00E5FF" stroke-width="1" opacity="0.3"/>
      <!-- Hexagons -->
      <polygon points="180,145 228,172 228,228 180,255 132,228 132,172" fill="none" stroke="#00E5FF" stroke-width="2" opacity="0.5"/>
      <polygon points="180,160 215,180 215,220 180,240 145,220 145,180" fill="#0c0720" stroke="#7000FF" stroke-width="2.5" opacity="0.7"/>
      <!-- Cyber Circuit Tech Marks -->
      <path d="M40 200 L110 200 L125 185" fill="none" stroke="#00E5FF" stroke-width="1.5" opacity="0.5"/>
      <path d="M320 200 L250 200 L235 215" fill="none" stroke="#7000FF" stroke-width="1.5" opacity="0.5"/>
      <circle cx="40" cy="200" r="3" fill="#00E5FF"/>
      <circle cx="320" cy="200" r="3" fill="#7000FF"/>
      <!-- Matrix Data Beams in lower half -->
      <rect x="75" y="420" width="4" height="90" fill="url(#cyberBeam)" opacity="0.7" rx="2"/>
      <rect x="145" y="380" width="3" height="120" fill="url(#cyberBeam)" opacity="0.5" rx="1.5"/>
      <rect x="215" y="440" width="3" height="80" fill="url(#cyberBeam)" opacity="0.6" rx="1.5"/>
      <rect x="280" y="400" width="4" height="110" fill="url(#cyberBeam)" opacity="0.75" rx="2"/>
    </svg>`
  },
  {
    id: 'call_royal_gold_wave',
    title: '✨ Royal Gold Silk Wave',
    accent: '#FFD700',
    glow: '#FFA000',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <linearGradient id="goldBg" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#241b0b"/>
          <stop offset="45%" stop-color="#0c0803"/>
          <stop offset="100%" stop-color="#140f06"/>
        </linearGradient>
        <linearGradient id="goldRibbon1" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#FFF3B0" stop-opacity="0.9"/>
          <stop offset="40%" stop-color="#FFD700" stop-opacity="0.85"/>
          <stop offset="80%" stop-color="#FF9800" stop-opacity="0.6"/>
          <stop offset="100%" stop-color="#795548" stop-opacity="0.2"/>
        </linearGradient>
        <linearGradient id="goldRibbon2" x1="1" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#FFE082" stop-opacity="0.7"/>
          <stop offset="50%" stop-color="#FFB300" stop-opacity="0.8"/>
          <stop offset="100%" stop-color="#3E2723" stop-opacity="0.15"/>
        </linearGradient>
      </defs>
      <rect width="360" height="640" fill="url(#goldBg)"/>
      <!-- 3D Silk Ribbons -->
      <path d="M-60 180 Q100 40 220 180 T440 140" fill="none" stroke="url(#goldRibbon1)" stroke-width="36" opacity="0.35"/>
      <path d="M-40 220 Q120 80 240 220 T460 180" fill="none" stroke="url(#goldRibbon1)" stroke-width="18" opacity="0.5"/>
      <path d="M-80 380 Q100 240 250 420 T460 350" fill="none" stroke="url(#goldRibbon2)" stroke-width="42" opacity="0.28"/>
      <path d="M-50 410 Q120 270 270 440 T480 380" fill="none" stroke="url(#goldRibbon2)" stroke-width="16" opacity="0.45"/>
      <!-- Royal Halo Rings -->
      <circle cx="180" cy="200" r="105" fill="none" stroke="#FFD700" stroke-width="1.5" stroke-dasharray="6 8" opacity="0.35"/>
      <circle cx="180" cy="200" r="82" fill="none" stroke="#FFD700" stroke-width="2" opacity="0.4"/>
      <!-- Golden Stardust Particles -->
      <circle cx="60" cy="110" r="2.5" fill="#FFE082" opacity="0.8"/>
      <circle cx="110" cy="80" r="1.8" fill="#FFF" opacity="0.9"/>
      <circle cx="280" cy="120" r="2.2" fill="#FFE082" opacity="0.85"/>
      <circle cx="320" cy="170" r="3" fill="#FFD700" opacity="0.9"/>
      <circle cx="70" cy="320" r="2.5" fill="#FFD700" opacity="0.75"/>
      <circle cx="290" cy="480" r="3.2" fill="#FFE082" opacity="0.85"/>
      <circle cx="160" cy="530" r="2" fill="#FFF" opacity="0.9"/>
    </svg>`
  },
  {
    id: 'call_galaxy_supernova',
    title: '🌌 Deep Galaxy Supernova',
    accent: '#B388FF',
    glow: '#7C4DFF',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="spaceBg" cx="50%" cy="36%" r="78%">
          <stop offset="0%" stop-color="#340a58"/>
          <stop offset="45%" stop-color="#120324"/>
          <stop offset="100%" stop-color="#020008"/>
        </radialGradient>
        <radialGradient id="supernovaCore" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stop-color="#FFFFFF" stop-opacity="1"/>
          <stop offset="25%" stop-color="#B388FF" stop-opacity="0.8"/>
          <stop offset="60%" stop-color="#7C4DFF" stop-opacity="0.3"/>
          <stop offset="100%" stop-color="#000" stop-opacity="0"/>
        </radialGradient>
      </defs>
      <rect width="360" height="640" fill="url(#spaceBg)"/>
      <!-- Supernova Core Glow -->
      <circle cx="180" cy="200" r="130" fill="url(#supernovaCore)"/>
      <!-- Orbital Dust Rings -->
      <ellipse cx="180" cy="200" rx="140" ry="60" fill="none" stroke="#B388FF" stroke-width="1.8" stroke-dasharray="10 8" opacity="0.45" transform="rotate(-25 180 200)"/>
      <ellipse cx="180" cy="200" rx="115" ry="45" fill="none" stroke="#00E5FF" stroke-width="1.5" stroke-dasharray="6 6" opacity="0.55" transform="rotate(-25 180 200)"/>
      <!-- Cosmic Starfield -->
      <circle cx="35" cy="70" r="1.8" fill="#FFF" opacity="0.9"/>
      <circle cx="120" cy="45" r="1.3" fill="#B388FF" opacity="0.8"/>
      <circle cx="290" cy="95" r="2.2" fill="#FFF" opacity="0.95"/>
      <circle cx="325" cy="140" r="1.5" fill="#80D8FF" opacity="0.85"/>
      <circle cx="50" cy="280" r="2.5" fill="#B388FF" opacity="0.75"/>
      <circle cx="85" cy="460" r="1.7" fill="#FFF" opacity="0.85"/>
      <circle cx="280" cy="370" r="2.8" fill="#80D8FF" opacity="0.9"/>
      <circle cx="310" cy="510" r="1.8" fill="#FFF" opacity="0.8"/>
      <!-- Diamond Star Glints -->
      <path d="M70 140 L73 148 L81 151 L73 154 L70 162 L67 154 L59 151 L67 148 Z" fill="#FFF" opacity="0.85"/>
      <path d="M260 260 L262 266 L268 268 L262 270 L260 276 L258 270 L252 268 L258 266 Z" fill="#00E5FF" opacity="0.8"/>
    </svg>`
  },
  {
    id: 'call_anime_thunder_god',
    title: '⚡ Anime Thunder God',
    accent: '#FF1744',
    glow: '#FF5252',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="animeBg" cx="50%" cy="38%" r="72%">
          <stop offset="0%" stop-color="#4a040e"/>
          <stop offset="50%" stop-color="#190005"/>
          <stop offset="100%" stop-color="#050001"/>
        </radialGradient>
      </defs>
      <rect width="360" height="640" fill="url(#animeBg)"/>
      <!-- Dynamic Electric Lightning Bolts -->
      <path d="M110 20 L160 120 L140 140 L210 250 L180 270 L240 380" fill="none" stroke="#FF5252" stroke-width="3" opacity="0.6"/>
      <path d="M270 50 L220 150 L240 180 L180 300 L200 320 L150 460" fill="none" stroke="#FFD700" stroke-width="2.5" opacity="0.65"/>
      <path d="M50 180 L90 240 L80 260 L140 340" fill="none" stroke="#FFF" stroke-width="1.8" opacity="0.75"/>
      <path d="M310 220 L270 280 L280 300 L220 390" fill="none" stroke="#FF1744" stroke-width="2" opacity="0.5"/>
      <!-- Shockwave Arc Rings -->
      <circle cx="180" cy="200" r="105" fill="none" stroke="#FF1744" stroke-width="2" opacity="0.45"/>
      <circle cx="180" cy="200" r="82" fill="none" stroke="#FFD700" stroke-width="1.8" stroke-dasharray="12 6" opacity="0.6"/>
      <circle cx="180" cy="200" r="60" fill="#200006" stroke="#FF5252" stroke-width="2.5" opacity="0.8"/>
      <!-- Central Lightning Crest -->
      <polygon points="186,170 168,202 182,202 174,232 198,194 184,194" fill="#FFD700" stroke="#FFF" stroke-width="1"/>
      <!-- Particle sparks -->
      <circle cx="140" cy="180" r="2" fill="#FFD700"/>
      <circle cx="220" cy="220" r="2.5" fill="#FFF"/>
      <circle cx="190" cy="150" r="1.5" fill="#FF5252"/>
    </svg>`
  },
  {
    id: 'call_aurora_borealis',
    title: '🌈 Mystic Northern Lights',
    accent: '#00E676',
    glow: '#1DE9B6',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <linearGradient id="auroraBg" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#021f1d"/>
          <stop offset="45%" stop-color="#041219"/>
          <stop offset="100%" stop-color="#010609"/>
        </linearGradient>
        <linearGradient id="auroraCurtain1" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0%" stop-color="#00E676" stop-opacity="0.25"/>
          <stop offset="45%" stop-color="#1DE9B6" stop-opacity="0.75"/>
          <stop offset="75%" stop-color="#00B0FF" stop-opacity="0.5"/>
          <stop offset="100%" stop-color="#7C4DFF" stop-opacity="0.1"/>
        </linearGradient>
        <linearGradient id="auroraCurtain2" x1="1" y1="0" x2="0" y2="0">
          <stop offset="0%" stop-color="#00E676" stop-opacity="0.2"/>
          <stop offset="50%" stop-color="#69F0AE" stop-opacity="0.65"/>
          <stop offset="100%" stop-color="#00B0FF" stop-opacity="0.15"/>
        </linearGradient>
      </defs>
      <rect width="360" height="640" fill="url(#auroraBg)"/>
      <!-- Aurora Borealis Flowing Curtains -->
      <path d="M-40 140 Q100 40 220 160 T440 100" fill="none" stroke="url(#auroraCurtain1)" stroke-width="48" opacity="0.45"/>
      <path d="M-20 180 Q130 70 240 190 T460 130" fill="none" stroke="url(#auroraCurtain2)" stroke-width="32" opacity="0.5"/>
      <path d="M-50 260 Q120 150 250 280 T480 210" fill="none" stroke="url(#auroraCurtain1)" stroke-width="26" opacity="0.35"/>
      <!-- Soft Ring in Upper Region -->
      <circle cx="180" cy="200" r="95" fill="none" stroke="#1DE9B6" stroke-width="1.5" opacity="0.35"/>
      <circle cx="180" cy="200" r="75" fill="none" stroke="#00E676" stroke-width="2" opacity="0.45"/>
      <!-- Northern Sky Stars -->
      <circle cx="45" cy="65" r="1.5" fill="#FFF" opacity="0.9"/>
      <circle cx="120" cy="40" r="2.2" fill="#E0F2F1" opacity="0.95"/>
      <circle cx="280" cy="70" r="1.8" fill="#FFF" opacity="0.85"/>
      <circle cx="315" cy="115" r="1.3" fill="#FFF" opacity="0.75"/>
      <circle cx="80" cy="340" r="1.8" fill="#69F0AE" opacity="0.8"/>
      <!-- Mountain & Pine Silhouettes at bottom -->
      <polygon points="0,580 70,520 150,570 230,500 310,560 360,530 360,640 0,640" fill="#010a0e" opacity="0.95"/>
      <polygon points="40,580 80,540 120,580" fill="#010f15"/>
      <polygon points="190,560 230,515 270,560" fill="#010f15"/>
    </svg>`
  },
  {
    id: 'call_synthwave_sunset_80s',
    title: '🌴 Retro 80s Synthwave Grid',
    accent: '#FF007F',
    glow: '#7928CA',
    svg: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <linearGradient id="synthBg" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#140026"/>
          <stop offset="45%" stop-color="#2c0042"/>
          <stop offset="100%" stop-color="#0a0014"/>
        </linearGradient>
        <linearGradient id="sunGrad" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#FFE600"/>
          <stop offset="45%" stop-color="#FF007F"/>
          <stop offset="100%" stop-color="#7928CA"/>
        </linearGradient>
      </defs>
      <rect width="360" height="640" fill="url(#synthBg)"/>
      <!-- Glowing 80s Segmented Sun -->
      <circle cx="180" cy="200" r="68" fill="url(#sunGrad)"/>
      <!-- Sun Horizontal Blind Cuts -->
      <line x1="115" y1="185" x2="245" y2="185" stroke="#140026" stroke-width="3"/>
      <line x1="118" y1="202" x2="242" y2="202" stroke="#140026" stroke-width="4"/>
      <line x1="124" y1="220" x2="236" y2="220" stroke="#140026" stroke-width="5"/>
      <line x1="134" y1="238" x2="226" y2="238" stroke="#140026" stroke-width="6"/>
      <line x1="148" y1="254" x2="212" y2="254" stroke="#140026" stroke-width="6"/>
      <!-- Perspective Wireframe Ground Grid -->
      <line x1="0" y1="360" x2="360" y2="360" stroke="#00E5FF" stroke-width="2" opacity="0.85"/>
      <line x1="0" y1="390" x2="360" y2="390" stroke="#FF007F" stroke-width="1.8" opacity="0.75"/>
      <line x1="0" y1="435" x2="360" y2="435" stroke="#FF007F" stroke-width="1.6" opacity="0.65"/>
      <line x1="0" y1="495" x2="360" y2="495" stroke="#FF007F" stroke-width="1.4" opacity="0.55"/>
      <line x1="0" y1="570" x2="360" y2="570" stroke="#FF007F" stroke-width="1.2" opacity="0.45"/>
      <!-- Perspective Lines converging to center horizon -->
      <line x1="180" y1="360" x2="-40" y2="640" stroke="#00E5FF" stroke-width="1.5" opacity="0.5"/>
      <line x1="180" y1="360" x2="40" y2="640" stroke="#00E5FF" stroke-width="1.5" opacity="0.6"/>
      <line x1="180" y1="360" x2="120" y2="640" stroke="#00E5FF" stroke-width="1.5" opacity="0.7"/>
      <line x1="180" y1="360" x2="180" y2="640" stroke="#00E5FF" stroke-width="2" opacity="0.8"/>
      <line x1="180" y1="360" x2="240" y2="640" stroke="#00E5FF" stroke-width="1.5" opacity="0.7"/>
      <line x1="180" y1="360" x2="320" y2="640" stroke="#00E5FF" stroke-width="1.5" opacity="0.6"/>
      <line x1="180" y1="360" x2="400" y2="640" stroke="#00E5FF" stroke-width="1.5" opacity="0.5"/>
      <!-- Neon Horizon Aura -->
      <line x1="0" y1="360" x2="360" y2="360" stroke="#FFE600" stroke-width="3" opacity="0.9"/>
    </svg>`
  }
];

themes.forEach(t => {
  const filePath = path.join(outDir, `${t.id}.svg`);
  fs.writeFileSync(filePath, t.svg.trim(), 'utf8');
  console.log(`✅ Generated clean 3D wallpaper: ${filePath}`);
});

console.log('🎉 Successfully generated all 6 clean Call Screen vector SVG assets!');
