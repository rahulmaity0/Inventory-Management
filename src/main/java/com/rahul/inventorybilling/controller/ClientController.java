package com.rahul.inventorybilling.controller;

import com.rahul.inventorybilling.dto.ClientSummaryResponse;
import com.rahul.inventorybilling.dto.CreateClientRequest;
import com.rahul.inventorybilling.dto.TransactionResponse;
import com.rahul.inventorybilling.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientSummaryResponse> getAllClients() {
        return clientService.getAllClients();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientSummaryResponse createClient(@Valid @RequestBody CreateClientRequest request) {
        return clientService.createClient(request);
    }

    @GetMapping("/{clientId}/ledger")
    public List<TransactionResponse> getClientLedger(@PathVariable Long clientId) {
        return clientService.getLedgerForClient(clientId);
    }
}
