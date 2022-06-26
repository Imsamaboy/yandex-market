package com.example.yandexmarket.service.api;

import com.example.yandexmarket.dto.ShopUnitWithChildren;
import com.example.yandexmarket.dto.ShopUnitImportRequest;
import com.example.yandexmarket.entity.ShopUnit;
import com.example.yandexmarket.exception.ShopUnitNotFoundException;
import com.example.yandexmarket.exception.ValidationException;
import com.example.yandexmarket.service.YandexMarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class YandexMarketApiService {
    private final YandexMarketService service;

    public void saveShopUnit(ShopUnitImportRequest request) throws ValidationException, ShopUnitNotFoundException {
        log.info("Trying to save ShopUnits...");
        service.handleShopUnitImportRequest(request);
        log.info("Save or update was successful");
    }

    public void deleteShopUnitById(UUID id) throws ShopUnitNotFoundException {
        log.info("Trying to delete ShopUnit by id: {}...", id);
        service.handleShopUnitDeleteRequest(id);
        log.info("Deleting ShopUnit by id: {} was successful", id);
    }

    public ShopUnitWithChildren getShopUnitNodesById(UUID id) throws ShopUnitNotFoundException {
        log.info("Trying to get ShopUnit by id: {}...", id);
        return service.handleGetRequest(id);
    }

    public List<ShopUnit> getSalesShopUnitListByDate(LocalDateTime date) {
        log.info("Trying to get Sales List by date: {}...", date);
        return service.handleSalesRequest(date);
    }
}
