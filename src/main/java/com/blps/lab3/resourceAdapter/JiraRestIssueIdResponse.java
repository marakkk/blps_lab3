package com.blps.lab3.resourceAdapter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class JiraRestIssueIdResponse {
    private final String id;
    private final String name;
}
