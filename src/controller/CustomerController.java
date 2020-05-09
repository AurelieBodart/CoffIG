package controller;

import business.CustomerManager;
import model.*;
import model.exceptions.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class CustomerController {
    private CustomerManager manager;

    public CustomerController(){
        manager = new CustomerManager();
    }

    public void addCustomer(Customer customer) throws AddException, ConnectionException {
        manager.addCustomer(customer);
    }
    public ArrayList<Locality> getAllLocalities() throws AllDataException, ConnectionException {
        return manager.getAllLocalities();
    }
    public ArrayList<Customer> getAllCustomers() throws AllDataException, ConnectionException, CharacterInputException, DateException, StringInputException, IntegerInputException {
        return manager.getAllCustomers();
    }
    public int getLastCustomerId() throws AllDataException, ConnectionException {
        return manager.getLastCustomerId();
    }
    public void modifyCustomer(Customer customer) throws ModifyException, ConnectionException {
        manager.modifyCustomer(customer);
    }
}