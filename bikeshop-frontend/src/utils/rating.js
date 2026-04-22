/** Genera un rating consistente (4.0–5.0) y cantidad de reseñas (8–120) basado en el id del producto.
 *  Determinístico — siempre devuelve el mismo valor para el mismo id.
 *  Cuando se implemente reseñas reales, se reemplaza esta función por datos del backend.
 */
function seeded(seed) {
  // Xorshift simple
  let x = seed ^ (seed << 13)
  x = x ^ (x >>> 7)
  x = x ^ (x << 5)
  return Math.abs(x) / 2147483647  // 0.0 – 1.0
}

export function getRating(id) {
  // 4.0 – 5.0 con 1 decimal
  return (4.0 + seeded(id * 7 + 3) * 1.0).toFixed(1)
}

export function getReviewCount(id) {
  // 8 – 128 reseñas
  return Math.floor(8 + seeded(id * 13 + 17) * 120)
}

/** Renderiza las estrellas como array [filled, filled, half, empty, empty] */
export function getStars(rating) {
  const r = parseFloat(rating)
  return [1, 2, 3, 4, 5].map(i => {
    if (r >= i) return 'full'
    if (r >= i - 0.5) return 'half'
    return 'empty'
  })
}
