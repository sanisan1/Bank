package com.example.bank.model.Account.CreditAccount;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreditAccountResponseDto {

    private String accountNumber;
    private BigDecimal balance;
    private BigDecimal creditLimit;
    private BigDecimal interestRate;
    private BigDecimal minimumPaymentRate;
    private Integer gracePeriod;
    private BigDecimal totalDebt; // показываем только totalDebt
    private LocalDate paymentDueDate;

    public CreditAccountResponseDto() {
    }

    public CreditAccountResponseDto(String accountNumber, BigDecimal balance, BigDecimal creditLimit,
                                    BigDecimal interestRate, BigDecimal minimumPaymentRate, Integer gracePeriod,
                                    BigDecimal totalDebt, LocalDate paymentDueDate) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.creditLimit = creditLimit;
        this.interestRate = interestRate;
        this.minimumPaymentRate = minimumPaymentRate;
        this.gracePeriod = gracePeriod;
        this.totalDebt = totalDebt;
        this.paymentDueDate = paymentDueDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getMinimumPaymentRate() {
        return minimumPaymentRate;
    }

    public void setMinimumPaymentRate(BigDecimal minimumPaymentRate) {
        this.minimumPaymentRate = minimumPaymentRate;
    }

    public Integer getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(Integer gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public BigDecimal getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(BigDecimal totalDebt) {
        this.totalDebt = totalDebt;
    }

    public LocalDate getPaymentDueDate() {
        return paymentDueDate;
    }

    public void setPaymentDueDate(LocalDate paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }
}
