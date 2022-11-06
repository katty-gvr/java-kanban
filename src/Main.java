import java.security.spec.ECPoint;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        Manager manager = new Manager();

        Task task1 = new Task("Задача 1", "Описание 1", "NEW");
        Task task2 = new Task("Задача 2", "Описание 2", "NEW");
        manager.addNewTask(task1); // id 1
        manager.addNewTask(task2); // id 2

        Epic epic1 = new Epic("Эпик 1", "Эпик 1", "NEW"); // id 3
        manager.addNewEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", "NEW", 3);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", "NEW", 3);
        manager.addNewSubtask(subtask1); // id 4
        manager.addNewSubtask(subtask2); // id 5

        Epic epic2 = new Epic("Эпик 2", "Эпик 2", "NEW");
        manager.addNewEpic(epic2); // id 6
        Subtask subtask3 = new Subtask("Подзадача 3", "Описание 3", "NEW", 6);
        manager.addNewSubtask(subtask3); // id 7

        System.out.println(manager.getListOfTasks());
        System.out.println(manager.getListOfSubtasks());
        System.out.println(manager.getListOfEpics());

        task1 = new Task(1,"Задача 1", "Описание 1", "DONE");
        manager.updateTask(task1);
        subtask1 = new Subtask(4, "Подзадача 1", "Описание 1", "DONE", 3);
        manager.updateSubtask(subtask1);
        subtask3 = new Subtask(7,"Подзадача 3", "Описание 3", "DONE", 6);
        manager.updateSubtask(subtask3);

        System.out.println(manager.getListOfTasks());
        System.out.println(manager.getListOfSubtasks());
        System.out.println(manager.getListOfEpics());

        manager.deleteTask(2);
        manager.deleteEpic(6);

        System.out.println(manager.getListOfTasks());
        System.out.println(manager.getListOfSubtasks());
        System.out.println(manager.getListOfEpics());

        epic1 = new Epic(3,"Обновление имени эпика 1", "Обновление описания эпика 1",
                "IN PROGRESS"); // попытка обновления эпика с присвоением статуса IN PROGRESS
        manager.updateEpic(epic1);
        System.out.println(manager.getListOfEpics()); // при печати статус эпика = NEW, т.к. у него еще нет подзадач

        // добавление подзадачи в обновленный эпик
        Subtask subtask4 = new Subtask("Подзадача 4", "Описание 4", "NEW", 3);
        manager.addNewSubtask(subtask4);
        System.out.println(manager.getListOfEpics()); // статус эпика по прежнему NEW, т.к. у его подзадачи статус NEW

    }
}
