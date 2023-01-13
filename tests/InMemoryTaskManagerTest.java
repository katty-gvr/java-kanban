import managers.TaskManager;
import managers.impl.InMemoryTaskManager;
import managers.Managers;
import org.junit.jupiter.api.BeforeEach;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void beforeEach() {
      taskManager = new InMemoryTaskManager();
    }
}
