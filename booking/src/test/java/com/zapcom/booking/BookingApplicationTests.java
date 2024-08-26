package com.zapcom.booking;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.zapcom.booking.controller.BookingController;
import com.zapcom.booking.service.BookingService;
import com.zapcom.common.model.Booking;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class BookingApplicationTests {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private Booking booking;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        booking = new Booking(); // Initialize with necessary fields
    }

    @Test
    public void testCreateBooking() {
        when(bookingService.saveBooking(any(Booking.class), eq(1), eq(2))).thenReturn(booking);

        ResponseEntity<Booking> response = bookingController.createBooking(booking, 1, 2);

        verify(bookingService, times(1)).saveBooking(any(Booking.class), eq(1), eq(2));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(booking, response.getBody());
    }

    @Test
    public void testGetAllBookings() {
        Page<Booking> page = new PageImpl<>(Arrays.asList(booking));
        when(bookingService.getAllBookings(0, 10)).thenReturn(page);

        ResponseEntity<List<Booking>> response = bookingController.getAllBookings(0, 10);

        verify(bookingService, times(1)).getAllBookings(0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page.getContent(), response.getBody());
    }

    @Test
    public void testDeleteBookingByUserandcruise() {
        when(bookingService.deleteBookingByUserandcruise(1, "shipName")).thenReturn(true);

        ResponseEntity<String> response = bookingController.deleteBookingByUserandcruise(1, "shipName");

        verify(bookingService, times(1)).deleteBookingByUserandcruise(1, "shipName");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("you have cancelled your booking succesfully", response.getBody());
    }

    @Test
    public void testGetBookingByUser() {
        Page<Booking> page = new PageImpl<>(Arrays.asList(booking));
        when(bookingService.getBookingByUser(1, 0, 10)).thenReturn(page);

        ResponseEntity<List<Booking>> response = bookingController.getBookingByUser(1, 0, 10);

        verify(bookingService, times(1)).getBookingByUser(1, 0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page.getContent(), response.getBody());
    }

    @Test
    public void testGetBookingByCruise() {
        Page<Booking> page = new PageImpl<>(Arrays.asList(booking));
        when(bookingService.getBookingBycruise("cruiseName", 0, 10)).thenReturn(page);

        ResponseEntity<List<Booking>> response = bookingController.getBookingBycruise("cruiseName", 0, 10);

        verify(bookingService, times(1)).getBookingBycruise("cruiseName", 0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page.getContent(), response.getBody());
    }

    @Test
    public void testGetBookingByCruiseLine() {
        Page<Booking> page = new PageImpl<>(Arrays.asList(booking));
        when(bookingService.getBookingByCruiseLine(1, 0, 10)).thenReturn(page);

        ResponseEntity<List<Booking>> response = bookingController.getBookingByCruiseLine(1, 0, 10);

        verify(bookingService, times(1)).getBookingByCruiseLine(1, 0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page.getContent(), response.getBody());
    }

    @Test
    public void testGetBookingByStartdestination() {
        Page<Booking> page = new PageImpl<>(Arrays.asList(booking));
        when(bookingService.getBookingByStartdestination("startDest", 0, 10)).thenReturn(page);

        ResponseEntity<List<Booking>> response = bookingController.getBookingByStartdestination("startDest", 0, 10);

        verify(bookingService, times(1)).getBookingByStartdestination("startDest", 0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page.getContent(), response.getBody());
    }

    @Test
    public void testGetBookingByEnddestination() {
        Page<Booking> page = new PageImpl<>(Arrays.asList(booking));
        when(bookingService.getBookingByEnddestination("endDest", 0, 10)).thenReturn(page);

        ResponseEntity<List<Booking>> response = bookingController.getBookingByEnddestination("endDest", 0, 10);

        verify(bookingService, times(1)).getBookingByEnddestination("endDest", 0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page.getContent(), response.getBody());
    }

    @Test
    public void testGetBookingByDateduration() {
        Page<Booking> page = new PageImpl<>(Arrays.asList(booking));
        when(bookingService.getBookingByDateduration("2024-01-01", "2024-01-31", 0, 10)).thenReturn(page);

        ResponseEntity<List<Booking>> response = bookingController.getBookingByDateduration("2024-01-01", "2024-01-31", 0, 10);

        verify(bookingService, times(1)).getBookingByDateduration("2024-01-01", "2024-01-31", 0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page.getContent(), response.getBody());
    }
}

