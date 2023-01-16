package tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    protected List<Integer> subtaskIds = new ArrayList<>();

    LocalDateTime endTime;

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtaskIds=" + getSubtaskIds() +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + getDuration() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtaskIds, epic.subtaskIds) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskIds, endTime);
    }
}
