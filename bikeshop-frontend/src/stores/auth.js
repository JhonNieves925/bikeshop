import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const _storage = localStorage.getItem('recordar') === '0' ? sessionStorage : localStorage
  const user = ref(JSON.parse(_storage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!user.value)
  const isCliente  = computed(() => user.value?.tipo === 'CLIENTE')

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

  function setSession(data, recordar = true) {
    user.value = {
      id: data.id,
      nombre: data.nombre,
      email: data.email || null,
      celular: data.celular || null,
      tipo: data.tipo,
      rol: data.rol,
    }
    const storage = recordar ? localStorage : sessionStorage
    storage.setItem('user', JSON.stringify(user.value))
    localStorage.setItem('recordar', recordar ? '1' : '0')
  }

  async function logout() {
    try { await api.post('/auth/logout') } catch {}
    user.value = null
    ;['user'].forEach(k => {
      localStorage.removeItem(k)
      sessionStorage.removeItem(k)
    })
    localStorage.removeItem('recordar')
  }

  return { user, isLoggedIn, isCliente, loginCliente, registroCliente, setSession, logout }
})
