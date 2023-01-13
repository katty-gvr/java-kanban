import managers.impl.FileBackedTasksManager;
import managers.impl.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Task;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    public static final Path path = Path.of("data/data.csv");
    File file = new File(String.valueOf(path));

    @BeforeEach
    public void beforeEach() {
        taskManager = new FileBackedTasksManager(file);
    }

    @Test
    public void shouldCorrectlySaveAndLoad() {
        Task task = new Task("Задача 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 2, 1, 15, 00));
        taskManager.addNewTask(task);
        Epic epic = new Epic("Description", "Title", Status.NEW);
        taskManager.addNewEpic(epic);
        taskManager.getTask(task.getId());

        FileBackedTasksManager.loadFromFile(file);

        assertEquals(List.of(task), taskManager.getListOfTasks());
        assertEquals(List.of(epic), taskManager.getListOfEpics());
    }

    @Test
    public void shouldSaveAndLoadEmptyTasksEpicsSubtasks() {
        Epic epic = new Epic("Description", "Title", Status.NEW);
        taskManager.addNewEpic(epic);
        taskManager.getEpic(epic.getId());

        FileBackedTasksManager.loadFromFile(file);

        assertEquals(Collections.EMPTY_LIST, taskManager.getListOfTasks());
        assertEquals(List.of(epic), taskManager. getListOfEpics());
        assertEquals(Collections.EMPTY_LIST, taskManager.getListOfSubtasks());
    }

    @Test
    public void shouldSaveAndLoadEmptyHistory() {
        FileBackedTasksManager.loadFromFile(file);

        assertEquals(Collections.EMPTY_LIST, taskManager.getHistory());
    }
}
