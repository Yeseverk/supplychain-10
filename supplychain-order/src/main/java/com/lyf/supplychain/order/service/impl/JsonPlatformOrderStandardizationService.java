package com.lyf.supplychain.order.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyf.supplychain.common.exception.BusinessException;
import com.lyf.supplychain.order.request.OrderAddressRequest;
import com.lyf.supplychain.order.request.OrderCreateRequest;
import com.lyf.supplychain.order.request.OrderItemRequest;
import com.lyf.supplychain.order.service.PlatformOrderStandardizationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JSON 平台订单标准化服务实现。
 *
 * @author liyunfei
 * @date 2026-05-25
 */
@Service
public class JsonPlatformOrderStandardizationService implements PlatformOrderStandardizationService {

    private final ObjectMapper objectMapper;

    public JsonPlatformOrderStandardizationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将平台原始 JSON 报文转换为 OMS 标准订单创建请求。
     *
     * @param platform 平台编码
     * @param rawData  原始报文
     * @return 标准订单创建请求
     */
    @Override
    public OrderCreateRequest standardize(String platform, String rawData) {
        try {
            JsonNode root = objectMapper.readTree(rawData);
            OrderCreateRequest request = new OrderCreateRequest();
            request.setPlatform(platform);
            request.setPlatformOrderNo(text(root, "platformOrderNo", "orderNo", "order_id", "id"));
            request.setStoreId(longValue(root, "storeId", "store_id"));
            request.setCurrency(defaultText(root, "USD", "currency", "currency_code"));
            request.setDiscountAmount(decimal(root, BigDecimal.ZERO, "discountAmount", "discount_amount", "total_discount"));
            request.setShippingFee(decimal(root, BigDecimal.ZERO, "shippingFee", "shipping_fee", "shipping_price"));
            request.setExchangeRate(decimal(root, BigDecimal.ONE, "exchangeRate", "exchange_rate"));
            request.setWarehouseId(requiredLong(root, "warehouseId", "warehouse_id"));
            request.setDeliveryDeadline(date(root, "deliveryDeadline", "delivery_deadline"));
            request.setPlatformOrderTime(dateTime(root, "platformOrderTime", "platform_order_time", "created_at"));
            request.setPlatformPayTime(dateTime(root, "platformPayTime", "platform_pay_time", "paid_at"));
            request.setItems(items(root));
            request.setAddress(address(root));
            if (request.getPlatformOrderNo() == null || request.getPlatformOrderNo().isBlank()) {
                BusinessException.throwException(15015, "平台订单号缺失，无法标准化");
            }
            if (request.getItems().isEmpty()) {
                BusinessException.throwException(15015, "平台订单明细缺失，无法标准化");
            }
            return request;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            BusinessException.throwException(15015, "平台订单标准化失败，请检查原始报文字段");
            return null;
        }
    }

    private List<OrderItemRequest> items(JsonNode root) {
        JsonNode itemsNode = firstNode(root, "items", "lineItems", "line_items", "orderItems");
        if (itemsNode == null || !itemsNode.isArray()) {
            return List.of();
        }
        List<OrderItemRequest> result = new ArrayList<>();
        Iterator<JsonNode> iterator = itemsNode.elements();
        while (iterator.hasNext()) {
            JsonNode node = iterator.next();
            OrderItemRequest item = new OrderItemRequest();
            item.setSkuId(requiredLong(node, "skuId", "sku_id"));
            item.setSkuCode(text(node, "skuCode", "sku_code", "sku"));
            item.setSkuName(text(node, "skuName", "sku_name", "title", "name"));
            item.setPlatformSkuId(text(node, "platformSkuId", "platform_sku_id", "variant_id"));
            item.setQuantity(integer(node, 1, "quantity", "qty"));
            item.setUnitPrice(decimal(node, BigDecimal.ZERO, "unitPrice", "unit_price", "price"));
            item.setDiscount(decimal(node, BigDecimal.ZERO, "discount", "discount_amount"));
            result.add(item);
        }
        return result;
    }

    private OrderAddressRequest address(JsonNode root) {
        JsonNode node = firstNode(root, "address", "shippingAddress", "shipping_address", "recipient");
        if (node == null || node.isMissingNode() || node.isNull()) {
            BusinessException.throwException(15015, "收货地址缺失，无法标准化");
        }
        OrderAddressRequest address = new OrderAddressRequest();
        address.setReceiverName(text(node, "receiverName", "receiver_name", "name", "recipient_name"));
        address.setPhone(text(node, "phone", "receiver_phone", "phone_number"));
        address.setEmail(text(node, "email"));
        address.setCountryCode(text(node, "countryCode", "country_code"));
        address.setCountryName(text(node, "countryName", "country_name", "country"));
        address.setState(text(node, "state", "province"));
        address.setCity(text(node, "city"));
        address.setAddressLine1(text(node, "addressLine1", "address_line1", "address1"));
        address.setAddressLine2(text(node, "addressLine2", "address_line2", "address2"));
        address.setZipCode(text(node, "zipCode", "zip_code", "zip", "postcode"));
        return address;
    }

    private JsonNode firstNode(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String... names) {
        JsonNode value = firstNode(node, names);
        return value == null ? null : value.asText();
    }

    private String defaultText(JsonNode node, String defaultValue, String... names) {
        String value = text(node, names);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private Long longValue(JsonNode node, String... names) {
        JsonNode value = firstNode(node, names);
        return value == null ? null : value.asLong();
    }

    private Long requiredLong(JsonNode node, String... names) {
        Long value = longValue(node, names);
        if (value == null || value <= 0) {
            BusinessException.throwException(15015, "平台订单关键ID缺失，无法标准化");
        }
        return value;
    }

    private Integer integer(JsonNode node, Integer defaultValue, String... names) {
        JsonNode value = firstNode(node, names);
        return value == null ? defaultValue : value.asInt();
    }

    private BigDecimal decimal(JsonNode node, BigDecimal defaultValue, String... names) {
        JsonNode value = firstNode(node, names);
        if (value == null || value.asText().isBlank()) {
            return defaultValue;
        }
        return new BigDecimal(value.asText());
    }

    private LocalDate date(JsonNode node, String... names) {
        String value = text(node, names);
        return value == null || value.isBlank() ? null : LocalDate.parse(value.substring(0, 10));
    }

    private LocalDateTime dateTime(JsonNode node, String... names) {
        String value = text(node, names);
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.endsWith("Z") || value.matches(".*[+-]\\d{2}:\\d{2}$")) {
            return OffsetDateTime.parse(value).toLocalDateTime();
        }
        return LocalDateTime.parse(value);
    }
}
