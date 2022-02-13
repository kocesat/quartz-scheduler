package com.example.emailscheduler.web;

import com.example.emailscheduler.payload.EmailRequest;
import com.example.emailscheduler.payload.EmailResponse;
import com.example.emailscheduler.quartz.job.EmailJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
public class EmailSchedulerController {

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
            JobDetail jobDetail = buildJobDetail(emailRequest);
            Trigger trigger = buildTrigger(jobDetail, dateTime);

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

    private JobDetail buildJobDetail(EmailRequest scheduleEmailRequest){
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", scheduleEmailRequest.getEmail());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send email job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send email trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

}
