const BASE = 'http://localhost:8080'

/**
 * Construye la URL completa de una imagen del backend.
 * Maneja correctamente URLs que ya traen "/" al inicio.
 */
export function imgUrl(path) {
  if (!path) return null
  if (path.startsWith('http')) return path
  return BASE + (path.startsWith('/') ? path : '/' + path)
}
