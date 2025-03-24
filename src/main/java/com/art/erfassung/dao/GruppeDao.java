package com.art.erfassung.dao;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.repository.GruppeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GruppeDao {

    private final GruppeRepository gruppeRepository;

    @Autowired
    public GruppeDao(GruppeRepository gruppeRepository) {
        this.gruppeRepository = gruppeRepository;
    }

    public List<Gruppe> findAll() {
        return gruppeRepository.findAll(); // Holt alle Gruppen aus der Datenbank
    }
}
