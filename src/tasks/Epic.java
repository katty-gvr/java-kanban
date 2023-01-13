package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    protected List<Subtask> subtasksOfEpic = new ArrayList<>();
    public List<Subtask> getSubtasksOfEpic() {
        return subtasksOfEpic;
    }

    public void setSubtasksOfEpic(List<Subtask> subtasksOfEpic) {
        this.subtasksOfEpic = subtasksOfEpic;
    }

    LocalDateTime endTime;
    LocalDateTime startTime;

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }


    @Override
    public LocalDateTime getStartTime() {

        List<Subtask> subtasksOfEpic = getSubtasksOfEpic();
        if(subtasksOfEpic.isEmpty()) {
            return null;
        }
        List<LocalDateTime> listWithTime = new ArrayList<>();
        for (Subtask subtask : subtasksOfEpic) {
            LocalDateTime subStartTime = subtask.getStartTime();
            listWithTime.add(subStartTime);
        }
        LocalDateTime startTime1 = listWithTime.get(0); // переменная для сравнения времени

        for (LocalDateTime dateTime : listWithTime) { // проходимся по списку времени
            if (dateTime.isBefore(startTime1) || dateTime.isEqual(startTime1)) {
                startTime = dateTime;
            }
        }
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {

        List<Subtask> subtasksOfEpic = getSubtasksOfEpic();
        if(subtasksOfEpic.isEmpty()) {
            return null;
        }
        List<LocalDateTime> listWithTime = new ArrayList<>();
        for (Subtask subtask : subtasksOfEpic) {
                LocalDateTime subEndTime = subtask.getEndTime();
                listWithTime.add(subEndTime);
        }
        LocalDateTime endTime1 = listWithTime.get(0);

        for (LocalDateTime dateTime : listWithTime) {
            if (dateTime.isAfter(endTime1) || dateTime.isEqual(endTime1)) {
                endTime = dateTime;
            }
        }
        return endTime;
    }

    @Override
    public long getDuration() {
        if (startTime != null && endTime != null) {
            Duration epicDuration = Duration.between(startTime, endTime);
            return epicDuration.toMinutes();
        } else {
            return 0;
        }
    }

    public void addSubtaskToEpic(Subtask subtask) {
        subtasksOfEpic.add(subtask);
    }

    public List<Integer> getSubtaskIds() {
        List<Integer> subIds = new ArrayList<>();
        for(Subtask subtask : subtasksOfEpic) {
            subIds.add(subtask.getId());
        }
        return subIds;
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
}
