package com.rahul.inventorybilling.service;

import com.rahul.inventorybilling.dto.DashboardResponse;
import com.rahul.inventorybilling.model.Client;
import com.rahul.inventorybilling.model.InventoryState;
import com.rahul.inventorybilling.repository.ClientRepository;
import com.rahul.inventorybilling.repository.InventoryStateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** The summary numbers on the front page. */
@Service
public class DashboardService {

    private final ClientRepository clientRepository;
    private final InventoryStateRepository inventoryStateRepository;

    public DashboardService(ClientRepository clientRepository,
                            InventoryStateRepository inventoryStateRepository) {
        this.clientRepository = clientRepository;
        this.inventoryStateRepository = inventoryStateRepository;
    }

    public DashboardResponse getDashboard() {
        Optional<InventoryState> foundInventory = inventoryStateRepository.findById(1L);
        if (foundInventory.isEmpty()) {
            throw new IllegalStateException("Inventory state not found");
        }
        InventoryState inventoryState = foundInventory.get();

        BigDecimal lockerPureGold = inventoryState.getLockerPureGold();

        // Add up what every client owes.
        BigDecimal totalClientPureGold = BigDecimal.ZERO;
        List<Client> clients = clientRepository.findAll();
        for (Client client : clients) {
            // BigDecimal cannot be changed in place - add() returns a new
            // value, so the result has to be assigned back.
            totalClientPureGold = totalClientPureGold.add(client.getCurrentPureBalance());
        }

        // Gold in the locker plus gold out with clients is everything the
        // business is holding. If a transaction ever broke, this total would
        // be the number that moved when it should not have.
        BigDecimal totalBusinessPureGold = lockerPureGold.add(totalClientPureGold);

        return new DashboardResponse(
                lockerPureGold,
                totalClientPureGold,
                totalBusinessPureGold,
                clientRepository.count());
    }
}
