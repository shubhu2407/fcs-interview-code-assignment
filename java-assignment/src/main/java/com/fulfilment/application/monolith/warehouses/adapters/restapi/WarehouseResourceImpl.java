package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseOperation createWarehouseOperation;
  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.listAll().stream()
        .filter(dbWarehouse -> dbWarehouse.archivedAt == null)
        .map(this::toWarehouseResponse)
        .toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();

    createWarehouseOperation.create(warehouse);

    DbWarehouse created = findActiveByBusinessUnitCode(warehouse.businessUnitCode);
    return toWarehouseResponse(created);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    DbWarehouse entity = warehouseRepository.findById(parseId(id));
    if (entity == null) {
      throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
    }
    return toWarehouseResponse(entity);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    DbWarehouse entity = warehouseRepository.findById(parseId(id));
    if (entity == null) {
      throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
    }

    var warehouse = entity.toWarehouse();
    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    var newWarehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    newWarehouse.businessUnitCode = businessUnitCode;
    newWarehouse.location = data.getLocation();
    newWarehouse.capacity = data.getCapacity();
    newWarehouse.stock = data.getStock();

    replaceWarehouseOperation.replace(newWarehouse);

    DbWarehouse created = findActiveByBusinessUnitCode(businessUnitCode);
    return toWarehouseResponse(created);
  }

  private DbWarehouse findActiveByBusinessUnitCode(String buCode) {
    return warehouseRepository
        .find("businessUnitCode = ?1 and archivedAt is null", buCode)
        .firstResult();
  }

  private Long parseId(String id) {
    try {
      return Long.valueOf(id);
    } catch (NumberFormatException e) {
      throw new WebApplicationException("Invalid warehouse id: " + id, 400);
    }
  }

  private Warehouse toWarehouseResponse(DbWarehouse dbWarehouse) {
    var response = new Warehouse();
    response.setId(String.valueOf(dbWarehouse.id));
    response.setBusinessUnitCode(dbWarehouse.businessUnitCode);
    response.setLocation(dbWarehouse.location);
    response.setCapacity(dbWarehouse.capacity);
    response.setStock(dbWarehouse.stock);
    return response;
  }

  @Provider
  public static class IllegalArgumentExceptionMapper
      implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException exception) {
      return Response.status(400).entity(exception.getMessage()).build();
    }
  }
}