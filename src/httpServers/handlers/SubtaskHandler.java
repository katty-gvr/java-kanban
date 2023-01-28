package httpServers.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final TaskManager taskManager;

    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        int statusCode;
        String response;
        String method = httpExchange.getRequestMethod();

        switch (method) {
            case "GET":
                String query = httpExchange.getRequestURI().getQuery();
                if(query == null) {
                    statusCode = 200;
                    response = gson.toJson(taskManager.getListOfSubtasks());
                } else {
                    try {
                        int subtaskId = TaskManager.extractId(query);
                        Subtask subtask = taskManager.getSubtask(subtaskId);
                        if(subtask != null) {
                            response = gson.toJson(subtask);
                        } else {
                            response = "Подзадачи с id " + subtaskId + " не существует.";
                        }
                        statusCode = 200;
                    } catch (NumberFormatException e) {
                        statusCode = 400;
                        response = "id подзадачи введен некорректно.";
                    }
                }
                break;
            case "POST":
                String bodyRequest = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    Subtask subtask = gson.fromJson(bodyRequest, Subtask.class);
                    int subtaskId = subtask.getId();
                    if (taskManager.getTask(subtaskId) != null) {
                        taskManager.updateTask(subtask);
                        statusCode = 201;
                        response = "Задача с id=" + subtaskId + " обновлена";
                    } else {
                        taskManager.addNewSubtask(subtask);
                        int newSubtaskId = subtask.getId();
                        statusCode = 201;
                        response = "Создана подзадача с id " + newSubtaskId;
                    }
                } catch (JsonSyntaxException e) {
                    statusCode = 400;
                    response = "Неверный формат подзадачи";
                }
                break;
            case "DELETE":
                query = httpExchange.getRequestURI().getQuery();
                if(query == null) {
                    taskManager.deleteAllSubtasks();
                    statusCode = 200;
                    response = "Все подзадачи были успешно удалены.";
                } else {
                    try {
                        int subtaskId = TaskManager.extractId(query);
                        taskManager.deleteTask(subtaskId);
                        statusCode = 200;
                        response = "Удалена подзадача с id=" + subtaskId;
                    } catch (NumberFormatException e) {
                        statusCode = 400;
                        response = "id подзадачи введен некорректно.";
                    }
                }
                break;
            default:
                statusCode = 400;
                response = "Некорректный запрос";
        }

        httpExchange.sendResponseHeaders(statusCode, 0);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}

