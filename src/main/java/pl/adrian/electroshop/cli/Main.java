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
import pl.adrian.electroshop.repository.factory.FileRepositoryFactory;
import pl.adrian.electroshop.repository.factory.InMemoryRepositoryFactory;
import pl.adrian.electroshop.repository.factory.RepositoryFactory;
import pl.adrian.electroshop.repository.inmemory.InMemoryCustomerRepository;
import pl.adrian.electroshop.repository.inmemory.InMemoryProductRepository;
import pl.adrian.electroshop.service.CartService;
import pl.adrian.electroshop.service.CustomerManager;
import pl.adrian.electroshop.service.DiscountService;
import pl.adrian.electroshop.service.OrderProcessor;
import pl.adrian.electroshop.service.ProductManager;
import pl.adrian.electroshop.service.concurrency.ProductLockRegistry;
import pl.adrian.electroshop.service.invoice.InvoiceNumberGenerator;
import pl.adrian.electroshop.service.invoice.SequentialInvoiceNumberGenerator;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        ConsoleReader reader = new ConsoleReader();

        System.out.println("Gdzie przechowywać zamówienia i faktury?");
        System.out.println("1. W pamięci (dane znikają po zamknięciu)");
        System.out.println("2. W plikach (data/orders, data/invoices)");
        System.out.print("Wybierz opcję: ");
        String storageChoice = reader.readLine();

        /*
        Design Pattern: STRATEGY (behavioral, GoF) applied to the selection of the data
        storage mechanism

        OrderRepository and InvoiceRepository are strategy interfaces
        InMemoryOrderRepository and FileOrderRepository are two interchangeable
        implementations of the same algorithm how to save/read an order
        The choice is made once, in a single place
        The entire rest of the system OrderProcessor, CartService, CLI menu depends solely on the
        interface and never checks which implementation it is communicating with
         */
        RepositoryFactory repositoryFactory = "2".equals(storageChoice)
                ? new FileRepositoryFactory()
                : new InMemoryRepositoryFactory();

        OrderRepository orderRepository = repositoryFactory.createOrderRepository();
        InvoiceRepository invoiceRepository = repositoryFactory.createInvoiceRepository(orderRepository);

        System.out.println("2".equals(storageChoice)
                ? "--> Wybrano zapis do plików."
                : "--> Wybrano zapis w pamięci.");

        ProductRepository productRepository = new InMemoryProductRepository();
        CustomerRepository customerRepository = new InMemoryCustomerRepository();

        Clock systemClock = Clock.systemUTC();
        Clock accountingClock = systemClock.withZone(ZoneId.of("Europe/Warsaw"));

        ProductLockRegistry productLockRegistry = new ProductLockRegistry();
        ProductManager productManager = new ProductManager(productRepository, productLockRegistry);
        CustomerManager customerManager = new CustomerManager(customerRepository);
        InvoiceNumberGenerator invoiceNumberGenerator = new SequentialInvoiceNumberGenerator(accountingClock);

        OrderProcessor orderProcessor = new OrderProcessor(
                orderRepository, invoiceRepository, invoiceNumberGenerator, productManager, accountingClock);

        Cart cart = new Cart();
        DiscountService discountService = new DiscountService();
        CartService cartService = new CartService(productManager, cart, systemClock, discountService);

        seedSampleProducts(productManager);

        CustomerMenu customerMenu = new CustomerMenu(reader, productManager, cartService, customerManager, orderProcessor);
        EmployeeMenu employeeMenu = new EmployeeMenu(reader, productManager, orderRepository, orderProcessor);

        System.out.println("\nDemo CLI ElectroShop");
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