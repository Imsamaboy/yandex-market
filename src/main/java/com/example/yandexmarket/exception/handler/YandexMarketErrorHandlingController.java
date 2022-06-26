package com.example.yandexmarket.exception.handler;

import com.example.yandexmarket.exception.ShopUnitNotFoundException;
import com.example.yandexmarket.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class YandexMarketErrorHandlingController {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Void> handleValidationException(ValidationException ex) {
        log.error("ValidationException " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(ShopUnitNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleShopUnitNotFoundException(ShopUnitNotFoundException ex) {
        log.error("ShopUnitNotFoundException " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("code", "404", "message", "Item not found"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("MethodArgumentTypeMismatchException " + ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(Map.of("code", "400", "message", "Validation Failed"));
    }
}
