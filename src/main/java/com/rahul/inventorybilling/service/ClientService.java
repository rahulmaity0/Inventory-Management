package com.rahul.inventorybilling.service;

import com.rahul.inventorybilling.dto.ClientSummaryResponse;
import com.rahul.inventorybilling.dto.CreateClientRequest;
import com.rahul.inventorybilling.dto.TransactionResponse;
import com.rahul.inventorybilling.model.Client;
import com.rahul.inventorybilling.model.GoldTransaction;
import com.rahul.inventorybilling.repository.ClientRepository;
import com.rahul.inventorybilling.repository.GoldTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final GoldTransactionRepository goldTransactionRepository;

    public ClientService(ClientRepository clientRepository, GoldTransactionRepository goldTransactionRepository) {
        this.clientRepository = clientRepository;
        this.goldTransactionRepository = goldTransactionRepository;
    }

    public List<ClientSummaryResponse> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(this::toClientSummary)
                .toList();
    }

    public ClientSummaryResponse createClient(CreateClientRequest request) {
        BigDecimal openingBalance = request.openingPureBalance() == null
                ? BigDecimal.ZERO
                : request.openingPureBalance();

        Client client = new Client(request.name(), openingBalance);
        Client savedClient = clientRepository.save(client);
        return toClientSummary(savedClient);
    }

    public List<TransactionResponse> getLedgerForClient(Long clientId) {
        return goldTransactionRepository.findByClientIdOrderByTransactionDateDescIdDesc(clientId)
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    private ClientSummaryResponse toClientSummary(Client client) {
        return new ClientSummaryResponse(
                client.getId(),
                client.getName(),
                client.getCurrentPureBalance()
        );
    }

    private TransactionResponse toTransactionResponse(GoldTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionDate(),
                transaction.getTransactionType(),
                transaction.getGrossWeight(),
                transaction.getPurityPercent(),
                transaction.getMakingChargePercent(),
                transaction.getEffectivePurityPercent(),
                transaction.getPureGoldEquivalent(),
                transaction.getNotes()
        );
    }
}
