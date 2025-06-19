package com.ahimsasystems.chenup.foundationdb;


import com.apple.foundationdb.ApiVersion;
import com.apple.foundationdb.Database;
import com.apple.foundationdb.FDB;
import com.apple.foundationdb.tuple.Tuple;

public class FdbApp {
    public static void main(String[] args) {
        FDB fdb = FDB.selectAPIVersion(ApiVersion.LATEST); // match your installed FDB version
        try (Database db = fdb.open()) {
            // Set a value
            db.run(tr -> {
                tr.set(Tuple.from("hello").pack(), Tuple.from("world").pack());
                return null;
            });

            // Get a value
            String value = db.read(tr -> {
                byte[] result = tr.get(Tuple.from("hello").pack()).join();
                return Tuple.fromBytes(result).getString(0);
            });

            System.out.println("Value for 'hello': " + value);
        }
    }
}
