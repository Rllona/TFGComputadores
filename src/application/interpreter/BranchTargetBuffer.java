package application.interpreter;

public class BranchTargetBuffer {
	
	private String predictionState;
	private int targetAddress;
	
	public BranchTargetBuffer(String predictionState, int targetAddress) {
		this.predictionState = predictionState;
		this.targetAddress = targetAddress;
	}
	
	public String getPredictionState() {
		return predictionState;
	}
	
	public void setPredictionState(String predictionState) {
		this.predictionState = predictionState;
	}
	
	public int getTargetAddress() {
		return targetAddress;
	}
	
	public void setTargetAddress(int targetAddress) {
		this.targetAddress = targetAddress;
	}
}
