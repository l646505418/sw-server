package com.schedulework.server.service;

import com.google.gson.Gson;
import com.schedulework.server.entity.workflow;
import com.schedulework.server.util.JobStatus;
import com.schedulework.server.vo.responseEnum;
import com.schedulework.server.vo.serverResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author:Li Jinming
 * @Description:
 * @date:2023-02-22
 */

@Slf4j
public class workflowServiceImpl implements workflowService{
    private Gson gson=new Gson();
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    private static String workflowCollection="workflowInfo";
    @Override
    public serverResponse registWorkflow(workflow addWorkflow) {
        log.info(String.format("start to persist workflow:%s",addWorkflow.getWorkflowName()));

        CompletableFuture<Exception> futureDB=CompletableFuture.supplyAsync(() ->{
            log.info(String.format("start to persist db for workflow:%s",addWorkflow.getWorkflowName()));
                try {
                    mongoTemplate.insert(addWorkflow, workflowCollection);
                } catch (Exception e){
                    return e;
                }
                return null;
            });
        CompletableFuture<Exception> futureCashe=CompletableFuture.supplyAsync(()->{
            log.info(String.format("start to persist cache for workflow:%s",addWorkflow.getWorkflowName()));
            String cacheWL=gson.toJson(addWorkflow);
                String key=generateRedisKey(addWorkflow);
                if(redisTemplate.hasKey(key)){
                    return new Exception("");
                }
                redisTemplate.opsForSet().add(key,JobStatus.PENDING);
                redisTemplate.opsForSet().add(key,cacheWL);
                redisTemplate.opsForValue().set(key+"-num",0);
                return null;
        });
        if(futureDB.join()==null&&futureCashe.join()==null){
            log.info(String.format("Successfully regist workflow:%s",addWorkflow.getWorkflowName()));
            return new serverResponse(responseEnum.RWORKFLOW_SUCCESS.getStatusCode(),responseEnum.RWORKFLOW_SUCCESS.getStatusDescription(),addWorkflow);
        }
        log.error(String.format("job :%s already exist",addWorkflow.getWorkflowName()));
        return new serverResponse(responseEnum.RWORKFLOW_FAILED.getStatusCode(),"job already exist",addWorkflow);
    }

    @Override
    public serverResponse updateWorkflow(workflow updateWorkflow) {
        log.info(String.format("start update workflow:%s"),updateWorkflow.getWorkflowName());
        String key=generateRedisKey(updateWorkflow);
        String cacheWL=gson.toJson(updateWorkflow);
        CompletableFuture.runAsync(()->{
            log.info("update in cache");
            redisTemplate.opsForSet().pop(key);
            redisTemplate.opsForSet().add(key,JobStatus.PENDING);
            redisTemplate.opsForSet().add(key,cacheWL);
        });
        CompletableFuture.runAsync(()->{
            log.info("update in db");
            Query query=new Query();
            query.addCriteria(Criteria.where("ownerId").is(updateWorkflow.getOwnerId())
                    .and("projectName").is(updateWorkflow.getProjectName())
                    .and("serviceName").is(updateWorkflow.getServiceName())
                    .and("workflowName").is(updateWorkflow.getWorkflowName()));
            Update update=new Update();
            update.set("workflowName",updateWorkflow.getWorkflowName());
            update.set("ownJobs",updateWorkflow.getOwnJobs());
            mongoTemplate.upsert(query,update, workflow.class,workflowCollection);
        });
        return new serverResponse(responseEnum.UWORKFLOW_SUCCESS.getStatusCode(),responseEnum.UWORKFLOW_SUCCESS.getStatusDescription(),updateWorkflow);

    }


//TODO: use read lock and write lock

    @Override
    public serverResponse modifyWorkflow(workflow modifyWorkflow) {
        log.info(String.format("start to modify workflow:%s",modifyWorkflow.getWorkflowName()));
        CompletableFuture.runAsync(()->{
            log.info(String.format("start to modify workflow:%s in db",modifyWorkflow.getWorkflowName()));
            Query query=new Query();
            query.addCriteria(Criteria.where("ownerId").is(modifyWorkflow.getOwnerId())
                    .and("projectName").is(modifyWorkflow.getProjectName())
                    .and("serviceName").is(modifyWorkflow.getServiceName())
                    .and("workflowName").is(modifyWorkflow.getWorkflowName()));
            Update update=new Update();
            update.set("workflowName",modifyWorkflow.getWorkflowName());
            update.set("ownJobs",modifyWorkflow.getOwnJobs());
            mongoTemplate.upsert(query,update, workflow.class,workflowCollection);
        });
        CompletableFuture.runAsync(()->{
            log.info(String.format("start to update workflow:%s in cache",modifyWorkflow.getWorkflowName()));
            String cacheWL=gson.toJson(modifyWorkflow);
            String key=generateRedisKey(modifyWorkflow);
            if(!redisTemplate.hasKey(key)) {
                redisTemplate.opsForSet().add(key, JobStatus.PENDING);
                redisTemplate.opsForSet().add(key, cacheWL);
            }
            else{
                while(redisTemplate.opsForValue().setIfAbsent(key+"-lock",1,30,TimeUnit.SECONDS)) {
                    redisTemplate.opsForSet().pop(key);
                    redisTemplate.opsForSet().add(key, JobStatus.PENDING);
                    redisTemplate.opsForSet().add(key, cacheWL);
                    redisTemplate.delete(key+"-lock");
                }
            }
        });
        return new serverResponse(responseEnum.UWORKFLOW_SUCCESS.getStatusCode(),responseEnum.UWORKFLOW_SUCCESS.getStatusDescription(),modifyWorkflow);

    }

    @Override
    public serverResponse finishWorkflow(workflow finishWorkflow) {
        log.info("finishing the thread"+finishWorkflow.getWorkflowName());
        workflow initWorkflow=finishWorkflow;
        initWorkflow.setWorkflowName(initWorkflow.getWorkflowName().split("_")[0]);
        String key=generateRedisKey(initWorkflow);
        String keyThread=generateRedisKey(finishWorkflow);
        CompletableFuture<Set> cacheInit=CompletableFuture.supplyAsync(()->{
            return redisTemplate.opsForSet().members(key);
        });
        CompletableFuture<Set> cacheNow=CompletableFuture.supplyAsync(()->{
            return redisTemplate.opsForSet().members(keyThread);
        });
        if(cacheNow.join().size()==0){
            log.error(String.format("cannot found the thread:%s",keyThread));
            return new serverResponse(responseEnum.FWORKFLOW_FAILED.getStatusCode(),responseEnum.FWORKFLOW_FAILED.getStatusDescription(),"no thread found");
        }
        if(cacheInit.join().toArray()[0].equals(JobStatus.PENDING)){
            log.info("persist in DB and delete in cache,no need other performs");
        }
        else{
            log.info(String.format("need add whole workflow:%s in DB and cache",initWorkflow.getWorkflowName()));
            initWorkflow.getOwnJobs().entrySet().stream().forEach(stringrunTimeJobEntry -> stringrunTimeJobEntry
                    .setValue(stringrunTimeJobEntry.getValue().changeStatus(JobStatus.PENDING)));
            while(redisTemplate.opsForValue().setIfAbsent(key+"-lock",1,30,TimeUnit.SECONDS)){
                redisTemplate.opsForSet().pop(key);
                initWorkflow.setStatus(JobStatus.PENDING);
                CompletableFuture.runAsync(()->{
                    String currCacheWL=gson.toJson(initWorkflow);
                    log.info(String.format("start to persist cache for workflow:%s",key));
                    redisTemplate.opsForSet().add(key, initWorkflow.getStatus());
                    redisTemplate.opsForSet().add(key,currCacheWL);
                });
                CompletableFuture.runAsync(() ->{
                    log.info(String.format("start to persist DB for workflow:%s",key));
                    mongoTemplate.insert(initWorkflow, workflowCollection);
                });

                redisTemplate.delete(key+"-lock");
            }
        }
        finishWorkflow.setStatus("FINISH");
        CompletableFuture.runAsync(()->{
            redisTemplate.opsForSet().pop(keyThread);
        });
        CompletableFuture.runAsync(()->{
            Query query=new Query();
            query.addCriteria(Criteria.where("ownerId").is(finishWorkflow.getOwnerId())
                    .and("projectName").is(finishWorkflow.getProjectName())
                    .and("serviceName").is(finishWorkflow.getServiceName())
                    .and("workflowName").is(finishWorkflow.getWorkflowName()));
            Update update=new Update();
            update.set("status",finishWorkflow.getStatus());
            mongoTemplate.upsert(query,update, workflow.class,workflowCollection);
        });
 return new serverResponse(responseEnum.FWORKFLOW_SUCCESS.getStatusCode(),responseEnum.FWORKFLOW_SUCCESS.getStatusDescription(),null);

    }


    //TODO:1. use a util class to encapsulation Gson functions 2. create different handler and a handler manager
    @Override
    public serverResponse startWorkflow(workflow runWorkflow) {
        if(runWorkflow.getWorkflowName().contains("_")){
            log.info(String.format("%s is a thread"),runWorkflow.getWorkflowName());
            log.info(String.format("start handle thread:%s"),runWorkflow.getWorkflowName());
            String workFlowName=runWorkflow.getWorkflowName().split("_")[0];
            workflow parentWorkFlow=runWorkflow;
            parentWorkFlow.setWorkflowName(workFlowName);
            String parentKey=generateRedisKey(parentWorkFlow);
            log.info("get the parent workflow");
            try {
                String cacheWL = (String) redisTemplate.opsForSet().members(parentKey).toArray()[1];
                parentWorkFlow = gson.fromJson(cacheWL, workflow.class);
            }catch (Exception e){
                log.error(String.format("cannot find the parent workflow for thread:%s",runWorkflow.getWorkflowName()));
                //todo:write handler and define ourselves exception
                throw new RuntimeException("there are something worng when use in SW in your code directly");
            }
            workflow finalParentWorkFlow = parentWorkFlow;
            finalParentWorkFlow.setWorkflowName(runWorkflow.getWorkflowName());
            finalParentWorkFlow.setStatus(JobStatus.PROCESSING);
            CompletableFuture.runAsync(()->{
                String keyNow=generateRedisKey(finalParentWorkFlow);
                log.info(String.format("start to persist cache for thread:%s", finalParentWorkFlow.getWorkflowName()));
                String finalCacheWL=gson.toJson(finalParentWorkFlow);
                redisTemplate.opsForSet().add(keyNow,finalParentWorkFlow.getStatus());
                redisTemplate.opsForSet().add(keyNow,finalCacheWL);
            });
            CompletableFuture.runAsync(()->{
                log.info(String.format("start to persist DB for thread:%s",finalParentWorkFlow.getWorkflowName()));
                mongoTemplate.insert(finalParentWorkFlow, workflowCollection);
            });
            return new serverResponse(responseEnum.SWORKFLOW_SUCCESS.getStatusCode(),responseEnum.SWORKFLOW_SUCCESS.getStatusDescription(),finalParentWorkFlow);

        }
        String key=generateRedisKey(runWorkflow);
        log.info(String.format("start handle workflow:%s"),key);
        workflow maintainWorkflow=null;
        CompletableFuture<Set> cacheRes=CompletableFuture.supplyAsync(()->{
            return redisTemplate.opsForSet().members(key);
        });
        CompletableFuture<workflow> DBRes=CompletableFuture.supplyAsync(()->{
            Query query=new Query();
            query.addCriteria(Criteria.where("ownerId").is(runWorkflow.getOwnerId())
                    .and("projectName").is(runWorkflow.getProjectName())
                    .and("serviceName").is(runWorkflow.getServiceName())
                    .and("workflowName").is(runWorkflow.getWorkflowName()));
            return mongoTemplate.findOne(query,workflow.class,workflowCollection);
        });
        if(cacheRes==null){
            log.info(String.format("cannot find the workflow :%s in cache"),key);
            if(DBRes==null){
                log.info("new workflow from spring boot application");
                CompletableFuture.runAsync(()->{
                    log.info(String.format("start to persist cache for workflow:%s",runWorkflow.getWorkflowName()));
                    String cacheWL=gson.toJson(runWorkflow);
                    redisTemplate.opsForSet().add(key,"creating");
                    redisTemplate.opsForSet().add(key,cacheWL);
                    redisTemplate.opsForValue().set(key+"-num",1);
                });
//                CompletableFuture.runAsync(() ->{
//                    log.info(String.format("start to persist db for workflow:%s",runWorkflow.getWorkflowName()));
//                        mongoTemplate.insert(runWorkflow, workflowCollection);
//                });
                workflow currentWorkFlow=runWorkflow;
                String keyNow=generateRedisKey(currentWorkFlow);
                currentWorkFlow.setWorkflowName(currentWorkFlow.getWorkflowName()+"_0");
                currentWorkFlow.setStatus(JobStatus.PROCESSING);
                CompletableFuture.runAsync(()->{
                    log.info(String.format("start to persist cache for thread:%s",currentWorkFlow.getWorkflowName()));
                    String cacheWL=gson.toJson(currentWorkFlow);
                    redisTemplate.opsForSet().add(keyNow,currentWorkFlow.getStatus());
                    redisTemplate.opsForSet().add(keyNow,cacheWL);
                });
                CompletableFuture.runAsync(() ->{
                    log.info(String.format("start to persist DB for thread:%s",keyNow));
                    mongoTemplate.insert(currentWorkFlow, workflowCollection);
                });
                return new serverResponse(responseEnum.SWORKFLOW_SUCCESS.getStatusCode(),responseEnum.SWORKFLOW_SUCCESS.getStatusDescription(),currentWorkFlow);
            }else{
                log.info("use the DB data");
                workflow finalMaintainWorkflow = DBRes.join();
                log.info(String.format("start to persist cache for workflow:%s", finalMaintainWorkflow.getWorkflowName()));
                String cacheWL=gson.toJson(finalMaintainWorkflow);
                redisTemplate.opsForSet().add(key, JobStatus.PENDING);
                redisTemplate.opsForSet().add(key,cacheWL);
                redisTemplate.opsForValue().set(key+"-num",0);

                while(redisTemplate.opsForValue().setIfAbsent(key+"-lock",1,30,TimeUnit.SECONDS)){
                    Object WLNum=redisTemplate.opsForValue().get(key+"-num");
                    workflow currWorkflow=finalMaintainWorkflow;
                    currWorkflow.setWorkflowName(finalMaintainWorkflow.getWorkflowName()+"_"+(String)WLNum);
                    currWorkflow.setStatus(JobStatus.PROCESSING);
                    String  keyNow=generateRedisKey(currWorkflow);
                    CompletableFuture.runAsync(()->{
                        String currCacheWL=gson.toJson(currWorkflow);
                        log.info(String.format("start to persist cache for thread:%s",keyNow));
                        redisTemplate.opsForSet().add(keyNow, JobStatus.PROCESSING);
                        redisTemplate.opsForSet().add(keyNow,currCacheWL);
                    });
                    CompletableFuture.runAsync(() ->{
                        log.info(String.format("start to persist DB for thread:%s",keyNow));
                        mongoTemplate.insert(currWorkflow, workflowCollection);
                    });
                    redisTemplate.opsForValue().increment(key+"-num",(Long)WLNum);
                    redisTemplate.delete(key+"-lock");
                    log.info(String.format("successfully start workflow:%s"),currWorkflow.getWorkflowName());
                    return new serverResponse(responseEnum.SWORKFLOW_SUCCESS.getStatusCode(),responseEnum.SWORKFLOW_SUCCESS.getStatusDescription(),currWorkflow);

                }
            }
        }
        else{
            log.info(String.format("find the workflow :%s in cache"),key);
            if(cacheRes.join().stream().filter(e -> e.equals(JobStatus.PENDING)).toList().size()!=0)
                while (redisTemplate.opsForValue().setIfAbsent(key+"-lock", 1, 30, TimeUnit.SECONDS)){
                    maintainWorkflow=gson.fromJson((String)cacheRes.join().toArray()[1],workflow.class);
                    workflow finalMaintainWorkflow = maintainWorkflow;
                    String keyNow=generateRedisKey(finalMaintainWorkflow);
                    CompletableFuture.runAsync(()->{
                        String cacheWL=gson.toJson(finalMaintainWorkflow);
                        log.info(String.format("start to persist cache for thread:%s",keyNow));
                        redisTemplate.opsForSet().add(keyNow, JobStatus.PROCESSING);
                        redisTemplate.opsForSet().add(keyNow,cacheWL);
                    });
                    CompletableFuture.runAsync(() ->{
                        log.info(String.format("start to persist DB for thread:%s",keyNow));
                        mongoTemplate.insert(finalMaintainWorkflow, workflowCollection);
                    });

                    redisTemplate.delete(key+"-lock");
                    log.info(String.format("successfully start workflow:%s"),finalMaintainWorkflow.getWorkflowName());
                    return new serverResponse(responseEnum.SWORKFLOW_SUCCESS.getStatusCode(),responseEnum.SWORKFLOW_SUCCESS.getStatusDescription(),finalMaintainWorkflow);
                }
            else{
                log.info(String.format("find the workflow :%s in cache,but do not regist in client"),key);
                while (redisTemplate.opsForValue().setIfAbsent(key+"-lock", 1, 30, TimeUnit.SECONDS)){
                    workflow finalMaintainWorkflow = runWorkflow;
                    Object WLNum=redisTemplate.opsForValue().get(key+"-num");
                    finalMaintainWorkflow.setWorkflowName(finalMaintainWorkflow.getWorkflowName()+"_"+(String)WLNum);
                    finalMaintainWorkflow.setStatus(JobStatus.PROCESSING);
                    String keyNow=generateRedisKey(finalMaintainWorkflow);
                    CompletableFuture.runAsync(()->{
                        String cacheWL=gson.toJson(finalMaintainWorkflow);
                        log.info(String.format("start to persist cache for thread:%s",keyNow));
                        redisTemplate.opsForSet().add(keyNow, JobStatus.PROCESSING);
                        redisTemplate.opsForSet().add(keyNow,cacheWL);
                    });
                    CompletableFuture.runAsync(() ->{
                        log.info(String.format("start to persist DB for thread:%s",keyNow));
                        mongoTemplate.insert(finalMaintainWorkflow, workflowCollection);
                    });
                    redisTemplate.opsForValue().increment(key+"-num",(Long)WLNum);
                    redisTemplate.delete(key+"-lock");
                    log.info(String.format("successfully start workflow:%s"),finalMaintainWorkflow.getWorkflowName());
                    return new serverResponse(responseEnum.SWORKFLOW_SUCCESS.getStatusCode(),responseEnum.SWORKFLOW_SUCCESS.getStatusDescription(),finalMaintainWorkflow);
                }
            }
        }
        return null;
    }
    private String generateRedisKey(workflow keyWorkFlow){
        return String.format("%s-%s-%s-%s",keyWorkFlow.getOwnerId(),keyWorkFlow.getProjectName(),keyWorkFlow.getServiceName(),keyWorkFlow.getWorkflowName());
    }

}
