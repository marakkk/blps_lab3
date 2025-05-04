package com.blps.lab3.resourceAdapter;

import jakarta.resource.cci.Record;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JiraRequestRecord implements Record {
    private String appName;
    private Long appId;
    private String recordName;
    private String description;

    @Override
    public void setRecordShortDescription(String s) {

    }

    @Override
    public String getRecordShortDescription() {
        return "";
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return null;
    }

}



