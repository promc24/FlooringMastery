package org.example.service;

import org.example.dao.FlooringDao;
import org.example.model.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FlooringServiceLayerImpl implements FlooringServiceLayer{

    FlooringDao dao;

    public FlooringServiceLayerImpl(FlooringDao dao){
        this.dao = dao;
    }

    @Override
    public void addOrder(LocalDate orderDate, Order order) throws FlooringDuplicateIdException, FlooringDataValidationException, FlooringPersistenceException {
        dao.addOrder(order.getOrderDate(), order);

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
    public List<Order> extractAllOrders() throws FlooringPersistenceException {
        return dao.extractAllOrders();
    }

}
