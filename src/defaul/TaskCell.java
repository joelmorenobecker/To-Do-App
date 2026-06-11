package defaul;

import java.time.format.DateTimeFormatter;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;

public class TaskCell extends ListCell<Task> {

    CheckBox cb = new CheckBox();
    ToDoApp app;

    public TaskCell(ToDoApp app) {
        this.app = app;
        setOnMouseClicked(e -> e.consume());
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);
        if (empty || task == null) {
            setGraphic(null);
        } else {
        	String dueDateText = task.dueDate != null ? " | " + task.dueDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) : "";
        	cb.setText(task.id + " | " + task.title + dueDateText + " | " + task.notes);
            cb.setOnAction(e -> {
            	e.consume();
                if (cb.isSelected()) {
                    try {
                        app.deleteTaskById(task.id);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            setGraphic(cb);
        }
    }
}