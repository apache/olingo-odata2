package org.apache.olingo.odata2.jpa.processor.ref.converter;

import java.util.UUID;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, String> {

  @Override
  public String convertToDatabaseColumn(UUID uuid) {
    return uuid.toString();
  }

  @Override
  public UUID convertToEntityAttribute(String string) {
    return UUID.fromString(string);
  }

}
