package cm.kfokam48.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/**
 * B4 — gestion centralisée des erreurs : TOUTES les erreurs sortent au format
 * imposé { code, message }. Aucune stack trace, aucune page d'erreur Spring.
 */
@RestControllerAdvice
public class GestionnaireErreurs {

    private static ResponseEntity<Map<String, String>> erreur(String code, String message, int status) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }

    /** Erreurs métier : le code stable du contrat + le statut portés par l'exception. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> metier(ApiException ex) {
        return erreur(ex.code, ex.getMessage(), ex.status);
    }

    /** Champ manquant ou corps illisible → 400 CHAMP_MANQUANT (contrat POST /api/sessions). */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            HandlerMethodValidationException.class})
    public ResponseEntity<Map<String, String>> validation(Exception ex) {
        String detail = "Champ manquant ou invalide.";
        if (ex instanceof MethodArgumentNotValidException manv
                && manv.getBindingResult().getFieldError() != null) {
            detail = manv.getBindingResult().getFieldError().getDefaultMessage();
        }
        return erreur("CHAMP_MANQUANT", detail, 400);
    }

    /** Ressource inconnue (mauvais chemin) → 404 au format imposé, jamais la page Whitelabel. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> introuvable(NoResourceFoundException ex) {
        return erreur("RESSOURCE_INCONNUE", "Ressource inexistante.", 404);
    }

    /** Filet : toute erreur non prévue sort aussi au format imposé (B4/ENF4). */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> inattendue(Exception ex) {
        return erreur("ERREUR_INATTENDUE", "Une erreur interne est survenue.", 500);
    }
}
