package com.rahul.inventorybilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // lets @Scheduled methods actually run
public class InventoryBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryBillingApplication.class, args);
    }
}
