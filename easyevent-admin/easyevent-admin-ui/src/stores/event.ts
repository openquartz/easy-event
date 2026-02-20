import { defineStore } from 'pinia'
import { getEventDetail, updateEvent, deleteEvent, type BusEvent } from '@/api/event'

interface EventState {
  currentEvent: BusEvent | null
  loading: boolean
  error: string | null
}

export const useEventStore = defineStore('event', {
  state: (): EventState => ({
    currentEvent: null,
    loading: false,
    error: null
  }),
  actions: {
    async fetchEventDetail(id: string) {
      this.loading = true
      this.error = null
      try {
        const res = await getEventDetail(id)
        this.currentEvent = res
      } catch (err: any) {
        this.error = err.message || 'Failed to fetch event details'
      } finally {
        this.loading = false
      }
    },
    async updateEventDetail(id: string, data: Partial<BusEvent>) {
      this.loading = true
      try {
        await updateEvent(id, data)
        if (this.currentEvent) {
          this.currentEvent = { ...this.currentEvent, ...data }
        }
      } catch (err: any) {
        throw err
      } finally {
        this.loading = false
      }
    },
    async removeEvent(id: string) {
      this.loading = true
      try {
        await deleteEvent(id)
        this.currentEvent = null
      } catch (err: any) {
        throw err
      } finally {
        this.loading = false
      }
    }
  }
})
