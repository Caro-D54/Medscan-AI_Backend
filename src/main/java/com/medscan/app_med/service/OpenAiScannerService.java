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
    private final String apiKey;
    private final String model;
    private final String url;

    public OpenAiScannerService(ObjectMapper objectMapper,
                                @Value("${app.scan.openai.api-key:}") String apiKey,
                                @Value("${app.scan.openai.model:gpt-4o-mini}") String model,
                                @Value("${app.scan.openai.url:https://api.openai.com/v1/chat/completions}") String url) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.url = url;
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
        JsonNode medication = readTree(content);
        return new ScannedMedication(
                medication.path("brandName").asText(null),
                medication.path("activeIngredient").asText(null),
                medication.path("dosage").asText(null),
                medication.path("frequency").asText(null));
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

        return writeJson(body);
    }

    private String buildDataUrl(byte[] imageBytes, String contentType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return "data:" + contentType + ";base64," + base64Image;
    }

    private String callOpenAi(String payload) {
        return RestClient.create()
                .post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(String.class);
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
