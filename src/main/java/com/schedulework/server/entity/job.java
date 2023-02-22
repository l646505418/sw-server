package com.schedulework.server.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * @author:Li Jinming
 * @Description:
 * @date:2023-02-22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collation = "jobInfo")
public class job {
    private Long jobId;
    private String jobName;
    private Long ownerId;
    private String description;
    private Map<String,String> inputParams;
    private Map<String,String> outputParams;
}
