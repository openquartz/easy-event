<template>
  <div>
    <el-card>
      <el-form :inline="true" :model="query" class="demo-form-inline">
        <el-form-item label="Event Key">
          <el-input v-model="query.eventKey" placeholder="Event Key" />
        </el-form-item>
        <el-form-item label="State">
          <el-select v-model="query.processingState" placeholder="State" clearable>
            <el-option label="AVAILABLE" value="AVAILABLE" />
            <el-option label="PROCESS_COMPLETE" value="PROCESS_COMPLETE" />
            <el-option label="PROCESS_FAILED" value="PROCESS_FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="className" label="Class Name" width="250" show-overflow-tooltip />
        <el-table-column prop="eventKey" label="Key" width="150" />
        <el-table-column prop="processingState" label="State" width="150">
          <template #default="{ row }">
            <el-tag :type="getStateType(row.processingState)">{{ row.processingState }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" label="Errors" width="80" />
        <el-table-column prop="createdTime" label="Created Time" width="180" />
        <el-table-column label="Operations" width="150">
          <template #default="{ row }">
            <el-button
              v-if="row.processingState === 'PROCESS_FAILED'"
              size="small"
              type="danger"
              @click="handleRetry(row)"
            >
              Retry
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getEvents, retryEvents, type BusEvent, type EventQuery } from '@/api/event'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<BusEvent[]>([])
const total = ref(0)

const query = reactive<EventQuery>({
  page: 1,
  size: 20,
  eventKey: '',
  processingState: ''
})

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getEvents(query)
    tableData.value = res.records
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  query.eventKey = ''
  query.processingState = ''
  handleSearch()
}

const handleRetry = (row: BusEvent) => {
  ElMessageBox.confirm(`Are you sure to retry event ${row.id}?`, 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning'
  }).then(async () => {
    try {
      await retryEvents([row.id])
      ElMessage.success('Retry triggered successfully')
      handleSearch()
    } catch (e) {
      console.error(e)
    }
  })
}

const getStateType = (state: string) => {
  switch (state) {
    case 'PROCESS_COMPLETE':
      return 'success'
    case 'PROCESS_FAILED':
      return 'danger'
    case 'AVAILABLE':
      return 'primary'
    default:
      return 'info'
  }
}

onMounted(() => {
  handleSearch()
})
</script>
