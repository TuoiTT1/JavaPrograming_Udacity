package ui_component;

import api.AdminResource;
import model.*;
import utils.Constants;

import java.util.*;

public class AdminMenu {
    private static final AdminResource adminResource = AdminResource.getInstance();

    public static void execute() {
        Scanner inp = new Scanner(System.in);
        String chooseOption;
        boolean isNotExit;
        try {
            do {
                System.out.println("Admin Menu");
                System.out.println("_______________________________________________");
                System.out.println("1. See all Customers");
                System.out.println("2. See allRooms");
                System.out.println("3. See all Reservations");
                System.out.println("4. Add a Room");
                System.out.println("5. Back to Main Menu");
                System.out.println("6. Dummy data for testing");
                System.out.println("_______________________________________________");
                System.out.println("Please select a number for the admin menu option");

                chooseOption = inp.nextLine().trim();
                switch (chooseOption) {
                    case "1":
                        printAllCustomers();
                        break;
                    case "2":
                        printAllRooms();
                        break;
                    case "3":
                        printAllReservations();
                        break;
                    case "4":
                        addRoom(inp);
                    case "6":
                        dummyDataForTest();
                        break;
                    default:
                        break;
                }
                isNotExit = !"5".equals(chooseOption);
            } while (isNotExit);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            System.out.println("Something went wrong, please restart application");
            execute();
        }
    }

    private static void printAllCustomers() {
        Collection<Customer> customers = adminResource.getAllCustomers();
        if (!customers.isEmpty()) {
            System.out.println("All customers:");
            for (Customer customer : customers) {
                System.out.println(customer);
            }
        } else {
            System.out.println(Constants.MSG_CUSTOMER_NOT_FOUND);
        }
    }

    private static void printAllRooms() {
        Collection<IRoom> rooms = adminResource.getAllRooms();
        if (!rooms.isEmpty()) {
            System.out.println("All rooms:");
            for (IRoom room : rooms) {
                System.out.println(room);
            }
        } else {
            System.out.println(Constants.MSG_ROOM_NOT_FOUND);
        }
    }

    private static void printAllReservations() {
        adminResource.displayAllReservations();
    }

    private static void addRoom(Scanner inp) {
        System.out.println("Enter room number:");
        String roomNumber = inp.nextLine();
        Double roomPrice = enterRoomPrice(inp);
        RoomType roomType = enterRoomType(inp);

        IRoom room = new Room(roomNumber, roomPrice, roomType);
        if (adminResource.getAllRooms().contains(room)) {
            System.out.println(Constants.MSG_ROOM_EXISTS);
        } else {
            adminResource.addRoom(Collections.singletonList(room));
            System.out.println("Room has been added successfully.");
        }

        System.out.println("Would you like to add another room? y/n");
        String option = inp.nextLine().trim();
        while (!Constants.YES.equalsIgnoreCase(option) && !Constants.NO.equalsIgnoreCase(option)) {
            System.out.println("Please enter y (yes) or n (no):");
            option = inp.nextLine().trim();
        }
        if (Constants.YES.equalsIgnoreCase(option)) {
            addRoom(inp);
        }
    }

    private static Double enterRoomPrice(Scanner inp) {
        System.out.println("Enter room price per night:");
        try {
            double price = Double.parseDouble(inp.nextLine());
            if (price < 0) {
                System.out.println("Invalid room price. (price >= 0)");
                price = enterRoomPrice(inp);
            }
            return price;
        } catch (NumberFormatException e) {
            System.out.println("Invalid room price.");
            return enterRoomPrice(inp);
        }
    }

    private static RoomType enterRoomType(Scanner inp) {
        System.out.println("Enter room type: 1 for single bed, 2 for double bed");
        try {
            int roomTypeInp = Integer.parseInt(inp.nextLine());
            return RoomType.findByValueInt(roomTypeInp);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid room type.");
            return enterRoomType(inp);
        }
    }

    private static void dummyDataForTest() {
        //dummyCustomersData
        Customer customer1 = new Customer("Tuoi", "Tran", "t@gmail.com");
        Customer customer2 = new Customer("Hoa", "Nguyen", "hoa@gmail.com");
        Customer customer3 = new Customer("Lan", "Le", "lan@gmail.com");
        Customer customer4 = new Customer("Tung", "Tran", "tung@gmail.com");
        Customer customer5 = new Customer("Lien", "Tran", "lien@gmail.com");

        List<Customer> customers = new ArrayList<>();
        customers.add(customer1);
        customers.add(customer2);
        customers.add(customer3);
        customers.add(customer4);
        customers.add(customer5);
        adminResource.addCustomers(customers);

        //dummyRoomsData
        IRoom room1 = new Room("P.101", 12.3, RoomType.SINGLE);
        IRoom room2 = new Room("P.201", 20.0, RoomType.DOUBLE);
        IRoom room3 = new Room("P.302", 10.5, RoomType.SINGLE);
        IRoom room4 = new FreeRoom("P.601", RoomType.SINGLE);
        IRoom room5 = new FreeRoom("P.602", RoomType.SINGLE);
        IRoom room6 = new FreeRoom("P.603", RoomType.DOUBLE);

        List<IRoom> rooms = new ArrayList<>();
        rooms.add(room1);
        rooms.add(room2);
        rooms.add(room3);
        rooms.add(room4);
        rooms.add(room5);
        rooms.add(room6);
        adminResource.addRoom(rooms);

        //dummyReservationDate
        Reservation reservation1 = new Reservation(customer1, room1, new Date("02/26/2023"), new Date("03/04/2023"));
        Reservation reservation2 = new Reservation(customer4, room2, new Date("03/01/2023"), new Date("03/08/2023"));
        Reservation reservation3 = new Reservation(customer5, room3, new Date("03/01/2023"), new Date("03/07/2023"));
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(reservation2);
        reservations.add(reservation1);
        reservations.add(reservation3);
        adminResource.addReservations(reservations);
    }
}
