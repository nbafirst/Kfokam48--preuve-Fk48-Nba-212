package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.PresenceCreeDto;
import cm.kfokam48.backend.dto.PresenceFormateurDto;
import cm.kfokam48.backend.service.ServicePresence;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/sessions/{id}/presences — contrat complété (EF4/RG12) :
 * le formateur ajoute une présence à la main, marquée « ajouté par le formateur ».
 */
@RestController
@RequestMapping("/api/sessions/{sessionId}/presences")
public class PresenceFormateurController {

    private final ServicePresence service;

    public PresenceFormateurController(ServicePresence service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PresenceCreeDto> ajouter(@PathVariable long sessionId,
                                                   @Valid @RequestBody PresenceFormateurDto demande) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.ajouterParLeFormateur(sessionId, demande.etudiantId()));
    }
}
