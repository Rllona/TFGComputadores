package application.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import application.userinterface.*;

public class MIPSInterpreter {
	
	private MainController controller;
	private InstructionSetManager IM;
	protected List<Register> registers;
	protected int[] memory = new int[1024];
	protected int pc;
	private int cycles;
	protected FetchDecodeRegister fdregister;
	protected DecodeExecutionRegister deregister;
	protected ExecutionMemoryRegister emregister;
	protected MemoryWriteBackRegister mwregister;
	private int lastInstructionCompleted;
	
	private HashMap<String, Integer> labels;
	
	public MIPSInterpreter(MainController controller) {
		this.controller = controller;
		IM = new InstructionSetManager(this);
		registers = new ArrayList<Register>();
        pc = 0;
        cycles = 0;
        fdregister = new FetchDecodeRegister();
        deregister = new DecodeExecutionRegister();
        emregister = new ExecutionMemoryRegister();
        mwregister = new MemoryWriteBackRegister();
        lastInstructionCompleted = -1;
        labels = new HashMap<String, Integer>();
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
	
	private void executeCode(String[] instructions) {
		//controller.addFirstDiagramColumn();
        while (codeNotEnded(instructions.length)) {

            //Etapa WriteBack
        	lastInstructionCompleted = mwregister.getInstructionIndex();
            if(mwregister.getInstructionIndex() != -1) {
            	writeBack();
            }
            
            //Etapa Memory
            mwregister.setInstructionIndex(emregister.getInstructionIndex());
            if(emregister.getInstructionIndex() != -1) {
	            memory();
            }
            
            //Etapa Execute
            emregister.setInstructionIndex(deregister.getInstructionIndex());
            if(deregister.getInstructionIndex() != -1) {
	            execute();
            }
            
            //Etapa Decode
            deregister.setInstructionIndex(fdregister.getInstructionIndex());
            if(fdregister.getInstructionIndex() != -1) {
	            decode();
            }
            
            //Etapa Fetch
            if(pc < instructions.length) {
            	fdregister.setInstructionIndex(pc);
                String instruction = instructions[pc];
                fetchInstruction(instruction);
                controller.addDiagramRow(pc-2, instruction);
                pc++;
            }else {
            	fdregister.setInstructionIndex(-1);
            }
            
            cycles++;
            controller.addDiagramColumn(cycles, fdregister.getInstructionIndex(), deregister.getInstructionIndex(), emregister.getInstructionIndex(), mwregister.getInstructionIndex(), lastInstructionCompleted);
        }
        System.out.println("Número de ciclos ejecutados: " + cycles);
    }
	
	private boolean codeNotEnded(int nInstructions) {
		return (pc < nInstructions || mwregister.getInstructionIndex() != -1 || emregister.getInstructionIndex() != -1 || deregister.getInstructionIndex() != -1 || fdregister.getInstructionIndex() != -1);
	}
	
	private void fetchInstruction(String instruction) {
		String[] parts = instruction.split("\\s+");
		String opcode = parts[0].toLowerCase();
		
		fdregister.setOpcode(opcode);
		fdregister.setParts(parts);
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
			if ("beq".equals(opcode)){
				IM.beq(deregister.getValue1(), deregister.getValue2(), deregister.getDestJump());
			}else if ("bne".equals(opcode)){
				IM.bne(deregister.getValue1(), deregister.getValue2(), deregister.getDestJump());
			}
			break;
			
		case typeJump:
			IM.jump(deregister.getDestJump());
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

	public void Run(String code) {
		//MIPSInterpreter interpreter = new MIPSInterpreter();
		String[] instructions = instructionsParser(code);
		
		for (int i = 0; i < instructions.length; i++) {
			System.out.println(instructions[i]);
		}
		
		executeCode(instructions);
	}
	
	private String[] instructionsParser(String code) {
		String[] lines = code.split("\n");
		int numIns = 0;
		
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();
			if(lines[i].contains(";")) {
				int semiColonIndex = lines[i].indexOf(";");
				lines[i] = lines[i].substring(0, semiColonIndex);
			}
			if(lines[i] != null && lines[i] != "" && lines[i] != " " && lines[i] != "\n") {
				numIns++;
			}
		}
		
		String[] instructions = new String[numIns];
		
		int index = 0;
		for (int i = 0; i < lines.length; i++) {
			if(lines[i] != null && lines[i] != "" && lines[i] != " " && lines[i] != "\n") {
				if(lines[i].contains(":")) {
					int colonIndex = lines[i].indexOf(":");
					labels.put(lines[i].substring(0, colonIndex).trim(), index);
					lines[i] = lines[i].substring(colonIndex + 1);
				}
				instructions[index] = lines[i].trim();
				index++;
			}
		}
		
		return instructions;
	}
}
