package com.example.bank.model.Account;


import com.example.bank.model.AccountType;
import com.example.bank.model.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 10, unique = true, nullable = false)
    private String accountNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @NotNull
    private User user;

    private BigDecimal balance = BigDecimal.ZERO;
    protected Boolean blocked = false;


    @Enumerated(EnumType.STRING)
    private AccountType accountType;


}
