package org.example.view;

import java.math.BigDecimal;
import java.util.Scanner;

public class UserIoImpl implements UserIO{

    Scanner scanner = new Scanner(System.in);

    @Override
    public void print(String message) {
        System.out.println(message);
    }

    @Override
    public String readString(String prompt) {
        print(prompt);
        return scanner.nextLine().trim();
    }

    @Override
    public int readInt(String prompt) {
        print(prompt);

        String userInput = scanner.nextLine().trim();

        try {
            return Integer.parseInt(userInput);
        } catch (NumberFormatException e) {
            print("Invalid input! Try again.");
            return readInt(prompt);
        }
    }

    @Override
    public int readInt(String prompt, int min, int max) {
        print(prompt);
        try{
            String userInput = scanner.nextLine().trim();
            int input = Integer.parseInt(userInput);
            if (input <= max && input >= min){
                return input;

            } else {
                print("Invalid input! Try again.");
                return readInt(prompt);
            }
        } catch (NumberFormatException e){
            print("Invalid input! Try again.");
            return readInt(prompt);
        }



    }

    @Override
    public BigDecimal readBigDecimal(String prompt) {
        print(prompt);

        String userInput = scanner.nextLine().trim();

        try {
            return new BigDecimal(userInput);
        } catch (NumberFormatException e) {
            print("Invalid input! Try again.");
            return readBigDecimal(prompt);
        }
    }

}
