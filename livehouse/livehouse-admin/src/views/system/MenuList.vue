<template>
  <div class="menu-list">
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="请输入菜单名称" class="search-input"></el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增菜单</el-button>
    </div>
    <el-table :data="menuList" border>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="parentName" label="上级菜单"></el-table-column>
      <el-table-column prop="menuName" label="菜单名称"></el-table-column>
      <el-table-column prop="menuType" label="类型:图标">
        <template #default="scope">
          <el-tag :type="scope.row.menuType === 'menu' ? 'primary' : 'success'">
            {{ scope.row.menuType === 'menu' ? '菜单' : '按钮' }}
          </el-tag>
          <span class="menu-icon-preview" v-if="resolveMenuIcon(scope.row.icon)">
            <el-icon>
              <component :is="resolveMenuIcon(scope.row.icon)" />
            </el-icon>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由路径"></el-table-column>
      <el-table-column prop="component" label="组件路径"></el-table-column>
      <el-table-column prop="permission" label="权限标识"></el-table-column>
      <el-table-column prop="sortOrder" label="排序"></el-table-column>
      <el-table-column prop="visible" label="显示">
        <template #default="scope">
          <el-switch v-model="scope.row.visible" :active-value="1" :inactive-value="0" @change="handleVisibleChange(scope.row)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑菜单弹窗 -->
    <el-dialog title="新增/编辑菜单" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-select v-model="form.parentId">
            <el-option :value="0" label="顶级菜单"></el-option>
            <el-option v-for="menu in parentMenuOptions" :key="menu.id" :label="menu.menuName" :value="menu.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName"></el-input>
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-select v-model="form.menuType">
            <el-option value="menu" label="菜单"></el-option>
            <el-option value="button" label="按钮"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path"></el-input>
        </el-form-item>
        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component"></el-input>
        </el-form-item>
        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="form.permission"></el-input>
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-select v-model="form.icon" filterable clearable placeholder="请选择图标">
            <el-option
              v-for="option in iconOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            >
              <span class="icon-option">
                <el-icon>
                  <component :is="resolveMenuIcon(option.value)" />
                </el-icon>
                <span>{{ option.label }}</span>
              </span>
            </el-option>
          </el-select>
          <div class="selected-icon-preview" v-if="resolveMenuIcon(form.icon)">
            <el-icon>
              <component :is="resolveMenuIcon(form.icon)" />
            </el-icon>
            <span>{{ form.icon }}</span>
          </div>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input type="number" v-model="form.sortOrder"></el-input>
        </el-form-item>
        <el-form-item label="显示" prop="visible">
          <el-switch v-model="form.visible" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, inject } from 'vue'
import {
  getMenuListApi,
  addMenuApi,
  updateMenuApi,
  deleteMenuApi
} from '../../api/user'
import { menuIconOptions, resolveMenuIcon } from '../../utils/icon'

const refreshMenus = inject('refreshMenus', () => {})

const searchForm = reactive({
  keyword: ''
})

const menuList = ref([])

const dialogVisible = ref(false)

const form = reactive({
  id: null,
  parentId: 0,
  menuName: '',
  menuType: 'menu',
  path: '',
  component: '',
  permission: '',
  icon: '',
  sortOrder: 0,
  visible: 1
})

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

const parentMenuOptions = computed(() => {
  return menuList.value.filter(menu => menu.id !== form.id)
})

const iconOptions = menuIconOptions

const loadMenuList = async () => {
  try {
    const response = await getMenuListApi(searchForm.keyword)
    if (response.data.code === 200) {
      const menus = response.data.data
      // 添加上级菜单名称
      const menuMap = new Map(menus.map(m => [m.id, m.menuName]))
      menuList.value = menus.map(menu => ({
        ...menu,
        parentName: menu.parentId === 0 ? '顶级菜单' : menuMap.get(menu.parentId) || '未知'
      }))
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error)
  }
}

const handleSearch = () => {
  loadMenuList()
}

const handleAdd = () => {
  form.id = null
  form.parentId = 0
  form.menuName = ''
  form.menuType = 'menu'
  form.path = ''
  form.component = ''
  form.permission = ''
  form.icon = ''
  form.sortOrder = 99
  form.visible = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.parentId = row.parentId || 0
  form.menuName = row.menuName
  form.menuType = row.menuType
  form.path = row.path || ''
  form.component = row.component || ''
  form.permission = row.permission || ''
  form.icon = row.icon || ''
  form.sortOrder = row.sortOrder ?? 0
  form.visible = row.visible || 1
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await updateMenuApi(form)
      alert('更新成功')
    } else {
      await addMenuApi(form)
      alert('新增成功')
    }
    dialogVisible.value = false
    loadMenuList()
    // 刷新菜单缓存
    refreshMenus()
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败')
  }
}

const handleDelete = async (row) => {
  // 检查是否有子菜单
  const hasChildren = menuList.value.some(m => m.parentId === row.id)
  if (hasChildren) {
    alert('请先删除子菜单')
    return
  }
  
  if (confirm(`确定删除菜单 "${row.menuName}" 吗？`)) {
    try {
      await deleteMenuApi(row.id)
      alert('删除成功')
      loadMenuList()
    } catch (error) {
      console.error('删除失败:', error)
      alert('删除失败')
    }
  }
}

const handleVisibleChange = async (row) => {
  const oldVisible = row.visible
  try {
    await updateMenuApi({ ...row, visible: row.visible })
  } catch (error) {
    console.error('更新菜单显示状态失败:', error)
    alert('更新失败')
    // 回滚状态
    row.visible = oldVisible
  }
}

onMounted(() => {
  loadMenuList()
})
</script>

<style>
.menu-list {
  padding: 20px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  width: 300px;
}

.menu-icon-preview {
  display: inline-flex;
  align-items: center;
  margin-left: 10px;
  color: var(--el-color-primary-6);
}

.icon-option {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.selected-icon-preview {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  color: var(--el-text-color-secondary);
}

.selected-icon-preview .el-icon {
  font-size: 16px;
}
</style>
