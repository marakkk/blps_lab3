package com.blps.lab3.repo.googleplay;

import com.blps.lab3.entities.googleplay.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {}
