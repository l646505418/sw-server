package com.schedulework.server.service;

import com.schedulework.server.entity.workflow;
import com.schedulework.server.vo.serverResponse;

public interface workflowService {
    public abstract serverResponse registWorkflow(workflow addWorkflow);
    public abstract serverResponse updateWorkflow(workflow modifyWorkflow);
}
