package dataAccess;

import model.Assignment;
import model.Employee;
import model.Locality;
import model.Service;
import model.exceptions.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Hashtable;

public class EmployeeDBAccess implements EmployeeDataAccess {
    @Override
    public ArrayList<Employee> getAllEmployees() throws AllDataException, ConnectionException, DateException, CharacterInputException, StringInputException {
        ArrayList<Employee> employees = new ArrayList<>();

        try {
            Connection connection = SingletonConnection.getInstance();
            String sqlInstruction = "select * from employee"+
                    " join user on employee.employee_id = user.user_id "+
                    " order by employee.manager_id";
            PreparedStatement statement = connection.prepareStatement(sqlInstruction);
            ResultSet data = statement.executeQuery();
            Employee manager = null;

            while (data.next()) {
                Employee employee = createEmployee(data);

                Integer managerId = data.getInt("manager_id");
                if (!data.wasNull() && manager != null) employee.setManager(manager);
                else manager = employee;

                employees.add(employee);
            }
        } catch (SQLException exception) {
            throw new AllDataException(exception.getMessage(), "employés");
        } catch (IOException exception) {
            throw new ConnectionException(exception.getMessage());
        }
        return employees;
    }

    @Override
    public Employee getManager() throws AllDataException, ConnectionException, StringInputException, DateException, CharacterInputException {
        Employee manager;

        try {
            Connection connection = SingletonConnection.getInstance();
            String sqlInstruction = "select * from employee" +
                    " join user on employee.employee_id = user.user_id " +
                    " where employee.manager_id is null";

            PreparedStatement statement = connection.prepareStatement(sqlInstruction);
            ResultSet data = statement.executeQuery();

            data.next();
            manager = createEmployee(data);
        }
        catch (SQLException exception) {
            throw new AllDataException(exception.getMessage(), "manager(s)");
        }
        catch (IOException exception) {
            throw new ConnectionException(exception.getMessage());
        }
        return manager;
    }

    @Override
    public void addEmployee(Employee employee) throws ConnectionException, AddDataException {
        try {
            Connection connection = SingletonConnection.getInstance();

            String sqlUser = "insert into user (user_id, password, last_name, first_name, birth_date, street_name, email, phone, gender, locality_postal_code, locality_city) " +
                    "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement userStatement = connection.prepareStatement(sqlUser);
            userStatement.setInt(1, employee.getUserID());
            userStatement.setString(2, employee.getPassword());
            userStatement.setString(3, employee.getLastName());
            userStatement.setString(4, employee.getFirstName());
            userStatement.setDate(5, new java.sql.Date(employee.getBirthDate().getTimeInMillis()));
            userStatement.setString(6, employee.getStreetName());
            userStatement.setString(7, employee.getEmail());
            userStatement.setString(8, employee.getPhone());
            userStatement.setString(9, employee.getGender().toString());
            userStatement.setInt(10, employee.getLocality().getPostalCode());
            userStatement.setString(11, employee.getLocality().getCity());
            userStatement.executeUpdate();

            if(employee.getSecondName() != null) {
                sqlUser = "update user set second_name = ? where user_id = ?";
                userStatement = connection.prepareStatement(sqlUser);
                userStatement.setString(1, employee.getSecondName());
                userStatement.setInt(2, employee.getUserID());
                userStatement.executeUpdate();
            }

            if(employee.getMaidenName() != null) {
                sqlUser = "update user set maiden_name = ? where user_id = ?";
                userStatement = connection.prepareStatement(sqlUser);
                userStatement.setString(1, employee.getMaidenName());
                userStatement.setInt(2, employee.getUserID());
                userStatement.executeUpdate();
            }

            String sqlEmployee = "insert into employee (employee_id, hire_date, is_employee_of_the_month, discount) values (?,?,?,?)";
            PreparedStatement employeeStatement = connection.prepareStatement(sqlEmployee);
            employeeStatement.setInt(1, employee.getUserID());
            employeeStatement.setDate(2, new java.sql.Date(employee.getHireDate().getTimeInMillis()));
            employeeStatement.setBoolean(3, employee.getEmployeeOfMonth());
            employeeStatement.setDouble(4, employee.getDiscount());
            employeeStatement.executeUpdate();

            if (employee.getEndContractDate() != null) {
                sqlEmployee = "update employee set end_contract_date = ? where employee_id = ?";
                employeeStatement = connection.prepareStatement(sqlEmployee);
                employeeStatement.setDate(1, new java.sql.Date(employee.getEndContractDate().getTimeInMillis()));
                employeeStatement.setInt(2, employee.getUserID());
                employeeStatement.executeUpdate();
            }

            if (employee.getParkingSpaceNumber() != null) {
                sqlEmployee = "update employee set parking_space_number = ? where employee_id = ?";
                employeeStatement = connection.prepareStatement(sqlEmployee);
                employeeStatement.setInt(1, employee.getParkingSpaceNumber());
                employeeStatement.setInt(2, employee.getUserID());
                employeeStatement.executeUpdate();
            }

            if (employee.getManager() != null) {
                sqlEmployee = "update employee set manager_id = ? where employee_id = ?";
                employeeStatement = connection.prepareStatement(sqlEmployee);
                employeeStatement.setInt(1, employee.getManager().getUserID());
                employeeStatement.setInt(2, employee.getUserID());
                employeeStatement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new AddDataException(exception.getMessage(), "employé");
        } catch (IOException exception) {
            throw new ConnectionException(exception.getMessage());
        }
    }

    @Override
    public ArrayList<Employee> getCurrentlyWorkingEmployees() throws AllDataException, ConnectionException, StringInputException, DateException, CharacterInputException {
        ArrayList<Employee> workingEmployees = new ArrayList<>();

        try {
            Connection connection = SingletonConnection.getInstance();
            String sqlInstruction = "select e.employee_id, hire_date, end_contract_date, is_employee_of_the_month," +
                    " discount, parking_space_number, manager_id, password, last_name, first_name, second_name," +
                    " maiden_name, birth_date, street_name, email, phone, gender," +
                    " locality_postal_code, locality_city" +
                    " from employee e join user u on e.employee_id = u.user_id" +
                    " join assignment a on e.employee_id = a.employee_id" +
                    " join service s on a.service_id = s.service_id" +
                    " where date = curdate() and curtime() between start_time and end_time";

            PreparedStatement preparedStatement = connection.prepareStatement(sqlInstruction);
            ResultSet data = preparedStatement.executeQuery();

            while (data.next()) {
                workingEmployees.add(createEmployee(data));
            }
        } catch (SQLException exception) {
            throw new AllDataException(exception.getMessage(), "employés");
        } catch (IOException exception) {
            throw new ConnectionException(exception.getMessage());
        }
        return workingEmployees;
    }

    @Override
    public int getLastParkingSpaceNumber() throws ConnectionException, AllDataException {
        int nbParkingSpaceNumber;

        try {
            Connection connection = SingletonConnection.getInstance();
            String sqlInstruction = "select max(parking_space_number) from employee";
            PreparedStatement statement = connection.prepareStatement(sqlInstruction);
            ResultSet result = statement.executeQuery(sqlInstruction);

            result.next();
            nbParkingSpaceNumber = result.getInt("max(parking_space_number)");
        } catch (IOException exception) {
            throw new ConnectionException(exception.getMessage());
        } catch (SQLException exception) {
            throw new AllDataException(exception.getMessage(), "numéros d'emplacement de parking !");
        }
        return nbParkingSpaceNumber;
    }

    @Override
    public ArrayList<Assignment> searchAssignments(String identity, GregorianCalendar startDate, GregorianCalendar endDate) throws AllDataException, ConnectionException, TimeException {
        ArrayList<Assignment> assignments = new ArrayList<>();

        try {
            Connection connection = SingletonConnection.getInstance();
            Hashtable<Integer, Service> services = new Hashtable<>();
            String sqlInstruction = "select * from service" +
                    " left join assignment a on service.service_id = a.service_id" +
                    " join employee e on a.employee_id = e.employee_id" +
                    " join user u on e.employee_id = u.user_id" +
                    " where u.first_name = ? and u.last_name = ?" +
                    " and a.date between ? and ?" +
                    " order by a.date";
            PreparedStatement statement = connection.prepareStatement(sqlInstruction);
            statement.setString(1, identity.split("\\s")[0]);
            statement.setString(2, identity.split("\\s")[1]);
            statement.setDate(3, new Date(startDate.getTimeInMillis()));
            statement.setDate(4, new Date(endDate.getTimeInMillis()));
            ResultSet data = statement.executeQuery();

            while (data.next()) {
                int serviceId = data.getInt("service_id");
                GregorianCalendar date = new GregorianCalendar();

                if (!services.containsKey(serviceId))
                    services.put(serviceId, new Service(data.getTime("start_time").toLocalTime(), data.getTime("end_time").toLocalTime()));
                date.setTime(data.getDate("date"));
                assignments.add(new Assignment(services.get(serviceId), date));
            }
        } catch (SQLException exception) {
            throw new AllDataException(exception.getMessage(), "services");
        } catch (IOException exception) {
            throw new ConnectionException(exception.getMessage());
        }
        return assignments;
    }

    private Employee createEmployee(ResultSet data) throws SQLException, CharacterInputException, DateException, StringInputException {
        GregorianCalendar birthDate = new GregorianCalendar();
        java.sql.Date birthDateSql = data.getDate("birth_date");
        birthDate.setTime(birthDateSql);

        GregorianCalendar hireDate = new GregorianCalendar();
        java.sql.Date hireDateSql = data.getDate("hire_date");
        hireDate.setTime(hireDateSql);

        Employee employee = new Employee(
                data.getInt("employee_id"),
                data.getString("password"),
                data.getString("last_name"),
                data.getString("first_name"),
                birthDate,
                data.getString("street_name"),
                new Locality(data.getInt("locality_postal_code"),
                        data.getString("locality_city")),
                data.getString("email"),
                data.getString("phone"),
                data.getString("gender").charAt(0),
                hireDate,
                data.getBoolean("is_employee_of_the_month"),
                data.getDouble("discount"));

        String secondName = data.getString("second_name");
        if(!data.wasNull())
            employee.setSecondName(secondName);

        String maidenName = data.getString("maiden_name");
        if(!data.wasNull())
            employee.setMaidenName(maidenName);

        java.sql.Date endContractDateSql = data.getDate("end_contract_date");
        if (!data.wasNull()) {
            GregorianCalendar endContractDate = new GregorianCalendar();
            endContractDate.setTime(endContractDateSql);
            employee.setEndContractDate(endContractDate);
        }

        Integer parkingSpaceNumber = data.getInt("parking_space_number");
        if (!data.wasNull())
            employee.setParkingSpaceNumber(parkingSpaceNumber);

        return employee;
    }
}
