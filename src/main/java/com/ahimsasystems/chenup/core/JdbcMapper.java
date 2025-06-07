package com.ahimsasystems.chenup.core;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public interface JdbcMapper extends Mapper {
    public void setConnection(@NotNull Connection connection);
}
