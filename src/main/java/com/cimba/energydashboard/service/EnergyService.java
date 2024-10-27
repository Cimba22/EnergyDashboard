package com.cimba.energydashboard.service;

import com.cimba.energydashboard.entity.EnergyData;
import com.cimba.energydashboard.repository.EnergyDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class that provides business logic for managing energy data.
 */
@Service
public class EnergyService {

    @Autowired
    private EnergyDataRepository energyDataRepository;

    /**
     * Saves a new energy data record.
     *
     * @param energyData the energy data to save
     * @return the saved energy data record
     */
    public EnergyData save(EnergyData energyData) {
        return energyDataRepository.save(energyData);
    }

    /**
     * Retrieves all energy data records.
     *
     * @return a list of all energy data records
     */
    public List<EnergyData> list() {
        return energyDataRepository.findAll();
    }

    /**
     * Retrieves an energy data record by its ID.
     *
     * @param id the ID of the energy data record
     * @return the energy data record with the specified ID
     * @throws ResponseStatusException if the record is not found
     */
    public EnergyData get(int id) {
        Optional<EnergyData> energyData = energyDataRepository.findById(id);
        if (energyData.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Energy data not found"
            );
        }
        return energyData.get();
    }

    /**
     * Updates an existing energy data record by its ID.
     *
     * @param id the ID of the record to update
     * @param energyData the new energy data to set
     * @return the updated energy data record
     * @throws ResponseStatusException if the record is not found
     */
    public EnergyData update(Integer id, EnergyData energyData) {
        Optional<EnergyData> energyDataOptional = energyDataRepository.findById(id);
        if (energyDataOptional.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Energy data not found"
            );
        }
        EnergyData existingEnergyData = energyDataOptional.get();
        existingEnergyData.setStationName(energyData.getStationName());
        return energyDataRepository.save(existingEnergyData);
    }

    /**
     * Deletes an energy data record by its ID.
     *
     * @param id the ID of the record to delete
     */
    public void delete(Integer id) {
        energyDataRepository.deleteById(id);
    }

    /**
     * Retrieves energy data records within a specified date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of energy data records within the date range
     */
    public List<EnergyData> getDataBetweenDates(LocalDate startDate, LocalDate endDate) {
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<EnergyData> data = energyDataRepository.findByDateBetween(start, end);
        System.out.println("Fetched data: " + data);
        return data;
    }

    /**
     * Filters energy data based on multiple optional criteria.
     *
     * @param minEnergyConsumed the minimum energy consumed threshold
     * @param maxEnergyConsumed the maximum energy consumed threshold
     * @param startDate the start date of the filter range
     * @param endDate the end date of the filter range
     * @param stationName the name of the station to filter by
     * @return a list of energy data records that match the filter criteria
     */
    public List<EnergyData> filter(Integer minEnergyConsumed, Integer maxEnergyConsumed, Date startDate, Date endDate, String stationName) {
        return energyDataRepository.findAll().stream()
                .filter(record -> minEnergyConsumed == null || record.getEnergyConsumed() >= minEnergyConsumed)
                .filter(record -> maxEnergyConsumed == null || record.getEnergyConsumed() <= maxEnergyConsumed)
                .filter(record -> startDate == null || record.getDate().after(startDate))
                .filter(record -> endDate == null || record.getDate().before(endDate))
                .filter(record -> stationName == null || stationName.isEmpty() || record.getStationName().equalsIgnoreCase(stationName))
                .collect(Collectors.toList());
    }
}
