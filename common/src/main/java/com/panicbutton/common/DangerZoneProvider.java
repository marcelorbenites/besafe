package com.panicbutton.common;

import java.util.List;

public interface DangerZoneProvider {

    void insert(DangerZone dangerZone);

    DangerZone get(String id);

    List<DangerZone> getAll();

    boolean delete(String id);

}
