package pl.adrian.electroshop.cli;

import pl.adrian.electroshop.model.cart.Cart;
import pl.adrian.electroshop.model.product.Computer;
import pl.adrian.electroshop.model.product.Electronics;
import pl.adrian.electroshop.model.product.Product;
import pl.adrian.electroshop.model.product.Smartphone;
import pl.adrian.electroshop.repository.CustomerRepository;
import pl.adrian.electroshop.repository.InvoiceRepository;
import pl.adrian.electroshop.repository.OrderRepository;
import pl.adrian.electroshop.repository.ProductRepository;
import pl.adrian.electroshop.repository.inmemory.InMemoryCustomerRepository;
import pl.adrian.electroshop.repository.inmemory.InMemoryInvoiceRepository;
import pl.adrian.electroshop.repository.inmemory.InMemoryOrderRepository;
import pl.adrian.electroshop.repository.inmemory.InMemoryProductRepository;
import pl.adrian.electroshop.service.CartService;
import pl.adrian.electroshop.service.CustomerManager;
import pl.adrian.electroshop.service.OrderProcessor;
import pl.adrian.electroshop.service.ProductManager;
import pl.adrian.electroshop.service.invoice.InvoiceNumberGenerator;
import pl.adrian.electroshop.service.invoice.SequentialInvoiceNumberGenerator;

import java.math.BigDecimal;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        ProductRepository productRepository = new InMemoryProductRepository();
        CustomerRepository customerRepository = new InMemoryCustomerRepository();
        OrderRepository orderRepository = new InMemoryOrderRepository();
        InvoiceRepository invoiceRepository = new InMemoryInvoiceRepository();

        ProductManager productManager = new ProductManager(productRepository);
        CustomerManager customerManager = new CustomerManager(customerRepository);
        InvoiceNumberGenerator invoiceNumberGenerator = new SequentialInvoiceNumberGenerator();
        OrderProcessor orderProcessor =
                new OrderProcessor(orderRepository, invoiceRepository, invoiceNumberGenerator, productManager);

        Cart cart = new Cart();
        CartService cartService = new CartService(productManager, cart);

        seedSampleProducts(productManager);

        ConsoleReader reader = new ConsoleReader();
        CustomerMenu customerMenu = new CustomerMenu(reader, productManager, cartService, customerManager, orderProcessor);
        EmployeeMenu employeeMenu = new EmployeeMenu(reader, productManager, orderRepository, orderProcessor);

        System.out.println("Demo CLI ElectroShop");
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("Kim jesteś?");
            System.out.println("1. Klient");
            System.out.println("2. Pracownik sklepu");
            System.out.println("0. Wyjście");
            System.out.print("Wybierz opcję: ");

            switch (reader.readLine()) {
                case "1" -> customerMenu.show();
                case "2" -> employeeMenu.show();
                case "0" -> running = false;
                default -> System.out.println("Nieznana opcja.");
            }
        }

        System.out.println("Do zobaczenia!");
    }

    // produkty testowe
    private static void seedSampleProducts(ProductManager productManager) {
        Product cable = new Electronics("E1", "Kabel USB-C", new BigDecimal("29.99"), 50);

        Product laptop = new Computer("C1", "Dell XPS 15", new BigDecimal("5999.00"), 5,
                List.of("Intel i5", "Intel i7"), List.of(8, 16, 32));

        Product phone = new Smartphone("S1", "Galaxy S24", new BigDecimal("3499.00"), 8,
                List.of("Czarny", "Srebrny"), List.of(4000, 5000),
                List.of("Etui", "Szkło ochronne", "Ładowarka"));

        productManager.addProduct(cable);
        productManager.addProduct(laptop);
        productManager.addProduct(phone);
    }
}
