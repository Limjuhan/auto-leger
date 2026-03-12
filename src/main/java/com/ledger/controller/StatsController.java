package com.ledger.controller;

import com.ledger.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class StatsController {

    private final TransactionService transactionService;

    @GetMapping("/stats")
    public String stats(Model model) {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        model.addAttribute("categoryStats", transactionService.getMonthlyCategoryStats(year, month));
        model.addAttribute("monthlyTotals", transactionService.getMonthlyTotals());
        model.addAttribute("year", year);
        model.addAttribute("month", month);

        return "stats/monthly";
    }
}
