package com.cts.WellnessPlanManagementModule.Config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration
{
    @Bean
  public OpenAPI changeDocument()
  {
      OpenAPI openAPI=
              new OpenAPI().info(
                      new Info().
                              title("Wellness Plan Management Module")
                              .version("v1.1").description("It is a module that manages all the activities related to wellness Plan ")
              )
              ;

      return openAPI;
  }
}
