package com.ahimsasystems.chenup.foundationdb;

import jakarta.inject.Singleton;

import com.apple.foundationdb.ApiVersion;
import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.tuple.Tuple;


@Singleton
public class FoundationDBService {
    private final Database db;

    public FoundationDBService() {
        FDB fdb = FDB.selectAPIVersion(710);
        this.db = fdb.open();
    }

    public void put(String key, String value) {
        db.run(tr -> {
            tr.set(Tuple.from(key).pack(), Tuple.from(value).pack());
            return null;
        });
    }

    // More operations...
}
