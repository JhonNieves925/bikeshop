import axios from 'axios'

const apiBaseUrl = import.meta.env.VITE_API_URL?.trim() || '/api'

/**
 * Cliente HTTP centralizado.
 *
 * En desarrollo usa el proxy de Vite hacia localhost:8080.
 * En produccion, VITE_API_URL permite apuntar al backend desplegado.
 */
const api = axios.create({
  baseURL: apiBaseUrl,
  timeout: 15000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
})

// Manejo global de errores
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const isAuthEndpoint = error.config?.url?.includes('/auth/')

      const user = JSON.parse(
        localStorage.getItem('user') || sessionStorage.getItem('user') || 'null'
      )

      if (user && !isAuthEndpoint) {
        const isStaff = user?.rol === 'ADMIN' || user?.rol === 'EMPLEADO'

        ;['user'].forEach(k => {
          localStorage.removeItem(k)
          sessionStorage.removeItem(k)
        })
        localStorage.removeItem('recordar')

        window.location.href = isStaff ? '/panel/staff/login' : (import.meta.env.BASE_URL + 'login')
      }
    }
    return Promise.reject(error)
  }
)

export default api
