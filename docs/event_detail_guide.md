# EasyEvent Event Detail API & Interface Guide

## 1. Overview
This document describes the new Event Detail feature, including the Backend API and Frontend Interface.

## 2. Backend API

### Get Event Details
Retrieves detailed information for a specific event, including execution history and status.

- **URL**: `/api/events/{eventId}/details`
- **Method**: `GET`
- **Auth**: Required (Header `Authorization: <token>`)
- **Rate Limit**: 10 requests per second

#### Request Parameters
| Name | Type | Required | Description |
|---|---|---|---|
| eventId | Long | Yes | The unique ID of the event |

#### Response Success (200 OK)
```json
{
  "id": 12345,
  "appId": "order-service",
  "sourceId": 1001,
  "className": "com.example.OrderCreatedEvent",
  "title": "Order Created #1001",
  "content": "{\"orderId\": 1001, \"amount\": 99.99}",
  "processingState": "PROCESS_COMPLETE",
  "errorCount": 0,
  "maxRetries": 5,
  "retryCount": 0,
  "createdTime": "2023-10-27T10:00:00.000+00:00",
  "updatedTime": "2023-10-27T10:00:05.000+00:00",
  "startedTime": "2023-10-27T10:00:01.000+00:00",
  "startExecutionTime": "2023-10-27T10:00:01.000+00:00",
  "executionSuccessTime": "2023-10-27T10:00:05.000+00:00",
  "estimatedCompleteTime": "2023-10-27T10:00:02.000+00:00",
  "statusHistory": [
    {
      "status": "PROCESS_COMPLETE",
      "context": "Process State Updated: ",
      "createTime": "2023-10-27T10:00:05.000+00:00"
    },
    {
      "status": "IN_PROCESSING",
      "context": "Start Processing",
      "createTime": "2023-10-27T10:00:01.000+00:00"
    },
    {
      "status": "AVAILABLE",
      "context": "Send Complete",
      "createTime": "2023-10-27T10:00:00.000+00:00"
    }
  ]
}
```

#### Response Error
- **401 Unauthorized**: Missing or invalid token.
- **403 Forbidden**: Insufficient permissions.
- **404 Not Found**: Event ID does not exist.
- **429 Too Many Requests**: Rate limit exceeded.

## 3. Frontend Interface

### Event List
- The event list now includes a "Details" button for each event row.
- Clicking the button opens the Event Detail Dialog.

### Event Detail Dialog
The dialog displays comprehensive information organized in sections:
1.  **Basic Info**: ID, App ID, Class Name, Title.
2.  **Content**: Event Data (JSON formatted), Context/Keys.
3.  **Execution Stats**: Retry Count / Max Retries, Error Count.
4.  **Status**: Current Status tag, Status History Table (Time, Status, Context).
5.  **Time**: Created, Started, Updated, Start Execution, Execution Success, Estimated Completion.

### Responsive Design
- The dialog adapts to screen size (mobile/desktop).
- Uses Element Plus components for layout.

## 4. Database Changes
- Added fields to `ee_bus_event_entity`: `title`, `max_retries`, `started_time`, `estimated_complete_time`.
- Created table `ee_bus_event_history` for tracking status changes.

## 5. Usage
1.  Navigate to Event List in Admin UI.
2.  Click "Details" on any event.
3.  View history and details in the popup.
