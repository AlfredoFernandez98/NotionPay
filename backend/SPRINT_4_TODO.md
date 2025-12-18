# Sprint 4 - User Stories TODO List

## User Story 10: Activity Logging (7 pts)
**Aktiviteter logges ved brug af systemet (Aktiv/passiv log af)**

### Current Implementation Status:
✅ ActivityLog entity created
✅ ActivityLogType enum (LOGIN, LOGOUT, PAYMENT, ADD_CARD, REMOVE_CARD)
✅ ActivityLogStatus enum (SUCCESS, FAILURE)
✅ ActivityLogDAO implemented
✅ Login activity logging implemented
✅ Logout activity logging implemented

### TODO Tasks:

#### 1. Add Missing Activity Types
- [x] Add `SUBSCRIPTION_CREATED` to ActivityLogType enum
- [x] Add `SUBSCRIPTION_CANCELLED` to ActivityLogType enum
- [x] Add `SUBSCRIPTION_RENEWED` to ActivityLogType enum
- [x] Add `PASSWORD_CHANGED` to ActivityLogType enum
- [x] Add `PROFILE_UPDATED` to ActivityLogType enum
- [x] Add `SMS_SENT` to ActivityLogType enum
- [x] Add `SMS_PURCHASE` to ActivityLogType enum

#### 2. Implement Activity Logging in PaymentController
- [x] Log activity when payment is created (PAYMENT type)
- [x] Log activity when card is added (ADD_CARD type) - already has type
- [ ] Log activity when card is removed (REMOVE_CARD type) - already has type
- [x] Include payment details in metadata (amount, currency, status)
- [x] Include subscription ID in metadata if subscription payment

#### 3. Implement Activity Logging in SubscriptionController
- [x] Log activity when subscription is created (SUBSCRIPTION_CREATED)
- [x] Log activity when subscription is cancelled (SUBSCRIPTION_CANCELLED)
- [x] Log activity when subscription is renewed (SUBSCRIPTION_RENEWED)
- [x] Include subscription details in metadata (plan, status, dates)

#### 4. Implement Activity Logging in CustomerController
- [ ] Log activity when customer profile is updated (PROFILE_UPDATED)
- [ ] Log activity when password is changed (PASSWORD_CHANGED)
- [ ] Include changed fields in metadata

#### 5. Implement Activity Logging for SMS Operations
- [ ] Log activity when SMS is sent (SMS_SENT)
- [ ] Log activity when SMS package is purchased (SMS_PURCHASE)
- [ ] Include SMS details in metadata (recipient, count, balance)

#### 6. Create ActivityLog Viewing Endpoints
- [ ] Create endpoint: GET /api/customers/{id}/activity-logs
- [ ] Add pagination support (page, limit)
- [ ] Add filtering by type (query param: ?type=PAYMENT)
- [ ] Add filtering by date range (query params: ?from=date&to=date)
- [ ] Add filtering by status (query param: ?status=SUCCESS)
- [ ] Return ActivityLogDTO list with customer and session info

#### 7. Create ActivityLog Statistics Endpoint
- [ ] Create endpoint: GET /api/customers/{id}/activity-stats
- [ ] Return count by type (e.g., total logins, total payments)
- [ ] Return count by status (success vs failure)
- [ ] Return recent activity summary (last 7 days, 30 days)


#### 9. Passive Logging (Background Events)
- [ ] Log failed login attempts (already using ActivityLogStatus.FAILURE)
- [ ] Log session expiration events
- [ ] Log automatic subscription renewals (Phase 2 integration)
- [ ] Log payment failures
- [ ] Log system errors related to customer actions

#### 10. Testing
- [ ] Write unit tests for ActivityLog creation
- [ ] Write integration tests for activity logging endpoints
- [ ] Test pagination and filtering
- [ ] Test metadata storage and retrieval
- [ ] Verify all activity types are logged correctly

---

## User Story 9: Hent Faktura (2 pts)
**Retrieve Invoice/Receipt**

### Current Implementation Status:
✅ Receipt entity created
✅ ReceiptDAO implemented
✅ Receipt generation on successful payment
✅ Endpoint: GET /api/payments/{paymentId}/receipt

### TODO Tasks:

#### 1. Enhance Receipt Retrieval
- [x] Create endpoint: GET /api/customers/{id}/receipts (list all receipts)
- [x] Add sorting options (sorted by date, newest first)
- [ ] Add pagination support (future enhancement)
- [ ] Add filtering by date range (future enhancement)
- [ ] Add filtering by amount range (future enhancement) (date, amount)

#### 2. Receipt Download/Export
- [ ] Create endpoint: GET /api/receipts/{id}/download
- [ ] Generate PDF receipt (use library like iText or Apache PDFBox)
- [ ] Include company logo and branding
- [ ] Include all payment details
- [ ] Include customer information
- [ ] Include line items (subscription/product details)

#### 3. Receipt Email Functionality
- [ ] Create email service for sending receipts
- [ ] Send receipt email automatically after successful payment
- [ ] Create endpoint: POST /api/receipts/{id}/resend (resend receipt email)
- [ ] Use email template with HTML formatting
- [ ] Include PDF attachment

#### 4. Receipt Search and Filtering
- [ ] Add search by receipt number
- [ ] Add search by date
- [ ] Add search by amount
- [ ] Add search by payment method (last4 digits)

#### 5. Receipt Details Enhancement
- [ ] Add tax calculation fields (if applicable)
- [ ] Add discount fields (if applicable)
- [ ] Add notes/memo field
- [ ] Add receipt status tracking (sent, viewed, downloaded)

#### 6. Testing
- [ ] Write unit tests for receipt generation
- [ ] Write integration tests for receipt endpoints
- [ ] Test PDF generation
- [ ] Test email sending (mock email service)
- [ ] Test receipt retrieval with filters

---

## Storyless Tasks

### General Improvements
- [ ] Add comprehensive error handling for all new endpoints
- [ ] Add input validation for all request parameters
- [ ] Add API documentation (Swagger/OpenAPI) for new endpoints
- [ ] Add rate limiting for activity log endpoints
- [ ] Optimize database queries for activity logs (add indexes)

### Security Enhancements
- [ ] Ensure customers can only view their own activity logs
- [ ] Ensure customers can only view their own receipts
- [ ] Add admin role check for admin endpoints
- [ ] Sanitize metadata before storing in activity logs

### Performance Optimization
- [ ] Add database indexes on activity_log.timestamp
- [ ] Add database indexes on activity_log.type
- [ ] Add database indexes on receipt.created_at
- [ ] Consider archiving old activity logs (> 1 year)

---

## Sprint 4 Acceptance Criteria

### User Story 10 (Activity Logging):
- [ ] All user actions are logged (active logging)
- [ ] System events are logged (passive logging)
- [ ] Customers can view their activity history
- [ ] Activity logs include relevant metadata
- [ ] Activity logs are searchable and filterable
- [ ] Activity statistics are available

### User Story 9 (Hent Faktura):
- [ ] Customers can retrieve all their receipts
- [ ] Receipts can be downloaded as PDF
- [ ] Receipts are automatically emailed after payment
- [ ] Receipts can be resent on demand
- [ ] Receipts are searchable by various criteria

---

## Estimated Time: 1-2 Weeks

**Priority Order:**
1. Complete Activity Logging (User Story 10) - 7 pts
2. Complete Receipt Retrieval (User Story 9) - 2 pts
3. Testing and documentation
4. Performance optimization

---

## Notes:
- User Story 10 has partial implementation (LOGIN, LOGOUT already done)
- Focus on adding remaining activity types and viewing endpoints
- User Story 9 has basic implementation (receipt generation exists)
- Focus on PDF generation and email functionality
- Both stories integrate with existing payment and subscription systems
