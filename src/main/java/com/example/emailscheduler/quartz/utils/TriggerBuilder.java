package com.example.emailscheduler.quartz.utils;

import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;

import java.time.ZonedDateTime;
import java.util.Date;

public class TriggerBuilder {

    public static Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        return org.quartz.TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send email trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

}
