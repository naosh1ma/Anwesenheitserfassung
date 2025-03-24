package com.art.erfassung.dao;

import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.StudentenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentenDao {

    private final StudentenRepository studentenRepository;

    @Autowired
    public StudentenDao(StudentenRepository studentenRepository) {
        this.studentenRepository = studentenRepository;
    }

    public List<Studenten> getAlleStudenten(){
        return studentenRepository.findAll();
    }
}
