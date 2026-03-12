package com.ledger.controller;

import com.ledger.dto.ParseResultDto;
import com.ledger.dto.TransactionFormDto;
import com.ledger.repository.CategoryRepository;
import com.ledger.service.KakaoParserService;
import com.ledger.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/kakao")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoParserService kakaoParserService;
    private final TransactionService transactionService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String form(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("form", new TransactionFormDto());
        return "transaction/kakao";
    }

    @PostMapping("/parse")
    public String parse(@RequestParam String rawText, Model model) {
        ParseResultDto result = kakaoParserService.parse(rawText);
        model.addAttribute("result", result);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("rawText", rawText);

        TransactionFormDto form = new TransactionFormDto();
        if (result.isSuccess()) {
            form.setAmount(result.getAmount());
            form.setMerchant(result.getMerchant());
            form.setTxDate(result.getTxDate());
            form.setTxTime(result.getTxTime() != null ? result.getTxTime().toString() : null);
            form.setCategoryId(result.getSuggestedCategoryId());
            form.setSource("KAKAO");
        }
        model.addAttribute("form", form);

        return "transaction/kakao";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute TransactionFormDto form) {
        if (form.getSource() == null) form.setSource("KAKAO");
        transactionService.save(form);
        return "redirect:/transactions";
    }
}
