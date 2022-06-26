package com.example.yandexmarket.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shop_unit")
public class ShopUnit {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @Type(type = "uuid-char")
    private UUID id;

    @Column
    private String name;

    @Column
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;

    @Column
    @Type(type = "uuid-char")
    private UUID parentId;

    @Column
    private ShopUnitType type;

    @Column
    private Integer price;

    @Column(name = "`left`")
    private Integer left;

    @Column(name = "`right`")
    private Integer right;

    @Column
    private Integer level;
}
