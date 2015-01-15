package sqldb;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by v1ar on 03.11.14.
 */
public class DBTableModel extends AbstractTableModel {
    // здесь мы будем хранить названия столбцов
    public ArrayList<String> columnNames;
    // список типов столбцов
    public ArrayList columnTypes;
    // хранилище для полученных данных из базы данных
    public ArrayList data;
    public ArrayList<String> ids;

    public ArrayList<HashMap<String, String>> hashMaps;
    public ArrayList<String> nameWithID;


    // конструктор позволяет задать возможность редактирования
    public DBTableModel() {
        columnNames = new ArrayList<String>();
        columnTypes = new ArrayList();
        data = new ArrayList();
        ids = new ArrayList<String>();
        hashMaps = new ArrayList<HashMap<String, String>>();
        nameWithID = new ArrayList<String>();

    }

    public void addData () {
        ArrayList<Object> row = new ArrayList();
        for ( int i = 0; i < getColumnCount(); i++) {
            row.add( "" );
        }
        synchronized (data) {
            data.add(row);
            // сообщаем о прибавлении строки
            fireTableRowsInserted( data.size()-1, data.size()-1 );
        }
    }

    // количество строк
    public int getRowCount() {
        synchronized (data) {
            return data.size();
        }
    }

    // количество столбцов
    public int getColumnCount() {
        return columnNames.size();
    }

    // тип данных столбца
    public Class getColumnClass(int column) {
        //return (Class)columnTypes.get(column);
        return String.class;
    }

    // название столбца
    public String getColumnName(int column) {
        return (String)columnNames.get(column);
    }

    // данные в ячейке
    public Object getValueAt(int row, int column) {
        synchronized (data) {
            try {
                return ((ArrayList)data.get(row)).get(column);
            } catch (IndexOutOfBoundsException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    // возможность редактирования
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    // замена значения ячейки
    public void setValueAt(Object value, int row, int column){
        synchronized (data) {
            ((ArrayList)data.get(row)).set(column, value);
        }
    }

    // =========================================
    // заполнение данными таблицы, применение к ним combobox
    public void setMetaData (ResultSet rs) throws Exception {
        columnNames.clear();
        columnTypes.clear();
        // получаем вспомогательную информацию о столбцах
        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 1 /*0*/; i < rsmd.getColumnCount(); i++) {
            // название столбца
            columnNames.add( rsmd.getColumnName(i+1) );
            // тип столбца
            columnTypes.add( Class.forName(rsmd.getColumnClassName(i + 1)) );
        }
        fireTableStructureChanged(); // сообщаем об изменениях в структуре данных
    }

    public void setMetaDataForQuery (ResultSet rs) throws Exception {
        columnNames.clear();
        columnTypes.clear();
        // получаем вспомогательную информацию о столбцах
        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            // название столбца
            columnNames.add( rsmd.getColumnName(i+1) );
            // тип столбца
            columnTypes.add(Class.forName(rsmd.getColumnClassName(i + 1)));
        }
        fireTableStructureChanged(); // сообщаем об изменениях в структуре данных
    }

    public void setHashes(DBAdapter db) throws IOException, SQLException {
        // создание списка хештаблиц [name, id]
        for (String name: columnNames) {
            if (name.indexOf("id") != -1 && name.compareTo("id") != 0) { // имя колонки содержит id
                nameWithID.add(name.replaceAll("id", ""));
                hashMaps.add(new HashMap<String, String>(){{
                    put("  ","null");
                }});
            }
        }
        // заполнение хещ
        for (int i = 0; i < nameWithID.size(); i++) {

            ResultSet resSet = db.select("SELECT id, name FROM " + nameWithID.get(i) );
            while ( resSet.next() ) { // получаем данные [name, id]
                String s1 = resSet.getString(2).trim();
                String s2 = String.valueOf(resSet.getInt(1));
                hashMaps.get(i).put(s1, s2);
            }
        }
    }

    public String hashMapGetKey (HashMap<String, String> hm, String value) {
        for (String s : hm.keySet()) {
            if (hm.get(s).contains(value)) {
                return s;
            }
        }
        return "";
    }


    public void setDataSource(ResultSet rs) throws Exception {
        // удаляем прежние данные
        data.clear();
        ids.clear();

        fireTableStructureChanged(); // сообщаем об изменениях в структуре данных
        while ( rs.next() ) { // получаем данные
            // здесь будем хранить ячейки одной строки
            ArrayList<String> row = new ArrayList();
            ids.add(String.valueOf(rs.getInt(1))); // id для всех полей
            System.out.println();
            for ( int i = 0; i < columnNames.size(); i++) {
                if ( columnNames.get(i).indexOf("id") != -1) {
                    try {
                        HashMap<String, String> hm = hashMaps.get(nameWithID.indexOf( columnNames.get(i).replaceAll("id", "") ));
                        row.add( hashMapGetKey(hm, rs.getString(i+2).trim()) );
                    } catch (NullPointerException ex) {
                        row.add("");
                    }
                } else {
                    if ( columnTypes.get(i) == String.class ) {
                        try {
                            row.add( rs.getString(i+2).trim() );
                        } catch (NullPointerException ex) {
                            row.add("");
                        }
                    } else {
                        if ( columnTypes.get(i) == Integer.class ) {
                            try {
                                row.add( String.valueOf(rs.getInt(i+2)) );
                            } catch (NullPointerException ex) {
                                row.add("");
                            }
                        } else {
                            if ( columnTypes.get(i) == java.sql.Date.class ) {
                                try {
                                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                                    row.add(df.format((Date)rs.getDate(i+2)) );
                                } catch (NullPointerException ex) {
                                    row.add("");
                                }
                            } else {
                                try {
                                    row.add((String)rs.getObject(i+2));
                                } catch (NullPointerException ex) {
                                    row.add("");
                                }

                            }
                        }
                    }
                }
            }
            synchronized (data) {
                data.add(row);
                // сообщаем о прибавлении строки
                fireTableRowsInserted( data.size()-1, data.size()-1 );
            }
        }
    }

    public void setDataSourceForQuery(ResultSet rs) throws Exception {
        // удаляем прежние данные
        data.clear();
        ids.clear();

        while ( rs.next() ) { // получаем данные
            // здесь будем хранить ячейки одной строки
            ArrayList<Object> row = new ArrayList<>();
            ids.add(String.valueOf(rs.getInt(1))); // id для всех полей
            for ( int i = 0; i < columnNames.size(); i++) {
                if ( columnTypes.get(i) == java.lang.String.class ) {
                    try {
                        row.add(rs.getString(i + 1).trim());
                    } catch (NullPointerException e) {
                        row.add( "NULL");
                    }
                } else {
                    if ( columnTypes.get(i) == java.lang.Integer.class ) {
                        try {
                            row.add( String.valueOf(rs.getInt(i+1)) );
                        } catch (NullPointerException e) {
                            row.add( "NULL");
                        }
                    } else {
                        if ( columnTypes.get(i) == java.sql.Date.class ) {
                            try {
                                DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                                row.add( df.format((Date)rs.getDate(i+1)) );
                            } catch (NullPointerException e) {
                                row.add( "NULL");
                            }
                        } else {
                            try {
                                row.add( rs.getObject(i+1));
                            } catch (NullPointerException e) {
                                row.add( "NULL");
                            }
                        }
                    }
                }
            }
            synchronized (data) {
                data.add(row);
                // сообщаем о прибавлении строки
                fireTableRowsInserted( data.size()-1, data.size()-1 );
            }
        }
    }
}