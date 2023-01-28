package managers.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import httpServers.KVTaskClient;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HttpTaskManager extends FileBackedTasksManager {
    final KVTaskClient client;
    private static final Gson gson = new Gson();

    public HttpTaskManager(String serverUrl) throws IOException, InterruptedException {
        super(null);
        client = new KVTaskClient(serverUrl);
    }

    @Override
    public void save() {
        client.put("tasks", gson.toJson(tasks.values()));
        client.put("subtasks", gson.toJson(subtasks.values()));
        client.put("epics", gson.toJson(epics.values()));
        List<Integer> historyIds = new ArrayList<>();
        for(Task task : historyManager.getHistory()) {
            historyIds.add(task.getId());
        }
        client.put("history", gson.toJson(historyIds));
    }

    public void loadFromServer() {
        JsonElement jsonTasks = JsonParser.parseString(client.load("tasks"));
        if (!jsonTasks.isJsonNull()) {
            JsonArray jsonTasksArray = jsonTasks.getAsJsonArray();
            for (JsonElement jsonTask : jsonTasksArray) {
                Task task = gson.fromJson(jsonTask, Task.class);
                readTasks(task);
            }
        }
        JsonElement jsonEpics = JsonParser.parseString(client.load("epics"));
        if (!jsonEpics.isJsonNull()) {
            JsonArray jsonEpicsArray = jsonEpics.getAsJsonArray();
            for (JsonElement jsonEpic : jsonEpicsArray) {
                Epic epic = gson.fromJson(jsonEpic, Epic.class);
                readTasks(epic);
            }
        }
        JsonElement jsonSubtasks = JsonParser.parseString(client.load("subtasks"));
        if (!jsonSubtasks.isJsonNull()) {
            JsonArray jsonSubtasksArray = jsonSubtasks.getAsJsonArray();
            for (JsonElement jsonSubtask : jsonSubtasksArray) {
                Subtask subtask = gson.fromJson(jsonSubtask, Subtask.class);
                readTasks(subtask);
            }
        }
        JsonElement jsonHistoryList = JsonParser.parseString(client.load("history"));
        List<Integer> historyIds = new ArrayList<>();
        if (!jsonHistoryList.isJsonNull()) {
            JsonArray jsonHistoryArray = jsonHistoryList.getAsJsonArray(); //получиили массив
            for (JsonElement jsonTaskId : jsonHistoryArray) {
                int taskId = jsonTaskId.getAsInt();
                historyIds.add(taskId);
            }
            readHistory(historyIds);
        }
    }
}


