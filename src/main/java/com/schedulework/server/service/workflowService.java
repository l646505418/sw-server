package com.schedulework.server.service;

import com.schedulework.server.entity.workflow;
import com.schedulework.server.vo.serverResponse;

public interface workflowService {
    public abstract serverResponse registWorkflow(workflow addWorkflow);
    public abstract serverResponse updateWorkflow(workflow updateWorkflow);
    public abstract serverResponse startWorkflow(workflow runWorkflow);
    public abstract serverResponse modifyWorkflow(workflow modifyWorkflow);
    public abstract serverResponse finishWorkflow(workflow finishWorkflow);
}
