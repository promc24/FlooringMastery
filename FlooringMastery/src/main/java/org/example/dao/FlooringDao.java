package org.example.dao;

import org.example.model.Order;
import org.example.dao.FlooringPersistenceException;

import java.time.LocalDate;
import java.util.List;

public interface FlooringDao {

    Order addOrder(LocalDate orderDate, Order order) throws FlooringPersistenceException;

    List<Order> getAllOrders(LocalDate orderDate) throws FlooringPersistenceException;

    void updateOrder(Order order, LocalDate orderDate) throws FlooringPersistenceException;

    Order editOrder(LocalDate orderDate, int orderNumber) throws FlooringPersistenceException;

    Order removeOrder(LocalDate orderDate, int orderNumber) throws FlooringPersistenceException;

    List<Order> extractAllOrders() throws FlooringPersistenceException;

};
