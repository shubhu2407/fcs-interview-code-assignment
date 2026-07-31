package com.fulfilment.application.monolith.stores;

public class StoreEvent {

  public enum Type {
    CREATED,
    UPDATED
  }

  public final Type type;
  public final Store store;

  public StoreEvent(Type type, Store store) {
    this.type = type;
    this.store = store;
  }
}