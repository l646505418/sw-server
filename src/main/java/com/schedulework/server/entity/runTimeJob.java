package com.schedulework.server.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author:Li Jinming
 * @Description:
 * @date:2023-02-22
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collation = "runtimeJobInfo")
public class runTimeJob {
    private  job job;
    private String parentJobName;
    private workflow workflow;
    private String status;
}
