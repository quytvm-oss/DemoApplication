package com.example.devop.demo.application.job;

import com.example.devop.demo.infrastructure.persistence.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class SampleSpringJob implements Job {
    @Autowired
    private UserRepository _userRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Job started at: " + LocalDateTime.now());
        var users = _userRepository.findAll();
        for (var user : users) {
            log.info("User: {}", user.getFirstName());
        }
        //userService.deactivateInactiveUsers();
        log.info("Job finished at: " + LocalDateTime.now());
    }
}
