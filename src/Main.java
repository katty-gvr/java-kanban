import Managers.Impl.InMemoryHistoryManager;
import Managers.Impl.InMemoryTaskManager;
import Managers.Managers;
import Tasks.Epic;
import Tasks.Status;
import Tasks.Subtask;
import Tasks.Task;
import Managers.TaskManager;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", Status.NEW);
        manager.addNewTask(task1); // id 1
        manager.addNewTask(task2); // id 2

        Epic epic1 = new Epic("Эпик 1 с тремя подзадачами", "Эпик 1", Status.NEW);
        manager.addNewEpic(epic1); // id 3
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 3);
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.NEW, 3);
        Subtask subtask3 = new Subtask("Подзадача 3 эпика 1", "Описание 3", Status.NEW, 3);

        manager.addNewSubtask(subtask1); // id 4
        manager.addNewSubtask(subtask2); // id 5
        manager.addNewSubtask(subtask3); // id 6

        Epic epic2 = new Epic("Эпик 2 без подзадач", "Эпик 2", Status.NEW);
        manager.addNewEpic(epic2); // id 7

        manager.getTask(1);
        manager.getEpic(7);
        manager.getSubtask(4);
        manager.getSubtask(5);
        manager.getTask(2);
        System.out.println(manager.getHistory());

        manager.getEpic(7);
        manager.getTask(1);
        manager.getEpic(3);
        manager.getSubtask(6);
        System.out.println(manager.getHistory());

        manager.deleteTask(2);
        System.out.println(manager.getHistory());

        manager.deleteEpic(3);
        System.out.println(manager.getHistory());
    }
}
