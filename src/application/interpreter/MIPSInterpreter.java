package application.interpreter;

import java.util.ArrayList;
import java.util.List;

public class MIPSInterpreter {
	
	private InstructionSetManager IM;
	protected List<Register> registers;
	protected int pc;
	private int cycles;
	private FetchDecodeRegister fdregister;
	private DecodeExecutionRegister deregister;
	private ExecutionMemoryRegister emregister;
	private MemoryWriteBackRegister mwregister;
	
	public MIPSInterpreter() {
		IM = new InstructionSetManager(this);
		registers = new ArrayList<Register>();
        pc = 0;
        cycles = 0;
        fdregister = new FetchDecodeRegister();
        deregister = new DecodeExecutionRegister();
        emregister = new ExecutionMemoryRegister();
        mwregister = new MemoryWriteBackRegister();
        initializeRegisters();
    }
	
	public List<Register> getRegisters(){
		return registers;
	}
	
	private void initializeRegisters() {
		for (int i = 0; i < 32; i++) {
			Register r = new Register("R" + i, 0);
            registers.add(r);
        }
	}
	
	private void executeCode(String[] instructions) {
        while (pc < instructions.length || mwregister.getInstructionIndex() != -1) {

            //Etapa WriteBack
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
                pc++;
            }else {
            	fdregister.setInstructionIndex(-1);
            }
            
            cycles++;
        }
        System.out.println("Número de ciclos ejecutados: " + cycles);
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
			
		}else if(IM.isTypeJump(opcode)) {
			instructionType = InstructionType.typeJump;
			
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
			
			break;
			
		case typeJump:
			
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
		String[] instructions = code.split("\n");
		
		for (int i = 0; i < instructions.length; i++) {
			System.out.println(instructions[i]);
		}
		
		executeCode(instructions);
	}
}
