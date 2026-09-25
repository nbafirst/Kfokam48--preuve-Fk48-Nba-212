package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.ExerciceCreeDto;
import cm.kfokam48.backend.dto.ExerciceCreationDto;
import cm.kfokam48.backend.service.ServiceExercice;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** POST /api/exercices — contrat : 201 {id, statut} · 400 LIEN_INVALIDE · 409 EXERCICE_DEJA_DEPOSE. */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ServiceExercice service;

    public ExerciceController(ServiceExercice service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ExerciceCreeDto> deposer(@Valid @RequestBody ExerciceCreationDto demande) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.deposer(demande));
    }
}
