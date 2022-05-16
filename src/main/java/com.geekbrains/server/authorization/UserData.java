package com.geekbrains.server.authorization;

import com.geekbrains.server.ClientHandler;
import com.geekbrains.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserData {
    private final String login;
    private final String password;
    private final String nickName;
    private static Logger LOGGER = LogManager.getLogger(UserData.class);

    public UserData(String login, String password, String nickName) {
        this.login = login;
        this.password = password;
        this.nickName = nickName;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNickName() {
        return nickName;
    }

    public void info() {            //logger hw3-6-3*
        LOGGER.info("логин: " + this.login +  " ; пароль: "+ this.password + " ; никнейм:" + this.nickName);
    }
}