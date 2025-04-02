package com.art.erfassung.service;

import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.StudentenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentenService {

    private final StudentenRepository studentenRepository;

    @Autowired
    public StudentenService(StudentenRepository studentenRepository) {
        this.studentenRepository = studentenRepository;
    }

    public List<Studenten> findByGruppeId(Integer gruppeId) {
        return studentenRepository.findByGruppeId(gruppeId);
    }

    public Studenten findOrThrow(Integer id) {
        return studentenRepository.findById(id).orElseThrow();
    }
}
