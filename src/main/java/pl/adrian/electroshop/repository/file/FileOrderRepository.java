package pl.adrian.electroshop.repository.file;

import lombok.NonNull;
import pl.adrian.electroshop.exception.FileRepositoryException;
import pl.adrian.electroshop.model.order.Order;
import pl.adrian.electroshop.repository.OrderRepository;
import pl.adrian.electroshop.repository.file.serialization.OrderFileSerializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileOrderRepository implements OrderRepository {

    private final Path directory;
    private final OrderFileSerializer serializer;

    public FileOrderRepository(Path directory, OrderFileSerializer serializer) {
        this.directory = directory;
        this.serializer = serializer;
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new FileRepositoryException("Could not create orders directory: " + directory, e);
        }
    }

    @Override
    public void save(@NonNull Order order) {
        try {
            String serializedData = serializer.serialize(order);
            Files.writeString(fileFor(order.getOrderId()), serializedData, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FileRepositoryException("Could not save order: " + order.getOrderId(), e);
        }
    }

    @Override
    public void deleteById(@NonNull String id) {
        try {
            Files.deleteIfExists(fileFor(id));
        } catch (IOException e) {
            throw new FileRepositoryException("Could not delete order: " + id, e);
        }
    }

    @Override
    public Optional<Order> findById(@NonNull String id) {
        Path file = fileFor(id);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            return Optional.of(serializer.deserialize(lines));
        } catch (IOException e) {
            throw new FileRepositoryException("Could not read order file: " + file, e);
        }
    }

    @Override
    public List<Order> findAll() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.txt")) {
            List<Order> orders = new ArrayList<>();
            for (Path file : stream) {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                orders.add(serializer.deserialize(lines));
            }
            return List.copyOf(orders);
        } catch (IOException e) {
            throw new FileRepositoryException("Could not list orders in: " + directory, e);
        }
    }

    private Path fileFor(String orderId) {
        return directory.resolve(orderId + ".txt");
    }
}