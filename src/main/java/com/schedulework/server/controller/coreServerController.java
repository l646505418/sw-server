package com.schedulework.server.controller;


import com.schedulework.server.entity.job;
import com.schedulework.server.entity.workflow;
import com.schedulework.server.service.jobService;
import com.schedulework.server.service.jobServiceImpl;
import com.schedulework.server.service.workflowService;
import com.schedulework.server.vo.serverResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author:Li Jinming
 * @Description:
 * @date:2023-02-22
 */

@Slf4j
@RestController
public class coreServerController {
@Autowired
jobService jobService;
@Autowired
    workflowService workflowService;
    @PostMapping(path = "/regist/job")
    public serverResponse addJob(@RequestBody job registJob){
        return jobService.registJob(registJob);
    }
    @PostMapping(path = "/update/job")
    public serverResponse updateJob(@RequestBody job modifyJob){
        return jobService.updateJob(modifyJob);
    }


    @PostMapping(path = "/regist/workflow")
    public serverResponse addWorkflow(@RequestBody workflow addWorkflow){
        return workflowService.registWorkflow(addWorkflow);
    }
    @PostMapping(path = "/update/workflow")
    public serverResponse updateWorkflow(@RequestBody workflow modifyWorkflow){
        return workflowService.updateWorkflow(modifyWorkflow);
    }
}
