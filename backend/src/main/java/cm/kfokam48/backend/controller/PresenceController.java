package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.PresenceCreeDto;
import cm.kfokam48.backend.dto.PresenceCreationDto;
import cm.kfokam48.backend.service.ServicePresence;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final ServicePresence service;

    public PresenceController(ServicePresence service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PresenceCreeDto> marquer(@Valid @RequestBody PresenceCreationDto demande) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.marquer(demande));
    }
}
