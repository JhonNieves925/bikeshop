<template>
  <RouterLink :to="`/producto/${producto.id}`"
    class="group bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden hover:border-[#E31837] transition-all duration-300 flex flex-col">

    <!-- Imagen -->
    <div class="relative h-64 bg-[#0a0a0a] overflow-hidden">
      <img
        v-if="producto.imagenPrincipalUrl"
        :src="imgUrl(producto.imagenPrincipalUrl)"
        :alt="producto.nombre"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
      />
      <div v-else class="w-full h-full flex items-center justify-center text-6xl text-[#2a2a2a]">🚲</div>

      <div v-if="producto.stock === 0"
        class="absolute inset-0 bg-black/60 flex items-center justify-center">
        <span class="text-white text-xs font-bold tracking-widest uppercase bg-[#2a2a2a] px-3 py-1 rounded-full">
          Agotado
        </span>
      </div>
    </div>

    <!-- Info -->
    <div class="p-4 flex flex-col flex-1">
      <p class="text-[#a0a0a0] text-xs uppercase tracking-wider mb-1">{{ producto.categoriaNombre }}</p>
      <h3 class="text-white font-semibold text-sm leading-tight mb-2 group-hover:text-[#E31837] transition-colors">
        {{ producto.nombre }}
      </h3>
      <!-- Estrellas -->
      <div class="flex items-center gap-1 mb-auto">
        <div class="flex gap-0.5">
          <span v-for="(tipo, i) in stars" :key="i" class="text-xs leading-none"
            :class="tipo === 'empty' ? 'text-[#3a3a3a]' : 'text-yellow-400'">
            {{ tipo === 'full' ? '★' : tipo === 'half' ? '⯨' : '★' }}
          </span>
        </div>
        <span class="text-[#a0a0a0] text-[10px]">({{ reviewCount }})</span>
      </div>
      <div class="flex items-center justify-between mt-3">
        <span class="text-[#E31837] font-bold">{{ formatPrice(producto.precio) }}</span>
        <button
          v-if="producto.stock > 0"
          @click.prevent="addToCart"
          class="bg-[#E31837] hover:bg-[#b5112a] text-white text-xs px-3 py-1.5 rounded-lg transition-colors font-medium"
        >
          + Agregar
        </button>
      </div>
    </div>
  </RouterLink>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { imgUrl } from '@/utils/imgUrl'
import { getRating, getReviewCount, getStars } from '@/utils/rating'

const props = defineProps({ producto: Object })
const cart = useCartStore()

const stars = computed(() => getStars(getRating(props.producto.id)))
const reviewCount = computed(() => getReviewCount(props.producto.id))

function formatPrice(price) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(price)
}

function addToCart() {
  cart.agregar(props.producto)
  cart.isOpen = true
}
</script>
