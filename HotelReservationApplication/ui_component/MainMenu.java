package ui_component;

import api.HotelResource;
import exception.DataExistsException;
import model.IRoom;
import model.Reservation;
import utils.Constants;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainMenu {
    private static final HotelResource hotelResource = HotelResource.getInstance();

    public static void execute() {
        Scanner inp = new Scanner(System.in);
        String chooseOption;
        boolean isNotExit;
        System.out.println("Welcome to the Hotel Reservation Application");
        try {
            do {
                System.out.println("_______________________________________________");
                System.out.println("1. Find and reserve a room");
                System.out.println("2. See my reservations");
                System.out.println("3. Create an account");
                System.out.println("4. Admin");
                System.out.println("5. Exit");
                System.out.println("_______________________________________________");
                System.out.println("Please select a number for the main menu option");

                chooseOption = inp.nextLine().trim();
                switch (chooseOption) {
                    case "1" -> findAndReserveRoom(inp);
                    case "2" -> displayCustomerReservations(inp);
                    case "3" -> createAccount(inp);
                    case "4" -> AdminMenu.execute();
                    default -> {
                    }
                }
                isNotExit = !"5".equals(chooseOption);
            } while (isNotExit);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            System.out.println("Something went wrong, please restart application");
            execute();
        }

    }

    private static void findAndReserveRoom(Scanner inp) {
        System.out.println("Enter check in date:");
        Date checkIn = enterDate(inp);
        Date checkOut;
        do {
            System.out.println("Enter check out date:");
            checkOut = enterDate(inp);
            if (checkOut.before(checkIn)) {
                System.out.println("Check out date must not be less than check in date.");
            }
        } while (checkOut.before(checkIn));
        System.out.println("Enter category room: 1 (All) or 2 (Paid) or 3 (Free)");
        int categoryRoom = enterCategoryRoom(inp);

        Collection<IRoom> searchRooms = findAvailableRoom(checkIn, checkOut, categoryRoom);
        if (!searchRooms.isEmpty()) {
            System.out.println("List rooms found in range: from " + Constants.DATE_FORMATTER.format(checkIn) + " to " + Constants.DATE_FORMATTER.format(checkOut) + ": ");
            hotelResource.printRooms(searchRooms);
        } else {
            System.out.println("All rooms from " + Constants.DATE_FORMATTER.format(checkIn) + " to " + Constants.DATE_FORMATTER.format(checkOut) + " have been booked.");
            System.out.println("You can enter the number of days for us to recommend in case the room is available:");
            int recommendDays = enterRecommendDays(inp);
            checkIn = addDays(checkIn, recommendDays);
            checkOut = addDays(checkOut, recommendDays);
            searchRooms = findAvailableRoom(checkIn, checkOut, categoryRoom);
            if (searchRooms.isEmpty()) {
                System.out.println(Constants.MSG_ROOM_NOT_FOUND);
            } else {
                System.out.println("We found list rooms in range from " + Constants.DATE_FORMATTER.format(checkIn) + " to " + Constants.DATE_FORMATTER.format(checkOut) + ": ");
                hotelResource.printRooms(searchRooms);
            }
        }
        reserveRoom(searchRooms, inp, checkIn, checkOut);
    }

    private static Collection<IRoom> findAvailableRoom(Date checkIn, Date checkOut, int categoryRoom) {
        switch (categoryRoom) {
            case 1 -> {
                return hotelResource.findARoom(checkIn, checkOut);
            }
            case 2 -> {
                return hotelResource.findPaidRooms(checkIn, checkOut);
            }
            case 3 -> {
                return hotelResource.findFreeRooms(checkIn, checkOut);
            }
            default -> {
            }
        }
        return new ArrayList<>();
    }

    private static void reserveRoom(Collection<IRoom> searchRooms, Scanner inp, Date checkIn, Date checkOut) {
        if (!searchRooms.isEmpty()) {
            System.out.println("Would you like to book a room? y/n");
            String option = enterOptionYOrN(inp);
            if (Constants.YES.equalsIgnoreCase(option)) {
                String email = enterEmailForReserve(inp);
                if (!email.isBlank()) {
                    System.out.println("What room number would you like to reserve:");
                    String roomNumber = inp.nextLine();
                    IRoom room = hotelResource.getRoom(roomNumber);
                    if (room != null && searchRooms.contains(room)) {
                        Reservation completedReservation = hotelResource.bookARoom(email, room, checkIn, checkOut);
                        System.out.println("Reservation has been create successfully.");
                        System.out.println(completedReservation);
                    } else {
                        System.out.println("Room number not available.");
                    }
                }
            }
        }
    }

    private static String enterEmailForReserve(Scanner inp) {
        System.out.println("Do you have an account with us? y/n");
        String option = enterOptionYOrN(inp);
        String email;
        if (Constants.YES.equalsIgnoreCase(option)) {
            System.out.println("Enter email (format name@domain.com):");
            email = inp.nextLine();
            if (hotelResource.getCustomer(email) == null) {
                System.out.println(Constants.MSG_CUSTOMER_NOT_FOUND + ". You may need to create an account");
                email = enterEmailForReserve(inp);
            }
        } else {
            System.out.println("Create an account:");
            email = createAccount(inp);
            if (email.isBlank()) {
                email = enterEmailForReserve(inp);
            }
        }
        return email;
    }

    private static Date enterDate(Scanner inp) {
        String dateInp = inp.nextLine();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        try {
            Date date = Date.from(LocalDate.parse(dateInp, dateTimeFormatter).atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
            Date today = Date.from(LocalDate.now().atStartOfDay()
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
            if (date.before(today)) {
                System.out.println("You cannot enter past date, please re-enter date:");
                return enterDate(inp);
            } else {
                return date;
            }
        } catch (DateTimeException e) {
            System.out.println("Invalid date, please enter date in the format MM/dd/yyyy");
            return enterDate(inp);
        }
    }

    private static void displayCustomerReservations(Scanner inp) {
        System.out.println("Enter email (format name@domain.com): ");
        String email = inp.nextLine();

        Collection<Reservation> customerReservations = hotelResource.getCustomerReservations(email);
        if (customerReservations == null || customerReservations.isEmpty()) {
            System.out.println(Constants.MSG_RESERVATION_NOT_FOUND);
        } else {
            System.out.println("My reservations: ");
            for (Reservation reservation : customerReservations) {
                System.out.println(reservation);
            }
        }
    }

    private static String createAccount(Scanner inp) {
        System.out.println("Enter first name:");
        String firstName = inp.nextLine();
        System.out.println("Enter last name:");
        String lastName = inp.nextLine();
        System.out.println("Enter email (format name@domain.com):");
        String email = Constants.BLANK;
        try {
            email = inp.nextLine();
            hotelResource.createACustomer(email, firstName, lastName);
            System.out.println("Customer has been created successfully.");
        } catch (IllegalArgumentException e) {
            email = Constants.BLANK;
            System.out.println("Invalid email format (name@domain.com)");
        } catch (DataExistsException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return email;
    }

    private static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
//        calendar.add(Calendar.DATE, Constants.RECOMMENDED_ROOMS_PLUS_DAYS);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    private static int enterRecommendDays(Scanner inp) {
        String line = inp.nextLine();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number");
            return enterRecommendDays(inp);
        }
    }

    private static String enterOptionYOrN(Scanner inp) {
        String option = inp.nextLine().trim();
        while (!Constants.YES.equalsIgnoreCase(option) && !Constants.NO.equalsIgnoreCase(option)) {
            System.out.println("Please enter y (yes) or n (no):");
            option = inp.nextLine().trim();
        }
        return option;
    }

    private static int enterCategoryRoom(Scanner inp) {
        String option = inp.nextLine().trim();
        while (!"1".equals(option) && !"2".equalsIgnoreCase(option) && !"3".equalsIgnoreCase(option)) {
            System.out.println("Please enter 1 (All) or 2 (Paid) or 3 (Free):");
            option = inp.nextLine().trim();
        }
        return Integer.parseInt(option);
    }

}
