<template>
  <div class="home-config-list">
    <div class="search-bar">
      <el-button type="success" @click="handleAdd">新增配置</el-button>
    </div>

    <el-table :data="configList" border>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="configKey" label="配置键"></el-table-column>
      <el-table-column prop="configName" label="配置名称"></el-table-column>
      <el-table-column prop="configType" label="类型">
        <template #default="scope">
          <el-tag :type="scope.row.configType === 'json' ? 'primary' : 'success'">
            {{ scope.row.configType === 'json' ? 'JSON' : '文本' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="configValue" label="配置值" show-overflow-tooltip></el-table-column>
      <el-table-column prop="sortOrder" label="排序"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="新增/编辑配置" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" :disabled="!!form.id"></el-input>
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName"></el-input>
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="form.configType">
            <el-option value="json" label="JSON"></el-option>
            <el-option value="text" label="文本"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="6"></el-input>
          <div v-if="form.configType === 'json'" style="color:#999;font-size:12px;margin-top:8px;">
            示例格式（Banner）：[{"id":1,"title":"标题","coverImage":"图片URL","linkType":"house","linkId":1}]
          </div>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input type="number" v-model="form.sortOrder"></el-input>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getHomeConfigListApi, addHomeConfigApi, updateHomeConfigApi, deleteHomeConfigApi, updateHomeConfigStatusApi } from '@/api/user.js'

const configList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  configKey: '',
  configName: '',
  configType: 'json',
  configValue: '',
  sortOrder: 0,
  status: 1
})

const rules = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
}

const loadList = async () => {
  const res = await getHomeConfigListApi()
  if (res.data && res.data.data) {
    configList.value = res.data.data
  }
}

const handleAdd = () => {
  Object.assign(form, { id: null, configKey: '', configName: '', configType: 'json', configValue: '', sortOrder: 0, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该配置吗？', '提示', { type: 'warning' })
  await deleteHomeConfigApi(row.id)
  ElMessage.success('删除成功')
  await loadList()
}

const handleStatusChange = async (row) => {
  await updateHomeConfigStatusApi(row.id, row.status)
  ElMessage.success(row.status === 1 ? '已启用' : '已禁用')
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (form.id) {
    await updateHomeConfigApi(form.id, form)
    ElMessage.success('更新成功')
  } else {
    await addHomeConfigApi(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  await loadList()
}

onMounted(loadList)
</script>