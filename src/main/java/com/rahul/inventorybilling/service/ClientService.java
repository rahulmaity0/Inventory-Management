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
import java.util.ArrayList;
import java.util.List;

/** Creating clients, listing them, and reading one client's ledger. */
@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final GoldTransactionRepository goldTransactionRepository;

    public ClientService(ClientRepository clientRepository,
                         GoldTransactionRepository goldTransactionRepository) {
        this.clientRepository = clientRepository;
        this.goldTransactionRepository = goldTransactionRepository;
    }

    public List<ClientSummaryResponse> getAllClients() {
        List<Client> clients = clientRepository.findAll();

        // Entities are never returned straight to the caller. We copy the
        // fields we want into a response object, so the JSON is not tied to
        // the database table.
        List<ClientSummaryResponse> result = new ArrayList<>();
        for (Client client : clients) {
            result.add(toClientSummary(client));
        }
        return result;
    }

    public ClientSummaryResponse createClient(CreateClientRequest request) {
        BigDecimal openingBalance = request.getOpeningPureBalance();
        if (openingBalance == null) {
            openingBalance = BigDecimal.ZERO;
        }

        Client client = new Client(request.getName(), openingBalance);
        Client savedClient = clientRepository.save(client);
        return toClientSummary(savedClient);
    }

    public List<TransactionResponse> getLedgerForClient(Long clientId) {
        // Spring Data builds this query from the method name: find by client
        // id, newest date first, and newest id first when two share a date.
        List<GoldTransaction> transactions =
                goldTransactionRepository.findByClientIdOrderByTransactionDateDescIdDesc(clientId);

        List<TransactionResponse> result = new ArrayList<>();
        for (GoldTransaction transaction : transactions) {
            result.add(toTransactionResponse(transaction));
        }
        return result;
    }

    private ClientSummaryResponse toClientSummary(Client client) {
        return new ClientSummaryResponse(
                client.getId(),
                client.getName(),
                client.getCurrentPureBalance());
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
                transaction.getItemType(),
                transaction.getNotes());
    }
}
