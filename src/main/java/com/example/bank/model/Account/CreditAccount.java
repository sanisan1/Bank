package com.example.bank.model.Account;


import com.example.bank.model.Account.DebitAccount.Account;
import com.example.bank.model.Account.DebitAccount.DebitAccount;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@SecondaryTable(name = "credit_accounts", pkJoinColumns = @PrimaryKeyJoinColumn(name = "account_id"))
public class CreditAccount extends Account {

    @Column(table = "credit_accounts")
    private BigDecimal creditLimit;
    @Column(table = "credit_accounts")
    private BigDecimal interestRate;
    @Column(table = "credit_accounts")
    private BigDecimal minimumPaymentRate = BigDecimal.valueOf(5);
    @Column(table = "credit_accounts")
    private Integer gracePeriod = 0;
    @Column(table = "credit_accounts")
    private BigDecimal debt;
    @Column(table = "credit_accounts")
    private LocalDate paymentDueDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);





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

    public BigDecimal getDebt() {
        return debt;
    }

    public void setDebt(BigDecimal debt) {
        this.debt = debt;
    }

    public LocalDate getPaymentDueDate() {
        return paymentDueDate;
    }

    public void setPaymentDueDate(LocalDate paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }


    public BigDecimal getCreditLimit() {
        return creditLimit;
    }




}
