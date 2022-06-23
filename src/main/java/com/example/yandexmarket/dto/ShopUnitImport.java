package com.example.yandexmarket.dto;

import com.example.yandexmarket.entity.ShopUnitType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class ShopUnitImport {
    @NotNull
    private UUID id;

    @NotNull
    private String name;

    @NotNull
    private ShopUnitType type;

    private UUID parentId;

    private Integer price;
}
