package com.zapcom.booking.globalexceptionhandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class globalexceptionhandler {
	@ExceptionHandler(UserNotFound.class)
	public ResponseEntity<String> usernotfound(UserNotFound exception){
		return  ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(exception.getMessage());
	}

}
