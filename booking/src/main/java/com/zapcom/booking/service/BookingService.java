package com.zapcom.booking.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.zapcom.booking.globalexceptionhandler.UserNotFound;
import com.zapcom.booking.repository.BookingRepository;
import com.zapcom.common.model.Booking;
import com.zapcom.common.model.Cruise;
import com.zapcom.common.model.Cruiseline;
import com.zapcom.common.model.DatabaseSequence;
import com.zapcom.common.model.User;


@Service
public class BookingService {
	
	Logger log=LoggerFactory.getLogger(BookingService.class);

	@Autowired
	private BookingRepository  bookingRepository;
	

	@Autowired
	private MongoTemplate mongoTemplate;

	
	
	//@Autowired
	private RestTemplate restTemplate;
	
	
	public BookingService(RestTemplateBuilder builder) {
		
		this.restTemplate=builder.build();
		
	}

    // Create or Update Booking
    public Booking saveBooking(Booking booking,int uid,int cid) {
    	
    	User user=restTemplate.getForObject("http://localhost:8080/user/{id}", User.class, uid);
    	
    	if(user.getName()==null) {
    		throw new UserNotFound("user not found with the id "+ uid);
    	}
        Cruise cruise=restTemplate.getForObject("http://localhost:8081/cruise/{id}", Cruise.class, cid);
        int id=generateSequence("Booking");
        booking.setId(id);
        booking.setUser(user);
        booking.setCruise(cruise);
    	return bookingRepository.save(booking);
    }
    
    

	@Transactional
	public int generateSequence(String seqName) {
		Query query = Query.query(Criteria.where("_id").is(seqName));
		Update update = new Update().inc("seq", 1);

		DatabaseSequence counter = mongoTemplate.findAndModify(
				query,
				update,
				FindAndModifyOptions.options().returnNew(true).upsert(true), // Ensure upsert and return the new document after the update
				DatabaseSequence.class
				);
		if (counter == null) {
			counter = new DatabaseSequence();
			counter.setId(seqName);
			counter.setSeq(1);
			mongoTemplate.save(counter);
			return 1;
		}

		return counter != null ? (int) counter.getSeq() : 1;
	}


    // Get all Bookings
    public Page<Booking> getAllBookings(int page,int size) {
    	
    	Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findAll(pageable);
    }

//    // Get Booking by ID
//    public Optional<Booking> getBookingById(int id) {
//        Optional<Booking> booking= bookingRepository.findById(id);
//        if(booking.isPresent()) {
//        	return booking;
//        }
//        else {
//        	throw Bookingnothappen("you didnt book for this ")
//        }
//    }

    // Delete Booking by ID
    public boolean deleteBookingByUserandcruise(int id,String shipname) {
    	log.error(String.valueOf(id));
    	User user=restTemplate.getForObject("http://localhost:8080/user/{id}", User.class, id);
        Cruise cruise=restTemplate.getForObject("http://localhost:8081/cruisesByship/{shipname}", Cruise.class, shipname);
        if(user.getName()==null) {
    		throw new UserNotFound("user not booked with this userid "+ id);
    	}
        else
		{
			   bookingRepository.deleteByUserAndCruise(user,cruise);
			return true;
		}
    }

	public Page<Booking> getBookingByUser(int id,int page,int size) {
		Pageable pageable = PageRequest.of(page, size);
	User user=restTemplate.getForObject("http://localhost:8080/user/{id}", User.class, id);
	if(user.getName()!=null) {
		
		return bookingRepository.findByUser(user,pageable);
	}
		return null;
	}

	public Page<Booking> getBookingBycruise(String name,int page,int size) {
		Pageable pageable = PageRequest.of(page, size);
        Cruise cruise=restTemplate.getForObject("http://localhost:8081/cruisesByship/{shipname}", Cruise.class, name);
		return bookingRepository.findBycruise(cruise,pageable);
	}

	public Page<Booking> getBookingByCruiseLine(int id,int page,int size) {
		Pageable pageable = PageRequest.of(page, size);
		// TODO Auto-generated method stub
		  Cruiseline cruiseline=restTemplate.getForObject("http://localhost:8081/cruiseline/{id}", Cruiseline.class, id);
		  Cruise[] cruisesArray = restTemplate.getForObject(
				    "http://localhost:8081/cruisesBycruiseline/{cruiseline}?page=0&size=100", 
				    Cruise[].class, 
				    cruiseline.getName()
				);
				List<Cruise> cruises = Arrays.asList(cruisesArray);
				List<Booking> bookings = cruises.stream()
					    .flatMap(s -> bookingRepository.findBycruise(s,pageable).stream())
					    .collect(Collectors.toList());
				
				return new PageImpl<>(bookings, pageable, bookings.size());
//
//		return bookings;
	}

	public Page<Booking> getBookingByStartdestination(String startdestination,int page,int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		Cruise[] cruisesArray = restTemplate.getForObject(
			    "http://localhost:8081/cruisesBystartdestination/{startdestination}?page=0&size=100", 
			    Cruise[].class, 
			    startdestination
			);

				List<Cruise> cruises = Arrays.asList(cruisesArray);
				List<Booking> bookings = cruises.stream()
					    .flatMap(s -> bookingRepository.findBycruise(s,pageable).stream())
					    .collect(Collectors.toList());

		return new PageImpl<>(bookings, pageable, bookings.size());
				
	}

	public Page<Booking> getBookingByEnddestination(String enddestination,int page,int size) {
		Pageable pageable = PageRequest.of(page, size);
		Cruise[] cruisesArray = restTemplate.getForObject(
			    "http://localhost:8081/cruisesByenddestination/{enddestination}?page=0&size=100", 
			    Cruise[].class, 
			    enddestination
			);

				List<Cruise> cruises = Arrays.asList(cruisesArray);
				List<Booking> bookings = cruises.stream()
					    .flatMap(s -> bookingRepository.findBycruise(s,pageable).stream())
					    .collect(Collectors.toList());

				return new PageImpl<>(bookings, pageable, bookings.size());
	}

	public Page<Booking> getBookingByDateduration(String date1, String date2,int page,int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		
		
		String url = UriComponentsBuilder.fromHttpUrl("http://localhost:8081/cruises/BybetweenDates")
                .queryParam("startDate", date1)
                .queryParam("endDate", date2)
                .toUriString();
		// TODO Auto-generated method stub
		Cruise[] cruisesArray = restTemplate.getForObject(url, Cruise[].class);


				List<Cruise> cruises = Arrays.asList(cruisesArray);
				List<Booking> bookings = cruises.stream()
					    .flatMap(s -> bookingRepository.findBycruise(s,pageable).stream())
					    .collect(Collectors.toList());

				return new PageImpl<>(bookings, pageable, bookings.size());
	}
	
	
}
