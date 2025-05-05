package com.blps.lab3.resourceAdapter;

import jakarta.resource.cci.Record;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@AllArgsConstructor
public class JiraStatusUpdateRecord implements Record {
    private String issueId;
    private String status;
    private String username;
    private String token;

    @Override
    public String getRecordName() {
        return "";
    }

    @Override
    public void setRecordName(String s) {

    }

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