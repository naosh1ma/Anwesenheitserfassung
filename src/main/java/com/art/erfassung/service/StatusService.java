package com.art.erfassung.service;

import com.art.erfassung.model.Status;
import com.art.erfassung.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusService {

    private final StatusRepository statusRepository;

    @Autowired
    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public List<Status> findAll() {
        return statusRepository.findAll();
    }

    public Status findOrThrow(Integer id) {
        return statusRepository.findById(id).orElseThrow();
    }


}
