package com.geekbrains.client;

import com.geekbrains.server.Server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class History implements Serializable {
    private static String path;
    private static File file;
    private static ArrayList<String> list;

    public History(File file, String path, List<String> list) throws IOException {
        this.path = path;
        this.file = new File(path);
        this.list = (ArrayList<String>) list;
    }


    public void addMessage(String message) {
        if (list.size() <= 100) {
            list.add(message);
        } else {
            list.remove(0);
            list.add(message);
        }
    }

    public ArrayList<String> getMessage() {
        return list;
    }

   /* public void serialize() throws IOException, NoClassDefFoundError {
        try (
                FileOutputStream fOS = new FileOutputStream(path, false);
                ObjectOutputStream oOS = new ObjectOutputStream(fOS)) {
            oOS.writeObject(this.list);
            oOS.close();
            fOS.close();
        }
    }

    public String deserialize() throws NoClassDefFoundError, EOFException {
        if (file.length() != 0) {
            try (FileInputStream fIS = new FileInputStream(path);
                 ObjectInputStream oIS = new ObjectInputStream(fIS)) {
                this.list = (ArrayList) oIS.readObject();
                fIS.close();
                oIS.close();
            } catch (IOException | ClassNotFoundException e) {
                Server.LOGGER.error(e);     //logger hw3-6-3*
            }
        *//*    for (String message : this.list) {
                System.out.println(message);
            }*//*
        }
        return null;
    }*/

}
