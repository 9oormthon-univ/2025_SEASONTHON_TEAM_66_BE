package com.goormthon.careroad.common.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 엔티티 필드에 @Convert(converter = EncryptedStringConverter.class)
 * 를 붙여 문자열을 투명 암호화/복호화한다.
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static AesGcmEncryptor staticEncryptor;

    @Autowired
    public void setEncryptor(AesGcmEncryptor encryptor) { staticEncryptor = encryptor; }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return staticEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return staticEncryptor.decrypt(dbData);
    }
}
