package com.tradevision.alertservice.repository;

import com.tradevision.alertservice.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUserId(Long userId);

    List<Alert> findByUserIdAndActive(Long userId, boolean active);

    List<Alert> findBySymbolAndActive(String symbol, boolean active);

    Optional<Alert> findByIdAndUserId(Long id, Long userId);
}
