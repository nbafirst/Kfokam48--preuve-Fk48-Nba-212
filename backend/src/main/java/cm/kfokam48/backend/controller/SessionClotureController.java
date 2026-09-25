package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.SessionClotureeDto;
import cm.kfokam48.backend.service.ServiceSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/sessions/{id}/cloture — EF12/RG8/RG13 : le formateur clôture la session.
 * 200 {id, clotureAt} — 404 SESSION_INCONNUE — 409 SESSION_DEJA_CLOTUREE.
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionClotureController {

    private final ServiceSession service;

    public SessionClotureController(ServiceSession service) {
        this.service = service;
    }

    @PostMapping("/{id}/cloture")
    public ResponseEntity<SessionClotureeDto> cloturer(@PathVariable Long id) {
        SessionClotureeDto dto = service.cloturer(id);
        return ResponseEntity.ok(dto);
    }
}