package com.ahimsasystems.chenup.core;

import io.quarkus.runtime.annotations.RegisterForReflection;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
@RegisterForReflection
public interface PersistenceInitializer {
    /**
     * Registers all classes that need to be persisted with the given PersistenceManager.
     *
     * @param pm the PersistenceManager to register classes with
     */
    void registerAll(PersistenceManager pm);
}
