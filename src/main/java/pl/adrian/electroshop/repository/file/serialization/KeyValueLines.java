package pl.adrian.electroshop.repository.file.serialization;

import pl.adrian.electroshop.exception.CorruptedFileDataException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyValueLines {

    private final Map<String, String> values;

    private KeyValueLines(Map<String, String> values) {
        this.values = values;
    }

    public static KeyValueLines parse(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split("=", 2);
            values.put(parts[0], parts.length > 1 ? parts[1] : "");
        }
        return new KeyValueLines(values);
    }

    public String getRequired(String key) {
        String value = values.get(key);
        if (value == null) {
            throw new CorruptedFileDataException("Missing required key in file data: " + key);
        }
        return value;
    }
}