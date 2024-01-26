package application.interpreter;

public class MemoryWriteBackRegister extends PipelineRegister{

	private Register destRegister;
	private int destValue;
	
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
