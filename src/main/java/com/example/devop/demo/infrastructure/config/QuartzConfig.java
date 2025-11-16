package com.example.devop.demo.infrastructure.config;

import com.example.devop.demo.application.job.EmailJob;
import com.example.devop.demo.application.job.ReportJob;
import com.example.devop.demo.application.job.SampleSpringJob;
import com.example.devop.demo.infrastructure.job.AutowiringSpringBeanJobFactory;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;


@Configuration
@EnableScheduling
public class QuartzConfig {

    // ===== JOB DETAILS =====

    @Bean
    public JobDetail sampleJobDetail() {
        return JobBuilder.newJob(SampleSpringJob.class)
                .withIdentity("sampleJob", "defaultGroup")
                .withDescription("Sample job that runs every 30 seconds")
                .storeDurably() // Job tồn tại ngay cả khi không có trigger
                .requestRecovery() // ✅ Chạy lại nếu miss do server restart
                .build();
    }

    @Bean
    public JobDetail emailJobDetail() {
        return JobBuilder.newJob(EmailJob.class)
                .withIdentity("emailJob", "emailGroup")
                .withDescription("Send email notifications")
                .usingJobData("email", "admin@example.com") // ✅ Truyền data vào job
                .storeDurably()
                .build();
    }

    @Bean
    public JobDetail reportJobDetail() {
        return JobBuilder.newJob(ReportJob.class)
                .withIdentity("reportJob", "reportGroup")
                .storeDurably()
                .build();
    }

    // ===== TRIGGERS =====

    // ✅ CronTrigger: chạy mỗi 30 giây
    @Bean
    public Trigger sampleJobTrigger(@Qualifier("sampleJobDetail") JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("sampleTrigger", "defaultGroup")
                .withDescription("Runs every 30 seconds")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 23 * * ?"))
                .build();
    }

    // ✅ SimpleTrigger: chạy mỗi 5 phút
    @Bean
    public Trigger emailJobTrigger(@Qualifier("emailJobDetail") JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("emailTrigger", "emailGroup")
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInMinutes(5)
                                .repeatForever()
                )
                .startNow() // ✅ Bắt đầu ngay lập tức
                .build();
    }

    // ✅ CronTrigger: chạy vào 2h sáng mỗi ngày
    @Bean
    public Trigger reportJobTrigger(@Qualifier("reportJobDetail") JobDetail jobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity("reportTrigger", "reportGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?"))
                .build();
    }

    // ===== SCHEDULER CONFIGURATION =====

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            @Qualifier("quartzJobFactory") SpringBeanJobFactory jobFactory) {

        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        // ✅ Config
        factory.setOverwriteExistingJobs(true);
        factory.setAutoStartup(true);
        factory.setJobFactory(jobFactory);
        factory.setWaitForJobsToCompleteOnShutdown(true);

        factory.setJobDetails(
                sampleJobDetail(),
                emailJobDetail(),
                reportJobDetail()
        );

        factory.setTriggers(
                sampleJobTrigger(sampleJobDetail()),
                emailJobTrigger(emailJobDetail()),
                reportJobTrigger(reportJobDetail())
        );

        return factory;
    }

    // ✅ JobFactory để inject Spring beans vào Quartz Job
    @Bean("quartzJobFactory")
    public SpringBeanJobFactory springBeanJobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }
}