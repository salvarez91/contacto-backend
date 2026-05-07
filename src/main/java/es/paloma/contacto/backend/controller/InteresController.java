package es.paloma.contacto.backend.controller;

import es.paloma.contacto.backend.model.Interes;
import es.paloma.contacto.backend.repository.InteresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/intereses")
public class InteresController {

    @Autowired
    private InteresRepository interesRepository;

    @GetMapping
    public List<Interes> getAll() {
        return interesRepository.findAll();
    }

    @PostMapping
    public Interes create(@RequestBody Interes interes) {
        return interesRepository.save(interes);
    }
}