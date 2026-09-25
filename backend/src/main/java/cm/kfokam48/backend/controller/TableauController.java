package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.TableauLigneDto;
import cm.kfokam48.backend.service.ServiceTableau;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET /api/tableau — EF10/RG15 : le tableau récapitulatif du formateur.
 * Par étudiant : présences, exercices déposés, moyenne des notes reçues, relectures en attente.
 * La moyenne est calculée par l'API (RG15, F3 du sujet).
 */
@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final ServiceTableau service;

    public TableauController(ServiceTableau service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TableauLigneDto>> afficher(@RequestParam Long promotionId) {
        return ResponseEntity.ok(service.afficher(promotionId));
    }
}