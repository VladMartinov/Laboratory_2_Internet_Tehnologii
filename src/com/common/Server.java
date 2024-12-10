package com.common;

import java.io.*;
import java.net.*;

public class Server extends Thread
{
    private static final int port  = 5555; // открываемый порт сервера
    private String TEMPL_MSG  = "Клиент '%d' послал сообщение:\t";
    private String TEMPL_CONN = "Клиент '%d' закрыл коннект.";

    private  Socket socket;
    private  int    num;

    public Server() {}
    public void setSocket(int num, Socket socket)
    {
        // Определение значений
        this.num    = num;
        this.socket = socket;

        // Установка daemon-потока
        setDaemon(true);

        /*
         * Определение стандартного приоритета главного потока
         * int java.lang.Thread.NORM_PRIORITY = 5 - the default
         *               priority that is assigned to a thread.
         */
        setPriority(NORM_PRIORITY);

        // Старт потока
        start();
    }
    // Обработчик строки
    private String ServerProcessor(String line, com.common.Connect member)
    {
        String result;

        if (line.toUpperCase().startsWith("SQL:"))
        {
            result = member.updateBase(line.substring(4).trim());
            return result;
        }

        return line.toUpperCase();
    }
    // раннер для вызова
    public void run()
    {
        // Параметры JDBC-коннекта с базой данных Postgres
        String url = "jdbc:postgresql://localhost:5432/laboratory_java";
        String login = "postgres";
        String password = "";

        try {
            // создаем обьект коннекта с базой данных Postgres
            com.common.Connect member = new com.common.Connect(url, login, password);

            // Определяем входной и выходной потоки сокета
            // для обмена данными с клиентом
            InputStream  sin  = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            DataInputStream  dis = new DataInputStream (sin );
            DataOutputStream dos = new DataOutputStream(sout);

            String line = null;
            while(true) {
                // Ожидание сообщения от клиента
                line = dis.readUTF();

                System.out.println(String.format(TEMPL_MSG, num) + line);

                // Отсылаем клиенту обратно обработанну на сервере строку (она может быть той же самой)
                // Обработка строки перед отправкой
                line=ServerProcessor(line, member);
                System.out.println("Сервер отправил сообщение обратно:");
                System.out.println(line);
                dos.writeUTF(line);

                // Завершаем передачу данных
                dos.flush();
                System.out.println();

                if (line.equalsIgnoreCase("quit")) {
                    // завершаем соединение с БД
                    member.closeBase();

                    // закрываем сокет
                    socket.close();

                    System.out.println(String.format(TEMPL_CONN, num));
                    break;
                }
            }
        }
        // обработчик ошибок
        catch(Exception e) {
            System.out.println("Exception : " + e);
        }
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public static void main(String[] ar)
    {
        ServerSocket srvSocket = null;
        try {
            try {
                int i = 0; // Счётчик подключений

                // Подключение сокета к localhost
                InetAddress ia = InetAddress.getByName("localhost");
                srvSocket = new ServerSocket(port, 0, ia);

                System.out.println("Старт работы сервера \n\n");

                while(true) {
                    // ожидание подключения
                    Socket socket = srvSocket.accept();
                    System.err.println("Клиент акцептован");

                    // Стартуем обработку клиента в отдельном потоке
                    new Server().setSocket(i++, socket);
                }
            }
            // секция обработки ошибок
            catch(Exception e) {
                System.out.println("Exception: " + e);
            }
        } finally {
            try {
                // финализация - закрытие сокета на сервере
                if (srvSocket != null)
                    srvSocket.close();
            }
            // секция обработки ошибок
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
}
