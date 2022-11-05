import java.util.ArrayList;
import java.util.HashMap;

public class Manager {
    // 1. хранение задач
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    int generatorId = 0;

    // 2.1. Получение списков всех типов задач

    public ArrayList<Task> getListOfTasks() { // тест пройден
        ArrayList<Task> tasksList = new ArrayList<>(tasks.values());
        return tasksList;
    }
    public ArrayList<Epic> getListOfEpics() {
        ArrayList<Epic> epicsList = new ArrayList<>(epics.values());
        return epicsList;
    }
    public ArrayList<Subtask> getListOfSubtasks() {
        // subtasks.put(1, subtask1);
        ArrayList<Subtask> subtasksList = new ArrayList<>(subtasks.values());
        return subtasksList;
    }

    // 2.2 Удаление списков всех типов задач

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        // вычисление статуса эпиков
        ArrayList<Epic> epicsList = getListOfEpics();
        for(Epic epic : epicsList) {
            updateEpicStatus(epic);
        }
    }

    // 2.3 Получение по идентификатору

    public Task getTask(int id) {
        return tasks.get(id);
    }
    public Epic getEpic(int id) {
        return epics.get(id);
    }
    public Subtask getSubtask(int id) {

        return subtasks.get(id);
    }

    // 2.4 Создание - передача объекта в качестве параметра

    public int addNewTask(Task task) {
        int id = ++generatorId;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    public int addNewEpic(Epic epic) {
        int id = ++generatorId;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    public Integer addNewSubtask(Subtask subtask) {
       Epic currentEpic = getEpic(subtask.getEpicId());
        if(currentEpic == null) {
            System.out.println("Такого эпика не существует!" + subtask.getEpicId());
            return -1;
        }
        Integer id = ++generatorId;
        subtask.setId(id);
        currentEpic.subtasksId.add(id);
        subtasks.put(id, subtask);
        updateEpicStatus(currentEpic);
        return id;
    }

    // 2.4 Обновление объектов

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        updateEpicStatus(epic);
        epics.put(epic.getId(), epic);
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.subtasksId.isEmpty()) {
            epic.setStatus("NEW");
            return;
        }
        ArrayList<String> statusOfSubtasksOfEpic = new ArrayList<>();
        ArrayList<Integer> subIDs = epic.getSubtasksId(); // вернули список с айди подзадач, входящих в эпик
        for (int i = 0; i < subIDs.size(); i++) {
            Integer subtaskId = subIDs.get(i);
            Subtask sub = getSubtask(subtaskId);
            statusOfSubtasksOfEpic.add(sub.status);
        }
        int statusNew = 0;
        int statusDone = 0;
        int statusInProgress = 0;

        for (String status : statusOfSubtasksOfEpic) {
            if (status.equals("NEW")) {
                statusNew++;
            } else if (status.equals("DONE")) {
                statusDone++;
            } else if (status.equals("IN PROGRESS")) {
                statusInProgress++;
            }
        }
        if (statusDone == 0 && statusInProgress == 0) {
            epic.setStatus("NEW");
        } else if (statusNew == 0 && statusInProgress == 0) {
            epic.setStatus("DONE");
        } else {
            epic.setStatus("IN PROGRESS");
        }
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = getEpic(subtask.getEpicId());
        updateEpicStatus(epic);
    }

    // 2.5 Удаление объекта по индентификатору

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteEpic(int id) {
        Epic epic = getEpic(id);
        ArrayList<Subtask> subOfEpic = new ArrayList<>();
        ArrayList<Integer> subIds = epic.getSubtasksId();
      for(int i = 0; i < subIds.size(); i++) {
           Integer subtaskId = subIds.get(i);
            subtasks.remove(subtaskId);
        }
      epics.remove(id);
    }

    public void deleteSubtask(int id) {
        subtasks.remove(id);
    }

    // 3.1 получение списка всех подзадач определенного эпика

    public ArrayList<Task> getSubtasksOfEpic(Epic epic) {
        ArrayList<Task> subtasksOfEpic = new ArrayList<>();
        ArrayList<Integer> subIDs = epic.getSubtasksId();
        for(int i = 0; i < subIDs.size(); i++) {
            Integer subtaskId = subIDs.get(i);
            Subtask sub = getSubtask(subtaskId);
            subtasksOfEpic.add(sub);
            }
        return subtasksOfEpic;
        }
    }




