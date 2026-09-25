package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.ResultatExerciceDto;
import cm.kfokam48.backend.service.ServiceRelecture;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET /api/etudiants/{id}/resultats — EF11/RG10 : notes et commentaires reçus par l'étudiant.
 * L'auteur ne voit jamais le nom du relecteur (RG10).
 */
@RestController
@RequestMapping("/api/etudiants")
public class EtudiantResultatsController {

    private final ServiceRelecture service;

    public EtudiantResultatsController(ServiceRelecture service) {
        this.service = service;
    }

    @GetMapping("/{id}/resultats")
    public ResponseEntity<List<ResultatExerciceDto>> resultats(@PathVariable Long id) {
        return ResponseEntity.ok(service.resultats(id));
    }
}