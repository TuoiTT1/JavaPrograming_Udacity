package api;

import exception.DataExistsException;
import model.Customer;
import model.IRoom;
import model.Reservation;
import service.CustomerService;
import service.ReservationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class HotelResource {
    private static HotelResource reference;

    private final ReservationService reservationService;
    private final CustomerService customerService;

    private HotelResource() {
        this.reservationService = ReservationService.getInstance();
        this.customerService = CustomerService.getInstance();
    }

    public static HotelResource getInstance() {
        if (reference == null) {
            reference = new HotelResource();
        }
        return reference;
    }

    public Customer getCustomer(String email) {
        return customerService.getCustomer(email);
    }

    public void createACustomer(String email, String firstName, String lastName) throws IllegalArgumentException, DataExistsException {
        CustomerService.getInstance().addCustomer(email, firstName, lastName);
    }

    public IRoom getRoom(String roomNumber) {
        return reservationService.getARoom(roomNumber);
    }

    public Reservation bookARoom(String customerEmail, IRoom room, Date checkInDate, Date checkoutDate) {
        return this.reservationService.reserveARoom(getCustomer(customerEmail), room, checkInDate, checkoutDate);
    }

    public Collection<Reservation> getCustomerReservations(String customerEmail) {
        Customer customer = getCustomer(customerEmail);
        if (customer == null) {
            return new ArrayList<>();
        }
        return this.reservationService.getCustomersReservation(customer);
    }

    public Collection<IRoom> findARoom(Date checkIn, Date checkOut) {
        return this.reservationService.findRooms(checkIn, checkOut);
    }

    public Collection<IRoom> findFreeRooms(Date checkIn, Date checkOut) {
        return this.reservationService.findFreeRooms(checkIn, checkOut);
    }

    public Collection<IRoom> findPaidRooms(Date checkIn, Date checkOut) {
        return this.reservationService.findPaidRooms(checkIn, checkOut);
    }

    public void printRooms(Collection<IRoom> rooms) {
        if (!rooms.isEmpty()) {
            rooms.forEach(System.out::println);
        }
    }

}
