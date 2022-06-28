package com.example.yandexmarket.service;

import com.example.yandexmarket.dto.ShopUnitWithChildren;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class YandexMarketService {
    private final ShopUnitRepository repository;

    /* path: /imports
        1. добавляем все ShopUnit в бд
        2. строим дерево\ребилдим
        3. считаем все mean price начиная с root и обновляем поля в бд
     */
    @Transactional
    public void handleShopUnitImportRequest(ShopUnitImportRequest request) throws ValidationException, ShopUnitNotFoundException {
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
                        .left(null)
                        .right(null)
                        .level(null)
                        .build();
                repository.save(unit);
            }
        }
        // TODO: почему процедура вызывается до insert value?
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

    /* path: /delete/{id}
        1. Ищем поддерево
        2. Удаляем найденное поддерево
        3. Ребилдим дерево, считаем заново mean price
    */
    @Transactional
    public void handleShopUnitDeleteRequest(UUID id) throws ShopUnitNotFoundException {
        rebuild();
        Optional<ShopUnit> unitOptional = repository.findById(id);
        if (unitOptional.isPresent()) {
            ShopUnit unit = unitOptional.get();
            if (unit.getType() == ShopUnitType.CATEGORY) {
                List<ShopUnit> children = repository.findAllChildrenByParentId(unit.getId().toString());
                children.forEach(child -> {
                    log.info("Deleting ShopUnit with ID: {}", child.getId());
                    repository.delete(child);
                });
                log.info("Deleting Parent ShopUnit with ID: {}", unit.getId());
                repository.delete(unit);
            } else repository.delete(unit);
            rebuild();
        } else throw new ShopUnitNotFoundException(id);
    }

    private void rebuild() throws ShopUnitNotFoundException {
        repository.rebuildTree();
        List<ShopUnit> allRoots = repository.findRoots();
        for (ShopUnit root: allRoots) {
            ShopUnitWithChildren hierarchicalRoot = generateShopUnitWithChildren(root.getId());
            System.out.println(hierarchicalRoot);
            countAverage(hierarchicalRoot);
        }

    }

    // path: /node/{id}
    public ShopUnitWithChildren handleGetRequest(UUID id) throws ShopUnitNotFoundException {
        rebuild();
        return generateShopUnitWithChildren(id);
    }

    private ShopUnitWithChildren generateShopUnitWithChildren(UUID id) throws ShopUnitNotFoundException {
        ShopUnit root = repository.findById(id).orElseThrow(() -> new ShopUnitNotFoundException(id));
        // все дети root включая root
        List<ShopUnit> subtree = repository.findAllChildrenByParentBorders(root.getLeft(), root.getRight());
        // subtree.forEach(System.out::println);
        List<ShopUnitWithChildren> rootChildrenWithChildrenField = new ArrayList<>();
        for (ShopUnit unit : subtree) {
            List<ShopUnitWithChildren> children = unit.getType() == ShopUnitType.CATEGORY ? new ArrayList<>(): null;
            ShopUnitWithChildren shopUnitWithChildren = new ShopUnitWithChildren(unit.getId(),
                    unit.getParentId(),
                    unit.getName(),
                    unit.getType(),
                    unit.getPrice(),
                    unit.getDate(),
                    unit.getLevel(),
                    children);
            rootChildrenWithChildrenField.add(shopUnitWithChildren);
        }

        int maxLevel = subtree.stream()
                .map(ShopUnit::getLevel)
                .mapToInt(unit -> unit)
                .filter(unit -> unit >= 0)
                .max()
                .orElse(0);

        for (int currentLevel = root.getLevel() + 1; currentLevel < maxLevel + 1; currentLevel++) {
            int finalCurrentLevel = currentLevel;
            for (ShopUnitWithChildren child: rootChildrenWithChildrenField.stream().filter(x -> x.getLevel() == finalCurrentLevel).toList()) {
                List<ShopUnitWithChildren> list = rootChildrenWithChildrenField.stream().filter(x -> x.getId().equals(child.getParentId())).toList();
                if (!list.isEmpty()) {
                    ShopUnitWithChildren parent = list.get(0);
                    if (parent.getChildren() != null) {
                        parent.getChildren().add(child);
                    } else {
                        parent.setChildren(null);
                    }
                } else {
                    // Что тут делать, если плохой случай какой-то?
                    System.out.println("FUCK");
                }
            }
        }
        return rootChildrenWithChildrenField.stream().filter(x -> x.getId().equals(root.getId())).toList().get(0);
    }

    // TODO: сделать для нескольких root
    // TODO: СЕЙЧАС НЕПРАВИЛЬНО СЧИТАЮТСЯ MEAN VALUE
    static double ans = 0.0;
    static List<Double> means = new ArrayList<>();
    private double[] countAverage(ShopUnitWithChildren root) {
        // Checks if current node is not null and doesn't have any children
        // Если null => offer
        if (root.getChildren() == null) {
            means.add(Double.valueOf(root.getPrice()));
            System.out.println("IF STATEMENT: " + root.getPrice());
            return new double[] { root.getPrice(), 1 };
        } else if (root.getChildren().size() == 0) {
            root.setPrice(0);
            return new double[] { 0, 1 };
        }
        // Stores sum of its subtree in index 0 and count number of nodes in index 1
        double[] childResult = new double[2];
        // Traverse all children of the current node
        for (ShopUnitWithChildren child : root.getChildren()) {
            System.out.println("CHILD: " + child.getName() + " PRICE: " + child.getPrice());
            // Recursively calculate average subtrees among its children
            double[] childTotal = countAverage(child);
            // Increment sum by sum of its child's subtree
            childResult[0] = childResult[0] + childTotal[0];
            // Increment number of nodes by its child's node
            childResult[1] = childResult[1] + childTotal[1];
//            childResult[1] = childResult[1] + childTotal[1];
        }
        // Increment sum by current node's value
        double sum = root.getPrice() != null ? childResult[0] + root.getPrice() : childResult[0];
//        double sum = childResult[0]; // + root.getPrice() : childResult[0];
        System.out.println("OUT: " + sum + " " + Arrays.toString(childResult));
//        double sum = childResult[0] + root.getPrice();
        // Increment number of nodes by one
        double count = childResult[1]; // + 1
        // Take maximum of ans and current node's average
        root.setPrice((int) Math.floor(sum / count));
        System.out.println("ROOT: " + root.getName() + " PRICE: " + root.getPrice());
        means.add(sum / count);
        // Finally return pair of {sum, count}
        return new double[] { sum, count };
    }

    // path: /sales
    public List<ShopUnit> handleSalesRequest(LocalDateTime date) {
        List<ShopUnit> allOffers = repository.findAllOffers();
        return allOffers.stream()
                        .filter(offer -> offer.getDate().isAfter(date.minusDays(1)) && offer.getDate().isBefore(date))
                        .collect(Collectors.toList());
    }
}
