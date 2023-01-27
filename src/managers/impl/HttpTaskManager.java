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
        client.put("history", historyManager.getHistory().toString());
    }

    public void loadFromServer() {
        JsonElement jsonTasks = JsonParser.parseString(client.load("tasks"));
        if (!jsonTasks.isJsonNull()) {
            JsonArray jsonTasksArray = jsonTasks.getAsJsonArray();
            for (JsonElement jsonTask : jsonTasksArray) {
                Task task = gson.fromJson(jsonTask, Task.class);
                addNewTask(task);
            }
        }
        JsonElement jsonEpics = JsonParser.parseString(client.load("epics"));
        if (!jsonEpics.isJsonNull()) {
            JsonArray jsonEpicsArray = jsonEpics.getAsJsonArray();
            for (JsonElement jsonEpic : jsonEpicsArray) {
                Epic epic = gson.fromJson(jsonEpic, Epic.class);
                addNewEpic(epic);
            }
        }
        JsonElement jsonSubtasks = JsonParser.parseString(client.load("subtasks"));
        if (!jsonSubtasks.isJsonNull()) {
            JsonArray jsonSubtasksArray = jsonSubtasks.getAsJsonArray();
            for (JsonElement jsonSubtask : jsonSubtasksArray) {
                Subtask subtask = gson.fromJson(jsonSubtask, Subtask.class);
                this.addNewSubtask(subtask);
            }
        }
        JsonElement jsonHistoryList = JsonParser.parseString(client.load("history"));
        if (!jsonHistoryList.isJsonNull()) {
            JsonArray jsonHistoryArray = jsonHistoryList.getAsJsonArray();
            for (JsonElement jsonTaskId : jsonHistoryArray) {
                int taskId = jsonTaskId.getAsInt();
                if (this.subtasks.containsKey(taskId)) {
                    this.getSubtask(taskId);
                } else if (this.epics.containsKey(taskId)) {
                    this.getEpic(taskId);
                } else if (this.tasks.containsKey(taskId)) {
                    this.getTask(taskId);
                }
            }
        }
    }
}

