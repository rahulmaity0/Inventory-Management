package com.rahul.inventorybilling.repository;

import com.rahul.inventorybilling.model.InventoryState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryStateRepository extends JpaRepository<InventoryState, Long> {
}
