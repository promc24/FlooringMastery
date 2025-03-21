package org.example.service;

import org.example.dao.FlooringDaoImpl;
import org.example.dao.FlooringPersistenceException;
import org.example.model.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlooringServiceLayerTest {
    private FlooringServiceLayerImpl service;
    private static final String TEST_ORDERS_DIRECTORY = "src/test/resources/";
    private static final String TEST_TAXES_FILE = "src/test/resources/TestTaxes.txt";
    private static final String TEST_PRODUCTS_FILE = "src/test/resources/TestProducts.txt";
    private static final String TEST_ORDERS_FILE_NAME = "Orders_05302025.txt";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        setUp(true);
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        tearDown();
    }

    //set up method
    void setUp(boolean setUpNeeded) {
        FlooringDaoImpl dao = new FlooringDaoImpl();
        service = new FlooringServiceLayerImpl(dao);
        dao.ORDERS_DIRECTORY = TEST_ORDERS_DIRECTORY;
        service.TAX_FILE = TEST_TAXES_FILE;
        service.PRODUCT_FILE = TEST_PRODUCTS_FILE;

        if(setUpNeeded){
            File directory = new File(TEST_ORDERS_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs(); // Create directories if they don't exist
            }
            // Create a test order file
            String testFileName = TEST_ORDERS_DIRECTORY + "/" + TEST_ORDERS_FILE_NAME; // Adjust date format
            File file = new File(testFileName);

            if (!file.exists()){
                // Write sample order data
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {

                    writer.write("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total");
                    writer.newLine();
                    writer.write("1,Mikasa Ackerman,Texas,6.25,Wood,100,5.15,4.75,515.00,475.00,61.88,1051.88");
                    writer.newLine();
                    writer.write("2,Levi Ackerman,Texas,6.25,Wood,100,5.15,4.75,515.00,475.00,61.88,1051.88");
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    //delete file after each test
    void tearDown() {
        String testFileName = TEST_ORDERS_DIRECTORY + "/" + TEST_ORDERS_FILE_NAME;
        File file = new File(testFileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.println("Failed to delete test file: " + testFileName);
            }
        }
    }



    //tests add order
    @Test
    void testAddOrder() throws FlooringPersistenceException, FlooringDataValidationException {
        setUp(true);
        LocalDate orderDate = LocalDate.of(2025, 5, 30);
        Order order = new Order(3, "Armin Arlert", "Texas", new BigDecimal("4.45"),
                "Wood", new BigDecimal("100"), new BigDecimal("5.15"), new BigDecimal("4.75"),
                new BigDecimal("515.00"), new BigDecimal("475.00"), new BigDecimal("39.60"), new BigDecimal("1029.60"));

        service.addOrder(orderDate, order);
        List<Order> retrievedOrders = service.getAllOrders(orderDate);
        assertEquals(3, retrievedOrders.size());
        assertEquals(order.getOrderNumber(), retrievedOrders.get(2).getOrderNumber());
        tearDown();
    }

    //tests get order list
    @Test
    void testGetAllOrders() throws FlooringPersistenceException {
        setUp(true);
        LocalDate orderDate = LocalDate.of(2025, 5, 30);
        List<Order> orders = service.getAllOrders(orderDate);
        assertEquals(2, orders.size());
        tearDown();
    }
    //test order update
    @Test
    void testUpdateOrder() throws FlooringPersistenceException, FlooringDataValidationException {
        setUp(true);
        LocalDate orderDate = LocalDate.of(2025, 5, 30);
        Order order = new Order(3, "Armin Arlert", "Texas", new BigDecimal("4.45"),
                "Wood", new BigDecimal("100"), new BigDecimal("5.15"), new BigDecimal("4.75"),
                new BigDecimal("515.00"), new BigDecimal("475.00"), new BigDecimal("39.60"), new BigDecimal("1029.60"));

        service.addOrder(orderDate, order);

        Order updatedOrder = new Order(3, "Eren Jaegar", "CA", new BigDecimal("7.50"),
                "Tile", new BigDecimal("150"), new BigDecimal("4.25"), new BigDecimal("3.75"),
                new BigDecimal("637.5"), new BigDecimal("562.5"), new BigDecimal("90.00"), new BigDecimal("1290"));

        service.updateOrder(updatedOrder, orderDate);
        List<Order> orders = service.getAllOrders(orderDate);

        assertEquals(3, orders.size());
        assertEquals("Eren Jaegar", orders.get(2).getCustomerName());
        tearDown();
    }

    //test removing order
    @Test
    void testRemoveOrder() throws FlooringPersistenceException {
        setUp(true);
        LocalDate orderDate = LocalDate.of(2025, 5, 30);
        service.removeOrder(orderDate, 1);
        List<Order> orders = service.getAllOrders(orderDate);
        assertEquals(1, orders.size());
        tearDown();
    }

    @Test
    void testRemoveOrderOrderNotFound() throws FlooringPersistenceException {
        setUp(true);
        LocalDate orderDate = LocalDate.of(2025, 5, 30);
        FlooringPersistenceException exception = assertThrows(
                FlooringPersistenceException.class,
                () -> service.removeOrder(orderDate, 3),
                "Order not found for the given date and order number."
        );
        assertEquals("Order not found for the given date and order number.", exception.getMessage());
        tearDown();
    }

    //tests order info calculations
    @Test
    void testCalculateOrderInfo() {
        setUp(false);
        ArrayList<String> tempOrderInfoList = new ArrayList<>();
        tempOrderInfoList.add("Texas"); // State
        tempOrderInfoList.add("Wood"); // ProductType
        tempOrderInfoList.add("100"); // Area

        ArrayList<BigDecimal> results = service.calculateOrderInfo(tempOrderInfoList);

        assertFalse(results.isEmpty());
        assertEquals(7, results.size());
        assertEquals(new BigDecimal("4.45"), results.get(0)); //tax rate
        assertEquals(new BigDecimal("5.15"), results.get(1)); //cost per square foot
        assertEquals(new BigDecimal("4.75"), results.get(2)); //labor cost per square foot
        assertEquals(new BigDecimal("515.00"), results.get(3)); //material cost
        assertEquals(new BigDecimal("475.00"), results.get(4)); //labor cost
        assertEquals(new BigDecimal("39.60"), results.get(5)); //tax
        assertEquals(new BigDecimal("1029.60"), results.get(6)); //total
    }

    @Test
    void testLoadTaxInfo() {
        setUp(false);
        String state = "Texas";
        BigDecimal result = service.loadTaxInfo(state);
        assertEquals(new BigDecimal("4.45"), result);
    }

    @Test
    void testLoadProductInfo() {
        setUp(false);
        String state = "Carpet";
        BigDecimal[] expectedArray = new BigDecimal[]{new BigDecimal("2.25"), new BigDecimal("2.10")};
        BigDecimal[] result = service.loadProductInfo(state);
        for (int i = 0; i < expectedArray.length; i++) {
            assertEquals(expectedArray[i], result[i]);
        }
    }

    @Test
    void testStateInfo() {
        setUp(false);
        List<String> expectedStates = Arrays.asList("Texas", "Washington", "Kentucky", "California");
        ArrayList<String> states = service.getStateInfo();
        assertEquals(expectedStates, states);

    }

    @Test
    void testCapitaliseFirstLetter() {
        setUp(false);
        String string = service.capitalizeFirstLetter("my name");
        assertEquals("My Name", string);
    }
}