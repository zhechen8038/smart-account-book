package com.example.project1server.service;

import com.example.project1server.dto.BillRecognitionResponse;
import com.example.project1server.exception.BusinessException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class BillRecognitionService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public BillRecognitionService(
            ObjectMapper objectMapper,
            @Value("${qwen.api-key}") String apiKey,
            @Value("${qwen.model}") String model,
            @Value("${qwen.base-url}") String baseUrl
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "未配置DASHSCOPE_API_KEY环境变量"
            );
        }

        this.objectMapper = objectMapper;
        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        "Authorization",
                        "Bearer " + apiKey
                )
                .build();
    }

    public BillRecognitionResponse recognize(MultipartFile image) {
        validateImage(image);

        try {
            String mimeType = image.getContentType();

            String base64 = Base64.getEncoder()
                    .encodeToString(image.getBytes());

            String imageDataUrl =
                    "data:" + mimeType + ";base64," + base64;

            Map<String, Object> imageContent = Map.of(
                    "type", "image_url",
                    "image_url", Map.of(
                            "url", imageDataUrl
                    )
            );

            Map<String, Object> textContent = Map.of(
                    "type", "text",
                    "text", createPrompt()
            );

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", List.of(imageContent, textContent)
            );

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(message),
                    "temperature", 0.1,
                    "stream", false
            );

            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new BusinessException("通义千问没有返回识别结果");
            }

            String resultText = response
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            resultText = cleanJson(resultText);

            return objectMapper.readValue(
                    resultText,
                    BillRecognitionResponse.class
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(
                    "账单识别失败：" + exception.getMessage()
            );
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException("请选择支付宝账单图片");
        }

        String contentType = image.getContentType();

        if (!List.of(
                "image/jpeg",
                "image/png",
                "image/webp"
        ).contains(contentType)) {
            throw new BusinessException(
                    "只支持JPG、PNG或WebP图片"
            );
        }

        if (image.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("图片不能超过10MB");
        }
    }

    private String createPrompt() {
        return """
                请识别这张支付宝账单截图。

                只返回JSON，不要返回Markdown或其他解释。

                JSON格式：
                {
                  "type": "INCOME或EXPENSE",
                  "category": "简短中文类目",
                  "amount": 交易金额数字,
                  "recordDate": "yyyy-MM-dd",
                  "remark": "交易对象和商品说明",
                  "confidence": 0到1之间的可信度
                }

                判断规则：
                1. 收款、退款到账、工资到账属于INCOME。
                2. 付款、消费、转出属于EXPENSE。
                3. category示例：餐饮消费、话费消费、交通消费、工资收入。
                4. amount不能包含人民币符号。
                5. 不要将余额、优惠金额识别为实际交易金额。
                """;
    }

    private String cleanJson(String text) {
        String cleaned = text.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }
}