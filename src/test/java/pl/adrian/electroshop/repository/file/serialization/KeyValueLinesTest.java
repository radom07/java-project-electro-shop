package pl.adrian.electroshop.repository.file.serialization;

import org.junit.jupiter.api.Test;
import pl.adrian.electroshop.exception.CorruptedFileDataException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeyValueLinesTest {

    @Test
    void shouldParseValidLinesProperly() {
        // given
        List<String> lines = List.of(
                "orderId=OR123",
                "status=PLACED",
                "emptyValue="
        );

        // when
        KeyValueLines parsed = KeyValueLines.parse(lines);

        // then
        assertThat(parsed.getRequired("orderId")).isEqualTo("OR123");
        assertThat(parsed.getRequired("status")).isEqualTo("PLACED");
        assertThat(parsed.getRequired("emptyValue")).isEmpty();
    }

    @Test
    void shouldIgnoreBlankLines() {
        // given
        List<String> lines = List.of(
                "key1=value1",
                "   ",
                "",
                "key2=value2"
        );

        // when
        KeyValueLines parsed = KeyValueLines.parse(lines);

        // then
        assertThat(parsed.getRequired("key1")).isEqualTo("value1");
        assertThat(parsed.getRequired("key2")).isEqualTo("value2");
    }

    @Test
    void shouldThrowExceptionWhenRequiredKeyIsMissing() {
        // given
        List<String> lines = List.of("existingKey=value");
        KeyValueLines parsed = KeyValueLines.parse(lines);

        // when & then
        assertThatThrownBy(() -> parsed.getRequired("missingKey"))
                .isInstanceOf(CorruptedFileDataException.class)
                .hasMessageContaining("Missing required key in file data: missingKey");
    }
}