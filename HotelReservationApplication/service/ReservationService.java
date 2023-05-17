package service;

import model.Customer;
import model.IRoom;
import model.Reservation;
import utils.Constants;

import java.util.*;
import java.util.stream.Collectors;

public class ReservationService {

    private static ReservationService reference;
    private final Map<String, IRoom> rooms;
    private final Map<String, Collection<Reservation>> reservations;

    private ReservationService() {
        this.rooms = new HashMap<>();
        this.reservations = new HashMap<>();
    }

    public static ReservationService getInstance() {
        if (reference == null) {
            reference = new ReservationService();
        }
        return reference;
    }

    public void addRoom(IRoom room) {
        if (room != null) {
            if (!rooms.containsKey(room.getRoomNumber())) {
                rooms.put(room.getRoomNumber(), room);
            }
        }
    }

    public IRoom getARoom(String roomId) {
        return rooms.get(roomId);
    }

    public Collection<IRoom> getAllRoom() {
        return rooms.values();
    }

    public Reservation reserveARoom(Customer customer, IRoom room, Date checkInDate, Date checkOutDate) {
        Collection<Reservation> customerReservations = getCustomersReservation(customer);
        if (customerReservations == null) {
            customerReservations = new ArrayList<>();
        }
        Reservation reservation = new Reservation(customer, room, checkInDate, checkOutDate);
        customerReservations.add(reservation);
        this.reservations.put(customer.getEmail(), customerReservations);

        return reservation;
    }

    public Collection<IRoom> findRooms(Date checkInDate, Date checkOutDate) {
        Collection<Reservation> allReservations = getAllReservations();
        Collection<IRoom> bookedRooms = new ArrayList<>();
        for (Reservation reservation : allReservations) {
            if (isBookedRoom(reservation, checkInDate, checkOutDate)) {
                bookedRooms.add(reservation.getRoom());
            }
        }
        Collection<IRoom> availableRooms = new ArrayList<>();
        for (IRoom room : rooms.values()) {
            if (!bookedRooms.contains(room)) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    public Collection<IRoom> findFreeRooms(Date checkInDate, Date checkOutDate) {
        Collection<IRoom> rooms = findRooms(checkInDate, checkOutDate);
        return rooms.stream().filter(room -> room.isFree()).collect(Collectors.toList());
    }

    public Collection<IRoom> findPaidRooms(Date checkInDate, Date checkOutDate) {
        Collection<IRoom> rooms = findRooms(checkInDate, checkOutDate);
        return rooms.stream().filter(room -> !room.isFree()).collect(Collectors.toList());
    }

    boolean isBookedRoom(Reservation reservation, Date checkInDate, Date checkOutDate) {
        return checkInDate.before(reservation.getCheckOutDate())
                && checkOutDate.after(reservation.getCheckInDate());
    }

    private Collection<Reservation> getAllReservations() {
        Collection<Reservation> result = new ArrayList<>();
        for (Collection<Reservation> reservations : reservations.values()) {
            result.addAll(reservations);
        }
        return result;
    }

    public Collection<Reservation> getCustomersReservation(Customer customer) {
        return reservations.get(customer.getEmail());
    }

    public void printAllReservation() {
        Collection<Reservation> allReservations = getAllReservations();
        if (allReservations.isEmpty()) {
            System.out.println(Constants.MSG_RESERVATION_NOT_FOUND);
        } else {
            System.out.println("Reservations:");
            for (Reservation reservation : allReservations) {
                System.out.println(reservation);
            }
        }
    }
}
