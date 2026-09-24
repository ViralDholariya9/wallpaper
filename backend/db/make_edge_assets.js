const fs = require('fs');
const path = require('path');

const edgeDir = path.join(__dirname, '..', 'public', 'uploads', 'edge');
if (!fs.existsSync(edgeDir)) {
  fs.mkdirSync(edgeDir, { recursive: true });
}

function makeSvg(name, title, gradientStops, styleType) {
  const stopsXml = gradientStops.map((c, i) => {
    const offset = Math.round((i / (gradientStops.length - 1)) * 100);
    return `<stop offset="${offset}%" stop-color="${c}"/>`;
  }).join('\n      ');

  let extraSvg = '';
  if (styleType === 'punch_hole') {
    extraSvg = `<circle cx="100" cy="24" r="8" fill="#00FFCC" filter="url(#glow)"/>
      <circle cx="100" cy="24" r="5" fill="#000"/>`;
  }

  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 360" width="200" height="360">
  <defs>
    <linearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
      ${stopsXml}
    </linearGradient>
    <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
      <feGaussianBlur stdDeviation="6" result="blur" />
      <feMerge>
        <feMergeNode in="blur" />
        <feMergeNode in="SourceGraphic" />
      </feMerge>
    </filter>
  </defs>
  <rect width="200" height="360" rx="28" fill="#0c0e17" />
  <rect x="8" y="8" width="184" height="344" rx="22" fill="none" stroke="url(#grad)" stroke-width="6" filter="url(#glow)" />
  <rect x="16" y="16" width="168" height="328" rx="16" fill="#08090f" />
  ${extraSvg}
  <text x="100" y="180" fill="#ffffff" font-size="14" font-weight="bold" font-family="sans-serif" text-anchor="middle">${title}</text>
  <text x="100" y="202" fill="rgba(255,255,255,0.6)" font-size="10" font-family="sans-serif" text-anchor="middle">EDGE GLOW PRESET</text>
</svg>`;

  fs.writeFileSync(path.join(edgeDir, name), svg, 'utf8');
  console.log(`Generated ${name}`);
}

makeSvg('thumb_rgb_rainbow.svg', 'RGB RAINBOW', ['#FF0055', '#FF7700', '#FFE600', '#00FF66', '#00E5FF', '#7000FF']);
makeSvg('thumb_cyber_snake.svg', 'CYBER SNAKE', ['#00E5FF', '#7000FF', '#00E5FF']);
makeSvg('thumb_neon_pulse.svg', 'NEON PULSE', ['#FF007F', '#9B00FF', '#00FFFF']);
makeSvg('thumb_galaxy_glow.svg', 'GALAXY DRIFT', ['#4A00E0', '#8E2DE2', '#00C9FF']);
makeSvg('thumb_solar_flare.svg', 'SOLAR FLARE', ['#FF3E00', '#FF8500', '#FFD200', '#FF1E56']);
makeSvg('thumb_punch_aura.svg', 'PUNCH AURA', ['#00FFCC', '#0072FF', '#00FFCC'], 'punch_hole');
