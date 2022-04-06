package com.geekbrains.server;

import com.geekbrains.client.ChatController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    private String nickName;
    private String login;

    private File historyClient;

    public String getNickName() {
        return nickName;
    }

    public ClientHandler(ExecutorService executorService, Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        authentication();
                        readMessages();

                    } catch (IOException exception) {
                        Server.LOGGER.error(exception);             //logger hw3-6-3*
                    }
                }
            }).start();
        } catch (IOException exception) {
            throw new RuntimeException("Проблемы при создании обработчика");
        }
    }

    public void authentication() throws IOException {
        while (true) {
            String message = inputStream.readUTF();
            if (message.startsWith(ServerCommandConstants.AUTHENTICATION)) {
                String[] authInfo = message.split(" ");
                login = authInfo[1];
                String nickName = server.getAuthService().getNickNameByLoginAndPassword(authInfo[1], authInfo[2]);
                if (nickName != null) {
                    if (!server.isNickNameBusy(nickName)) {
                        sendAuthenticationMessage(true);
                        this.nickName = nickName;
                        server.broadcastMessage(ServerCommandConstants.ENTER + " " + nickName);
                        sendMessage(server.getClients());
                        server.addConnectedUser(this);
                        return;
                    } else {
                        sendAuthenticationMessage(false);
                    }
                } else {
                    sendAuthenticationMessage(false);
                }
            }
        }
    }

    private void sendAuthenticationMessage(boolean authenticated) throws IOException {
        outputStream.writeBoolean(authenticated);
    }

    private void readMessages() throws IOException {
        while (true) {
            String messageInChat = inputStream.readUTF();
           // Server.LOGGER.info("от " + nickName + ": " + messageInChat);
            if (messageInChat.equals(ServerCommandConstants.EXIT)) {
                closeConnection();
                return;
            }
            server.broadcastMessage(nickName + ": " + messageInChat);
        }
    }
//сохранение истории пользователя
    private void saveHistory(String message) throws IOException, ClassNotFoundException {
        historyClient = new File("history_" + login + ".txt");
        if (!historyClient.exists()) {
            historyClient.createNewFile();
        }
        try {
            PrintWriter fileWriter = new PrintWriter(new FileWriter(historyClient, true));
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(message + "\n");
            bufferedWriter.close();

        } catch (IOException e) {
            Server.LOGGER.error(e);         //logger hw3-6-3*
        }
    }


    public void sendMessage(String message) throws IOException {
        try {
            outputStream.writeUTF(message);
            //добавляем в историю
            saveHistory(message);
        } catch (IOException | ClassNotFoundException exception) {
            Server.LOGGER.error(exception);         //logger hw3-6-3*
        }
    }

    private void closeConnection() throws IOException {
        server.disconnectUser(this);
        server.broadcastMessage(ServerCommandConstants.EXIT + " " + nickName);
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException exception) {
            Server.LOGGER.error(exception);             //logger hw3-6-3*
        }
    }

}
