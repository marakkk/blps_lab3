package com.blps.lab3.repo.googleplay;

import com.blps.lab3.entities.googleplay.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {}
