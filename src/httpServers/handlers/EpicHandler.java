package httpServers.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import tasks.Epic;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class EpicHandler implements HttpHandler {

    private final Gson gson = new Gson();
    private final TaskManager taskManager;

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        int statusCode = 0;
        String response;
        String method = httpExchange.getRequestMethod();

        switch (method) {
            case "GET":
                String query = httpExchange.getRequestURI().getQuery();
                if(query == null) {
                    statusCode = 200;
                    response = gson.toJson(taskManager.getListOfEpics());
                } else {
                    try {
                        int epicId = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        Epic epic = taskManager.getEpic(epicId);
                        if(epic != null) {
                            response = gson.toJson(epic);
                        } else {
                            response = "Эпика с id " + epicId + " не существует.";
                        }
                        statusCode = 200;
                    } catch (NumberFormatException e) {
                        statusCode = 400;
                        response = "id эпика введен некорректно.";
                    }
                }
                break;
            case "POST":
                String bodyRequest = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    Epic epic = gson.fromJson(bodyRequest, Epic.class);
                    int epicId = epic.getId();
                    if (taskManager.getEpic(epicId) != null) {
                        taskManager.updateEpic(epic);
                        statusCode = 201;
                        response = "Эпик с id " + epicId + " был успешно обновлен.";
                    } else {
                        taskManager.addNewEpic(epic);
                        int newEpicId = epic.getId();
                        statusCode = 201;
                        response = "Эпик с id " + newEpicId + " был успешно создан.";
                    }
                } catch (JsonSyntaxException e) {
                    statusCode = 400;
                    response = "Неверный формат эпика";
                }
                break;
            case "DELETE":
                query = httpExchange.getRequestURI().getQuery();
                if(query == null) {
                    taskManager.deleteAllEpics();
                    statusCode = 200;
                    response = "Все эпики были успешно удалены.";
                } else {
                    try {
                        int epicId = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        taskManager.deleteEpic(epicId);
                        statusCode = 200;
                        response = "Эпик с id " + epicId + " был успешно удален.";
                    } catch (NumberFormatException e) {
                        statusCode = 400;
                        response = "id эпика введен некорректно.";
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
