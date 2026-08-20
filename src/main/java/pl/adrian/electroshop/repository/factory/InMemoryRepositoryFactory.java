package pl.adrian.electroshop.repository.factory;

import pl.adrian.electroshop.repository.InvoiceRepository;
import pl.adrian.electroshop.repository.OrderRepository;
import pl.adrian.electroshop.repository.inmemory.InMemoryInvoiceRepository;
import pl.adrian.electroshop.repository.inmemory.InMemoryOrderRepository;

public class InMemoryRepositoryFactory implements RepositoryFactory {

    @Override
    public OrderRepository createOrderRepository() {
        return new InMemoryOrderRepository();
    }

    @Override
    public InvoiceRepository createInvoiceRepository(OrderRepository orderRepository) {
        return new InMemoryInvoiceRepository();
    }
}
