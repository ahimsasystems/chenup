package com.ahimsasystems.chenup.foundationdb;

import com.ahimsasystems.chenup.core.PersistenceContext;
import com.apple.foundationdb.Transaction;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public class FdbContext implements PersistenceContext {


        private final Transaction transaction;
        public FdbContext(Transaction transaction) {
            this.transaction = transaction;
        }
        public Transaction getTransaction() {
            return transaction;
        }

}
