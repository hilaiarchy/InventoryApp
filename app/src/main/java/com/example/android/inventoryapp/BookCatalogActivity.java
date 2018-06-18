package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.Data.BookContract.BooksEntry;
import com.example.android.inventoryapp.Data.BookDBHelper;
/**
 * Displays list of Books in the inventory.
 */

public class BookCatalogActivity extends AppCompatActivity {

    private BookDBHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_catalog);

        mDbHelper = new BookDBHelper(this);
    }
    /**
     * Before inserting a new book - checking if it exist in inventory already
     */
    public boolean CheckBookExistance(int mCode, String mName, int mPrice, String mSupplierName,String mPhone) {

        SQLiteDatabase db =  mDbHelper.getWritableDatabase();
        String Query = "Select * from " + BooksEntry.TABLE_NAME + " where "
                + BooksEntry.COLUMN_PRODUCT_CODE + " = " + mCode
                + " and " + BooksEntry.COLUMN_PRODUCT_NAME + " = " + "\"" + mName + "\""
                + " and " + BooksEntry.COLUMN_PRICE  + " = " + mPrice
                + " and " + BooksEntry.COLUMN_SUPPLIER_NAME + " = " + "\"" + mSupplierName + "\""
                + " and " + BooksEntry.COLUMN_SUPPLIER_PHONE_NUMBER  + " = " + "\"" + mPhone + "\"";
        Log.e("insert: ", Query);
        Cursor cursor = db.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    /**
     * Insert a new row to the inventory database
     */
    private void insertBook(int mCode, String mName, int mPrice, int mQuantity,String mSupplierName,String mPhone,int mInStock) {

        SQLiteDatabase db =  mDbHelper.getWritableDatabase();
        try {
            final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + BooksEntry.TABLE_NAME + " ("
                    + BooksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + BooksEntry.COLUMN_PRODUCT_CODE + " INTEGER NOT NULL, "
                    + BooksEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                    + BooksEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                    + BooksEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 1, "
                    + BooksEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                    + BooksEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL,"
                    + BooksEntry.COLUMN_IN_STOCK + " INTEGER NOT NULL DEFAULT 1);";
            db.execSQL(CREATE_TABLE);
            Toast.makeText(BookCatalogActivity.this, "Product inserted successful", Toast.LENGTH_LONG).show();

            // checking if the product exist
            if (CheckBookExistance(mCode, mName, mPrice, mSupplierName, mPhone)) {

                ContentValues newValues  = new ContentValues();
                int newQuantity = getScore(mCode) + mQuantity;
                newValues.put(BooksEntry.COLUMN_QUANTITY,newQuantity);

                try {
                    db.update(BooksEntry.TABLE_NAME,newValues,BooksEntry.COLUMN_PRODUCT_CODE + "=?",new String[] {String.valueOf(mCode)});

                    Toast.makeText(BookCatalogActivity.this, "Product inserted successfully", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Toast.makeText(BookCatalogActivity.this, "ERROR " + e.toString(), Toast.LENGTH_LONG).show();
                }

            } else {
                try {
                    String newBook =
                            "INSERT or replace INTO " + BooksEntry.TABLE_NAME + "(" + BooksEntry.COLUMN_PRODUCT_CODE + ", " + BooksEntry.COLUMN_PRODUCT_NAME
                                    + ", " + BooksEntry.COLUMN_PRICE + ", " + BooksEntry.COLUMN_QUANTITY
                                    + ", " + BooksEntry.COLUMN_SUPPLIER_NAME + ", " + BooksEntry.COLUMN_SUPPLIER_PHONE_NUMBER + ", " + BooksEntry.COLUMN_IN_STOCK +
                                    ") VALUES(" + mCode + ",\"" + mName + "\", " + mPrice + ", " + mQuantity + ", \"" + mSupplierName + "\", \"" + mPhone + "\", " + mInStock + ");";
                    Log.e("insert: ", newBook);
                    db.execSQL(newBook);

                    Toast.makeText(BookCatalogActivity.this, "Product sold successfully", Toast.LENGTH_LONG).show();
                }
                catch (Exception e) {
                    Toast.makeText(BookCatalogActivity.this, "ERROR " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
        catch (Exception e) {
            Toast.makeText(BookCatalogActivity.this, "ERROR " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Deletes ALL the rows in the table - in the future will add + fragment to verify the user wants to delete
     */
    private void deleteAllBooks() {
        SQLiteDatabase db =  mDbHelper.getWritableDatabase();
        //open a fragment to make sure we want to delete
        try {
            db.execSQL("delete from "+ BooksEntry.TABLE_NAME);
            db.close();
            Toast.makeText(BookCatalogActivity.this, "Table deleted", Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            Toast.makeText(BookCatalogActivity.this, "ERROR " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    // Get the score
    public int getScore (int codeInt) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor = database.query(BooksEntry.TABLE_NAME, null, BooksEntry.COLUMN_PRODUCT_CODE + " = ?",
                            new String[] {String.valueOf(codeInt) }, null, null, null, null);

        int quantityColumnIndex = cursor.getColumnIndex(BooksEntry.COLUMN_QUANTITY);
        int prevQuantity;

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            prevQuantity = cursor.getInt(quantityColumnIndex);
        } else {
            prevQuantity = 0;
        }

        cursor.close();

        return prevQuantity;
    }

    /**
     * Selling a book - lowering the quantity of the book in the inventory
     */
    private void sellingBook(int mCode, int mQuantity) {

        //the user enters the PRODUCT_CODE and QUANTITY that was sold

        ContentValues newValues  = new ContentValues();

        if (getScore(mCode) == 0){
            Toast.makeText(BookCatalogActivity.this, "ERROR No books to sell", Toast.LENGTH_LONG).show();
        }
        else if (getScore(mCode) < mQuantity) {
            Toast.makeText(BookCatalogActivity.this, "ERROR the desired selling quantity is larger then the inventory quantity", Toast.LENGTH_LONG).show();
        }
        else {
            int newQuantity = getScore(mCode) - mQuantity;
            newValues.put(BooksEntry.COLUMN_QUANTITY,newQuantity);

            SQLiteDatabase db =  mDbHelper.getWritableDatabase();
            try {
                final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + BooksEntry.TABLE_NAME + " ("
                        + BooksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + BooksEntry.COLUMN_PRODUCT_CODE + " INTEGER NOT NULL, "
                        + BooksEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                        + BooksEntry.COLUMN_PRICE + " INTEGER, "
                        + BooksEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 1, "
                        + BooksEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                        + BooksEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT NOT NULL,"
                        + BooksEntry.COLUMN_IN_STOCK + " INTEGER NOT NULL DEFAULT 1);";
                db.execSQL(CREATE_TABLE);

                db.update(BooksEntry.TABLE_NAME,newValues,BooksEntry.COLUMN_PRODUCT_CODE + "=?",new String[] {String.valueOf(mCode)});

                Toast.makeText(BookCatalogActivity.this, "Product sold successfully", Toast.LENGTH_LONG).show();
            }
            catch (Exception e) {
                Toast.makeText(BookCatalogActivity.this, "ERROR " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
    /**
     * Show BookCatalogActivity:
     **/
    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        BookDBHelper mDbHelper = new BookDBHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM inventory"
        // to get a Cursor that contains all rows from the inventory table.
        Cursor cursorAll = db.query(BooksEntry.TABLE_NAME, null,null,null,null,null,null);

        // Perform this raw SQL query will get the sum of books
        String[] tableColumns = new String[] {"sum("+BooksEntry.COLUMN_QUANTITY+") AS noOfBooks"};
        Cursor cursorSum = db.query(BooksEntry.TABLE_NAME, tableColumns,null,null,null,null,null);

        TextView displayBooksView = (TextView) findViewById(R.id.unique_books_left);

        TextView displayUniqueBooksView = (TextView) findViewById(R.id.books_left);

        try {

            displayBooksView.setText(getApplicationContext().getString(R.string.unique_books_left, cursorAll.getCount()));

            int noOfBooksColumnIndex = cursorSum.getColumnIndex("noOfBooks");

            if(cursorSum.moveToFirst()){
                displayUniqueBooksView.setText(getApplicationContext().getString(R.string.book_left, cursorSum.getInt(noOfBooksColumnIndex)));
            } else {
                displayUniqueBooksView.setText(getApplicationContext().getString(R.string.book_left, 0));
            }

        } finally {
            // This releases all its resources and makes it invalid.
            cursorAll.close();
            cursorSum.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_books_catalog options from the res/menu_books_catalog/menu_catalog.xml file.
        // This adds menu_books_catalog items to the app bar.
        getMenuInflater().inflate(R.menu.menu_books_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu_books_catalog option in the app bar overflow menu_books_catalog
        switch (item.getItemId()) {
            case R.id.action_insert:
                //hard coded values to make sure the code works --> need to change --> will be collected from user typing
                insertBook(918, "Fun Learning Android", 38, 5,"Max Books","0549842586",BooksEntry.IN_STOCK);
                insertBook(920, "Fun Learning Android 2", 45, 1,"Max Books","0549842586",BooksEntry.NOT_IN_STOCK);
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete Specific entry" menu_books_catalog option
            case R.id.action_selling:
                //hard coded values to make sure the code works --> need to change --> will be collected from user typing
                sellingBook(918,2);
                displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu_books_catalog option
            case R.id.action_delete_all:
                deleteAllBooks();
                displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
