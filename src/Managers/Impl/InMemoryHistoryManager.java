package Managers.Impl;

import Managers.HistoryManager;
import Tasks.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    public LinkedList<Task> history = new LinkedList<>(); // список истории просмотров

    @Override
    public void addTask(Task task) {
        history.add(task);
        if (history.size() > 10) {
            history.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
