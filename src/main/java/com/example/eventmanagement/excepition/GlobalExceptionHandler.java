package com.example.eventmanagement.excepition;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex,
                                                   HttpServletRequest req) {
        log.warn("Not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(),
                req, violation("resource",
                        "RESOURCE_NOT_FOUND", ex.getMessage()));
    }


    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessRuleException ex,
                                                   HttpServletRequest req) {
        log.warn("Business rule violated: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req,
                violation("registration",
                        "BUSINESS_RULE_VIOLATION",
                        ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex,
                                                    HttpServletRequest req) {
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT,
                "Request conflicts with existing data",
                req, violation("data", "DATA_INTEGRITY_VIOLATION",
                        "Request conflicts with existing data"));

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {} {}", req.getMethod(),
                req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong", req,
                violation("server", "INTERNAL_ERROR",
                        "Unexpected error occurred"));
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex,
                                                     HttpServletRequest req) {
        String msg = "No endpoint found for %s.".formatted(req.getRequestURI());
        log.warn("No resource found: {}", msg);
        return build(HttpStatus.NOT_FOUND, msg, req,
                violation("path", "ENDPOINT_NOT_FOUND", msg));
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest req) {
        String expected = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName() : "valid type";
        String msg = String.format("Invalid value '%s' for parameter '%s'. Expected %s.",
                ex.getValue(), ex.getName(), expected);

        log.warn("Type mismatch: {}", msg);
        return build(HttpStatus.BAD_REQUEST, msg, req,
                violation(ex.getName(), "INVALID_PARAMETER_TYPE", msg));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        var violations = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.FieldViolation(f.getField(),f.getCode(),f.getDefaultMessage()))
                .toList();
        var body = new ApiError(Instant.now(), 400, "Bad Request", "Validation failed",
                ((ServletWebRequest) request).getRequest().getRequestURI(), violations);
        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest req, List<ApiError.FieldViolation> violations) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), status.getReasonPhrase(),
                message, req.getRequestURI(),  violations == null ? List.of() : violations));
    }

    private List<ApiError.FieldViolation> violation(String field, String code, String message) {
        return List.of(new ApiError.FieldViolation(field, code, message));
    }
}
