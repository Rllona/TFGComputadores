package application.interpreter;

public class PipelineRegister {
	
	private int instructionIndex = -1;
	private String opcode;
	
	public int getInstructionIndex() {
		return instructionIndex;
	}
	
	public void setInstructionIndex(int instructionIndex) {
		this.instructionIndex = instructionIndex;
	}
	
	public String getOpcode() {
		return opcode;
	}
	
	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}
}
