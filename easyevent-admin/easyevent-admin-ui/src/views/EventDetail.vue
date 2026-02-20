<template>
  <div class="event-detail">
    <div class="header">
      <el-page-header @back="goBack" :title="$t('event.detail')">
        <template #extra>
          <el-button type="primary" @click="handleEdit">{{ $t('common.edit') }}</el-button>
          <el-button type="danger" @click="handleDelete">{{ $t('common.delete') }}</el-button>
        </template>
      </el-page-header>
    </div>

    <el-card v-loading="loading" class="detail-card">
      <el-descriptions :title="$t('event.information')" :column="2" border>
        <el-descriptions-item :label="$t('event.id')">{{ currentEvent?.id }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.eventKey')">{{ currentEvent?.eventKey }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.appId')">{{ currentEvent?.appId }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.sourceId')">{{ currentEvent?.sourceId }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.className')">{{ currentEvent?.className }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.state')">
          <el-tag :type="getStatusType(currentEvent?.processingState)">
            {{ currentEvent?.processingState ? $t(`event.states.${currentEvent.processingState}`) : '' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('event.errorCount')">{{ currentEvent?.errorCount }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.traceId')">{{ currentEvent?.traceId }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.createdTime')">{{ currentEvent?.createdTime }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.updatedTime')">{{ currentEvent?.updatedTime }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.startExecutionTime')">{{ currentEvent?.startExecutionTime }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.executionSuccessTime')">{{ currentEvent?.executionSuccessTime }}</el-descriptions-item>
        <el-descriptions-item :label="$t('event.content')" :span="2">
          <pre>{{ formattedEventData }}</pre>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('event.failedReason')" :span="2" v-if="currentEvent?.processingFailedReason">
          <el-alert type="error" :closable="false">{{ currentEvent?.processingFailedReason }}</el-alert>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="$t('event.editTitle')" width="50%">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item :label="$t('event.eventKey')" prop="eventKey">
          <el-input v-model="form.eventKey" disabled />
        </el-form-item>
        <el-form-item :label="$t('event.state')" prop="processingState">
           <el-select v-model="form.processingState">
             <el-option :label="$t('event.states.INIT')" value="INIT" />
             <el-option :label="$t('event.states.IN_PROCESSING')" value="IN_PROCESSING" />
             <el-option :label="$t('event.states.PROCESS_COMPLETE')" value="PROCESS_COMPLETE" />
             <el-option :label="$t('event.states.PROCESS_FAILED')" value="PROCESS_FAILED" />
             <el-option :label="$t('event.states.DEAD')" value="DEAD" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('event.content')" prop="eventData">
          <el-input type="textarea" v-model="form.eventData" :rows="10" />
        </el-form-item>
        <el-form-item :label="$t('event.failedReason')" prop="processingFailedReason">
           <el-input type="textarea" v-model="form.processingFailedReason" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitForm(formRef)">{{ $t('common.confirm') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useEventStore } from '@/stores/event'
import { ElMessageBox, ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const store = useEventStore()
const { t } = useI18n()

const loading = computed(() => store.loading)
const currentEvent = computed(() => store.currentEvent)

const formattedEventData = computed(() => {
  const data = currentEvent.value?.eventData
  if (!data) return ''
  try {
    return JSON.stringify(JSON.parse(data), null, 2)
  } catch (e) {
    return data
  }
})

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = ref({
  eventKey: '',
  processingState: '',
  eventData: '',
  processingFailedReason: ''
})

const rules = computed<FormRules>(() => ({
  eventData: [
    { required: true, message: t('event.validation.dataRequired'), trigger: 'blur' }
  ],
  processingState: [
    { required: true, message: t('event.validation.stateRequired'), trigger: 'change' }
  ]
}))

const eventId = route.params.id as string

onMounted(() => {
  if (eventId) {
    store.fetchEventDetail(eventId)
  }
})

const goBack = () => {
  router.push('/events')
}

const getStatusType = (status?: string) => {
  switch (status) {
    case 'PROCESS_COMPLETE': return 'success'
    case 'PROCESS_FAILED': return 'danger'
    case 'IN_PROCESSING': return 'warning'
    default: return 'info'
  }
}

const handleEdit = () => {
  if (currentEvent.value) {
    form.value = {
      eventKey: currentEvent.value.eventKey,
      processingState: currentEvent.value.processingState,
      eventData: currentEvent.value.eventData,
      processingFailedReason: currentEvent.value.processingFailedReason
    }
    dialogVisible.value = true
  }
}

const handleDelete = () => {
  ElMessageBox.confirm(
    t('event.deleteConfirm'),
    t('common.warning'),
    {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        await store.removeEvent(eventId)
        ElMessage.success(t('event.deleteSuccess'))
        router.push('/events')
      } catch (error) {
        // store handles error logging
      }
    })
    .catch(() => {})
}

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      try {
        await store.updateEventDetail(eventId, form.value)
        ElMessage.success(t('event.updateSuccess'))
        dialogVisible.value = false
      } catch (error) {
         // store handles error
      }
    }
  })
}
</script>

<style scoped>
.event-detail {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
.detail-card {
  margin-bottom: 20px;
}
pre {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
