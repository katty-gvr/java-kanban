import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    // 1. хранение задач
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    int generatorId = 0;
    HistoryManager historyManager = new InMemoryHistoryManager();
    InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();


    // 2.1. Получение списков всех типов задач
    @Override
    public ArrayList<Task> getListOfTasks() {
        ArrayList<Task> tasksList = new ArrayList<>(tasks.values());
        return tasksList;
    }
    @Override
    public ArrayList<Epic> getListOfEpics() {
        ArrayList<Epic> epicsList = new ArrayList<>(epics.values());
        return epicsList;
    }
    @Override
    public ArrayList<Subtask> getListOfSubtasks() {
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
        ArrayList<Epic> epicsList = getListOfEpics();
        for(Epic epic : epicsList) {
            updateEpicStatus(epic);
        }
    }

    // 2.3 Получение по идентификатору

    @Override
    public Task getTask(int id) {
        historyManager.addTask(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpic(int id) {
        historyManager.addTask(epics.get(id));
        return epics.get(id);
    }

    public Epic getEpicForSubtask(int id) {
        return epics.get(id);
    }

    @Override
    public Subtask getSubtask(int id) {
        historyManager.addTask(subtasks.get(id));
        return subtasks.get(id);
    }

    public Subtask getSubtaskForUpdateEpicStatus(int id) {
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
        currentEpic.subtasksId.add(id);
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
        epic.setName(epic.name);
        epic.setDescription(epic.description);
        epics.put(epic.id, epic);
        updateEpicStatus(epic); // автоматический расчет статуса эпика на основании наличия или статуса его подзадач
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.subtasksId.isEmpty()) {
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
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = getEpic(id);
        ArrayList<Integer> subIds = epic.getSubtasksId();
        for(int i = 0; i < subIds.size(); i++) {
            Integer subtaskId = subIds.get(i);
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        subtasks.remove(id);
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

/*
    @Override
    public void updateHistory() {
        if(history.size() > 10) {
            history.remove(0);
        }
    }*/
}

