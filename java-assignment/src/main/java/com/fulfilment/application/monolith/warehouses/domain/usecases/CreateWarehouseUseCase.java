package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    // Business Unit Code Verification
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException(
          "A warehouse with business unit code " + warehouse.businessUnitCode + " already exists.");
    }

    // Location Validation
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Location " + warehouse.location + " does not exist.");
    }

    var activeWarehousesAtLocation =
        warehouseStore.getAll().stream()
            .filter(w -> w.location.equals(location.identification))
            .filter(w -> w.archivedAt == null)
            .toList();

    // Warehouse Creation Feasibility
    if (activeWarehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException(
          "Maximum number of warehouses reached for location " + location.identification);
    }

    // Capacity and Stock Validation
    int currentCapacityAtLocation =
        activeWarehousesAtLocation.stream().mapToInt(w -> w.capacity).sum();

    if (currentCapacityAtLocation + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Warehouse capacity exceeds the maximum capacity allowed for location "
              + location.identification);
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Warehouse stock cannot exceed its capacity.");
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
  }
}