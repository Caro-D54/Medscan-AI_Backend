package com.medscan.app_med.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAiScannerServiceTest {

    private final OpenAiScannerService service =
            new OpenAiScannerService(new ObjectMapper(), "test-key", "gpt-4o-mini",
                    "https://api.openai.com/v1/chat/completions");

    @Test
    void parseResponseExtractsMedicationFields() {
        String response = """
                {"choices":[{"message":{"role":"assistant","content":"{\\"brandName\\":\\"Acetaminophen\\",\\"activeIngredient\\":\\"Paracetamol 500 mg\\",\\"dosage\\":\\"1 tableta cada 8 horas\\",\\"frequency\\":\\"cada 8 horas\\"}"}}]}
                """;

        ScannedMedication result = service.parseResponse(response);

        assertThat(result.brandName()).isEqualTo("Acetaminophen");
        assertThat(result.activeIngredient()).isEqualTo("Paracetamol 500 mg");
        assertThat(result.dosage()).isEqualTo("1 tableta cada 8 horas");
        assertThat(result.frequency()).isEqualTo("cada 8 horas");
    }

    @Test
    void parseResponseThrowsWhenNoChoices() {
        assertThatThrownBy(() -> service.parseResponse("{\"choices\":[]}"))
                .isInstanceOf(ScanProcessingException.class);
    }

    @Test
    void parseResponseThrowsWhenContentIsMissing() {
        assertThatThrownBy(() -> service.parseResponse("{\"choices\":[{\"message\":{}}]}"))
                .isInstanceOf(ScanProcessingException.class);
    }

    @Test
    void scanThrowsWithoutApiKey() {
        OpenAiScannerService noKey = new OpenAiScannerService(new ObjectMapper(), "",
                "gpt-4o-mini", "https://api.openai.com/v1/chat/completions");

        assertThatThrownBy(() -> noKey.scan(new byte[]{1, 2}, "image/jpeg"))
                .isInstanceOf(ScanProcessingException.class);
    }

    @Test
    void scanThrowsOnEmptyImage() {
        assertThatThrownBy(() -> service.scan(new byte[0], "image/jpeg"))
                .isInstanceOf(ScanProcessingException.class);
    }
}
