package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.RelectureEnAttenteDto;
import cm.kfokam48.backend.dto.RelectureRendueDto;
import cm.kfokam48.backend.service.ServiceRelecture;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contrôleur pour les relectures (EF8, EF9, RG14).
 * Endpoints : POST /api/relectures/{id}, POST /api/relectures/{id}/correction,
 * GET /api/relectures/en-attente.
 */
@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final ServiceRelecture service;

    public RelectureController(ServiceRelecture service) {
        this.service = service;
    }

    /**
     * POST /api/relectures/{id} — EF8 : le relecteur rend sa note et son commentaire.
     * 200 si OK, 400 NOTE_INVALIDE, 403 AUTO_RELECTURE, 409 RELECTURE_DEJA_RENDUE.
     */
    @PostMapping("/{id}")
    public ResponseEntity<Void> rendre(@PathVariable Long id,
                                       @RequestParam Long relecteurId,
                                       @Valid @RequestBody RelectureRendueDto demande) {
        service.rendre(id, relecteurId, demande);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/relectures/{id}/correction — EF9 : le relecteur corrige sa note (RG13, C1).
     * 200 si OK, 400 NOTE_INVALIDE, 409 SESSION_CLOTUREE, 404 RELECTURE_INCONNUE.
     */
    @PostMapping("/{id}/correction")
    public ResponseEntity<Void> corriger(@PathVariable Long id,
                                         @RequestParam Long relecteurId,
                                         @Valid @RequestBody RelectureRendueDto demande) {
        service.corriger(id, relecteurId, demande);
        return ResponseEntity.ok().build();
    }

    /**
     * GET /api/relectures/en-attente — relectures qu'un étudiant doit encore rendre (EF8, RG14).
     */
    @GetMapping("/en-attente")
    public ResponseEntity<List<RelectureEnAttenteDto>> enAttente(@RequestParam Long relecteurId) {
        return ResponseEntity.ok(service.enAttente(relecteurId));
    }
}