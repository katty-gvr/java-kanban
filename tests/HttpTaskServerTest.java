import com.google.gson.Gson;
import httpServers.HttpTaskServer;
import managers.TaskManager;
import managers.impl.InMemoryTaskManager;
import org.junit.jupiter.api.*;
import tasks.Status;
import tasks.Task;
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest { //перед запуском тестов сделать запуск KV-сервера через Main

    Gson gson = new Gson();
    private static TaskManager taskManager;
    private static HttpTaskServer taskServer;


    @BeforeAll
    static void init() throws IOException, InterruptedException {
        taskManager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        taskServer.start();
    }

    @AfterAll
    static void stopServer() {
        taskServer.stop();
    }

    @Test
    void shouldGetTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2024, 2, 1, 15, 00));
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldGetSubtasks() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Эпик 1", "Эпик 1", Status.NEW);
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), gson.toJson(taskManager.getListOfSubtasks()));
    }

    @Test
    void shouldGetEpics() throws IOException, InterruptedException {
        Epic epic2 = new Epic("Эпик 2 без подзадач", "Эпик 2", Status.NEW);
        taskManager.addNewEpic(epic2); // id 7

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), gson.toJson(taskManager.getListOfEpics()));
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2024, 2, 1, 15, 00));
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task?id=" + task1.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task task = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode());
        assertEquals(task, taskManager.getTask(task1.getId()));
    }

    @Test
    void shouldGetSubtaskById() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Эпик 1 с тремя подзадачами", "Эпик 1", Status.NEW);
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), epic1.getId());

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask?id=" + subtask1.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Subtask subtask = gson.fromJson(response.body(), Subtask.class);

        assertEquals(200, response.statusCode());
        assertEquals(subtask, taskManager.getSubtask(subtask1.getId()));
    }

    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Эпик", "Эпик ", Status.NEW);
        taskManager.addNewEpic(epic1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic?id=" + epic1.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Epic epic = gson.fromJson(response.body(), Epic.class);

        assertEquals(200, response.statusCode());
        assertEquals(epic, taskManager.getEpic(epic1.getId()));
    }

    @Test
    void shouldAddNewTask() throws IOException,InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");

        Task task = new Task("Задача ", "Описание", Status.NEW, 60,
                LocalDateTime.of(2022, 10, 1, 15, 00));
        taskManager.addNewTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
    }

    @Test
    void shouldAddNewEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic/");

        Epic epic = new Epic("Эпик", "Эпик", Status.NEW);
        taskManager.addNewEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
    }

    @Test
    void shouldAddNewSubtask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Epic epic1 = new Epic("Эпик 1", "Эпик 1", Status.NEW);
        taskManager.addNewEpic(epic1);
        Subtask subtask = new Subtask("Подзадача", "Описание", Status.NEW, 60,
                LocalDateTime.of(2026, 3, 1, 15, 00), epic1.getId());
        taskManager.addNewSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
    }

    @Test
    void shouldGetSubtaskByEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Эпик 1 с подзадачами", "Эпик 1", Status.NEW);
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=" + epic1.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), gson.toJson(taskManager.getSubtasksOfEpic(epic1)));
    }

    @Test
    void shouldGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2024, 2, 1, 15, 00));
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic = new Epic("Эпик без подзадач", "Эпик", Status.NEW);
        taskManager.addNewEpic(epic);

        taskManager.getTask(task2.getId());
        taskManager.getEpic(epic.getId());
        taskManager.getTask(task1.getId());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), gson.toJson(taskManager.getHistory()));
    }

    @Test
    void shouldGetPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2024, 2, 1, 15, 00));
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic = new Epic("Эпик без подзадач", "Эпик", Status.NEW);
        taskManager.addNewEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.body(), gson.toJson(taskManager.getPrioritizedTask()));
    }

    @Test
    void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2024, 2, 1, 15, 00));
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldDeleteSubTaskById() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Эпик с подзадачами", "Эпик", Status.NEW);
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic2 = new Epic("Эпик 2 без подзадач", "Эпик 2", Status.NEW);
        taskManager.addNewEpic(epic2); // id 7

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=" + epic2.getId());

        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        assertEquals(200, response.statusCode());
        assertFalse(taskManager.getListOfEpics().contains(epic2));
    }

    @Test
    void shouldDeleteAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2024, 2, 1, 15, 00));
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getListOfTasks().isEmpty());
    }

    @Test
    void shouldDeleteAllSubtasks() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Эпик 1 с тремя подзадачами", "Эпик 1", Status.NEW);
        taskManager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), epic1.getId());
        Subtask subtask3 = new Subtask("Подзадача 3 эпика 1", "Описание 3", Status.NEW, 60,
                LocalDateTime.of(2023, 3, 1, 15, 00), epic1.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        taskManager.addNewSubtask(subtask3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getListOfSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteAllEpics() throws IOException, InterruptedException {
        Epic epic2 = new Epic("Эпик 2 без подзадач", "Эпик 2", Status.NEW);
        taskManager.addNewEpic(epic2); // id 7

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getListOfEpics().isEmpty());
    }
}
