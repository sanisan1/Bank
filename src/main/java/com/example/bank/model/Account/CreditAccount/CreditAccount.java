package com.example.bank.model.Account.CreditAccount;


import com.example.bank.model.Account.Account;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "credit_accounts")
public class CreditAccount extends Account {


    private BigDecimal creditLimit;
    private BigDecimal interestRate;
    private BigDecimal minimumPaymentRate = BigDecimal.valueOf(5);
    private Integer gracePeriod = 0;
    private BigDecimal debt;
    private BigDecimal accruedInterest;
    private BigDecimal totalDebt;
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

    public BigDecimal getAccruedInterest() {
        return accruedInterest;
    }
    public void setAccruedInterest(BigDecimal accruedInterest) {
        this.accruedInterest = accruedInterest;
    }

    public BigDecimal getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(BigDecimal totalDebt) {
        this.totalDebt = totalDebt;
    }

    public void updateDebt() {
        BigDecimal calculatedDebt = creditLimit.subtract(getBalance());
        if (calculatedDebt.compareTo(BigDecimal.ZERO) < 0) {
            calculatedDebt = BigDecimal.ZERO;
        }
        this.debt = calculatedDebt;
        this.totalDebt = debt.add(accruedInterest);
    }
    public void accrueInterest() {
        if (debt.compareTo(BigDecimal.ZERO) <= 0) {
            return; // если нет долга, проценты не считаем
        }

        // месячная ставка = годовая / 12
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // проценты за месяц
        BigDecimal interest = debt.multiply(monthlyRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        this.accruedInterest = this.accruedInterest.add(interest);
        this.totalDebt = debt.add(accruedInterest);
    }


}
