<template>
  <div class="min-h-screen bg-[#0a0a0a] flex items-center justify-center px-4 py-10">
    <div class="w-full max-w-md">

      <RouterLink to="/" class="flex items-center gap-1 justify-center mb-10">
        <span class="text-[#E31837] text-2xl font-black">BIKE</span>
        <span class="text-white text-2xl font-black">SHOP</span>
      </RouterLink>

      <Transition name="fade" mode="out-in">

        <!-- ── Pantalla: cambiar contraseña obligatorio ──────────────────── -->
        <div v-if="pantalla === 'cambiarClave'" class="bg-[#141414] border border-[#2a2a2a] rounded-2xl p-8">
          <div class="flex items-center gap-3 mb-6">
            <span class="text-3xl">🔒</span>
            <div>
              <h1 class="text-xl font-black">Cambia tu contraseña</h1>
              <p class="text-[#a0a0a0] text-xs mt-0.5">Por seguridad debes crear una nueva contraseña</p>
            </div>
          </div>

          <form @submit.prevent="cambiarClave" class="space-y-4">
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Contraseña temporal</label>
              <div class="relative">
                <input v-model="formCambio.claveActual" :type="verActual ? 'text' : 'password'" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 pr-11 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="La que te asignaron" />
                <button type="button" @click="verActual = !verActual" class="absolute right-3 top-1/2 -translate-y-1/2 text-[#a0a0a0] hover:text-white transition-colors">
                  <svg v-if="!verActual" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                  <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/></svg>
                </button>
              </div>
            </div>
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Nueva contraseña</label>
              <div class="relative">
                <input v-model="formCambio.claveNueva" :type="verNueva ? 'text' : 'password'" required minlength="8"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 pr-11 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="Mínimo 8 caracteres con letras y números" />
                <button type="button" @click="verNueva = !verNueva" class="absolute right-3 top-1/2 -translate-y-1/2 text-[#a0a0a0] hover:text-white transition-colors">
                  <svg v-if="!verNueva" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                  <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/></svg>
                </button>
              </div>
            </div>
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Confirmar contraseña</label>
              <div class="relative">
                <input v-model="formCambio.confirmar" :type="verConfirmar ? 'text' : 'password'" required
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 pr-11 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="Repite la nueva contraseña" />
                <button type="button" @click="verConfirmar = !verConfirmar" class="absolute right-3 top-1/2 -translate-y-1/2 text-[#a0a0a0] hover:text-white transition-colors">
                  <svg v-if="!verConfirmar" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                  <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/></svg>
                </button>
              </div>
            </div>
            <p v-if="errorCambio" class="text-[#E31837] text-sm">{{ errorCambio }}</p>
            <button type="submit" :disabled="loadingCambio"
              class="w-full bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl font-bold transition-colors">
              {{ loadingCambio ? 'Guardando...' : 'Guardar nueva contraseña' }}
            </button>
          </form>
        </div>

        <!-- ── Pantalla: login personal ──────────────────────────────────── -->
        <div v-else class="space-y-0">
          <div class="bg-[#141414] border border-[#2a2a2a] rounded-2xl p-8">
            <h1 class="text-2xl font-black mb-1">Acceso personal</h1>
            <p class="text-[#a0a0a0] text-sm mb-6">Panel de empleados y administración</p>

            <form @submit.prevent="loginStaff" class="space-y-4">
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Número de documento</label>
                <input v-model="form.numeroDocumento" type="text" required autocomplete="username"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                  placeholder="Ej: 1234567890" />
              </div>
              <div>
                <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Contraseña</label>
                <div class="relative">
                  <input v-model="form.contrasena" :type="mostrarClave ? 'text' : 'password'" required autocomplete="current-password"
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 pr-11 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                    placeholder="••••••••" />
                  <button type="button" @click="mostrarClave = !mostrarClave"
                    class="absolute right-3 top-1/2 -translate-y-1/2 text-[#a0a0a0] hover:text-white transition-colors">
                    <svg v-if="!mostrarClave" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                    <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/></svg>
                  </button>
                </div>
              </div>

              <label class="flex items-center gap-2 cursor-pointer select-none">
                <input v-model="recordar" type="checkbox"
                  class="w-4 h-4 rounded accent-[#E31837] cursor-pointer" />
                <span class="text-[#a0a0a0] text-sm">Recordarme</span>
              </label>

              <p v-if="error" class="text-[#E31837] text-sm">{{ error }}</p>
              <button type="submit" :disabled="loading"
                class="w-full bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl font-bold transition-colors">
                {{ loading ? 'Ingresando...' : 'Ingresar' }}
              </button>
            </form>

          </div>
        </div>

      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const auth    = useAuthStore()
const router  = useRouter()

const pantalla    = ref('login')   // 'login' | 'cambiarClave'
const loading     = ref(false)
const error       = ref('')
const recordar    = ref(true)
const mostrarClave = ref(false)

const form = ref({ numeroDocumento: '', contrasena: '' })

// ── Login ──────────────────────────────────────────────────────────────────────
async function loginStaff() {
  error.value = ''
  loading.value = true
  try {
    const data = await auth.loginUsuario(
      form.value.numeroDocumento,
      form.value.contrasena,
      recordar.value
    )
    if (data?.debeCambiarClave) {
      pantalla.value = 'cambiarClave'
    } else {
      router.push('/admin')
    }
  } catch (e) {
    error.value = e.response?.data?.message || 'Credenciales inválidas'
  } finally {
    loading.value = false
  }
}

// ── Cambiar contraseña (primer acceso) ────────────────────────────────────────
const formCambio    = ref({ claveActual: '', claveNueva: '', confirmar: '' })
const loadingCambio = ref(false)
const errorCambio   = ref('')
const verActual     = ref(false)
const verNueva      = ref(false)
const verConfirmar  = ref(false)

async function cambiarClave() {
  errorCambio.value = ''
  if (formCambio.value.claveNueva !== formCambio.value.confirmar) {
    errorCambio.value = 'Las contraseñas no coinciden.'
    return
  }
  loadingCambio.value = true
  try {
    await api.post('/auth/usuarios/cambiar-clave', {
      claveActual: formCambio.value.claveActual,
      claveNueva:  formCambio.value.claveNueva,
    })
    if (auth.user) {
      auth.user.debeCambiarClave = false
      localStorage.setItem('user', JSON.stringify(auth.user))
    }
    router.push('/admin')
  } catch (e) {
    errorCambio.value = e.response?.data?.message || 'Error al cambiar la contraseña.'
  } finally {
    loadingCambio.value = false
  }
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
