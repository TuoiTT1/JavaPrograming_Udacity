package api;

import exception.DataExistsException;
import model.Customer;
import model.IRoom;
import model.Reservation;
import service.CustomerService;
import service.ReservationService;

import java.util.Collection;
import java.util.List;

public class AdminResource {
    private static AdminResource reference;
    private final CustomerService customerService;
    private final ReservationService reservationService;

    private AdminResource() {
        this.customerService = CustomerService.getInstance();
        this.reservationService = ReservationService.getInstance();
    }

    public static AdminResource getInstance() {
        if (reference == null) {
            reference = new AdminResource();
        }
        return reference;
    }

    public Customer getCustomer(String email) {
        return customerService.getCustomer(email);
    }

    public void addRoom(List<IRoom> rooms) {
        if (rooms != null && !rooms.isEmpty()) {
            for (IRoom room : rooms) {
                reservationService.addRoom(room);
            }
        }
    }

    public Collection<IRoom> getAllRooms() {
        return reservationService.getAllRoom();
    }

    public Collection<Customer> getAllCustomers() {
        return customerService.getAllCustomer();
    }

    public void displayAllReservations() {
        reservationService.printAllReservation();
    }

    public void addCustomers(List<Customer> customers) {
        for (Customer customer : customers) {
            try {
                customerService.addCustomer(customer.getEmail(), customer.getFirstName(), customer.getLastName());
            } catch (DataExistsException e) {
                System.out.println(e.getLocalizedMessage());
            }
        }
    }

    public void addReservations(List<Reservation> reservations) {
        for (Reservation reservation : reservations) {
            reservationService.reserveARoom(reservation.getCustomer(), reservation.getRoom(), reservation.getCheckInDate(), reservation.getCheckOutDate());
        }
    }
}
