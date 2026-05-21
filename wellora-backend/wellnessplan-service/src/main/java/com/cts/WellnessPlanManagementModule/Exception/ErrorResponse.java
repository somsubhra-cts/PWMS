package com.cts.WellnessPlanManagementModule.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.Date;


@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class ErrorResponse
{

    private Date timeStamp;
    private int status;
    private String error;
    private String message;
}
