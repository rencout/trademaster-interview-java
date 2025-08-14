package com.trademaster.inventory.repository;

import com.trademaster.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findBySku(String sku);

    @Modifying
    @Query("UPDATE InventoryItem i SET i.quantity = i.quantity + :delta WHERE i.sku = :sku")
    void adjustQuantityBySku(@Param("sku") String sku, @Param("delta") Integer delta);

    @Modifying
    @Query("UPDATE InventoryItem i SET i.quantity = i.quantity - :amount WHERE i.sku = :sku AND i.quantity >= :amount")
    int decrementQuantityIfAvailable(@Param("sku") String sku, @Param("amount") Integer amount);
}
