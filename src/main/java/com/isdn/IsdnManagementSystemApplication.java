package com.isdn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IsdnManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(IsdnManagementSystemApplication.class, args);
	}

}
