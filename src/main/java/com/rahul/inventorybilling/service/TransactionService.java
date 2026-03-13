package com.rahul.inventorybilling.service;

import com.rahul.inventorybilling.dto.CreateTransactionRequest;
import com.rahul.inventorybilling.dto.CreateTransactionResponse;
import com.rahul.inventorybilling.model.Client;
import com.rahul.inventorybilling.model.GoldTransaction;
import com.rahul.inventorybilling.model.InventoryState;
import com.rahul.inventorybilling.model.TransactionType;
import com.rahul.inventorybilling.repository.ClientRepository;
import com.rahul.inventorybilling.repository.GoldTransactionRepository;
import com.rahul.inventorybilling.repository.InventoryStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TransactionService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final ClientRepository clientRepository;
    private final GoldTransactionRepository goldTransactionRepository;
    private final InventoryStateRepository inventoryStateRepository;

    public TransactionService(
            ClientRepository clientRepository,
            GoldTransactionRepository goldTransactionRepository,
            InventoryStateRepository inventoryStateRepository
    ) {
        this.clientRepository = clientRepository;
        this.goldTransactionRepository = goldTransactionRepository;
        this.inventoryStateRepository = inventoryStateRepository;
    }

    @Transactional
    public CreateTransactionResponse createTransaction(CreateTransactionRequest request) {
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        InventoryState inventoryState = inventoryStateRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Inventory state not found"));

        BigDecimal makingChargePercent = request.makingChargePercent() == null
                ? BigDecimal.ZERO
                : request.makingChargePercent();

        BigDecimal effectivePurityPercent = calculateEffectivePurity(request.transactionType(), request.purityPercent(), makingChargePercent);
        BigDecimal pureGoldEquivalent = calculatePureGold(request.grossWeight(), effectivePurityPercent);

        if (request.transactionType() == TransactionType.ISSUE) {
            ensureEnoughLockerGold(inventoryState.getLockerPureGold(), pureGoldEquivalent);
            inventoryState.setLockerPureGold(inventoryState.getLockerPureGold().subtract(pureGoldEquivalent));
            client.setCurrentPureBalance(client.getCurrentPureBalance().add(pureGoldEquivalent));
        } else {
            ensureClientHasEnoughBalance(client.getCurrentPureBalance(), pureGoldEquivalent);
            inventoryState.setLockerPureGold(inventoryState.getLockerPureGold().add(pureGoldEquivalent));
            client.setCurrentPureBalance(client.getCurrentPureBalance().subtract(pureGoldEquivalent));
        }

        GoldTransaction transaction = new GoldTransaction();
        transaction.setClient(client);
        transaction.setTransactionType(request.transactionType());
        transaction.setGrossWeight(scale3(request.grossWeight()));
        transaction.setPurityPercent(scale2(request.purityPercent()));
        transaction.setMakingChargePercent(scale2(makingChargePercent));
        transaction.setEffectivePurityPercent(scale2(effectivePurityPercent));
        transaction.setPureGoldEquivalent(pureGoldEquivalent);
        transaction.setTransactionDate(request.transactionDate());
        transaction.setNotes(request.notes());

        clientRepository.save(client);
        inventoryStateRepository.save(inventoryState);
        GoldTransaction savedTransaction = goldTransactionRepository.save(transaction);

        return new CreateTransactionResponse(
                savedTransaction.getId(),
                client.getId(),
                client.getName(),
                pureGoldEquivalent,
                client.getCurrentPureBalance(),
                inventoryState.getLockerPureGold()
        );
    }

    private BigDecimal calculateEffectivePurity(TransactionType transactionType, BigDecimal purityPercent, BigDecimal makingChargePercent) {
        if (transactionType == TransactionType.ISSUE) {
            return purityPercent.add(makingChargePercent);
        }
        return purityPercent;
    }

    private BigDecimal calculatePureGold(BigDecimal grossWeight, BigDecimal effectivePurityPercent) {
        return grossWeight
                .multiply(effectivePurityPercent)
                .divide(HUNDRED, 3, RoundingMode.HALF_UP);
    }

    private void ensureEnoughLockerGold(BigDecimal lockerPureGold, BigDecimal pureGoldEquivalent) {
        if (lockerPureGold.compareTo(pureGoldEquivalent) < 0) {
            throw new IllegalArgumentException("Not enough locker gold for this issue transaction");
        }
    }

    private void ensureClientHasEnoughBalance(BigDecimal clientBalance, BigDecimal pureGoldEquivalent) {
        if (clientBalance.compareTo(pureGoldEquivalent) < 0) {
            throw new IllegalArgumentException("Client balance is lower than the received pure gold");
        }
    }

    private BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scale3(BigDecimal value) {
        return value.setScale(3, RoundingMode.HALF_UP);
    }
}
