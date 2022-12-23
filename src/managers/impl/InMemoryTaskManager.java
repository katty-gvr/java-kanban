package managers.impl;

import managers.HistoryManager;
import managers.Managers;
import managers.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    // 1. хранение задач
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    HistoryManager historyManager = Managers.getDefaultHistory();
    int generatorId = 0;

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // 2.1. Получение списков всех типов задач
    @Override
    public List<Task> getListOfTasks() {
        return new ArrayList<>(tasks.values());
    }
    @Override
    public List<Epic> getListOfEpics() {
        return new ArrayList<>(epics.values());
    }
    @Override
    public List<Subtask> getListOfSubtasks() {
        return new ArrayList<>(subtasks.values());
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
        //allTaskIds.add(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = ++generatorId;
        epic.setId(id);
       // allTaskIds.add(id);
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
        currentEpic.getSubtaskIds().add(id);
        //allTaskIds.add(id);
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
        if (epic.getSubtaskIds().isEmpty()) {
            epic.status = Status.NEW;
            return;
        }
        ArrayList<Status> statusOfSubtasksOfEpic = new ArrayList<>();
        ArrayList<Integer> subIDs = epic.getSubtaskIds(); // вернули список с айди подзадач, входящих в эпик
        for(Integer subtaskId : subIDs) {
            Subtask sub = getSubtaskForUpdateEpicStatus(subtaskId);
            statusOfSubtasksOfEpic.add(sub.status);
        }
        int statusNew = 0;
        int statusDone = 0;
        int statusInProgress = 0;

        for (Status status : statusOfSubtasksOfEpic) {
            if (status == Status.NEW) {
                statusNew++;
            } else if (status == Status.DONE) {
                statusDone++;
            } else if (status == Status.IN_PROGRESS) {
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
        ArrayList<Integer> subIds = epic.getSubtaskIds();
        for (Integer subtaskId : subIds) {
            deleteSubtask(subtaskId);
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Epic epic = getEpic(subtasks.get(id).getEpicId());
        epic.getSubtaskIds().remove((Integer)id);
        updateEpic(epic);
        subtasks.remove(id);
        historyManager.remove(id);
    }

    // 3.1 получение списка всех подзадач определенного эпика

    @Override
    public ArrayList<Task> getSubtasksOfEpic(Epic epic) {
        ArrayList<Task> subtasksOfEpic = new ArrayList<>();
        for (Integer subtaskId : epic.getSubtaskIds()) {
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

