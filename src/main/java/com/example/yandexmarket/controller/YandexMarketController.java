package com.example.yandexmarket.controller;

import com.example.yandexmarket.dto.ShopUnitImportRequest;
import com.example.yandexmarket.exception.ShopUnitNotFoundException;
import com.example.yandexmarket.exception.ValidationException;
import com.example.yandexmarket.service.api.YandexMarketApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
public class YandexMarketController {
    private final YandexMarketApiService service;

    @PostMapping("/imports")
    public void postImports(@Valid @RequestBody ShopUnitImportRequest request) throws ValidationException {
        service.saveShopUnit(request);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteById(@Valid @PathVariable UUID id) throws ShopUnitNotFoundException {
        service.deleteShopUnitById(id);
    }

    @GetMapping("/nodes/{id}")
    public void getNodesById(@PathVariable UUID id) {

    }

    @GetMapping("/sales")
    public void getSalesList() {

    }

    @GetMapping("/node/{id}/statistic")
    public void getNodeStatisticById(@PathVariable UUID id) {

    }
}
