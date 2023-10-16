package com.wyners.accountstatement;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AccountStatementServiceTests {
    @Autowired
    AccountStatementService accountStatementService;

    @Test
    public void testPrepareStatement() throws IOException {
        Path transactionsFilePath = Paths.get("C:\\Users\\odsch\\OneDrive\\Documents\\_Wyners", "FY2024Q1", "Transactions.csv");
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(accountStatementService.HEADERS_ORIGINAL)
                .setSkipHeaderRecord(true)
                .build();
        Iterable<CSVRecord> csvRecords = csvFormat.parse(new FileReader(transactionsFilePath.toFile(), StandardCharsets.UTF_8));
        List<AccountStatement> accountStatements = accountStatementService.prepareStatement(csvRecords);

        // accountStatements.stream().map(AccountStatement::getCategory).forEach(System.out::println);

        Assertions.assertThat(accountStatements.size()).isEqualTo(200);
        // Assertions.assertThat(accountStatements.get(0).getTransactionDetails()).contains("QBE");
    }
}
