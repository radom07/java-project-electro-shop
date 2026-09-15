package pl.adrian.electroshop.cli;

import lombok.RequiredArgsConstructor;
import pl.adrian.electroshop.exception.ElectroShopException;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.model.order.OrderStatus;
import pl.adrian.electroshop.model.product.Computer;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.Smartphone;
import pl.adrian.electroshop.repository.OrderRepository;
import pl.adrian.electroshop.service.OrderProcessor;
import pl.adrian.electroshop.service.ProductManager;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EmployeeMenu {

    private final ConsoleReader reader;
    private final ProductManager productManager;
    private final OrderRepository orderRepository;
    private final OrderProcessor orderProcessor;

    public void show() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Menu pracownika ---");
            System.out.println("1. Przeglądaj produkty");
            System.out.println("2. Dodaj produkt");
            System.out.println("3. Usuń produkt");
            System.out.println("4. Zmień status zamówienia");
            System.out.println("0. Wróć");
            System.out.print("Wybierz opcję: ");

            switch (reader.readLine()) {
                case "1" -> browseProducts();
                case "2" -> addProduct();
                case "3" -> removeProduct();
                case "4" -> changeOrderStatus();
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
        products.forEach(System.out::println);
    }

    private void addProduct() {
        System.out.println("Jaki typ produktu?");
        System.out.println("1. Electronics");
        System.out.println("2. Computer");
        System.out.println("3. Smartphone");
        String choice = reader.readLine();

        String id = "P-" + System.currentTimeMillis(); // uproszczone ID do demo CLI
        System.out.print("Nazwa produktu: ");
        String name = reader.readLine();
        BigDecimal price = new BigDecimal(readLineReplacingComma("Cena: "));
        int quantity = reader.readInt("Ilość: ");

        Product product = switch (choice) {
            case "2" -> {
                List<String> cpus = readCommaSeparatedStrings("Dostępne CPU (oddzielone przecinkiem, np. Intel i5,Intel i7): ");
                List<Integer> rams = readCommaSeparatedInts("Dostępne RAM w GB (oddzielone przecinkiem, np. 8,16,32): ");
                yield new Computer(id, name, price, quantity, cpus, rams);
            }
            case "3" -> {
                List<String> colors = readCommaSeparatedStrings("Dostępne kolory (oddzielone przecinkiem): ");
                List<Integer> batteries = readCommaSeparatedInts("Dostępne pojemności baterii w mAh (oddzielone przecinkiem): ");
                List<String> accessories = readCommaSeparatedStrings("Dostępne akcesoria (oddzielone przecinkiem): ");
                yield new Smartphone(id, name, price, quantity, colors, batteries, accessories);
            }
            default -> new Electronics(id, name, price, quantity);
        };

        try {
            productManager.addProduct(product);
            System.out.println("Produkt dodany.");
        } catch (ElectroShopException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("Nie udało się dodać produktu: " + e.getMessage());
        }
    }

    private void removeProduct() {
        System.out.print("Podaj ID produktu do usunięcia: ");
        String id = reader.readLine();
        try {
            productManager.removeProduct(id);
            System.out.println("Produkt usunięty.");
        } catch (IllegalArgumentException e) {
            System.out.println("Nie udało się usunąć produktu: " + e.getMessage());
        }
    }

    // brak opcji na cancel, ale to demo cli
    private void changeOrderStatus() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            System.out.println("Brak zamówień w systemie.");
            return;
        }

        System.out.println("--- Zamówienia ---");
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            System.out.println((i + 1) + ". " + order + " [status: " + order.getStatus() + "]");
        }
        int index = reader.readInt("Wybierz numer zamówienia: ") - 1;
        if (index < 0 || index >= orders.size()) {
            System.out.println("Nieprawidłowy numer zamówienia.");
            return;
        }
        Order order = orders.get(index);

        System.out.println("Nowy status:");
        List<OrderStatus> statuses = Arrays.asList(OrderStatus.values());
        for (int i = 0; i < statuses.size(); i++) {
            System.out.println((i + 1) + ". " + statuses.get(i));
        }
        int statusIndex = reader.readInt("Wybierz numer statusu: ") - 1;
        if (statusIndex < 0 || statusIndex >= statuses.size()) {
            System.out.println("Nieprawidłowy numer statusu.");
            return;
        }

        try {
            orderProcessor.changeOrderStatus(order.getOrderId(), statuses.get(statusIndex));
            System.out.println("Status zaktualizowany.");
        } catch (ElectroShopException | IllegalArgumentException | IllegalStateException e) {
            System.out.println("Nie udało się zmienić statusu: " + e.getMessage());
        }
    }

    private String readLineReplacingComma(String prompt) {
        System.out.print(prompt);
        return reader.readLine().replace(",", ".");
    }

    private List<String> readCommaSeparatedStrings(String prompt) {
        System.out.print(prompt);
        String input = reader.readLine();
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    // rzuci NumberFormatException jeśli coś nienumerycznego, dla prostego CLI zostawiam
    private List<Integer> readCommaSeparatedInts(String prompt) {
        System.out.print(prompt);
        String input = reader.readLine();
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}