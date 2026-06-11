package defaul;

import java.time.LocalDate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Task {
	int id;
	String title;
	Category category;
	LocalDate dueDate;
	String notes;
	BooleanProperty completed = new SimpleBooleanProperty(false);
	
	
	 public Task(int id, String title, Category category, LocalDate dueDate, String notes) {
		 	this.id = id;
	        this.title = title;
	        this.category = category;
	        this.dueDate = dueDate;
	        this.notes = notes;
	    }
	 
}
