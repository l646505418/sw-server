package com.schedulework.server.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;

/**
 * @author:Li Jinming
 * @Description:
 * @date:2023-02-22
 */

@Data
@AllArgsConstructor
@Document(collation = "workflowInfo")
public class workflow {
    private String workflowName;
    private String serviceName;
    private String projectName;
    private Long ownerId;
    private String status;
    private HashMap<String,runTimeJob> ownJobs;

    public workflow() {
        this.status="REGIST";
    }
}
