/*
 * Copyright (C) 2011 Markus Junginger, greenrobot (http://greenrobot.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.greenrobot.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.*;

/** Database utils, for example to execute SQL scripts */
// TODO add unit tests
public class DbUtils {

    private static ExceptionListener exceptionListener;
    private static PerfListener perfListener;
    private static CustomSerializeAndDeserializer customSerializeAndDeserializer;
    private static LazyListProvider lazyListProvider;

    public static void vacuum(SQLiteDatabase db) {
        db.execSQL("VACUUM");
    }

    /**
     * Calls {@link #executeSqlScript(Context, SQLiteDatabase, String, boolean)} with transactional set to true.
     *
     * @return number of statements executed.
     */
    public static int executeSqlScript(Context context, SQLiteDatabase db, String assetFilename) throws IOException {
        return executeSqlScript(context, db, assetFilename, true);
    }

    /**
     * Executes the given SQL asset in the given database (SQL file should be UTF-8). The database file may contain
     * multiple SQL statements. Statements are split using a simple regular expression (something like
     * "semicolon before a line break"), not by analyzing the SQL syntax. This will work for many SQL files, but check
     * yours.
     *
     * @return number of statements executed.
     */
    public static int executeSqlScript(Context context, SQLiteDatabase db, String assetFilename, boolean transactional)
            throws IOException {
        byte[] bytes = readAsset(context, assetFilename);
        String sql = new String(bytes, "UTF-8");
        String[] lines = sql.split(";(\\s)*[\n\r]");
        int count;
        if (transactional) {
            count = executeSqlStatementsInTx(db, lines);
        } else {
            count = executeSqlStatements(db, lines);
        }
        DaoLog.i("Executed " + count + " statements from SQL script '" + assetFilename + "'");
        return count;
    }

    public static int executeSqlStatementsInTx(SQLiteDatabase db, String[] statements) {
        db.beginTransaction();
        try {
            int count = executeSqlStatements(db, statements);
            db.setTransactionSuccessful();
            return count;
        } finally {
            db.endTransaction();
        }
    }

    public static int executeSqlStatements(SQLiteDatabase db, String[] statements) {
        int count = 0;
        for (String line : statements) {
            line = line.trim();
            if (line.length() > 0) {
                db.execSQL(line);
                count++;
            }
        }
        return count;
    }

    /**
     * Copies all available data from in to out without closing any stream.
     *
     * @return number of bytes copied
     */
    public static int copyAllBytes(InputStream in, OutputStream out) throws IOException {
        int byteCount = 0;
        byte[] buffer = new byte[4096];
        while (true) {
            int read = in.read(buffer);
            if (read == -1) {
                break;
            }
            out.write(buffer, 0, read);
            byteCount += read;
        }
        return byteCount;
    }

    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyAllBytes(in, out);
        return out.toByteArray();
    }

    public static byte[] readAsset(Context context, String filename) throws IOException {
        InputStream in = context.getResources().getAssets().open(filename);
        try {
            return readAllBytes(in);
        } finally {
            in.close();
        }
    }

    public static void logTableDump(SQLiteDatabase db, String tablename) {
        Cursor cursor = db.query(tablename, null, null, null, null, null, null);
        try {
            String dump = DatabaseUtils.dumpCursorToString(cursor);
            DaoLog.d(dump);
        } finally {
            cursor.close();
        }
    }

    public static byte[] serialize(Object o, boolean ignoreCustomSerializer) throws IOException {
        if(ignoreCustomSerializer == false && customSerializeAndDeserializer != null) {
            return customSerializeAndDeserializer.serialize(o);
        }
        ByteArrayOutputStream bos = null;
        try {
            ObjectOutput out = null;
            bos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            // Get the bytes of the serialized object
            return bos.toByteArray();
        } finally {
            closeQuietly(bos);
        }
    }

    public static byte[] serializeObject(Object o) {
        if(o == null) {
            return null;
        }
        try {
            if(perfListener != null) {
                perfListener.onStartSerialization();
            }
            return serialize(o, false);
        } catch(IOException ioe) {
            if(exceptionListener != null) {
                exceptionListener.onSerializationError(ioe);
            }
            Log.e("DBUTIL", "error" + ioe == null ? "null" : ioe.getMessage());
            throw new RuntimeException(ioe == null ? "cannot serialize object. FATAL:" : "cannot serialize object, FATAL: " + ioe.getMessage());
        } finally {
            if(perfListener != null) {
                perfListener.onFinishSerialization();
            }
        }
    }

    public static Object deserialize(byte[] b, Class klass, boolean ignoreCustomDeserializer) throws IOException, ClassNotFoundException {
        if(ignoreCustomDeserializer == false && customSerializeAndDeserializer != null) {
            return customSerializeAndDeserializer.deserialize(b, klass);
        }
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new ByteArrayInputStream(b));
            return in.readObject();
        } finally {
            closeQuietly(in);
        }
    }

    public static Object deserializeObject(byte[] b, Class klass) {
        if(b == null) {
            return null;
        }
        try {
            if(perfListener != null) {
                perfListener.onStartDeserialization(klass);
            }
            return deserialize(b, klass,false);
        } catch(ClassNotFoundException cnfe) {
            if(exceptionListener != null) {
                exceptionListener.onDeserializationError(cnfe);
            }
            throw new RuntimeException("inconsisitent db detected");
        } catch(IOException ioe) {
            if(exceptionListener != null) {
                exceptionListener.onDeserializationError(ioe);
            }
            throw new RuntimeException("inconsisitent db detected");
        } catch (Exception e) {
            if(exceptionListener != null) {
                exceptionListener.onDeserializationError(e);
            }
            throw new RuntimeException("inconsisitent db detected");
        }
        finally {
            if(perfListener != null) {
                perfListener.onFinishDeserialization();
            }
        }
    }

    private static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            }
            catch (Exception e) {
                //
            }
        }
    }

    public static void setExceptionListener(ExceptionListener exceptionListener) {
        DbUtils.exceptionListener = exceptionListener;
    }

    public static void setPerfListener(PerfListener perfListener) {
        DbUtils.perfListener = perfListener;
    }

    public static void setCustomSerializeAndDeserializer(CustomSerializeAndDeserializer customSerializeAndDeserializer) {
        DbUtils.customSerializeAndDeserializer = customSerializeAndDeserializer;
    }

    public static void setLazyListProvider(LazyListProvider lazyListProvider) {
        DbUtils.lazyListProvider = lazyListProvider;
    }

    public static <E> LazyList<E> createLazyList(AbstractDao<E, ?> dao, Cursor cursor, boolean cacheEntities) {
        if(lazyListProvider == null) {
            return new LazyList<E>(dao, cursor, cacheEntities);
        }
        return lazyListProvider.createLazyList(dao, cursor, cacheEntities);
    }

    public static interface ExceptionListener {
        public void onSerializationError(IOException ioException);
        public void onDeserializationError(ClassNotFoundException cnfException);
        public void onDeserializationError(IOException ioException);
        public void onDeserializationError(Exception anyException);
    }

    public static interface PerfListener {
        public void onStartDeserialization(Class klass);
        public void onFinishDeserialization();
        public void onStartSerialization();
        public void onFinishSerialization();
    }

    public static interface CustomSerializeAndDeserializer {
        public Object deserialize(byte[] b, Class klass) throws IOException, ClassNotFoundException;
        public byte[] serialize(Object o) throws IOException;
    }

    public static interface LazyListProvider {
        public <E> LazyList<E> createLazyList(AbstractDao<E, ?> dao, Cursor cursor, boolean cacheEntities);
    }

}
