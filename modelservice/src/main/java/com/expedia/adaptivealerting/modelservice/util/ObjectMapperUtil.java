package com.expedia.adaptivealerting.modelservice.util;

import com.expedia.adaptivealerting.modelservice.entity.ElasticsearchDetector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ObjectMapperUtil {

    private ObjectMapper objectMapper = new ObjectMapper();

    public String convertToString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert to Json", e);
        }
    }

    public Object convertToObject(String data, TypeReference c) {
        try {
            return objectMapper.readValue(data, c);
        } catch (IOException e) {
            throw new HibernateException("unable to read object from result set", e);
        }
    }

}
