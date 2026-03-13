package com.rahul.inventorybilling.service;

import com.rahul.inventorybilling.dto.DashboardResponse;
import com.rahul.inventorybilling.model.InventoryState;
import com.rahul.inventorybilling.repository.ClientRepository;
import com.rahul.inventorybilling.repository.InventoryStateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {

    private final ClientRepository clientRepository;
    private final InventoryStateRepository inventoryStateRepository;

    public DashboardService(ClientRepository clientRepository, InventoryStateRepository inventoryStateRepository) {
        this.clientRepository = clientRepository;
        this.inventoryStateRepository = inventoryStateRepository;
    }

    public DashboardResponse getDashboard() {
        InventoryState inventoryState = inventoryStateRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Inventory state not found"));

        BigDecimal lockerPureGold = inventoryState.getLockerPureGold();
        BigDecimal totalClientPureGold = clientRepository.findAll()
                .stream()
                .map(client -> client.getCurrentPureBalance())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBusinessPureGold = lockerPureGold.add(totalClientPureGold);

        return new DashboardResponse(
                lockerPureGold,
                totalClientPureGold,
                totalBusinessPureGold,
                clientRepository.count()
        );
    }
}
