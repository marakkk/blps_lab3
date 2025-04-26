package com.blps.lab2.repo.googleplay;

import com.blps.lab2.entities.googleplay.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {}
