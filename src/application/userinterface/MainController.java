package application.userinterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

import application.interpreter.*;

public class MainController {

	@FXML
	private TextArea inputCodeArea;
	@FXML
    private TableView<Register> registersTable;
	@FXML
    private TableColumn<Register, String> registersIdCol;
    @FXML
    private TableColumn<Register, Integer> registersValueCol;
	
	public void onExecuteButtonDown(ActionEvent e) {
		MIPSInterpreter interpreter = new MIPSInterpreter();
		interpreter.Run(inputCodeArea.getText());
		
		loadRegistersTable(interpreter.getRegisters());
	}
	
	public void loadRegistersTable(List<Register> regs) {
		ObservableList<Register> regsList = FXCollections.observableArrayList(regs);
		
		registersIdCol.setCellValueFactory(new PropertyValueFactory<Register, String>("id"));
		registersValueCol.setCellValueFactory(new PropertyValueFactory<Register, Integer>("value"));
		
		registersTable.setItems(regsList);
	}
}
