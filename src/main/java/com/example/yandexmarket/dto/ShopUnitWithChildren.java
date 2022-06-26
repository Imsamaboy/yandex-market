package com.example.yandexmarket.dto;

import com.example.yandexmarket.entity.ShopUnit;
import com.example.yandexmarket.entity.ShopUnitType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopUnitWithChildren {
    private UUID id;
    private UUID parentId;
    private String name;
    private ShopUnitType type;
    private Integer price;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer level;
    private List<ShopUnitWithChildren> children;
}
