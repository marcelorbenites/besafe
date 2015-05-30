package com.panicbutton.panicbutton;

import java.util.ArrayList;
import java.util.List;

public class InMemoryDangerZoneProvider implements DangerZoneProvider {

    private static InMemoryDangerZoneProvider instance;
    private final List<DangerZone> dangerZones;

    public static InMemoryDangerZoneProvider getInstance() {
        if (instance == null) {
            instance = new InMemoryDangerZoneProvider();
        }
        return instance;
    }

    private InMemoryDangerZoneProvider() {
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
        return new ArrayList<>(dangerZones);
    }

    @Override public boolean delete(String id) {
        DangerZone dangerZone = get(id);
        if (dangerZone != null) {
            return dangerZones.remove(dangerZone);
        }
        return false;
    }
}
