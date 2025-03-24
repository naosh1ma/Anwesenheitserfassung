package com.art.erfassung.controller;

import com.art.erfassung.dao.StudentenDao;
import com.art.erfassung.model.Studenten;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/anwesenheit")
public class StudentenController {

    private final StudentenDao studentenDao;



    @Autowired
    public StudentenController(StudentenDao studentenDao) {
        this.studentenDao = studentenDao;
    }

    @GetMapping
    public String showStudents(Model model) {
        List<Studenten> studenten = studentenDao.getAlleStudenten();
        model.addAttribute("studenten", studenten);
        model.addAttribute("datum", LocalDate.now()); // Heutiges Datum hinzufügen
        return "anwesenheit-backup"; // Zeigt die anwesenheit-backup.html Seite
    }

















    // define @PostConstruct to load the student data ... only once!
    @PostConstruct
    public void loadData() {

        List<Studenten> theStudentens = new ArrayList<>();
        theStudentens = new ArrayList<>();

        // TODO Studenten aus DB laden
        theStudentens.add(new Studenten());
        theStudentens.add(new Studenten());
        theStudentens.add(new Studenten());
    }




    // define endpoint for "/student/{studentId}" - return student an index

    @GetMapping("/student/{studentId}")
    public Studenten getStudent(@PathVariable int studentId) {
        List<Studenten> theStudentens =new ArrayList<>();

        return theStudentens.get(studentId);
    }
}
