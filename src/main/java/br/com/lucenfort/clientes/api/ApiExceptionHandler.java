package br.com.lucenfort.clientes.api;

import br.com.lucenfort.clientes.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> handle(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ErrorResponse(ex.getCode(), ex.getMessage(), OffsetDateTime.now(), Map.of()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (first, ignored) -> first));
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", "Dados invalidos", OffsetDateTime.now(), fields));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Erro interno inesperado", OffsetDateTime.now(), Map.of()));
    }
    public record ErrorResponse(String code, String message, OffsetDateTime timestamp, Map<String, String> fields) {}
}
