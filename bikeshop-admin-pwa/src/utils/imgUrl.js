/**
 * Construye la URL completa de una imagen del backend.
 * En desarrollo usa localhost:8080, en producción las rutas son relativas.
 */
export function imgUrl(path) {
  if (!path) return null
  if (path.startsWith('http')) return path
  return path.startsWith('/') ? path : '/' + path
}
