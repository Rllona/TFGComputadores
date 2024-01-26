package application.interpreter;

public class ExecutionMemoryRegister extends PipelineRegister{
	
	private InstructionType instructionType;
	private Register destRegister;
	private int destValue;
	
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
	
	public int getDestValue() {
		return destValue;
	}
	
	public void setDestValue(int destValue) {
		this.destValue = destValue;
	}
}
