package com.example.yandexmarket.repository;

import com.example.yandexmarket.entity.ShopUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShopUnitRepository extends JpaRepository<ShopUnit, UUID> {
//    @Query(value = "select * from shop_unit where shop_unit.parent_id = ?1", nativeQuery = true)
    // SET SESSION cte_max_recursion_depth = 1000000; - сделать в докере для бд
    @Query(value = "with recursive children as " +
            "(select id, name, type, parent_id FROM shop_unit WHERE parent_id=?1 " +
            "UNION ALL SELECT child.id, child.name, child.type, child.parent_id " +
            "FROM shop_unit child JOIN children g ON g.id = child.parent_id)" +
            "SELECT * FROM children g JOIN shop_unit parent ON g.parent_id=parent.id",
            nativeQuery = true)
    List<ShopUnit> findAllChildrenByParentId(String id);
}
