import httpServers.KVServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException { // запуск сервера-хранилища
     KVServer kvServer = new KVServer();
     kvServer.start();
    }
}

