package application.interpreter;

import java.util.HashMap;

public class InstructionSetManager{
	
	MIPSInterpreter interpreter;
	
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
		int index = Character.getNumericValue(reg.charAt(1));
		return index;
	}
	
	public void resetPipelineRegister() {
		interpreter.fdregister.setInstructionIndex(-1);
	}
	
	
	//Identificación Tipos de Instrucción
	
	public boolean isTypeR(String opcode) {
		return ("add".equals(opcode) || "sub".equals(opcode) || "addi".equals(opcode));
	}
	
	public boolean isTypeMem(String opcode) {
		return ("lw".equals(opcode) || "sw".equals(opcode));
	}
	
	public boolean isTypeBranch(String opcode) {
		return ("beq".equals(opcode) || "bne".equals(opcode));
	}
	
	public boolean isTypeJump(String opcode) {
		return "j".equals(opcode);
	}
	
	
	//Repertorio de Instrucciones
	
	public int add(int value1, int value2) {
		return value1 + value2;
	}
	
	public int sub(int value1, int value2) {
		return value1 - value2;
	}
	
	public void jump(String label) {
		HashMap<String, Integer> labels = interpreter.getLabels();
		interpreter.pc = labels.get(label);
		resetPipelineRegister();
	}
}
