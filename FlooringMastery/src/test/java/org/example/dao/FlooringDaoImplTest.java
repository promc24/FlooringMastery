package org.example.dao;

import org.example.model.Order;
import org.example.service.FlooringServiceLayerImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class FlooringDaoImplTest {

    private FlooringDaoImpl dao;
    private static final String TEST_ORDERS_DIRECTORY = "src/test/resources/";
    private static final String TEST_TAXES_FILE = "src/test/resources/TestTaxes.txt";
    private static final String TEST_PRODUCTS_FILE = "src/test/resources/TestProducts.txt";
    private static final String TEST_ORDERS_FILE_NAME = "Orders_05302025.txt";

    @BeforeEach
    void setUp() {
        dao = new FlooringDaoImpl();
        dao.ORDERS_DIRECTORY = TEST_ORDERS_DIRECTORY;
        dao.TAX_FILE = TEST_TAXES_FILE;
        dao.PRODUCT_FILE = TEST_PRODUCTS_FILE;

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

    //delete file after each test
    @AfterEach
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
    void testAddOrder() throws FlooringPersistenceException {
        LocalDate orderDate = LocalDate.of(2025, 5, 30);
        Order order = new Order(3, "Armin Arlert", "Texas", new BigDecimal("4.45"),
                "Wood", new BigDecimal("100"), new BigDecimal("5.15"), new BigDecimal("4.75"),
                new BigDecimal("515.00"), new BigDecimal("475.00"), new BigDecimal("39.60"), new BigDecimal("1029.60"));

        dao.addOrder(orderDate, order);
        List<Order> retrievedOrders = dao.getAllOrders(orderDate);
        assertEquals(3, retrievedOrders.size());
        assertEquals(order.getOrderNumber(), retrievedOrders.get(2).getOrderNumber());
    }

    //tests get order list
    @Test
    void testGetAllOrders() throws FlooringPersistenceException {
        LocalDate orderDate = LocalDate.of(2025, 5, 30);
        List<Order> orders = dao.getAllOrders(orderDate);
        assertEquals(2, orders.size());
    }
    //test order update
    @Test
    void testUpdateOrder() throws FlooringPersistenceException {
        LocalDate orderDate = LocalDate.of(2025, 5, 30);
        Order order = new Order(3, "Armin Arlert", "Texas", new BigDecimal("4.45"),
                "Wood", new BigDecimal("100"), new BigDecimal("5.15"), new BigDecimal("4.75"),
                new BigDecimal("515.00"), new BigDecimal("475.00"), new BigDecimal("39.60"), new BigDecimal("1029.60"));

        dao.addOrder(orderDate, order);

        Order updatedOrder = new Order(3, "Eren Jaegar", "CA", new BigDecimal("7.50"),
                "Tile", new BigDecimal("150"), new BigDecimal("4.25"), new BigDecimal("3.75"),
                new BigDecimal("637.5"), new BigDecimal("562.5"), new BigDecimal("90.00"), new BigDecimal("1290"));

        dao.updateOrder(updatedOrder, orderDate);
        List<Order> orders = dao.getAllOrders(orderDate);

        assertEquals(3, orders.size());
        assertEquals("Eren Jaegar", orders.get(2).getCustomerName());
    }

    //test removing order
    @Test
    void testRemoveOrder() throws FlooringPersistenceException {
        LocalDate orderDate = LocalDate.of(2025, 5, 30);
        dao.removeOrder(orderDate, 1);
        List<Order> orders = dao.getAllOrders(orderDate);
        assertEquals(1, orders.size());
    }
}

