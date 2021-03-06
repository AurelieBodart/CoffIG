package business;

import dataAccess.CoffeeDBAccess;
import dataAccess.CoffeeDataAccess;
import model.Coffee;
import model.exceptions.*;

import java.util.ArrayList;

public class CoffeeManager {
    private CoffeeDataAccess dataAccessor;

    public CoffeeManager() {
        dataAccessor = new CoffeeDBAccess();
    }

    public ArrayList<Coffee> getAllCoffees() throws AllDataException, ConnectionException, DoubleInputException, IntegerInputException, DateException {
        return dataAccessor.getAllCoffees();
    }

    public void addCoffee(Coffee coffee) throws ConnectionException, AddDataException, BusinessException {
        if (coffee == null)
            throw new BusinessException("CoffeeManager", "l'ajout d'un café", "Le café");
        dataAccessor.addCoffee(coffee);
    }

    public int getLastCoffeeID() throws ConnectionException, AddDataException {
        return dataAccessor.getLastCoffeeID();
    }

    public void updateCoffee(Coffee coffee) throws ConnectionException, ModifyException, BusinessException {
        if (coffee == null)
            throw new BusinessException("CoffeeManager", "la mise à jour d'un café", "Le café");
        dataAccessor.updateCoffee(coffee);
    }

    public ArrayList<String> getFeatures() throws AllDataException, ConnectionException {
        return dataAccessor.getFeatures();
    }
}
