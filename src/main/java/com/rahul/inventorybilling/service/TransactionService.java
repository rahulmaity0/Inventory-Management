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
import java.util.Optional;

/**
 * The only place gold moves.
 *
 * Two things must stay true no matter what happens here:
 *   1. The locker can never go negative - we cannot issue gold we do not have.
 *   2. A client's balance can never go negative - they cannot return more
 *      than they owe.
 *
 * Both are checked before anything is written, and the whole method runs
 * inside one database transaction, so either every change lands or none does.
 */
@Service
public class TransactionService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final ClientRepository clientRepository;
    private final GoldTransactionRepository goldTransactionRepository;
    private final InventoryStateRepository inventoryStateRepository;

    public TransactionService(ClientRepository clientRepository,
                              GoldTransactionRepository goldTransactionRepository,
                              InventoryStateRepository inventoryStateRepository) {
        this.clientRepository = clientRepository;
        this.goldTransactionRepository = goldTransactionRepository;
        this.inventoryStateRepository = inventoryStateRepository;
    }

    /**
     * @Transactional is what makes this safe. This method changes three rows -
     * the client, the locker and the new ledger entry. If the third write
     * failed and the first two had already been committed, the locker and the
     * client balance would disagree forever. With @Transactional, a failure
     * anywhere rolls all of it back.
     */
    @Transactional
    public CreateTransactionResponse createTransaction(CreateTransactionRequest request) {

        // Look up the client. findById gives an Optional, which is Java's way
        // of saying "there may or may not be a row here".
        Optional<Client> foundClient = clientRepository.findById(request.getClientId());
        if (foundClient.isEmpty()) {
            throw new IllegalArgumentException("Client not found");
        }
        Client client = foundClient.get();

        // There is only ever one locker row, and its id is 1.
        Optional<InventoryState> foundInventory = inventoryStateRepository.findById(1L);
        if (foundInventory.isEmpty()) {
            throw new IllegalStateException("Inventory state not found");
        }
        InventoryState inventoryState = foundInventory.get();

        // Making charge is optional in the request. Missing means zero.
        BigDecimal makingChargePercent = request.getMakingChargePercent();
        if (makingChargePercent == null) {
            makingChargePercent = BigDecimal.ZERO;
        }

        BigDecimal effectivePurityPercent = calculateEffectivePurity(
                request.getTransactionType(),
                request.getPurityPercent(),
                makingChargePercent);

        BigDecimal pureGoldEquivalent = calculatePureGold(
                request.getGrossWeight(),
                effectivePurityPercent);

        if (request.getTransactionType() == TransactionType.ISSUE) {
            // Gold is going out to the client.
            //
            // Two different numbers matter here, and this is the heart of the
            // whole project:
            //   - actualPureGold is the metal that physically leaves the
            //     locker, at its real purity.
            //   - pureGoldEquivalent is what the client is billed, at purity
            //     plus the making charge.
            // The client owes more than left the locker. That difference is
            // the business's margin.
            BigDecimal actualPureGold = calculatePureGold(
                    request.getGrossWeight(),
                    request.getPurityPercent());

            ensureEnoughLockerGold(inventoryState.getLockerPureGold(), actualPureGold);

            BigDecimal newLockerGold = inventoryState.getLockerPureGold().subtract(actualPureGold);
            inventoryState.setLockerPureGold(newLockerGold);

            BigDecimal newClientBalance = client.getCurrentPureBalance().add(pureGoldEquivalent);
            client.setCurrentPureBalance(newClientBalance);

        } else {
            // RECEIPT - gold is coming back from the client. No making charge
            // applies, so the locker gains exactly what the client is credited.
            ensureClientHasEnoughBalance(client.getCurrentPureBalance(), pureGoldEquivalent);

            BigDecimal newLockerGold = inventoryState.getLockerPureGold().add(pureGoldEquivalent);
            inventoryState.setLockerPureGold(newLockerGold);

            BigDecimal newClientBalance = client.getCurrentPureBalance().subtract(pureGoldEquivalent);
            client.setCurrentPureBalance(newClientBalance);
        }

        // Record what happened, so the ledger can be read back later.
        GoldTransaction transaction = new GoldTransaction();
        transaction.setClient(client);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setGrossWeight(scale3(request.getGrossWeight()));
        transaction.setPurityPercent(scale2(request.getPurityPercent()));
        transaction.setMakingChargePercent(scale2(makingChargePercent));
        transaction.setEffectivePurityPercent(scale2(effectivePurityPercent));
        transaction.setPureGoldEquivalent(pureGoldEquivalent);
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());

        clientRepository.save(client);
        inventoryStateRepository.save(inventoryState);
        GoldTransaction savedTransaction = goldTransactionRepository.save(transaction);

        return new CreateTransactionResponse(
                savedTransaction.getId(),
                client.getId(),
                client.getName(),
                pureGoldEquivalent,
                client.getCurrentPureBalance(),
                inventoryState.getLockerPureGold());
    }

    /**
     * On an issue the client is billed at purity plus making charge.
     * On a receipt there is no making charge, so the purity is used as-is.
     */
    private BigDecimal calculateEffectivePurity(TransactionType transactionType,
                                                BigDecimal purityPercent,
                                                BigDecimal makingChargePercent) {
        if (transactionType == TransactionType.ISSUE) {
            return purityPercent.add(makingChargePercent);
        }
        return purityPercent;
    }

    /**
     * grossWeight x purity% / 100, kept to three decimal places.
     *
     * BigDecimal is used rather than double because double cannot represent
     * decimal fractions exactly. With double, adding 0.1 ten times does not
     * give 1.0 - and here that error would be missing gold.
     *
     * divide() must be told how many decimals to keep and how to round, or it
     * throws when the division does not terminate. HALF_UP is ordinary
     * commercial rounding: 0.5 rounds away from zero.
     */
    private BigDecimal calculatePureGold(BigDecimal grossWeight, BigDecimal effectivePurityPercent) {
        BigDecimal total = grossWeight.multiply(effectivePurityPercent);
        return total.divide(HUNDRED, 3, RoundingMode.HALF_UP);
    }

    /** Rule 1: never issue more gold than the locker holds. */
    private void ensureEnoughLockerGold(BigDecimal lockerPureGold, BigDecimal pureGoldNeeded) {
        // compareTo returns a negative number when the first value is smaller.
        // Use compareTo, not equals - BigDecimal.equals("1.0", "1.00") is false
        // because it compares scale as well as value.
        if (lockerPureGold.compareTo(pureGoldNeeded) < 0) {
            throw new IllegalArgumentException("Not enough locker gold for this issue transaction");
        }
    }

    /** Rule 2: a client cannot return more gold than they owe. */
    private void ensureClientHasEnoughBalance(BigDecimal clientBalance, BigDecimal pureGoldReceived) {
        if (clientBalance.compareTo(pureGoldReceived) < 0) {
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
