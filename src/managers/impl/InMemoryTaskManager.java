package managers.impl;

import exceptions.ManagerValidateException;
import managers.HistoryManager;
import managers.Managers;
import managers.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    // 1. хранение задач
    int generatorId = 0;
    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    HashMap<Integer, Subtask> subtasks = new HashMap<>();
    HistoryManager historyManager = Managers.getDefaultHistory();

    Comparator<Task> comparator = new Comparator<>() {
        @Override
        public int compare(Task o1, Task o2) {

            return o1.getStartTime().compareTo(o2.getStartTime());
        }
    };
    Set<Task> prioritizedTasks = new TreeSet<>(comparator); // отсортированный по приоритету список задач
    List<Task> tasksWithoutStartTime = new ArrayList<>();


    private void validateTaskPriority(Task task) {
        checkTaskTime(task);
    }

    protected void checkTaskTime(Task task) {
        if (task.getStartTime() == null && task.getEndTime() == null) {
            tasksWithoutStartTime.add(task);
        } else {
            addToPrioritizedTask(task);
        }
    }

    private void addToPrioritizedTask(Task task) {
        if (isIntersected(task)) {
            throw new ManagerValidateException("Задача " + task.getId() + " пересекается с другой задачей. " +
                        "Измените время начала выполнения задачи.");
        } else {
            prioritizedTasks.add(task);
        }
    }

    private void checkTaskBeforeUpdating(Task task) {
        List<Task> tasksList = getPrioritizedTask();
        for(Task taskFromList : tasksList) {
            if(taskFromList.getId() == task.getId()) {
                prioritizedTasks.remove(taskFromList);
            }
        }
    }

    @Override
    public List<Task> getPrioritizedTask() {
        List<Task> tasksPriority = new ArrayList<>(prioritizedTasks);
        tasksPriority.addAll(tasksWithoutStartTime);
        return tasksPriority;
    }

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
        prioritizedTasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        prioritizedTasks.clear();
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
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.addTask(subtask);
        return subtask;
    }

    // 2.4 Создание - передача объекта в качестве параметра

    @Override
    public int addNewTask(Task task) {
        int id = ++generatorId;
        task.setId(id);
        tasks.put(id, task);
        validateTaskPriority(task);
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
        if (currentEpic == null) {
            System.out.println("Такого эпика не существует!" + subtask.getEpicId());
            return -1;
        }
        Integer id = ++generatorId;
        subtask.setId(id);
        currentEpic.getSubtaskIds().add(id);
        subtasks.put(id, subtask);
        validateTaskPriority(subtask);
        updateEpicStatus(currentEpic);
        updateEpicTime(currentEpic);
        return id;
    }

    // 2.4 Обновление объектов

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
        checkTaskBeforeUpdating(task);
        validateTaskPriority(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        epic.setName(epic.getName());
        epic.setDescription(epic.getDescription());
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic); // автоматический расчет статуса эпика на основании наличия или статуса его подзадач
        updateEpicTime(epic);
    }

    public void updateEpicTime(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(0);
        } else {
            epic.setStartTime(getStartTimeOfEpic(epic));
            epic.setEndTime(getEndTimeOfEpic(epic));
            epic.setDuration(getEpicDuration(epic));
        }
    }

    private Subtask getSubtaskForEpic(int id) {
        return subtasks.get(id);
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.status = Status.NEW;
            return;
        }
        List<Status> statusOfSubtasksOfEpic = new ArrayList<>();
        List<Integer> subIDs = epic.getSubtaskIds(); // вернули список с айди подзадач, входящих в эпик
        for(Integer subtaskId : subIDs) {
            Subtask sub = getSubtaskForEpic(subtaskId);
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
        checkTaskBeforeUpdating(subtask);
        validateTaskPriority(subtask);
        Epic epic = getEpic(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
            updateEpicTime(epic);
        } else {
            System.out.println("Такого эпика не существует!");
        }
    }

    // 2.5 Удаление объекта по индентификатору

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        prioritizedTasks.removeIf(task -> task.getId() == id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = getEpic(id);
        List<Integer> subtasksOfEpic = epic.getSubtaskIds();
        for (Integer subId : subtasksOfEpic) {
            subtasks.remove(subId);
        }
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Epic epic = getEpic(subtasks.get(id).getEpicId());
        epic.getSubtaskIds().remove((Integer) id);
        updateEpic(epic); // в том числе, обновление EpicTime
        subtasks.remove(id);
        prioritizedTasks.removeIf(subtask -> subtask.getId() == id);
        historyManager.remove(id);
    }

    // 3.1 получение списка всех подзадач определенного эпика

    @Override
    public List<Subtask> getSubtasksOfEpic(Epic epic) {
        List<Subtask> subtasksOfEpic = new ArrayList<>();
        for(Integer subtaskId : epic.getSubtaskIds()) {
            Subtask sub = getSubtaskForEpic(subtaskId);
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

    public LocalDateTime getStartTimeOfEpic(Epic epic) {

        List<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic);

        List<LocalDateTime> listWithTime = new ArrayList<>();
        for (Subtask subtask : subtasksOfEpic) {
            LocalDateTime subStartTime = subtask.getStartTime();
            listWithTime.add(subStartTime);
        }
        LocalDateTime epicStartTime = listWithTime.get(0); // переменная для сравнения времени

        for (LocalDateTime dateTime : listWithTime) { // проходимся по списку времени, сравниваем каждый элемент с первым
            if (dateTime.isBefore(epicStartTime)) {
                epicStartTime = dateTime;
            }
        }
        return epicStartTime;
    }

    public LocalDateTime getEndTimeOfEpic(Epic epic) {

        List<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic);

        List<LocalDateTime> listWithTime = new ArrayList<>();
        for (Subtask subtask : subtasksOfEpic) {
            LocalDateTime subEndTime = subtask.getEndTime();
            listWithTime.add(subEndTime);
        }
        LocalDateTime epicEndTime = listWithTime.get(0); // переменная для сравнения времени

        for (LocalDateTime dateTime : listWithTime) { // проходимся по списку времени, сравниваем каждый элемент с первым
            if (dateTime.isAfter(epicEndTime)) {
                epicEndTime = dateTime;
            }
        }
        return epicEndTime;
    }

    public long getEpicDuration(Epic epic) {
        List<Subtask> subtasksOfEpic = getSubtasksOfEpic(epic);
        long epicDuration = 0;
        for(Subtask subtask : subtasksOfEpic) {
            epicDuration += subtask.getDuration();
        }
        return epicDuration;
    }

    public boolean isIntersected(Task task) { // проверка пересечения
        List<Task> list = getPrioritizedTask();
        boolean isIntersected = false;

        for (Task sortedTask : list) {
            LocalDateTime startDate1 = task.getStartTime();
            LocalDateTime endDate1 = task.getEndTime();
            LocalDateTime startDate2 = sortedTask.getStartTime();
            LocalDateTime endDate2 = sortedTask.getEndTime();

            if (startDate2 != null && endDate2 != null) {
                isIntersected = isItCaseOfIntersection(startDate1, endDate1, startDate2, endDate2);
            }
            if (isIntersected) {
                break;
            }
        }
        return isIntersected;
    }

    public boolean isItCaseOfIntersection(LocalDateTime from1, LocalDateTime to1, LocalDateTime from2, LocalDateTime to2) {
        return ((from2.isBefore(from1) && to2.isAfter(from1)) || (from2.isAfter(from1) && from2.isBefore(to1))
                && (from1.isEqual(from2) || to1.isEqual(to2))) ;
    }
}

