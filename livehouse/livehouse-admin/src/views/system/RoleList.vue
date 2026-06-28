<template>
  <div class="role-list">
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="请输入角色名称" class="search-input"></el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增角色</el-button>
    </div>
    <el-table :data="roleList" border>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="roleName" label="角色名称"></el-table-column>
      <el-table-column prop="roleCode" label="角色编码"></el-table-column>
      <el-table-column prop="description" label="角色描述"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间"></el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" :disabled="scope.row.id === SYSTEM_CONSTANTS.SUPER_ADMIN_ROLE_ID" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" :disabled="scope.row.id === SYSTEM_CONSTANTS.SUPER_ADMIN_ROLE_ID" @click="handleDelete(scope.row)">删除</el-button>
          <el-button size="small" type="warning" :disabled="scope.row.id === SYSTEM_CONSTANTS.SUPER_ADMIN_ROLE_ID" @click="handleAssignMenu(scope.row)">分配菜单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog title="新增/编辑角色" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName"></el-input>
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode"></el-input>
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input type="textarea" v-model="form.description"></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单弹窗 -->
    <el-dialog title="分配菜单权限" v-model="menuDialogVisible" width="600px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTreeData"
        :props="treeProps"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedMenuIds"
      ></el-tree>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleMenuSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, inject } from 'vue'
import {
  getRoleListApi,
  addRoleApi,
  updateRoleApi,
  deleteRoleApi,
  getMenuTreeApi,
  getRoleMenusApi,
  assignMenuApi
} from '../../api/user'
import { SYSTEM_CONSTANTS } from '../../utils/constants'

const refreshMenus = inject('refreshMenus', () => {})

const searchForm = reactive({
  keyword: ''
})

const roleList = ref([])
const menuTreeData = ref([])
const menuTreeRef = ref(null)

const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const currentRoleId = ref(null)
const checkedMenuIds = ref([])

const form = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const treeProps = {
  label: 'menuName',
  children: 'children'
}

const loadRoleList = async () => {
  try {
    const response = await getRoleListApi(searchForm.keyword)
    if (response.data.code === 200) {
      roleList.value = response.data.data
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const loadMenuTree = async () => {
  try {
    const response = await getMenuTreeApi()
    if (response.data.code === 200) {
      menuTreeData.value = response.data.data
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error)
  }
}

const handleSearch = () => {
  loadRoleList()
}

const handleAdd = () => {
  form.id = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.roleName = row.roleName
  form.roleCode = row.roleCode
  form.description = row.description
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await updateRoleApi(form)
      alert('更新成功')
    } else {
      await addRoleApi(form)
      alert('新增成功')
    }
    dialogVisible.value = false
    loadRoleList()
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败')
  }
}

const handleDelete = async (row) => {
  if (confirm(`确定删除角色 "${row.roleName}" 吗？`)) {
    try {
      await deleteRoleApi(row.id)
      alert('删除成功')
      loadRoleList()
    } catch (error) {
      console.error('删除失败:', error)
      alert('删除失败')
    }
  }
}

const handleStatusChange = async (row) => {
  const oldStatus = row.status
  try {
    await updateRoleApi({ ...row, status: row.status })
  } catch (error) {
    console.error('更新角色状态失败:', error)
    alert('更新失败')
    // 回滚状态
    row.status = oldStatus
  }
}

const handleAssignMenu = async (row) => {
  currentRoleId.value = row.id
  // 获取角色已分配的菜单
  try {
    const response = await getRoleMenusApi(row.id)
    if (response.data.code === 200) {
      checkedMenuIds.value = response.data.data
    }
    await loadMenuTree()
    menuDialogVisible.value = true
  } catch (error) {
    console.error('获取角色菜单失败:', error)
  }
}

const handleMenuSubmit = async () => {
  // 使用 el-tree 的 getCheckedKeys 方法获取选中的菜单ID
  const selectedKeys = menuTreeRef.value.getCheckedKeys()

  try {
    await assignMenuApi(currentRoleId.value, selectedKeys)
    alert('分配成功')
    menuDialogVisible.value = false
    // 刷新菜单缓存
    refreshMenus()
  } catch (error) {
    console.error('分配菜单失败:', error)
    alert('分配失败')
  }
}

onMounted(() => {
  loadRoleList()
})
</script>

<style>
.role-list {
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
</style>