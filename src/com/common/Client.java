package com.common;

import java.net.*;
import java.io.*;

public class Client
{
    private  static final int    serverPort = 5555; // порт для обмена сообщениями
    private  static final String localhost  = "127.0.0.1"; // сервер размещен локально

    public static void main(String[] ar)
    {
        Socket socket = null;
        try{
            try {
                System.out.println("Клиентский слой архитектуры \n" +
                        "Коннект к серверному уровню \n\t" +
                        "(IP  " + localhost +
                        ", port " + serverPort + ")");

                InetAddress ipAddress = InetAddress.getByName(localhost);
                socket = new Socket(ipAddress, serverPort);
                System.out.println("Соединение установлено.");

                System.out.println("\tLocalPort = " + socket.getLocalPort() +
                        "\n\tInetAddress.HostAddress = " + socket.getInetAddress().getHostAddress() +
                        "\n\tReceiveBufferSize = " + socket.getReceiveBufferSize());

                // Получаем входной и выходной потоки сокета для обмена сообщениями с сервером
                InputStream  sin  = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();

                DataInputStream  in  = new DataInputStream (sin );
                DataOutputStream out = new DataOutputStream(sout);

                // Создаем поток для чтения с клавиатуры.
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader keyboard = new BufferedReader(isr);

                String line = null;
                while (true) {
                    System.out.println("Введите данные с клавиатуры и нажмите Enter:");

                    // Пользователь должен ввести строку и нажать Enter
                    line = keyboard.readLine();
                    out.writeUTF(line);         // Отсылаем введенную строку серверу
                    out.flush();                // Завершаем поток
                    // line = in.readUTF();        // Ждем ответа от сервера

                    if (line.equalsIgnoreCase("quit")) // выход из цикла по quit
                    {
                        break;
                    }
                    else
                    {
                        line = in.readUTF();
                        System.out.println("С сервера получена строка:");
                        System.out.println(line);
                        System.out.println();
                    }

                }
            }
            // секция обработки ошибок
            catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            try {
                // финализируем - закрываем сокет
                if (socket != null)
                    socket.close();
            }
            // секция обработки эксепшен
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}