package com.art.erfassung.repository;

import com.art.erfassung.model.Gruppe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GruppeRepository extends JpaRepository<Gruppe, Integer> {
}
