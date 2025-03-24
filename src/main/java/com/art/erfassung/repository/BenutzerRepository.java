package com.art.erfassung.repository;

import com.art.erfassung.model.Benutzer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BenutzerRepository extends JpaRepository<Benutzer, Integer> {
    Optional<Benutzer> findByBenutzername(String benutzername);
}

