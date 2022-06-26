package com.example.yandexmarket.repository;

import com.example.yandexmarket.entity.ShopUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import javax.persistence.NamedStoredProcedureQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopUnitRepository extends JpaRepository<ShopUnit, UUID> {
//    @Query(value = "select * from shop_unit where shop_unit.parent_id = ?1", nativeQuery = true)
    // SET SESSION cte_max_recursion_depth = 1000000; - сделать в докере для бд
    @Query(value = "with recursive children as " +
            "(select id, name, type, parent_id, price FROM shop_unit WHERE parent_id=?1 " +
            "UNION ALL SELECT child.id, child.name, child.type, child.parent_id, child.price " +
            "FROM shop_unit child JOIN children g ON g.id = child.parent_id)" +
            "SELECT * FROM children g JOIN shop_unit parent ON g.parent_id=parent.id",
            nativeQuery = true)
    List<ShopUnit> findAllChildrenByParentId(String id);

    @Query(value = "select * from shop_unit where shop_unit.parent_id = ?1", nativeQuery = true)
    Optional<ShopUnit> findByParentId(String id);

    @Query(value = "select * from shop_unit where shop_unit.parent_id IS NULL", nativeQuery = true)
    List<ShopUnit> findRoots();

    @Query(value = "select * from shop_unit where shop_unit.`left` >= ?1 and shop_unit.`right` <= ?2", nativeQuery = true)
    List<ShopUnit> findAllChildrenByParentBorders(int left, int right);

    @Query(value = "select * from shop_unit where shop_unit.type = 0", nativeQuery = true)
    List<ShopUnit> findAllOffers();

//    @Query(value = """
//            DELIMITER $$
//            DROP PROCEDURE IF EXISTS build_nested_set_tree;
//            CREATE PROCEDURE build_nested_set_tree()
//            BEGIN
//            UPDATE shop_unit SET `left` = NULL, `right` = NULL;
//            SET @i := 0;
//            UPDATE shop_unit t SET `left` = (@i := @i + 1), `right` = (@i := @i + 1), `level` = 1
//            WHERE t.parent_id IS NULL;
//            SET @level := 2;
//            forever: LOOP
//            SET @parent_id := NULL;
//            SELECT t.id, t.`right` FROM shop_unit t, shop_unit tc
//            WHERE t.id = tc.parent_id AND tc.`left` IS NULL AND t.`right` IS NOT NULL
//            ORDER BY t.`right` LIMIT 1 INTO @parent_id, @parent_right;
//            IF @parent_id IS NULL THEN LEAVE forever; END IF;
//            SET @current_left := @parent_right;
//            SELECT @current_left + COUNT(*) * 2 FROM shop_unit
//            WHERE parent_id = @parent_id INTO @parent_right;
//            SET @current_length := @parent_right - @current_left;
//            UPDATE shop_unit SET `level` = @level WHERE shop_unit.parent_id = @parent_id;
//            SET @level = @level + 1;
//            UPDATE shop_unit t SET `right` = `right` + @current_length
//            WHERE `right` >= @current_left ORDER BY `right`;
//            UPDATE shop_unit t SET `left` = `left` + @current_length
//            WHERE `left` > @current_left ORDER BY `left`;
//            SET @i := (@current_left - 1);
//            UPDATE shop_unit t SET `left` = (@i := @i + 1), `right` = (@i := @i + 1)
//            WHERE parent_id = @parent_id ORDER BY id;
//            END LOOP;
//            END$$
//
//            CALL build_nested_set_tree();
//            """, nativeQuery = true)
    @Procedure(procedureName = "build_nested_set_tree")
    void rebuildTree();
}
