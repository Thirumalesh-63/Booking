package com.zapcom.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zapcom.booking.service.BookingService;
import com.zapcom.common.model.Booking;

@RestController
public class BookingController {
	
	
	@Autowired
	private BookingService bookingService;
	
	// Create or Update Booking
    @PostMapping("/booking/{uid}/{cid}")
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking,@PathVariable int uid,@PathVariable int cid) {
    	
        Booking savedBooking = bookingService.saveBooking(booking,uid,cid);
        return new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
    }

    // Get All Bookings
    @GetMapping("/booking")
    public ResponseEntity<List<Booking>> getAllBookings(
    		@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        Page<Booking> bookings = bookingService.getAllBookings(page,size);
        return new ResponseEntity<>(bookings.getContent(), HttpStatus.OK);
    }


    // Delete Booking by ID
    @DeleteMapping("/booking/{id}/{shipname}")
    public ResponseEntity<String> deleteBookingByUserandcruise(@PathVariable int id,@PathVariable String shipname) {
       
        boolean isDeleted =  bookingService.deleteBookingByUserandcruise(id,shipname);
	    if (isDeleted) {
	        return new ResponseEntity<>("you have cancelled your booking succesfully",HttpStatus.OK);
	    } else {
	        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    }
    }
    
    
    @GetMapping("/booking/ByUser/{id}")
    public ResponseEntity<List<Booking>> getBookingByUser(@PathVariable int id,
    		@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        Page<Booking> bookings = bookingService.getBookingByUser(id,page,size);
        return new ResponseEntity<>(bookings.getContent(), HttpStatus.OK);
    }
    
    
    @GetMapping("/booking/ByCruise/{id}")
    public ResponseEntity<List<Booking>> getBookingBycruise(@PathVariable String name,
    		@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        Page<Booking> bookings = bookingService.getBookingBycruise(name,page,size);
        return new ResponseEntity<>(bookings.getContent(), HttpStatus.OK);
    }
    
    @GetMapping("/booking/ByCruiseLine/{id}")
    public ResponseEntity<List<Booking>> getBookingByCruiseLine(@PathVariable int id,
    		@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        Page<Booking> bookings = bookingService.getBookingByCruiseLine(id,page,size);
        return new ResponseEntity<>(bookings.getContent(), HttpStatus.OK);
    }
    @GetMapping("/booking/ByStartdestination/{startdestination}")
    public ResponseEntity<List<Booking>> getBookingByStartdestination(@PathVariable String startdestination,
    		@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
        Page<Booking> bookings = bookingService.getBookingByStartdestination(startdestination,page,size);
        return new ResponseEntity<>(bookings.getContent(), HttpStatus.OK);
    }
    @GetMapping("/booking/Byenddestination/{enddestination}")
    public ResponseEntity<List<Booking>> getBookingByEnddestination(@PathVariable String enddestination,
    		@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
    	
        Page<Booking> bookings = bookingService.getBookingByEnddestination(enddestination,page,size);
        return new ResponseEntity<>(bookings.getContent(), HttpStatus.OK);
    }
    @GetMapping("/booking/ByDateduration/{date1}/{date2}")
    public ResponseEntity<List<Booking>> getBookingByDateduration(@PathVariable String date1,@PathVariable String date2,
    		@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size) {
    	
    	System.err.println(date1);
        Page<Booking> bookings = bookingService.getBookingByDateduration(date1,date2,page,size);
        return new ResponseEntity<>(bookings.getContent(), HttpStatus.OK);
    }

	

}
