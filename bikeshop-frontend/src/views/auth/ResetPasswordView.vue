<template>
  <div class="min-h-screen bg-[#0a0a0a] flex items-center justify-center px-4 py-10">
    <div class="w-full max-w-md">

      <RouterLink to="/" class="flex items-center gap-1 justify-center mb-10">
        <span class="text-[#E31837] text-2xl font-black">BIKE</span>
        <span class="text-white text-2xl font-black">SHOP</span>
      </RouterLink>

      <div class="bg-[#141414] border border-[#2a2a2a] rounded-2xl p-8">

        <!-- Token inválido / expirado -->
        <div v-if="tokenInvalido" class="text-center">
          <span class="text-5xl block mb-4">⏰</span>
          <h1 class="text-xl font-black mb-2">Enlace expirado</h1>
          <p class="text-[#a0a0a0] text-sm mb-6">
            Este enlace de recuperación no es válido o ya fue utilizado.<br>
            Solicita uno nuevo desde la página de inicio de sesión.
          </p>
          <RouterLink to="/login"
            class="inline-block bg-[#E31837] hover:bg-[#b5112a] text-white px-6 py-3 rounded-xl font-bold transition-colors">
            Volver al login
          </RouterLink>
        </div>

        <!-- Éxito -->
        <div v-else-if="exito" class="text-center">
          <span class="text-5xl block mb-4">✅</span>
          <h1 class="text-xl font-black mb-2">¡Contraseña actualizada!</h1>
          <p class="text-[#a0a0a0] text-sm mb-6">
            Ya puedes iniciar sesión con tu nueva contraseña.
          </p>
          <RouterLink to="/login"
            class="inline-block bg-[#E31837] hover:bg-[#b5112a] text-white px-6 py-3 rounded-xl font-bold transition-colors">
            Ir al login
          </RouterLink>
        </div>

        <!-- Formulario de nueva contraseña -->
        <div v-else>
          <div class="flex items-center gap-3 mb-6">
            <span class="text-3xl">🔑</span>
            <div>
              <h1 class="text-xl font-black">Nueva contraseña</h1>
              <p class="text-[#a0a0a0] text-xs mt-0.5">Crea una contraseña segura para tu cuenta</p>
            </div>
          </div>

          <form @submit.prevent="aplicarReset" class="space-y-4">
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Nueva contraseña</label>
              <input v-model="claveNueva" type="password" required minlength="8"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                placeholder="Mínimo 8 caracteres con letras y números"
                autocomplete="new-password" />
            </div>
            <div>
              <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Confirmar contraseña</label>
              <input v-model="confirmar" type="password" required minlength="8"
                class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
                placeholder="Repite la nueva contraseña"
                autocomplete="new-password" />
            </div>

            <!-- Indicador de requisitos -->
            <ul class="text-xs space-y-1">
              <li :class="claveNueva.length >= 8 ? 'text-green-400' : 'text-[#a0a0a0]'">
                {{ claveNueva.length >= 8 ? '✓' : '○' }} Mínimo 8 caracteres
              </li>
              <li :class="tieneLetras ? 'text-green-400' : 'text-[#a0a0a0]'">
                {{ tieneLetras ? '✓' : '○' }} Contiene letras
              </li>
              <li :class="tieneNumeros ? 'text-green-400' : 'text-[#a0a0a0]'">
                {{ tieneNumeros ? '✓' : '○' }} Contiene números
              </li>
            </ul>

            <p v-if="error" class="text-[#E31837] text-sm">{{ error }}</p>

            <button type="submit" :disabled="loading || !formularioValido"
              class="w-full bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl font-bold transition-colors">
              {{ loading ? 'Guardando...' : 'Guardar nueva contraseña' }}
            </button>
          </form>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import api from '@/services/api'

const route  = useRoute()
const router = useRouter()

const token      = ref('')
const claveNueva = ref('')
const confirmar  = ref('')
const loading    = ref(false)
const error      = ref('')
const exito      = ref(false)
const tokenInvalido = ref(false)

// Indicadores de complejidad
const tieneLetras  = computed(() => /[a-zA-Z]/.test(claveNueva.value))
const tieneNumeros = computed(() => /\d/.test(claveNueva.value))
const formularioValido = computed(() =>
  claveNueva.value.length >= 8 &&
  tieneLetras.value &&
  tieneNumeros.value &&
  claveNueva.value === confirmar.value
)

onMounted(() => {
  token.value = route.query.token || ''
  if (!token.value) {
    tokenInvalido.value = true
  }
})

async function aplicarReset() {
  error.value = ''

  if (claveNueva.value !== confirmar.value) {
    error.value = 'Las contraseñas no coinciden.'
    return
  }
  if (!formularioValido.value) {
    error.value = 'La contraseña no cumple los requisitos mínimos.'
    return
  }

  loading.value = true
  try {
    await api.post('/auth/clientes/reset-clave', {
      token: token.value,
      claveNueva: claveNueva.value
    })
    exito.value = true
  } catch (e) {
    const msg = e.response?.data?.message || ''
    if (msg.includes('expirado') || msg.includes('inválido') || msg.includes('utilizado')) {
      tokenInvalido.value = true
    } else {
      error.value = msg || 'Error al restablecer la contraseña. Intenta de nuevo.'
    }
  } finally {
    loading.value = false
  }
}
</script>
