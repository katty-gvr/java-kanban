package tasks;

import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends Task {
    protected int epicId;

    public Subtask(String name, String description, Status status, long duration, LocalDateTime startTime, int epicId) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status, long duration, LocalDateTime startTime, int epicId) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return epicId == subtask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }
}
