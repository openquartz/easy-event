import request from './request'

export interface EventQuery {
  page: number
  size: number
  sourceId?: number
  eventKey?: string
  processingState?: string
  startTime?: string
  endTime?: string
}

export interface BusEvent {
  id: number
  appId: string
  sourceId: number
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

export const retryEvents = (eventIds: number[]) => {
  return request.post('/events/retry', eventIds)
}

export const getDashboardStats = () => {
  return request.get<any, any>('/stats/dashboard')
}
