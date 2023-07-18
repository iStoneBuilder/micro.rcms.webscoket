package com.stone.it.micro.rcms.web.scoket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * WebSocketShellApplication
 */

@SpringBootApplication
@ComponentScan(basePackages = {"com.stone.it"})
public class WebSocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebSocketApplication.class, args);
	}

}
