package com.ledger.controller;

import com.ledger.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TransactionService transactionService;

    @GetMapping("/")
    public String index(Model model) {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("monthlyTotal", transactionService.getMonthlyTotal(year, month));
        model.addAttribute("categoryStats", transactionService.getMonthlyCategoryStats(year, month));
        model.addAttribute("recentTransactions", transactionService.getRecentTransactions());

        return "index";
    }
}
