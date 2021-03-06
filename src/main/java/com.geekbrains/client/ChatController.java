package com.geekbrains.client;


import com.geekbrains.server.Server;
import com.geekbrains.server.ServerCommandConstants;
import com.geekbrains.server.authorization.JdbcApp;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ChatController implements Initializable {
    @FXML
    private TextArea textArea;
    @FXML
    private TextField messageField, loginField, changeNickField;
    @FXML
    private HBox messagePanel, authPanel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ListView<String> clientList;
    @FXML
    public HBox changeNickPanel;

    private String path = "serverHistory.txt";
    private File file = new File(path);
    private ArrayList<String> historySrv = new ArrayList<String>();
    private History history;

    private final Network network;

    public ChatController() throws IOException {
        this.network = new Network(this);
    }

    public void setAuthenticated(boolean authenticated) {
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        messagePanel.setVisible(authenticated);
        messagePanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);
        changeNickPanel.setVisible(false);
        changeNickPanel.setManaged(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAuthenticated(false);
    }


    public void displayMessage(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                int oldText = textArea.getLength() + 1;
                String[] newText = text.split(" ");
                int selectText = oldText + newText[0].length() + newText[1].length() + 1;
                if (textArea.getText().isEmpty()) {
                    textArea.setText(text);
                } else {
                    textArea.setText(textArea.getText() + "\n" + text);
                    //???????????????????????? ??????????????
                    try {
                        history.addMessage(text);
                        history.serialize();
                    } catch (IOException e) {
                        Server.LOGGER.error(e);
                    }
                }
                if (text.contains(" private ")) {
                    textArea.selectRange(oldText, selectText);
                }
            }
        });
    }


    public void displayClient(String nickName) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientList.getItems().add(nickName);
            }
        });
    }


    public void removeClient(String nickName) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                clientList.getItems().remove(nickName);
            }
        });
    }


    public void sendAuth(ActionEvent event) throws IOException, RuntimeException {
        boolean authenticated = network.sendAuth(loginField.getText(), passwordField.getText());
        if (authenticated) {
            loginField.clear();
            passwordField.clear();
            setAuthenticated(true);
//3.2 ???????????????????????????? ???? ?????????? ?????????????? ?????? ?????????? ?? ??????
            if (!file.exists()) {
                file.createNewFile();
            }
            history = new History(file, path, historySrv);
            try {
                history.deserialize();
                historySrv = history.getMessage();
                for (String hstry : historySrv
                ) {
                    if (!(textArea.getText().isEmpty())) {
                        textArea.setText(textArea.getText() + "\n" + hstry);
                    } else {
                        textArea.setText(hstry);
                    }
                }
            } catch (EOFException e) {
                Server.LOGGER.error(e);
            }
        }
    }


    public void sendMessage(ActionEvent event) {
        network.sendMessage(messageField.getText());
        messageField.clear();
    }

    public void close() {
        network.closeConnection();
    }

    public void handleMouseClick(MouseEvent mouseEvent) {
        String nickName = clientList.getSelectionModel().getSelectedItem();
        messageField.setText(ServerCommandConstants.PRIVATE + " " + nickName + " ");
    }

    //?????????????? 2.2
    // ???????????????????? ???????????? ?????? ?????????? ???????????? ????????
    public void changeNick(ActionEvent event) {
        changeNickPanel.setVisible(true);
        changeNickPanel.setManaged(true);
    }

    //?????????? ???????????????? ?????????? ???? ???????????? JDBC ?????? ?????????? ???????? ?? ???????? ????????????
    public void chN(String lastNick, String newNick) {
        try {
            JdbcApp.updateEx(lastNick, newNick);
        } catch (SQLException e) {
            Server.LOGGER.error(e);
            Server.LOGGER.error("???????????? ???????????????????? ????????");
        }
    }

    //?????? ???????????? ???????? ?? ????????????????, ?? ???????? ?????????? ???????? ???????????????????? ?????????? ??????
    //********
    public void setClientList(String lastNick, String newNick) {
        Platform.runLater(new Runnable() {
                              @Override
                              public void run() {
                                  for (String client : clientList.getItems()) {
                                      if (client.equals(lastNick)) {
                                          displayClient(newNick);
                                          removeClient(lastNick);
                                      }
                                  }
                              }
                          }
        );
    }

    //?????????? ???????????????????? ?????????????????? ???? ???????????? ?? ???????????????? / ?????????????? ???????????? ?????? ?????????? ???????????? ????????????????
    public void changeNickOk(ActionEvent event) {
        network.sendMessage(ServerCommandConstants.CHANGENICK + " " + changeNickField.getText());
        changeNickPanel.setVisible(false);
        changeNickPanel.setManaged(false);
        changeNickField.clear();

    }


}

