package testing;

import model.Customer;

public class Tester {
    public static void main(String[] args) {
        Customer customer1 = new Customer("Tuoi", "Tran", "test2@gmail.com");
        System.out.println(customer1); //Customer{firstName='Tuoi', lastName='Tran', email='test1@gmail.com'}
        Customer customer2 = new Customer("Tuoi", "Tran", "test1@gmail");
        System.out.println(customer2); // Exception in thread "main" java.lang.IllegalArgumentException
    }
}
