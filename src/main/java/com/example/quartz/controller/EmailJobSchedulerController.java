package com.example.quartz.controller;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.quartz.job.EmailJob;
import com.example.quartz.model.ScheduleEmailRequest;
import com.example.quartz.model.ScheduleEmailResponse;

import jakarta.validation.Valid;

@RestController
public class EmailJobSchedulerController {
    private static final Logger LOG = LoggerFactory.getLogger(EmailJobSchedulerController.class);
    @Autowired
    private Scheduler scheduler;

    @PostMapping("/scheduleEmail")
    public ResponseEntity<ScheduleEmailResponse> scheduleEmail(
            @Valid @RequestBody ScheduleEmailRequest scheduleEmailRequest) {
        try {
            ZonedDateTime zoneDateTime = ZonedDateTime.of(scheduleEmailRequest.getLocalDateTime(),
                    scheduleEmailRequest.getZoneId());
            if (zoneDateTime.isBefore(ZonedDateTime.now())) {
                return new ResponseEntity<ScheduleEmailResponse>(new ScheduleEmailResponse(false,
                        "DateTime must be after current time"), HttpStatus.BAD_REQUEST);
            }
            JobDetail jobDetail = buildJobDetail(scheduleEmailRequest);
            Trigger trigger = buildJobTrigger(jobDetail, zoneDateTime);
            scheduler.scheduleJob(jobDetail, trigger);
            return new ResponseEntity<ScheduleEmailResponse>(
                    new ScheduleEmailResponse(true, jobDetail.getKey().getName(),
                            jobDetail.getKey().getGroup(), "Email Scheduled Successfully!"),
                    HttpStatus.OK);
        } catch (SchedulerException e) {
            LOG.error("Error Scheduling email", e);
            return new ResponseEntity<ScheduleEmailResponse>(new ScheduleEmailResponse(false,
                    "Error scheduling email. Please try again."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Trigger buildJobTrigger(JobDetail jobDetail, ZonedDateTime zoneDateTime) {
        return TriggerBuilder.newTrigger().forJob(jobDetail)
        .withIdentity(jobDetail.getKey().getName(),"email-triggers")
        .withDescription("Send Email Trigger")
        .startAt(Date.from(zoneDateTime.toInstant()))
        .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
        .build();
    }

    private JobDetail buildJobDetail(ScheduleEmailRequest scheduleEmailRequest) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("email", scheduleEmailRequest.getEmail());
        jobDataMap.put("subject", scheduleEmailRequest.getSubject());
        jobDataMap.put("body", scheduleEmailRequest.getBody());
        return JobBuilder.newJob(EmailJob.class)
        .withIdentity(UUID.randomUUID().toString(), "email_jobs")
        .withDescription("Send Email Job")
        .usingJobData(jobDataMap)
        .storeDurably()
        .build();
    }
}
