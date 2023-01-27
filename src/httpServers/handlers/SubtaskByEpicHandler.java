package httpServers.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import tasks.Epic;

import java.io.IOException;
import java.io.OutputStream;

public class SubtaskByEpicHandler implements HttpHandler {
    private final Gson gson = new Gson();
    private final TaskManager taskManager;

    public SubtaskByEpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        int statusCode = 0;
        String response;
        String method = httpExchange.getRequestMethod();
        String path = String.valueOf(httpExchange.getRequestURI());

        System.out.println("Обрабатывается запрос " + path + " с методом " + method);

        if ("GET".equals(method)) {
            String query = httpExchange.getRequestURI().getQuery();
            try {
                int id = Integer.parseInt(query.substring(query.indexOf("id=") + 3));
                Epic epic = taskManager.getEpic(id);
                if (epic != null) {

                    response = gson.toJson(taskManager.getSubtasksOfEpic(epic));
                } else {
                    response = "Эпика с id " + id + " не существует.";
                }
                statusCode = 200;

            } catch (StringIndexOutOfBoundsException | NullPointerException e) {
                response = "В запросе отсутствует необходимый параметр - id";
            } catch (NumberFormatException e) {
                response = "Неверный формат id";
            }
        } else {
            statusCode = 400;
            response = "Некорректный запрос";
        }
        httpExchange.sendResponseHeaders(statusCode, 0);

        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
