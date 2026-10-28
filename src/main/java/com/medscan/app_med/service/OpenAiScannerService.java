package com.medscan.app_med.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiScannerService implements ScannerService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiScannerService.class);

    private static final String EXTRACTION_PROMPT =
            "Extrae del prospecto o receta los datos del medicamento y responde solo con JSON con los campos: "
                    + "brandName, activeIngredient, dosage, frequency.";

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final String url;
    private final String geminiApiKey;
    private final String geminiModel;
    private final String geminiUrl;
    private final String ocrApiKey;
    private final String ocrUrl;

    @Autowired
    public OpenAiScannerService(ObjectMapper objectMapper,
                                RestClient.Builder restClientBuilder,
                                @Value("${app.scan.openai.api-key:}") String apiKey,
                                @Value("${app.scan.openai.model:gpt-4o-mini}") String model,
                                @Value("${app.scan.openai.url:https://api.openai.com/v1/chat/completions}") String url,
                                @Value("${app.scan.gemini.api-key:}") String geminiApiKey,
                                @Value("${app.scan.gemini.model:gemini-2.0-flash}") String geminiModel,
                                @Value("${app.scan.gemini.url:https://generativelanguage.googleapis.com/v1beta/models}") String geminiUrl,
                                @Value("${app.scan.ocr.api-key:K88856237488957}") String ocrApiKey,
                                @Value("${app.scan.ocr.url:https://api.ocr.space/parse/image}") String ocrUrl,
                                @Value("${app.scan.openai.timeout-ms:15000}") int timeoutMs) {

        this.objectMapper = objectMapper;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model;
        this.url = url;
        this.geminiApiKey = geminiApiKey != null ? geminiApiKey.trim() : "";
        this.geminiModel = geminiModel != null && !geminiModel.isBlank() ? geminiModel : "gemini-2.0-flash";
        this.geminiUrl = geminiUrl;
        this.ocrApiKey = ocrApiKey != null && !ocrApiKey.isBlank() ? ocrApiKey : "K88856237488957";
        this.ocrUrl = ocrUrl != null && !ocrUrl.isBlank() ? ocrUrl : "https://api.ocr.space/parse/image";

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
        this(objectMapper, RestClient.builder(), apiKey, model, url, "", "gemini-2.0-flash", "", "", "", 15000);
    }

    @Override
    public ScannedMedication scan(byte[] imageBytes, String contentType) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ScanProcessingException("La imagen recibida está vacía");
        }

        String effectiveContentType = (contentType != null && !contentType.isBlank()) ? contentType : "image/jpeg";

        // 1. Intentar con OpenAI Vision si la clave está configurada
        if (!apiKey.isBlank()) {
            try {
                String payload = buildOpenAiRequest(imageBytes, effectiveContentType);
                String response = callOpenAi(payload);
                return parseResponse(response);
            } catch (Exception ex) {
                log.warn("Error en el escaneo con OpenAI ({}), intentando fallback con IA/OCR...", ex.getMessage());
            }
        }

        // 2. Intentar con Google Gemini Vision si la clave está configurada
        if (!geminiApiKey.isBlank()) {
            try {
                return callGemini(imageBytes, effectiveContentType);
            } catch (Exception ex) {
                log.warn("Error en el escaneo con Gemini ({}), intentando fallback con OCR Space...", ex.getMessage());
            }
        }

        // 3. Fallback con OCR Space + Parser Farmacéutico Inteligente
        if (!ocrApiKey.isBlank() && !ocrApiKey.equals("disabled")) {
            try {
                return callOcrSpace(imageBytes, effectiveContentType);
            } catch (Exception ex) {
                log.warn("Error en el escaneo con OCR Space ({})", ex.getMessage());
            }
        }

        // Si ninguna API estaba configurada o todas fallaron
        if (apiKey.isBlank() && geminiApiKey.isBlank()) {
            throw new ScanProcessingException("La API key de escaneo no está configurada");
        }

        throw new ScanProcessingException("No se pudo procesar la imagen del medicamento con los servicios disponibles");
    }

    private ScannedMedication callGemini(byte[] imageBytes, String contentType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String endpoint = String.format("%s/%s:generateContent?key=%s",
                geminiUrl.replaceAll("/+$", ""), geminiModel, geminiApiKey);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", EXTRACTION_PROMPT),
                                Map.of("inline_data", Map.of(
                                        "mime_type", contentType,
                                        "data", base64Image
                                ))
                        )
                )),
                "generationConfig", Map.of("response_mime_type", "application/json")
        );

        String jsonPayload = writeJson(body);
        String response = restClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonPayload)
                .retrieve()
                .body(String.class);

        JsonNode root = readTree(response);
        JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        String content = textNode.asText(null);
        if (content == null || content.isBlank()) {
            throw new ScanProcessingException("Gemini no devolvió contenido extraído");
        }
        return parseMedicationContent(content);
    }

    private ScannedMedication callOcrSpace(byte[] imageBytes, String contentType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String dataUrl = "data:" + contentType + ";base64," + base64Image;

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("apikey", ocrApiKey);
        formData.add("base64Image", dataUrl);
        formData.add("language", "spa");
        formData.add("isOverlayRequired", "false");
        formData.add("scale", "true");
        formData.add("OCREngine", "2");

        String response = restClient.post()
                .uri(ocrUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(String.class);

        JsonNode root = readTree(response);
        JsonNode parsedResults = root.path("ParsedResults");
        if (!parsedResults.isArray() || parsedResults.isEmpty()) {
            throw new ScanProcessingException("OCR Space no reconoció texto en la imagen");
        }

        String fullText = parsedResults.get(0).path("ParsedText").asText("");
        log.info("Texto extraído por OCR Space: {}", fullText.replace("\n", " ").trim());

        return MedicationTextExtractor.extract(fullText);
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

    private String buildOpenAiRequest(byte[] imageBytes, String contentType) {
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

    private String writeJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new ScanProcessingException("Error serializando petición para el servicio de escaneo", e);
        }
    }
}
