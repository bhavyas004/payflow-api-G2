# Leave Request API Documentation

## Overview
This document describes the Leave Request API endpoints that have been implemented for the PayFlow application.

## Base URL
```
http://localhost:8080/payflowapi/leave-requests
```

## Authentication
All endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### 1. Apply for Leave
**POST** `/apply`

Creates a new leave request for an employee.

**Request Body:**
```json
{
  "employeeId": 1,
  "employeeName": "John Doe",
  "employeeEmail": "john.doe@company.com",
  "startDate": "2025-08-15",
  "endDate": "2025-08-17",
  "totalDays": 3,
  "reason": "Personal work",
  "leaveYear": 2025
}
```

**Response:**
```json
{
  "success": true,
  "message": "Leave request submitted successfully",
  "data": {
    "id": 1,
    "employeeId": 1,
    "employeeName": "John Doe",
    "employeeEmail": "john.doe@company.com",
    "startDate": "2025-08-15",
    "endDate": "2025-08-17",
    "totalDays": 3,
    "reason": "Personal work",
    "leaveYear": 2025,
    "status": "PENDING",
    "approvedBy": null,
    "approvedAt": null,
    "rejectionReason": null,
    "createdAt": "2025-07-29T12:00:00",
    "updatedAt": "2025-07-29T12:00:00"
  }
}
```

### 2. Get Employee Leave Requests
**GET** `/employee/{employeeId}`

Retrieves all leave requests for a specific employee.

**Response:**
```json
{
  "success": true,
  "message": "Leave requests retrieved successfully",
  "data": [
    {
      "id": 1,
      "employeeId": 1,
      "employeeName": "John Doe",
      "status": "PENDING",
      // ... other fields
    }
  ]
}
```

### 3. Get Pending Leave Requests
**GET** `/pending`

Retrieves all pending leave requests (for managers/HR).

### 4. Approve Leave Request
**PUT** `/{requestId}/approve`

Approves a leave request.

**Response:**
```json
{
  "success": true,
  "message": "Leave request approved successfully",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "approvedBy": "manager@company.com",
    "approvedAt": "2025-07-29T12:30:00",
    // ... other fields
  }
}
```

### 5. Reject Leave Request
**PUT** `/{requestId}/reject`

Rejects a leave request.

**Request Body:**
```json
{
  "rejectionReason": "Insufficient staffing during requested period"
}
```

### 6. Get Leave Balance
**GET** `/balance/{employeeId}/{year}`

Gets the leave balance for an employee in a specific year.

**Response:**
```json
{
  "success": true,
  "message": "Leave balance retrieved successfully",
  "data": {
    "employeeId": 1,
    "year": 2025,
    "remainingDays": 9,
    "totalDays": 12,
    "usedDays": 3
  }
}
```

## Error Responses

All endpoints return error responses in the following format:
```json
{
  "success": false,
  "message": "Error description here"
}
```

Common error scenarios:
- **400 Bad Request**: Invalid request data, insufficient leave balance, overlapping dates
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Attempting to access other employee's data
- **404 Not Found**: Leave request not found
- **500 Internal Server Error**: Server error

## Business Rules

1. **Leave Balance**: Each employee has 12 days of annual leave per year
2. **Date Validation**: Start date cannot be in the past, end date cannot be before start date
3. **Overlapping Leaves**: Employees cannot have overlapping leave requests
4. **Security**: Employees can only apply for and view their own leave requests
5. **Status Flow**: PENDING → APPROVED/REJECTED (final states)

## Database Schema

The `leave_requests` table has been created with the following structure:
- Primary key: `id` (BIGINT, auto-increment)
- Foreign key: `employee_id` references `employee(id)`
- Enum status: PENDING, APPROVED, REJECTED, CANCELLED
- Indexes on: employee_id, status, leave_year, date range

## Frontend Integration

The React frontend should call this API as follows:
```javascript
const response = await axios.post('/payflowapi/leave-requests/apply', leaveRequest, {
  headers: { Authorization: `Bearer ${token}` }
});
```

This matches the existing frontend code in `LeaveRequestForm.jsx`.
