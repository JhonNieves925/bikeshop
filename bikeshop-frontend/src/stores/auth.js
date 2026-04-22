import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

/**
 * Store de autenticación.
 *
 * Frontend H1: el token JWT ya NO se almacena en localStorage/sessionStorage.
 * Viaja en una cookie HttpOnly establecida por el backend (no legible desde JS).
 * Aquí solo guardamos la información del usuario (nombre, rol, etc.) para la UI.
 *
 * isLoggedIn depende de si `user` existe en storage, no de si hay token.
 * Si el token de cookie expira, el primer API call devolverá 401 y el interceptor
 * en api.js limpiará el estado y redirigirá al login.
 */
export const useAuthStore = defineStore('auth', () => {
  const _storage = localStorage.getItem('recordar') === '0' ? sessionStorage : localStorage
  const user = ref(JSON.parse(_storage.getItem('user') || 'null'))

  const isLoggedIn    = computed(() => !!user.value)
  const isAdmin       = computed(() => user.value?.rol === 'ADMIN')
  const isEmpleado    = computed(() => user.value?.rol === 'EMPLEADO')
  const isCliente     = computed(() => user.value?.tipo === 'CLIENTE')
  const isStaff       = computed(() => isAdmin.value || isEmpleado.value)
  const debeCambiarClave = computed(() => user.value?.debeCambiarClave === true)

  async function loginUsuario(numeroDocumento, contrasena, recordar = true) {
    const { data } = await api.post('/auth/usuarios/login', { numeroDocumento, contrasena })
    setSession(data.data, recordar)
    return data.data
  }

  async function loginCliente(email, contrasena, recordar = true) {
    const { data } = await api.post('/auth/clientes/login', { email, contrasena })
    setSession(data.data, recordar)
    return data.data
  }

  async function registroCliente(payload) {
    const { data } = await api.post('/auth/clientes/registro', payload)
    setSession(data.data)
    return data.data
  }

  async function cambiarClave(claveActual, claveNueva) {
    await api.post('/auth/usuarios/cambiar-clave', { claveActual, claveNueva })
    if (user.value) {
      user.value.debeCambiarClave = false
      const storage = localStorage.getItem('recordar') === '0' ? sessionStorage : localStorage
      storage.setItem('user', JSON.stringify(user.value))
    }
  }

  function setSession(data, recordar = true) {
    user.value = {
      id: data.id,
      nombre: data.nombre,
      email: data.email || null,
      celular: data.celular || null,
      tipo: data.tipo,
      rol: data.rol,
      debeCambiarClave: data.debeCambiarClave
    }
    // Solo guardamos la info del usuario (no el token — está en cookie HttpOnly)
    const storage = recordar ? localStorage : sessionStorage
    storage.setItem('user', JSON.stringify(user.value))
    localStorage.setItem('recordar', recordar ? '1' : '0')
    // Guardar el tipo de acceso para que la PWA sepa a qué login redirigir
    const rol = data.rol
    if (rol === 'ADMIN' || rol === 'EMPLEADO') {
      localStorage.setItem('lastRole', 'STAFF')
    } else {
      localStorage.setItem('lastRole', 'CLIENTE')
    }
  }

  async function logout() {
    // Pedirle al backend que limpie la cookie HttpOnly (maxAge=0)
    try {
      await api.post('/auth/logout')
    } catch {
      // Si falla la llamada igual limpiamos el estado local
    }
    user.value = null
    ;['user'].forEach(k => {
      localStorage.removeItem(k)
      sessionStorage.removeItem(k)
    })
    localStorage.removeItem('recordar')
    // NO borramos lastRole — así la próxima vez que abran la app
    // el router sabe a qué login llevarlos
  }

  return {
    user,
    isLoggedIn, isAdmin, isEmpleado, isCliente, isStaff, debeCambiarClave,
    loginUsuario, loginCliente, registroCliente, cambiarClave,
    setSession, logout
  }
})
