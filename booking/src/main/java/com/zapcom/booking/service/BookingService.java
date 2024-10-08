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
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.zapcom.booking.globalexceptionhandler.BookingsNotAvailable;
import com.zapcom.booking.globalexceptionhandler.UserNotFound;
import com.zapcom.booking.repository.BookingRepository;
import com.zapcom.common.model.Booking;
import com.zapcom.common.model.Cruise;
import com.zapcom.common.model.Cruiseline;
import com.zapcom.common.model.DatabaseSequence;
import com.zapcom.common.model.User;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class BookingService {

	Logger log = LoggerFactory.getLogger(BookingService.class);

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	private RestTemplate restTemplate;

	public BookingService(RestTemplateBuilder builder) {

		this.restTemplate = builder.build();

	}

	// Create or Update Booking
	public Booking saveBooking(Booking booking, int uid, String cruiseName) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtToken = request.getHeader("Authorization"); // Retrieve the token
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", jwtToken); // Use the same token
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<User> response = restTemplate.exchange("http://localhost:8083/userregistry/user/{id}",
				HttpMethod.GET, entity, User.class, uid);
		User user = response.getBody();
		if (user == null) {
			throw new UserNotFound("user not found with the id " + uid);
		}
		ResponseEntity<Cruise> response2 = restTemplate.exchange(
				"http://localhost:8083/shipmanagement//cruises/cruisesBycruiseName/{cruiseName}", HttpMethod.GET,
				entity, Cruise.class, cruiseName);
		Cruise cruise = response2.getBody();
		int id = generateSequence("Booking");
		booking.setId(id);
		booking.setUser(user);
		booking.setCruise(cruise);
		return bookingRepository.save(booking);
	}

	@Transactional
	public int generateSequence(String seqName) {
		Query query = Query.query(Criteria.where("_id").is(seqName));
		Update update = new Update().inc("seq", 1);

		DatabaseSequence counter = mongoTemplate.findAndModify(query, update,
				FindAndModifyOptions.options().returnNew(true).upsert(true), // Ensure upsert and return the new
																				// document after the update
				DatabaseSequence.class);
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
	public Page<Booking> getAllBookings(int page, int size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<Booking> pages = bookingRepository.findAll(pageable);
		if (pages.getContent().size() == 0)
			throw new BookingsNotAvailable("bookings not found in this page " + page);
		return pages;
	}

	public boolean cancelBookingByUserandcruise(int id, String cruiseName) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtToken = request.getHeader("Authorization"); // Retrieve the token

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", jwtToken); // Use the same token

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<User> response = restTemplate.exchange("http://localhost:8083/userregistry/user/{id}",
				HttpMethod.GET, entity, User.class, id);

		User user = response.getBody();

		ResponseEntity<Cruise> response2 = restTemplate.exchange(
				"http://localhost:8083/shipmanagement/cruises/cruisesBycruiseName/{cruiseName}", HttpMethod.GET, entity,
				Cruise.class, cruiseName);

		Cruise cruise = response2.getBody();
		Booking book = bookingRepository.findByUserAndCruise(user, cruise);
		if (book == null) {
			throw new BookingsNotAvailable("user not booked for this cruise " + cruiseName);
		} else {
			bookingRepository.deleteByUserAndCruise(user, cruise);
			return true;
		}
	}

	public Page<Booking> getBookingByUser(String email, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtToken = request.getHeader("Authorization"); // Retrieve the token

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", jwtToken); // Use the same token

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<User> response = restTemplate.exchange("http://localhost:8083/userregistry/userbyemail/{email}",
				HttpMethod.GET, entity, User.class, email);

		User user = response.getBody();

		if (user.getName() != null) {
			Page<Booking> book = bookingRepository.findByUser(user, pageable);
			if (book.getContent().size() != 0) {
				return book;
			}
			throw new BookingsNotAvailable("booking bot avialable in this page " + page);
		} else
			throw new UserNotFound("user not found with this email " + email);
	}

	public Page<Booking> getBookingBycruise(String cruiseName, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtToken = request.getHeader("Authorization"); // Retrieve the token

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", jwtToken); // Use the same token

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Cruise> response = restTemplate.exchange(
				"http://localhost:8083/shipmanagement/cruises/cruisesBycruiseName/{cruiseName}", HttpMethod.GET, entity,
				Cruise.class, cruiseName);

		Cruise cruise = response.getBody();
		Page<Booking> pages = bookingRepository.findBycruise(cruise, pageable);
		if (pages.getContent().size() == 0)
			throw new BookingsNotAvailable("bookings not found for the cruise " + cruiseName + " in this page " + page);
		return pages;
	}

	public Page<Booking> getBookingByCruiseLine(String cruiselinename, int page, int size) {

		System.err.println(size);
		Pageable pageable = PageRequest.of(page, size);

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtToken = request.getHeader("Authorization"); // Retrieve the token

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", jwtToken); // Use the same token

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Cruiseline> response = restTemplate.exchange(
				"http://localhost:8083/shipmanagement/admin/cruiselinebyname/{cruiselinename}", HttpMethod.GET, entity, Cruiseline.class,
				cruiselinename);
		Cruiseline cruiseline = response.getBody();
		ResponseEntity<Cruise[]> response2 = restTemplate.exchange(
				"http://localhost:8083/shipmanagement/cruisesBycruiseline/{cruiseline}", HttpMethod.GET, entity,
				Cruise[].class, cruiseline.getName());

		Cruise[] cruisesArray = response2.getBody();
		List<Cruise> cruises = Arrays.asList(cruisesArray);

		List<Booking> bookings = cruises.stream().flatMap(s -> bookingRepository.findBycruise(s, pageable).stream())
				.collect(Collectors.toList());

		int start = Math.min((int) pageable.getOffset(), bookings.size());
		int end = Math.min((start + pageable.getPageSize()), bookings.size());
		List<Booking> paginatedList = bookings.subList(start, end);
		Page<Booking> pages = new PageImpl<>(paginatedList, pageable, bookings.size());
		if (pages.getContent().size() == 0)
			throw new BookingsNotAvailable(
					"bookings not found for the cruiseline " + cruiseline.getName() + " in this page " + page);
		return pages;

	}

	public Page<Booking> getBookingByStartdestination(String startdestination, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtToken = request.getHeader("Authorization"); // Retrieve the token

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", jwtToken); // Use the same token

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Cruise[]> response2 = restTemplate.exchange(
				"http://localhost:8083/shipmanagement/cruisesBystartdestination/{startdestination}", HttpMethod.GET,
				entity, Cruise[].class, startdestination);

		Cruise[] cruisesArray = response2.getBody();
		List<Cruise> cruises = Arrays.asList(cruisesArray);
		List<Booking> bookings = cruises.stream().flatMap(s -> bookingRepository.findBycruise(s).stream())
				.collect(Collectors.toList());
		int start = Math.min((int) pageable.getOffset(), bookings.size());
		int end = Math.min((start + pageable.getPageSize()), bookings.size());
		List<Booking> paginatedList = bookings.subList(start, end);
		Page<Booking> pages = new PageImpl<>(paginatedList, pageable, bookings.size());
		if (pages.getContent().size() == 0)
			throw new BookingsNotAvailable(
					"bookings not found for the startdestination " + startdestination + " in this page " + page);
		return pages;
	}

	public Page<Booking> getBookingByEnddestination(String enddestination, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtToken = request.getHeader("Authorization"); // Retrieve the token

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", jwtToken); // Use the same token

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Cruise[]> response2 = restTemplate.exchange(
				"http://localhost:8083/shipmanagement/cruisesByenddestination/{enddestination}", HttpMethod.GET, entity,
				Cruise[].class, enddestination);
		Cruise[] cruisesArray = response2.getBody();
		List<Cruise> cruises = Arrays.asList(cruisesArray);
		List<Booking> bookings = cruises.stream().flatMap(s -> bookingRepository.findBycruise(s).stream())
				.collect(Collectors.toList());
		int start = Math.min((int) pageable.getOffset(), bookings.size());
		int end = Math.min((start + pageable.getPageSize()), bookings.size());
		List<Booking> paginatedList = bookings.subList(start, end);
		Page<Booking> pages = new PageImpl<>(paginatedList, pageable, bookings.size());
		if (pages.getContent().size() == 0)
			throw new BookingsNotAvailable(
					"bookings not found for the endestination " + enddestination + " in this page " + page);
		return pages;
	}

	public Page<Booking> getBookingByDateduration(String date1, String date2, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);
		String url = UriComponentsBuilder.fromHttpUrl("http://localhost:8083/shipmanagement/cruises/BybetweenDates")
				.queryParam("startDate", date1).queryParam("endDate", date2).toUriString();

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String jwtToken = request.getHeader("Authorization"); // Retrieve the token

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", jwtToken); // Use the same token

		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Cruise[]> response2 = restTemplate.exchange(url, HttpMethod.GET, entity, Cruise[].class);

		Cruise[] cruisesArray = response2.getBody();

		List<Cruise> cruises = Arrays.asList(cruisesArray);
		List<Booking> bookings = cruises.stream().flatMap(s -> bookingRepository.findBycruise(s).stream())
				.collect(Collectors.toList());
		int start = Math.min((int) pageable.getOffset(), bookings.size());
		int end = Math.min((start + pageable.getPageSize()), bookings.size());
		List<Booking> paginatedList = bookings.subList(start, end);
		Page<Booking> pages = new PageImpl<>(paginatedList, pageable, bookings.size());
		if (pages.getContent().size() == 0)
			throw new BookingsNotAvailable(
					"bookings not found for the between dates " + date1 + " date2 " + " in this page " + page);
		return pages;
	}

}
