package com.ahimsasystems.chenup.core;

import java.time.Instant;
import java.util.UUID;

public interface PersistenceCapable {

    // These methods have implementations in AbstractPersistenceCapable so the code generator will not generate them again.
    // The code to skip generating these methods is in the code generator and if additional methods need to be ignored, update the code generator accordingly.
    // Also, the code generator will skip generating implementations if any methods in extending interfaces have default implementations.
    // That was not exactly worded correctly. Methods here may or may not have implementations in AbstractPersistenceCapable. Whether they are generated or not depends on the code generator logic.


    UUID getId();


    void setId(UUID uuid);

    Instant getCurrentTime();
}
