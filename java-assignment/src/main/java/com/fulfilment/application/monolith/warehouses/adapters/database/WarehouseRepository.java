package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    var dbWarehouse = new DbWarehouse();
    dbWarehouse.businessUnitCode = warehouse.businessUnitCode;
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    dbWarehouse.createdAt = warehouse.createdAt != null ? warehouse.createdAt : LocalDateTime.now();
    dbWarehouse.archivedAt = warehouse.archivedAt;

    this.persist(dbWarehouse);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse entity = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (entity == null) {
      throw new IllegalArgumentException(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " does not exist.");
    }

    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.archivedAt = warehouse.archivedAt;
    // entity is a managed Panache entity, so no explicit persist() call is
    // needed here -- Hibernate flushes the dirty state at commit.
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse entity = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (entity != null) {
      this.delete(entity);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity =
        find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return entity != null ? entity.toWarehouse() : null;
  }
}
