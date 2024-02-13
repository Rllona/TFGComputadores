package application.userinterface;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

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
    @FXML
    private GridPane grid;
    @FXML
    private VBox fixedColumn;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ScrollPane fixedScrollPane;
    
    //Constantes:
    final double BOX_WIDTH = 70;
    final double BOX_HEIGHT = 30;
    final double FIRST_BOX_WIDTH = 140;
	
	public void onExecuteButtonDown(ActionEvent e) {
		initializeDiagram();
		
		MIPSInterpreter interpreter = new MIPSInterpreter(this);
		interpreter.Run(inputCodeArea.getText());
		
		loadRegistersTable(interpreter.getRegisters());
	}
	
	public void loadRegistersTable(List<Register> regs) {
		ObservableList<Register> regsList = FXCollections.observableArrayList(regs);
		
		registersIdCol.setCellValueFactory(new PropertyValueFactory<Register, String>("id"));
		registersValueCol.setCellValueFactory(new PropertyValueFactory<Register, Integer>("value"));
		
		registersTable.setItems(regsList);
	}
	
	private void initializeDiagram() {
		clearDiagram();
        scrollPane.vvalueProperty().bindBidirectional(fixedScrollPane.vvalueProperty());

        fixedColumn.setMinWidth(FIRST_BOX_WIDTH);
        fixedColumn.setPrefWidth(FIRST_BOX_WIDTH);
        fixedColumn.setMaxWidth(FIRST_BOX_WIDTH);
	}
	
	public void addDiagramRow(int nInstruction, String instruction) {
		grid.setMinHeight(BOX_HEIGHT * nInstruction);
		grid.setPrefHeight(BOX_HEIGHT * nInstruction);
		grid.setMaxHeight(BOX_HEIGHT * nInstruction);
		fixedColumn.setMinHeight(BOX_HEIGHT * nInstruction);
		fixedColumn.setPrefHeight(BOX_HEIGHT * nInstruction);
		fixedColumn.setMaxHeight(BOX_HEIGHT * nInstruction);
		addBox(0, nInstruction, instruction, "aliceblue", true);
	}
	
	public void addDiagramColumn(int nCycles, int instructionF, int instructionD, int instructionE, int instructionM, int instructionW) {
		grid.setMinWidth(BOX_WIDTH * nCycles);
		grid.setPrefWidth(BOX_WIDTH * nCycles);
		grid.setMaxWidth(BOX_WIDTH * nCycles);
		if(instructionF != -1) {
			addBox(nCycles, instructionF, "IF", "gold", false);
		}
		if(instructionD != -1) {
			addBox(nCycles, instructionD, "ID", "powderblue", false);
		}
		if(instructionE != -1) {
			addBox(nCycles, instructionE, "EX", "indianred", false);
		}
		if(instructionM != -1) {
			addBox(nCycles, instructionM, "MEM", "lightgreen", false);
		}
		if(instructionW != -1) {
			addBox(nCycles, instructionW, "WB", "plum", false);
		}
	}
	
	private void addBox(int col, int row, String text, String color, boolean isInstruction) {
        Pane box = new Pane();
        box.setStyle("-fx-border-color: darkgray; -fx-background-color: " + color + ";");        
        box.setMinWidth(isInstruction ? FIRST_BOX_WIDTH : BOX_WIDTH);
        box.setMaxWidth(isInstruction ? FIRST_BOX_WIDTH : BOX_WIDTH);  
        box.setMinHeight(BOX_HEIGHT);
        box.setMaxHeight(BOX_HEIGHT);
        
        Label label = new Label(text);
        label.setStyle("-fx-alignment: center;");
        label.setMinWidth(isInstruction ? FIRST_BOX_WIDTH : BOX_WIDTH);
        label.setMinHeight(BOX_HEIGHT);
        
        box.getChildren().add(label);
        
        if(isInstruction) {
        	fixedColumn.getChildren().add(box);
        }else {
        	grid.add(box, col, row);
        }
    }
	
	public void clearDiagram() {
		grid.getChildren().clear();
		grid.setMinHeight(0);
		grid.setPrefHeight(0);
		grid.setMaxHeight(0);
		grid.setMinWidth(0);
		grid.setPrefWidth(0);
		grid.setMaxWidth(0);
		fixedColumn.getChildren().clear();
		fixedColumn.setMinHeight(0);
		fixedColumn.setPrefHeight(0);
		fixedColumn.setMaxHeight(0);
	}
}
