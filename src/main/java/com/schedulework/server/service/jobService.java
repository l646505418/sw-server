package com.schedulework.server.service;

import com.schedulework.server.entity.job;
import com.schedulework.server.entity.runTimeJob;
import com.schedulework.server.entity.workflow;
import com.schedulework.server.vo.serverResponse;

public interface jobService {
    public  abstract serverResponse registJob(job addJob);
    public  abstract serverResponse updateJob(job modifyJob);
    public  abstract workflow updateStatus(runTimeJob updateJob);
}
