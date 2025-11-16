package com.example.devop.demo.application.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportJob  implements Job {

    //@Autowired
    //private EmailService emailService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String email = dataMap.getString("report");

        log.info("📧 Sending report to: {}", email);
        //emailService.sendEmail(email, "Scheduled Email", "This is a test");
    }
}
