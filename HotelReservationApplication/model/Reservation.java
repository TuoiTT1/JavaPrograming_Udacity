package model;

import utils.Constants;

import java.util.Date;

public class Reservation implements Comparable {
    private Customer customer;
    private IRoom room;
    private Date checkInDate;
    private Date checkOutDate;

    public Reservation(Customer customer, IRoom room, Date checkInDate, Date checkOutDate) {
        this.customer = customer;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public IRoom getRoom() {
        return room;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    @Override
    public String toString() {
        return "Reservation:" +
                customer +
                ", \n" + room +
                ", \nCheck in date: " + Constants.DATE_FORMATTER.format(checkInDate) +
                ", \nCheck out date: " + Constants.DATE_FORMATTER.format(checkOutDate);
    }


    @Override
    public int compareTo(Object o) {
        Reservation reservation = (Reservation) o;
        return this.checkInDate.compareTo(reservation.getCheckInDate());
    }
}
