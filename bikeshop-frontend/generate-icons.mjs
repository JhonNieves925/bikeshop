/**
 * Genera los íconos PNG para la PWA de BikeShop.
 * Ejecutar una sola vez: node generate-icons.mjs
 *
 * No requiere dependencias externas — usa Node.js puro (zlib + Buffer).
 */

import { writeFileSync, mkdirSync } from 'fs'
import { deflateSync } from 'zlib'

// ── Helpers PNG puro ─────────────────────────────────────────────────────────
function to4(n) {
  return [(n >>> 24) & 0xff, (n >>> 16) & 0xff, (n >>> 8) & 0xff, n & 0xff]
}

function crc32(buf) {
  let crc = 0xffffffff
  for (const b of buf) {
    crc ^= b
    for (let i = 0; i < 8; i++) crc = (crc & 1) ? (crc >>> 1) ^ 0xedb88320 : crc >>> 1
  }
  return (crc ^ 0xffffffff) >>> 0
}

function chunk(type, data) {
  const typeBytes = Buffer.from(type, 'ascii')
  const dataBytes = Buffer.isBuffer(data) ? data : Buffer.from(data)
  const lenBuf = Buffer.from(to4(dataBytes.length))
  const crcBuf = Buffer.from(to4(crc32(Buffer.concat([typeBytes, dataBytes]))))
  return Buffer.concat([lenBuf, typeBytes, dataBytes, crcBuf])
}

/**
 * Crea un PNG con:
 *  - fondo oscuro (#0a0a0a)
 *  - círculo rojo (#E31837)
 *  - letras "BS" en blanco centradas
 * usando solo píxeles sólidos (sin antialiasing).
 */
function makePNG(size) {
  // Paleta
  const BG  = [0x0a, 0x0a, 0x0a]
  const RED = [0xe3, 0x18, 0x37]
  const WHT = [0xff, 0xff, 0xff]

  const cx = size / 2, cy = size / 2
  const r  = size * 0.42

  // Rasteriza "B" y "S" pixel a pixel (bitmap 5×7 escalado)
  const B_MAP = [
    [1,1,1,0,0],
    [1,0,0,1,0],
    [1,0,0,1,0],
    [1,1,1,0,0],
    [1,0,0,1,0],
    [1,0,0,1,0],
    [1,1,1,0,0],
  ]
  const S_MAP = [
    [0,1,1,1,0],
    [1,0,0,0,0],
    [1,0,0,0,0],
    [0,1,1,0,0],
    [0,0,0,1,0],
    [0,0,0,1,0],
    [1,1,1,0,0],
  ]

  // Escala de la letra relativa al tamaño del ícono
  const scale = Math.max(1, Math.floor(size / 64))
  const charW = 5 * scale, charH = 7 * scale
  const gap   = scale * 2
  const totalW = charW * 2 + gap
  const startX = Math.round(cx - totalW / 2)
  const startY = Math.round(cy - charH / 2)

  // Construye imagen row by row
  const rows = []
  for (let y = 0; y < size; y++) {
    const row = [0] // filter byte = None
    for (let x = 0; x < size; x++) {
      const dx = x - cx, dy = y - cy
      const inCircle = Math.sqrt(dx*dx + dy*dy) <= r

      let px = BG
      if (inCircle) px = RED

      // Letra B
      const bx = x - startX, by = y - startY
      const bc = Math.floor(bx / scale), br = Math.floor(by / scale)
      if (br >= 0 && br < 7 && bc >= 0 && bc < 5 && B_MAP[br][bc]) px = WHT

      // Letra S
      const sx = x - (startX + charW + gap), sy = y - startY
      const sc = Math.floor(sx / scale), sr = Math.floor(sy / scale)
      if (sr >= 0 && sr < 7 && sc >= 0 && sc < 5 && S_MAP[sr][sc]) px = WHT

      row.push(...px)
    }
    rows.push(Buffer.from(row))
  }

  const imgData  = Buffer.concat(rows)
  const compData = deflateSync(imgData, { level: 9 })

  const sig  = Buffer.from([137,80,78,71,13,10,26,10])
  const ihdr = chunk('IHDR', Buffer.from([
    ...to4(size), ...to4(size),
    8, 2, 0, 0, 0  // 8-bit RGB, no interlace
  ]))
  const idat = chunk('IDAT', compData)
  const iend = chunk('IEND', Buffer.alloc(0))

  return Buffer.concat([sig, ihdr, idat, iend])
}

// ── Generar ──────────────────────────────────────────────────────────────────
mkdirSync('public/icons', { recursive: true })

for (const size of [192, 512]) {
  const buf  = makePNG(size)
  const path = `public/icons/icon-${size}.png`
  writeFileSync(path, buf)
  console.log(`✓ ${path}  (${buf.length} bytes)`)
}

console.log('\n✅ Íconos generados. Ya puedes hacer npm run build.')
