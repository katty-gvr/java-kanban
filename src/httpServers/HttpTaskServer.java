package httpServers;

import com.sun.net.httpserver.HttpServer;
import httpServers.handlers.*;
import managers.HistoryManager;
import managers.Managers;
import managers.TaskManager;
import managers.impl.FileBackedTasksManager;
import managers.impl.HttpTaskManager;
import managers.impl.InMemoryTaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final HttpServer httpServer;
    private static final int PORT = 8080;
    TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException, InterruptedException {
        //TaskManager taskManager = Managers.getDefault();
        //HttpTaskManager taskManager = Managers.getDefault();
        //TaskManager taskManager = new InMemoryTaskManager();
        //FileBackedTasksManager taskManager = new FileBackedTasksManager(new File("data/data.csv"));

        //taskManager = Managers.getDefault();
        this.taskManager = taskManager;

        this.httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks/task", new TaskHandler(taskManager));
        httpServer.createContext("/tasks/subtask", new SubtaskHandler(taskManager));
        httpServer.createContext("/tasks/epic", new EpicHandler(taskManager));
        httpServer.createContext("/tasks/subtask/epic", new SubtaskByEpicHandler(taskManager));
        httpServer.createContext("/tasks/history", new HistoryHandler(taskManager));
        httpServer.createContext("/tasks", new PrioritizedTasksHandler(taskManager));
        //start();
        //httpServer.start();
        //System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        //httpServer.stop(1);
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Сервер на порту " + PORT + " остановлен.");
    }

    /*private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HistoryManager historyManager = Managers.getDefaultHistory();
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2024, 2, 1, 15, 00));
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW,60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        taskManager.addNewTask(task1); // id 1
        taskManager.addNewTask(task2); // id 2

        Epic epic1 = new Epic("Эпик 1 с тремя подзадачами", "Эпик 1", Status.NEW);
        taskManager.addNewEpic(epic1); // id 3
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), 3);
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), 3);
        Subtask subtask3 = new Subtask("Подзадача 3 эпика 1", "Описание 3", Status.NEW, 60,
                LocalDateTime.of(2023, 3, 1, 15, 00), 3);

        taskManager.addNewSubtask(subtask1); // id 4
        taskManager.addNewSubtask(subtask2); // id 5
        taskManager.addNewSubtask(subtask3); // id 6

        Epic epic2 = new Epic("Эпик 2 без подзадач", "Эпик 2", Status.NEW);
        taskManager.addNewEpic(epic2); // id 7
        Task task3 = new Task("Задача без времени", "Описание", Status.NEW);
        taskManager.addNewTask(task3);


        HttpServer httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks/task", new TaskHandler(taskManager));
        httpServer.createContext("/tasks/subtask", new SubtaskHandler(taskManager));
        httpServer.createContext("/tasks/epic", new EpicHandler(taskManager));
        httpServer.createContext("/tasks/subtask/epic", new SubtaskByEpicHandler(taskManager));
        httpServer.createContext("/tasks/history", new HistoryHandler(taskManager));
        httpServer.createContext("/tasks", new PrioritizedTasksHandler(taskManager));


        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
        //httpServer.stop(1);


    }*/

    /*HttpClient client = HttpClient.newHttpClient();
    URI url = URI.create("http://localhost:8080/tasks/task/");
    HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());*/
}
