package org.example.dao;

import org.example.model.Order;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FlooringDaoImpl implements FlooringDao{

    public String TAX_FILE = "C:\\Users\\promo\\IdeaProjects\\java-practice-promc24\\flooring-mastery-project-promc24\\FlooringMastery\\src\\main\\java\\org\\example\\textfiles\\Data\\Taxes.txt";
    public String PRODUCT_FILE = "C:\\Users\\promo\\IdeaProjects\\java-practice-promc24\\flooring-mastery-project-promc24\\FlooringMastery\\src\\main\\java\\org\\example\\textfiles\\Data\\Products.txt";
    public String ORDERS_DIRECTORY = "C:\\Users\\promo\\IdeaProjects\\java-practice-promc24\\flooring-mastery-project-promc24\\FlooringMastery\\src\\main\\java\\org\\example\\textfiles\\Orders\\";

    private final Map<LocalDate, List<Order>> orders = new HashMap<>();

    @Override
    public Order addOrder(LocalDate orderDate, Order order) throws FlooringPersistenceException {
        orders.putIfAbsent(orderDate, new ArrayList<>());
        orders.get(orderDate).add(order);
        writeOrder(orderDate, order);
        clearOrders();
        return order;
    }

    @Override
    public List<Order> getAllOrders(LocalDate orderDate) throws FlooringPersistenceException {
        String fileName = ORDERS_DIRECTORY + "Orders_" + orderDate.format(DateTimeFormatter.ofPattern("MMddyyyy")) + ".txt";
        List<Order> ordersList = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            //skips first line (header)
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                Order order = getOrder(line);
                ordersList.add(order);
            }
            reader.close();
        } catch (IOException e) {
            return ordersList;
        }
        return ordersList;


    }

    @Override
    public void updateOrder(Order order, LocalDate orderDate) throws FlooringPersistenceException {

        //loads the orders into hashmap
        loadOrders(orderDate);
        //gets the list of orders for the date
        List<Order> ordersList = orders.get(orderDate);
        boolean orderFound = false;
        //checks is order is in the list and
        for (int i = 0; i < ordersList.size(); i++) {
            if (ordersList.get(i).getOrderNumber() == order.getOrderNumber()) {
                ordersList.set(i, order);
                orderFound = true;
                break;
            }
        }
        //if order is found sorts them and rewrites to file
        if (orderFound) {
            orders.put(orderDate, ordersList);
            ordersList.sort(Comparator.comparingInt(Order::getOrderNumber));
            String fileName = ORDERS_DIRECTORY + "Orders_" + orderDate.format(DateTimeFormatter.ofPattern("MMddyyyy")) + ".txt";
            rewriteOrdersToFile(fileName, ordersList, false);
        }
    }

    //finds order to be edited using order date and number
    @Override
    public Order editOrder(LocalDate orderDate, int orderNumber) throws FlooringPersistenceException {
        List<Order> ordersList = getAllOrders(orderDate);
        Order orderToEdit = null;

        for (Order order : ordersList) {
            if (order.getOrderNumber() == orderNumber) {
                orderToEdit = order;
                break;
            }
        }
        return orderToEdit;
    }

    //removes order from file and deletes if no orders in file
    @Override
    public Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringPersistenceException {
        String fileName = ORDERS_DIRECTORY + "Orders_" + orderDate.format(DateTimeFormatter.ofPattern("MMddyyyy")) + ".txt";
        List<Order> ordersList = new ArrayList<>();
        Order orderToRemove = null;
        try {
            //reads orders from file and adds them to list
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            //skips first line (header)
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                Order order = getOrder(line);
                order.setOrderDate(orderDate);
                ordersList.add(order);
            }
            reader.close();
        } catch (FlooringPersistenceException | IOException e) {
            throw new FlooringPersistenceException("Error reading file: " + e);
        }

        //loops through list and removes order if found
        for(Order order : ordersList){
            if (order.getOrderDate() != null && order.getOrderDate().equals(orderDate) && order.getOrderNumber() == orderNumber) {
                orderToRemove = order;
                ordersList.remove(order);
                break;
            }
        }
        if (orderToRemove != null) {
            //rewrites the updated list to the file
            rewriteOrdersToFile(fileName, ordersList, false);
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String header = reader.readLine();
                String line = reader.readLine();
                reader.close();
                //checks if header exists and if first line is null if so deletes file
                if(!header.isEmpty() && line == null){
                    File file = new File(fileName);
                    if (file.delete()) {
                        throw new FlooringPersistenceException("File " + fileName + " deleted as file is empty.");
                    } else {
                        throw new FlooringPersistenceException("Failed to delete " + fileName);
                    }

                }
            } catch (FlooringPersistenceException | IOException e) {
                throw new FlooringPersistenceException("Error reading file: " + e);
            }
        }
        return orderToRemove;

    }

    //extract all date from all files in ascending order number
    @Override
    public List<Order> extractAllOrders() {
        List<Order> allOrders = new ArrayList<>();
        File ordersDirectory = new File(ORDERS_DIRECTORY);

        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("Orders_") && name.endsWith(".txt");
            }
        };

        File[] orderFiles = ordersDirectory.listFiles(filenameFilter);

        if (orderFiles != null) {
            for (File file : orderFiles) {
                String fileName = file.getName();
                String extractDateString = fileName.substring(7, 15);
                LocalDate orderDate = LocalDate.parse(extractDateString, DateTimeFormatter.ofPattern("MMddyyyy"));

                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    //skips first line (header)
                    reader.readLine();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Order order = getOrder(line);
                        order.setOrderDate(orderDate);
                        allOrders.add(order);
                    }
                } catch (IOException e) {
                    throw new FlooringPersistenceException("Error reading file: " + file.getName() + " - " + e.getMessage());
                }
            }
        }

        allOrders.sort(Comparator.comparingInt(Order::getOrderNumber));

        String exportedFiles = "C:\\Users\\promo\\IdeaProjects\\java-practice-promc24\\flooring-mastery-project-promc24\\FlooringMastery\\src\\main\\java\\org\\example\\textfiles\\Backup\\DataExport.txt";
        rewriteOrdersToFile(exportedFiles, allOrders, true);
        return allOrders;
    }

    //rewrites file if it extracts all input it adds date as well
    private void rewriteOrdersToFile(String fileName, List<Order> ordersList, boolean isExportAll) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
            if (isExportAll) {
                //writes the header
                writer.println("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total,OrderDate");
                //writes each order to the file
                for (Order order : ordersList) {
                    writer.println(orderToText(order, true));
                    writer.flush();
                }
            } else {
                //writes the header
                writer.println("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total");
                //writes each order to the file
                for (Order order : ordersList) {
                    writer.println(orderToText(order, false));
                    writer.flush();
                }
            }
            writer.close();

        } catch (FlooringPersistenceException | IOException e) {
            throw new FlooringPersistenceException("Error writing to file: " + e.getMessage());
        }
    }

    //creates order from file
    private static Order getOrder(String line) {
        String[] parts = line.split(",");
        return new Order(
                Integer.parseInt(parts[0]),
                parts[1],
                parts[2],
                new BigDecimal(parts[3]),
                parts[4],
                new BigDecimal(parts[5]),
                new BigDecimal(parts[6]),
                new BigDecimal(parts[7]),
                new BigDecimal(parts[8]),
                new BigDecimal(parts[9]),
                new BigDecimal(parts[10]),
                new BigDecimal(parts[11])
        );
    }

    public void clearOrders() {
        orders.clear();
    }

    //loads orders in file into hashmap
    public void loadOrders(LocalDate orderDate) throws FlooringPersistenceException {
        String fileName = ORDERS_DIRECTORY + "Orders_" + orderDate.format(DateTimeFormatter.ofPattern("MMddyyyy")) + ".txt";
        File orderFile = new File(fileName);

        if (!orderFile.exists()) {
            orders.put(orderDate, new ArrayList<>());
            return;
        }

        List<Order> ordersList = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(new BufferedReader(new FileReader(orderFile)));
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Order order = getOrder(line);
                ordersList.add(order);
            }

            orders.put(orderDate, ordersList);
            scanner.close();

        } catch (FlooringPersistenceException | FileNotFoundException e) {
            throw new FlooringPersistenceException("Could not load order file: " + fileName, e);
        }
    }

    //writes order into file
    public void writeOrder(LocalDate orderDate,Order orders){
        String orderFileName = ORDERS_DIRECTORY + "Orders_" + orderDate.format(DateTimeFormatter.ofPattern("MMddyyyy")) + ".txt";

        File orderFile = new File(orderFileName);
        //boolean to check if file doesn't exist
        boolean isFileNew = !orderFile.exists();

        PrintWriter out;
        try {
            out = new PrintWriter(new FileWriter(orderFileName, true));
            //if file doesn't exist add header
            if (isFileNew){
                out.println("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total");
            }
            //writes order to file immediately
            out.println(orderToText(orders, false));
            out.flush();
            out.close();
        } catch (FlooringPersistenceException | IOException e) {
            throw new FlooringPersistenceException("Error writing to file: " + e.getMessage());
        }
    }


    //converts order details into a text
    String orderToText(Order order, boolean isExport){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        if (isExport){
            return order.getOrderNumber() + "," +
                    order.getCustomerName() + "," +
                    order.getState() + "," +
                    order.getTaxRate() + "," +
                    order.getProductType() + "," +
                    order.getArea() + "," +
                    order.getCostPerSquareFoot() + "," +
                    order.getLaborCostPerSquareFoot() + "," +
                    order.getMaterialCost() + "," +
                    order.getLaborCost() + "," +
                    order.getTax() + "," +
                    order.getTotal() + "," +
                    order.getOrderDate().format(formatter);
        } else {
            return order.getOrderNumber() + "," +
                    order.getCustomerName() + "," +
                    order.getState() + "," +
                    order.getTaxRate() + "," +
                    order.getProductType() + "," +
                    order.getArea() + "," +
                    order.getCostPerSquareFoot() + "," +
                    order.getLaborCostPerSquareFoot() + "," +
                    order.getMaterialCost() + "," +
                    order.getLaborCost() + "," +
                    order.getTax() + "," +
                    order.getTotal();

        }
    }
}
