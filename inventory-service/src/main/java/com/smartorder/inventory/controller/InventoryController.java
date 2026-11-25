package com.smartorder.inventory.controller;

import com.smartorder.inventory.entity.Inventory;
import com.smartorder.inventory.service.InventoryService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public record StockUpsertRequest(Long productId, Integer quantity) {
    }

    @GetMapping
    public List<Inventory> getAll() {
        return inventoryService.findAll();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.findByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<Inventory> upsert(@RequestBody StockUpsertRequest request) {
        Inventory saved = inventoryService.upsertStock(request.productId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}