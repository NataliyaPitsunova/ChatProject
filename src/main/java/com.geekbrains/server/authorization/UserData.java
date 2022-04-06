package com.geekbrains.server.authorization;

import com.geekbrains.server.Server;

public class UserData {
    private final String login;
    private final String password;
    private final String nickName;

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

    public void info() {
        Server.LOGGER.info("логин: " + this.login +  " ; пароль: "+ this.password + " ; никнейм:" + this.nickName);
    }
}