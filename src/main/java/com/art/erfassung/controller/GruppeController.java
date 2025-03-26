package com.art.erfassung.controller;

import com.art.erfassung.model.Gruppe;
import com.art.erfassung.dao.GruppeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping // Alle Methoden in dieser Klasse betreffen "/dashboard"
public class GruppeController {

    private final GruppeDao gruppeDao;

    @Autowired
    public GruppeController(GruppeDao gruppeDao) {
        this.gruppeDao = gruppeDao;
    }

    @GetMapping("/gruppen")
    public String showDashboard(Model model) {
        List<Gruppe> gruppenList = gruppeDao.findAll();
        model.addAttribute("gruppenListe", gruppenList);
        return "gruppen";
    }
}
