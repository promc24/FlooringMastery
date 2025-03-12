package org.example.controller;

import org.example.service.FlooringDataValidationException;
import org.example.service.FlooringDuplicateIdException;
import org.example.dao.FlooringPersistenceException;
import org.example.model.Order;
import org.example.service.FlooringServiceLayer;
import org.example.view.FlooringView;

import java.time.LocalDate;
import java.util.List;

public class FlooringController {

    private FlooringView view;
    private FlooringServiceLayer service;

    public FlooringController(FlooringServiceLayer service, FlooringView view){
        this.service = service;
        this.view = view;
    }

    public void run() {
        boolean keepGoing = true;
        int menuSelection = 0;
        try{
            while (keepGoing) {

                menuSelection = getMenuSelection();

                switch (menuSelection) {
                    case 1:
                        listOfOrders();
                        break;
                    case 2:
                        createNewOrder();
                        break;
                    case 3:
                        editOrder();
                        break;
                    case 4:
                        removeOrder();
                        break;
                    case 5:
                        exportAllOrders();
                        break;
                    case 6:
                        keepGoing = false;
                        break;
                    default:
                        unknownCommand();
                }

            }
            exitMessage();
        } catch (FlooringPersistenceException e){
            view.displayErrorMessage(e.getMessage());
        } catch (FlooringDuplicateIdException | FlooringDataValidationException e) {
            throw new RuntimeException(e);
        }

    }

    //gets menu selection from view
    private int getMenuSelection(){
        return view.printMenuAndGetSelection();
    }

    //create new order
    private void createNewOrder() throws FlooringPersistenceException, FlooringDuplicateIdException, FlooringDataValidationException {
        view.displayCreateOrderBanner();
        Order newOrder  = view.getNewOrderInfo();

        if (newOrder  != null){
            service.addOrder(newOrder.getOrderDate(),newOrder);
            view.displayCreateSuccessBanner();
        } else {
            view.displayOrderCancelledBanner();
        }
    }

    //gets list of orders
    private void listOfOrders() throws FlooringPersistenceException {
        LocalDate orderDate = view.askForDate();
        List<Order> orderList = service.getAllOrders(orderDate);
        view.displayOrderList(orderList, orderDate);
    }

    //removes order
    private void removeOrder() throws FlooringPersistenceException {
        view.displayRemoveOrderBanner();
        LocalDate orderDate = view.askForDate();
        int orderNumber = view.askForOrderNumber();
        Order removedOrder = service.removeOrder(orderDate, orderNumber);
        if(removedOrder != null){
            view.displayRemoveSuccessBanner();
        }

    }

    //exports all data
    private void exportAllOrders() throws FlooringPersistenceException {
        service.extractAllOrders();
        view.displayExtractionSuccessBanner();
    }

    //edit order
    private void editOrder() throws FlooringPersistenceException {
        view.displayEditOrderBanner();
        LocalDate orderDate = view.askForDate();
        int orderNumber = view.askForOrderNumber();
        Order orderToEdit = service.editOrder(orderDate, orderNumber);
        if (orderToEdit != null) {
            Order editedOrder = view.displayOrderToEdit(orderToEdit, orderDate);
            if (editedOrder != null) {
                service.updateOrder(editedOrder, orderDate);
                view.displayEditOrderSuccessBanner();
            }
        }
    }

    //unknown command msg using view
    private void unknownCommand() {
        view.displayUnknownCommandBanner();
    }

    //exit msg using view
    private void exitMessage() {
        view.displayExitBanner();
    }
}
