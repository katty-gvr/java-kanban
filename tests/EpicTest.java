import managers.TaskManager;
import managers.impl.InMemoryTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = new InMemoryTaskManager();
    }

    protected Epic createEpic() {
        return new Epic("Epic for test", "Description of epic", Status.NEW);
    }

    // a. Пустой список подзадач эпика
    @Test
    public void epicStatusShouldBeNewWhenSubtasksOfEpicAreEmpty() {
        Epic epic = createEpic();
        taskManager.addNewEpic(epic);
        final Epic savedEpic = taskManager.getEpic(epic.getId());

        assertEquals(Status.NEW, savedEpic.status); // NEW, т.к. у него еще нет подзадач
    }

    //   b.   Все подзадачи со статусом NEW.
    @Test
    public void epicStatusShouldBeNewWhenAllSubtasksOfEpicAreNew() {
        Epic epic = createEpic();
        taskManager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.NEW, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), epic.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        final Epic savedEpic = taskManager.getEpic(epic.getId());

        assertEquals(Status.NEW, savedEpic.status); // NEW, т.к. все его подзадачи NEW
    }

    //   c.    Все подзадачи со статусом DONE.
    @Test
    public void epicStatusShouldBeDoneWhenAllSubtasksOfEpicAreDone() {
        Epic epic = createEpic();
        taskManager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.DONE, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.DONE, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), epic.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        final Epic savedEpic = taskManager.getEpic(epic.getId());

        assertEquals(Status.DONE, savedEpic.status); // DONE, т.к. все его подзадачи DONE
    }

    //   d.    Подзадачи со статусами NEW и DONE.
    @Test
    public void epicStatusShouldBeInProgressWhenSubtasksOfEpicAreNewAndDone() {
        Epic epic = createEpic();
        taskManager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.NEW, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.DONE, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), epic.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        final Epic savedEpic = taskManager.getEpic(epic.getId());

        assertEquals(Status.IN_PROGRESS, savedEpic.status); // IN_PROGRESS, т.к. одна подзадача NEW, вторая - DONE
    }

    //   e.    Подзадачи со статусом IN_PROGRESS.
    @Test
    public void epicStatusShouldBeInProgressWhenSubtasksOfEpicAreInProgress() {
        Epic epic = createEpic();
        taskManager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1 эпика 1", "Описание 1", Status.IN_PROGRESS, 60,
                LocalDateTime.of(2023, 9, 1, 15, 00), epic.getId());
        Subtask subtask2 = new Subtask("Подзадача 2 эпика 1", "Описание 2", Status.IN_PROGRESS, 60,
                LocalDateTime.of(2023, 7, 1, 15, 00), epic.getId());
        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        final Epic savedEpic = taskManager.getEpic(epic.getId());

        assertEquals(Status.IN_PROGRESS, savedEpic.status); // IN_PROGRESS, т.к. все его подзадачи IN_PROGRESS
    }
}
