package com.cts.WellnessPlanManagementModule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WellnessPlanManagementModuleApplication
{
    public static void main(String[] args)
    {
		SpringApplication.run(WellnessPlanManagementModuleApplication.class, args);
	}
}
