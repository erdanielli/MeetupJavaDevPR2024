package study.meetup.springuicada.pg;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public final class JsonRowMapperFactory {
    private final ObjectMapper objectMapper;

    public JsonRowMapperFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> RowMapper<T> forType(Class<T> type) {
        return (rs, _) -> {
            try (var jsonb = rs.getBinaryStream(1)) {
                if (jsonb == null) return null;
                return objectMapper.readValue(jsonb, type);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
