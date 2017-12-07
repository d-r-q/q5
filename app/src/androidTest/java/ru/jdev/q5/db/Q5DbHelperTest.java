package ru.jdev.q5.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.jdev.q5.CsvTransactionLog;
import ru.jdev.q5.Transaction;
import ru.jdev.q5.TrxDate;
import ru.jdev.q5.categories.Categories;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class Q5DbHelperTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        Q5DbHelper q5DbHelper = new Q5DbHelper(appContext);
        q5DbHelper.deleteDb();
        CsvTransactionLog csvTransactionLog = new CsvTransactionLog(appContext);
        csvTransactionLog.delete();
        csvTransactionLog.storeTrx(new Transaction("15", "testCategory", "comment", "manual", new TrxDate(new Date())));
        SQLiteDatabase db = q5DbHelper.getWritableDatabase();
        assertEquals("testCategory", new Categories(db).listAll().get(0).getName());
    }
}
