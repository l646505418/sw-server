package com.schedulework.server.service;


import com.schedulework.server.entity.job;
import com.schedulework.server.entity.runTimeJob;
import com.schedulework.server.entity.workflow;
import com.schedulework.server.vo.responseEnum;
import com.schedulework.server.vo.serverResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * @author:Li Jinming
 * @Description:
 * @date:2023-02-22
 */

@Service
@Slf4j
public class jobServiceImpl implements jobService{
    private static String jobCollection="jobInfo";
    @Autowired
    MongoTemplate mongoTemplate;
    @Override
    public serverResponse registJob(job addJob) {
        log.info("start to persist db for job");
        try {
            mongoTemplate.insert(addJob, jobCollection);
        }catch (Exception e){
            log.error(String.format("job :%s already exist",addJob.getJobName()));
            return new serverResponse(responseEnum.RJOb_FAILED.getStatusCode(),"job already exist",addJob);
        }
        return new serverResponse(responseEnum.RJOb_SUCCESS.getStatusCode(),responseEnum.RJOb_SUCCESS.getStatusDescription(),addJob);
    }

    @Override
    public serverResponse updateJob(job modifyJob) {
        Query query=new Query();
        query.addCriteria(Criteria.where("ownerId").is(modifyJob.getOwnerId()).and("jobId").is(modifyJob.getJobId()));
        Update update=new Update();
        update.set("jobName",modifyJob.getJobName());
        update.set("description",modifyJob.getDescription());
        update.set("inputParams",modifyJob.getInputParams());
        update.set("outputParams",modifyJob.getOutputParams());
        mongoTemplate.upsert(query,update,job.class,jobCollection);
        return new serverResponse(responseEnum.UJOb_SUCCESS.getStatusCode(),responseEnum.UJOb_SUCCESS.getStatusDescription(),modifyJob);
    }

    @Override
    public workflow updateStatus(runTimeJob updateJob) {
        Query query=new Query();
        query.addCriteria(Criteria.where("ownerId").is(updateJob.getJob().getOwnerId()).and("projectName").is(updateJob.getProjectName())
                .and("serviceName").is(updateJob.getServiceName()).and("workflowName").is(updateJob.getWorkflowName()));
        workflow currentWorkFlow =mongoTemplate.findOne(query,workflow.class,"workflowInfo");
        if(currentWorkFlow.getOwnJobs().containsKey(updateJob.getJob().getJobName()))
            currentWorkFlow.getOwnJobs().get(updateJob.getJob().getJobName()).setStatus(updateJob.getStatus());
        else
            currentWorkFlow.getOwnJobs().put(updateJob.getJob().getJobName(),updateJob);

        return currentWorkFlow;
    }

}
