package com.medscan.app_med.controller;

import com.medscan.app_med.repository.UserRepo;
import com.medscan.app_med.security.JwtService;
import com.medscan.app_med.service.ScannedMedication;
import com.medscan.app_med.service.ScannerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScanController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ScanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScannerService scannerService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepo userRepo;

    @Test
    void scanProcessReturnsExtractedMedication() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "receta.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3});

        when(scannerService.scan(any(byte[].class), eq(MediaType.IMAGE_JPEG_VALUE)))
                .thenReturn(new ScannedMedication("Acetaminophen", "Paracetamol 500 mg",
                        "1 tableta cada 8 horas", "cada 8 horas"));

        mockMvc.perform(multipart("/api/v1/scan/process").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brandName").value("Acetaminophen"))
                .andExpect(jsonPath("$.activeIngredient").value("Paracetamol 500 mg"))
                .andExpect(jsonPath("$.dosage").value("1 tableta cada 8 horas"))
                .andExpect(jsonPath("$.frequency").value("cada 8 horas"));
    }

    @Test
    void scanProcessRejectsEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "receta.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/v1/scan/process").file(file))
                .andExpect(status().isBadRequest());
    }
}
