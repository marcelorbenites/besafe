package com.panicbutton.common;

import java.util.ArrayList;
import java.util.List;

public class InMemoryDangerZoneProvider implements DangerZoneProvider {

    private final List<DangerZone> dangerZones;

    public InMemoryDangerZoneProvider() {
        dangerZones = new ArrayList<>();
    }

    @Override public void insert(DangerZone dangerZone) {
        dangerZones.add(dangerZone);
    }

    @Override public DangerZone get(String id) {
        DangerZone result = null;
        for (DangerZone dangerZone: dangerZones) {
            if (dangerZone.getId().equals(id)) {
                result = dangerZone;
                break;
            }
        }
        return result;
    }

    @Override public List<DangerZone> getAll() {
        return dangerZones;
    }

    @Override public boolean delete(String id) {
        DangerZone dangerZone = get(id);
        if (dangerZone != null) {
            return dangerZones.remove(dangerZone);
        }
        return false;
    }
}
