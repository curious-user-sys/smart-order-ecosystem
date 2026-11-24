package com.smartorder.inventory.service;

import com.smartorder.inventory.entity.Inventory;
import com.smartorder.inventory.entity.ProcessedOrder;
import com.smartorder.inventory.repository.InventoryRepository;
import com.smartorder.inventory.repository.ProcessedOrderRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProcessedOrderRepository processedOrderRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProcessedOrderRepository processedOrderRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.processedOrderRepository = processedOrderRepository;
    }

    @Transactional(readOnly = true)
    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Inventory findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("Stok kaydı bulunamadı. productId=" + productId));
    }

    @Transactional
    public Inventory upsertStock(Long productId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Stok adedi negatif olamaz.");
        }
        return inventoryRepository.findByProductId(productId)
                .map(existing -> {
                    existing.setQuantity(quantity);
                    return inventoryRepository.save(existing);
                })
                .orElseGet(() -> inventoryRepository.save(new Inventory(productId, quantity)));
    }

    @Transactional
    public void deductStock(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Düşülecek stok adedi sıfırdan büyük olmalıdır.");
        }
        Inventory inventory = findByProductId(productId);
        if (inventory.getQuantity() < quantity) {
            throw new IllegalStateException(
                "Yetersiz stok. productId=" + productId + ", mevcut=" + inventory.getQuantity() + ", istenen=" + quantity
            );
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void deductStockForOrder(Long orderId, Long productId, Integer quantity) {
        if (orderId != null && processedOrderRepository.existsById(orderId)) {
            return;
        }
        deductStock(productId, quantity);
        if (orderId != null) {
            processedOrderRepository.save(new ProcessedOrder(orderId, Instant.now()));
        }
    }
}