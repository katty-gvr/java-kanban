package Managers;

import Tasks.Epic;
import Tasks.Subtask;
import Tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    // История просмотров
    List<Task> getHistory();

    // 2.1. Получение списков всех типов задач

    List<Task> getListOfTasks();
    List<Epic> getListOfEpics();
    List<Subtask> getListOfSubtasks();

    // 2.2 Удаление списков всех типов задач

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    // 2.3 Получение по идентификатору

    Task getTask(int id);
    Epic getEpic(int id);
    Subtask getSubtask(int id);

    // 2.4 Создание - передача объекта в качестве параметра

    int addNewTask(Task task);

    int addNewEpic(Epic epic);

    Integer addNewSubtask(Subtask subtask);

    // 2.4 Обновление объектов

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    // 2.5 Удаление объекта по индентификатору

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    // 3.1 получение списка всех подзадач определенного эпика

    List<Task> getSubtasksOfEpic(Epic epic);

}




