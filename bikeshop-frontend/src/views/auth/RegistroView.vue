<template>
  <div class="min-h-screen bg-[#0a0a0a] flex items-center justify-center px-4 py-10">
    <div class="w-full max-w-md">
      <RouterLink to="/" class="flex items-center gap-1 justify-center mb-10">
        <span class="text-[#E31837] text-2xl font-black">BIKE</span>
        <span class="text-white text-2xl font-black">SHOP</span>
      </RouterLink>

      <div class="bg-[#141414] border border-[#2a2a2a] rounded-2xl p-8">
        <h1 class="text-2xl font-black mb-6">Crear cuenta</h1>

        <form @submit.prevent="registro" class="space-y-4">
          <div>
            <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Nombre completo</label>
            <input v-model="form.nombreCompleto" type="text" required
              class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
              placeholder="Tu nombre" />
          </div>
          <div>
            <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Email</label>
            <input v-model="form.email" type="email" required
              class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
              placeholder="tu@email.com" />
          </div>
          <div>
            <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Contraseña</label>
            <input v-model="form.contrasena" type="password" required minlength="8"
              class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
              placeholder="Mínimo 8 caracteres" />
          </div>
          <div>
            <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Celular (opcional)</label>
            <input v-model="form.celular" type="tel"
              class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors"
              placeholder="3XX XXX XXXX" />
          </div>

          <p v-if="error" class="text-[#E31837] text-sm">{{ error }}</p>

          <button type="submit" :disabled="loading"
            class="w-full bg-[#E31837] hover:bg-[#b5112a] disabled:opacity-50 text-white py-3 rounded-xl font-bold transition-colors">
            {{ loading ? 'Creando cuenta...' : 'Crear cuenta' }}
          </button>
        </form>

        <p class="text-center text-[#a0a0a0] text-sm mt-6">
          ¿Ya tienes cuenta?
          <RouterLink to="/login" class="text-[#E31837] hover:underline ml-1">Ingresa</RouterLink>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

const loading = ref(false)
const error = ref('')
const form = ref({ nombreCompleto: '', email: '', contrasena: '', celular: '' })

async function registro() {
  error.value = ''
  loading.value = true
  try {
    await auth.registroCliente(form.value)
    router.push('/mi-cuenta')
  } catch (e) {
    error.value = e.response?.data?.message || 'Error al crear la cuenta'
  } finally {
    loading.value = false
  }
}
</script>
