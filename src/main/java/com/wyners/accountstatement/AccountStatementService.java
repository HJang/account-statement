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

@Service
public class AccountStatementService {
    private String[] HEADERS_ORIGINAL = { "Date", "Amount", "Account Number", "Empty", "Transaction Type",
            "Transaction Details",
            "Balance", "Category", "Merchant Name" };
    private String[] HEADERS_NEW = { "Date", "Transaction Type", "Transaction Details", "Debit", "Credit", "Category",
            "Merchant Name" };

    public void process(Path transactionsFilePath, Path accountStatementFilePath) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS_ORIGINAL)
                .setSkipHeaderRecord(true)
                .build();

        Iterable<CSVRecord> csvRecords;
        try {
            csvRecords = csvFormat
                    .parse(new FileReader(transactionsFilePath.toFile(), StandardCharsets.UTF_8));
            List<AccountStatement> accountStatements = new ArrayList<>();

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy", Locale.ENGLISH);

            for (CSVRecord csvRecord : csvRecords) {
                BigDecimal amountBD = new BigDecimal(csvRecord.get("Amount"));
                AccountStatement accountStatement = new AccountStatement(dateFormat.parse(csvRecord.get("Date")),
                        csvRecord.get("Transaction Type"),
                        csvRecord.get("Transaction Details"),
                        (amountBD.signum() < 0 ? amountBD : BigDecimal.ZERO),
                        (amountBD.signum() > 0 ? amountBD : BigDecimal.ZERO),
                        csvRecord.get("Category"),
                        csvRecord.get("Merchant Name"));
                accountStatements.add(accountStatement);
            }

            // sort accountStatements by date
            accountStatements.sort((a, b) -> a.date.compareTo(b.date));

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
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }
}
