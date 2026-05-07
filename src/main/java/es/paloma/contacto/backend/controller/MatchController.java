package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Match;
import es.paloma.contacto.backend.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private MatchRepository matchRepository;

    @GetMapping("/mayor/{mayorId}")
    public List<Match> getMatchesByMayorId(@PathVariable Long mayorId) {
        return matchRepository.findByMayorId(mayorId);
    }

    @GetMapping("/voluntario/{voluntarioId}")
    public List<Match> getMatchesByVoluntarioId(@PathVariable Long voluntarioId) {
        return matchRepository.findByVoluntarioId(voluntarioId);
    }

    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        Match nuevoMatch = matchRepository.save(match);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoMatch);
    }
}