package com.wyners.accountstatement;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccountStatmentApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AccountStatmentApplication.class, args);
	}

	@Autowired
	public AccountStatementService accountStatementService;

	@Override
	public void run(String... args) throws Exception {
		if (args.length < 1) {
			System.out.println("Please provide a transactions file path");
			return;
		}
		if (!Paths.get(args[0]).toFile().exists()) {
			System.out.println("Please provide a valid transactions file path");
			return;
		}
		if (!Paths.get(args[0]).toFile().isFile()) {
			System.out.println("Please provide a valid transactions file path");
			return;
		}
		if (!Paths.get(args[0]).toFile().canRead()) {
			System.out.println("Please provide a valid transactions file path");
			return;
		}

		Path transactionsFilePath = Paths.get(args[0]);
		Path accountStatementFilePath = Paths.get(args[1]);
		accountStatementService.process(transactionsFilePath, accountStatementFilePath);
	}

}
