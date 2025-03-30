package com.art.erfassung.service;

import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.StudentenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentenService {

    private final StudentenRepository studentenRepository;

    @Autowired
    public StudentenService(StudentenRepository studentenRepository) {
        this.studentenRepository = studentenRepository;
    }

    public List<Studenten> getAlleStudenten(){
        return studentenRepository.findAll();
    }
}
