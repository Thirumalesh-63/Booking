package com.zapcom.booking.globalexceptionhandler;

public class UserNotFound extends RuntimeException{
	
	public UserNotFound(String msg){
		super(msg);
	}

}
