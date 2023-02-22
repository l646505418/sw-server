package com.schedulework.server.service;

import com.schedulework.server.entity.job;
import com.schedulework.server.vo.serverResponse;

public interface jobService {
    public  abstract serverResponse registJob(job addJob);
    public  abstract serverResponse updateJob(job modifyJob);
}
