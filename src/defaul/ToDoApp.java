package defaul;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ToDoApp extends Application implements EventHandler<ActionEvent> {

	Button btnAddTask;
	Button btnConfirmTask;
	TextField taskInput;
	TextField dateInput;
	ListView<Task> taskList;
	String title;
	LocalDate dueDate;
	ComboBox<Category> categoryBox;
	String categoryText;
	Button btnDeleteTask;
	CheckBox cbxDueDate;
	String notes;
	TextField notesInput;
	DateTimeFormatter formatter;
	Button btnChangeScene;
	VBox layout2;
	Button btnZurück;
	Stage primaryStage;
	Scene scene;
	Scene scene2;
	java.sql.Connection conn;
	PreparedStatement pstmt;
	int id;

	@Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("To Do App");
        
        VBox layout = new VBox();
        layout2 = new VBox();
        
        
        layout.setAlignment(Pos.CENTER);
        layout2.setAlignment(Pos.CENTER);
        
        scene = new Scene(layout, 300, 350);
        scene.getStylesheets().add("file:src/defaul/style.css");
        primaryStage.setScene(scene);
        
        scene2 = new Scene(layout2, 300, 350);
        scene2.getStylesheets().add("file:src/defaul/style.css"); 
     
        primaryStage.show();
        
        
        
		taskList = new ListView<Task>();
		taskList.setCellFactory(lv -> new TaskCell(this));
		
		btnDeleteTask = new Button("Aufgabe löschen");
		btnDeleteTask.setId("btnDeleteTask");
		btnDeleteTask.setOnAction(e -> {
			try {
				deleteTask();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		
		btnChangeScene = new Button("+ Neue Aufgabe");
		btnChangeScene.setId("btnChangeScene");
	       btnChangeScene.setOnAction(e -> {
	        primaryStage.setScene(scene2);
	        changeScene();
	        	
	        });
	    
       
		
	
        layout.getChildren().add(taskList);
        layout.getChildren().add(btnDeleteTask);
        layout.getChildren().add(btnChangeScene);
              
        
        initDB();
        
        
       
    }

	public void initDB() {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = java.sql.DriverManager.getConnection("jdbc:sqlite:tasks.db");
			System.out.println("erfolgreich!");
			String sql = "CREATE TABLE IF NOT EXISTS tasks (" + "id INTEGER PRIMARY KEY," + "title TEXT NOT NULL,"
					+ "category TEXT," +
					"dueDate TEXT," +
					"notes TEXT)";
			conn.createStatement().execute(sql);
		} catch (Exception e) {
		    showAlert("Datenbankfehler: " + e.getMessage());
		}

		loadTasks();

	}

	public int insertTasks(Connection conn, String title, String category, String notes, LocalDate dueDate) throws SQLException {
	    String sql = "INSERT INTO tasks (title, category, dueDate, notes) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	        pstmt.setString(1, title);
	        pstmt.setString(2, category);
	        String dueDateStr = "";
	        if (dueDate != null) {
	            dueDateStr = dueDate.toString();
	        }
	        pstmt.setString(3, dueDateStr);
	        pstmt.setString(4, notes);
	        pstmt.executeUpdate();
	        ResultSet rs = pstmt.getGeneratedKeys();
	        if (rs.next()) {
	            return rs.getInt(1);
	        }
	    }
	    return -1;
	}

	public void loadTasks() {
		taskList.getItems().clear();
		String sql = "SELECT * FROM tasks";
		try (PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				 String d = rs.getString("dueDate");
				 LocalDate dueDate = null;
				 if (d != null && !d.isEmpty()) {
		                dueDate = LocalDate.parse(d);
		            }
				 
				 String c = rs.getString("category");
				 Category category = null;
				 for (Category cat : Category.values()) {
				     if (cat.toString().equals(c)) {
				         category = cat;
				     }
				 }
			
				 taskList.getItems().add(new Task(rs.getInt("id"), rs.getString("title"), category, dueDate, rs.getString("notes")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(ActionEvent event) {
	}

	static ArrayList<Task> tasks = new ArrayList<>();

	public void addTask() throws SQLException {
		title = taskInput.getText();
		if (title.isBlank()) {
		    showAlert("Titel darf nicht leer sein!");
		    return;
		}

		notes = notesInput.getText();

		Category category = categoryBox.getValue();
		if (category == null) {
			categoryText = " ";
		} else {
			categoryText = category.toString();
		}

		if (cbxDueDate.isSelected()) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
				dueDate = LocalDate.parse(dateInput.getText(), formatter);
			} catch (Exception e) {
			    showAlert("Ungültiges Datum! Bitte TT.MM.JJJJ eingeben.");
			    return;
			}

		} 

		
		id = insertTasks(conn, title, categoryText, notes, cbxDueDate.isSelected() ? dueDate : null);
		tasks.add(new Task(id, title, category, dueDate, notes));
		loadTasks();
	

	}

	public void deleteTask() throws SQLException {
		Task selected = taskList.getSelectionModel().getSelectedItem();
		if (selected == null) {
	        showAlert("Bitte zuerst eine Aufgabe auswählen!");
	        return;
	    }
	    deleteTaskById(selected.id);

		

	}
	
	public void deleteTaskById(int id) throws SQLException {
	    String sql = "DELETE FROM tasks WHERE id = ?";
	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, id);
	        pstmt.executeUpdate();
	    }
	    loadTasks();
	}

	public void clearTask() {
		taskInput.clear();
		dateInput.clear();
		notesInput.clear();
		cbxDueDate.setSelected(false);

	}

	public void changeScene() {
		taskInput = new TextField();
		taskInput.setPromptText("Titel eingeben");

		dateInput = new TextField();
		dateInput.setPromptText("Frist eingeben");
		dateInput.setVisible(false);

		cbxDueDate = new CheckBox();

		cbxDueDate.setOnAction(e -> {
			if (cbxDueDate.isSelected()) {
				dateInput.setVisible(true);

			} else {
				dateInput.setVisible(false);
			}
		});
		
		

		categoryBox = new ComboBox<>();
		categoryBox.getItems().addAll(Category.values());
		categoryBox.setPromptText("Kategorie");

		notesInput = new TextField();
		notesInput.setPromptText("Notizen");
		notesInput.setVisible(true);

		btnConfirmTask = new Button("Hinzufügen");
		btnConfirmTask.setId("btnConfirmTask");
		btnConfirmTask.setOnAction(e -> {
			try {
				addTask();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			clearTask();

		});

		btnZurück = new Button("Zurück");
		btnZurück.setId("btnZurück");
		btnZurück.setOnAction(e -> {
			primaryStage.setScene(scene);
			layout2.getChildren().clear();
		});

		layout2.getChildren().add(taskInput);
		layout2.getChildren().add(cbxDueDate);
		layout2.getChildren().add(dateInput);
		layout2.getChildren().add(categoryBox);
		layout2.getChildren().add(notesInput);
		layout2.getChildren().add(btnConfirmTask);
		layout2.getChildren().add(btnZurück);

	}
	
	public void showAlert(String message) {
	    Alert alert = new Alert(Alert.AlertType.ERROR);
	    alert.setTitle("Fehler");
	    alert.setHeaderText(null);
	    alert.setContentText(message);
	    alert.showAndWait();
	}

	public static void main(String[] args) {
		launch(args);
	}
}