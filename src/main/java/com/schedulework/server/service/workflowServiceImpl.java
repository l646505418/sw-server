package com.schedulework.server.service;


import com.schedulework.server.entity.job;
import com.schedulework.server.entity.workflow;
import com.schedulework.server.vo.responseEnum;
import com.schedulework.server.vo.serverResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * @author:Li Jinming
 * @Description:
 * @date:2023-02-22
 */

@Slf4j
public class workflowServiceImpl implements workflowService{
    @Autowired
    MongoTemplate mongoTemplate;

    private static String workflowCollection="workflowInfo";
    @Override
    public serverResponse registWorkflow(workflow addWorkflow) {
        log.info("start to persist db for workflow");
        try {
            mongoTemplate.insert(addWorkflow, workflowCollection);
        }catch (Exception e){
            log.error(String.format("job :%s already exist",addWorkflow.getWorkflowName()));
            return new serverResponse(responseEnum.RWORKFLOW_FAILED.getStatusCode(),"job already exist",addWorkflow);
        }
        return new serverResponse(responseEnum.RWORKFLOW_SUCCESS.getStatusCode(),responseEnum.RWORKFLOW_SUCCESS.getStatusDescription(),addWorkflow);

    }

    @Override
    public serverResponse updateWorkflow(workflow modifyWorkflow) {
        Query query=new Query();
        query.addCriteria(Criteria.where("ownerId").is(modifyWorkflow.getOwnerId())
                .and("projectName").is(modifyWorkflow.getProjectName())
                .and("serviceName").is(modifyWorkflow.getServiceName())
                .and("workflowName").is(modifyWorkflow.getWorkflowName()));
        Update update=new Update();
        update.set("workflowName",modifyWorkflow.getWorkflowName());
        update.set("ownJobs",modifyWorkflow.getOwnJobs());
        mongoTemplate.upsert(query,update, workflow.class,workflowCollection);
        return new serverResponse(responseEnum.UWORKFLOW_SUCCESS.getStatusCode(),responseEnum.UWORKFLOW_SUCCESS.getStatusDescription(),modifyWorkflow);

    }
}
