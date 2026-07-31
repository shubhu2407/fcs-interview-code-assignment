package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // The Warehouse being replaced must exist and still be active
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code " + newWarehouse.businessUnitCode + " does not exist.");
    }
    if (existing.archivedAt != null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code "
              + newWarehouse.businessUnitCode
              + " is already archived.");
    }

    // Location Validation (for the new warehouse's location)
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Location " + newWarehouse.location + " does not exist.");
    }

    var otherActiveWarehousesAtLocation =
        warehouseStore.getAll().stream()
            .filter(w -> w.location.equals(location.identification))
            .filter(w -> w.archivedAt == null)
            .filter(w -> !w.businessUnitCode.equals(existing.businessUnitCode))
            .toList();

    // Warehouse Creation Feasibility (the old unit is being archived, so it
    // doesn't count against the location's limit for the new one)
    if (otherActiveWarehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException(
          "Maximum number of warehouses reached for location " + location.identification);
    }

    int currentCapacityAtLocation =
        otherActiveWarehousesAtLocation.stream().mapToInt(w -> w.capacity).sum();

    if (currentCapacityAtLocation + newWarehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException(
          "Warehouse capacity exceeds the maximum capacity allowed for location "
              + location.identification);
    }

    // Capacity Accommodation: new warehouse must be able to hold the old one's stock
    if (newWarehouse.capacity < existing.stock) {
      throw new IllegalArgumentException(
          "New warehouse capacity must accommodate the stock of the warehouse being replaced.");
    }

    // Stock Matching
    if (!newWarehouse.stock.equals(existing.stock)) {
      throw new IllegalArgumentException(
          "New warehouse stock must match the stock of the warehouse being replaced.");
    }

    // Archive the old one, then create the new one under the same Business Unit Code
    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}