package com.homeservice.service;

import com.homeservice.dao.BookingDAO;
import com.homeservice.model.Booking;

import java.sql.Date;
import java.util.List;

public class BookingService {
    private final BookingDAO bookingDAO = new BookingDAO();

    public int createBooking(int customerId, int providerId, int categoryId, Date serviceDate,
                             String timeSlot, String address, String problemDescription, double totalCost) {
        Booking b = new Booking();
        b.setCustomerId(customerId);
        b.setProviderId(providerId);
        b.setCategoryId(categoryId);
        b.setServiceDate(serviceDate);
        b.setTimeSlot(timeSlot);
        b.setAddress(address);
        b.setProblemDescription(problemDescription);
        b.setStatus("PENDING");
        b.setTotalCost(totalCost);
        return bookingDAO.createBooking(b);
    }

    public boolean acceptBooking(int bookingId) {
        return bookingDAO.updateStatus(bookingId, "ACCEPTED");
    }

    public boolean rejectBooking(int bookingId) {
        return bookingDAO.updateStatus(bookingId, "REJECTED");
    }

    public boolean startBooking(int bookingId) {
        return bookingDAO.updateStatus(bookingId, "IN_PROGRESS");
    }

    public boolean completeBooking(int bookingId) {
        return bookingDAO.updateStatus(bookingId, "COMPLETED");
    }

    public boolean cancelBooking(int bookingId) {
        return bookingDAO.updateStatus(bookingId, "CANCELLED");
    }

    public List<Booking> getCustomerBookings(int customerId) {
        return bookingDAO.getBookingsByCustomer(customerId);
    }

    public List<Booking> getProviderBookings(int providerId) {
        return bookingDAO.getBookingsByProvider(providerId);
    }

    public List<Booking> getAllBookings() {
        return bookingDAO.getAllBookings();
    }
}
