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

    ArrayList<BigDecimal> calculatedInfoList = new ArrayList<BigDecimal>();
    ArrayList<String> statesInfile = new ArrayList<String>();
    ArrayList<String> productsInFile = new ArrayList<String>();



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
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ordersList;


    }

    @Override
    public void updateOrder(Order order, LocalDate orderDate) throws FlooringPersistenceException {
        //loads the orders into hashmap
        loadOrders(orderDate);
        //gets the list of orders for the date

        List<Order> ordersList = orders.get(orderDate);
        //checks is list is empty and if so sends error message
        if(ordersList == null || ordersList.isEmpty()){
            System.out.println("No orders for this date:" + orderDate);
            return;
        }

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

        } else {
            System.out.println("No such order found");
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

        if (orderToEdit == null){
            System.out.println("No such order found");
        }

        return orderToEdit;

    }

    //removes order from file
    @Override
    public Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringPersistenceException {
        String fileName = ORDERS_DIRECTORY + "Orders_" + orderDate.format(DateTimeFormatter.ofPattern("MMddyyyy")) + ".txt";
        List<Order> ordersList = new ArrayList<>();
        Order orderToRemove = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            //skips first line (header)
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                Order order = getOrder(line);
                order.setOrderDate(orderDate);
                ordersList.add(order);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
            try{
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(fileName));
                    //skips first line (header)
                    String header = reader.readLine();
                    String line = reader.readLine();
                    if(!header.isEmpty() && line == null){
                        File file = new File(fileName);
                        if (file.delete()) {
                            System.out.println("Deleted empty file: " + fileName);
                        } else {
                            System.out.println("Failed to delete empty file: " + fileName);
                        }

                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Error reading file: " + e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }


        } else {
            System.out.println("Order not found for the given date and order number.");
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
                    System.out.println("Error reading file: " + file.getName() + " - " + e.getMessage());
                }
            }
        }

        allOrders.sort(Comparator.comparingInt(Order::getOrderNumber));

        String EXPORTED_FILES = "C:\\Users\\promo\\IdeaProjects\\java-practice-promc24\\flooring-mastery-project-promc24\\FlooringMastery\\src\\main\\java\\org\\example\\textfiles\\Backup\\DataExport.txt";
        rewriteOrdersToFile(EXPORTED_FILES, allOrders, true);
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
                writer.close();
            } else {
                //writes the header
                writer.println("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total");
                //writes each order to the file
                for (Order order : ordersList) {
                    writer.println(orderToText(order, false));
                    writer.flush();
                }

                writer.close();
            }

        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
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

        } catch (FileNotFoundException e) {
            throw new FlooringPersistenceException("Could not load order file: " + fileName, e);
        }
    }

    //loads and reads information from tax file
    private BigDecimal loadTaxInfo(String state) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(TAX_FILE));
            //skips first line (header)
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] taxInfo = line.split(",");
                //checks if state matches
                if (taxInfo[1].equalsIgnoreCase(state)) {
                    //returns tax rate
                    return new BigDecimal(taxInfo[2]);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading tax file: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return BigDecimal.ZERO;
    }

    //loads and reads information from product file
    private BigDecimal[] loadProductInfo(String productType) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(PRODUCT_FILE));
            //skips first line (header)
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] productInfo = line.split(",");
                //checks if product matches
                if (productInfo[0].equalsIgnoreCase(productType)) {

                    return new BigDecimal[]{new BigDecimal(productInfo[1]), new BigDecimal(productInfo[2])};
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading product file: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new BigDecimal[]{BigDecimal.ZERO};
    }

    //calculates values
    public ArrayList<BigDecimal> calculateOrderInfo(ArrayList<String> tempOrderInfoList){
        //gets information from temp lost and state info from file
        String state = tempOrderInfoList.get(0);
        String productType = tempOrderInfoList.get(1);
        BigDecimal area = new BigDecimal(tempOrderInfoList.get(2));
        BigDecimal[] costAndLaborCostPerSquareFoot = loadProductInfo(productType);
        BigDecimal taxRate = loadTaxInfo(state);

        //values calculations
        BigDecimal materialCost = area.multiply(costAndLaborCostPerSquareFoot[0]);
        BigDecimal laborCost = area.multiply(costAndLaborCostPerSquareFoot[1]);
        BigDecimal materialAndLaborTot = materialCost.add(laborCost);
        BigDecimal taxRateHundred = taxRate.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal tax = materialAndLaborTot.multiply(taxRateHundred).setScale(2,RoundingMode.HALF_UP);
        BigDecimal total = materialCost.add(laborCost.add(tax)).setScale(2,RoundingMode.HALF_UP);

        //adds calculated values to list
        calculatedInfoList.add(taxRate);
        calculatedInfoList.add(costAndLaborCostPerSquareFoot[0]);
        calculatedInfoList.add(costAndLaborCostPerSquareFoot[1]);
        calculatedInfoList.add(materialCost);
        calculatedInfoList.add(laborCost);
        calculatedInfoList.add(tax);
        calculatedInfoList.add(total);

        return calculatedInfoList;
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
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    //gets states information from Tax file
    public ArrayList<String> getStateInfo(){
        try {
            BufferedReader stateReader = new BufferedReader(new FileReader(TAX_FILE));
            //skips first line (header)
            stateReader.readLine();
            String line;
            while ((line = stateReader.readLine()) != null) {
                String[] productInfo = line.split(",");
                statesInfile.add(productInfo[1]);

            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading state file: " + e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return statesInfile;
    }



}
