package com.medscan.app_med.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiScannerService implements ScannerService {

    private static final String EXTRACTION_PROMPT =
            "Extrae del prospecto o receta los datos del medicamento y responde solo con JSON con los campos: "
                    + "brandName, activeIngredient, dosage, frequency.";

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String url;

    @org.springframework.beans.factory.annotation.Autowired
    public OpenAiScannerService(ObjectMapper objectMapper,
                                RestClient.Builder restClientBuilder,
                                @Value("${app.scan.openai.api-key:}") String apiKey,
                                @Value("${app.scan.openai.model:gpt-4o-mini}") String model,
                                @Value("${app.scan.openai.url:https://api.openai.com/v1/chat/completions}") String url,
                                @Value("${app.scan.openai.timeout-ms:15000}") int timeoutMs) {

        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.url = url;

        org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeoutMs);
        requestFactory.setReadTimeout(timeoutMs);

        this.restClient = restClientBuilder.requestFactory(requestFactory).build();
    }

    public OpenAiScannerService(ObjectMapper objectMapper,
                                String apiKey,
                                String model,
                                String url) {
        this(objectMapper, RestClient.builder(), apiKey, model, url, 15000);
    }

    @Override
    public ScannedMedication scan(byte[] imageBytes, String contentType) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ScanProcessingException("La imagen recibida está vacía");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new ScanProcessingException("La API key de OpenAI no está configurada");
        }

        String payload = buildRequest(imageBytes, contentType);
        String response = callOpenAi(payload);
        return parseResponse(response);
    }

    public ScannedMedication parseResponse(String responseJson) {
        JsonNode root = readTree(responseJson);
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new ScanProcessingException("La respuesta de OpenAI no contiene resultados");
        }

        String content = choices.get(0).path("message").path("content").asText(null);
        if (content == null || content.isBlank()) {
            throw new ScanProcessingException("La respuesta de OpenAI no contiene contenido extraído");
        }
        return parseMedicationContent(content);
    }

    private ScannedMedication parseMedicationContent(String content) {
        String cleanJson = stripMarkdownCodeBlocks(content);
        JsonNode medication = readTree(cleanJson);
        return new ScannedMedication(
                medication.path("brandName").asText(null),
                medication.path("activeIngredient").asText(null),
                medication.path("dosage").asText(null),
                medication.path("frequency").asText(null));
    }

    private String stripMarkdownCodeBlocks(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private String buildRequest(byte[] imageBytes, String contentType) {
        String dataUrl = buildDataUrl(imageBytes, contentType);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(Map.of(
                "role", "user",
                "content", List.of(
                        Map.of("type", "text", "text", EXTRACTION_PROMPT),
                        Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))))));
        body.put("response_format", Map.of("type", "json_object"));

        return writeJson(body);
    }

    private String buildDataUrl(byte[] imageBytes, String contentType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return "data:" + contentType + ";base64," + base64Image;
    }

    private String callOpenAi(String payload) {
        try {
            return restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            throw new ScanProcessingException("Error en la comunicación con OpenAI: " + ex.getMessage(), ex);
        }
    }


    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new ScanProcessingException("Respuesta JSON inválida del servicio de escaneo", e);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new ScanProcessingException("No se pudo construir la petición de escaneo", e);
        }
    }
}
