package managers.impl;

import exceptions.ManagerLoadException;
import exceptions.ManagerSaveException;
import managers.HistoryManager;
import managers.TaskManager;
import tasks.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {
    private final File file;

    public FileBackedTasksManager(File file) {
        this.file = file;
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileManager = new FileBackedTasksManager(file);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            while (br.ready()) {
                String line = br.readLine();
                if (line.isEmpty()) {
                    List<Integer> history = historyFromString(br.readLine());
                    assert history != null;
                    fileManager.readHistory(history);
                    break;
                } else {
                    Task currentTask = fromString(line);
                    fileManager.readTasks(currentTask);
                }
            }
        }
        catch (IOException exception) {
            throw new ManagerLoadException("Ошибка чтения файла.");
        }
        return fileManager;
    }

    private void readHistory(List<Integer> history) {
        for (Integer currentId : history) {
            if (epics.containsKey(currentId)) {
                getEpicForFileLoad(currentId);
            } else if (subtasks.containsKey(currentId)) {
                getSubtaskForFileLoad(currentId);
            } else {
                getTaskForFileLoad(currentId);
            }
        }
    }

    private <T extends Task> void readTasks(Task currentTask) {
        if (currentTask instanceof Epic) {
            epics.put(currentTask.getId(), (Epic) currentTask);
        } else if (currentTask instanceof Subtask) {
            int epicId = ((Subtask) currentTask).getEpicId();
            Epic epic = getEpicForFileLoad(epicId);
            //epic.addSubtaskToEpic((Subtask) currentTask);
            epic.addSubtaskId(currentTask.getId());
            epics.put(epicId, epic);
            subtasks.put(currentTask.getId(), (Subtask) currentTask);
        } else {
            tasks.put(currentTask.getId(), currentTask);
        }
        if(currentTask.getId() > generatorId) {
            generatorId = currentTask.getId();
        }
    }

    void save() {
        try(Writer writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,duration,startTime,endTime,epic\n");
            writeTasks(getListOfTasks(), writer);
            writeTasks(getListOfEpics(), writer);
            writeTasks(getListOfSubtasks(), writer);
            writer.write("\n");
            writer.write(historyToString(historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи файла.");
        }
    }

    private <T extends Task> void writeTasks(List<T> tasks, Writer writer) throws IOException {
        for(Task task : tasks) {
            writer.write(toCsvString(task) + "\n");
        }
    }

    private String toCsvString(Task task) {
        String type = "TASK";
        String endOfString = "";

        if (task instanceof Epic) {
            type = "EPIC";

        } else if (task instanceof Subtask) {
            type = "SUBTASK";
            endOfString += ((Subtask) task).getEpicId();
        }
        String csvString = task.getId() + "," +
                type + "," +
                task.getName() + "," +
                task.status + "," +
                task.getDescription() + "," +
                task.getDuration() + "," +
                task.getStartTime() + "," +
                task.getEndTime() + ",";
        return csvString + endOfString;
    }

    private static Task fromString(String value) {
        String[] taskData = value.split(",");
        int id = Integer.parseInt(taskData[0]);
        TasksType tasksType = TasksType.valueOf(taskData[1]);
        String name = taskData[2];
        Status status = Status.valueOf(taskData[3]);
        String description = taskData[4];
        long duration = Long.parseLong(taskData[5]);
        LocalDateTime startTime;
        if (Objects.equals(taskData[6], "null")) {
             startTime = null;
        } else {
             startTime = LocalDateTime.parse(taskData[6]);
        }

        switch (tasksType) {
            case TASK:
                return new Task(id, name, description, status, duration, startTime);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                int epicId = Integer.parseInt(taskData[8]);
                return new Subtask(id, name, description, status, duration, startTime, epicId);
            default:
                return null;
        }
    }

     private String historyToString(HistoryManager manager) {
        List<String> taskIds  = new ArrayList<>();

        for (Task task : manager.getHistory()) {
            taskIds.add(String.valueOf(task.getId()));
        }
        return String.join(",", taskIds);
    }

    static List<Integer> historyFromString(String value) {
        if (value.isBlank()) {
            return null;
        } else {
            String[] idsString = value.split(",");

            List<Integer> tasksIds = new ArrayList<>();
            for (String idString : idsString) {
                tasksIds.add(Integer.valueOf(idString));
            }
            return tasksIds;
        }
    }


    @Override
    public List<Task> getHistory() {
        List<Task> historyList = super.getHistory();
        save();
        return historyList;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }

    private void getTaskForFileLoad (int id) {
        Task task = tasks.get(id);
        historyManager.addTask(task);
    }

    private Epic getEpicForFileLoad (int id) {
        Epic epic = epics.get(id);
        historyManager.addTask(epic);
        return epic;
    }
    private void getSubtaskForFileLoad (int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.addTask(subtask);
    }

    @Override
    public int addNewTask(Task task) {
        int taskId = super.addNewTask(task);
        save();
        return taskId;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int epicId = super.addNewEpic(epic);
        save();
        return epicId;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        int subtaskId = super.addNewSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public List<Task> getPrioritizedTask() {
        List<Task> tasksPriority = new ArrayList<>(prioritizedTasks);
        tasksPriority.addAll(tasksWithoutStartTime);
        return tasksPriority;
    }


    public static void main(String[] args) {
        FileBackedTasksManager manager = new FileBackedTasksManager(new File("data/data.csv"));
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2024, 2, 1, 15, 00));
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW,60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        manager.addNewTask(task1); // id 1
        manager.addNewTask(task2); // id 2

        Epic epic1 = new Epic("Эпик 1 с тремя подзадачами", "Эпик 1", Status.NEW);
        manager.addNewEpic(epic1); // id 3
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), 3);
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), 3);
        Subtask subtask3 = new Subtask("Подзадача 3 эпика 1", "Описание 3", Status.NEW, 60,
                LocalDateTime.of(2023, 3, 1, 15, 00), 3);

        manager.addNewSubtask(subtask1); // id 4
        manager.addNewSubtask(subtask2); // id 5
        manager.addNewSubtask(subtask3); // id 6

        Epic epic2 = new Epic("Эпик 2 без подзадач", "Эпик 2", Status.NEW);
        manager.addNewEpic(epic2); // id 7
        Task task3 = new Task("Задача без времени", "Описание", Status.NEW);
        manager.addNewTask(task3);

        // Проверка вывода сообщения о пересечении задач
        Task task4 = new Task("Задача 4", "Описание 4", Status.NEW, 30,
                LocalDateTime.of(2023, 1,30,12,30));
        Task task5 = new Task("Задача 5", "Описание 5", Status.NEW, 30,
                LocalDateTime.of(2023, 1,30,12,40));
        manager.addNewTask(task4);
        manager.addNewTask(task5);

        manager.getTask(1);
        manager.getEpic(7);
        manager.getSubtask(4);
        manager.getEpic(3);


        System.out.println(manager.getPrioritizedTask());

        FileBackedTasksManager manager2 = loadFromFile(new File("data/data.csv"));

    }
}
