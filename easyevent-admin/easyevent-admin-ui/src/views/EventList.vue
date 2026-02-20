<template>
  <div>
    <el-card>
      <el-form :inline="true" :model="query" class="demo-form-inline">
        <el-form-item :label="$t('event.eventKey')">
          <el-input v-model="query.eventKey" :placeholder="$t('event.eventKey')" />
        </el-form-item>
        <el-form-item :label="$t('event.state')" style="width: 200px">
          <el-select v-model="query.processingState" :placeholder="$t('event.state')" clearable style="width: 100%">
            <el-option :label="$t('event.states.AVAILABLE')" value="AVAILABLE" />
            <el-option :label="$t('event.states.PROCESS_COMPLETE')" value="PROCESS_COMPLETE" />
            <el-option :label="$t('event.states.PROCESS_FAILED')" value="PROCESS_FAILED" />
            <el-option :label="$t('event.states.IN_PROCESSING')" value="IN_PROCESSING" />
            <el-option :label="$t('event.states.TRANSFER_SUCCESS')" value="TRANSFER_SUCCESS" />
            <el-option :label="$t('event.states.TRANSFER_FAILED')" value="TRANSFER_FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('event.timeRange')">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="-"
            :start-placeholder="$t('event.startTime')"
            :end-placeholder="$t('event.endTime')"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ $t('common.search') }}</el-button>
          <el-button @click="handleReset">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" :label="$t('event.id')" width="100" />
        <el-table-column prop="className" :label="$t('event.className')" width="250" show-overflow-tooltip />
        <el-table-column prop="eventKey" :label="$t('event.eventKey')" width="150" />
        <el-table-column prop="processingState" :label="$t('event.state')" width="150">
          <template #default="{ row }">
            <el-tag :type="getStateType(row.processingState)">{{ $t(`event.states.${row.processingState}`) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorCount" :label="$t('event.errorCount')" width="80" />
        <el-table-column prop="createdTime" :label="$t('event.createdTime')" width="180" />
        <el-table-column :label="$t('common.operations')" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleDetail(row)">{{ $t('common.detail') }}</el-button>
            <el-button
              v-if="row.processingState === 'PROCESS_FAILED'"
              size="small"
              type="danger"
              @click="handleRetry(row)"
            >
              {{ $t('event.retry') }}
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

    <el-dialog v-model="detailVisible" :title="$t('event.detail')" width="70%">
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="$t('event.id')">{{ currentEvent?.id }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.className')">{{ currentEvent?.className }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.title')">{{ currentEvent?.title }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.eventKey')">{{ currentEvent?.eventKey }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.state')">
          <el-tag :type="getStateType(currentEvent?.processingState || '')">
            {{ currentEvent?.processingState ? $t(`event.states.${currentEvent.processingState}`) : '' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('event.errorCount')">{{ currentEvent?.errorCount }} / {{ currentEvent?.maxRetries }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.createdTime')">{{ currentEvent?.createdTime }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.updatedTime')">{{ currentEvent?.updatedTime }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.startExecutionTime')">{{ currentEvent?.startExecutionTime }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.executionSuccessTime')">{{ currentEvent?.executionSuccessTime }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.appId')">{{ currentEvent?.appId }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.sourceId')">{{ currentEvent?.sourceId }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.traceId')">{{ currentEvent?.traceId }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.creatingOwner')">{{ currentEvent?.creatingOwner }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.processingOwner')">{{ currentEvent?.processingOwner }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.processingAvailableDate')">{{ currentEvent?.processingAvailableDate }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 20px;">
        <h3>{{ $t('event.statusHistory') }}</h3>
        <el-table :data="currentEvent?.statusHistory || []" style="width: 100%" border>
          <el-table-column prop="status" :label="$t('event.status')" width="150" />
          <el-table-column prop="context" :label="$t('event.context')" show-overflow-tooltip />
          <el-table-column prop="createTime" :label="$t('event.time')" width="180" />
        </el-table>
      </div>

      <div style="margin-top: 20px;">
        <h3>{{ $t('event.content') }}</h3>
        <el-input
          v-model="currentEventData"
          type="textarea"
          :rows="10"
          readonly
        />
      </div>

      <div style="margin-top: 20px;">
        <h3>{{ $t('event.failedReason') }}</h3>
        <el-input
          v-model="currentEventFailedReason"
          type="textarea"
          :rows="3"
          readonly
        />
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">{{ $t('common.close') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getEvents, retryEvents, type BusEvent, type EventQuery } from '@/api/event'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const router = useRouter()
const loading = ref(false)
const tableData = ref<BusEvent[]>([])
const total = ref(0)
const timeRange = ref<[string, string] | null>(null)
const detailVisible = ref(false)
const currentEvent = ref<BusEvent | null>(null)

const currentEventData = computed(() => {
  if (!currentEvent.value?.eventData) return ''
  try {
    return JSON.stringify(JSON.parse(currentEvent.value.eventData), null, 2)
  } catch (e) {
    return currentEvent.value.eventData
  }
})

const currentEventFailedReason = computed(() => {
  return currentEvent.value?.processingFailedReason || ''
})

const query = reactive<EventQuery>({
  page: 1,
  size: 20,
  eventKey: '',
  processingState: ''
})

const handleSearch = async () => {
  loading.value = true
  if (timeRange.value && timeRange.value.length === 2) {
    query.startTime = timeRange.value[0]
    query.endTime = timeRange.value[1]
  } else {
    query.startTime = undefined
    query.endTime = undefined
  }
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
  timeRange.value = null
  handleSearch()
}

const handleDetail = (row: BusEvent) => {
  router.push(`/events/${row.id}`)
}

const handleRetry = (row: BusEvent) => {
  ElMessageBox.confirm(t('event.retryConfirm', { id: row.id }), t('common.warning'), {
    confirmButtonText: t('common.ok'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  }).then(async () => {
    try {
      await retryEvents([row.id])
      ElMessage.success(t('event.retrySuccess'))
      handleSearch()
    } catch (e) {
      console.error(e)
    }
  })
}

const getStateType = (state: string) => {
  switch (state) {
    case 'PROCESS_COMPLETE':
    case 'TRANSFER_SUCCESS':
      return 'success'
    case 'PROCESS_FAILED':
    case 'TRANSFER_FAILED':
      return 'danger'
    case 'AVAILABLE':
    case 'IN_PROCESSING':
    case 'PROCESSING':
      return 'primary'
    default:
      return 'info'
  }
}

onMounted(() => {
  handleSearch()
})
</script>
