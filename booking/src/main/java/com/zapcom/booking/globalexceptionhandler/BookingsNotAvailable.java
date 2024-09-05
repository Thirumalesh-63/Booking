package com.zapcom.booking.globalexceptionhandler;

public class BookingsNotAvailable extends RuntimeException{
	
	public BookingsNotAvailable(String msg){
		super(msg);
	}

}
