package mu.server.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mu.server.persistence.util.AESConverter;
import org.springframework.stereotype.Component;

@Converter
@Component
@RequiredArgsConstructor
public class EncryptionConverter implements AttributeConverter<String, String> {


    private final AESConverter aesConverter;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return Optional.ofNullable(attribute)
                .map(aesConverter::encrypt)
                .orElse(null);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return Optional.ofNullable(dbData)
                .map(aesConverter::decrypt)
                .orElse(null);
    }
}
