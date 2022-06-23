package com.example.yandexmarket.service;

import com.example.yandexmarket.dto.ShopUnitImport;
import com.example.yandexmarket.dto.ShopUnitImportRequest;
import com.example.yandexmarket.entity.ShopUnit;
import com.example.yandexmarket.entity.ShopUnitType;
import com.example.yandexmarket.exception.ShopUnitNotFoundException;
import com.example.yandexmarket.exception.ValidationException;
import com.example.yandexmarket.repository.ShopUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class YandexMarketService {
    private final ShopUnitRepository repository;

    // TODO: надо пилить проверки по тз
    @Transactional
    public void handleShopUnitImportRequest(ShopUnitImportRequest request) throws ValidationException {
        Set<UUID> uuids = new HashSet<>();
        for (ShopUnitImport shopUnitImport : request.getItems()) {
            // Проверка на содержание двух элементов с одинаковым id в одном запросе
            if (uuids.contains(shopUnitImport.getId()))
                throw new ValidationException("ShopUnitImport ID shouldn't be repeated");
            else uuids.add(shopUnitImport.getId());

            // Проверка на параметры импорта
            checkShopUnitForBadParams(shopUnitImport);

            // Если всё окей, добавляем\обновляем бд
            Optional<ShopUnit> unitOptional = repository.findById(shopUnitImport.getId());
            // Если ShopUnit уже в бд
            if (unitOptional.isPresent()) {
                ShopUnit updateShopUnit = unitOptional.get();
                // Проверка на: изменение типа элемента с товара на категорию или... не допускается
                if (updateShopUnit.getType() != shopUnitImport.getType()) {
                    log.error("ShopUnitImport with UUID: {} type should be the same as before", shopUnitImport.getId());
                    throw new ValidationException("ShopUnitImport Type Exception");
                }
                updateShopUnit.setName(shopUnitImport.getName());
                updateShopUnit.setPrice(shopUnitImport.getPrice());
                updateShopUnit.setParentId(shopUnitImport.getParentId());
                updateShopUnit.setDate(request.getDate());
                repository.save(updateShopUnit);
            } else {
                ShopUnit unit = ShopUnit.builder()
                        .id(shopUnitImport.getId())
                        .name(shopUnitImport.getName())
                        .type(shopUnitImport.getType())
                        .price(shopUnitImport.getPrice())
                        .parentId(shopUnitImport.getParentId())
                        .date(request.getDate())
                        .build();
                repository.save(unit);
            }
        }
    }

    private void checkShopUnitForBadParams(ShopUnitImport shopUnitImport) throws ValidationException {
        // Проверка на: название элемента не может быть NULL
        if (shopUnitImport.getName() == null) {
            log.error("ShopUnitImport with UUID: {} name shouldn't be NULL", shopUnitImport.getId());
            throw new ValidationException("ShopUnitImport Name Exception");
        }

        // Проверка на: родителем товара или категории может быть только категория
        if (shopUnitImport.getParentId() != null) {
            Optional<ShopUnit> parentUnit = repository.findById(shopUnitImport.getParentId());
            if (parentUnit.isPresent() && parentUnit.get().getType() != ShopUnitType.CATEGORY) {
                log.error("ShopUnitImport ParentId: {} | The parent of a OFFER or CATEGORY can only be a CATEGORY",
                        shopUnitImport.getParentId());
                throw new ValidationException("ShopUnitImport Parent Id Exception");
            }
        }

        // Проверки для категорий
        if (shopUnitImport.getType() == ShopUnitType.CATEGORY) {
            if (shopUnitImport.getPrice() != null) {
                log.error("ShopUnitImport with UUID: {} price for CATEGORY should be NULL", shopUnitImport.getId());
                throw new ValidationException("ShopUnitImport Price Exception for CATEGORY");
            }
        }

        // Проверки для товаров
        if (shopUnitImport.getType() == ShopUnitType.OFFER) {
            if (shopUnitImport.getPrice() == null || shopUnitImport.getPrice() < 0) {
                log.error("ShopUnitImport with UUID: {} price for OFFER shouldn't be NULL or lower 0", shopUnitImport.getId());
                throw new ValidationException("ShopUnitImport Price Exception for OFFER");
            }
        }
    }

    @Transactional
    public void handleShopUnitDeleteRequest(UUID id) throws ShopUnitNotFoundException {
        // TODO: Если удаляем категорию, то надо удалить всех её детей
        Optional<ShopUnit> unitOptional = repository.findById(id);
        if (unitOptional.isPresent()) {
            ShopUnit unit = unitOptional.get();

            // Если категория, то ищем всех детей
            if (unit.getType() == ShopUnitType.CATEGORY) {
                List<ShopUnit> children = getAllShopUnitChildren(unit);
                children.forEach(child -> {
                    log.info("Deleting ShopUnit with ID: {}", child.getId());
                    repository.delete(child);
                });
            } else {
                repository.delete(unit);
            }

        } else throw new ShopUnitNotFoundException(id);
    }
    private List<ShopUnit> getAllShopUnitChildren(ShopUnit start) {
        ArrayDeque<ShopUnit> stack = new ArrayDeque<>();
        // TODO: Подумать как заменить
        List<ShopUnit> isVisited = new ArrayList<>();
        stack.push(start);
        List<ShopUnit> allChildren = new ArrayList<>();
        while (!stack.isEmpty()) {
            ShopUnit current = stack.pop();
            if (!isVisited.contains(current)) {
                isVisited.add(current);
                allChildren.add(current);
                for (ShopUnit dest : repository.findAllByParentId(current.getId().toString())) {
                    if (!isVisited.contains(dest))
                        stack.push(dest);
                }
            }
        }
        return allChildren;
    }
}
