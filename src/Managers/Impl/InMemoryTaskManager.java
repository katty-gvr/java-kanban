package Managers.Impl;

import Managers.HistoryManager;
import Managers.Managers;
import Managers.TaskManager;
import Tasks.Epic;
import Tasks.Status;
import Tasks.Subtask;
import Tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    // 1. хранение задач
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    int generatorId = 0;
    HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // 2.1. Получение списков всех типов задач
    @Override
    public List<Task> getListOfTasks() {
        ArrayList<Task> tasksList = new ArrayList<>(tasks.values());
        return tasksList;
    }
    @Override
    public List<Epic> getListOfEpics() {
        ArrayList<Epic> epicsList = new ArrayList<>(epics.values());
        return epicsList;
    }
    @Override
    public List<Subtask> getListOfSubtasks() {
        // subtasks.put(1, subtask1);
        ArrayList<Subtask> subtasksList = new ArrayList<>(subtasks.values());
        return subtasksList;
    }

    // 2.2 Удаление списков всех типов задач
    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        // вычисление статуса эпиков
        List<Epic> epicsList = getListOfEpics();
        for(Epic epic : epicsList) {
            updateEpicStatus(epic);
        }
    }

    // 2.3 Получение по идентификатору

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.addTask(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.addTask(epic);
        return epic;
    }

    private Epic getEpicForSubtask(int id) {
        return epics.get(id);
    }

    @Override
    public Subtask getSubtask(int id)  {
        Subtask subtask = subtasks.get(id);
        historyManager.addTask(subtask);
        return subtask;
    }

    private Subtask getSubtaskForUpdateEpicStatus(int id) {
        return subtasks.get(id);
    }

    // 2.4 Создание - передача объекта в качестве параметра

    @Override
    public int addNewTask(Task task) {
        int id = ++generatorId;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = ++generatorId;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Epic currentEpic = getEpicForSubtask(subtask.getEpicId());
        if(currentEpic == null) {
            System.out.println("Такого эпика не существует!" + subtask.getEpicId());
            return -1;
        }
        Integer id = ++generatorId;
        subtask.setId(id);
        currentEpic.getSubtasksId().add(id);
        subtasks.put(id, subtask);
        updateEpicStatus(currentEpic);
        return id;
    }

    // 2.4 Обновление объектов

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        epic.setName(epic.getName());
        epic.setDescription(epic.getDescription());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic); // автоматический расчет статуса эпика на основании наличия или статуса его подзадач
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtasksId().isEmpty()) {
            epic.status = Status.NEW;
            return;
        }
        ArrayList<Status> statusOfSubtasksOfEpic = new ArrayList<>();
        ArrayList<Integer> subIDs = epic.getSubtasksId(); // вернули список с айди подзадач, входящих в эпик
        for(Integer subtaskId : subIDs) {
            Subtask sub = getSubtaskForUpdateEpicStatus(subtaskId);
            statusOfSubtasksOfEpic.add(sub.status);
        }
        int statusNew = 0;
        int statusDone = 0;
        int statusInProgress = 0;

        for (Status status : statusOfSubtasksOfEpic) {
            if (status.equals("NEW")) {
                statusNew++;
            } else if (status.equals("DONE")) {
                statusDone++;
            } else if (status.equals("IN PROGRESS")) {
                statusInProgress++;
            }
        }
        if (statusDone == 0 && statusInProgress == 0) {
            epic.status = Status.NEW;
        } else if (statusNew == 0 && statusInProgress == 0) {
            epic.status = Status.DONE;
        } else {
            epic.status = Status.IN_PROGRESS;
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = getEpic(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        } else {
            System.out.println("Такого эпика не существует!");
        }
    }

    // 2.5 Удаление объекта по индентификатору

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = getEpic(id);
        ArrayList<Integer> subIds = epic.getSubtasksId();
        for(int i = 0; i < subIds.size(); i++) {
            Integer subtaskId = subIds.get(i);
            deleteSubtask(subtaskId);
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        subtasks.remove(id);
        historyManager.remove(id);
    }

    // 3.1 получение списка всех подзадач определенного эпика

    @Override
    public ArrayList<Task> getSubtasksOfEpic(Epic epic) {
        ArrayList<Task> subtasksOfEpic = new ArrayList<>();
        for(Integer subtaskId : epic.getSubtasksId()) {
            Subtask sub = getSubtask(subtaskId);
            subtasksOfEpic.add(sub);
        }
        return subtasksOfEpic;
    }

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }
}

