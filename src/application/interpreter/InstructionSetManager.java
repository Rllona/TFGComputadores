package application.interpreter;

import java.util.HashMap;
import java.util.List;

public class InstructionSetManager{
	
	MIPSInterpreter interpreter;
	List<String> instructionsTypeRList = List.of("add", "sub", "addi");
	List<String> instructionsTypeMemList = List.of("lw", "sw");
	List<String> instructionsTypeJumpList = List.of("j");
	List<String> instructionsTypeBranchList = List.of("beq", "bne");
	
	public InstructionSetManager(MIPSInterpreter interpreter){
		this.interpreter = interpreter;
	}
	
	
	//Getters y Setters para la lista de registros del interprete
	
	public Register getRegister(String reg) {
		Register r = interpreter.registers.get(getIndexOfRegister(reg));
		return r;
	}
	
	public void setRegister(Register reg, int value) {
		reg.setValue(value);
		interpreter.registers.set(getIndexOfRegister(reg.getId()), reg);
	}
	
	public int getRegisterValue(String reg) {
		int value = interpreter.registers.get(getIndexOfRegister(reg)).getValue();
		return value;
	}
	
	public int getIndexOfRegister(String reg) {
		int index = Integer.parseInt(reg.substring(1));
		return index;
	}
	
	public void resetPipelineRegister() {
		interpreter.fdregister.setInstructionIndex(-1);
		interpreter.fdregister.setTotalInsIndex(-1);
	}
	
	
	//Identificación Tipos de Instrucción
	
	public boolean isTypeR(String opcode) {
		return (instructionsTypeRList.contains(opcode));
	}
	
	public boolean isTypeMem(String opcode) {
		return (instructionsTypeMemList.contains(opcode));
	}
	
	public boolean isTypeBranch(String opcode) {
		return (instructionsTypeBranchList.contains(opcode));
	}
	
	public boolean isTypeJump(String opcode) {
		return (instructionsTypeJumpList.contains(opcode));
	}
	
	
	//Repertorio de Instrucciones
	
	public int add(int value1, int value2) {
		return value1 + value2;
	}
	
	public int sub(int value1, int value2) {
		return value1 - value2;
	}
	
	public void jump(String label) {
		if(!interpreter.branchBuffer.containsKey(interpreter.deregister.getInstructionIndex())) {
			HashMap<String, Integer> labels = interpreter.getLabels();
			interpreter.pc = labels.get(label);
			resetPipelineRegister();
		}else {
			BranchTargetBuffer buffer = interpreter.branchBuffer.get(interpreter.deregister.getInstructionIndex());
			if (interpreter.branchPredictor == 1) {
				if(buffer.getPredictionState().equals("0")) {
					HashMap<String, Integer> labels = interpreter.getLabels();
					interpreter.pc = labels.get(label);
					resetPipelineRegister();
				}
			}else if (interpreter.branchPredictor == 2) {
				if (buffer.getPredictionState().equals("00") || buffer.getPredictionState().equals("01")) {
					HashMap<String, Integer> labels = interpreter.getLabels();
					interpreter.pc = labels.get(label);
					resetPipelineRegister();
				}
			}
		}
	}
	
	public boolean beq(int value1, int value2, String label) {
		boolean branchTaken = false;
		if(value1 == value2) {
			jump(label);
			branchTaken = true;
		}else {
			evaluateBranchNotTaken();
		}
		return branchTaken;
	}
	
	public boolean bne(int value1, int value2, String label) {
		boolean branchTaken = false;
		if(value1 != value2) {
			jump(label);
			branchTaken = true;
		}else {
			evaluateBranchNotTaken();
		}
		return branchTaken;
	}
	
	public void evaluateBranchNotTaken() {
		if(interpreter.branchBuffer.containsKey(interpreter.deregister.getInstructionIndex())) {
			BranchTargetBuffer buffer = interpreter.branchBuffer.get(interpreter.deregister.getInstructionIndex());
			if (interpreter.branchPredictor == 1) {
				if(buffer.getPredictionState().equals("1")) {
					interpreter.pc = interpreter.deregister.getInstructionIndex() + 1;
					resetPipelineRegister();
				}
			}else if (interpreter.branchPredictor == 2) {
				if (buffer.getPredictionState().equals("11") || buffer.getPredictionState().equals("10")) {
					interpreter.pc = interpreter.deregister.getInstructionIndex() + 1;
					resetPipelineRegister();
				}
			}
		}
	}
}
