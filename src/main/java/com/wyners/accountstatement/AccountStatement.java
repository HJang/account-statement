package com.wyners.accountstatement;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
public class AccountStatement {
    // create fields: Date, Amount, Account Number, Transaction Type, Transaction Details, Category, Merchant Name
    public Date date;
    public String transactionType;
    public String transactionDetails;
    public BigDecimal debit;
    public BigDecimal credit;
    public String category;
    public String merchantName;

    public AccountStatement(Date date, String transactionType, String transactionDetails, BigDecimal debit, BigDecimal credit, String category, String merchantName) {
        this.date = date;
        this.transactionType = transactionType;
        this.transactionDetails = transactionDetails;
        this.debit = debit;
        this.credit = credit;
        this.category = category;
        this.merchantName = merchantName;
    }

    @Override
    public String toString() {
        return "Date: " + date + ", Transaction Type: " + transactionType + ", Transaction Details: " + transactionDetails + ", Debit: " + debit + ", Credit: " + credit + ", Category: " + category + ", Merchant Name: " + merchantName;
    }
}
