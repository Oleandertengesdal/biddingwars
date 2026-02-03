# Exception Handling System - Visual Guide

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT REQUEST                           │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                  @RestController (API Endpoint)                  │
│  ✓ No exception handling                                         │
│  ✓ Delegates to service layer                                   │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                    @Service (Business Logic)                     │
│  ✓ Performs validations                                         │
│  ✓ Throws appropriate exceptions                               │
│  ✓ Examples:                                                    │
│    - throw new ResourceNotFoundException()                      │
│    - throw new ValidationException()                            │
│    - throw new UnauthorizedException()                          │
└─────────────────────────────────┬───────────────────────────────┘
                                  │
                    ┌─────────────┴──────────────┐
                    │                            │
                    ▼                            ▼
            ┌─────────────────┐      ┌──────────────────┐
            │   SUCCESS       │      │   EXCEPTION      │
            │ ✓ Return DTO    │      │ ✓ Thrown         │
            │ ✓ HTTP 200/201  │      │ ✓ Propagated     │
            └─────────────────┘      └────────┬─────────┘
                    │                         │
                    │         ┌───────────────┘
                    │         │
                    │         ▼
                    │    ┌───────────────────────────────────┐
                    │    │ @RestControllerAdvice            │
                    │    │ GlobalExceptionHandler           │
                    │    │                                   │
                    │    │ ✓ Catches all exceptions         │
                    │    │ ✓ Maps to HTTP status            │
                    │    │ ✓ Builds error response          │
                    │    │ ✓ Logs error                     │
                    │    │                                   │
                    │    │ Exception Handlers:              │
                    │    │  • AppException → Status + Code  │
                    │    │  • Validation → 400              │
                    │    │  • Security → 403, 401           │
                    │    │  • NotFound → 404                │
                    │    │  • Conflict → 409                │
                    │    │  • Generic → 500                 │
                    │    └───────────┬───────────────────────┘
                    │                │
                    └────────┬───────┘
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│               ERROR RESPONSE (JSON)                              │
│  {                                                              │
│    "timestamp": "2026-02-03T10:30:45.123456",                  │
│    "status": 404,                                              │
│    "error": "Not Found",                                        │
│    "errorCode": "RESOURCE_NOT_FOUND",                           │
│    "message": "User not found with ID: 123",                    │
│    "fieldErrors": null,                                         │
│    "path": "/api/users/123"                                     │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                     ┌────────────────┐
                     │   CLIENT       │
                     │  ✓ Receives    │
                     │    error with  │
                     │    proper code │
                     └────────────────┘
```

## Exception Hierarchy Tree

```
RuntimeException (Java)
    │
    ├─── AppException (Abstract)
    │       │
    │       ├─── ResourceNotFoundException (404)
    │       │    • User not found
    │       │    • Auction not found
    │       │    • Category not found
    │       │
    │       ├─── InvalidOperationException (409)
    │       │    • Auction ended
    │       │    • Cannot update auction
    │       │    • Invalid state transition
    │       │
    │       ├─── UnauthorizedException (403)
    │       │    • Not auction owner
    │       │    • Insufficient permissions
    │       │    • Access denied
    │       │
    │       ├─── ValidationException (400)
    │       │    • Bid too low
    │       │    • Invalid times
    │       │    • Bidder is owner
    │       │
    │       ├─── DuplicateResourceException (409)
    │       │    • Username exists
    │       │    • Email already registered
    │       │    • Duplicate entry
    │       │
    │       └─── FileStorageException (400/413)
    │            • Invalid file type
    │            • File too large
    │            • Storage error
    │
    └─── (Other RuntimeExceptions)
         • EntityNotFoundException
         • Spring Security exceptions
         • Validation exceptions
```

## HTTP Status Code Decision Tree

```
                        ┌─ ERROR ─┐
                        │         │
                 ┌──────┴────┬────┴──────┐
                 │           │           │
            CLIENT ERROR  AUTH ERROR  SERVER ERROR
            (4xx)         (401/403)   (5xx)
                 │
         ┌───────┼───────────┐
         │       │           │
        400    403          404          409
        BAD   FORBIDDEN    NOT FOUND    CONFLICT
       REQUEST             │             │
         │       │         │             │
    Validation  Permission  Resource    Business
    Error       Denied      Missing     Logic
         │       │         │             │
      • Invalid  • Not     • User not  • Auction
        bid      owner      found       ended
      • Wrong   • Can't    • Auction   • Duplicate
        time    update      not found   • Has bids
      • Bid too • No       • Category  • Wrong
        low    access      not found    state
         │       │         │             │
         │       │         │             │
      └──┴───┬──┴────┴──┬──┴──────┬──────┘
             │ ValidationException    │
             │ UnauthorizedException │
             │ ResourceNotFoundException
             │ InvalidOperationException
             │ DuplicateResourceException
             └─────────────────────────┘
```

## Request/Response Flow

### Success Flow
```
GET /api/users/1
    ↓
@GetMapping("/{id}")
public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
    ↓
    UserService.getUserById(id)
        ↓
        userRepository.findById(id).get()  ← Found!
        ↓
        Return User DTO
    ↓
HTTP 200 OK
{
    "id": 1,
    "username": "john_doe",
    ...
}
```

### Error Flow (404)
```
GET /api/users/999
    ↓
@GetMapping("/{id}")
public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
    ↓
    UserService.getUserById(id)
        ↓
        userRepository.findById(id)
            ↓
            Optional.empty() ← Not found!
            ↓
            throw new ResourceNotFoundException("User", 999L);
    ↓
Exception propagates up
    ↓
@RestControllerAdvice
GlobalExceptionHandler.handleResourceNotFound()
    ↓
    status: 404
    errorCode: "RESOURCE_NOT_FOUND"
    message: "User not found with ID: 999"
    ↓
HTTP 404 Not Found
{
    "timestamp": "2026-02-03T...",
    "status": 404,
    "error": "Not Found",
    "errorCode": "RESOURCE_NOT_FOUND",
    "message": "User not found with ID: 999",
    "path": "/api/users/999"
}
```

### Error Flow (400 Validation)
```
POST /api/bids
{
    "itemId": 5,
    "amount": 50.00  ← Too low!
}
    ↓
@PostMapping
public ResponseEntity<BidDTO> placeBid(@RequestBody BidRequestDTO request) {
    return ResponseEntity.ok(bidService.placeBid(request, currentUser));
    ↓
    BidService.placeBid(request, user)
        ↓
        currentPrice = 100.00
        ↓
        if (request.amount() <= currentPrice)  ← 50 <= 100 = TRUE
            throw new ValidationException("Bid must be higher...");
    ↓
Exception propagates up
    ↓
@RestControllerAdvice
GlobalExceptionHandler.handleValidationException()
    ↓
    status: 400
    errorCode: "VALIDATION_ERROR"
    message: "Bid must be higher than current price of 100.00"
    ↓
HTTP 400 Bad Request
{
    "timestamp": "2026-02-03T...",
    "status": 400,
    "error": "Bad Request",
    "errorCode": "VALIDATION_ERROR",
    "message": "Bid must be higher than current price of 100.00",
    "path": "/api/bids"
}
```

## Exception Selection Guide (Decision Matrix)

```
┌─────────────────────────────────────────────────────────────────┐
│                      What went wrong?                            │
└─────────────────────────────────────────────────────────────────┘
                              │
            ┌─────────────────┼─────────────────┐
            │                 │                 │
      CLIENT FAULT        SERVER FAULT    EXPECTED ERROR
      (Handle it)         (Fix code)      (Recoverable)
            │                 │                 │
            ▼                 │                 │
    ┌───────────────┐         │                 │
    │ INPUT ERROR?  │◄────────┤                 │
    └───┬───────────┘         │                 │
        │                     │                 │
        ├─ Validation? ─→ ValidationException  │
        ├─ Duplicate?   ─→ DuplicateResourceException
        └─ Type error?  ─→ ValidationException  │
                             │                 │
        ┌───────────────────┬─┴──┐              │
        │                   │    │              │
    ┌───┴────────────────────────┘              │
    │                                           │
    │ RESOURCE MISSING?                         │
    │ ┌─ ResourceNotFoundException              │
    └─┘                                         │
        │                                       │
        │ PERMISSION ISSUE?                    │
        │ ┌─ UnauthorizedException             │
        └─┘                                     │
            │                                   │
            │ BUSINESS LOGIC PROBLEM?           │
            │ ┌─ InvalidOperationException      │
            └─┘                                 │
                │                               │
                │ FILE PROBLEM?                 │
                │ ┌─ FileStorageException       │
                └─┘                             │
                    │                           │
                    │◄──────────────────────────┘
                    ▼
            THROW SPECIFIC EXCEPTION
                    │
                    ▼
        GlobalExceptionHandler
                    │
                    ▼
        HTTP Status + Error Code
                    │
                    ▼
        JSON Response to Client
```

## Error Response Schema

```json
{
  "timestamp": "ISO-8601 DateTime",
  "status": "HTTP Status Code (int)",
  "error": "HTTP Status Reason Phrase (String)",
  "errorCode": "Application Error Code (String)",
  "message": "Human-readable error message (String)",
  "fieldErrors": {
    "fieldName": "Error message for field (Object | null)"
  },
  "path": "Request path (String)"
}
```

## Example Scenarios

### Scenario 1: Resource Not Found
```
Request:  GET /api/users/999
Response: 404 Not Found
{
  "status": 404,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "User not found with ID: 999"
}
```

### Scenario 2: Invalid Bid
```
Request:  POST /api/bids
          { "itemId": 5, "amount": 10.00 }
Response: 400 Bad Request
{
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Bid must be higher than current price of 50.00"
}
```

### Scenario 3: Duplicate Email
```
Request:  POST /api/auth/register
          { "email": "test@example.com", ... }
Response: 409 Conflict
{
  "status": 409,
  "errorCode": "DUPLICATE_RESOURCE",
  "message": "User with email 'test@example.com' already exists"
}
```

### Scenario 4: No Permission
```
Request:  PUT /api/auctions/5
          { "title": "New Title" }
Response: 403 Forbidden
{
  "status": 403,
  "errorCode": "UNAUTHORIZED",
  "message": "You can only update your own auctions"
}
```

### Scenario 5: Business Logic Error
```
Request:  POST /api/bids
          { "itemId": 5, "amount": 100.00 }
Response: 409 Conflict
{
  "status": 409,
  "errorCode": "INVALID_OPERATION",
  "message": "This auction has ended. Bidding is no longer allowed."
}
```

### Scenario 6: Validation Errors (Multiple Fields)
```
Request:  POST /api/auth/register
          { "email": "invalid" }
Response: 400 Bad Request
{
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "One or more fields have validation errors",
  "fieldErrors": {
    "username": "Username is required",
    "email": "Email format is invalid",
    "password": "Password must be at least 8 characters"
  }
}
```

## Integration Points

```
                   GlobalExceptionHandler
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   Spring Security    Bean Validation      Custom Exceptions
        │                   │                   │
   • Authentication      • @Valid          • ResourceNotFound
   • Authorization       • @Validated      • InvalidOperation
   • AccessDenied                          • Unauthorized
                                           • Validation
                                           • Duplicate
                                           • FileStorage
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
                    Unified Error Response
                            │
                     Error Response DTO
                            │
                      JSON to Client
```

## Key Takeaways

✅ **One Exception per Problem Type**
- ResourceNotFoundException for missing resources
- ValidationException for input errors
- InvalidOperationException for state errors
- UnauthorizedException for permission errors
- DuplicateResourceException for duplicates
- FileStorageException for file errors

✅ **One Handler for All Exceptions**
- GlobalExceptionHandler catches all
- Automatically maps to HTTP status
- Consistent response format
- No exception handling in controllers

✅ **Client-Friendly Responses**
- Meaningful HTTP status codes
- Error codes for programmatic handling
- Human-readable messages
- Request path for debugging
- Timestamp for tracking

✅ **Production-Ready**
- Secure (no stack traces exposed)
- Logged (for monitoring)
- Extensible (easy to add new types)
- Documented (clear and comprehensive)
