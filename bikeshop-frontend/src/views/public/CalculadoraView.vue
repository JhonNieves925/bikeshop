<template>
  <div class="min-h-screen bg-[#0a0a0a] text-white py-12 px-4">
    <div class="max-w-2xl mx-auto">

      <!-- Header -->
      <RouterLink to="/" class="inline-flex items-center gap-2 text-[#a0a0a0] hover:text-white text-sm transition-colors mb-8 group">
        <svg class="w-4 h-4 group-hover:-translate-x-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        Volver al inicio
      </RouterLink>

      <div class="text-center mb-10">
        <p class="text-[#E31837] text-xs font-semibold tracking-[0.3em] uppercase mb-2">Herramienta gratuita</p>
        <h1 class="text-4xl font-black mb-3">CALCULADORA DE TALLA</h1>
        <p class="text-[#a0a0a0] max-w-md mx-auto">Ingresa tu estatura y el tipo de bicicleta para encontrar la talla perfecta.</p>
      </div>

      <div class="bg-[#141414] border border-[#2a2a2a] rounded-2xl p-8 space-y-8">

        <!-- Tipo de bici -->
        <div>
          <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-3">Tipo de bicicleta</p>
          <div class="grid grid-cols-3 gap-3">
            <button v-for="t in tipos" :key="t.id" @click="tipo = t.id"
              class="flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition-all"
              :class="tipo === t.id
                ? 'border-[#E31837] bg-[#E31837]/10 text-white'
                : 'border-[#2a2a2a] text-[#a0a0a0] hover:border-[#555] hover:text-white'">
              <span class="text-3xl">{{ t.emoji }}</span>
              <span class="text-xs font-semibold">{{ t.label }}</span>
            </button>
          </div>
        </div>

        <!-- Estatura -->
        <div>
          <div class="flex items-center justify-between mb-3">
            <p class="text-xs text-[#a0a0a0] uppercase tracking-wider">Tu estatura</p>
            <div class="flex items-center gap-2 bg-[#0a0a0a] border border-[#2a2a2a] rounded-lg px-3 py-1.5">
              <input v-model.number="estatura" type="number" min="140" max="220"
                class="w-14 bg-transparent text-white text-center text-sm font-bold focus:outline-none"
                @input="clampEstatura" />
              <span class="text-[#a0a0a0] text-xs">cm</span>
            </div>
          </div>
          <input v-model.number="estatura" type="range" min="140" max="220" step="1"
            class="w-full accent-[#E31837] cursor-pointer" />
          <div class="flex justify-between text-xs text-[#555] mt-1">
            <span>140 cm</span><span>180 cm</span><span>220 cm</span>
          </div>
        </div>

        <!-- Resultado -->
        <Transition name="slide-up" mode="out-in">
          <div v-if="resultado" :key="resultado.talla" class="border-t border-[#2a2a2a] pt-6">

            <!-- Talla principal -->
            <div class="flex items-center justify-between mb-6 flex-wrap gap-4">
              <div>
                <p class="text-[#a0a0a0] text-xs uppercase tracking-wider mb-1">Talla recomendada</p>
                <div class="flex items-baseline gap-3">
                  <span class="text-6xl font-black text-[#E31837]">{{ resultado.talla }}</span>
                  <span class="text-white font-semibold text-lg">{{ resultado.marco }}</span>
                </div>
              </div>
              <div class="text-right">
                <p class="text-[#a0a0a0] text-xs uppercase tracking-wider mb-1">Rango de estatura</p>
                <p class="text-white font-bold">{{ resultado.rango }}</p>
              </div>
            </div>

            <!-- Barra de tallas -->
            <div class="mb-6">
              <p class="text-xs text-[#a0a0a0] uppercase tracking-wider mb-3">Guía de tallas</p>
              <div class="flex gap-1">
                <div v-for="s in tallasTipo" :key="s.talla"
                  class="flex-1 rounded-lg py-2 text-center text-xs font-bold transition-all"
                  :class="s.talla === resultado.talla
                    ? 'bg-[#E31837] text-white scale-105 shadow-lg shadow-[#E31837]/20'
                    : 'bg-[#2a2a2a] text-[#555]'">
                  {{ s.talla }}
                </div>
              </div>
            </div>

            <!-- Info adicional -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-6">
              <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-4">
                <p class="text-[#a0a0a0] text-xs mb-1">{{ tipoActual.label }}</p>
                <p class="text-white font-semibold text-sm">{{ resultado.marco }}</p>
              </div>
              <div class="bg-[#1a1a1a] border border-[#2a2a2a] rounded-xl p-4">
                <p class="text-[#a0a0a0] text-xs mb-1">Altura del sillín</p>
                <p class="text-white font-semibold text-sm">{{ resultado.sillin }}</p>
              </div>
            </div>

            <!-- Consejo -->
            <div class="bg-yellow-500/10 border border-yellow-500/20 rounded-xl p-4 mb-6">
              <p class="text-yellow-300 text-xs font-semibold mb-1">💡 Consejo</p>
              <p class="text-yellow-200/80 text-sm">{{ resultado.consejo }}</p>
            </div>

            <!-- CTA -->
            <RouterLink to="/catalogo"
              class="block w-full text-center bg-[#E31837] hover:bg-[#b5112a] text-white py-3.5 rounded-xl font-bold transition-colors">
              Ver bicicletas en talla {{ resultado.talla }} →
            </RouterLink>
          </div>
        </Transition>
      </div>

      <!-- Disclaimer -->
      <p class="text-center text-[#555] text-xs mt-6 leading-relaxed">
        Esta calculadora es una guía general. La talla ideal puede variar según la proporción de tus piernas,
        tu postura y preferencias de manejo. Te recomendamos probar la bici antes de comprar.
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { RouterLink } from 'vue-router'

const tipo     = ref('MTB')
const estatura = ref(170)

function clampEstatura() {
  if (estatura.value < 140) estatura.value = 140
  if (estatura.value > 220) estatura.value = 220
}

const tipos = [
  { id: 'MTB',   label: 'Montaña',  emoji: '🏔️' },
  { id: 'RUTA',  label: 'Ruta',     emoji: '🛣️' },
  { id: 'URBAN', label: 'Urbana',   emoji: '🏙️' },
]

// Tablas de tallas por tipo
const tablas = {
  MTB: [
    { talla: 'XS', rango: '148–158 cm', marco: '13–14 pulgadas', sillinMin: 62, sillinMax: 67 },
    { talla: 'S',  rango: '158–168 cm', marco: '15–16 pulgadas', sillinMin: 67, sillinMax: 72 },
    { talla: 'M',  rango: '168–178 cm', marco: '17–18 pulgadas', sillinMin: 72, sillinMax: 77 },
    { talla: 'L',  rango: '178–188 cm', marco: '19–20 pulgadas', sillinMin: 77, sillinMax: 82 },
    { talla: 'XL', rango: '188–198 cm', marco: '21–22 pulgadas', sillinMin: 82, sillinMax: 87 },
    { talla: 'XXL',rango: '198+ cm',    marco: '23+ pulgadas',   sillinMin: 87, sillinMax: 94 },
  ],
  RUTA: [
    { talla: 'XS', rango: '148–158 cm', marco: '47–50 cm',  sillinMin: 64, sillinMax: 69 },
    { talla: 'S',  rango: '158–168 cm', marco: '51–53 cm',  sillinMin: 69, sillinMax: 74 },
    { talla: 'M',  rango: '168–175 cm', marco: '54–56 cm',  sillinMin: 74, sillinMax: 78 },
    { talla: 'L',  rango: '175–183 cm', marco: '57–59 cm',  sillinMin: 78, sillinMax: 83 },
    { talla: 'XL', rango: '183–190 cm', marco: '60–62 cm',  sillinMin: 83, sillinMax: 88 },
    { talla: 'XXL',rango: '190+ cm',    marco: '63+ cm',    sillinMin: 88, sillinMax: 95 },
  ],
  URBAN: [
    { talla: 'XS', rango: '148–158 cm', marco: '14–15 pulgadas', sillinMin: 60, sillinMax: 66 },
    { talla: 'S',  rango: '158–168 cm', marco: '16–17 pulgadas', sillinMin: 66, sillinMax: 72 },
    { talla: 'M',  rango: '168–178 cm', marco: '18–19 pulgadas', sillinMin: 72, sillinMax: 77 },
    { talla: 'L',  rango: '178–188 cm', marco: '20–21 pulgadas', sillinMin: 77, sillinMax: 82 },
    { talla: 'XL', rango: '188+ cm',    marco: '22+ pulgadas',   sillinMin: 82, sillinMax: 90 },
  ],
}

const consejos = {
  MTB:   'Para montaña se prefiere un marco ligeramente más pequeño: da más control en bajadas técnicas y saltos.',
  RUTA:  'En bicicletas de ruta es crucial la longitud del manubrio. Si tienes brazos largos, considera un marco un talla más grande.',
  URBAN: 'Para ciudad prioriza la comodidad al montar y desmontar. Un marco ligeramente más pequeño facilita el manejo urbano.',
}

const tallasTipo = computed(() => tablas[tipo.value] || tablas.MTB)
const tipoActual = computed(() => tipos.find(t => t.id === tipo.value))

const resultado = computed(() => {
  const tabla = tallasTipo.value
  const h = estatura.value
  // Encontrar la talla por rango
  let found = tabla[tabla.length - 1] // default mayor
  for (const row of tabla) {
    const [min, max] = row.rango.replace(' cm', '').split('–').map(n => parseInt(n))
    if (h >= min && (isNaN(max) || h < max)) { found = row; break }
  }
  const sillinMid = Math.round((found.sillinMin + found.sillinMax) / 2)
  return {
    ...found,
    sillin: `${found.sillinMin}–${found.sillinMax} cm (aprox. ${sillinMid} cm)`,
    consejo: consejos[tipo.value],
  }
})
</script>

<style scoped>
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s ease; }
.slide-up-enter-from { opacity: 0; transform: translateY(10px); }
.slide-up-leave-to   { opacity: 0; transform: translateY(-10px); }
</style>
