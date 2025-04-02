package com.art.erfassung.service;

import com.art.erfassung.model.Erfassung;
import com.art.erfassung.model.Studenten;
import com.art.erfassung.repository.ErfassungRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StatistikService {

    private final ErfassungRepository erfassungRepository;

    public StatistikService(ErfassungRepository erfassungRepository) {
        this.erfassungRepository = erfassungRepository;
    }

    public StatistikErgebnis berechneFuerStudent(Integer studentId) {
        List<Erfassung> erfassungen = erfassungRepository.findByStudenten_id(studentId);
        long total = erfassungen.size();

        Map<String, Long> statusCount = erfassungen.stream()
                .collect(Collectors.groupingBy(e -> e.getStatus().getBezeichnung(), Collectors.counting()));

        long verspaetet = erfassungen.stream()
                .filter(e -> Optional.ofNullable(e.getKommentar()).orElse("").toLowerCase().contains("verspätung"))
                .count();

        long anwesend = statusCount.getOrDefault("Anwesend", 0L);
        double prozent = (total > 0) ? ((double) anwesend / total * 100.0) : 0.0;

        Studenten student = erfassungen.isEmpty() ? null : erfassungen.get(0).getStudenten();

        return new StatistikErgebnis(student, prozent,
                statusCount.getOrDefault("Entschuldigt", 0L),
                statusCount.getOrDefault("Unentschuldigt", 0L),
                statusCount.getOrDefault("Krank", 0L),
                verspaetet);
    }

    public record StatistikErgebnis(
            Studenten student,
            double gesamtAnwesenheit,
            long entschuldigt,
            long unentschuldigt,
            long krank,
            long verspaetungen) { }
}
