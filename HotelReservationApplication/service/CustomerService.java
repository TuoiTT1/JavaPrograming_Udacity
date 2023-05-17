package service;

import exception.DataExistsException;
import model.Customer;
import utils.Constants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CustomerService {
    private static CustomerService reference;
    private final Map<String, Customer> customers;

    private CustomerService() {
        this.customers = new HashMap<>();
    }

    public static CustomerService getInstance() {
        if (reference == null) {
            reference = new CustomerService();
        }
        return reference;
    }

    public void addCustomer(String email, String firstName, String lastName) throws DataExistsException, IllegalArgumentException {
        if (!customers.containsKey(email)) {
            customers.put(email, new Customer(firstName, lastName, email));
        } else {
            throw new DataExistsException(Constants.MSG_CUSTOMER_EXISTS + " Email: " + email);
        }
    }

    public Customer getCustomer(String customerEmail) {
        return customers.get(customerEmail);
    }

    public Collection<Customer> getAllCustomer() {
        return customers.values();
    }
}
