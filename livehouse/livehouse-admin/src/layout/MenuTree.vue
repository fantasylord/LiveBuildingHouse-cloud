<template>
  <template v-for="menu in menus" :key="menu.id">
    <el-sub-menu
      v-if="menu.children && menu.children.length > 0 && menu.menuType === 'menu'"
      :index="menu.path || menu.id + ''"
    >
      <template #title>
        <el-icon v-if="resolveMenuIcon(menu.icon)">
          <component :is="resolveMenuIcon(menu.icon)" />
        </el-icon>
        <span>{{ menu.menuName }}</span>
      </template>
      <menu-tree :menus="menu.children" />
    </el-sub-menu>
    <el-menu-item
      v-else-if="menu.menuType === 'menu'"
      :index="menu.path || menu.id + ''"
    >
      <el-icon v-if="resolveMenuIcon(menu.icon)">
        <component :is="resolveMenuIcon(menu.icon)" />
      </el-icon>
      <span>{{ menu.menuName }}</span>
    </el-menu-item>
  </template>
</template>

<script setup>
import { resolveMenuIcon } from '../utils/icon'

defineProps({
  menus: {
    type: Array,
    default: () => []
  }
})
</script>
