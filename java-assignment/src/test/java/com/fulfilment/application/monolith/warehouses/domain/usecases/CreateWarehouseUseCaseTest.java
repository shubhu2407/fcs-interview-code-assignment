package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private InMemoryWarehouseStore warehouseStore;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  public void setup() {
    warehouseStore = new InMemoryWarehouseStore();
    useCase = new CreateWarehouseUseCase(warehouseStore, new LocationGateway());
  }

  private Warehouse newWarehouse(String buCode, String location, int capacity, int stock) {
    var w = new Warehouse();
    w.businessUnitCode = buCode;
    w.location = location;
    w.capacity = capacity;
    w.stock = stock;
    return w;
  }

  @Test
  public void testCreateSucceedsWithValidData() {
    useCase.create(newWarehouse("MWH.100", "AMSTERDAM-001", 50, 20));

    Warehouse created = warehouseStore.findByBusinessUnitCode("MWH.100");
    assertEquals("MWH.100", created.businessUnitCode);
    assertEquals("AMSTERDAM-001", created.location);
  }

  @Test
  public void testCreateFailsWhenBusinessUnitCodeAlreadyExists() {
    warehouseStore.create(newWarehouse("MWH.100", "AMSTERDAM-001", 50, 20));

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(newWarehouse("MWH.100", "AMSTERDAM-002", 30, 10)));
  }

  @Test
  public void testCreateFailsWhenLocationDoesNotExist() {
    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(newWarehouse("MWH.100", "NOWHERE-001", 30, 10)));
  }

  @Test
  public void testCreateFailsWhenMaxNumberOfWarehousesReachedAtLocation() {
    // ZWOLLE-001 allows only 1 warehouse (see LocationGateway seed data)
    warehouseStore.create(newWarehouse("MWH.100", "ZWOLLE-001", 30, 10));

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(newWarehouse("MWH.101", "ZWOLLE-001", 5, 2)));
  }

  @Test
  public void testCreateFailsWhenCapacityExceedsLocationMaxCapacity() {
    // ZWOLLE-002 allows maxCapacity=50, maxNumberOfWarehouses=2
    warehouseStore.create(newWarehouse("MWH.100", "ZWOLLE-002", 40, 10));

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(newWarehouse("MWH.101", "ZWOLLE-002", 20, 5)));
  }

  @Test
  public void testCreateFailsWhenStockExceedsCapacity() {
    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.create(newWarehouse("MWH.100", "AMSTERDAM-001", 30, 50)));
  }

  /** Minimal in-memory fake for WarehouseStore, used across the use case tests. */
  public static class InMemoryWarehouseStore implements WarehouseStore {
    private final List<Warehouse> warehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return warehouses;
    }

    @Override
    public void create(Warehouse warehouse) {
      warehouses.add(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
      // findByBusinessUnitCode returns direct object references from this list,
      // so callers that mutate the returned Warehouse (e.g. setting archivedAt)
      // have already updated the list in place. update() only needs to add it
      // if it's genuinely a new/different object instance.
      if (!warehouses.contains(warehouse)) {
        warehouses.add(warehouse);
      }
    }

    @Override
    public void remove(Warehouse warehouse) {
      warehouses.removeIf(w -> w.businessUnitCode.equals(warehouse.businessUnitCode));
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouses.stream()
          .filter(w -> w.businessUnitCode.equals(buCode) && w.archivedAt == null)
          .findFirst()
          .orElse(null);
    }
  }
}