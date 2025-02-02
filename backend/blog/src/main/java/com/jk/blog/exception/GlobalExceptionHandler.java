package com.jk.blog.exception;

import com.jk.blog.dto.APIResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${app.debug}")
    private boolean debugMode;

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleSecurityException(Exception exception) {
        ProblemDetail errorDetail = null;

        if (exception instanceof BadCredentialsException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
            errorDetail.setProperty("description", "The username or password is incorrect");

            return errorDetail;
        }

        if (exception instanceof AccountStatusException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty("description", "The account is locked");
        }

        if (exception instanceof AccessDeniedException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty("description", "You are not authorized to access this resource");
        }

        if (exception instanceof SignatureException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty("description", "The JWT signature is invalid");
        }

        if (exception instanceof ExpiredJwtException) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
            errorDetail.setProperty("description", "The JWT token has expired");
        }

        if (errorDetail == null) {
            errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            errorDetail.setProperty("description", "Unknown internal server error.");
        }

        return errorDetail;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> resourceNotFoundException(ResourceNotFoundException exception) {
        String message = exception.getMessage();
        APIResponse<Void> apiResponse = new APIResponse<>(false, message);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Map<String, String>>> handleMethodArgsNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> messageMap = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String defaultMessage = error.getDefaultMessage();
            messageMap.put(fieldName, defaultMessage);
        });
        APIResponse<Map<String, String>> apiResponse = new APIResponse<>(false, "Validation failed", messageMap);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        APIResponse<String> apiResponse = new APIResponse<>(false, ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity<APIResponse<String>> handlePasswordNotMatchException(PasswordNotMatchException ex) {
        APIResponse<String> apiResponse = new APIResponse<>(false, ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<APIResponse<String>> handleInvalidTokenException(InvalidTokenException ex) {
        APIResponse<String> apiResponse = new APIResponse<>(false, ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<APIResponse<String>> handleTokenExpiredException(TokenExpiredException ex) {
        APIResponse<String> apiResponse = new APIResponse<>(false, ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAlreadyExistingException.class)
    public ResponseEntity<APIResponse<String>> handleUserAlreadyExistingException(UserAlreadyExistingException ex) {
        APIResponse<String> apiResponse = new APIResponse<>(false, ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<APIResponse<String>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        APIResponse<String> apiResponse = new APIResponse<>(false, "Endpoint not found: " + ex.getRequestURL());
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<APIResponse<String>> handleNullPointerException(NullPointerException exception) {
        APIResponse<String> apiResponse = new APIResponse<>(false, "Null pointer exception occurred", null);
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<APIResponse<String>> handleEmailSendingException(EmailSendingException ex) {
        APIResponse<String> apiResponse = new APIResponse<>(false, ex.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = extractConstraintViolationMessage(ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);

        // Include error details only in development/test mode
        if (debugMode) {
            errorResponse.put("errorDetails", ex.getRootCause().getMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Handle Rate Limit Exceptions
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<APIResponse<String>> handleRateLimitExceededException(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new APIResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(RateLimitConfigurationException.class)
    public ResponseEntity<APIResponse<String>> handleRateLimitConfigurationException(RateLimitConfigurationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new APIResponse<>(false, ex.getMessage(), null));
    }

    // Handle Invalid Country Exception
    @ExceptionHandler(InvalidCountryException.class)
    public ResponseEntity<APIResponse<String>> handleInvalidCountryException(InvalidCountryException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse<>(false, ex.getMessage(), null));
    }

    // Handle Invalid Phone Number Exception
    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ResponseEntity<APIResponse<String>> handleInvalidPhoneNumberException(InvalidPhoneNumberException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse<>(false, ex.getMessage(), null));
    }

    // Handle Directory Creation Exception
    @ExceptionHandler(DirectoryCreationException.class)
    public ResponseEntity<APIResponse<String>> handleDirectoryCreationException(DirectoryCreationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new APIResponse<>(false, ex.getMessage(), null));
    }

    // Handle Invalid File Exception
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<APIResponse<String>> handleInvalidFileException(InvalidFileException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidPostStateException.class)
    public ResponseEntity<APIResponse<String>> handleInvalidPostStateException(InvalidPostStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(FieldUpdateNotAllowedException.class)
    public ResponseEntity<APIResponse<String>> handleFieldUpdateNotAllowedException(FieldUpdateNotAllowedException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<APIResponse<String>> handleInvalidFormatException(InvalidFormatException ex) {
        return ResponseEntity.badRequest().body(new APIResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(UserAccountAlreadyActiveException.class)
    public ResponseEntity<APIResponse<Void>> handleUserAccountAlreadyActiveException(UserAccountAlreadyActiveException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new APIResponse<>(false, ex.getMessage()));
    }

    @ExceptionHandler(AccountDeletionPeriodExceededException.class)
    public ResponseEntity<APIResponse<Void>> handleAccountDeletionPeriodExceededException(AccountDeletionPeriodExceededException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new APIResponse<>(false, ex.getMessage()));
    }


    /**
     * Extracts user-friendly messages from DataIntegrityViolationException based on constraint violations.
     */
    private String extractConstraintViolationMessage(DataIntegrityViolationException ex) {
        String errorMessage = ex.getRootCause().getMessage();

        if (errorMessage.contains("Unique index or primary key violation")) {
            if (errorMessage.contains("USERS(MOBILE")) {
                return "The mobile number you entered is already registered. Please use a different number.";
            } else if (errorMessage.contains("USERS(EMAIL")) {
                return "The email address you entered is already registered. Try logging in instead.";
            } else if (errorMessage.contains("USERS(USER_NAME")) {
                return "This username is already taken. Please choose another one.";
            }
        }

        return "A database constraint violation occurred. Please check your input.";
    }
}
