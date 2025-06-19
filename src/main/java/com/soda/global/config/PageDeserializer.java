package com.soda.global.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageDeserializer extends JsonDeserializer<PageImpl<?>> {

    @Override
    public PageImpl<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        List<?> content = mapper.convertValue(
                node.get("content"),
                mapper.getTypeFactory().constructCollectionType(List.class, Object.class)
        );

        long total = node.get("totalElements").asLong();

        JsonNode pageableData = node.get("pageable").get(1);
        int number = pageableData.get("pageNumber").asInt();
        int size = pageableData.get("pageSize").asInt();

        JsonNode sortNode = node.get("sort");
        List<Sort.Order> orders = new ArrayList<>();
        if (sortNode != null && sortNode.isArray()) {
            for (final JsonNode objNode : sortNode) {
                if (objNode.has("property") && objNode.has("direction")) {
                    String property = objNode.get("property").asText();
                    String direction = objNode.get("direction").asText();
                    orders.add(new Sort.Order(Sort.Direction.fromString(direction), property));
                }
            }
        }

        PageRequest pageRequest = PageRequest.of(number, size, Sort.by(orders));
        return new PageImpl<>(content, pageRequest, total);
    }
}