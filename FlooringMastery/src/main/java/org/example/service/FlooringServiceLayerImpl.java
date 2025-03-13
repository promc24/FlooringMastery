package org.example.service;

import org.example.dao.FlooringDao;
import org.example.dao.FlooringPersistenceException;
import org.example.model.Order;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FlooringServiceLayerImpl implements FlooringServiceLayer{

    FlooringDao dao;

    public String TAX_FILE = "C:\\Users\\promo\\IdeaProjects\\java-practice-promc24\\flooring-mastery-project-promc24\\FlooringMastery\\src\\main\\java\\org\\example\\textfiles\\Data\\Taxes.txt";
    public String PRODUCT_FILE = "C:\\Users\\promo\\IdeaProjects\\java-practice-promc24\\flooring-mastery-project-promc24\\FlooringMastery\\src\\main\\java\\org\\example\\textfiles\\Data\\Products.txt";

    ArrayList<BigDecimal> calculatedInfoList = new ArrayList<BigDecimal>();
    ArrayList<String> statesInfile = new ArrayList<String>();

    public FlooringServiceLayerImpl(FlooringDao dao){
        this.dao = dao;
    }

    @Override
    public void addOrder(LocalDate orderDate, Order order) throws FlooringDataValidationException, FlooringPersistenceException {
        dao.addOrder(orderDate, order);

    }

    @Override
    public List<Order> getAllOrders(LocalDate orderDate) throws FlooringPersistenceException {
        return dao.getAllOrders(orderDate);
    }

    @Override
    public void updateOrder(Order order, LocalDate orderDate) throws FlooringPersistenceException {
        dao.updateOrder(order, orderDate);
    }

    @Override
    public Order editOrder(LocalDate orderDate, int orderNumber) throws FlooringPersistenceException {
        return dao.editOrder(orderDate, orderNumber);
    }

    @Override
    public Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringPersistenceException {
        return dao.removeOrder(orderDate, orderNumber);
    }

    @Override
    public void extractAllOrders() throws FlooringPersistenceException {
        dao.extractAllOrders();
    }

    //loads and reads information from tax file
    public BigDecimal loadTaxInfo(String state) {
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
        } catch (FlooringPersistenceException | IOException e) {
            throw new FlooringPersistenceException("Error reading tax file: " + e);
        }
        return BigDecimal.ZERO;
    }

    //loads and reads information from product file
    public BigDecimal[] loadProductInfo(String productType) {
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
        } catch (FlooringPersistenceException | IOException e) {
            throw new FlooringPersistenceException("Error reading product file: " + e);
        }
        return new BigDecimal[]{BigDecimal.ZERO};
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
        } catch (FlooringPersistenceException | IOException e) {
            throw new FlooringPersistenceException("Error reading tax file: " + e);
        }

        return statesInfile;
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



}
