package cm.kfokam48.backend.exception;

/**
 * Erreur métier portant le code stable du contrat d'API
 * (ex. CHAMP_MANQUANT, CODE_EXPIRE) et le statut HTTP associé.
 * Un seul type d'exception, traité par le @RestControllerAdvice (B4).
 */
public class ApiException extends RuntimeException {

    public final String code;
    public final int status;

    public ApiException(String code, String message, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
