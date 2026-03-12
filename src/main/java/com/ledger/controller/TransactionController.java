package com.ledger.controller;

import com.ledger.dto.TransactionFormDto;
import com.ledger.entity.Transaction;
import com.ledger.repository.CategoryRepository;
import com.ledger.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Integer month,
                       Model model) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();

        model.addAttribute("transactions", transactionService.getMonthlyTransactions(y, m));
        model.addAttribute("year", y);
        model.addAttribute("month", m);
        model.addAttribute("monthlyTotal", transactionService.getMonthlyTotal(y, m));

        // 이전/다음 달 계산
        LocalDate current = LocalDate.of(y, m, 1);
        LocalDate prev = current.minusMonths(1);
        LocalDate next = current.plusMonths(1);
        model.addAttribute("prevYear", prev.getYear());
        model.addAttribute("prevMonth", prev.getMonthValue());
        model.addAttribute("nextYear", next.getYear());
        model.addAttribute("nextMonth", next.getMonthValue());

        return "transaction/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new TransactionFormDto());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("today", LocalDate.now().toString());
        model.addAttribute("isEdit", false);
        return "transaction/form";
    }

    @PostMapping
    public String save(@ModelAttribute TransactionFormDto form) {
        transactionService.save(form);
        return "redirect:/transactions";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Transaction t = transactionService.getById(id);
        TransactionFormDto form = TransactionFormDto.builder()
                .id(t.getId())
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .amount(t.getAmount())
                .merchant(t.getMerchant())
                .memo(t.getMemo())
                .txDate(t.getTxDate())
                .txTime(t.getTxTime() != null ? t.getTxTime().toString() : null)
                .source(t.getSource())
                .build();

        model.addAttribute("form", form);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("isEdit", true);
        return "transaction/form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute TransactionFormDto form) {
        transactionService.update(id, form);
        return "redirect:/transactions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        transactionService.delete(id);
        return "redirect:/transactions";
    }
}
