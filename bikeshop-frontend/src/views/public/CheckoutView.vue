<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">

      <h1 class="text-3xl font-black mb-8">Checkout</h1>

      <!-- Carrito vacío -->
      <div v-if="cart.items.length === 0 && !pedidoConfirmado"
        class="flex flex-col items-center justify-center py-24 text-center">
        <p class="text-5xl mb-4">🛒</p>
        <p class="text-white font-semibold text-xl mb-2">Tu carrito está vacío</p>
        <p class="text-[#a0a0a0] text-sm mb-6">Agrega productos antes de continuar.</p>
        <RouterLink to="/catalogo"
          class="bg-[#E31837] hover:bg-[#b5112a] text-white px-6 py-3 rounded-xl text-sm font-semibold transition-colors">
          Ver catálogo
        </RouterLink>
      </div>

      <!-- Confirmación exitosa -->
      <div v-else-if="pedidoConfirmado"
        class="max-w-md mx-auto bg-[#1a1a1a] border border-green-500/30 rounded-2xl p-10 text-center">
        <div class="w-16 h-16 bg-green-500/20 rounded-full flex items-center justify-center mx-auto mb-6">
          <svg class="w-8 h-8 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/>
          </svg>
        </div>
        <h2 class="text-2xl font-black text-white mb-2">¡Pedido recibido!</h2>
        <p class="text-[#a0a0a0] text-sm mb-2">Tu pedido ha sido registrado exitosamente.</p>
        <p class="text-[#E31837] font-bold text-lg mb-6">Pedido #{{ pedidoId }}</p>
        <p class="text-[#a0a0a0] text-xs mb-8">
          Nos pondremos en contacto al correo <strong class="text-white">{{ form.emailCliente }}</strong>
          para confirmar tu pedido.
        </p>
        <RouterLink to="/catalogo"
          class="inline-block bg-[#E31837] hover:bg-[#b5112a] text-white px-8 py-3 rounded-xl font-bold text-sm transition-colors">
          Seguir comprando
        </RouterLink>
      </div>

      <!-- Checkout form -->
      <div v-else class="flex flex-col lg:flex-row gap-8">

        <!-- Formulario datos de envío -->
        <div class="flex-1">
          <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-6">
            <h2 class="text-lg font-black mb-6">Datos de envío</h2>
            <form @submit.prevent="realizarPedido" class="space-y-4">
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Nombre *</label>
                  <input v-model="form.nombreCliente" type="text" required
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]"
                    placeholder="Tu nombre" />
                </div>
                <div>
                  <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Apellido *</label>
                  <input v-model="form.apellidoCliente" type="text" required
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]"
                    placeholder="Tu apellido" />
                </div>
                <div>
                  <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Email *</label>
                  <input v-model="form.emailCliente" type="email" required
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]"
                    placeholder="tu@email.com" />
                </div>
                <div>
                  <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Teléfono *</label>
                  <input v-model="form.celularCliente" type="tel" required
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]"
                    placeholder="3XX XXX XXXX" />
                </div>
                <div class="sm:col-span-2">
                  <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Dirección *</label>
                  <input v-model="form.direccionEntrega" type="text" required
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]"
                    placeholder="Calle, barrio, número" />
                </div>
                <div>
                  <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Departamento *</label>
                  <select v-model="form.departamentoEntrega" required @change="form.ciudadEntrega = ''"
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors appearance-none cursor-pointer">
                    <option value="" disabled>Selecciona departamento</option>
                    <option v-for="dep in DEPARTAMENTOS" :key="dep" :value="dep">{{ dep }}</option>
                  </select>
                </div>
                <div>
                  <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Ciudad / Municipio *</label>
                  <select v-model="form.ciudadEntrega" required :disabled="!form.departamentoEntrega"
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors appearance-none cursor-pointer disabled:opacity-40 disabled:cursor-not-allowed">
                    <option value="" disabled>{{ form.departamentoEntrega ? 'Selecciona municipio' : 'Primero elige departamento' }}</option>
                    <option v-for="mun in municipiosDisponibles" :key="mun" :value="mun">{{ mun }}</option>
                  </select>
                </div>
                <div class="sm:col-span-2">
                  <label class="block text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Notas (opcional)</label>
                  <textarea v-model="form.notasEntrega" rows="2"
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors resize-none placeholder-[#4a4a4a]"
                    placeholder="Instrucciones especiales, piso, apartamento..." />
                </div>
              </div>

              <!-- Pago -->
              <div class="border border-[#2a2a2a] rounded-xl p-4">
                <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-3">Método de pago</p>
                <label class="flex items-center gap-3 cursor-pointer">
                  <div class="w-4 h-4 rounded-full border-2 border-[#E31837] flex items-center justify-center">
                    <div class="w-2 h-2 rounded-full bg-[#E31837]" />
                  </div>
                  <span class="text-white text-sm font-medium">Pago contra entrega</span>
                </label>
                <p class="text-[#a0a0a0] text-xs mt-2 ml-7">Pagas cuando recibas tu pedido.</p>
              </div>

              <p v-if="errorPedido" class="text-red-400 text-sm">{{ errorPedido }}</p>

              <button type="submit" :disabled="realizando"
                class="w-full py-4 rounded-xl font-black text-sm transition-all bg-[#E31837] hover:bg-[#b5112a] text-white disabled:opacity-50 disabled:cursor-not-allowed">
                {{ realizando ? 'Procesando...' : `Confirmar pedido — ${formatPrice(cart.totalConDescuento)}` }}
              </button>
            </form>
          </div>
        </div>

        <!-- Resumen carrito -->
        <div class="lg:w-80 shrink-0">
          <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-6 sticky top-6">
            <h2 class="text-lg font-black mb-5">Resumen</h2>

            <div class="space-y-4 mb-5">
              <div v-for="item in cart.items" :key="item.id"
                class="flex items-center gap-3">
                <div class="w-12 h-12 bg-[#0a0a0a] rounded-lg overflow-hidden shrink-0">
                  <img v-if="item.imagenPrincipalUrl" :src="imgUrl(item.imagenPrincipalUrl)"
                    class="w-full h-full object-cover" />
                  <span v-else class="w-full h-full flex items-center justify-center text-xl">🚲</span>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-white text-sm font-medium truncate">{{ item.nombre }}</p>
                  <p class="text-[#a0a0a0] text-xs">x{{ item.cantidad }}</p>
                </div>
                <p class="text-white text-sm font-semibold shrink-0">
                  {{ formatPrice(item.precio * item.cantidad) }}
                </p>
              </div>
            </div>

            <div class="border-t border-[#2a2a2a] pt-4 space-y-2">
              <div class="flex items-center justify-between">
                <span class="text-[#a0a0a0] text-sm">Subtotal</span>
                <span class="text-white font-semibold">{{ formatPrice(cart.subtotal) }}</span>
              </div>
              <div v-if="cart.cupon" class="flex items-center justify-between">
                <span class="text-green-400 text-sm flex items-center gap-1">
                  <span>✓</span> Cupón <strong>{{ cart.cupon.codigo }}</strong>
                </span>
                <span class="text-green-400 text-sm font-semibold">− {{ formatPrice(cart.descuentoCupon) }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-[#a0a0a0] text-sm">Envío</span>
                <span class="text-green-400 text-sm font-semibold">Gratis</span>
              </div>
              <div class="flex items-center justify-between pt-1 border-t border-[#2a2a2a]">
                <span class="text-white font-black">Total</span>
                <span class="text-2xl font-black text-[#E31837]">{{ formatPrice(cart.totalConDescuento) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import api from '@/services/api'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { imgUrl } from '@/utils/imgUrl'
import { DEPARTAMENTOS, getMunicipios } from '@/data/colombia'

const cart = useCartStore()
const auth = useAuthStore()

const pedidoConfirmado = ref(false)
const pedidoId = ref(null)
const realizando = ref(false)
const errorPedido = ref('')

const form = ref({
  nombreCliente: '',
  apellidoCliente: '',
  emailCliente: '',
  celularCliente: '',
  direccionEntrega: '',
  ciudadEntrega: '',
  departamentoEntrega: '',
  notasEntrega: '',
})

const municipiosDisponibles = computed(() => getMunicipios(form.value.departamentoEntrega))

function formatPrice(p) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(p)
}

async function realizarPedido() {
  realizando.value = true
  errorPedido.value = ''
  try {
    const payload = {
      nombreCliente: `${form.value.nombreCliente} ${form.value.apellidoCliente}`.trim(),
      emailCliente: form.value.emailCliente,
      celularCliente: form.value.celularCliente,
      direccionEntrega: form.value.direccionEntrega,
      ciudadEntrega: form.value.ciudadEntrega,
      departamentoEntrega: form.value.departamentoEntrega,
      notasEntrega: form.value.notasEntrega || null,
      items: cart.items.map(i => ({ productoId: i.id, cantidad: i.cantidad })),
      cuponCodigo: cart.cupon?.codigo || null,
    }
    const endpoint = auth.isLoggedIn ? '/clientes/pedidos' : '/public/pedidos'
    const { data } = await api.post(endpoint, payload)
    pedidoId.value = data.data?.id
    cart.vaciar()
    pedidoConfirmado.value = true
  } catch (e) {
    errorPedido.value = e.response?.data?.message || 'Error al procesar el pedido. Intenta de nuevo.'
  } finally { realizando.value = false }
}

onMounted(() => {
  if (auth.isLoggedIn && auth.user) {
    const partes = (auth.user.nombre || '').trim().split(' ')
    form.value.nombreCliente = partes[0] || ''
    form.value.apellidoCliente = partes.slice(1).join(' ') || ''
    form.value.emailCliente = auth.user.email || ''
    form.value.celularCliente = auth.user.celular || ''
  }
})
</script>
