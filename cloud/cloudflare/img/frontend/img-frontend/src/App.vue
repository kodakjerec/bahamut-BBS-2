<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const isUploading = ref(false)
const uploadedFiles = ref<any[]>([])
const myHistory = ref<any[]>([])
const galleryImages = ref<any[]>([])
const offset = ref(0)
const hasMore = ref(true)
const isLoadingGallery = ref(false)
const showHistory = ref(false)
const previewImage = ref<string | null>(null) // 控制圖片放大預覽

// 響應式計算目前應該有幾個欄位
const numCols = ref(4)
const updateCols = () => {
  if (window.innerWidth < 768) numCols.value = 2
  else if (window.innerWidth < 1024) numCols.value = 3
  else numCols.value = 4
}

// 將拿到的圖片依序發牌到不同欄位 (保證由左至右順序)
const columns = computed(() => {
  const cols: any[][] = Array.from({ length: numCols.value }, () => [])
  galleryImages.value.forEach((img, index) => {
    cols[index % numCols.value].push(img)
  })
  return cols
})

const getThumb = (url: string) => {
  const filename = url.split('/').pop()
  return `https://img.kodakjerec.work/cdn-cgi/image/width=300,quality=75/${filename}`
}

const copyUrl = (url: string) => {
  navigator.clipboard.writeText(url)
  alert('✅ 已複製網址：' + url)
}

const handleUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || !files.length) return
  
  isUploading.value = true
  
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    const formData = new FormData()
    formData.append('image', file)
    
    try {
      const res = await fetch('https://img.kodakjerec.work/upload', {
        method: 'POST',
        body: formData
      })
      
      if (res.ok) {
        const data = await res.json()
        const filename = data.url.split('/').pop()
        
        const newItem = { 
          url: data.url, 
          filename: filename, 
          time: Date.now() 
        }

        uploadedFiles.value.unshift(newItem)
        myHistory.value.unshift(newItem)
        localStorage.setItem('myUploads', JSON.stringify(myHistory.value))
        
        galleryImages.value.unshift({
          id: filename,
          url: data.url,
          filename: filename
        })
      }
    } catch (error) {
      console.error('上傳失敗:', error)
      alert('上傳失敗，請檢查網路狀態或圖片大小')
    }
  }
  
  isUploading.value = false
  target.value = ''
}

const loadGallery = async () => {
  if (isLoadingGallery.value || !hasMore.value) return
  isLoadingGallery.value = true
  
  try {
    const res = await fetch(`https://img.kodakjerec.work/list?limit=20&offset=${offset.value}`)
    if (res.ok) {
      const data = await res.json()
      
      if (data.images && data.images.length > 0) {
        const newImages = data.images.map((img: any) => ({
          ...img,
          filename: img.url.split('/').pop()
        }))
        
        galleryImages.value = [...galleryImages.value, ...newImages]
        offset.value = data.next_offset
        hasMore.value = data.has_more
      } else {
        hasMore.value = false
      }
    }
  } catch (error) {
    console.error('取得列表失敗:', error)
  }
  
  isLoadingGallery.value = false
}

const handleScroll = () => {
  const { scrollTop, scrollHeight, clientHeight } = document.documentElement
  if (scrollTop + clientHeight >= scrollHeight - 400) {
    loadGallery()
  }
}

onMounted(() => {
  const historyStr = localStorage.getItem('myUploads')
  if (historyStr) {
    try {
      myHistory.value = JSON.parse(historyStr)
    } catch (e) {
      myHistory.value = []
    }
  }

  updateCols()
  window.addEventListener('resize', updateCols)
  window.addEventListener('scroll', handleScroll)
  
  loadGallery()
})

onUnmounted(() => {
  window.removeEventListener('resize', updateCols)
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="bg-gray-50 dark:bg-gray-950 text-gray-800 dark:text-gray-100 min-h-screen transition-colors duration-300">
    <!-- 頂部：上傳區塊 -->
    <div class="max-w-3xl mx-auto pt-16 px-4 text-center">
      <h1 class="text-4xl font-black text-gray-800 dark:text-gray-100 mb-3 tracking-tight">分享一張圖</h1>
      <p class="text-gray-500 dark:text-gray-400 mb-2 font-medium">拖曳、貼上或點擊上傳。</p>
      <div class="flex flex-wrap justify-center gap-3 mb-10 text-sm font-bold text-gray-600 dark:text-gray-300">
        <span class="bg-gray-100 dark:bg-gray-800 px-3 py-1 rounded-full">📦 單檔 &lt; 20MB</span>
        <span class="bg-gray-100 dark:bg-gray-800 px-3 py-1 rounded-full">🖼️ 僅支援 JPG / PNG / MP4</span>
        <span class="bg-gray-100 dark:bg-gray-800 px-3 py-1 rounded-full">🥷 完全匿名分享 (我也不知道是誰)</span>
      </div>
      
      <label class="block w-full p-16 border-2 border-dashed border-cyan-400 dark:border-cyan-700/50 rounded-3xl bg-white dark:bg-gray-900/50 hover:bg-cyan-50 dark:hover:bg-cyan-900/20 hover:border-cyan-500 dark:hover:border-cyan-500 cursor-pointer transition shadow-sm relative">
        <input type="file" multiple accept="image/*" class="hidden" @change="handleUpload">
        <div class="flex flex-col items-center">
          <div class="bg-cyan-100 dark:bg-cyan-900/50 text-cyan-600 dark:text-cyan-400 p-4 rounded-full mb-4 transition-colors">
            <svg class="w-10 h-10" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
          </div>
          <span class="text-2xl font-bold text-cyan-700 dark:text-cyan-400" v-if="!isUploading">把圖丟進來 或 點擊選擇</span>
          <span class="text-2xl font-bold text-gray-500 dark:text-gray-400" v-else>圖片玩命上傳中...</span>
        </div>
      </label>

      <!-- 本次上傳的網址列表 -->
      <div class="mt-8 text-left" v-if="uploadedFiles.length > 0">
        <h3 class="font-bold text-gray-700 dark:text-gray-300 mb-3 text-lg">剛剛上傳的網址：</h3>
        <div class="bg-white dark:bg-gray-900 p-2 rounded-2xl shadow-sm border border-gray-200 dark:border-gray-800 space-y-1 transition-colors">
          <div v-for="file in uploadedFiles" :key="file.url" class="flex items-center justify-between p-3 hover:bg-gray-50 dark:hover:bg-gray-800/50 rounded-xl transition">
            <a :href="file.url" target="_blank" class="text-cyan-600 dark:text-cyan-400 hover:underline truncate mr-4 font-mono text-sm">{{ file.url }}</a>
            <button @click="copyUrl(file.url)" class="px-4 py-1.5 bg-gray-100 dark:bg-gray-800 hover:bg-cyan-100 dark:hover:bg-cyan-900/50 hover:text-cyan-700 dark:hover:text-cyan-300 font-bold rounded-lg text-sm whitespace-nowrap transition text-gray-700 dark:text-gray-300">複製</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分隔線與首頁畫廊 -->
    <div class="max-w-7xl mx-auto px-4 my-16">
      <div class="flex items-center space-x-4 mb-8">
        <hr class="flex-grow border-gray-300 dark:border-gray-800 transition-colors">
        <h2 class="text-2xl font-black text-gray-400 dark:text-gray-600 transition-colors">最新分享的圖</h2>
        <hr class="flex-grow border-gray-300 dark:border-gray-800 transition-colors">
      </div>
      
      <!-- 瀑布流畫廊 (由左至右發牌) -->
      <div class="flex gap-4 items-start">
        <div v-for="(col, colIndex) in columns" :key="colIndex" class="flex-1 flex flex-col gap-4">
          <div v-for="img in col" :key="img.id + Math.random()" class="bg-white dark:bg-gray-900 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-800 overflow-hidden hover:shadow-lg dark:hover:shadow-cyan-900/10 transition duration-300 group">
            <a href="#" @click.prevent="previewImage = img.url" class="block overflow-hidden cursor-zoom-in">
              <img :src="getThumb(img.url)" loading="lazy" class="w-full object-cover group-hover:scale-105 transition duration-500">
            </a>
            <div class="p-4 flex justify-between items-center bg-white dark:bg-gray-900 transition-colors">
              <span class="text-xs text-gray-500 dark:text-gray-400 font-mono truncate">{{ img.filename }}</span>
              <button @click="copyUrl(img.url)" class="text-gray-400 dark:text-gray-500 hover:text-cyan-600 dark:hover:text-cyan-400 p-2 -mr-2 rounded-lg hover:bg-cyan-50 dark:hover:bg-cyan-900/30 transition" title="複製連結">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path></svg>
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="isLoadingGallery" class="text-center py-10 text-gray-400 dark:text-gray-600 font-bold tracking-widest">
        LOADING...
      </div>
      <div v-if="!hasMore && galleryImages.length > 0" class="text-center py-10 text-gray-300 dark:text-gray-700 font-bold tracking-widest">
        END OF GALLERY
      </div>
    </div>

    <!-- 左下角：我的紀錄按鈕 -->
    <button @click="showHistory = true" class="fixed bottom-6 left-6 bg-cyan-500 hover:bg-cyan-600 dark:bg-cyan-600 dark:hover:bg-cyan-500 text-white rounded-full px-6 py-4 shadow-[0_8px_30px_rgb(6,182,212,0.4)] dark:shadow-[0_8px_30px_rgb(6,182,212,0.2)] flex items-center justify-center transition hover:scale-105 z-40">
      <svg class="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
      <span class="font-bold tracking-wide">我的紀錄</span>
    </button>

    <!-- 我的歷史紀錄 Modal 面板 -->
    <div v-if="showHistory" class="fixed inset-0 z-50 flex justify-start items-end sm:items-center sm:p-6 bg-gray-900 bg-opacity-40 dark:bg-opacity-60 backdrop-blur-sm transition-all" @click.self="showHistory = false">
      <div class="bg-white dark:bg-gray-900 w-full sm:w-[450px] sm:rounded-3xl sm:ml-4 rounded-t-3xl h-[85vh] flex flex-col shadow-2xl slide-up overflow-hidden border border-gray-100 dark:border-gray-800 transition-colors">
        <div class="px-6 py-5 bg-cyan-500 dark:bg-gray-800 text-white flex justify-between items-center shadow-sm z-10 border-b border-cyan-600 dark:border-gray-700 transition-colors">
          <div class="flex items-center space-x-2">
            <svg class="w-6 h-6 text-white dark:text-cyan-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"></path></svg>
            <h3 class="text-xl font-bold tracking-wide text-white">我的上傳</h3>
          </div>
          <button @click="showHistory = false" class="hover:bg-cyan-600 dark:hover:bg-gray-700 p-1.5 rounded-xl transition">
            <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>
        
        <div class="overflow-y-auto flex-1 p-2 bg-gray-50 dark:bg-gray-950 transition-colors">
          <div v-if="myHistory.length === 0" class="text-center text-gray-400 dark:text-gray-600 py-20 font-medium">
            <svg class="w-16 h-16 mx-auto mb-4 text-gray-300 dark:text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 002-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path></svg>
            尚無上傳紀錄
          </div>

          <div class="space-y-2 p-2">
            <div v-for="item in myHistory" :key="item.url + Math.random()" class="flex flex-row items-center space-x-4 p-3 bg-white dark:bg-gray-900 border border-gray-100 dark:border-gray-800 rounded-2xl shadow-sm hover:shadow-md dark:hover:shadow-black/50 transition">
              <a href="#" @click.prevent="previewImage = item.url" class="shrink-0 cursor-zoom-in">
                <img :src="getThumb(item.url)" class="w-16 h-16 object-cover rounded-xl bg-gray-100 dark:bg-gray-800 hover:opacity-80 transition">
              </a>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-bold text-gray-800 dark:text-gray-200 truncate mb-0.5">{{ item.filename }}</p>
                <a :href="item.url" target="_blank" class="text-xs text-gray-400 hover:text-cyan-600 dark:hover:text-cyan-400 hover:underline truncate block font-mono">{{ item.url }}</a>
              </div>
              <button @click="copyUrl(item.url)" class="shrink-0 text-cyan-600 dark:text-cyan-400 hover:text-white bg-cyan-50 dark:bg-cyan-900/30 hover:bg-cyan-500 dark:hover:bg-cyan-600 p-2.5 rounded-xl transition shadow-sm">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path></svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 圖片放大預覽 (Lightbox Modal) -->
    <div v-if="previewImage" class="fixed inset-0 z-[60] flex justify-center items-center bg-gray-900 bg-opacity-80 dark:bg-opacity-90 backdrop-blur-sm p-4 sm:p-10 transition-all" @click="previewImage = null">
      <div class="relative max-w-full max-h-full flex justify-center items-center slide-up">
        
        <!-- 頂部工具列 -->
        <div class="absolute -top-16 left-0 right-0 flex justify-between items-center" @click.stop>
          <div class="flex items-center space-x-3 bg-gray-800/60 dark:bg-gray-900/80 backdrop-blur-md px-4 py-2 rounded-xl border border-gray-700/50 dark:border-gray-600/50 shadow-sm transition-colors">
            <span class="text-gray-200 dark:text-gray-300 font-mono text-sm max-w-[150px] sm:max-w-sm truncate">{{ previewImage?.split('/').pop() }}</span>
            <button @click="copyUrl(previewImage)" class="text-gray-300 dark:text-gray-400 hover:text-white dark:hover:text-cyan-300 bg-gray-700/50 dark:bg-gray-800/50 hover:bg-cyan-500 dark:hover:bg-gray-700 p-1.5 rounded-lg transition" title="複製連結">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path></svg>
            </button>
          </div>
          
          <button @click="previewImage = null" class="text-white hover:text-cyan-400 transition p-2 bg-gray-800/60 dark:bg-gray-900/80 backdrop-blur-md rounded-full border border-gray-700/50 dark:border-gray-600/50 ml-4">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>

        <img :src="previewImage" class="max-w-full max-h-[80vh] object-contain rounded-xl shadow-2xl dark:shadow-black/80" @click.stop>
      </div>
    </div>
  </div>
</template>
