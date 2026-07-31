package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCaseTest.InMemoryWarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private InMemoryWarehouseStore warehouseStore;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  public void setup() {
    warehouseStore = new InMemoryWarehouseStore();
    useCase = new ReplaceWarehouseUseCase(warehouseStore, new LocationGateway());
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
  public void testReplaceSucceedsAndArchivesOldWarehouse() {
    warehouseStore.create(newWarehouse("MWH.100", "AMSTERDAM-001", 50, 20));

    useCase.replace(newWarehouse("MWH.100", "AMSTERDAM-001", 60, 20));

    long activeCount =
        warehouseStore.getAll().stream()
            .filter(w -> w.businessUnitCode.equals("MWH.100") && w.archivedAt == null)
            .count();
    long archivedCount =
        warehouseStore.getAll().stream()
            .filter(w -> w.businessUnitCode.equals("MWH.100") && w.archivedAt != null)
            .count();

    assertTrue(activeCount == 1);
    assertTrue(archivedCount == 1);
  }

  @Test
  public void testReplaceFailsWhenOriginalDoesNotExist() {
    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.replace(newWarehouse("MWH.999", "AMSTERDAM-001", 60, 20)));
  }

  @Test
  public void testReplaceFailsWhenStockDoesNotMatch() {
    warehouseStore.create(newWarehouse("MWH.100", "AMSTERDAM-001", 50, 20));

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.replace(newWarehouse("MWH.100", "AMSTERDAM-001", 60, 25)));
  }

  @Test
  public void testReplaceFailsWhenNewCapacityCannotAccommodateOldStock() {
    warehouseStore.create(newWarehouse("MWH.100", "AMSTERDAM-001", 50, 20));

    assertThrows(
        IllegalArgumentException.class,
        () -> useCase.replace(newWarehouse("MWH.100", "AMSTERDAM-001", 15, 20)));
  }
}