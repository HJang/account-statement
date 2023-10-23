package com.wyners.accountstatement;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class AccountStatementService {
    private final String defaultCategory = "TBD";

    String[] HEADERS_ORIGINAL = {"Date", "Amount", "Account Number", "Empty", "Transaction Type",
            "Transaction Details", "Balance", "Category", "Merchant Name"};
    private String[] HEADERS_NEW = {"Date", "Transaction Type", "Transaction Details", "Debit",
            "Credit", "Category", "Merchant Name"};

    public void process(Path transactionsFilePath, Path accountStatementFilePath) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(HEADERS_ORIGINAL)
                .setSkipHeaderRecord(true).build();

        try {
            List<AccountStatement> accountStatements = prepareStatement(csvFormat
                    .parse(new FileReader(transactionsFilePath.toFile(), StandardCharsets.UTF_8)));

            try (OutputStream outputStream = Files.newOutputStream(accountStatementFilePath);
                    Workbook workbook = new Workbook(outputStream, "account-statement", null)) {
                Worksheet worksheet = workbook.newWorksheet("Transactions");
                for (int i = 0; i < HEADERS_NEW.length; i++)
                    worksheet.value(0, i, HEADERS_NEW[i]);

                for (int i = 0; i < accountStatements.size(); i++) {
                    AccountStatement accountStatement = accountStatements.get(i);
                    worksheet.value(i + 1, 0, accountStatement.date);
                    worksheet.style(i + 1, 0).format("dd/MM/yyyy").set();
                    worksheet.value(i + 1, 1, accountStatement.transactionType);
                    worksheet.value(i + 1, 2, accountStatement.transactionDetails);
                    worksheet.value(i + 1, 3, accountStatement.debit);
                    worksheet.style(i + 1, 3).format("$0.00").set();
                    worksheet.value(i + 1, 4, accountStatement.credit);
                    worksheet.style(i + 1, 4).format("$0.00").set();
                    worksheet.value(i + 1, 5, accountStatement.category);
                    worksheet.value(i + 1, 6, accountStatement.merchantName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    List<AccountStatement> prepareStatement(Iterable<CSVRecord> csvRecords) {
        List<AccountStatement> accountStatements = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy", Locale.ENGLISH);

        for (CSVRecord csvRecord : csvRecords) {
            Date date;
            try {
                date = dateFormat.parse(csvRecord.get("Date"));
            } catch (ParseException e) {
                System.out.println("Invalid date format [" + csvRecord.get("Date")
                        + "] for tranaction [" + csvRecord.get("Transaction Details") + "]");
                date = new Date();
            }

            BigDecimal amountBD = new BigDecimal(csvRecord.get("Amount"));
            AccountStatement accountStatement =
                    new AccountStatement(date, csvRecord.get("Transaction Type"),
                            csvRecord.get("Transaction Details"),
                            (amountBD.signum() < 0 ? amountBD : BigDecimal.ZERO),
                            (amountBD.signum() > 0 ? amountBD : BigDecimal.ZERO),
                            getCategory(csvRecord.get("Transaction Details"),
                                    csvRecord.get("Category"), csvRecord.get("Merchant Name")),
                            csvRecord.get("Merchant Name"));
            accountStatements.add(accountStatement);
        }

        // sort accountStatements by date
        accountStatements.sort((a, b) -> a.date.compareTo(b.date));

        return accountStatements;
    }

    String getCategory(String transactionDetails, String category, String merchantName) {
        String newCategory;

        switch (category) {
            case "Transfers out":
                newCategory = getCategoryForTransferOut(transactionDetails, category);
                break;
            case "Internal transfers":
                newCategory = transactionDetails.contains("Salary") ? "Salary YJ" : defaultCategory;
                break;
            case "Transfers in":
                newCategory =
                        transactionDetails.contains("Sirius Business Wyners Pty Ltd") ? "Sirius"
                                : defaultCategory;
                break;
            case "Bills":
                newCategory = getCategoryForBills(category, merchantName);
                break;
            case "Shopping":
                newCategory = getCategoryForShopping(transactionDetails, category, merchantName);
                break;
            case "Subscriptions":
                newCategory = "Software";
                break;
            case "Financial":
                newCategory = merchantName.equals("Australian Taxation Office") ? "ATO" : category;
                break;
            case "Food & drink":
                newCategory = merchantName.equals("Crunchyroll") ? "Software" : "Business Meeting";
                break;
            case "Health":
                newCategory = merchantName.equals("Parramatta City Tennis") ? "Business Meeting"
                        : "Office Supply";
                break;
            case "Groceries":
            case "Home":
                newCategory = "Office Supply";
                break;
            case "Entertainment":
                newCategory = "Business Meeting";
                break;
            case "Transport":
            case "Travel":
                newCategory = "Business Trip";
                break;
            case "Other Income":
            case "Cash":
            case "Services":
            case "Uncategorised":
            newCategory = defaultCategory;
                break;
            default:
                newCategory = category;
                break;
        }

        return newCategory;
    }

    private String getCategoryForTransferOut(String transactionDetails, String category) {
        if (transactionDetails.contains("Salary")) {
            return transactionDetails.replace("INTERNET TRANSFER ", "");
        } else if (transactionDetails.contains("MB Financial")) {
            return "Business Car";
        } else {
            return defaultCategory;
        }
    }

    private String getCategoryForShopping(String transactionDetails, String category,
            String merchantName) {
        String newCategory;
        switch (merchantName) {
            case "Amazon Web Services":
            case "Reckon":
            case "Adobe Systems":
                newCategory = "Software";
                break;
            case "Squre":
                newCategory = "Business Meeting";
                break;
            default:
                newCategory = "Office Supply";
        }

        return newCategory;
    }

    private String getCategoryForBills(String category, String merchantName) {
        String newCategory;

        switch (merchantName) {
            case "QBE Insurance":
            case "icare":
                newCategory = "Business Insurance";
                break;
            case "Vodafone":
                newCategory = "Mobile";
                break;
            case "Aussie Broadband":
            case "Superloop":
                newCategory = "Broadband";
                break;
            default:
                newCategory = category;

        }
        return newCategory;
    }
}
