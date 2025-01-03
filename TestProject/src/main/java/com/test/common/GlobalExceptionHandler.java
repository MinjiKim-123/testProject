package com.test.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger HANDLER_LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	/**
	 * validation 오류
	 */
	@ExceptionHandler(exception = { MethodArgumentNotValidException.class, ConstraintViolationException.class })
	public void handlerValidationException(Exception ex) {
		
		String errorMessage = null;
		
		if(ex instanceof BindException bindException) {
			List<FieldError> fieldErrors = bindException.getBindingResult().getFieldErrors();
			
			if(fieldErrors != null && !fieldErrors.isEmpty())
				errorMessage = fieldErrors.get(0).getDefaultMessage();
		
		}else if(ex instanceof ConstraintViolationException constraintViolationException 
				&& constraintViolationException.getConstraintViolations() != null && !constraintViolationException.getConstraintViolations().isEmpty()) {
			@SuppressWarnings("rawtypes")
			ConstraintViolation constraintViolation = constraintViolationException.getConstraintViolations().iterator().next();
			errorMessage = constraintViolation.getMessageTemplate();		
		}
		
		if (errorMessage.isBlank())
			errorMessage = "입력 값을 확인해주세요.";
		
		HANDLER_LOGGER.error(errorMessage, ex);
	}
	
	/**
	 * 그 외 모든 처리되지 않은 오류 처리
	 */
	@ExceptionHandler
	public void handlerException(Exception ex){
		HANDLER_LOGGER.error(ex.getMessage(), ex);
	}
	
}
