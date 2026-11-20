package com.linli.supermarket.controller;

import com.linli.supermarket.common.Result;
import com.linli.supermarket.dto.DeliveryAreaDTO;
import com.linli.supermarket.entity.Deliveryman;
import com.linli.supermarket.service.DeliveryService;
import com.linli.supermarket.vo.DeliveryAreaVO;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 配送区域与配送员接口。 */
@RestController
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    /** 前端启用的配送区域树。 */
    @GetMapping("/api/delivery/areas")
    public Result<List<DeliveryAreaVO>> enabledAreas() {
        return Result.ok(deliveryService.treeEnabled());
    }

    @GetMapping("/api/admin/delivery/areas")
    public Result<List<DeliveryAreaVO>> allAreas() {
        return Result.ok(deliveryService.treeAll());
    }

    @PostMapping("/api/admin/delivery/areas")
    public Result<Map<String, Long>> createArea(@Valid @RequestBody DeliveryAreaDTO dto) {
        return Result.ok(Collections.singletonMap("id", deliveryService.createArea(dto)));
    }

    @PutMapping("/api/admin/delivery/areas/{id}")
    public Result<Void> updateArea(@PathVariable Long id, @Valid @RequestBody DeliveryAreaDTO dto) {
        deliveryService.updateArea(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/api/admin/delivery/areas/{id}")
    public Result<Void> deleteArea(@PathVariable Long id) {
        deliveryService.deleteArea(id);
        return Result.ok();
    }

    @GetMapping("/api/admin/deliverymen")
    public Result<List<Deliveryman>> deliverymen() {
        return Result.ok(deliveryService.listDeliverymen());
    }

    @PostMapping("/api/admin/deliverymen")
    public Result<Map<String, Long>> createDeliveryman(@Valid @RequestBody Deliveryman deliveryman) {
        return Result.ok(Collections.singletonMap("id",
                deliveryService.createDeliveryman(deliveryman)));
    }

    @PutMapping("/api/admin/deliverymen/{id}")
    public Result<Void> updateDeliveryman(@PathVariable Long id, @RequestBody Deliveryman deliveryman) {
        deliveryService.updateDeliveryman(id, deliveryman);
        return Result.ok();
    }

    @DeleteMapping("/api/admin/deliverymen/{id}")
    public Result<Void> deleteDeliveryman(@PathVariable Long id) {
        deliveryService.deleteDeliveryman(id);
        return Result.ok();
    }
}
