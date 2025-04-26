package com.blps.lab2.entities.googleplay;

import com.blps.lab2.enums.DevAccount;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "developer")
public class Developer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;

    private String email;
    private boolean paymentProfile;

    @Enumerated(EnumType.STRING)
    private DevAccount accStatus;

    private double earnings;

    @OneToMany(mappedBy = "developer", fetch = FetchType.EAGER)
    private List<App> apps;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Developer developer = (Developer) o;
        return Objects.equals(id, developer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}