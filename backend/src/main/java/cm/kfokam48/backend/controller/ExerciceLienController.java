package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.ExerciceLienRemplaceDto;
import cm.kfokam48.backend.dto.LienRemplaceDto;
import cm.kfokam48.backend.service.ServiceExercice;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * PUT /api/exercices/{id}/lien — EF6/RG11 : remplacer le lien tant que la relecture n'a pas commencé.
 * 200 {id, statut, lien} — 400 LIEN_INVALIDE — 409 LIEN_VERROUILLE / SESSION_CLOTUREE — 404 EXERCICE_INCONNU.
 */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceLienController {

    private final ServiceExercice service;

    public ExerciceLienController(ServiceExercice service) {
        this.service = service;
    }

    @PutMapping("/{id}/lien")
    public ResponseEntity<ExerciceLienRemplaceDto> remplacerLien(@PathVariable Long id,
                                                                  @RequestParam Long etudiantId,
                                                                  @Valid @RequestBody LienRemplaceDto demande) {
        ExerciceLienRemplaceDto dto = service.remplacerLien(id, etudiantId, demande.lien());
        return ResponseEntity.ok(dto);
    }
}