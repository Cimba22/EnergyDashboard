package com.cimba.energydashboard.controller;

import com.cimba.energydashboard.entity.EnergyData;
import com.cimba.energydashboard.service.EnergyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(EnergyController.class)
class EnergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnergyService energyService;

    private ObjectMapper objectMapper;
    private EnergyData sampleData;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Use Calendar to get the current date
        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime(); // Get the current date

        // Create the sample data with Date type
        sampleData = new EnergyData(1, "Station A", currentDate, 100.0, 90.0);
    }

    @Test
    void getAllEnergyData() throws Exception {
        Mockito.when(energyService.list()).thenReturn(List.of(sampleData));

        mockMvc.perform(MockMvcRequestBuilders.get("/energy/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stationName").value(sampleData.getStationName()));
    }

    @Test
    void saveEnergyData() throws Exception {
        Mockito.when(energyService.save(ArgumentMatchers.any(EnergyData.class))).thenReturn(sampleData);

        mockMvc.perform(MockMvcRequestBuilders.post("/energy/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleData)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.stationName").value(sampleData.getStationName()));
    }

    @Test
    void getEnergyData() throws Exception {
        Mockito.when(energyService.get(ArgumentMatchers.anyInt())).thenReturn(sampleData);

        mockMvc.perform(MockMvcRequestBuilders.get("/energy/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.stationName").value(sampleData.getStationName()));
    }

    @Test
    void updateEnergyData() throws Exception {
        Mockito.when(energyService.update(ArgumentMatchers.anyInt(), ArgumentMatchers.any(EnergyData.class))).thenReturn(sampleData);

        mockMvc.perform(MockMvcRequestBuilders.put("/energy/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleData)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.stationName").value(sampleData.getStationName()));
    }

    @Test
    void deleteEnergyData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/energy/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(energyService).delete(1);
    }

    @Test
    void downloadJson() throws Exception {
        Mockito.when(energyService.list()).thenReturn(List.of(sampleData));

        mockMvc.perform(MockMvcRequestBuilders.get("/energy/download/json"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Disposition", "attachment; filename=\"energy_data.json\""))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void uploadJsonFile() throws Exception {
        String jsonData = objectMapper.writeValueAsString(new EnergyData[]{sampleData});
        MockMultipartFile file = new MockMultipartFile("file", "data.json", "application/json", jsonData.getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/energy/upload").file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Data uploaded successfully."));
    }

    @Test
    void compareData() throws Exception {
        Mockito.when(energyService.getDataBetweenDates(ArgumentMatchers.any(LocalDate.class), ArgumentMatchers.any(LocalDate.class)))
                .thenReturn(List.of(sampleData));

        mockMvc.perform(MockMvcRequestBuilders.get("/energy/compare")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-12-31"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stationName").value(sampleData.getStationName()));
    }

    @Test
    void filterData() throws Exception {
        Mockito.when(energyService.filter(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(List.of(sampleData));

        mockMvc.perform(MockMvcRequestBuilders.get("/energy/filter")
                        .param("minEnergyConsumed", "50")
                        .param("maxEnergyConsumed", "150")
                        .param("stationName", "Station A"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].stationName").value(sampleData.getStationName()));
    }
}
