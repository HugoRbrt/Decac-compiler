package fr.ensimag.ima.pseudocode.instructions;

import fr.ensimag.ima.pseudocode.BinaryInstructionDValToReg;
import fr.ensimag.ima.pseudocode.DVal;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.ImmediateInteger;

/**
 * @author Ensimag
 * @date 01/01/2022
 */
public class LOAD extends BinaryInstructionDValToReg {

    public LOAD(DVal op1, Register op2) {
        super(op1, op2);
    }

    public LOAD(int i, Register r) {
        this(new ImmediateInteger(i), r);
    }

}
