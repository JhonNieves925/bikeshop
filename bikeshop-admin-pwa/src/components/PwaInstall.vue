<template>
  <Transition name="slide-up">
    <div v-if="visible" class="fixed bottom-0 left-0 right-0 z-50 p-3 safe-bottom">
      <div class="max-w-sm mx-auto bg-[#1a1a1a] border border-[#2a2a2a] rounded-2xl p-4 shadow-2xl flex items-center gap-3">
        <img :src="iconSrc" alt="BS Admin" class="w-11 h-11 rounded-xl flex-shrink-0" />

        <div class="flex-1 min-w-0">
          <p class="text-white text-sm font-semibold leading-tight">BikeShop Admin</p>
          <p v-if="!isIos" class="text-[#a0a0a0] text-xs mt-0.5">Instala el panel en tu dispositivo</p>
          <p v-else class="text-[#a0a0a0] text-xs mt-0.5">
            Toca
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#E31837" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="inline align-middle mx-0.5"><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/><polyline points="16 6 12 2 8 6"/><line x1="12" y1="2" x2="12" y2="15"/></svg>
            y <strong class="text-white">"Agregar a inicio"</strong>
          </p>
        </div>

        <button v-if="!isIos" @click="install"
          class="flex-shrink-0 bg-[#E31837] hover:bg-[#c51530] text-white text-xs font-bold px-3 py-2 rounded-xl transition-colors">
          Instalar
        </button>

        <button @click="dismiss" class="flex-shrink-0 text-[#a0a0a0] hover:text-white p-1 transition-colors">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const STORAGE_KEY = 'bikeshop-admin-pwa-dismissed'
const iconSrc = `${import.meta.env.BASE_URL}icons/icon-192.png`

const visible = ref(false)
const isIos = ref(false)
let deferredPrompt = null

function isIosDevice() {
  return /iphone|ipad|ipod/i.test(navigator.userAgent) && !window.MSStream
}

function isInStandaloneMode() {
  return window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone === true
}

function onBeforeInstallPrompt(e) {
  e.preventDefault()
  deferredPrompt = e
  if (!sessionStorage.getItem(STORAGE_KEY)) {
    setTimeout(() => { visible.value = true }, 2000)
  }
}

async function install() {
  if (!deferredPrompt) return
  deferredPrompt.prompt()
  const { outcome } = await deferredPrompt.userChoice
  if (outcome === 'accepted') dismiss()
  deferredPrompt = null
}

function dismiss() {
  visible.value = false
  sessionStorage.setItem(STORAGE_KEY, '1')
}

onMounted(() => {
  if (isInStandaloneMode()) return
  if (isIosDevice()) {
    isIos.value = true
    if (!sessionStorage.getItem(STORAGE_KEY)) setTimeout(() => { visible.value = true }, 2000)
    return
  }
  window.addEventListener('beforeinstallprompt', onBeforeInstallPrompt)
})

onUnmounted(() => {
  window.removeEventListener('beforeinstallprompt', onBeforeInstallPrompt)
})
</script>

<style scoped>
.slide-up-enter-active, .slide-up-leave-active { transition: transform 0.3s ease, opacity 0.3s ease; }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(100%); opacity: 0; }
.safe-bottom { padding-bottom: max(12px, env(safe-area-inset-bottom)); }
</style>
