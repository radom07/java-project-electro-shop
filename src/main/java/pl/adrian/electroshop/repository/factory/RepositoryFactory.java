package pl.adrian.electroshop.repository.factory;

import pl.adrian.electroshop.repository.InvoiceRepository;
import pl.adrian.electroshop.repository.OrderRepository;

/*
Design Pattern: ABSTRACT FACTORY (creational, GoF)
An interface producing a consistent FAMILY of objects responsible for order data
storage: OrderRepository and link InvoiceRepository
The family isconsistent by design - FileInvoiceRepository must receive the exact same
OrderRepository used by the rest of the application to read the order associated with an invoice
Without the Abstract Factory, mistakes are easy: one could accidentally assemble FileInvoiceRepository
with InMemoryOrderRepository and end up with an inconsistent data store

Each implementation of this interface guarantees that both returned implementations
belong to the same "family"

Adding a third storage type requires only a single new class
implementing this interface - zero changes in Main or in classes
consuming the repositories code OrderProcessor, code CartService
 */
public interface RepositoryFactory {
    OrderRepository createOrderRepository();

    InvoiceRepository createInvoiceRepository(OrderRepository orderRepository);
}
