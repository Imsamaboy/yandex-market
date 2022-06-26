package com.example.yandexmarket.dto;


import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class ShopUnitImportRequest {
    @NotNull
    private List<ShopUnitImport> items;

    @NotNull
    private LocalDateTime date;
}
