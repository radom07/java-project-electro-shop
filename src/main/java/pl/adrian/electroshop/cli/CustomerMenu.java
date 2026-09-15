package pl.adrian.electroshop.cli;

import lombok.RequiredArgsConstructor;
import pl.adrian.electroshop.exception.ElectroShopException;
import pl.adrian.electroshop.model.customer.Customer;
import pl.adrian.electroshop.model.invoice.Invoice;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.product.CartItem;
import pl.adrian.electroshop.model.product.Computer;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.Smartphone;
import pl.adrian.electroshop.model.product.configuration.ComputerConfiguration;
import pl.adrian.electroshop.model.product.configuration.NoConfiguration;
import pl.adrian.electroshop.model.product.configuration.ProductConfiguration;
import pl.adrian.electroshop.model.product.configuration.SmartphoneConfiguration;
import pl.adrian.electroshop.service.CartService;
import pl.adrian.electroshop.service.CustomerManager;
import pl.adrian.electroshop.service.OrderProcessor;
import pl.adrian.electroshop.service.ProductManager;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CustomerMenu {

    private final ConsoleReader reader;
    private final ProductManager productManager;
    private final CartService cartService;
    private final CustomerManager customerManager;
    private final OrderProcessor orderProcessor;

    private final List<Order> myOrders = new ArrayList<>(); // tylko w pamięci sesji CLI dla demo anulowania orderu

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Menu klienta ---");
            System.out.println("1. Przeglądaj produkty");
            System.out.println("2. Dodaj produkt do koszyka");
            System.out.println("3. Zobacz koszyk");
            System.out.println("4. Złóż zamówienie");
            System.out.println("5. Anuluj zamówienie");
            System.out.println("0. Wróć");
            System.out.print("Wybierz opcję: ");

            switch (reader.readLine()) {
                case "1" -> browseProducts();
                case "2" -> addToCart();
                case "3" -> viewCart();
                case "4" -> placeOrder();
                case "5" -> cancelOrder();
                case "0" -> back = true;
                default -> System.out.println("Nieznana opcja.");
            }
        }
    }

    private void browseProducts() {
        List<Product> products = productManager.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("Brak produktów w sklepie.");
            return;
        }
        System.out.println();
        System.out.println("--- Dostępne produkty ---");
        products.forEach(System.out::println);
    }

    private void addToCart() {
        List<Product> products = productManager.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("Brak produktów w sklepie.");
            return;
        }

        System.out.println();
        for (int i = 0; i < products.size(); i++) {
            System.out.println((i + 1) + ". " + products.get(i));
        }
        int index = reader.readInt("Wybierz numer produktu: ") - 1;

        if (index < 0 || index >= products.size()) {
            System.out.println("Nieprawidłowy numer produktu.");
            return;
        }
        Product product = products.get(index);

        ProductConfiguration configuration = askForConfiguration(product);
        int quantity = reader.readInt("Podaj ilość: ");

        try {
            cartService.addToCart(product.getId(), configuration, quantity);
            System.out.println("Dodano do koszyka.");
        } catch (ElectroShopException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("Nie udało się dodać do koszyka: " + e.getMessage());
        }
    }

    private void viewCart() {
        List<CartItem> items = cartService.viewCart();
        if (items.isEmpty()) {
            System.out.println("Koszyk jest pusty.");
            return;
        }
        System.out.println();
        System.out.println("--- Twój koszyk ---");
        items.forEach(item -> System.out.println(
                item.getProductName() + " x" + item.getQuantity() + " - " + item.getSubtotal() + " zł"));
    }

    private void placeOrder() {
        if (cartService.viewCart().isEmpty()) {
            System.out.println("Koszyk jest pusty.");
            return;
        }

        System.out.println("Podaj swoje dane do zamówienia:");
        String id = "CU-" + System.currentTimeMillis(); // uproszczone ID do demo CLI
        String firstName = readNonBlank("Imię: ");
        String lastName = readNonBlank("Nazwisko: ");
        String email = readNonBlank("E-mail: ");

        Customer customer = new Customer(id, firstName, lastName, email);

        try {
            customerManager.addCustomer(customer);
            Order order = cartService.placeOrder(customer);
            Invoice invoice = orderProcessor.processOrder(order);
            myOrders.add(order);

            System.out.println("Zamówienie złożone!");
            System.out.println(order);
            System.out.println(invoice);
        } catch (ElectroShopException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("Nie udało się złożyć zamówienia: " + e.getMessage());
        }
    }

    private void cancelOrder() {
        if (myOrders.isEmpty()) {
            System.out.println("Nie złożyłeś jeszcze żadnego zamówienia.");
            return;
        }

        System.out.println("--- Twoje zamówienia ---");
        for (int i = 0; i < myOrders.size(); i++) {
            Order order = myOrders.get(i);
            System.out.println((i + 1) + ". " + order + " [status: " + order.getStatus() + "]");
        }
        int index = reader.readInt("Wybierz numer zamówienia do anulowania: ") - 1;
        if (index < 0 || index >= myOrders.size()) {
            System.out.println("Nieprawidłowy numer zamówienia.");
            return;
        }

        Order order = myOrders.get(index);
        try {
            orderProcessor.cancelOrder(order.getOrderId());
            System.out.println("Zamówienie anulowane.");
        } catch (ElectroShopException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("Nie udało się anulować zamówienia: " + e.getMessage());
        }
    }

    /*
    Uproszczone podejście przez instanceof do demonstracji CLI
    można by to zrobić generycznie, dodając do Product opis wymaganych pytań konfiguracyjnych
    Rzuci wyjątek jeśli opcja spoza zakresu
     */

    private ProductConfiguration askForConfiguration(Product product) {
        if (product instanceof Computer computer) {
            String cpu = chooseFromList("Dostępne procesory:", computer.getAvailableCpus());
            int ram = chooseFromList("Dostępne ilości RAM (GB):", computer.getAvailableRamOptions());
            return new ComputerConfiguration(cpu, ram);
        }

        if (product instanceof Smartphone smartphone) {
            String color = chooseFromList("Dostępne kolory:", smartphone.getAvailableColors());
            int battery = chooseFromList("Dostępne pojemności baterii (mAh):", smartphone.getAvailableBatteryCapacities());
            List<String> accessories = chooseAccessories(smartphone.getAvailableAccessories());
            return new SmartphoneConfiguration(color, battery, accessories);
        }

        return new NoConfiguration();
    }
    private <T> T chooseFromList(String header, List<T> options) {
        System.out.println(header);
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        int index = reader.readInt("Wybierz numer: ") - 1;
        return options.get(index);
    }

    private List<String> chooseAccessories(List<String> availableAccessories) {
        List<String> selected = new ArrayList<>();
        System.out.println("Dostępne akcesoria:");
        for (int i = 0; i < availableAccessories.size(); i++) {
            System.out.println((i + 1) + ". " + availableAccessories.get(i));
        }
        System.out.println("Podaj numery akcesoriów oddzielone spacją (Enter, jeśli żadne):");
        String input = reader.readLine();
        if (input.isBlank()) {
            return selected;
        }
        for (String token : input.split("\\s+")) {
            try {
                int index = Integer.parseInt(token) - 1;
                if (index >= 0 && index < availableAccessories.size()) {
                    selected.add(availableAccessories.get(index));
                }
            } catch (NumberFormatException ignored) {
                // pomijam nieprawidłowy
            }
        }
        return selected;
    }

    private String readNonBlank(String prompt) {
        String value;
        do {
            System.out.print(prompt);
            value = reader.readLine();
        } while (value.isBlank());
        return value;
    }
}
