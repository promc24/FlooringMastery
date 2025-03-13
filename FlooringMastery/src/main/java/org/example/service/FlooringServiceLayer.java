package org.example.service;

import org.example.dao.FlooringPersistenceException;
import org.example.model.Order;

import java.time.LocalDate;
import java.util.List;

public interface FlooringServiceLayer {

    void addOrder(LocalDate orderDate, Order order) throws
            FlooringDataValidationException,
            FlooringPersistenceException;

    List<Order> getAllOrders(LocalDate orderDate) throws FlooringPersistenceException;

    void updateOrder(Order order, LocalDate orderDate) throws FlooringPersistenceException;

    Order editOrder(LocalDate orderDate, int orderNumber) throws FlooringPersistenceException;

    Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringPersistenceException;

    void extractAllOrders() throws FlooringPersistenceException;



}
