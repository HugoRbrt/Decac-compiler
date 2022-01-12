package fr.ensimag.ima.pseudocode;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.POP;


/**
 * Register operand (including special registers like SP).
 *
 * @author Ensimag
 * @date 01/01/2022
 */
public class Register extends DVal {
    private String name; 
    /**
     * number of given registers (16 by default)
     */
    private static int maxIndex = 16;
    
    /**
     * current index for a supposedly available register
    */
    private int currentIndex = 2;
    
    /**
     * stackAnalyzer to operate increments on
    *
    * private StackAnalyzer stackAnalyzer;
    *
    *public setStackAnalyzer(StackAnalyzer stackAnalyzer) {
    *    this.stackAnalyzer = stackAnalyzer;
    *}
    */
    
    /**
    * public constructor to access them more easily
    */
    public Register() {
        this(16);
    }
    
    /**
     * @param maxIndex : total number of registers
     */
    public Register(int maxIndex) {
        this("Register Bench", maxIndex);
    }
    
    protected Register(String name) {
        this(name, 16);
    }
    
    protected Register(String name, int maxIndex) {
        this.name = name;
        this.maxIndex = maxIndex;
    }
       


    @Override
    public String toString() {
        return name;
    }

    /**
     * Global Base register
     */
    public static final Register GB = new Register("GB");
    /**
     * Local Base register
     */
    public static final Register LB = new Register("LB");
    /**
     * Stack Pointer
     */
    public static final Register SP = new Register("SP");
    /**
     * General Purpose Registers. Array is private because Java arrays cannot be
     * made immutable, use getR(i) to access it.
     */
    protected static final GPRegister[] R = initRegisters();
    /**
     * General Purpose Registers
     */
    public static GPRegister getR(int i) {
        return R[i];
    }
    /**
     * Convenience shortcut for R[0]
     */
    public static final GPRegister R0 = R[0];
    /**
     * Convenience shortcut for R[1]
     */
    public static final GPRegister R1 = R[1];

    static private GPRegister[] initRegisters() {
        GPRegister [] res = new GPRegister[maxIndex];
        for (int i = 0; i <= maxIndex; i++) {
            res[i] = new GPRegister("R" + i, i);
        }
        return res;
    }

    
    /**
     * @return a register. This register is taken as the first available
     * register. If no such register is found, we return the last one
     * and indicate that it will need to be pushed
     */
    public GPRegister getRegister(DecacCompiler compiler){
        
        for (int k = currentIndex; k <= maxIndex; k++) {
            // if the register is available
            if (R[k].available()) {
                // we make it unavailable and say that we do not need to push it
                R[k].use();
                R[k].setNeedPush(false);
                // we update the index for a supposedly free register
                currentIndex = k+1;
                return R[k];
            }
        }
        
        // if we arrive here, no available register was found
        // in this case, we take the last register and push it
        // before using it
        GPRegister pushedRegister = R[maxIndex-1]; // for now
        assert !(pushedRegister.available());
        pushedRegister.setNeedPush(true);
        
        compiler.addInstruction(new PUSH(pushedRegister));
        /* stackAnalyzer.incrCountPush(1) */
        
        return R[maxIndex-1];
    }
    
    /**
     * free register given in argument and set its needPush field to false
     */
    public void freeRegister(GPRegister usedRegister, DecacCompiler compiler) {
        
        if (usedRegister.getNeedPush()) {
            // we POP the register but this register still contains info
            compiler.addInstruction(new POP(usedRegister));
            /* stackAnalyzer.incrCountPop(1) */
        } else {
            // we do not need to pop and just make it free
            usedRegister.free();
            currentIndex = usedRegister.getNumber();
        }
        usedRegister.setNeedPush(false);
    }


}
