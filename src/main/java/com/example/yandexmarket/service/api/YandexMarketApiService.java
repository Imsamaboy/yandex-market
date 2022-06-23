package com.example.yandexmarket.service.api;

import com.example.yandexmarket.dto.ShopUnitImportRequest;
import com.example.yandexmarket.exception.ShopUnitNotFoundException;
import com.example.yandexmarket.exception.ValidationException;
import com.example.yandexmarket.service.YandexMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class YandexMarketApiService {
    private final YandexMarketService service;

    public void saveShopUnit(ShopUnitImportRequest request) throws ValidationException {
        log.info("Trying to save ShopUnits...");
        service.handleShopUnitImportRequest(request);
        log.info("Save or update was successful");
    }

    public void deleteShopUnitById(UUID id) throws ShopUnitNotFoundException {
        log.info("Trying to delete ShopUnit by id: {}...", id);
        service.handleShopUnitDeleteRequest(id);
        log.info("Deleting ShopUnit by id: {} was successful", id);
    }
}
