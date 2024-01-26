module TFGComputadores {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.base;
	
	opens application to javafx.graphics, javafx.fxml;
	opens application.userinterface to javafx.graphics, javafx.fxml;
	opens application.interpreter to javafx.graphics, javafx.fxml, javafx.base;
}
