<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

      <RouterLink to="/catalogo"
        class="inline-flex items-center gap-2 text-[#a0a0a0] hover:text-white text-sm transition-colors mb-8 group">
        <svg class="w-4 h-4 group-hover:-translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        Volver al catálogo
      </RouterLink>

      <!-- Loading skeleton -->
      <div v-if="loading" class="flex flex-col lg:flex-row gap-10 animate-pulse">
        <div class="lg:w-1/2 space-y-3">
          <div class="aspect-square bg-[#1a1a1a] rounded-2xl" />
          <div class="flex gap-3">
            <div v-for="n in 4" :key="n" class="w-20 h-20 bg-[#1a1a1a] rounded-xl" />
          </div>
        </div>
        <div class="lg:w-1/2 space-y-4">
          <div class="h-4 bg-[#1a1a1a] rounded w-24" />
          <div class="h-8 bg-[#1a1a1a] rounded w-3/4" />
          <div class="h-6 bg-[#1a1a1a] rounded w-1/3" />
          <div class="space-y-2 pt-4">
            <div class="h-3 bg-[#1a1a1a] rounded w-full" />
            <div class="h-3 bg-[#1a1a1a] rounded w-5/6" />
          </div>
          <div class="h-12 bg-[#1a1a1a] rounded-xl w-full mt-4" />
        </div>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="flex flex-col items-center justify-center py-24 text-center">
        <svg class="w-16 h-16 text-[#2a2a2a] mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
            d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <p class="text-white font-semibold text-xl mb-2">Producto no encontrado</p>
        <p class="text-[#a0a0a0] text-sm mb-6">Este producto no existe o fue eliminado.</p>
        <RouterLink to="/catalogo"
          class="px-5 py-2.5 bg-[#E31837] hover:bg-[#b5112a] text-white rounded-lg text-sm font-medium transition-colors">
          Ver catálogo
        </RouterLink>
      </div>

      <!-- Detalle -->
      <div v-else-if="producto" class="flex flex-col lg:flex-row gap-10">

        <!-- Galería -->
        <div class="lg:w-1/2 space-y-3">
          <div class="relative aspect-square bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl overflow-hidden group">
            <img v-if="activeImage" :src="imgUrl(activeImage)" :alt="producto.nombre"
              class="w-full h-full object-cover cursor-zoom-in transition-transform duration-300 group-hover:scale-[1.02]"
              @click="abrirLightbox(activeImage)" />
            <div v-else class="w-full h-full flex flex-col items-center justify-center text-[#2a2a2a]">
              <svg class="w-24 h-24" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1"
                  d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
              </svg>
              <span class="text-sm mt-2">Sin imagen</span>
            </div>
            <!-- Icono ampliar -->
            <div v-if="activeImage" class="absolute bottom-3 right-3 bg-black/60 text-white rounded-full w-8 h-8 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zm4 0v4m0 0h-4"/>
              </svg>
            </div>
            <div v-if="stockActual === 0 && !producto.tieneTallas"
              class="absolute top-4 left-4 bg-black/70 text-white text-xs font-bold px-3 py-1.5 rounded-full tracking-widest uppercase">
              Agotado
            </div>
          </div>
          <div v-if="producto.imagenes && producto.imagenes.length > 1" class="flex gap-3 overflow-x-auto pb-1">
            <button v-for="img in producto.imagenes" :key="img.id" @click="activeImage = img.url"
              class="shrink-0 w-20 h-20 rounded-xl overflow-hidden border-2 transition-colors"
              :class="activeImage === img.url ? 'border-[#E31837]' : 'border-[#2a2a2a] hover:border-[#a0a0a0]'">
              <img :src="imgUrl(img.url)" :alt="producto.nombre" class="w-full h-full object-cover" />
            </button>
          </div>
        </div>

        <!-- Info -->
        <div class="lg:w-1/2 flex flex-col">
          <span class="inline-flex self-start text-xs bg-[#E31837]/10 text-[#E31837] border border-[#E31837]/20 rounded-full px-3 py-1 font-medium mb-3">
            {{ producto.categoriaNombre || 'Sin categoría' }}
          </span>
          <h1 class="text-2xl sm:text-3xl font-black leading-tight text-white mb-3">{{ producto.nombre }}</h1>

          <!-- Estrellas + personas viendo -->
          <div class="flex flex-wrap items-center gap-4 mb-4">
            <div class="flex items-center gap-2">
              <div class="flex gap-0.5">
                <span v-for="(tipo, i) in productStars" :key="i"
                  class="text-base leading-none"
                  :class="tipo === 'empty' ? 'text-[#3a3a3a]' : 'text-yellow-400'">
                  {{ tipo === 'full' ? '★' : tipo === 'half' ? '⯨' : '★' }}
                </span>
              </div>
              <span class="text-white font-semibold text-sm">{{ productRating }}</span>
              <span class="text-[#a0a0a0] text-sm">({{ productReviews }} reseñas)</span>
            </div>
            <!-- Personas viendo -->
            <div class="flex items-center gap-1.5 text-xs text-[#a0a0a0] bg-[#1a1a1a] border border-[#2a2a2a] rounded-full px-3 py-1">
              <span class="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse inline-block"></span>
              <span><strong class="text-white">{{ personasViendo }}</strong> personas ven esto ahora</span>
            </div>
          </div>

          <p class="text-3xl font-black text-[#E31837] mb-4">{{ formatPrice(producto.precio) }}</p>

          <!-- Indicador de stock -->
          <div class="flex items-center gap-2 mb-6">
            <span class="w-2.5 h-2.5 rounded-full shrink-0"
              :class="{
                'bg-green-500': stockActual > 5,
                'bg-yellow-400': stockActual >= 1 && stockActual <= 5,
                'bg-red-500': stockActual === 0
              }" />
            <span class="text-sm font-medium"
              :class="{
                'text-green-400': stockActual > 5,
                'text-yellow-400': stockActual >= 1 && stockActual <= 5,
                'text-red-400': stockActual === 0
              }">
              <template v-if="producto.tieneTallas && !tallaSeleccionada">Selecciona una talla</template>
              <template v-else-if="stockActual === 0">Sin stock</template>
              <template v-else-if="stockActual <= 5">Últimas {{ stockActual }} unidades</template>
              <template v-else>En stock ({{ stockActual }} disponibles)</template>
            </span>
          </div>

          <!-- ── Selector de tallas ────────────────────────────────────── -->
          <div v-if="producto.tieneTallas && producto.tallas?.length" class="mb-6">
            <p class="text-xs text-[#a0a0a0] uppercase tracking-wider font-semibold mb-3">
              Talla
              <span v-if="tallaSeleccionada" class="text-white normal-case tracking-normal ml-1">— {{ tallaSeleccionada }}</span>
            </p>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="t in producto.tallas"
                :key="t.id"
                type="button"
                @click="t.stock > 0 ? seleccionarTalla(t.talla) : null"
                class="relative min-w-[44px] h-10 px-3 rounded-lg border-2 text-sm font-semibold transition-all"
                :class="[
                  t.stock === 0
                    ? 'border-[#2a2a2a] text-[#4a4a4a] cursor-not-allowed'
                    : tallaSeleccionada === t.talla
                      ? 'border-[#E31837] bg-[#E31837]/10 text-white'
                      : 'border-[#2a2a2a] text-[#a0a0a0] hover:border-white hover:text-white cursor-pointer'
                ]">
                <!-- Línea diagonal de agotado -->
                <span v-if="t.stock === 0"
                  class="absolute inset-0 flex items-center justify-center pointer-events-none">
                  <span class="absolute w-full h-px bg-[#3a3a3a] rotate-[-20deg]"></span>
                </span>
                {{ t.talla }}
              </button>
            </div>
            <p v-if="producto.tieneTallas && !tallaSeleccionada" class="text-xs text-yellow-400/80 mt-2">
              ↑ Elige tu talla para continuar
            </p>
          </div>

          <div v-if="producto.descripcion" class="mb-8">
            <h2 class="text-xs text-[#a0a0a0] uppercase tracking-wider font-semibold mb-2">Descripción</h2>
            <p class="text-[#a0a0a0] text-sm leading-relaxed whitespace-pre-line">{{ producto.descripcion }}</p>
          </div>

          <div class="flex-1" />

          <div class="border-t border-[#2a2a2a] pt-6 space-y-4">
            <div v-if="canAddToCart" class="flex items-center gap-4">
              <label class="text-sm text-[#a0a0a0] font-medium">Cantidad</label>
              <div class="flex items-center bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg overflow-hidden">
                <button @click="decreaseQty" :disabled="qty <= 1"
                  class="w-10 h-10 flex items-center justify-center text-[#a0a0a0] hover:text-white hover:bg-[#2a2a2a] transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 12H4"/>
                  </svg>
                </button>
                <span class="w-10 text-center text-sm font-semibold text-white select-none">{{ qty }}</span>
                <button @click="increaseQty" :disabled="qty >= maxQty"
                  class="w-10 h-10 flex items-center justify-center text-[#a0a0a0] hover:text-white hover:bg-[#2a2a2a] transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
                  </svg>
                </button>
              </div>
            </div>

            <button @click="addToCart"
              :disabled="!canAddToCart || addedFeedback"
              class="w-full py-3.5 rounded-xl font-bold text-sm transition-all duration-200 flex items-center justify-center gap-2"
              :class="addedFeedback
                ? 'bg-green-600 text-white'
                : !canAddToCart
                  ? 'bg-[#1a1a1a] border border-[#2a2a2a] text-[#a0a0a0] cursor-not-allowed'
                  : 'bg-[#E31837] hover:bg-[#b5112a] text-white active:scale-[0.98]'">
              <template v-if="addedFeedback">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/>
                </svg>
                ¡Agregado al carrito!
              </template>
              <template v-else-if="producto.tieneTallas && !tallaSeleccionada">
                Selecciona una talla
              </template>
              <template v-else-if="stockActual === 0">Producto agotado</template>
              <template v-else>
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
                </svg>
                Agregar al carrito
              </template>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Lightbox ───────────────────────────────────────────────────── -->
    <Transition name="lightbox">
      <div v-if="lightboxOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-black/95"
        @click.self="cerrarLightbox" @keydown.esc="cerrarLightbox">

        <!-- Cerrar -->
        <button @click="cerrarLightbox"
          class="absolute top-4 right-4 text-white/70 hover:text-white bg-white/10 hover:bg-white/20 rounded-full w-10 h-10 flex items-center justify-center transition-all z-10">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>

        <!-- Flecha anterior -->
        <button v-if="imagenes.length > 1" @click="lightboxPrev"
          class="absolute left-4 top-1/2 -translate-y-1/2 text-white/70 hover:text-white bg-white/10 hover:bg-white/20 rounded-full w-10 h-10 flex items-center justify-center transition-all">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
          </svg>
        </button>

        <!-- Imagen -->
        <img :src="imgUrl(lightboxUrl)" :alt="producto.nombre"
          class="max-w-[90vw] max-h-[90vh] object-contain rounded-xl select-none" />

        <!-- Flecha siguiente -->
        <button v-if="imagenes.length > 1" @click="lightboxNext"
          class="absolute right-4 top-1/2 -translate-y-1/2 text-white/70 hover:text-white bg-white/10 hover:bg-white/20 rounded-full w-10 h-10 flex items-center justify-center transition-all">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
          </svg>
        </button>

        <!-- Indicador de posición -->
        <div v-if="imagenes.length > 1" class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-1.5">
          <span v-for="(img, i) in imagenes" :key="img.id"
            class="w-1.5 h-1.5 rounded-full transition-colors"
            :class="lightboxIndex === i ? 'bg-white' : 'bg-white/30'" />
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import api from '@/services/api'
import { imgUrl } from '@/utils/imgUrl'
import { useCartStore } from '@/stores/cart'
import { getRating, getReviewCount, getStars } from '@/utils/rating'

const route = useRoute()
const cart = useCartStore()
const producto = ref(null)
const loading = ref(false)
const error = ref(false)
const activeImage = ref(null)
const qty = ref(1)
const addedFeedback = ref(false)
const tallaSeleccionada = ref(null)

// Computed: lista de imágenes disponibles
const imagenes = computed(() => producto.value?.imagenes || [])

// Stock según talla seleccionada (o stock general si no hay tallas)
const stockActual = computed(() => {
  if (!producto.value) return 0
  if (producto.value.tieneTallas && tallaSeleccionada.value) {
    return producto.value.tallas?.find(t => t.talla === tallaSeleccionada.value)?.stock ?? 0
  }
  if (producto.value.tieneTallas) return 0  // tallas activas pero sin selección → no se puede agregar
  return producto.value.stock
})

const canAddToCart = computed(() => {
  if (!producto.value) return false
  if (producto.value.tieneTallas) {
    if (!tallaSeleccionada.value) return false
    return stockActual.value > 0
  }
  return producto.value.stock > 0
})

const maxQty = computed(() => Math.max(1, Math.min(stockActual.value, 10)))

// Reiniciar cantidad al cambiar talla
watch(tallaSeleccionada, () => { qty.value = 1 })

function seleccionarTalla(talla) {
  tallaSeleccionada.value = tallaSeleccionada.value === talla ? null : talla
}

// ── Estrellas y reseñas ficticias ────────────────────────────────────────────
const productRating   = computed(() => producto.value ? getRating(producto.value.id) : '5.0')
const productReviews  = computed(() => producto.value ? getReviewCount(producto.value.id) : 0)
const productStars    = computed(() => getStars(productRating.value))

// ── Personas viendo ahora (simula movimiento real) ───────────────────────────
const personasViendo = ref(0)
let personasTimer = null

function iniciarPersonasViendo(id) {
  const base = 3 + Math.floor(Math.abs(Math.sin(id * 7)) * 10)
  personasViendo.value = base
  personasTimer = setInterval(() => {
    const delta = Math.random() < 0.5 ? 1 : -1
    const nuevo = personasViendo.value + delta
    personasViendo.value = Math.max(2, Math.min(15, nuevo))
  }, 30000 + Math.random() * 15000)
}

// ── Lightbox ──────────────────────────────────────────────────────────────────
const lightboxOpen  = ref(false)
const lightboxIndex = ref(0)
const lightboxUrl   = computed(() => imagenes.value[lightboxIndex.value]?.url || null)

function abrirLightbox(url) {
  if (!url) return
  const idx = imagenes.value.findIndex(i => i.url === url)
  lightboxIndex.value = idx >= 0 ? idx : 0
  lightboxOpen.value = true
  document.body.style.overflow = 'hidden'
}

function cerrarLightbox() {
  lightboxOpen.value = false
  document.body.style.overflow = ''
}

function lightboxNext() {
  lightboxIndex.value = (lightboxIndex.value + 1) % imagenes.value.length
  activeImage.value = imagenes.value[lightboxIndex.value]?.url
}

function lightboxPrev() {
  lightboxIndex.value = (lightboxIndex.value - 1 + imagenes.value.length) % imagenes.value.length
  activeImage.value = imagenes.value[lightboxIndex.value]?.url
}

function onKeydown(e) {
  if (!lightboxOpen.value) return
  if (e.key === 'Escape')     cerrarLightbox()
  if (e.key === 'ArrowRight') lightboxNext()
  if (e.key === 'ArrowLeft')  lightboxPrev()
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
  if (personasTimer) clearInterval(personasTimer)
})

function formatPrice(price) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 }).format(price)
}
function decreaseQty() { if (qty.value > 1) qty.value-- }
function increaseQty() { if (qty.value < maxQty.value) qty.value++ }

function addToCart() {
  if (!canAddToCart.value) return
  cart.agregar(producto.value, qty.value, tallaSeleccionada.value)
  addedFeedback.value = true
  setTimeout(() => { addedFeedback.value = false }, 2000)
}

async function fetchProducto() {
  loading.value = true
  error.value = false
  tallaSeleccionada.value = null
  try {
    const res = await api.get(`/public/productos/${route.params.id}`)
    producto.value = res.data.data
    const imgs = producto.value.imagenes || []
    const principal = imgs.find(i => i.esPrincipal) || imgs[0]
    activeImage.value = principal ? principal.url : null
    qty.value = 1
    if (producto.value) iniciarPersonasViendo(producto.value.id)
  } catch {
    error.value = true
    producto.value = null
  } finally {
    loading.value = false
  }
}

onMounted(fetchProducto)
</script>

<style scoped>
.lightbox-enter-active, .lightbox-leave-active { transition: opacity 0.2s ease; }
.lightbox-enter-from, .lightbox-leave-to { opacity: 0; }
</style>
