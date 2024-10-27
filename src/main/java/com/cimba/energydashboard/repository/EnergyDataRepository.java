package com.cimba.energydashboard.repository;

import com.cimba.energydashboard.entity.EnergyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface EnergyDataRepository extends JpaRepository<EnergyData, Integer> {
    List<EnergyData> findByDateBetween(Date date, Date date2);
}
