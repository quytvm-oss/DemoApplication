package com.example.devop.demo.api;

import com.example.devop.demo.application.service.QuartzJobService;
import com.example.devop.demo.infrastructure.authorization.MustHavePermission;
import com.example.devop.demo.infrastructure.authorization.metadata.ActionName;
import com.example.devop.demo.infrastructure.authorization.metadata.ResourceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private QuartzJobService jobService;

    @PostMapping("/trigger/{jobName}")
    public ResponseEntity<String> triggerJob(@PathVariable String jobName) {
        try {
            jobService.triggerJobNow(jobName);
            return ResponseEntity.ok("Job triggered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/pause/{jobName}")
    public ResponseEntity<String> pauseJob(@PathVariable String jobName) {
        try {
            jobService.pauseJob(jobName);
            return ResponseEntity.ok("Job paused");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @MustHavePermission(resource = ResourceName.Customers, action = ActionName.View)
    public ResponseEntity<List<String>> getAllJobs() {
        try {
            return ResponseEntity.ok(jobService.getAllJobs());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
