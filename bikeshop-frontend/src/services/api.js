import axios from 'axios'

/**
 * Cliente HTTP centralizado.
 *
 * Frontend H1: el token JWT ya NO se almacena en localStorage.
 * Viaja en una cookie HttpOnly que el backend establece en el login.
 * El navegador la envía automáticamente gracias a `withCredentials: true`.
 * Como la cookie es HttpOnly, JavaScript no puede leerla → inmune a XSS.
 *
 * En desarrollo el proxy de Vite (vite.config.js) redirige /api → localhost:8080,
 * haciendo que las peticiones sean same-origin para el navegador.
 * En producción el frontend y el backend deben servirse desde el mismo dominio.
 */
const api = axios.create({
  baseURL: '/api',        // relativo → usa el proxy de Vite en dev / same-origin en prod
  timeout: 15000,
  withCredentials: true,  // envía la cookie HttpOnly en cada petición
  headers: { 'Content-Type': 'application/json' }
})

// Manejo global de errores
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Si el 401 viene de un endpoint de auth (login), es credencial inválida:
      // NO redirigir — dejar que el componente muestre el mensaje de error
      const isAuthEndpoint = error.config?.url?.includes('/auth/')

      // Solo hacer logout automático si había sesión activa y expiró (no en login)
      const user = JSON.parse(
        localStorage.getItem('user') || sessionStorage.getItem('user') || 'null'
      )

      if (user && !isAuthEndpoint) {
        const isStaff = user?.rol === 'ADMIN' || user?.rol === 'EMPLEADO'

        // Limpiar estado local
        ;['user'].forEach(k => {
          localStorage.removeItem(k)
          sessionStorage.removeItem(k)
        })
        localStorage.removeItem('recordar')

        // Redirigir al login correcto
        window.location.href = isStaff ? '/staff/login' : '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api
