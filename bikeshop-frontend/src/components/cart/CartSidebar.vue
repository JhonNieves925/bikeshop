<template>
  <!-- Overlay -->
  <Transition name="fade">
    <div v-if="cart.isOpen" class="fixed inset-0 bg-black/60 z-40" @click="cart.isOpen = false" />
  </Transition>

  <!-- Panel -->
  <Transition name="slide-right">
    <div v-if="cart.isOpen" class="fixed top-0 right-0 h-full w-96 bg-[#141414] border-l border-[#2a2a2a] z-50 flex flex-col">
      <!-- Header -->
      <div class="flex items-center justify-between px-6 py-5 border-b border-[#2a2a2a]">
        <h2 class="text-lg font-semibold">Carrito <span class="text-[#a0a0a0] text-sm font-normal">({{ cart.totalItems }})</span></h2>
        <button @click="cart.isOpen = false" class="text-[#a0a0a0] hover:text-white transition-colors text-xl">✕</button>
      </div>

      <!-- Items -->
      <div class="flex-1 overflow-y-auto px-6 py-4 space-y-4">
        <div v-if="cart.items.length === 0" class="text-center text-[#a0a0a0] py-20">
          <div class="text-5xl mb-4">🛒</div>
          <p>Tu carrito está vacío</p>
        </div>

        <div v-for="item in cart.items" :key="item.key"
          class="flex gap-4 bg-[#1a1a1a] rounded-xl p-4 border border-[#2a2a2a]">
          <img v-if="item.imagenUrl || item.imagenPrincipalUrl"
            :src="imgUrl(item.imagenUrl || item.imagenPrincipalUrl)"
            :alt="item.nombre"
            class="w-16 h-16 object-cover rounded-lg bg-[#2a2a2a]" />
          <div v-else class="w-16 h-16 bg-[#2a2a2a] rounded-lg flex items-center justify-center text-2xl">🚲</div>

          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium truncate">{{ item.nombre }}</p>
            <div class="flex items-center gap-2 mt-0.5">
              <p class="text-[#E31837] text-sm font-semibold">{{ formatPrice(item.precio) }}</p>
              <span v-if="item.talla"
                class="text-[10px] font-bold px-1.5 py-0.5 rounded bg-[#2a2a2a] text-[#a0a0a0] uppercase tracking-wide">
                {{ item.talla }}
              </span>
            </div>
            <div class="flex items-center gap-2 mt-2">
              <button @click="cart.actualizarCantidad(item.key, item.cantidad - 1)"
                class="w-6 h-6 bg-[#2a2a2a] rounded text-sm hover:bg-[#3a3a3a] transition-colors">−</button>
              <span class="text-sm w-6 text-center">{{ item.cantidad }}</span>
              <button @click="cart.actualizarCantidad(item.key, item.cantidad + 1)"
                class="w-6 h-6 bg-[#2a2a2a] rounded text-sm hover:bg-[#3a3a3a] transition-colors">+</button>
            </div>
          </div>

          <button @click="cart.quitar(item.key)" class="text-[#a0a0a0] hover:text-[#E31837] transition-colors text-sm self-start">✕</button>
        </div>
      </div>

      <!-- Footer -->
      <div v-if="cart.items.length > 0" class="px-6 py-5 border-t border-[#2a2a2a] space-y-3">

        <!-- Cupón -->
        <div v-if="!cart.cupon">
          <div class="flex gap-2">
            <input v-model="codigoCupon" type="text" placeholder="¿Tienes un cupón?"
              @keyup.enter="validarCupon"
              class="flex-1 bg-[#0a0a0a] border border-[#2a2a2a] rounded-xl px-3 py-2 text-white text-xs focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#555] uppercase" />
            <button @click="validarCupon" :disabled="loadingCupon || !codigoCupon.trim()"
              class="bg-[#2a2a2a] hover:bg-[#3a3a3a] disabled:opacity-40 text-white text-xs px-3 py-2 rounded-xl transition-colors font-medium whitespace-nowrap">
              {{ loadingCupon ? '...' : 'Aplicar' }}
            </button>
          </div>
          <p v-if="errorCupon" class="text-[#E31837] text-xs mt-1">{{ errorCupon }}</p>
        </div>

        <!-- Cupón aplicado -->
        <div v-else class="flex items-center justify-between bg-green-500/10 border border-green-500/20 rounded-xl px-3 py-2">
          <div class="flex items-center gap-2">
            <span class="text-green-400 text-sm">✓</span>
            <div>
              <p class="text-green-400 text-xs font-bold">{{ cart.cupon.codigo }}</p>
              <p class="text-[#a0a0a0] text-[10px]">
                {{ cart.cupon.tipo === 'PORCENTAJE' ? cart.cupon.descuento + '% de descuento' : formatPrice(cart.cupon.descuento) + ' de descuento' }}
              </p>
            </div>
          </div>
          <button @click="cart.quitarCupon()" class="text-[#a0a0a0] hover:text-white text-xs transition-colors">✕</button>
        </div>

        <!-- Totales -->
        <div class="space-y-1.5">
          <div class="flex justify-between text-sm">
            <span class="text-[#a0a0a0]">Subtotal</span>
            <span class="text-white">{{ formatPrice(cart.subtotal) }}</span>
          </div>
          <div v-if="cart.cupon" class="flex justify-between text-sm">
            <span class="text-green-400">Descuento</span>
            <span class="text-green-400 font-semibold">− {{ formatPrice(cart.descuentoCupon) }}</span>
          </div>
          <div class="flex justify-between pt-1 border-t border-[#2a2a2a]">
            <span class="text-white font-black">Total</span>
            <span class="text-xl font-black text-[#E31837]">{{ formatPrice(cart.totalConDescuento) }}</span>
          </div>
        </div>

        <RouterLink to="/checkout" @click="cart.isOpen = false"
          class="block w-full bg-[#E31837] hover:bg-[#b5112a] text-white text-center py-3 rounded-xl font-semibold transition-colors">
          Proceder al pago
        </RouterLink>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { imgUrl } from '@/utils/imgUrl'
import api from '@/services/api'

const cart = useCartStore()
const codigoCupon   = ref('')
const loadingCupon  = ref(false)
const errorCupon    = ref('')

function formatPrice(price) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(price)
}

async function validarCupon() {
  const codigo = codigoCupon.value.trim().toUpperCase()
  if (!codigo) return
  errorCupon.value = ''
  loadingCupon.value = true
  try {
    const { data } = await api.post('/public/cupones/validar', {
      codigo,
      subtotal: cart.subtotal
    })
    cart.aplicarCupon(data.data)
    codigoCupon.value = ''
  } catch (e) {
    errorCupon.value = e.response?.data?.message || 'Cupón no válido'
  } finally {
    loadingCupon.value = false
  }
}
</script>

<style scoped>
.slide-right-enter-active, .slide-right-leave-active { transition: transform 0.3s ease; }
.slide-right-enter-from, .slide-right-leave-to { transform: translateX(100%); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
