package httpServers.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TaskHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final TaskManager taskManager;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        int statusCode;
        String response;
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();

        System.out.println("Началась обработка " + method +  path + " запроса от клиента.");

        switch (method) {
            case "GET":
                String query = httpExchange.getRequestURI().getQuery();
                if (query == null) {
                    statusCode = 200;

                    String jsonString = gson.toJson(taskManager.getListOfTasks());
                    System.out.println("GET TASKS: " + jsonString);
                    response = gson.toJson(jsonString);
                } else {
                    try {
                        int taskId = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        Task task = taskManager.getTask(taskId);
                        if (task != null) {
                            response = gson.toJson(task);
                        } else {
                            response = "Задача с id " + taskId + " не найдена.";
                        }
                        statusCode = 200;
                    } catch (StringIndexOutOfBoundsException e) {
                        statusCode = 400;
                        response = "В запросе отсутствует необходимый параметр id";
                    } catch (NumberFormatException e) {
                        statusCode = 400;
                        response = "Неверный формат id";
                    }
                }
                break;

            case "POST":
                String bodyRequest = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    Task task = gson.fromJson(bodyRequest, Task.class);
                    int taskId = task.getId();
                    if (taskManager.getTask(taskId) != null) {
                        taskManager.updateTask(task);
                        statusCode = 201;
                        response = "Задача с id=" + taskId + " обновлена";
                    } else {
                        taskManager.addNewTask(task);
                        System.out.println("CREATED TASK: " + task);
                        int idCreated = task.getId();
                        statusCode = 201;
                        response = "Создана задача с id=" + idCreated;
                    }
                } catch (JsonSyntaxException e) {
                    statusCode = 400;
                    response = "Неверный формат запроса";
                }
                break;
            case "DELETE":
                query = httpExchange.getRequestURI().getQuery();
                if(query == null) {
                    taskManager.deleteAllTasks();
                    statusCode = 200;
                    response = "Все задачи были успешно удалены.";
                } else {
                    try {
                        int taskId = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                        taskManager.deleteTask(taskId);
                        statusCode = 200;
                        response = "Удалена задача с id=" + taskId;
                    } catch (StringIndexOutOfBoundsException e) {
                        statusCode = 400;
                        response = "В запросе отсутствует необходимый параметр id";
                    } catch (NumberFormatException e) {
                        statusCode = 400;
                        response = "Неверный формат id";
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



