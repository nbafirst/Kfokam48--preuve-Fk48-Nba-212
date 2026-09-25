package cm.kfokam48.backend.service;

import cm.kfokam48.backend.dto.TableauLigneDto;
import cm.kfokam48.backend.entity.Etudiant;
import cm.kfokam48.backend.entity.Exercice;
import cm.kfokam48.backend.entity.Presence;
import cm.kfokam48.backend.entity.Relecture;
import cm.kfokam48.backend.entity.SessionCours;
import cm.kfokam48.backend.entity.StatutExercice;
import cm.kfokam48.backend.exception.ApiException;
import cm.kfokam48.backend.repository.EtudiantRepository;
import cm.kfokam48.backend.repository.ExerciceRepository;
import cm.kfokam48.backend.repository.PresenceRepository;
import cm.kfokam48.backend.repository.PromotionRepository;
import cm.kfokam48.backend.repository.RelectureRepository;
import cm.kfokam48.backend.repository.SessionCoursRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EF10/RG15 : le tableau récapitulatif du formateur.
 * Pour chaque étudiant de la promotion : nombre de présences, nombre d'exercices déposés,
 * moyenne des notes reçues (null si aucune note), nombre de relectures en attente.
 * La moyenne est calculée par l'API (RG15, F3 du sujet).
 */
@Service
public class ServiceTableau {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;

    public ServiceTableau(PromotionRepository promotions, EtudiantRepository etudiants,
                          PresenceRepository presences, ExerciceRepository exercices,
                          RelectureRepository relectures) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
    }

    @Transactional(readOnly = true)
    public List<TableauLigneDto> afficher(Long promotionId) {
        promotions.findById(promotionId)
                .orElseThrow(() -> new ApiException("PROMOTION_INCONNUE",
                        "Cette promotion n'existe pas.", 404));

        List<Etudiant> eleves = etudiants.findByPromotionId(promotionId);

        // Présences par étudiant
        List<Presence> toutesPresences = presences.findByPromotionId(promotionId);
        Map<Long, Long> presencesParEtudiant = toutesPresences.stream()
                .collect(Collectors.groupingBy(p -> p.etudiant.id, Collectors.counting()));

        // Exercices déposés par étudiant
        List<Exercice> tousExercices = exercices.findByPromotionId(promotionId);
        Map<Long, Long> exercicesParEtudiant = tousExercices.stream()
                .collect(Collectors.groupingBy(e -> e.etudiant.id, Collectors.counting()));

        // Notes reçues par étudiant (moyenne calculée par l'API)
        Map<Long, Double> moyenneParEtudiant = tousExercices.stream()
                .filter(e -> e.statut == StatutExercice.RELU && e.relecture != null && e.relecture.note != null)
                .collect(Collectors.groupingBy(
                        e -> e.etudiant.id,
                        Collectors.averagingInt(e -> e.relecture.note)
                ));

        // Relectures en attente par étudiant (relecteur = l'étudiant)
        List<Relecture> relecturesEnAttente = relectures.findByRendueAtIsNull();
        Map<Long, Long> relecturesEnAttenteParEtudiant = relecturesEnAttente.stream()
                .collect(Collectors.groupingBy(r -> r.relecteur.id, Collectors.counting()));

        return eleves.stream().map(e -> new TableauLigneDto(
                e.id,
                e.nom,
                presencesParEtudiant.getOrDefault(e.id, 0L).intValue(),
                exercicesParEtudiant.getOrDefault(e.id, 0L).intValue(),
                moyenneParEtudiant.get(e.id),
                relecturesEnAttenteParEtudiant.getOrDefault(e.id, 0L).intValue()
        )).toList();
    }
}