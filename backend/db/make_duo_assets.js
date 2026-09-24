const fs = require('fs');
const path = require('path');

const outDir = path.join(__dirname, '..', 'public', 'uploads', 'duo');
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

// 6 Curated Flagship Duo / Double Wallpapers (Lock & Home Magic Pairs)
const duoPairs = [
  {
    id: 'duo_cyber_samurai',
    lock: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="samuraiBgLock" cx="50%" cy="40%" r="75%">
          <stop offset="0%" stop-color="#14072b"/>
          <stop offset="50%" stop-color="#070212"/>
          <stop offset="100%" stop-color="#020006"/>
        </radialGradient>
        <linearGradient id="cyberHazeLock" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#7000FF" stop-opacity="0.3"/>
          <stop offset="100%" stop-color="#000" stop-opacity="0"/>
        </linearGradient>
      </defs>
      <rect width="360" height="640" fill="url(#samuraiBgLock)"/>
      <circle cx="180" cy="240" r="120" fill="none" stroke="#7000FF" stroke-width="1.5" stroke-dasharray="6 6" opacity="0.35"/>
      <circle cx="180" cy="240" r="90" fill="none" stroke="#00E5FF" stroke-width="1" opacity="0.25"/>
      <!-- Meditating Samurai Silhouette (Eyes Closed / Dormant Energy) -->
      <path d="M180 150 C160 150 148 165 148 185 C148 205 160 215 180 215 C200 215 212 205 212 185 C212 165 200 150 180 150 Z" fill="#1c0f38" stroke="#7000FF" stroke-width="2"/>
      <!-- Horns / Cyber Kabuto Helmet -->
      <path d="M152 165 L130 135 L145 155 Z" fill="#7000FF" opacity="0.8"/>
      <path d="M208 165 L230 135 L215 155 Z" fill="#7000FF" opacity="0.8"/>
      <!-- Closed Eyes (Subtle dormant slit lines) -->
      <line x1="165" y1="188" x2="173" y2="188" stroke="#4A148C" stroke-width="2.5" stroke-linecap="round"/>
      <line x1="187" y1="188" x2="195" y2="188" stroke="#4A148C" stroke-width="2.5" stroke-linecap="round"/>
      <!-- Robe / Shoulders resting -->
      <path d="M120 280 C120 230 150 220 180 220 C210 220 240 230 240 280 L250 360 L110 360 Z" fill="#0f0722" stroke="#7000FF" stroke-width="2"/>
      <!-- Sheathed Katana across lap -->
      <rect x="90" y="320" width="180" height="8" rx="4" fill="#2A1B4E" stroke="#7000FF" stroke-width="1.5"/>
      <rect x="235" y="318" width="35" height="12" rx="2" fill="#7000FF" opacity="0.7"/>
      <!-- Dormant Embers floating -->
      <circle cx="100" cy="420" r="1.5" fill="#7000FF" opacity="0.6"/>
      <circle cx="260" cy="460" r="1.8" fill="#00E5FF" opacity="0.4"/>
      <circle cx="150" cy="500" r="1.2" fill="#7000FF" opacity="0.5"/>
    </svg>`,
    home: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="samuraiBgHome" cx="50%" cy="40%" r="85%">
          <stop offset="0%" stop-color="#2d0b59"/>
          <stop offset="45%" stop-color="#15032b"/>
          <stop offset="100%" stop-color="#04000a"/>
        </radialGradient>
        <linearGradient id="katanaGlow" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#00E5FF"/>
          <stop offset="50%" stop-color="#FF007F"/>
          <stop offset="100%" stop-color="#7000FF"/>
        </linearGradient>
      </defs>
      <rect width="360" height="640" fill="url(#samuraiBgHome)"/>
      <!-- Awakened Cyber Matrix Shockwave Rings -->
      <circle cx="180" cy="240" r="140" fill="none" stroke="#00E5FF" stroke-width="2" stroke-dasharray="16 8" opacity="0.6"/>
      <circle cx="180" cy="240" r="110" fill="none" stroke="#FF007F" stroke-width="2.5" opacity="0.75"/>
      <circle cx="180" cy="240" r="85" fill="none" stroke="#00E5FF" stroke-width="1.5" opacity="0.8"/>
      <!-- Awakened Cyber Samurai with Glowing Neon Eyes & Aura -->
      <path d="M180 150 C160 150 148 165 148 185 C148 205 160 215 180 215 C200 215 212 205 212 185 C212 165 200 150 180 150 Z" fill="#240c4a" stroke="#00E5FF" stroke-width="2.5"/>
      <!-- Glowing Kabuto Horns -->
      <path d="M152 165 L125 125 L145 155 Z" fill="#00E5FF"/>
      <path d="M208 165 L235 125 L215 155 Z" fill="#00E5FF"/>
      <!-- AWAKENED GLOWING NEON CYAN EYES -->
      <ellipse cx="169" cy="186" rx="5" ry="3.5" fill="#00E5FF"/>
      <circle cx="169" cy="186" r="2" fill="#FFFFFF"/>
      <ellipse cx="191" cy="186" rx="5" ry="3.5" fill="#00E5FF"/>
      <circle cx="191" cy="186" r="2" fill="#FFFFFF"/>
      <!-- Eye Neon Flare Trails -->
      <path d="M164 186 Q140 182 120 175" fill="none" stroke="#00E5FF" stroke-width="2" opacity="0.8"/>
      <path d="M196 186 Q220 182 240 175" fill="none" stroke="#00E5FF" stroke-width="2" opacity="0.8"/>
      <!-- Awakened Battle Armor -->
      <path d="M110 270 C110 220 150 210 180 210 C210 210 250 220 250 270 L265 370 L95 370 Z" fill="#15052d" stroke="#FF007F" stroke-width="2.5"/>
      <!-- UNSHEATHED BLAZING ENERGY KATANA -->
      <line x1="60" y1="460" x2="300" y2="220" stroke="url(#katanaGlow)" stroke-width="5" stroke-linecap="round"/>
      <line x1="60" y1="460" x2="300" y2="220" stroke="#FFFFFF" stroke-width="2" stroke-linecap="round"/>
      <!-- Cyber Lightning Sparks -->
      <path d="M140 290 L155 310 L148 325 L165 345" fill="none" stroke="#00E5FF" stroke-width="2"/>
      <path d="M220 285 L205 305 L212 320 L195 340" fill="none" stroke="#FF007F" stroke-width="2"/>
      <!-- Radiant Energy Particles -->
      <circle cx="80" cy="200" r="3" fill="#00E5FF"/>
      <circle cx="280" cy="180" r="3" fill="#FF007F"/>
      <circle cx="180" cy="110" r="3.5" fill="#00E5FF"/>
    </svg>`
  },
  {
    id: 'duo_skyline_timelapse',
    lock: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <linearGradient id="sunsetSky" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#FF5722"/>
          <stop offset="35%" stop-color="#FF9800"/>
          <stop offset="65%" stop-color="#FFC107"/>
          <stop offset="100%" stop-color="#FFE082"/>
        </linearGradient>
      </defs>
      <rect width="360" height="640" fill="url(#sunsetSky)"/>
      <!-- Golden Giant Sun -->
      <circle cx="180" cy="310" r="75" fill="#FFF8E1" opacity="0.95"/>
      <!-- Warm Mountain Ridges in Distance -->
      <polygon points="0,420 80,360 180,410 270,350 360,400 360,640 0,640" fill="#E65100" opacity="0.4"/>
      <!-- Daytime City Skyscraper Silhouettes -->
      <rect x="20" y="340" width="40" height="300" fill="#3E2723"/>
      <rect x="70" y="280" width="50" height="360" fill="#2E1B17"/>
      <rect x="130" y="240" width="55" height="400" fill="#1B0F0C"/>
      <rect x="195" y="300" width="45" height="340" fill="#2E1B17"/>
      <rect x="250" y="260" width="50" height="380" fill="#1F120F"/>
      <rect x="310" y="330" width="40" height="310" fill="#3E2723"/>
      <!-- Building Spire -->
      <line x1="157" y1="190" x2="157" y2="240" stroke="#1B0F0C" stroke-width="3"/>
      <circle cx="157" cy="188" r="3" fill="#FF5722"/>
      <!-- Golden Water Reflection at bottom -->
      <rect x="0" y="550" width="360" height="90" fill="#BF360C" opacity="0.85"/>
      <line x1="140" y1="570" x2="220" y2="570" stroke="#FFE082" stroke-width="2" opacity="0.7"/>
      <line x1="110" y1="590" x2="250" y2="590" stroke="#FFE082" stroke-width="1.8" opacity="0.6"/>
    </svg>`,
    home: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <linearGradient id="midnightSky" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#02010A"/>
          <stop offset="40%" stop-color="#0B0320"/>
          <stop offset="75%" stop-color="#1A0033"/>
          <stop offset="100%" stop-color="#050014"/>
        </linearGradient>
      </defs>
      <rect width="360" height="640" fill="url(#midnightSky)"/>
      <!-- Neon Full Moon with Cyber Aura -->
      <circle cx="180" cy="180" r="50" fill="#00E5FF" opacity="0.2"/>
      <circle cx="180" cy="180" r="40" fill="#E0F7FA"/>
      <!-- Stars -->
      <circle cx="50" cy="80" r="1.5" fill="#FFF"/>
      <circle cx="110" cy="45" r="2" fill="#00E5FF"/>
      <circle cx="280" cy="70" r="1.5" fill="#FFF"/>
      <circle cx="320" cy="120" r="1.8" fill="#FF007F"/>
      <!-- Midnight Cyberpunk Skyscrapers Glowing with Windows & Billboards -->
      <rect x="20" y="340" width="40" height="300" fill="#080214" stroke="#00E5FF" stroke-width="1"/>
      <rect x="70" y="280" width="50" height="360" fill="#050110" stroke="#FF007F" stroke-width="1.2"/>
      <rect x="130" y="240" width="55" height="400" fill="#03000A" stroke="#00E5FF" stroke-width="1.5"/>
      <rect x="195" y="300" width="45" height="340" fill="#060112" stroke="#7000FF" stroke-width="1"/>
      <rect x="250" y="260" width="50" height="380" fill="#04000E" stroke="#00E5FF" stroke-width="1.2"/>
      <rect x="310" y="330" width="40" height="310" fill="#080214" stroke="#FF007F" stroke-width="1"/>
      <!-- Spire with laser beacon -->
      <line x1="157" y1="190" x2="157" y2="240" stroke="#00E5FF" stroke-width="3"/>
      <circle cx="157" cy="188" r="4" fill="#00E5FF"/>
      <line x1="157" y1="0" x2="157" y2="188" stroke="#00E5FF" stroke-width="1" stroke-dasharray="4 6" opacity="0.6"/>
      <!-- Glowing Neon Windows & Cyber Billboards -->
      <rect x="80" y="300" width="30" height="15" fill="#FF007F" opacity="0.85"/>
      <rect x="140" y="270" width="35" height="20" fill="#00E5FF" opacity="0.9"/>
      <rect x="260" y="280" width="30" height="40" fill="#7000FF" opacity="0.8"/>
      <!-- Window Grids -->
      <circle cx="35" cy="370" r="1.5" fill="#FFE082"/>
      <circle cx="45" cy="370" r="1.5" fill="#FFE082"/>
      <circle cx="35" cy="400" r="1.5" fill="#00E5FF"/>
      <circle cx="45" cy="400" r="1.5" fill="#FFE082"/>
      <circle cx="145" cy="330" r="2" fill="#00E5FF"/>
      <circle cx="165" cy="330" r="2" fill="#00E5FF"/>
      <circle cx="145" cy="360" r="2" fill="#FF007F"/>
      <circle cx="165" cy="360" r="2" fill="#FFE082"/>
      <!-- Neon Water Reflections -->
      <rect x="0" y="550" width="360" height="90" fill="#030008"/>
      <line x1="60" y1="570" x2="130" y2="570" stroke="#FF007F" stroke-width="2.5" opacity="0.75"/>
      <line x1="130" y1="580" x2="220" y2="580" stroke="#00E5FF" stroke-width="3" opacity="0.85"/>
      <line x1="240" y1="590" x2="310" y2="590" stroke="#7000FF" stroke-width="2.5" opacity="0.75"/>
    </svg>`
  },
  {
    id: 'duo_celestial_portal',
    lock: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="portalBgLock" cx="50%" cy="42%" r="75%">
          <stop offset="0%" stop-color="#12072B"/>
          <stop offset="50%" stop-color="#060210"/>
          <stop offset="100%" stop-color="#010005"/>
        </radialGradient>
      </defs>
      <rect width="360" height="640" fill="url(#portalBgLock)"/>
      <!-- Stars in night sky -->
      <circle cx="60" cy="90" r="1.2" fill="#FFF" opacity="0.8"/>
      <circle cx="140" cy="50" r="1.8" fill="#FFF" opacity="0.9"/>
      <circle cx="290" cy="80" r="1.5" fill="#FFF" opacity="0.7"/>
      <circle cx="320" cy="160" r="1.2" fill="#B388FF" opacity="0.8"/>
      <!-- Ancient Stone Stargate Ring (Dormant State) -->
      <circle cx="180" cy="270" r="110" fill="none" stroke="#2D1B4E" stroke-width="16"/>
      <circle cx="180" cy="270" r="118" fill="none" stroke="#7000FF" stroke-width="2" opacity="0.4"/>
      <circle cx="180" cy="270" r="102" fill="none" stroke="#7000FF" stroke-width="2" opacity="0.4"/>
      <!-- Dormant Glyphs on Ring -->
      <circle cx="180" cy="160" r="5" fill="#4A148C" stroke="#7000FF" stroke-width="1"/>
      <circle cx="280" cy="230" r="5" fill="#4A148C" stroke="#7000FF" stroke-width="1"/>
      <circle cx="270" cy="330" r="5" fill="#4A148C" stroke="#7000FF" stroke-width="1"/>
      <circle cx="90" cy="230" r="5" fill="#4A148C" stroke="#7000FF" stroke-width="1"/>
      <circle cx="100" cy="330" r="5" fill="#4A148C" stroke="#7000FF" stroke-width="1"/>
      <!-- Inner Portal Void (Dark & Peaceful) -->
      <circle cx="180" cy="270" r="95" fill="#080214"/>
      <circle cx="180" cy="270" r="40" fill="#100325" opacity="0.6"/>
      <!-- Stone Pedestal -->
      <polygon points="120,440 240,440 270,640 90,640" fill="#0C051D" stroke="#2D1B4E" stroke-width="2"/>
    </svg>`,
    home: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="portalBgHome" cx="50%" cy="42%" r="85%">
          <stop offset="0%" stop-color="#3A0866"/>
          <stop offset="45%" stop-color="#140228"/>
          <stop offset="100%" stop-color="#020008"/>
        </radialGradient>
        <radialGradient id="wormholeVortex" cx="50%" cy="50%" r="50%">
          <stop offset="0%" stop-color="#FFFFFF"/>
          <stop offset="25%" stop-color="#00E5FF"/>
          <stop offset="55%" stop-color="#7000FF"/>
          <stop offset="85%" stop-color="#FF007F"/>
          <stop offset="100%" stop-color="#000" stop-opacity="0"/>
        </radialGradient>
      </defs>
      <rect width="360" height="640" fill="url(#portalBgHome)"/>
      <!-- Hyperspace Wormhole BLASTING Through Stargate Ring -->
      <circle cx="180" cy="270" r="130" fill="url(#wormholeVortex)" opacity="0.95"/>
      <!-- Swirling Hyperspace Vortex Spiral Arcs -->
      <ellipse cx="180" cy="270" rx="95" ry="40" fill="none" stroke="#00E5FF" stroke-width="3" opacity="0.8" transform="rotate(30 180 270)"/>
      <ellipse cx="180" cy="270" rx="95" ry="40" fill="none" stroke="#FF007F" stroke-width="2.5" opacity="0.75" transform="rotate(-40 180 270)"/>
      <ellipse cx="180" cy="270" rx="80" ry="25" fill="none" stroke="#FFF" stroke-width="2" opacity="0.9" transform="rotate(75 180 270)"/>
      <!-- Blazing Stargate Ring with Glowing Runes -->
      <circle cx="180" cy="270" r="110" fill="none" stroke="#00E5FF" stroke-width="16" opacity="0.85"/>
      <circle cx="180" cy="270" r="118" fill="none" stroke="#FFF" stroke-width="2.5"/>
      <circle cx="180" cy="270" r="102" fill="none" stroke="#00E5FF" stroke-width="2.5"/>
      <!-- Glowing Activated Runes -->
      <circle cx="180" cy="160" r="7" fill="#00E5FF" stroke="#FFF" stroke-width="2"/>
      <circle cx="280" cy="230" r="7" fill="#00E5FF" stroke="#FFF" stroke-width="2"/>
      <circle cx="270" cy="330" r="7" fill="#00E5FF" stroke="#FFF" stroke-width="2"/>
      <circle cx="90" cy="230" r="7" fill="#00E5FF" stroke="#FFF" stroke-width="2"/>
      <circle cx="100" cy="330" r="7" fill="#00E5FF" stroke="#FFF" stroke-width="2"/>
      <!-- Energy Beams shooting out -->
      <line x1="180" y1="270" x2="40" y2="80" stroke="#00E5FF" stroke-width="2" opacity="0.65"/>
      <line x1="180" y1="270" x2="320" y2="90" stroke="#FF007F" stroke-width="2" opacity="0.65"/>
      <line x1="180" y1="270" x2="180" y2="40" stroke="#FFF" stroke-width="3" opacity="0.8"/>
      <!-- Core Energy Flare -->
      <circle cx="180" cy="270" r="28" fill="#FFF"/>
      <!-- Glowing Pedestal with Runes -->
      <polygon points="120,440 240,440 270,640 90,640" fill="#14062E" stroke="#00E5FF" stroke-width="2"/>
      <line x1="180" y1="440" x2="180" y2="640" stroke="#00E5FF" stroke-width="2" stroke-dasharray="8 6"/>
    </svg>`
  },
  {
    id: 'duo_super_saiyan',
    lock: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="saiyanBgLock" cx="50%" cy="40%" r="75%">
          <stop offset="0%" stop-color="#1A1202"/>
          <stop offset="45%" stop-color="#0A0601"/>
          <stop offset="100%" stop-color="#020100"/>
        </radialGradient>
      </defs>
      <rect width="360" height="640" fill="url(#saiyanBgLock)"/>
      <!-- Calm Silhouette & Base Form Hair Spikes -->
      <polygon points="180,120 160,160 140,140 145,190 120,180 135,220 180,210 225,220 240,180 215,190 220,140 200,160" fill="#1F1505" stroke="#FF9800" stroke-width="1.8"/>
      <!-- Focused Head & Jaw -->
      <path d="M165 200 L180 230 L195 200 Z" fill="#2E1C05" stroke="#FF9800" stroke-width="1.5"/>
      <!-- Deep Concentration Eyes -->
      <line x1="168" y1="205" x2="176" y2="208" stroke="#FFB300" stroke-width="2"/>
      <line x1="184" y1="208" x2="192" y2="205" stroke="#FFB300" stroke-width="2"/>
      <!-- Muscular Gi Body Silhouette -->
      <path d="M130 260 L180 240 L230 260 L245 380 L115 380 Z" fill="#140B02" stroke="#FF9800" stroke-width="1.5"/>
      <!-- Subtle Sparks of Dormant Ki -->
      <circle cx="100" cy="300" r="1.5" fill="#FFC107" opacity="0.6"/>
      <circle cx="260" cy="280" r="1.8" fill="#FF9800" opacity="0.7"/>
      <circle cx="180" cy="100" r="2" fill="#FFD54F" opacity="0.8"/>
    </svg>`,
    home: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="saiyanBgHome" cx="50%" cy="40%" r="85%">
          <stop offset="0%" stop-color="#422800"/>
          <stop offset="45%" stop-color="#1C1000"/>
          <stop offset="100%" stop-color="#050200"/>
        </radialGradient>
        <linearGradient id="saiyanAura" x1="0" y1="1" x2="0" y2="0">
          <stop offset="0%" stop-color="#FF6F00" stop-opacity="0.9"/>
          <stop offset="50%" stop-color="#FFD600" stop-opacity="0.85"/>
          <stop offset="85%" stop-color="#FFF" stop-opacity="0.95"/>
          <stop offset="100%" stop-color="#00E5FF" stop-opacity="0.7"/>
        </linearGradient>
      </defs>
      <rect width="360" height="640" fill="url(#saiyanBgHome)"/>
      <!-- EXPLODING SUPER SAIYAN GOD KI AURA -->
      <path d="M180 30 C100 120 70 240 70 420 C70 540 120 600 180 600 C240 600 290 540 290 420 C290 240 260 120 180 30 Z" fill="url(#saiyanAura)" opacity="0.45"/>
      <path d="M180 70 C120 150 95 250 95 400 C95 500 135 560 180 560 C225 560 265 500 265 400 C265 250 240 150 180 70 Z" fill="url(#saiyanAura)" opacity="0.6"/>
      <!-- Blazing Electric Plasma Lightning Arcs -->
      <path d="M70 220 L110 260 L90 290 L130 350" fill="none" stroke="#00E5FF" stroke-width="3"/>
      <path d="M290 200 L250 250 L270 280 L230 340" fill="none" stroke="#00E5FF" stroke-width="3"/>
      <path d="M120 140 L150 180 L140 200" fill="none" stroke="#FFF" stroke-width="2.5"/>
      <path d="M240 130 L210 175 L220 195" fill="none" stroke="#FFF" stroke-width="2.5"/>
      <!-- Blazing Golden Upward Spikes Hair -->
      <polygon points="180,60 150,130 125,100 130,170 100,150 115,210 180,195 245,210 260,150 230,170 235,100 210,130" fill="#FFD600" stroke="#FFF" stroke-width="2.5"/>
      <!-- Glowing Eyes with Cyan God Iris -->
      <path d="M165 195 L180 225 L195 195 Z" fill="#FFECB3" stroke="#FF6F00" stroke-width="1.5"/>
      <ellipse cx="171" cy="202" rx="4" ry="2.5" fill="#00E5FF"/>
      <circle cx="171" cy="202" r="1.5" fill="#FFF"/>
      <ellipse cx="189" cy="202" rx="4" ry="2.5" fill="#00E5FF"/>
      <circle cx="189" cy="202" r="1.5" fill="#FFF"/>
      <!-- Awakened Gi with Fiery Radiance -->
      <path d="M115 250 L180 230 L245 250 L265 380 L95 380 Z" fill="#E65100" stroke="#FFD600" stroke-width="2.5"/>
      <!-- Rising Ki Spheres -->
      <circle cx="140" cy="180" r="4" fill="#00E5FF"/>
      <circle cx="220" cy="160" r="4.5" fill="#FFD600"/>
      <circle cx="180" cy="50" r="5" fill="#FFF"/>
    </svg>`
  },
  {
    id: 'duo_soulmate_connection',
    lock: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <linearGradient id="moonSky" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#050B1A"/>
          <stop offset="50%" stop-color="#0B1A3A"/>
          <stop offset="100%" stop-color="#02050D"/>
        </linearGradient>
      </defs>
      <rect width="360" height="640" fill="url(#moonSky)"/>
      <!-- Crescent Moon (Left Half of Soulmates Connection) -->
      <path d="M210 160 A90 90 0 1 0 210 340 A70 70 0 1 1 210 160 Z" fill="#80D8FF" opacity="0.85"/>
      <circle cx="210" cy="250" r="95" fill="none" stroke="#00E5FF" stroke-width="1.5" stroke-dasharray="6 6" opacity="0.4"/>
      <!-- Starry Constellations on Lock -->
      <circle cx="80" cy="140" r="2" fill="#FFF"/>
      <circle cx="130" cy="110" r="2.5" fill="#80D8FF"/>
      <circle cx="90" cy="220" r="2" fill="#FFF"/>
      <line x1="80" y1="140" x2="130" y2="110" stroke="#80D8FF" stroke-width="1" opacity="0.5"/>
      <line x1="80" y1="140" x2="90" y2="220" stroke="#80D8FF" stroke-width="1" opacity="0.5"/>
      <!-- Moon Maiden Profile Silhouette -->
      <path d="M120 340 C120 280 150 250 180 250 C190 250 200 255 205 265 C190 280 185 310 195 340 Z" fill="#0C1B38" stroke="#80D8FF" stroke-width="2"/>
      <!-- Reaching Hand Towards Right -->
      <path d="M170 350 Q210 330 250 320" fill="none" stroke="#00E5FF" stroke-width="3" stroke-linecap="round"/>
      <circle cx="250" cy="320" r="5" fill="#00E5FF"/>
    </svg>`,
    home: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="sunSky" cx="50%" cy="42%" r="85%">
          <stop offset="0%" stop-color="#3A1505"/>
          <stop offset="45%" stop-color="#190600"/>
          <stop offset="100%" stop-color="#050100"/>
        </radialGradient>
      </defs>
      <rect width="360" height="640" fill="url(#sunSky)"/>
      <!-- Radiant Sun (Right Half of Connection, Connecting with Golden Heart Flare) -->
      <circle cx="150" cy="250" r="85" fill="#FF6F00" opacity="0.3"/>
      <circle cx="150" cy="250" r="70" fill="#FFAB00" opacity="0.75"/>
      <circle cx="150" cy="250" r="55" fill="#FFF8E1"/>
      <!-- Sun King Profile Silhouette Reaching Back -->
      <path d="M240 340 C240 280 210 250 180 250 C170 250 160 255 155 265 C170 280 175 310 165 340 Z" fill="#2E1103" stroke="#FFD54F" stroke-width="2"/>
      <!-- Reaching Hand Towards Center Connection -->
      <path d="M190 350 Q150 330 110 320" fill="none" stroke="#FFD54F" stroke-width="3" stroke-linecap="round"/>
      <circle cx="110" cy="320" r="5" fill="#FFD54F"/>
      <!-- Interlocking Cosmic Heart Flare in Center -->
      <path d="M180 300 C180 290 170 280 160 280 C145 280 140 295 140 305 C140 325 180 350 180 350 C180 350 220 325 220 305 C220 295 215 280 200 280 C190 280 180 290 180 300 Z" fill="#FF1744" stroke="#FFF" stroke-width="2"/>
      <!-- Cosmic Flare Sparks -->
      <circle cx="180" cy="315" r="4" fill="#FFF"/>
      <circle cx="280" cy="140" r="3" fill="#FFAB00"/>
      <circle cx="90" cy="160" r="2.5" fill="#FFD54F"/>
    </svg>`
  },
  {
    id: 'duo_neon_wildlife',
    lock: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="pantherBg" cx="50%" cy="40%" r="75%">
          <stop offset="0%" stop-color="#0c0517"/>
          <stop offset="50%" stop-color="#05010a"/>
          <stop offset="100%" stop-color="#010003"/>
        </radialGradient>
      </defs>
      <rect width="360" height="640" fill="url(#pantherBg)"/>
      <!-- Stealth Shadow Panther Silhouette blending into darkness -->
      <path d="M180 180 L130 140 L140 190 L110 220 L150 240 L180 270 L210 240 L250 220 L220 190 L230 140 Z" fill="#120722" stroke="#4A148C" stroke-width="2"/>
      <!-- Stealth Slit Eyes (Dark Amethyst) -->
      <polygon points="155,215 168,220 162,225" fill="#7C4DFF" opacity="0.85"/>
      <polygon points="205,215 192,220 198,225" fill="#7C4DFF" opacity="0.85"/>
      <!-- Subtle Shadow Halo -->
      <circle cx="180" cy="220" r="110" fill="none" stroke="#311B92" stroke-width="1.5" stroke-dasharray="8 6" opacity="0.3"/>
    </svg>`,
    home: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 360 640" width="100%" height="100%">
      <defs>
        <radialGradient id="tigerBg" cx="50%" cy="40%" r="85%">
          <stop offset="0%" stop-color="#2d0042"/>
          <stop offset="45%" stop-color="#12001c"/>
          <stop offset="100%" stop-color="#030005"/>
        </radialGradient>
      </defs>
      <rect width="360" height="640" fill="url(#tigerBg)"/>
      <!-- AWAKENED CYBER NEON TIGER with Electric Fangs & Glowing Stripes -->
      <circle cx="180" cy="220" r="130" fill="none" stroke="#FF007F" stroke-width="2" stroke-dasharray="14 8" opacity="0.5"/>
      <circle cx="180" cy="220" r="105" fill="none" stroke="#00E5FF" stroke-width="2.5" opacity="0.75"/>
      <!-- Geometric Tiger Head -->
      <polygon points="180,170 120,130 135,185 95,220 145,245 180,290 215,245 265,220 225,185 240,130 Z" fill="#1b0028" stroke="#00E5FF" stroke-width="3"/>
      <!-- Glowing Electric Cyan Eyes -->
      <polygon points="152,210 168,218 160,226" fill="#00E5FF"/>
      <circle cx="160" cy="218" r="2" fill="#FFF"/>
      <polygon points="208,210 192,218 200,226" fill="#00E5FF"/>
      <circle cx="200" cy="218" r="2" fill="#FFF"/>
      <!-- Neon Magenta Tiger Stripes -->
      <path d="M180 185 L180 205 M170 190 L160 200 M190 190 L200 200" stroke="#FF007F" stroke-width="3" stroke-linecap="round"/>
      <path d="M125 210 L145 220 M120 230 L140 235" stroke="#FF007F" stroke-width="3" stroke-linecap="round"/>
      <path d="M235 210 L215 220 M240 230 L220 235" stroke="#FF007F" stroke-width="3" stroke-linecap="round"/>
      <!-- Electric Fangs -->
      <polygon points="168,255 174,272 176,255" fill="#FFF" stroke="#00E5FF" stroke-width="1"/>
      <polygon points="192,255 186,272 184,255" fill="#FFF" stroke="#00E5FF" stroke-width="1"/>
      <!-- Energy sparks -->
      <circle cx="80" cy="180" r="3" fill="#00E5FF"/>
      <circle cx="280" cy="160" r="3" fill="#FF007F"/>
      <circle cx="180" cy="100" r="3.5" fill="#00E5FF"/>
    </svg>`
  }
];

duoPairs.forEach(p => {
  const lockPath = path.join(outDir, `${p.id}_lock.svg`);
  const homePath = path.join(outDir, `${p.id}_home.svg`);
  fs.writeFileSync(lockPath, p.lock.trim(), 'utf8');
  fs.writeFileSync(homePath, p.home.trim(), 'utf8');
  console.log(`✅ Generated: ${lockPath}`);
  console.log(`✅ Generated: ${homePath}`);
});

console.log('🎉 Generated all 12 Duo Wallpaper vector assets (6 pairs)!');
