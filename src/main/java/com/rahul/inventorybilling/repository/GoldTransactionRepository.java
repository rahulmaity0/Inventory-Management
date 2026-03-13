package com.rahul.inventorybilling.repository;

import com.rahul.inventorybilling.model.GoldTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoldTransactionRepository extends JpaRepository<GoldTransaction, Long> {
    List<GoldTransaction> findByClientIdOrderByTransactionDateDescIdDesc(Long clientId);
}
