package com.ahimsasystems.chenup.core;

import java.util.UUID;

public interface PersistenceCapable {

    UUID getId();


    void setId(UUID uuid);
}
