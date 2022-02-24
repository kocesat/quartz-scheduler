package com.example.emailscheduler.web;

import com.example.emailscheduler.payload.EmailRequest;
import com.example.emailscheduler.payload.EmailResponse;
import com.example.emailscheduler.quartz.utils.JobDetailBuilder;
import com.example.emailscheduler.quartz.utils.TriggerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;

@Slf4j
@RestController
public class EmailSchedulerController {

//    @Autowired
//    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private Scheduler scheduler;

    @PostMapping("/schedule/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequest.getDateTime(), emailRequest.getTimeZone());
            if (dateTime.isBefore(ZonedDateTime.now())) {
                EmailResponse emailResponse = new EmailResponse(false, "Date time must be after now");
                return ResponseEntity
                        .badRequest()
                        .body(emailResponse);
            }

            JobDetail jobDetail = JobDetailBuilder.buildJobDetail(emailRequest);
            Trigger trigger = TriggerBuilder.buildTrigger(jobDetail, dateTime);

            scheduler.scheduleJob(jobDetail, trigger);

            EmailResponse emailResponseSuccess = new EmailResponse(true,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(),
                    "Email scheduled successfully");
            return ResponseEntity.ok(emailResponseSuccess);
        } catch (SchedulerException e) {
            log.error("Error while scheduling email: ", e);
            EmailResponse emailResponse = new EmailResponse(false, "Error. Please try again later");
            return ResponseEntity
                    .internalServerError()
                    .body(emailResponse);
        }
    }

    @GetMapping("/get")
    public ResponseEntity<String> getApiTest() {
        return ResponseEntity.ok("Get Api Test passed");
    }



}
