package managers;

import httpServers.KVServer;
import managers.impl.HttpTaskManager;
import managers.impl.InMemoryHistoryManager;

import java.io.IOException;

public class Managers {

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getDefault() throws IOException, InterruptedException {
        return new HttpTaskManager("http://localhost:" + KVServer.PORT);
    }
}
