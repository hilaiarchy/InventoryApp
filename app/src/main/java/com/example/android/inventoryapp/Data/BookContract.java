package com.example.android.inventoryapp.Data;

import android.provider.BaseColumns;

public final class BookContract {
    /**
     * Books inventory table.
     */
    public static abstract class BooksEntry implements BaseColumns {

        public static final String TABLE_NAME = "BookInventory";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_CODE = "ProductCode";
        public static final String COLUMN_PRODUCT_NAME = "ProductName";
        public static final String COLUMN_PRICE= "Price";
        public static final String COLUMN_QUANTITY = "Quantity";
        public static final String COLUMN_SUPPLIER_NAME = "SupplierName";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "SupplierPhone";
        public static final String COLUMN_IN_STOCK = "InStock";

        /**
         * Possible values for stock availability options.
         */
        public static final int NOT_IN_STOCK = 0;
        public static final int IN_STOCK = 1;

    }
}
