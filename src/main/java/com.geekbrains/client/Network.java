package com.geekbrains.client;

import com.geekbrains.CommonConstants;
import com.geekbrains.server.Server;
import com.geekbrains.server.ServerCommandConstants;
import com.geekbrains.server.authorization.JdbcApp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class Network {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private final ChatController controller;

    public Network(ChatController chatController) {
        this.controller = chatController;
    }

    private void startReadServerMessages() throws IOException {     //прошлый openconn, моменрт с которого сервер начинает читать сообщения и пересылать
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String messageFromServer = inputStream.readUTF();
                        //Server.LOGGER.info(messageFromServer);
                        if (messageFromServer.startsWith(ServerCommandConstants.ENTER)) {
                            String[] client = messageFromServer.split(" ");
                            controller.displayClient(client[1]);
                            controller.displayMessage("Пользователь " + client[1] + " зашел в чат");
                        } else if (messageFromServer.startsWith(ServerCommandConstants.EXIT)) {
                            String[] client = messageFromServer.split(" ");
                            controller.removeClient(client[1]);
                            controller.displayMessage("Пользователь " + client[1] + " покинул чат");
                        } else if (messageFromServer.contains(ServerCommandConstants.PRIVATE)) {
                            String[] client = messageFromServer.split(" ", 5);
                            controller.displayMessage(client[0] + " private " + client[3] + client[4]);
                            // КОГДА ПРИХОДИТ СООБЩЕНИЕ С СЕРВЕРА CHANGENICK ВЫБИРАЕМ ОТТУДА СТАРЫЙ И НОВЫЙ НИК
                            // И ОТПРАВЛЯЕМ В ЧАТ КОНТРОЛЛЕР СТАРЫЙ И НОВЫЙ НИК
                        } else if (messageFromServer.contains(ServerCommandConstants.CHANGENICK)) {
                            String[] client = messageFromServer.split(" ");
                            String[] oldN = client[0].split(":");
                            controller.chN(oldN[0], client[2]);
                            controller.setClientList(oldN[0], client[2]);
                            controller.displayMessage(oldN[0] + " сменил ник на " + client[2]);
                        } else if (messageFromServer.startsWith(ServerCommandConstants.CLIENTS)) {
                            String[] client = messageFromServer.split(" ");
                            for (int i = 1; i < client.length; i++) {
                                controller.displayClient(client[i]);
                            }
                        } else {
                            controller.displayMessage(messageFromServer);
                        }
                    }
                } catch (IOException exception) {
                    Server.LOGGER.error(exception);
                }
            }
        }).start();
    }

    private void initializeNetwork() throws IOException {
        socket = new Socket(CommonConstants.SERVER_ADDRESS, CommonConstants.SERVER_PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }


    public void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException exception) {
            Server.LOGGER.error(exception);
        }
    }

    public boolean sendAuth(String login, String password) {
        try {
            if (socket == null || socket.isClosed()) {
                initializeNetwork();
            }
            outputStream.writeUTF(ServerCommandConstants.AUTHENTICATION + " " + login + " " + password);
            boolean authenticated = inputStream.readBoolean();
            if (authenticated) {
                startReadServerMessages();
            }
            return authenticated;
        } catch (IOException e) {
            Server.LOGGER.error(e);
        }
        return false;
    }

    public void closeConnection() {
        try {
            outputStream.writeUTF(ServerCommandConstants.EXIT);
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException exception) {
            Server.LOGGER.error(exception);
        }

        System.exit(1);
    }

}
