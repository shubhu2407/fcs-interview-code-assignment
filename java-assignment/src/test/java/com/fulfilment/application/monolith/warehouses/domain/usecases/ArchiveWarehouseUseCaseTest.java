package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCaseTest.InMemoryWarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  private InMemoryWarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  public void setup() {
    warehouseStore = new InMemoryWarehouseStore();
    useCase = new ArchiveWarehouseUseCase(warehouseStore);
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
  public void testArchiveSetsArchivedAt() {
    warehouseStore.create(newWarehouse("MWH.100", "AMSTERDAM-001", 50, 20));

    var toArchive = new Warehouse();
    toArchive.businessUnitCode = "MWH.100";
    useCase.archive(toArchive);

    assertNotNull(warehouseStore.getAll().stream()
        .filter(w -> w.businessUnitCode.equals("MWH.100"))
        .findFirst()
        .get()
        .archivedAt);
  }

  @Test
  public void testArchiveFailsWhenWarehouseDoesNotExist() {
    var toArchive = new Warehouse();
    toArchive.businessUnitCode = "DOES-NOT-EXIST";

    assertThrows(IllegalArgumentException.class, () -> useCase.archive(toArchive));
  }

  @Test
  public void testArchiveFailsWhenAlreadyArchived() {
    var warehouse = newWarehouse("MWH.100", "AMSTERDAM-001", 50, 20);
    warehouseStore.create(warehouse);

    var toArchive = new Warehouse();
    toArchive.businessUnitCode = "MWH.100";
    useCase.archive(toArchive);

    assertThrows(IllegalArgumentException.class, () -> useCase.archive(toArchive));
  }
}