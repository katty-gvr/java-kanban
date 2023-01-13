import managers.HistoryManager;
import managers.impl.InMemoryHistoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;
    private int id = 0;
    public int generateId() {
        return ++id;
    }

    protected Task createTask() {
        return new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
    }

    @BeforeEach
    public void beforeEach() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddTaskToHistory() { // добавление в историю - стандартное поведение
        Task task = createTask();
        historyManager.addTask(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    void shouldNotAddTheSameTaskToHistory() { // добавление в историю - дублирование
        Task task = createTask();
        historyManager.addTask(task);
        historyManager.addTask(task);
        final List<Task> history = historyManager.getHistory();
        assertEquals(task, task);
        assertEquals(1, history.size());
    }

    @Test
    void shouldRemoveTaskFromTheBeginningOfTheHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId1 = generateId();
        task1.setId(newTaskId1);
        Task task2 = new Task("Задача 2", "Описание 3", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId2 = generateId();
        task2.setId(newTaskId2);
        Task task3 = new Task("Задача 3", "Описание 3", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId3 = generateId();
        task3.setId(newTaskId3);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);
        historyManager.remove(task1.getId());
        final List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task2,history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveTaskFromTheMiddleOfTheHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId1 = generateId();
        task1.setId(newTaskId1);
        Task task2 = new Task("Задача 2", "Описание 3", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId2 = generateId();
        task2.setId(newTaskId2);
        Task task3 = new Task("Задача 3", "Описание 3", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId3 = generateId();
        task3.setId(newTaskId3);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);
        historyManager.remove(task2.getId());
        final List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task1,history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveTaskFromTheEndOfTheHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId1 = generateId();
        task1.setId(newTaskId1);
        Task task2 = new Task("Задача 2", "Описание 3", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId2 = generateId();
        task2.setId(newTaskId2);
        Task task3 = new Task("Задача 3", "Описание 3", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId3 = generateId();
        task3.setId(newTaskId3);

        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);
        historyManager.remove(task3.getId());
        final List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task1,history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void shouldNotToRemoveTaskWithWrongId() {
        Task task = createTask();
        int newTaskId = generateId();
        task.setId(newTaskId);
        historyManager.addTask(task);
        historyManager.remove(0);

        assertEquals(List.of(task), historyManager.getHistory());
    }

    @Test
    void shouldReturnHistory() {
        Task task1 = createTask();
        int newTaskId1 = generateId();
        task1.setId(newTaskId1);
        Task task2 = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        int newTaskId2 = generateId();
        task2.setId(newTaskId2);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        final List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не пустая.");
        assertEquals(2, history.size(), "История не пустая.");
    }

    @Test
    void shouldReturnEmptyHistory() {
        final List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }
}
