package com.art.erfassung.service;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Status;
import com.art.erfassung.repository.ErfassungRepository;
import com.art.erfassung.repository.StatusRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ErfassungService {

    private final ErfassungRepository erfassungRepository;
    private final StatusRepository statusRepository;

    public ErfassungService(ErfassungRepository erfassungRepository, StatusRepository statusRepository) {
        this.erfassungRepository = erfassungRepository;
        this.statusRepository = statusRepository;
    }

    public List<Erfassung> getByGruppeUndMonat(Integer gruppeId, LocalDate monatStart, LocalDate monatEnde) {
        return erfassungRepository.findByStudenten_GruppeIdAndDatumBetween(gruppeId, monatStart, monatEnde);
    }

    public void updateErfassung(Integer erfassungId, Integer statusId, String kommentar) {
        Optional<Erfassung> optionalErfassung = erfassungRepository.findById(erfassungId);
        optionalErfassung.ifPresent(erfassung -> {
            Status neuerStatus = statusRepository.findById(statusId).orElseThrow();
            erfassung.setStatus(neuerStatus);
            erfassung.setKommentar(kommentar);
            erfassungRepository.save(erfassung);
        });
    }

    public void save(Erfassung erfassung) {
        erfassungRepository.save(erfassung);
    }

    public Optional<Erfassung> findByStudentAndDate(Integer studentId, LocalDate date) {
        return erfassungRepository.findByStudenten_IdAndDatum(studentId, date);
    }

    public Optional<Erfassung> findById(Integer id) {
        return erfassungRepository.findById(id);
    }
}
