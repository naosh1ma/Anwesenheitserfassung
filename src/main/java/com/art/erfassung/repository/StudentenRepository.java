package com.art.erfassung.repository;

import com.art.erfassung.model.Studenten;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentenRepository extends JpaRepository<Studenten, Integer> {
    List<Studenten> findByGruppeId(Integer gruppe_id);
}
