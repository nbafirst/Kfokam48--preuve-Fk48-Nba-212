package cm.kfokam48.backend.controller;

import cm.kfokam48.backend.dto.EtudiantPromotionDto;
import cm.kfokam48.backend.service.ServicePromotion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET /api/promotions/{id}/etudiants — liste des étudiants d'une promotion (Q1, EF2, H3).
 * L'étudiant choisit son nom dans la liste.
 */
@RestController
@RequestMapping("/api/promotions")
public class PromotionEtudiantsController {

    private final ServicePromotion service;

    public PromotionEtudiantsController(ServicePromotion service) {
        this.service = service;
    }

    @GetMapping("/{id}/etudiants")
    public ResponseEntity<List<EtudiantPromotionDto>> listerEtudiants(@PathVariable Long id) {
        return ResponseEntity.ok(service.listerEtudiants(id));
    }
}