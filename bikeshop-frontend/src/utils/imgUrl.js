/**
 * Construye la URL de una imagen del backend.
 * Usa VITE_BACKEND_URL en produccion y rutas relativas en desarrollo.
 */
const backendUrl = import.meta.env.VITE_BACKEND_URL?.trim()?.replace(/\/$/, '') || ''

export function imgUrl(path) {
  if (!path) return null
  if (path.startsWith('http')) return path
  const normalizedPath = path.startsWith('/') ? path : '/' + path
  return backendUrl ? `${backendUrl}${normalizedPath}` : normalizedPath
}
