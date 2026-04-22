<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white">

    <!-- Mobile filter drawer overlay -->
    <Transition name="fade">
      <div
        v-if="drawerOpen"
        class="fixed inset-0 bg-black/70 z-40 lg:hidden"
        @click="drawerOpen = false"
      />
    </Transition>

    <!-- Mobile filter drawer -->
    <Transition name="slide-left">
      <aside
        v-if="drawerOpen"
        class="fixed top-0 left-0 h-full w-72 bg-[#1a1a1a] border-r border-[#2a2a2a] z-50 lg:hidden flex flex-col"
      >
        <div class="flex items-center justify-between px-5 py-4 border-b border-[#2a2a2a]">
          <span class="font-bold text-base">Filtros</span>
          <button @click="drawerOpen = false" class="text-[#a0a0a0] hover:text-white transition-colors p-1">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>
        <div class="flex-1 overflow-y-auto p-5 space-y-6">
          <!-- Categorías -->
          <div>
            <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Categorías</p>
            <div class="flex flex-col gap-1">
              <button
                @click="selectCategory(null)"
                class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
                :class="filters.categoriaId === null
                  ? 'bg-[#E31837]/10 text-[#E31837] font-medium border border-[#E31837]/20'
                  : 'text-[#a0a0a0] hover:bg-[#2a2a2a] hover:text-white border border-transparent'"
              >
                Todos
              </button>
              <button
                v-for="cat in categorias"
                :key="cat.id"
                @click="selectCategory(cat.id)"
                class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center justify-between"
                :class="filters.categoriaId === cat.id
                  ? 'bg-[#E31837]/10 text-[#E31837] font-medium border border-[#E31837]/20'
                  : 'text-[#a0a0a0] hover:bg-[#2a2a2a] hover:text-white border border-transparent'"
              >
                {{ cat.nombre }}
                <svg v-if="filters.categoriaId === cat.id" class="w-3.5 h-3.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
              </button>
            </div>
          </div>
          <!-- Precio -->
          <div>
            <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-2">Precio (COP)</p>
            <div class="flex flex-col gap-2">
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-[#a0a0a0] text-xs">$</span>
                <input v-model="filters.precioMin" type="number" min="0" placeholder="Mínimo"
                  @change="onFilterChange"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-lg pl-6 pr-3 py-2 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
              </div>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-[#a0a0a0] text-xs">$</span>
                <input v-model="filters.precioMax" type="number" min="0" placeholder="Máximo"
                  @change="onFilterChange"
                  class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-lg pl-6 pr-3 py-2 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
              </div>
              <button v-if="filters.precioMin || filters.precioMax" @click="clearPrecio"
                class="text-xs text-[#a0a0a0] hover:text-[#E31837] transition-colors text-left">
                × Limpiar precio
              </button>
            </div>
          </div>
        </div>
      </aside>
    </Transition>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

      <!-- Page header -->
      <div class="mb-8">
        <h1 class="text-3xl font-black tracking-tight text-white">Catálogo</h1>
        <p class="text-[#a0a0a0] text-sm mt-1">
          {{ totalElements }} producto{{ totalElements !== 1 ? 's' : '' }} disponible{{ totalElements !== 1 ? 's' : '' }}
        </p>
      </div>

      <!-- Mobile top bar -->
      <div class="flex items-center gap-3 mb-6 lg:hidden">
        <button
          @click="drawerOpen = true"
          class="flex items-center gap-2 px-4 py-2 bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg text-sm hover:border-[#E31837] transition-colors"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4h18M7 12h10M11 20h2"/>
          </svg>
          Filtros
          <span v-if="filters.categoriaId" class="w-2 h-2 rounded-full bg-[#E31837] inline-block" />
        </button>

        <span
          v-if="activeCategoryName"
          class="text-xs bg-[#E31837]/20 text-[#E31837] border border-[#E31837]/30 rounded-full px-3 py-1 flex items-center gap-1.5"
        >
          {{ activeCategoryName }}
          <button @click="selectCategory(null)" class="hover:text-white transition-colors">
            <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </span>

        <div class="ml-auto">
          <select
            v-model="filters.sort"
            @change="onSortChange"
            class="bg-[#1a1a1a] border border-[#2a2a2a] text-white text-sm rounded-lg px-3 py-2 focus:outline-none focus:border-[#E31837] cursor-pointer"
          >
            <option value="nombre">A - Z</option>
            <option value="precio">Precio</option>
          </select>
        </div>
      </div>

      <div class="flex gap-8">

        <!-- Desktop sidebar -->
        <aside class="hidden lg:flex flex-col w-64 shrink-0">
          <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-5 sticky top-6">
            <div class="flex items-center justify-between mb-4">
              <span class="font-semibold text-sm text-white">Categorías</span>
              <span
                v-if="filters.categoriaId"
                class="text-xs text-[#E31837] bg-[#E31837]/10 border border-[#E31837]/20 rounded-full px-2 py-0.5"
              >
                Activo
              </span>
            </div>

            <div class="flex flex-col gap-1">
              <button
                @click="selectCategory(null)"
                class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors"
                :class="filters.categoriaId === null
                  ? 'bg-[#E31837]/10 text-[#E31837] font-medium border border-[#E31837]/20'
                  : 'text-[#a0a0a0] hover:bg-[#2a2a2a] hover:text-white border border-transparent'"
              >
                Todos
              </button>
              <button
                v-for="cat in categorias"
                :key="cat.id"
                @click="selectCategory(cat.id)"
                class="w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center justify-between"
                :class="filters.categoriaId === cat.id
                  ? 'bg-[#E31837]/10 text-[#E31837] font-medium border border-[#E31837]/20'
                  : 'text-[#a0a0a0] hover:bg-[#2a2a2a] hover:text-white border border-transparent'"
              >
                {{ cat.nombre }}
                <svg v-if="filters.categoriaId === cat.id" class="w-3.5 h-3.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
              </button>
            </div>

            <!-- Price range desktop -->
            <div class="mt-6 pt-5 border-t border-[#2a2a2a]">
              <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-3">Precio (COP)</p>
              <div class="flex flex-col gap-2">
                <div class="relative">
                  <span class="absolute left-3 top-1/2 -translate-y-1/2 text-[#a0a0a0] text-xs">$</span>
                  <input v-model="filters.precioMin" type="number" min="0" placeholder="Mínimo"
                    @change="onFilterChange"
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-lg pl-6 pr-3 py-2 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
                </div>
                <div class="relative">
                  <span class="absolute left-3 top-1/2 -translate-y-1/2 text-[#a0a0a0] text-xs">$</span>
                  <input v-model="filters.precioMax" type="number" min="0" placeholder="Máximo"
                    @change="onFilterChange"
                    class="w-full bg-[#0a0a0a] border border-[#2a2a2a] rounded-lg pl-6 pr-3 py-2 text-white text-sm focus:outline-none focus:border-[#E31837] transition-colors placeholder-[#4a4a4a]" />
                </div>
                <button v-if="filters.precioMin || filters.precioMax" @click="clearPrecio"
                  class="text-xs text-[#a0a0a0] hover:text-[#E31837] transition-colors text-left">
                  × Limpiar precio
                </button>
              </div>
            </div>

            <!-- Sort desktop -->
            <div class="mt-6 pt-5 border-t border-[#2a2a2a]">
              <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-3">Ordenar por</p>
              <div class="flex flex-col gap-2">
                <button
                  v-for="opt in sortOptions"
                  :key="opt.value"
                  @click="filters.sort = opt.value; onSortChange()"
                  class="flex items-center gap-2 text-sm px-3 py-2 rounded-lg transition-colors"
                  :class="filters.sort === opt.value
                    ? 'bg-[#E31837]/10 text-[#E31837] border border-[#E31837]/20'
                    : 'text-[#a0a0a0] hover:bg-[#2a2a2a] hover:text-white border border-transparent'"
                >
                  {{ opt.label }}
                </button>
              </div>
            </div>
          </div>
        </aside>

        <!-- Main content -->
        <main class="flex-1 min-w-0">

          <!-- Search bar -->
          <div class="mb-6 relative">
            <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#a0a0a0]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
            </svg>
            <input
              v-model="searchInput"
              @input="onSearchInput"
              type="text"
              placeholder="Buscar productos..."
              class="w-full bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl pl-10 pr-10 py-3 text-sm text-white placeholder-[#a0a0a0] focus:outline-none focus:border-[#E31837] transition-colors"
            />
            <button
              v-if="searchInput"
              @click="clearSearch"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-[#a0a0a0] hover:text-white transition-colors"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- Loading skeleton -->
          <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            <div
              v-for="n in 6"
              :key="n"
              class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl overflow-hidden animate-pulse"
            >
              <div class="h-52 bg-[#2a2a2a]" />
              <div class="p-4 space-y-3">
                <div class="h-3 bg-[#2a2a2a] rounded w-1/3" />
                <div class="h-4 bg-[#2a2a2a] rounded w-4/5" />
                <div class="flex justify-between items-center pt-2">
                  <div class="h-5 bg-[#2a2a2a] rounded w-24" />
                  <div class="h-7 bg-[#2a2a2a] rounded w-20" />
                </div>
              </div>
            </div>
          </div>

          <!-- Empty state -->
          <div
            v-else-if="productos.length === 0"
            class="flex flex-col items-center justify-center py-24 text-center"
          >
            <svg class="w-16 h-16 text-[#2a2a2a] mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <p class="text-white font-semibold text-lg mb-1">Sin resultados</p>
            <p class="text-[#a0a0a0] text-sm max-w-xs">
              No encontramos productos
              <span v-if="filters.q"> para "<strong class="text-white">{{ filters.q }}</strong>"</span>
              <span v-if="activeCategoryName"> en <strong class="text-white">{{ activeCategoryName }}</strong></span>.
            </p>
            <button
              @click="resetFilters"
              class="mt-5 px-5 py-2.5 bg-[#E31837] hover:bg-[#b5112a] text-white rounded-lg text-sm font-medium transition-colors"
            >
              Limpiar filtros
            </button>
          </div>

          <!-- Product grid -->
          <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            <ProductCard
              v-for="producto in productos"
              :key="producto.id"
              :producto="producto"
            />
          </div>

          <!-- Pagination -->
          <div
            v-if="!loading && totalPages > 1"
            class="mt-10 flex items-center justify-center gap-3"
          >
            <button
              @click="prevPage"
              :disabled="filters.page === 0"
              class="flex items-center gap-2 px-4 py-2.5 bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg text-sm font-medium transition-colors disabled:opacity-40 disabled:cursor-not-allowed hover:border-[#E31837] hover:text-[#E31837]"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
              </svg>
              Anterior
            </button>
            <span class="text-[#a0a0a0] text-sm px-2">
              Página <span class="text-white font-semibold">{{ filters.page + 1 }}</span> de <span class="text-white font-semibold">{{ totalPages }}</span>
            </span>
            <button
              @click="nextPage"
              :disabled="filters.page >= totalPages - 1"
              class="flex items-center gap-2 px-4 py-2.5 bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg text-sm font-medium transition-colors disabled:opacity-40 disabled:cursor-not-allowed hover:border-[#E31837] hover:text-[#E31837]"
            >
              Siguiente
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
              </svg>
            </button>
          </div>

        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import ProductCard from '@/components/common/ProductCard.vue'

const route = useRoute()
const router = useRouter()

const categorias = ref([])
const productos = ref([])
const loading = ref(false)
const drawerOpen = ref(false)
const totalPages = ref(0)
const totalElements = ref(0)
const searchInput = ref('')
let searchTimeout = null

const filters = reactive({
  categoriaId: null,
  q: '',
  precioMin: '',
  precioMax: '',
  sort: 'nombre',
  page: 0,
  size: 12
})

const sortOptions = [
  { value: 'nombre', label: 'Nombre A - Z' },
  { value: 'precio', label: 'Precio' }
]

const activeCategoryName = computed(() => {
  if (!filters.categoriaId) return null
  const cat = categorias.value.find(c => c.id === filters.categoriaId)
  return cat ? cat.nombre : null
})

async function fetchCategorias() {
  try {
    const res = await api.get('/public/categorias')
    categorias.value = res.data.data || []
  } catch {
    categorias.value = []
  }
}

async function fetchProductos() {
  loading.value = true
  try {
    const params = { page: filters.page, size: filters.size, sort: filters.sort }
    if (filters.categoriaId) params.categoriaId = filters.categoriaId
    if (filters.q && filters.q.trim()) params.q = filters.q.trim()
    if (filters.precioMin !== '' && filters.precioMin !== null) params.precioMin = filters.precioMin
    if (filters.precioMax !== '' && filters.precioMax !== null) params.precioMax = filters.precioMax
    const res = await api.get('/public/productos', { params })
    const page = res.data.data
    productos.value = page.content || []
    totalPages.value = page.totalPages || 0
    totalElements.value = page.totalElements || 0
  } catch {
    productos.value = []
    totalPages.value = 0
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

function selectCategory(id) {
  filters.categoriaId = id
  filters.page = 0
  drawerOpen.value = false
  syncUrl()
  fetchProductos()
}

function onSortChange() {
  filters.page = 0
  syncUrl()
  fetchProductos()
}

function onSearchInput() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    filters.q = searchInput.value
    filters.page = 0
    fetchProductos()
  }, 400)
}

function clearSearch() {
  searchInput.value = ''
  filters.q = ''
  filters.page = 0
  fetchProductos()
}

function onFilterChange() {
  filters.page = 0
  fetchProductos()
}

function clearPrecio() {
  filters.precioMin = ''
  filters.precioMax = ''
  filters.page = 0
  fetchProductos()
}

function resetFilters() {
  filters.categoriaId = null
  filters.q = ''
  filters.precioMin = ''
  filters.precioMax = ''
  filters.sort = 'nombre'
  filters.page = 0
  searchInput.value = ''
  syncUrl()
  fetchProductos()
}

function prevPage() {
  if (filters.page > 0) {
    filters.page--
    syncUrl()
    fetchProductos()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

function nextPage() {
  if (filters.page < totalPages.value - 1) {
    filters.page++
    syncUrl()
    fetchProductos()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

function syncUrl() {
  const query = {}
  if (filters.categoriaId) {
    const cat = categorias.value.find(c => c.id === filters.categoriaId)
    if (cat) query.categoria = cat.slug
  }
  router.replace({ query })
}

function applyQueryParams() {
  const { categoria } = route.query
  if (categoria && categorias.value.length) {
    const cat = categorias.value.find(c => c.slug === categoria)
    if (cat) filters.categoriaId = cat.id
  }
}

onMounted(async () => {
  await fetchCategorias()
  applyQueryParams()
  fetchProductos()
})
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-left-enter-active, .slide-left-leave-active { transition: transform 0.25s ease; }
.slide-left-enter-from, .slide-left-leave-to { transform: translateX(-100%); }
</style>
