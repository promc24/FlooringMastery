package org.example;

import org.example.controller.FlooringController;
import org.example.dao.FlooringDao;
import org.example.dao.FlooringDaoImpl;
import org.example.service.FlooringServiceLayer;
import org.example.service.FlooringServiceLayerImpl;
import org.example.view.FlooringView;
import org.example.view.UserIO;
import org.example.view.UserIoImpl;

public class Main {
    public static void main(String[] args) {
        UserIO myIo = new UserIoImpl();
        FlooringView myView = new FlooringView(myIo);
        FlooringDao myDao = new FlooringDaoImpl();
        FlooringServiceLayer myService = new FlooringServiceLayerImpl(myDao);
        FlooringController controller = new FlooringController(myService, myView);
        controller.run();
    }
}
