/**
 * Construye la URL de una imagen del backend.
 * Usa rutas relativas para que Vercel pueda proxyear /uploads al backend.
 */
export function imgUrl(path) {
  if (!path) return null
  if (path.startsWith('http')) return path
  return path.startsWith('/') ? path : '/' + path
}
