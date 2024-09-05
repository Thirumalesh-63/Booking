package com.zapcom.booking.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.zapcom.common.model.Booking;
import com.zapcom.common.model.Cruise;
import com.zapcom.common.model.User;

@Repository
public interface BookingRepository extends MongoRepository<Booking, Integer>{
	
	
//	@Query("{'cruise.startdestination':?0}")
//	public Booking findByStartdestination(String Startdestination);

	public Page<Booking> findByUser(User user, Pageable pageable);

	public void deleteByUserAndCruise(User user,Cruise cruise);

	public Page<Booking> findBycruise(Cruise cruise, Pageable pageable);
	public List<Booking> findBycruise(Cruise cruise);

	public Booking findByUserAndCruise(User user, Cruise cruise);

}
