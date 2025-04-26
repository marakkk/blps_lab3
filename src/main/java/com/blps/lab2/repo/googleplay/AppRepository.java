package com.blps.lab2.repo.googleplay;

import com.blps.lab2.entities.googleplay.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {}
