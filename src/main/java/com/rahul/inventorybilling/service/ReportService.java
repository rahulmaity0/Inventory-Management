package com.rahul.inventorybilling.service;

import com.rahul.inventorybilling.dto.MonthlyFlowResponse;
import com.rahul.inventorybilling.model.GoldTransaction;
import com.rahul.inventorybilling.model.TransactionType;
import com.rahul.inventorybilling.repository.GoldTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * How much gold went out and came back, month by month.
 *
 * Two passes: first sort every transaction into its month, then add up each
 * month's two totals.
 */
@Service
public class ReportService {

    private final GoldTransactionRepository goldTransactionRepository;

    public ReportService(GoldTransactionRepository goldTransactionRepository) {
        this.goldTransactionRepository = goldTransactionRepository;
    }

    public List<MonthlyFlowResponse> getMonthlyFlow() {
        List<GoldTransaction> allTransactions = goldTransactionRepository.findAll();

        // A TreeMap keeps its keys sorted, so the months come out oldest
        // first without any extra sorting step. A plain HashMap would give
        // them back in an unpredictable order.
        Map<YearMonth, List<GoldTransaction>> transactionsByMonth = new TreeMap<>();

        // Pass one: put each transaction in its month's bucket.
        for (GoldTransaction transaction : allTransactions) {
            YearMonth month = YearMonth.from(transaction.getTransactionDate());

            List<GoldTransaction> bucket = transactionsByMonth.get(month);
            if (bucket == null) {
                bucket = new ArrayList<>();
                transactionsByMonth.put(month, bucket);
            }
            bucket.add(transaction);
        }

        // Pass two: add up each bucket.
        List<MonthlyFlowResponse> result = new ArrayList<>();
        for (Map.Entry<YearMonth, List<GoldTransaction>> entry : transactionsByMonth.entrySet()) {
            YearMonth month = entry.getKey();
            List<GoldTransaction> transactionsThisMonth = entry.getValue();

            BigDecimal issueTotal = BigDecimal.ZERO;
            BigDecimal receiptTotal = BigDecimal.ZERO;

            for (GoldTransaction transaction : transactionsThisMonth) {
                if (transaction.getTransactionType() == TransactionType.ISSUE) {
                    issueTotal = issueTotal.add(transaction.getPureGoldEquivalent());
                } else {
                    receiptTotal = receiptTotal.add(transaction.getPureGoldEquivalent());
                }
            }

            result.add(new MonthlyFlowResponse(month.toString(), issueTotal, receiptTotal));
        }

        return result;
    }
}
