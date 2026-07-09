<template>
  <div v-if="!resolvedSrc" class="image-placeholder" :class="className" :style="computedStyle">
    <van-icon name="play-circle-o" size="32" color="#ccc" />
    <span class="placeholder-text">暂无封面</span>
  </div>
  <img
    v-else
    :src="resolvedSrc"
    :alt="alt"
    :class="className"
    :style="computedStyle"
    @error="handleError"
  />
</template>

<script setup>
import { computed } from 'vue'
import { Icon as VanIcon } from 'vant'

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  alt: {
    type: String,
    default: ''
  },
  className: {
    type: String,
    default: ''
  },
  width: {
    type: [String, Number],
    default: ''
  },
  height: {
    type: [String, Number],
    default: ''
  },
  mode: {
    type: String,
    default: 'cover',
    validator: (value) => ['cover', 'contain', 'fill', 'none', 'scale-down'].includes(value)
  },
  fallback: {
    type: String,
    default: ''
  }
})

const BASE_URL = ''

const resolvedSrc = computed(() => {
  if (!props.src) {
    return props.fallback || ''
  }
  
  if (props.src.startsWith('http://') || props.src.startsWith('https://')) {
    return props.src
  }
  
  if (props.src.startsWith('/')) {
    return BASE_URL + props.src
  }
  
  return BASE_URL + '/' + props.src
})

const computedStyle = computed(() => {
  const style = {}
  if (props.width) {
    style.width = typeof props.width === 'number' ? `${props.width}px` : props.width
  }
  if (props.height) {
    style.height = typeof props.height === 'number' ? `${props.height}px` : props.height
  }
  switch (props.mode) {
    case 'cover':
      style.objectFit = 'cover'
      break
    case 'contain':
      style.objectFit = 'contain'
      break
    case 'fill':
      style.objectFit = 'fill'
      break
    case 'none':
      style.objectFit = 'none'
      break
    case 'scale-down':
      style.objectFit = 'scale-down'
      break
  }
  return style
})

const handleError = (e) => {
  if (props.fallback) {
    e.target.src = props.fallback
  }
}
</script>
