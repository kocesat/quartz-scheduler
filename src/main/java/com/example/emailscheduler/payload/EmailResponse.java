package com.example.emailscheduler.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailResponse {

    private boolean success;

    private String jobId;

    private String jobGroup;

    private String message;

    public EmailResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

}
