<template>
  <div class="user-list">
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="请输入用户名/姓名/手机号" class="search-input"></el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增用户</el-button>
    </div>
    <el-table :data="userList" border>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="username" label="用户名"></el-table-column>
      <el-table-column prop="realName" label="真实姓名"></el-table-column>
      <el-table-column prop="phone" label="手机号"></el-table-column>
      <el-table-column prop="email" label="邮箱"></el-table-column>
      <el-table-column prop="roleName" label="角色"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间"></el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" :disabled="scope.row.id === SYSTEM_CONSTANTS.SUPER_ADMIN_USER_ID" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" :disabled="scope.row.id === SYSTEM_CONSTANTS.SUPER_ADMIN_USER_ID" @click="handleDelete(scope.row)">删除</el-button>
          <el-button size="small" :disabled="scope.row.id === SYSTEM_CONSTANTS.SUPER_ADMIN_USER_ID" @click="handleResetPwd(scope.row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.pageNum"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
    ></el-pagination>

    <el-dialog title="新增/编辑用户" v-model="dialogVisible" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId">
            <el-option v-for="role in roleList" :key="role.id" :label="role.roleName" :value="role.id"></el-option>
          </el-select>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserPageApi, addUserApi, updateUserApi, deleteUserApi, resetPasswordApi, getRoleListApi } from '../../api/user'
import { SYSTEM_CONSTANTS } from '../../utils/constants'

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const userList = ref([])
const roleList = ref([])

const dialogVisible = ref(false)
const form = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  roleId: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const loadUserList = async () => {
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword
    }
    const response = await getUserPageApi(params)
    if (response.data.code === 200) {
      userList.value = response.data.data.records
      pagination.total = response.data.data.total
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

const loadRoleList = async () => {
  try {
    const response = await getRoleListApi()
    if (response.data.code === 200) {
      roleList.value = response.data.data
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const handleSearch = () => {
  loadUserList()
}

const handleAdd = () => {
  form.id = null
  form.username = ''
  form.realName = ''
  form.phone = ''
  form.email = ''
  form.roleId = ''
  form.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.phone = row.phone
  form.email = row.email
  form.roleId = row.roleId
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await updateUserApi(form)
      alert('更新成功')
    } else {
      await addUserApi(form)
      alert('新增成功')
    }
    dialogVisible.value = false
    loadUserList()
  } catch (error) {
    console.error('操作失败:', error)
    alert('操作失败')
  }
}

const handleDelete = async (row) => {
  if (confirm(`确定删除用户 "${row.realName}" 吗？`)) {
    try {
      await deleteUserApi(row.id)
      alert('删除成功')
      loadUserList()
    } catch (error) {
      console.error('删除失败:', error)
      alert('删除失败')
    }
  }
}

const handleStatusChange = async (row) => {
  const oldStatus = row.status
  try {
    await updateUserApi({ ...row, status: row.status })
  } catch (error) {
    console.error('更新用户状态失败:', error)
    alert('更新失败')
    // 回滚状态
    row.status = oldStatus
  }
}

const handleResetPwd = async (row) => {
  if (confirm(`确定重置 "${row.realName}" 的密码吗？`)) {
    try {
      await resetPasswordApi(row.id)
      alert('密码已重置为: 123456')
    } catch (error) {
      console.error('重置密码失败:', error)
      alert('重置密码失败')
    }
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadUserList()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  loadUserList()
}

onMounted(() => {
  loadUserList()
  loadRoleList()
})
</script>

<style>
.user-list {
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