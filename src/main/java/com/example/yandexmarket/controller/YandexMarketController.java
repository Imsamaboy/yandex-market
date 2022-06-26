package com.example.yandexmarket.controller;

import com.example.yandexmarket.dto.ShopUnitWithChildren;
import com.example.yandexmarket.dto.ShopUnitImportRequest;
import com.example.yandexmarket.entity.ShopUnit;
import com.example.yandexmarket.exception.ShopUnitNotFoundException;
import com.example.yandexmarket.exception.ValidationException;
import com.example.yandexmarket.service.api.YandexMarketApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class YandexMarketController {
    private final YandexMarketApiService service;

    @PostMapping("/imports")
    public void postImports(@Valid @RequestBody ShopUnitImportRequest request) throws ValidationException, ShopUnitNotFoundException {
        service.saveShopUnit(request);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteById(@Valid @PathVariable UUID id) throws ShopUnitNotFoundException {
        service.deleteShopUnitById(id);
    }

    @GetMapping("/nodes/{id}")
    public ShopUnitWithChildren getNodesById(@Valid @PathVariable UUID id) throws ShopUnitNotFoundException {
        return service.getShopUnitNodesById(id);
    }

    @GetMapping("/sales")
    public List<ShopUnit> getSalesList(@Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return service.getSalesShopUnitListByDate(date);
    }

    @GetMapping("/node/{id}/statistic")
    public void getNodeStatisticById(@PathVariable UUID id,
                                     @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime leftDate,
                                     @Valid @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime rightDate) {

    }
}
