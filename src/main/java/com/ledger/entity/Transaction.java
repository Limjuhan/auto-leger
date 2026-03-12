package com.ledger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "ledger_transaction")   // 'transaction'은 SQL 예약어라 변경
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false, length = 100)
    private String merchant;

    @Column(length = 200)
    private String memo;

    @Column(nullable = false)
    private LocalDate txDate;

    private LocalTime txTime;

    @Column(length = 10)
    private String source;  // "KAKAO" | "MANUAL"

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (source == null) source = "MANUAL";
    }
}
