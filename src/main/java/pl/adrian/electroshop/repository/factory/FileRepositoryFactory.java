package pl.adrian.electroshop.repository.factory;

import pl.adrian.electroshop.repository.InvoiceRepository;
import pl.adrian.electroshop.repository.OrderRepository;
import pl.adrian.electroshop.repository.file.FileInvoiceRepository;
import pl.adrian.electroshop.repository.file.FileOrderRepository;
import pl.adrian.electroshop.repository.file.serialization.InvoiceFileSerializer;
import pl.adrian.electroshop.repository.file.serialization.OrderFileSerializer;

import java.nio.file.Path;

public class FileRepositoryFactory implements RepositoryFactory {

    @Override
    public OrderRepository createOrderRepository() {
        return new FileOrderRepository(Path.of("data/orders"), new OrderFileSerializer());
    }

    @Override
    public InvoiceRepository createInvoiceRepository(OrderRepository orderRepository) {
        return new FileInvoiceRepository(
                Path.of("data/invoices"),
                new InvoiceFileSerializer(orderRepository));
    }
}
