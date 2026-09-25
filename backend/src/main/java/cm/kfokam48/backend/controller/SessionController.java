package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.SessionCreeDto;
import cm.kfokam48.backend.dto.SessionCreationDto;
import cm.kfokam48.backend.service.ServiceSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final ServiceSession service;

    public SessionController(ServiceSession service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SessionCreeDto> ouvrir(@Valid @RequestBody SessionCreationDto demande) {
        SessionCreeDto cree = service.ouvrir(demande);
        return ResponseEntity
                .created(URI.create("/api/sessions/" + cree.id()))
                .body(cree);
    }
}
