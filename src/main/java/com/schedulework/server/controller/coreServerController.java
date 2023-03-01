package com.schedulework.server.controller;


import com.schedulework.server.entity.job;
import com.schedulework.server.entity.runTimeJob;
import com.schedulework.server.entity.workflow;
import com.schedulework.server.service.jobService;
import com.schedulework.server.service.jobServiceImpl;
import com.schedulework.server.service.workflowService;
import com.schedulework.server.vo.responseEnum;
import com.schedulework.server.vo.serverResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.ServerResponse;

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
        return workflowService.modifyWorkflow(modifyWorkflow);
    }
    @PostMapping(path = "/start/workflow")
    public serverResponse startWorkflow(@RequestBody workflow startedWorkflow){
        if(startedWorkflow.getOwnJobs().keySet().isEmpty()){
            return new serverResponse(responseEnum.SWORKFLOW_FAILED.getStatusCode(),responseEnum.SWORKFLOW_FAILED.getStatusDescription(),"there are no jobs in this workflow");
        }
        return workflowService.startWorkflow(startedWorkflow);
    }
    @PostMapping(path = "/finish/workflow")
    public serverResponse finishWorkflow(@RequestBody workflow finishWorkflow){
        return workflowService.finishWorkflow(finishWorkflow);
    }

    @PostMapping(path = "/change/JobStaus")
    public serverResponse changeJobStatus(@RequestBody runTimeJob currentJob){
        workflow currentWorkflow=jobService.updateStatus(currentJob);
        workflowService.updateWorkflow(currentWorkflow);
        //TODO:connect Kafka to notify front-end
        return new serverResponse(responseEnum.UJOB_STATUS_SUCCESS.getStatusCode(),responseEnum.UJOB_STATUS_SUCCESS.getStatusDescription(),"success");

    }
}
