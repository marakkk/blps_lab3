package com.blps.lab3.resourceAdapter;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JiraTransitionsResponse {
    private List<Transition> transitions;

    @Getter
    @Setter
    public static class Transition {
        private String id;
        private String name;
        private String status;
    }

}