package org.example.view;

import org.example.dao.FlooringDao;
import org.example.dao.FlooringDaoImpl;
import org.example.model.Order;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;

public class FlooringView {

    ArrayList<BigDecimal> calculatedInfo = new ArrayList<BigDecimal>();
    ArrayList<String> tempOrderInfoList = new ArrayList<String>();

    ArrayList<String> stateInfo = new ArrayList<String>();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private UserIO io;

    public FlooringView(UserIO io){
        this.io = io;
    }

    //displays application main menu
    public int printMenuAndGetSelection(){
        io.print("<<Flooring Program Menu>>");
        io.print("\n1. Display Orders");
        io.print("2. Add an Order");
        io.print("3. Edit an Order");
        io.print("4. Remove an Order");
        io.print("5. Export All Data");
        io.print("6. Exit");

        return io.readInt("\nPlease select from the above choices.",1,6);
    }

    //gets new order info from user
    public Order getNewOrderInfo() {

        FlooringDaoImpl flooringDao = new FlooringDaoImpl();

        //asks for dates and checks if it meets requirements and formats them into to the wanted order
        LocalDate orderDate;
        while (true) {
            try {
                orderDate = LocalDate.parse(io.readString("Enter order date (must be in the future 'MM-dd-yyyy'):"), formatter);
                if (orderDate.isAfter(LocalDate.now())) {
                    break;
                } else {
                    io.print("Please provide a date in the future!");
                }

            } catch (DateTimeParseException e) {
                io.print("Invalid input! Provide the date in the given format!");
            }
        }

        //asks for the other order info
        String customerName;
        while (true){
            try {
                customerName = capitalizeFirstLetter(io.readString("Enter customer Name:"));
                if (!customerName.isEmpty()){
                    break;
                } else{
                    io.print("Please provide a name!");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        String state;
        while (true){
            try{
                state = capitalizeFirstLetter(io.readString("Enter state:"));
                stateInfo = flooringDao.getStateInfo();
                if (!state.isEmpty() && stateInfo.contains(state)){
                    stateInfo.clear();
                    break;
                } else{
                    stateInfo.clear();
                    io.print("Please provide a valid state!");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        String productType = displayProductMenu(null, true);

        //ask for the area and checks if the input meets given requirements
        BigDecimal area;
        while (true) {
            try {
                area = io.readBigDecimal("Enter area (must be at least 100 sq fr):");
                if (area.compareTo(new BigDecimal(99)) == 1) {
                    break;
                } else {
                    io.print("Please provide an area that is at least of 100 sq ft.");
                }
            } catch (InputMismatchException e) {
                io.print("Invalid input! Provide a number!");
            }
        }

        //adds state, product type and are into an array
        tempOrderInfoList.add(state);
        tempOrderInfoList.add(productType);
        tempOrderInfoList.add(String.valueOf(area));


        calculatedInfo = flooringDao.calculateOrderInfo(tempOrderInfoList);
        BigDecimal taxRate = calculatedInfo.get(0);
        BigDecimal costPerSquareFoot = calculatedInfo.get(1);
        BigDecimal laborCostPerSquareFoot = calculatedInfo.get(2);
        BigDecimal materialCost = calculatedInfo.get(3);
        BigDecimal laborCost = calculatedInfo.get(4);
        BigDecimal tax = calculatedInfo.get(5);
        BigDecimal total = calculatedInfo.get(6);

        //displays inserted order info
        io.print("\n<<New Order Information>>\n");
        io.print("Order date: '" + orderDate + "'");
        io.print("Customer name: '" + customerName + "'");
        io.print("Order state: '" + state + "'");
        io.print("Order tax rate: '" + taxRate + "'");
        io.print("Order product type: '" + productType + "'");
        io.print("Order area: '" + area + "'");
        io.print("Order cost per square foot: '" + costPerSquareFoot + "'");
        io.print("Order labor cost per square foot: '" + laborCostPerSquareFoot + "'");
        io.print("Order material cost: '" + materialCost + "'");
        io.print("Order labor cost: '" + laborCost + "'");
        io.print("Order tax: '" + tax + "'");
        io.print("Order total: '" + total + "'\n");

        int saveOrder = 0;
        boolean isValidInput = false;

        while (!isValidInput) {
            try {
                saveOrder = io.readInt("Would you like to place the order (please select a number)?\n1. Yes\n2. No", 1, 2);
                isValidInput = true;
            } catch (InputMismatchException e){
                io.print("Invalid input! Try again");
            }
        }

        Order newOrder = null;
        //sets order details if user says yes
        if (saveOrder == 1) {
            newOrder = new Order(orderDate);
            newOrder.setOrderDate(orderDate);
            newOrder.setCustomerName(customerName);
            newOrder.setState(state);
            newOrder.setTaxRate(taxRate);
            newOrder.setProductType(productType);
            newOrder.setArea(area);
            newOrder.setCostPerSquareFoot(costPerSquareFoot);
            newOrder.setLaborCostPerSquareFoot(laborCostPerSquareFoot);
            newOrder.setMaterialCost(materialCost);
            newOrder.setLaborCost(laborCost);
            newOrder.setTax(tax);
            newOrder.setTotal(total);
            clearLists(calculatedInfo, tempOrderInfoList);

        } else {
            clearLists(calculatedInfo, tempOrderInfoList);
            return null;
        }

        return newOrder;
    }


    //displays list of orders depending on given order date
    public void displayOrderList(List<Order> orderList, LocalDate orderDate) {

        io.print("\n<<List of Orders for '" + orderDate.format(formatter) + "'>>\n");
        for (Order currentOrder : orderList) {

            io.print("Order Number: '" + currentOrder.getOrderNumber() + "'");
            io.print("Customer name: '" + currentOrder.getCustomerName() + "'");
            io.print("Order state: '" + currentOrder.getState() + "'");
            io.print("Order tax rate: '" + currentOrder.getTaxRate() + "'");
            io.print("Order product type: '" + currentOrder.getProductType() + "'");
            io.print("Order area: '" + currentOrder.getArea() + "'");
            io.print("Order cost per square foot: '" + currentOrder.getCostPerSquareFoot() + "'");
            io.print("Order labor cost per square foot: '" + currentOrder.getLaborCostPerSquareFoot() + "'");
            io.print("Order material cost: '" + currentOrder.getMaterialCost() + "'");
            io.print("Order labor cost: '" + currentOrder.getLaborCost() + "'");
            io.print("Order tax: '" + currentOrder.getTax() + "'");
            io.print("Order total: '" + currentOrder.getTotal() + "'\n");
            io.print("------------------------------------------------\n");
        }
    }

    //displays order to be edited
    public Order displayOrderToEdit(Order ogOrder, LocalDate ogDate) {
        FlooringDaoImpl flooringDao = new FlooringDaoImpl();
        //asks for the other order info
        String ogCustomerName = ogOrder.getCustomerName();
        String ogState = ogOrder.getState();
        String ogProductType = ogOrder.getProductType();
        BigDecimal ogArea = ogOrder.getArea();

        String newCustomerName = capitalizeFirstLetter(io.readString("Enter customer name '" + ogCustomerName + "':"));

        //asks for state and checks if input is valid
        String newState;
        while (true){
            try{
                newState = capitalizeFirstLetter(io.readString("Enter state'" + ogState + "':"));
                stateInfo = flooringDao.getStateInfo();
                if (newState.isEmpty() || stateInfo.contains(newState)){
                    stateInfo.clear();
                    break;
                } else{
                    stateInfo.clear();
                    io.print("Please provide a valid state!");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        String newProductType = displayProductMenu(ogProductType, false);

        //ask for the area and checks if the input meets given requirements
        BigDecimal newArea;
        while (true) {
            try {
                String stringArea = io.readString("Enter area (must be at least 100 sq fr) '" + ogArea + "':");
                if (stringArea.isEmpty()){
                    newArea = ogArea;
                    break;
                }
                newArea = BigDecimal.valueOf(Long.parseLong(stringArea));
                if (newArea.compareTo(new BigDecimal(99)) == 1) {
                    break;
                } else {
                    io.print("Please provide an area that is at least of 100 sq ft.");
                }
            } catch (InputMismatchException e) {
                io.print("Invalid input! Provide a number!");
            }
        }
        //sets new info as original is the input is empty
        if (newCustomerName.isEmpty()) {
            newCustomerName = ogCustomerName;
        }

        if (newState.isEmpty()) {
            newState = ogState;
        }

        if (newProductType.isEmpty()) {
            newProductType = ogProductType;
        }

        Order editedOrder = null;
        //if user changed values starts calculating and setting the new values
        if(Objects.equals(newCustomerName, ogCustomerName) && Objects.equals(newState, ogState) && Objects.equals(newProductType, ogProductType) && Objects.equals(newArea, ogArea)){
            io.print("You haven't changed any values. Returning to main menu");
        } else{
            //adds state, product type and are into an array
            tempOrderInfoList.add(newState);
            tempOrderInfoList.add(newProductType);
            tempOrderInfoList.add(String.valueOf(newArea));

            calculatedInfo = flooringDao.calculateOrderInfo(tempOrderInfoList);
            BigDecimal taxRate = calculatedInfo.get(0);
            BigDecimal costPerSquareFoot = calculatedInfo.get(1);
            BigDecimal laborCostPerSquareFoot = calculatedInfo.get(2);
            BigDecimal materialCost = calculatedInfo.get(3);
            BigDecimal laborCost = calculatedInfo.get(4);
            BigDecimal tax = calculatedInfo.get(5);
            BigDecimal total = calculatedInfo.get(6);

            //displays inserted order info
            io.print("\n<<Edited Order Information>>\n");
            io.print("Order date: '" + ogDate + "'");
            io.print("Order number: '" + ogOrder.getOrderNumber() + "'");
            io.print("Customer name: '" + newCustomerName + "'");
            io.print("Order state: '" + newState + "'");
            io.print("Order tax rate: '" + taxRate + "'");
            io.print("Order product type: '" + newProductType + "'");
            io.print("Order area: '" + newArea + "'");
            io.print("Order cost per square foot: '" + costPerSquareFoot + "'");
            io.print("Order labor cost per square foot: '" + laborCostPerSquareFoot + "'");
            io.print("Order material cost: '" + materialCost + "'");
            io.print("Order labor cost: '" + laborCost + "'");
            io.print("Order tax: '" + tax + "'");
            io.print("Order total: '" + total + "'\n");

            int saveOrder = 0;
            boolean isValidInput = false;

            while (!isValidInput) {
                try {
                    saveOrder = io.readInt("Would you like to place the order (please select a number)?\n1. Yes\n2. No", 1, 2);
                    isValidInput = true;
                } catch (InputMismatchException e) {
                    io.print("Invalid input! Try again!");
                }
            }

            //sets order details if user says yes
            if (saveOrder == 1) {
                editedOrder = new Order(ogOrder.getOrderDate());
                editedOrder.setOrderDate(ogOrder.getOrderDate());
                editedOrder.setOrderNumber(ogOrder.getOrderNumber());
                editedOrder.setCustomerName(newCustomerName);
                editedOrder.setState(newState);
                editedOrder.setTaxRate(taxRate);
                editedOrder.setProductType(newProductType);
                editedOrder.setArea(newArea);
                editedOrder.setCostPerSquareFoot(costPerSquareFoot);
                editedOrder.setLaborCostPerSquareFoot(laborCostPerSquareFoot);
                editedOrder.setMaterialCost(materialCost);
                editedOrder.setLaborCost(laborCost);
                editedOrder.setTax(tax);
                editedOrder.setTotal(total);
                clearLists(calculatedInfo, tempOrderInfoList);

            } else {
                clearLists(calculatedInfo, tempOrderInfoList);
                displayEditOrderCancelledBanner();
                return null;
            }

        }
        return editedOrder;


    }
    //clears lists
    public void clearLists(ArrayList<BigDecimal> listBigDecimal, ArrayList<String> listString){
        listBigDecimal.clear();
        listString.clear();
    }

    //asks user for order date
    public LocalDate askForDate(){
        LocalDate orderDate;
        while (true) {
            try {
                orderDate = LocalDate.parse(io.readString("Enter order date 'MM-dd-yyyy':"), formatter);
                    break;
            } catch (DateTimeParseException e) {
                io.print("Invalid input! Provide the date in the given format!");
            }
        }
        return orderDate;
    }

    //asks user for the order number
    public int askForOrderNumber(){
        int orderNumber;
        while (true) {
            try {
                orderNumber = io.readInt("Enter order number:");
                break;
            } catch (NumberFormatException e) {
                io.print("Invalid input! Try again!");
            }
        }
        return orderNumber;
    }

    //capitalises the first letter of the input
    public String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.split(" ");
        StringBuilder capitalizedString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedString.append(word.substring(0, 1).toUpperCase())  // First letter to upper case
                        .append(word.substring(1).toLowerCase())  // Rest of the word to lower case
                        .append(" ");  // Add space after each word
            }
        }
        return capitalizedString.toString().trim();
    }

    //displays product menu
    public String displayProductMenu(String ogProductType, boolean choiceType){
        io.print("<<Product Type>>");
        io.print("1. Carpet");
        io.print("2. Laminate");
        io.print("3. Tile");
        io.print("4. Wood");
        String productType = ogProductType;

        while (true) {
            if (choiceType) {
                try{
                    //takes input as int
                    int product = io.readInt("Enter product type from the list above (1-4):", 1,4);
                    productType = getProductTypeName(product);
                    break;

                } catch (InputMismatchException e){
                    io.print("Invalid Input! Please chose a number!");
                }

            } else {
                //takes input as string
                String stringProduct = io.readString("Enter product type from the list above (1-4):");
                //keeps original product if empty
                if (stringProduct.isEmpty()) {
                    break;
                }

                try {
                    //converts string into int and gets product name
                    int product = Integer.parseInt(stringProduct);
                    if (product >= 1 && product <= 4) {
                        productType = getProductTypeName(product);
                        break;
                    } else {
                        io.print("Please choose a number in the given range!");
                    }
                } catch (NumberFormatException e) {
                    io.print("Invalid Input! Please choose a number!");
                }
            }
        }
        return productType;
    }

    //gets product name depending on inputted integer
    private String getProductTypeName(int product) {
        switch (product) {
            case 1:
                return "Carpet";
            case 2:
                return "Laminate";
            case 3:
                return "Tile";
            case 4:
                return "Wood";
            default:
                return "";
        }
    }

    //banners
    public void displayCreateOrderBanner(){
        io.print("\n<<Create New Order>>");
    }

    public void displayCreateSuccessBanner(){
        io.print("Order successfully created!\n");
    }

    public void displayRemoveOrderBanner(){
        io.print("\n<<Remove Order>>");
    }

    public void displayRemoveSuccessBanner(){
        io.print("Order successfully removed!\n");
    }

    public void displayEditOrderBanner(){
        io.print("\n<<Edit Order>>");
    }

    public void displayEditOrderSuccessBanner(){
        io.print("Order successfully updated.\n");
    }

    public void displayOrderCancelledBanner(){
        io.print("Order cancelled.\n");
    }

    public void displayEditOrderCancelledBanner(){
        io.print("Order edit cancelled.\n");
    }

    public void displayExtractionSuccessBanner(){
        io.print("Orders extracted file successfully created!\n");
    }

    public void displayExitBanner() {
        io.print("Good Bye!");
    }

    public void displayUnknownCommandBanner() {
        io.print("Unknown Command!");
    }

    public void displayErrorMessage (String errorMessage){
        io.print("<<Remove Order>>");
        io.print(errorMessage);
    }
}
