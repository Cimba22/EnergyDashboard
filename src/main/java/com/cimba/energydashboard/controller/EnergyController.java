package com.cimba.energydashboard.controller;

import com.cimba.energydashboard.entity.EnergyData;
import com.cimba.energydashboard.service.EnergyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * REST controller for managing energy consumption data.
 */
@Slf4j
@RestController
@RequestMapping("energy")
public class EnergyController {

    @Autowired
    EnergyService energyService;

    /**
     * Retrieves all energy consumption data.
     *
     * @return a list of EnergyData objects
     */
    @GetMapping(value = "/", produces = "application/json")
    public List<EnergyData> getAllEnergyData() {
        return energyService.list();
    }

    /**
     * Saves a new energy consumption data entry.
     *
     * @param energyData the energy data to be saved
     * @return the saved EnergyData object
     */
    @PostMapping(value = "/new", consumes = "application/json", produces = "application/json")
    public EnergyData save(@RequestBody EnergyData energyData) {
        return energyService.save(energyData);
    }

    /**
     * Retrieves an energy data entry by its ID.
     *
     * @param id the unique identifier of the EnergyData
     * @return the EnergyData object
     */
    @GetMapping(value = "/{id}", produces = "application/json")
    public EnergyData getEnergyData(@PathVariable int id) {
        return energyService.get(id);
    }

    /**
     * Updates an energy data entry by its ID.
     *
     * @param id the unique identifier of the EnergyData
     * @param energyData the updated energy data
     * @return the updated EnergyData object
     */
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public EnergyData update(@PathVariable Integer id, @RequestBody EnergyData energyData) {
        return energyService.update(id, energyData);
    }

    /**
     * Deletes an energy data entry by its ID.
     *
     * @param id the unique identifier of the EnergyData
     */
    @DeleteMapping(value = "/{id}", produces = "application/json")
    public void delete(@PathVariable Integer id) {
        energyService.delete(id);
    }

    /**
     * Downloads all energy data as a JSON file.
     *
     * @return a ResponseEntity containing the energy data list as JSON
     */
    @GetMapping(value = "/download/json", produces = "application/json")
    public ResponseEntity<List<EnergyData>> downloadJson() {
        List<EnergyData> energyDataList = energyService.list();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"energy_data.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(energyDataList);
    }

    /**
     * Uploads a JSON file containing energy data entries.
     *
     * @param file the JSON file to be uploaded
     * @return a ResponseEntity indicating success or failure
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadJsonFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !Objects.equals(file.getContentType(), "application/json")) {
            return ResponseEntity.badRequest().body("Please upload a valid JSON file.");
        }

        try {
            List<EnergyData> energyDataList = parseJsonFile(file);
            energyDataList.forEach(energyService::save);
            return ResponseEntity.ok("Data uploaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        }
    }

    /**
     * Parses a JSON file into a list of EnergyData objects.
     *
     * @param file the JSON file to parse
     * @return a list of EnergyData objects
     * @throws IOException if file processing fails
     */
    private List<EnergyData> parseJsonFile(MultipartFile file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return Arrays.asList(objectMapper.readValue(file.getInputStream(), EnergyData[].class));
    }

    /**
     * Compares energy data between specified start and end dates.
     *
     * @param startDate the start date for comparison
     * @param endDate the end date for comparison
     * @return a list of EnergyData objects within the date range
     */
    @GetMapping(value = "/compare", produces = "application/json")
    public List<EnergyData> compareData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return energyService.getDataBetweenDates(startDate, endDate);
    }

    /**
     * Filters energy data based on provided criteria.
     *
     * @param minEnergyConsumed minimum energy consumption for filtering
     * @param maxEnergyConsumed maximum energy consumption for filtering
     * @param startDate start date for filtering
     * @param endDate end date for filtering
     * @param stationName name of the station for filtering
     * @return a list of filtered EnergyData objects
     */
    @GetMapping("/filter")
    public List<EnergyData> filterData(
            @RequestParam(required = false) Integer minEnergyConsumed,
            @RequestParam(required = false) Integer maxEnergyConsumed,
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate,
            @RequestParam(required = false) String stationName) {
        return energyService.filter(minEnergyConsumed, maxEnergyConsumed, startDate, endDate, stationName);
    }
}
