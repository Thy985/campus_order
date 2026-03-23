package com.xingchen.backend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.xingchen.backend.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

/**
 * Global Exception Handler
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle business exception
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("Business exception: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * Handle not login exception
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<?> handleNotLoginException(NotLoginException e) {
        log.error("Not login exception: {}", e.getMessage());
        String message = "Please login first";

        // Different message based on exception type
        if (e.getType().equals(NotLoginException.NOT_TOKEN)) {
            message = "No login credentials provided";
        } else if (e.getType().equals(NotLoginException.INVALID_TOKEN)) {
            message = "Invalid login credentials";
        } else if (e.getType().equals(NotLoginException.TOKEN_TIMEOUT)) {
            message = "Login expired, please login again";
        } else if (e.getType().equals(NotLoginException.BE_REPLACED)) {
            message = "Account logged in from another device";
        } else if (e.getType().equals(NotLoginException.KICK_OUT)) {
            message = "Account has been kicked out";
        }

        return Result.error(401, message);
    }

    /**
     * Handle not permission exception
     */
    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        log.error("Not permission exception: {}", e.getMessage());
        return Result.error(403, "No permission to access this resource");
    }

    /**
     * Handle not role exception
     */
    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleNotRoleException(NotRoleException e) {
        log.error("Not role exception: {}", e.getMessage());
        return Result.error(403, "No permission to access this resource");
    }

    /**
     * Handle validation exception (RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Parameter validation failed";
        log.error("Validation exception: {}", message);
        return Result.error(400, message);
    }

    /**
     * Handle validation exception (Form)
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Parameter validation failed";
        log.error("Bind exception: {}", message);
        return Result.error(400, message);
    }

    /**
     * Handle constraint violation exception
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String message = violations.stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Parameter validation failed");
        log.error("Constraint violation: {}", message);
        return Result.error(400, message);
    }

    /**
     * Handle file size exceeded exception
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("File size exceeded: {}", e.getMessage());
        return Result.error(400, "File size exceeds limit");
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("System exception: ", e);
        String message = e.getMessage() != null ? e.getMessage() : "System error, please try again later";
        return Result.error(500, message);
    }
}
