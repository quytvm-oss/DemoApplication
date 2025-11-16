package com.example.devop.demo.application.service.impl;

import com.example.devop.demo.application.service.QuartzJobService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QuartzJobServiceImpl implements QuartzJobService {

    @Autowired
    private Scheduler scheduler;

    // ✅ Thêm job mới
    public void scheduleJob(String jobName, String cronExpression, Class<? extends Job> jobClass)
            throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName)
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobName + "Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("✅ Scheduled job: {}", jobName);
    }

    // ✅ Pause job
    public void pauseJob(String jobName) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(jobName));
        log.info("⏸️ Paused job: {}", jobName);
    }

    // ✅ Resume job
    public void resumeJob(String jobName) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(jobName));
        log.info("▶️ Resumed job: {}", jobName);
    }

    // ✅ Xóa job
    public void deleteJob(String jobName) throws SchedulerException {
        scheduler.deleteJob(JobKey.jobKey(jobName));
        log.info("🗑️ Deleted job: {}", jobName);
    }

    // ✅ Trigger job ngay lập tức
    public void triggerJobNow(String jobName) throws SchedulerException {
        scheduler.triggerJob(JobKey.jobKey(jobName));
        log.info("🚀 Triggered job: {}", jobName);
    }

    // ✅ Lấy danh sách jobs
    public List<String> getAllJobs() throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.anyGroup())
                .stream()
                .map(JobKey::getName)
                .collect(Collectors.toList());
    }
}
