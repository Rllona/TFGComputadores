package application.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import application.userinterface.*;

public class MIPSInterpreter {
	
	private MainController controller;
	private InstructionSetManager IM;
	private String[] instructions;
	protected List<Register> registers;
	//private HashMap<String, Integer> memoryVariables;
	protected int pc;
	protected int totalInsCounter;
	private int cycles;
	protected FetchDecodeRegister fdregister;
	protected DecodeExecutionRegister deregister;
	protected ExecutionMemoryRegister emregister;
	protected MemoryWriteBackRegister mwregister;
	private int lastInstructionCompleted;
	
	private HashMap<String, Integer> labels;
	protected int branchPredictor; // 0 -> Desactivado / 1 -> Predicción de salto de 1 bit / 2 -> Predicción de salto de 2 bit
	protected HashMap<Integer, BranchTargetBuffer> branchBuffer;
	private boolean lastBranchTaken;
	
	private boolean stallCycle;
	
	public MIPSInterpreter(MainController controller) {
		this.controller = controller;
		IM = new InstructionSetManager(this);
		instructions = new String[0];
		registers = new ArrayList<Register>();
		//memoryVariables = new HashMap<String, Integer>();
        pc = 0;
        totalInsCounter = 0;
        cycles = 0;
        fdregister = new FetchDecodeRegister();
        deregister = new DecodeExecutionRegister();
        emregister = new ExecutionMemoryRegister();
        mwregister = new MemoryWriteBackRegister();
        lastInstructionCompleted = -1;
        labels = new HashMap<String, Integer>();
        branchPredictor = controller.getBranchPredictionConfig();
        branchBuffer = new HashMap<Integer, BranchTargetBuffer>();
        lastBranchTaken = false;
        stallCycle = false;
        initializeRegisters();
    }
	
	public int getCycles() {
		return cycles;
	}
	
	public List<Register> getRegisters(){
		return registers;
	}
	
	public HashMap<String, Integer> getLabels(){
		return labels;
	}
	
	private void initializeRegisters() {
		for (int i = 0; i < 32; i++) {
			Register r = new Register("R" + i, 0);
            registers.add(r);
        }
	}
	
	public void runCompleteCode() {
		for (int i = 0; i < instructions.length; i++) {
			System.out.println(instructions[i]);
		}

		while (codeNotEnded(instructions.length)) {
			executeCycle(instructions);
		}
        System.out.println("Número de ciclos ejecutados: " + cycles);	
	}
	
	public void runCycle() {
		if (codeNotEnded(instructions.length)) {
			executeCycle(instructions);
		}else {
			System.out.println("Número de ciclos ejecutados: " + cycles);
			controller.isFirstCycle = true;
		}
	}
	
	private void executeCycle(String[] instructions) {
		//Etapa WriteBack
    	lastInstructionCompleted = mwregister.getTotalInsIndex();
        if(mwregister.getInstructionIndex() != -1) {
        	writeBack();
        }
        
        //Etapa Memory
        mwregister.setInstructionIndex(emregister.getInstructionIndex());
        mwregister.setTotalInsIndex(emregister.getTotalInsIndex());
        if(emregister.getInstructionIndex() != -1) {
            memory();
        }
        
        //Etapa Execute
        emregister.setInstructionIndex(deregister.getInstructionIndex());
        emregister.setTotalInsIndex(deregister.getTotalInsIndex());
        if(deregister.getInstructionIndex() != -1) {
            execute();
        }
        
        //Etapa Decode 	
        //Check for data hazards
        if(fdregister.getInstructionIndex() != -1) {
        	decode();
        	stallCycle = dataHazard();
        }
        
        if(!stallCycle) {
        	deregister.setInstructionIndex(fdregister.getInstructionIndex());
            deregister.setTotalInsIndex(fdregister.getTotalInsIndex());
            /*if(fdregister.getInstructionIndex() != -1) {
	            decode();
            }*/
        }else {
        	deregister.setInstructionIndex(-1);
            deregister.setTotalInsIndex(-1);
        }
        
        //Etapa Fetch
        if(!stallCycle) {
            if(pc < instructions.length) {
            	fdregister.setInstructionIndex(pc);
            	fdregister.setTotalInsIndex(totalInsCounter);
                String instruction = instructions[pc];
                fetchInstruction(instruction);
                if(!lastBranchTaken) {
                	pc++;
                }
                lastBranchTaken = false;
                totalInsCounter++;
                controller.addDiagramRow(totalInsCounter, instruction);
            }else {
            	fdregister.setInstructionIndex(-1);
            	fdregister.setTotalInsIndex(-1);
            }
        }
        cycles++;
        controller.addDiagramColumn(cycles, fdregister.getTotalInsIndex(), deregister.getTotalInsIndex(), emregister.getTotalInsIndex(), mwregister.getTotalInsIndex(), lastInstructionCompleted, stallCycle);
	}
	
	private boolean codeNotEnded(int nInstructions) {
		return (pc < nInstructions || mwregister.getInstructionIndex() != -1 || emregister.getInstructionIndex() != -1 || deregister.getInstructionIndex() != -1 || fdregister.getInstructionIndex() != -1);
	}
	
	private void fetchInstruction(String instruction) {
		String[] parts = instruction.split("\\s+");
		String opcode = parts[0].toLowerCase();
		
		fdregister.setOpcode(opcode);
		fdregister.setParts(parts);
		
		//Brach prediction
		if (branchPredictor == 1 || branchPredictor == 2) {
			if (branchBuffer.containsKey(pc)) {
				BranchTargetBuffer buffer = branchBuffer.get(pc);
				
				if (branchPredictor == 1) {
					if(buffer.getPredictionState().equals("1")) {
						pc = buffer.getTargetAddress();
						lastBranchTaken = true;
					}
				}else if (branchPredictor == 2) {
					if (buffer.getPredictionState().equals("11") || buffer.getPredictionState().equals("10")) {
						pc = buffer.getTargetAddress();
						lastBranchTaken = true;
					}
				}
			}
		}
    }
	
	private void decode() {
		String opcode = fdregister.getOpcode();
		deregister.setOpcode(opcode);
		String[] parts = fdregister.getParts();
		
		InstructionType instructionType = InstructionType.unknown;
		
		if(IM.isTypeR(opcode)) {
			instructionType = InstructionType.typeR;
			if("add".equals(opcode) || "sub".equals(opcode)) {
				deregister.setDestRegister(IM.getRegister(parts[1]));
				deregister.setValue1(IM.getRegisterValue(parts[2]));
				deregister.setValue2(IM.getRegisterValue(parts[3]));
				
			}else if("addi".equals(opcode)) {
				deregister.setDestRegister(IM.getRegister(parts[1]));
				deregister.setValue1(IM.getRegisterValue(parts[2]));
				deregister.setValue2(Integer.parseInt(parts[3]));
				
			}
		}else if(IM.isTypeMem(opcode)) {
			instructionType = InstructionType.typeMem;
			deregister.setDestRegister(IM.getRegister(parts[1]));
			
		}else if(IM.isTypeBranch(opcode)) {
			instructionType = InstructionType.typeBranch;
			deregister.setValue1(IM.getRegisterValue(parts[1]));
			deregister.setValue2(IM.getRegisterValue(parts[2]));
			deregister.setDestJump(parts[3]);
			
		}else if(IM.isTypeJump(opcode)) {
			instructionType = InstructionType.typeJump;
			deregister.setDestJump(parts[1]);
		}
		
		deregister.setInstructionType(instructionType);
	}
	
	private void execute() {
		InstructionType instructionType = deregister.getInstructionType();
		emregister.setInstructionType(instructionType);
		String opcode = deregister.getOpcode();
		emregister.setOpcode(opcode);
		emregister.setDestRegister(deregister.getDestRegister());
		
		int instructionIndex = deregister.getInstructionIndex();
		boolean branchTaken;
		
		switch (instructionType) {
		case typeR:
			if ("add".equals(opcode) || "addi".equals(opcode)) {
				emregister.setDestValue(IM.add(deregister.getValue1(), deregister.getValue2()));
				
			}else if("sub".equals(opcode)) {
				emregister.setDestValue(IM.sub(deregister.getValue1(), deregister.getValue2()));
			}
		
		case typeMem:
			
			break;
		
		case typeBranch:
			branchTaken = false;
			if ("beq".equals(opcode)){
				branchTaken = IM.beq(deregister.getValue1(), deregister.getValue2(), deregister.getDestJump());
				
			}else if ("bne".equals(opcode)){
				branchTaken = IM.bne(deregister.getValue1(), deregister.getValue2(), deregister.getDestJump());
			}
			
			//To do si se predice que se salta y se falla
			branchPrediction(branchTaken, instructionIndex);
			break;
			
		case typeJump:
			branchTaken = true;
			IM.jump(deregister.getDestJump());
			
			branchPrediction(branchTaken, instructionIndex);
			break;
			
		case unknown:
			// Manejar instrucciones no soportadas o desconocidas
            System.out.println("Instrucción no soportada: " + opcode);
			break;
		}
	}
	
	private void memory() {
		InstructionType instructionType = emregister.getInstructionType();
		//mwregister.setInstructionType(instructionType);
		String opcode = emregister.getOpcode();
		mwregister.setOpcode(opcode);
		mwregister.setDestRegister(emregister.getDestRegister());
		int destValue = emregister.getDestValue();
		mwregister.setDestValue(destValue);
		
		if(instructionType == InstructionType.typeMem) {
			
		}
	}
	
	private void writeBack() {
		//String opcode = mwregister.getOpcode();
		Register destRegister = mwregister.getDestRegister();
		int destValue = mwregister.getDestValue();
		
		IM.setRegister(destRegister, destValue);
	}
	
	public void instructionsParser(String code) {
		String[] lines = code.split("\n");
		int numIns = 0;
		boolean inDataSection = false;
		boolean inTextSection = false;
		int indexTextSectionStart = -1;
		
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();
			
			//Eliminar Comentarios
			if(lines[i].contains(";")) {
				int semiColonIndex = lines[i].indexOf(";");
				lines[i] = lines[i].substring(0, semiColonIndex);
			}
			
			//Parsear sección .data
			if(inDataSection && lines[i] != null && lines[i] != "" && lines[i] != " " && lines[i] != "\n") {
				if(lines[i].contains(":")) {
					int colonIndex = lines[i].indexOf(":");
					//memoryVariables.put(lines[i].substring(0, colonIndex).trim(), index);
					lines[i] = lines[i].substring(colonIndex + 1);
				}
			}
			//Parsear sección .text
			else if(inTextSection && lines[i] != null && lines[i] != "" && lines[i] != " " && lines[i] != "\n") {
				numIns++;
			}
			
			if(lines[i].equals(".data")) {
				inDataSection = true;
				inTextSection = false;
			}
			else if(lines[i].equals(".text")) {
				inDataSection = false;
				inTextSection = true;
				indexTextSectionStart = i + 1;
			}
		}
		
		instructions = new String[numIns];
		
		int index = 0;
		for (int i = indexTextSectionStart; i < lines.length; i++) {
			if(lines[i] != null && lines[i] != "" && lines[i] != " " && lines[i] != "\n") {
				if(lines[i].contains(":")) {
					int colonIndex = lines[i].indexOf(":");
					labels.put(lines[i].substring(0, colonIndex).trim(), index);
					lines[i] = lines[i].substring(colonIndex + 1);
				}
				lines[i] = lines[i].replace(',', ' ');
				instructions[index] = lines[i].trim();
				index++;
			}
		}
	}
	
	private boolean dataHazard() {
		boolean check = false;
		if(deregister.getInstructionType() == InstructionType.typeR) {
			if(
				compareRegId(2, emregister) ||
				compareRegId(2, mwregister) ||
				compareRegId(3, emregister) ||
				compareRegId(3, mwregister)			
			) {
				check = true;
			}
		}else if(deregister.getInstructionType() == InstructionType.typeBranch) {
			if(
				compareRegId(1, emregister) ||
				compareRegId(1, mwregister) ||
				compareRegId(2, emregister) ||
				compareRegId(2, mwregister)			
			) {
				check = true;
			}
		}
		return check;
	}
	
	private boolean compareRegId(int partIndex, ExecutionMemoryRegister reg) {
		boolean equalRegIds = false;
		if(reg.getInstructionIndex() != -1 && fdregister.getParts().length > 3 && fdregister.getParts()[partIndex].length() > 1) {
			equalRegIds = IM.getIndexOfRegister(fdregister.getParts()[partIndex]) == IM.getIndexOfRegister(reg.getDestRegister().getId());
		}
		return equalRegIds;
	}
	
	private boolean compareRegId(int partIndex, MemoryWriteBackRegister reg) {
		boolean equalRegIds = false;
		if(reg.getInstructionIndex() != -1 && fdregister.getParts().length > 3 && fdregister.getParts()[partIndex].length() > 1) {
			equalRegIds = IM.getIndexOfRegister(fdregister.getParts()[partIndex]) == IM.getIndexOfRegister(reg.getDestRegister().getId());
		}
		return equalRegIds;
	}
	
	private void branchPrediction(boolean branchTaken, int instructionIndex) {
		if (branchPredictor == 1 || branchPredictor == 2) {
			String state = "";
			BranchTargetBuffer buffer;
			if (branchPredictor == 1) {
				state = (branchTaken)? "1" : "0";
				
			}else if (branchPredictor == 2) {
				if (branchBuffer.containsKey(instructionIndex)) {
					String previousState = branchBuffer.get(instructionIndex).getPredictionState();
					switch (previousState) {
					case "00":
						state = (branchTaken)? "01" : "00";
						break;
					case "01":
						state = (branchTaken)? "10" : "00";
						break;
					case "10":
						state = (branchTaken)? "11" : "01";
						break;
					case "11":
						state = (branchTaken)? "11" : "10";
						break;
					}			
				}else {
					state = (branchTaken)? "10" : "01";
				}
			}
			
			if (branchBuffer.containsKey(instructionIndex)) {
				buffer = branchBuffer.get(instructionIndex);
				buffer.setPredictionState(state);
				branchBuffer.replace(instructionIndex, buffer);
			}else {
				buffer = new BranchTargetBuffer(state, labels.get(deregister.getDestJump()));
				branchBuffer.put(instructionIndex, buffer);
			}
		}
	}
}
