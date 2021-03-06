package com.geekbrains.server.authorization;

import com.geekbrains.server.ClientHandler;
import com.geekbrains.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.geekbrains.server.authorization.JdbcApp.*;

public class InMemoryAuthServiceImpl implements AuthService {
    public final Map<String, UserData> users;
    //ИСПРАВЛЕН InMemoryAuthServiceImpl ОБРАЩАЕТСЯ к main методу класса jbdc
    public InMemoryAuthServiceImpl() {
        users = new HashMap<>();
        main();
        for (int i = 0; i < logins.size(); i++) {
            users.put("login"+(i+1), logins.get(i));
        }
    }
    @Override
    public void start() {Server.LOGGER.info("Сервис аутентификации инициализирован");
    }
    @Override
    public synchronized String getNickNameByLoginAndPassword(String login, String password) {
        UserData user = users.get(login);
        // Ищем пользователя по логину и паролю, если нашли то возвращаем никнэйм
        if (user != null && user.getPassword().equals(password)) {
            return user.getNickName();
        }

        return null;
    }

    @Override
    public void end() {
        Server.LOGGER.info("Сервис аутентификации отключен");
    }
}
