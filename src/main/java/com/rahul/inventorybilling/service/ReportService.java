package com.rahul.inventorybilling.service;

import com.rahul.inventorybilling.dto.MonthlyFlowResponse;
import com.rahul.inventorybilling.model.GoldTransaction;
import com.rahul.inventorybilling.model.TransactionType;
import com.rahul.inventorybilling.repository.GoldTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final GoldTransactionRepository goldTransactionRepository;

    public ReportService(GoldTransactionRepository goldTransactionRepository) {
        this.goldTransactionRepository = goldTransactionRepository;
    }

    public List<MonthlyFlowResponse> getMonthlyFlow() {
        Map<YearMonth, List<GoldTransaction>> grouped = goldTransactionRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(tx -> YearMonth.from(tx.getTransactionDate())));

        return grouped.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(entry -> {
                    BigDecimal issueTotal = entry.getValue().stream()
                            .filter(tx -> tx.getTransactionType() == TransactionType.ISSUE)
                            .map(GoldTransaction::getPureGoldEquivalent)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal receiptTotal = entry.getValue().stream()
                            .filter(tx -> tx.getTransactionType() == TransactionType.RECEIPT)
                            .map(GoldTransaction::getPureGoldEquivalent)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new MonthlyFlowResponse(
                            entry.getKey().toString(),
                            issueTotal,
                            receiptTotal
                    );
                })
                .toList();
    }
}
