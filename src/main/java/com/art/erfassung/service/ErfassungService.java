package com.art.erfassung.service;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.repository.ErfassungRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ErfassungService {

    private final ErfassungRepository erfassungRepository;


    public ErfassungService(ErfassungRepository erfassungRepository) {
        this.erfassungRepository = erfassungRepository;
    }

    public List<Erfassung> getErfassungByStudenten_Id(Integer studenten_id){
        return erfassungRepository.findByStudenten_id(studenten_id);
    }

    public void saveErfassung(Erfassung erfassung){
        erfassungRepository.save(erfassung);
    }
}
