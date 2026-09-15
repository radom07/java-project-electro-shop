package pl.adrian.electroshop.cli;

//Prosty wrapper na Scanner
//żeby uniknąć tworzenia wielu instancji Scanner

import java.util.Scanner;

public class ConsoleReader {

    private final Scanner scanner = new Scanner(System.in);

    public String readLine() {
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = readLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("To nie jest liczba, spróbuj ponownie.");
            }
        }
    }
}