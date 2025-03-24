package com.art.erfassung.repository;

import com.art.erfassung.model.Erfassung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ErfassungRepository extends JpaRepository<Erfassung, Integer> {
    List<Erfassung> findByStudenten_id(Integer studenten_Id);
}
