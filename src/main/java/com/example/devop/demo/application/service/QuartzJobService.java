package com.example.devop.demo.application.service;

import org.quartz.*;

import java.util.List;

public interface QuartzJobService {
    void scheduleJob(String jobName, String cronExpression, Class<? extends Job> jobClass) throws SchedulerException;

    void pauseJob(String jobName) throws SchedulerException;

    void resumeJob(String jobName) throws SchedulerException;

    void deleteJob(String jobName) throws SchedulerException;

    void triggerJobNow(String jobName) throws SchedulerException;

    List<String> getAllJobs() throws SchedulerException;
}
