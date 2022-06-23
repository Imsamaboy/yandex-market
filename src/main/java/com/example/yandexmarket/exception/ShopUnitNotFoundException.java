package com.example.yandexmarket.exception;

import java.util.UUID;

public class ShopUnitNotFoundException extends Exception {
    public ShopUnitNotFoundException(UUID id) {
        super(String.format("ShopUnit %s not found", id));
    }
}
