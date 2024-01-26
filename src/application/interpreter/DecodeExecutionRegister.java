package application.interpreter;

public class DecodeExecutionRegister extends PipelineRegister{
	
	private InstructionType instructionType;
	private Register destRegister;
	private int value1;
	private int value2;
	
	public InstructionType getInstructionType() {
		return instructionType;
	}
	
	public void setInstructionType(InstructionType instructionType) {
		this.instructionType = instructionType;
	}
	
	public Register getDestRegister() {
		return destRegister;
	}
	
	public void setDestRegister(Register destRegister) {
		this.destRegister = destRegister;
	}
	
	public int getValue1() {
		return value1;
	}
	
	public void setValue1(int value1) {
		this.value1 = value1;
	}
	
	public int getValue2() {
		return value2;
	}
	
	public void setValue2(int value2) {
		this.value2 = value2;
	}
}
