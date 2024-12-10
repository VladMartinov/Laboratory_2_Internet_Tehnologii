package com.common;

import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;

// класс для коннекта с базой данных
public class Connect {
    // переменные состояний класса
    Connection con;
    Statement stmt;

    // геттеры для состояний класса
    public Connection getCon() {
        return con;
    }
    public Statement getStmt() {
        return stmt;
    }

    // дескриптор создания коннекта с базой данных
    public Connect(String url, String login, String password)  throws SQLException  {
        try {
            // указываем, что работает с БД Postgres
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection(url, login, password);
            Statement stmt = con.createStatement();
            this.con = con;
            this.stmt = stmt;
        }
        // секция обработки ошибок
        catch (
                Exception e) {
            e.printStackTrace();
        }
    }

    // метод формирования строки из SELECT
    public String getBase(String sqlline) {
        String str = "";
        String strSQL = "";
        String[] filds;

        // выделить имена полей и поместить в массив
        filds=sqlline.substring(sqlline.toUpperCase().indexOf("SELECT")+6, sqlline.toUpperCase().indexOf("FROM")-1).trim().split(",");

        try {
            // получить резалтсет по SELECT
            ResultSet rs = stmt.executeQuery(sqlline);

            // пройтись по всем строкам
            while (rs.next()) {
                str = "";
                // в каждой записи соединить все в одну строку
                    for (int i = 0; i < filds.length; i++) {
                    str += rs.getString(filds[i].trim()) + ";";
                }
                strSQL += str+"\n";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return strSQL;
    }

    // метод формирования строки из UPDATE
    public String updateBase(String sqlline) {
        String str = "";
        String strSQL = "";

        String table = sqlline.substring(sqlline.toUpperCase().indexOf("UPDATE") + 6, sqlline.toUpperCase().indexOf("SET") - 1).trim();
        String whereClause = sqlline.substring(sqlline.toUpperCase().indexOf("WHERE") + 5, sqlline.toUpperCase().indexOf(";")).trim();

        try {
            // Выполнение UPDATE запроса
            stmt.executeUpdate(sqlline);

            // Формирование SELECT запроса с использованием MessageFormat для безопасного подстановки
            String sql = MessageFormat.format("SELECT * FROM {0} WHERE {1}", table, whereClause);

            try (ResultSet rs = stmt.executeQuery(sql)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                // Заголовок CSV
                for (int i = 1; i <= columnCount; i++) {
                    strSQL += metaData.getColumnName(i) + ";";
                }

                strSQL += "\n";


                while (rs.next()) {
                    str = "";

                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);

                        // Обработка разделителей и escape-символов в данных
                        value = value == null ? "" : value.replace(";", ";;").replace("\n", "\n;");
                        str += value + ";";
                    }

                    strSQL += str + "\n";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

        return strSQL;
    }

    // метод логирования сообщений сервера в базу данных
    public void putBase(String tableName, String inOut, String line) throws SQLException  {
        String sql;
        try {
            // вставка сообщений в таблицу логирования
        //    if (line.length()<=1000)
            sql= String.format("INSERT INTO %s(curdt,inout,line) VALUES('%s','%s','%s')", tableName, LocalDateTime.now(), inOut, line);
        //    else
       //     sql= String.format("INSERT INTO %s(curdt,inout,line) VALUES('%s','%s','%s')", tableName, LocalDateTime.now(), inOut, line.substring(0,999));

            stmt.executeUpdate(sql);
        }
        // секция обработки ошибок
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // закрытие коннекта с базой данных
    public void closeBase() throws SQLException
    {
        try {
            // закрываем коннект с базой данных
        getCon().close();
        }
        // секция обработки ошибок
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

