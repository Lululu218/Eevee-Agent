<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import BaseCard from './common/BaseCard.vue'
import BaseTable from './common/BaseTable.vue'
import BaseButton from './common/BaseButton.vue'
import BaseForm from './common/BaseForm.vue'
import FormField from './common/FormField.vue'

const emit = defineEmits(['back'])

const loading = ref(false)
const searchQuery = ref('')

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const sampleList = ref([])

const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteConfirm = ref(false)

const editingItem = ref({})
const itemToDelete = ref(null)

const createForm = reactive({
  title: '',
  description: '',
  question: ''
})

const editForm = reactive({
  title: '',
  description: '',
  question: ''
})

const showMessage = ref(false)
const messageText = ref('')
const messageType = ref('success')

const columns = [
  { key: 'title', title: '标题' },
  { key: 'description', title: '描述' },
  { key: 'question', title: '示例问题' },
  { key: 'createTime', title: '创建时间' },
  { key: 'updateTime', title: '更新时间' },
  { key: 'action', title: '操作', width: '160px' }
]

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).replace(/\//g, '/')
}

const loadSampleList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/example/sample-questions', {
      params: {
        current: pagination.current,
        size: pagination.size,
        keyword: searchQuery.value.trim()
      }
    })
    
    if (response.data.code === '0') {
      const pageData = response.data.data
      sampleList.value = pageData.records.map(item => ({
        id: item.id,
        title: item.title || '-',
        description: item.description || '-',
        question: item.question,
        createTime: formatDate(item.createTime),
        updateTime: formatDate(item.updateTime)
      }))
      pagination.total = pageData.total || 0
    } else {
      showToast('获取示例问题列表失败', 'error')
    }
  } catch (error) {
    console.error('获取示例问题列表失败:', error)
    showToast('获取示例问题列表失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadSampleList()
}

const handleRefresh = () => {
  searchQuery.value = ''
  pagination.current = 1
  loadSampleList()
}

const handlePageChange = (page) => {
  pagination.current = page
  loadSampleList()
}

const handlePageSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadSampleList()
}

const handleCreate = async () => {
  if (!createForm.question.trim()) {
    showToast('示例问题内容不能为空', 'warning')
    return
  }
  
  try {
    const response = await axios.post('/example/sample-questions', {
      title: createForm.title.trim(),
      description: createForm.description.trim(),
      question: createForm.question.trim()
    })
    
    if (response.data.code === '0') {
      showToast('创建成功', 'success')
      showCreateModal.value = false
      resetCreateForm()
      loadSampleList()
    } else {
      showToast(response.data.message || '创建失败', 'error')
    }
  } catch (error) {
    console.error('创建示例问题失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '创建失败'
    showToast(errorMessage, 'error')
  }
}

const handleEdit = (item) => {
  editingItem.value = { ...item }
  editForm.title = item.title === '-' ? '' : item.title
  editForm.description = item.description === '-' ? '' : item.description
  editForm.question = item.question
  showEditModal.value = true
}

const handleUpdate = async () => {
  if (!editForm.question.trim()) {
    showToast('示例问题内容不能为空', 'warning')
    return
  }
  
  try {
    const response = await axios.put(`/example/sample-questions/${editingItem.value.id}`, {
      title: editForm.title.trim(),
      description: editForm.description.trim(),
      question: editForm.question.trim()
    })
    
    if (response.data.code === '0') {
      showToast('更新成功', 'success')
      showEditModal.value = false
      resetEditForm()
      loadSampleList()
    } else {
      showToast(response.data.message || '更新失败', 'error')
    }
  } catch (error) {
    console.error('更新示例问题失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '更新失败'
    showToast(errorMessage, 'error')
  }
}

const handleDeleteClick = (item) => {
  itemToDelete.value = item
  showDeleteConfirm.value = true
}

const handleDeleteConfirm = async () => {
  if (!itemToDelete.value) return
  
  try {
    const response = await axios.delete(`/example/sample-questions/${itemToDelete.value.id}`)
    
    if (response.data.code === '0') {
      showToast('删除成功', 'success')
      showDeleteConfirm.value = false
      itemToDelete.value = null
      loadSampleList()
    } else {
      showToast(response.data.message || '删除失败', 'error')
    }
  } catch (error) {
    console.error('删除示例问题失败:', error)
    const errorMessage = error.response?.data?.message || error.message || '删除失败'
    showToast(errorMessage, 'error')
  }
}

const handleDeleteCancel = () => {
  showDeleteConfirm.value = false
  itemToDelete.value = null
}

const resetCreateForm = () => {
  createForm.title = ''
  createForm.description = ''
  createForm.question = ''
}

const resetEditForm = () => {
  editForm.title = ''
  editForm.description = ''
  editForm.question = ''
  editingItem.value = {}
}

const showToast = (text, type = 'success', duration = 3000) => {
  messageText.value = text
  messageType.value = type
  showMessage.value = true
  
  setTimeout(() => {
    showMessage.value = false
  }, duration)
}

onMounted(() => {
  loadSampleList()
})
</script>

<template>
  <div class="page-content sample-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">示例管理</h1>
      </div>
      <div class="header-right">
        <div class="search-box">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="请输入要搜索的内容"
            @keyup.enter="handleSearch"
          />
          <button class="search-button" @click="handleSearch">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
          </button>
        </div>
        <button class="refresh-button" @click="handleRefresh">
          <svg width="16" height="16" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
            <path d="M524.8 106.666667c-106.666667 0-209.066667 42.666667-285.866667 110.933333l-8.533333-68.266667c0-25.6-21.333333-42.666667-46.933333-38.4-25.6 0-42.666667 21.333333-38.4 46.933334l8.533333 115.2c4.266667 55.466667 51.2 98.133333 106.666667 98.133333h8.533333L384 362.666667c25.6 0 42.666667-21.333333 38.4-46.933334 0-25.6-21.333333-42.666667-46.933333-38.4l-85.333334 4.266667c64-55.466667 145.066667-89.6 230.4-89.6 187.733333 0 341.333333 153.6 341.333334 341.333333s-153.6 341.333333-341.333334 341.333334-341.333333-153.6-341.333333-341.333334c0-25.6-17.066667-42.666667-42.666667-42.666666s-42.666667 17.066667-42.666666 42.666666c0 234.666667 192 426.666667 426.666666 426.666667s426.666667-192 426.666667-426.666667c4.266667-234.666667-187.733333-426.666667-422.4-426.666666z" fill="currentColor"/>
          </svg>
          刷新
        </button>
        <BaseButton type="primary" @click="showCreateModal = true">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          新增示例
        </BaseButton>
      </div>
    </div>

    <!-- 示例列表 -->
    <div class="sample-list">
      <BaseTable
        :columns="columns"
        :data="sampleList"
        :loading="loading"
        :total="pagination.total"
        :pageSize="pagination.size"
        :currentPage="pagination.current"
        :showPagination="true"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #action="{ row }">
          <div class="table-actions">
            <BaseButton type="primary" size="small" @click="handleEdit(row)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
            </BaseButton>
            <BaseButton type="danger" size="small" @click="handleDeleteClick(row)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                <line x1="10" y1="11" x2="10" y2="17"/>
                <line x1="14" y1="11" x2="14" y2="17"/>
              </svg>
            </BaseButton>
          </div>
        </template>
      </BaseTable>

      <div v-if="sampleList.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无示例问题</p>
      </div>
    </div>

    <!-- 消息提示 -->
    <div v-if="showMessage" :class="['toast-message', messageType]">
      <div class="toast-content">
        <svg v-if="messageType === 'success'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="20 6 9 17 4 12"/>
        </svg>
        <svg v-else-if="messageType === 'error'" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <line x1="15" y1="9" x2="9" y2="15"/>
          <line x1="9" y1="9" x2="15" y2="15"/>
        </svg>
        <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="16" x2="12" y2="12"/>
          <line x1="12" y1="8" x2="12.01" y2="8"/>
        </svg>
        <span>{{ messageText }}</span>
      </div>
    </div>

    <!-- 新建示例弹窗 -->
    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">新建示例问题</h3>
          <BaseButton type="secondary" size="small" @click="showCreateModal = false" class="close-button">&times;</BaseButton>
        </div>
        <div class="modal-body">
          <p class="modal-subtitle">创建新的示例问题，用于训练和测试系统</p>
          <div class="form-group">
            <label class="form-label">标题 <span class="optional">(可选)</span></label>
            <input
              v-model="createForm.title"
              type="text"
              placeholder="请输入标题"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label class="form-label">描述 <span class="optional">(可选)</span></label>
            <textarea
              v-model="createForm.description"
              placeholder="请输入描述"
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">示例问题 <span class="required">*</span></label>
            <textarea
              v-model="createForm.question"
              placeholder="请输入示例问题内容"
              class="form-textarea"
              rows="4"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <BaseButton type="secondary" @click="showCreateModal = false">取消</BaseButton>
          <BaseButton type="primary" @click="handleCreate">创建</BaseButton>
        </div>
      </div>
    </div>

    <!-- 编辑示例弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3 class="modal-title">编辑示例问题</h3>
          <BaseButton type="secondary" size="small" @click="showEditModal = false" class="close-button">&times;</BaseButton>
        </div>
        <div class="modal-body">
          <p class="modal-subtitle">修改示例问题的内容和描述</p>
          <div class="form-group">
            <label class="form-label">标题 <span class="optional">(可选)</span></label>
            <input
              v-model="editForm.title"
              type="text"
              placeholder="请输入标题"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label class="form-label">描述 <span class="optional">(可选)</span></label>
            <textarea
              v-model="editForm.description"
              placeholder="请输入描述"
              class="form-textarea"
              rows="3"
            ></textarea>
          </div>
          <div class="form-group">
            <label class="form-label">示例问题 <span class="required">*</span></label>
            <textarea
              v-model="editForm.question"
              placeholder="请输入示例问题内容"
              class="form-textarea"
              rows="4"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <BaseButton type="secondary" @click="showEditModal = false">取消</BaseButton>
          <BaseButton type="primary" @click="handleUpdate">保存</BaseButton>
        </div>
      </div>
    </div>

    <!-- 删除确认弹窗 -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="handleDeleteCancel">
      <div class="modal delete-modal">
        <div class="modal-header">
          <h3 class="modal-title">确认删除</h3>
          <BaseButton type="secondary" size="small" @click="handleDeleteCancel" class="close-button">&times;</BaseButton>
        </div>
        <div class="modal-body">
          <div class="delete-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ff4d4f" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>
          <p class="delete-message">确定要删除示例问题吗？</p>
          <p class="delete-hint">此操作不可撤销，删除后相关数据将被永久清除。</p>
        </div>
        <div class="modal-footer">
          <BaseButton type="secondary" @click="handleDeleteCancel">取消</BaseButton>
          <BaseButton type="danger" @click="handleDeleteConfirm">删除</BaseButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sample-page {
  padding: 24px;
  min-height: calc(100vh - 80px);
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  justify-content: flex-end;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0;
  background-color: white;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  width: 280px;
  transition: all 0.2s ease;
  overflow: hidden;
}

.search-box:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.search-box input {
  border: none;
  outline: none;
  background: white;
  font-size: 14px;
  color: #333;
  width: 100%;
  padding: 10px 12px;
}

.search-box input::placeholder {
  color: #999;
}

.search-button {
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: none;
  border-left: 1px solid #e8e8e8;
  color: #999;
  cursor: pointer;
  padding: 10px 12px;
  transition: all 0.3s;
}

.search-button:hover {
  color: #667eea;
  background-color: #fafbff;
}

.refresh-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background-color: white;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  color: #666;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}

.refresh-button:hover {
  background-color: #fafbff;
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.sample-list {
  overflow: visible;
}

.sample-list table {
  transition: all 0.3s ease;
}

.sample-list tr {
  transition: all 0.3s ease;
}

.sample-list tr:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.1);
}

.sample-list tr:hover td {
  background-color: #fafbff;
  color: #333;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.empty-icon {
  margin-bottom: 16px;
  color: #d9d9d9;
  font-size: 48px;
}

.toast-message {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  animation: slideDown 0.3s ease-out;
}

.toast-message.success {
  background-color: #f0fdf4;
  border: 1px solid #dcfce7;
  color: #166534;
}

.toast-message.error {
  background-color: #fef2f2;
  border: 1px solid #fee2e2;
  color: #b91c1c;
}

.toast-message.warning {
  background-color: #fffbeb;
  border: 1px solid #fef3c7;
  color: #92400e;
}

.toast-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translate(-50%, -20px);
  }
  to {
    opacity: 1;
    transform: translate(-50%, 0);
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafafa;
}

.modal-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.close-button {
  padding: 4px;
  min-width: auto;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  max-height: 400px;
}

.modal-footer {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.optional {
  color: #999;
  font-weight: 400;
  font-size: 12px;
}

.required {
  color: #ff4d4f;
  font-weight: 400;
  font-size: 12px;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
  box-sizing: border-box;
}

.form-textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

@media (max-width: 768px) {
  .sample-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-box {
    width: 100%;
    max-width: 100%;
  }

  .refresh-button {
    align-self: flex-start;
  }

  .modal {
    width: 95%;
    max-height: 90vh;
  }
}
</style>
