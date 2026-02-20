import request from './request'

export interface EventQuery {
  page: number
  size: number
  sourceId?: string
  eventKey?: string
  processingState?: string
  startTime?: string
  endTime?: string
}

export interface BusEvent {
  id: string
  appId: string
  sourceId: string
  className: string
  errorCount: number
  processingState: string
  successfulSubscriber: string
  traceId: string
  eventData: string
  eventKey: string
  creatingOwner: string
  processingOwner: string
  processingAvailableDate: string
  processingFailedReason: string
  createdTime: string
  updatedTime: string
  title?: string
  maxRetries?: number
  startExecutionTime?: string
  executionSuccessTime?: string
}

export interface PageResult<T> {
  total: number
  records: T[]
  page: number
  size: number
}

export const getEvents = (params: EventQuery) => {
  return request.get<any, PageResult<BusEvent>>('/events/list', { params })
}

export const getEventDetail = (id: string) => {
  return request.get<any, BusEvent>(`/events/${id}/details`)
}

export const updateEvent = (id: string, data: Partial<BusEvent>) => {
  return request.put(`/events/${id}`, data)
}

export const deleteEvent = (id: string) => {
  return request.delete(`/events/${id}`)
}

export const retryEvents = (eventIds: string[]) => {
  return request.post('/events/retry', eventIds)
}

export const getDashboardStats = () => {
  return request.get<any, any>('/stats/dashboard')
}
