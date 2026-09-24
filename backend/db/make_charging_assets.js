const fs = require('fs');
const path = require('path');

const outDir = path.join(__dirname, '..', 'public', 'uploads', 'charging');
if (!fs.existsSync(outDir)) {
  fs.mkdirSync(outDir, { recursive: true });
}

// Helper to create basic Lottie shape layer
function makeLottieCircleLayer(name, colorRgb, radius, rotationSpeed, scalePulse) {
  const [r, g, b] = colorRgb; // 0..1 range
  return {
    ddd: 0,
    ind: 1,
    ty: 4,
    nm: name,
    sr: 1,
    ks: {
      o: { a: 0, k: 100 },
      r: rotationSpeed !== 0 ? {
        a: 1,
        k: [
          { t: 0, s: [0], e: [360] },
          { t: 180, s: [360] }
        ]
      } : { a: 0, k: 0 },
      p: { a: 0, k: [200, 200, 0] },
      a: { a: 0, k: [0, 0, 0] },
      s: scalePulse ? {
        a: 1,
        k: [
          { t: 0, s: [90, 90, 100] },
          { t: 90, s: [110, 110, 100] },
          { t: 180, s: [90, 90, 100] }
        ]
      } : { a: 0, k: [100, 100, 100] }
    },
    shapes: [
      {
        ty: 'el',
        d: 1,
        p: { a: 0, k: [0, 0] },
        s: { a: 0, k: [radius * 2, radius * 2] },
        nm: 'Ellipse'
      },
      {
        ty: 'st',
        c: { a: 0, k: [r, g, b, 1] },
        o: { a: 0, k: 90 },
        w: { a: 0, k: 8 },
        lc: 2,
        lj: 2,
        nm: 'Stroke'
      }
    ],
    ip: 0,
    op: 180,
    st: 0,
    bm: 0
  };
}

function makeLottieGlowCore(name, colorRgb, size) {
  const [r, g, b] = colorRgb;
  return {
    ddd: 0,
    ind: 2,
    ty: 4,
    nm: name,
    sr: 1,
    ks: {
      o: {
        a: 1,
        k: [
          { t: 0, s: [60] },
          { t: 90, s: [95] },
          { t: 180, s: [60] }
        ]
      },
      r: { a: 0, k: 0 },
      p: { a: 0, k: [200, 200, 0] },
      a: { a: 0, k: [0, 0, 0] },
      s: {
        a: 1,
        k: [
          { t: 0, s: [85, 85, 100] },
          { t: 90, s: [115, 115, 100] },
          { t: 180, s: [85, 85, 100] }
        ]
      }
    },
    shapes: [
      {
        ty: 'el',
        d: 1,
        p: { a: 0, k: [0, 0] },
        s: { a: 0, k: [size, size] },
        nm: 'CoreCircle'
      },
      {
        ty: 'fl',
        c: { a: 0, k: [r, g, b, 0.45] },
        o: { a: 0, k: 80 },
        nm: 'Fill'
      },
      {
        ty: 'st',
        c: { a: 0, k: [r, g, b, 1] },
        o: { a: 0, k: 100 },
        w: { a: 0, k: 4 },
        nm: 'CoreStroke'
      }
    ],
    ip: 0,
    op: 180,
    st: 0,
    bm: 0
  };
}

// Generate 5 animations
const anims = [
  {
    filename: 'neon_cyber_arc.json',
    color: [0, 0.898, 1], // #00E5FF
    title: 'Neon Cyber Arc Reactor',
    layers: [
      makeLottieCircleLayer('OuterRing', [0, 0.898, 1], 130, 360, true),
      makeLottieCircleLayer('MidRing', [0, 0.6, 1], 95, -360, false),
      makeLottieGlowCore('Core', [0, 0.898, 1], 60)
    ]
  },
  {
    filename: 'quantum_vortex.json',
    color: [0.69, 0.15, 1], // #B026FF
    title: 'Quantum Cosmic Vortex',
    layers: [
      makeLottieCircleLayer('VortexOuter', [0.69, 0.15, 1], 135, 720, true),
      makeLottieCircleLayer('VortexMid', [0.85, 0.2, 0.9], 100, -360, true),
      makeLottieGlowCore('Singularity', [0.69, 0.15, 1], 50)
    ]
  },
  {
    filename: 'liquid_bubble_flow.json',
    color: [0, 1, 0.533], // #00FF88
    title: 'AMOLED Toxic Green Liquid',
    layers: [
      makeLottieCircleLayer('FluidRing', [0, 1, 0.533], 125, 180, true),
      makeLottieCircleLayer('BubbleWave', [0, 0.8, 0.4], 90, -180, true),
      makeLottieGlowCore('LiquidCenter', [0, 1, 0.533], 65)
    ]
  },
  {
    filename: 'lightning_turbo.json',
    color: [1, 0.72, 0], // #FFB800
    title: 'High-Voltage Lightning Bolt',
    layers: [
      makeLottieCircleLayer('LightningRing', [1, 0.72, 0], 130, 540, true),
      makeLottieCircleLayer('SparkRing', [1, 0.9, 0.2], 85, -540, false),
      makeLottieGlowCore('VoltCore', [1, 0.72, 0], 55)
    ]
  },
  {
    filename: 'minimal_zen.json',
    color: [1, 1, 1], // #FFFFFF
    title: 'Minimalist Pure Zen Ring',
    layers: [
      makeLottieCircleLayer('ZenOuter', [1, 1, 1], 120, 180, false),
      makeLottieGlowCore('ZenCore', [1, 1, 1], 40)
    ]
  }
];

anims.forEach(anim => {
  const lottieData = {
    v: '5.7.4',
    fr: 60,
    ip: 0,
    op: 180,
    w: 400,
    h: 400,
    nm: anim.title,
    ddd: 0,
    assets: [],
    layers: anim.layers
  };
  fs.writeFileSync(path.join(outDir, anim.filename), JSON.stringify(lottieData, null, 2));
  console.log(`Generated Lottie: ${anim.filename}`);
});

// Generate thumbnail SVGs
const thumbs = [
  { file: 'thumb_neon_arc.svg', color: '#00E5FF', name: 'Cyber Arc' },
  { file: 'thumb_quantum_vortex.svg', color: '#B026FF', name: 'Vortex 3D' },
  { file: 'thumb_liquid_bubble.svg', color: '#00FF88', name: 'Liquid Flow' },
  { file: 'thumb_lightning.svg', color: '#FFB800', name: 'Turbo Bolt' },
  { file: 'thumb_zen_ring.svg', color: '#FFFFFF', name: 'Zen Ring' }
];

thumbs.forEach(t => {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 400" width="300" height="400">
    <defs>
      <radialGradient id="bgGlow" cx="50%" cy="50%" r="50%">
        <stop offset="0%" stop-color="${t.color}" stop-opacity="0.35"/>
        <stop offset="100%" stop-color="#0a0b10" stop-opacity="1"/>
      </radialGradient>
      <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
        <feGaussianBlur stdDeviation="8" result="blur" />
        <feMerge>
          <feMergeNode in="blur"/>
          <feMergeNode in="SourceGraphic"/>
        </feMerge>
      </filter>
    </defs>
    <rect width="300" height="400" fill="#07080c"/>
    <circle cx="150" cy="180" r="120" fill="url(#bgGlow)"/>
    <circle cx="150" cy="180" r="85" fill="none" stroke="${t.color}" stroke-width="5" stroke-dasharray="16 8" filter="url(#glow)"/>
    <circle cx="150" cy="180" r="60" fill="none" stroke="${t.color}" stroke-width="8" opacity="0.8"/>
    <circle cx="150" cy="180" r="32" fill="${t.color}" opacity="0.25"/>
    <text x="150" y="190" text-anchor="middle" font-family="'Plus Jakarta Sans', sans-serif" font-size="28" font-weight="800" fill="${t.color}" filter="url(#glow)">⚡</text>
    <text x="150" y="325" text-anchor="middle" font-family="'Plus Jakarta Sans', sans-serif" font-size="18" font-weight="700" fill="#ffffff">${t.name}</text>
    <text x="150" y="350" text-anchor="middle" font-family="'Plus Jakarta Sans', sans-serif" font-size="12" font-weight="600" fill="${t.color}" letter-spacing="2">DYNAMIC CHARGING</text>
  </svg>`;
  fs.writeFileSync(path.join(outDir, t.file), svg.trim());
  console.log(`Generated SVG thumbnail: ${t.file}`);
});
