package fr.ensimag.ima.pseudocode.instructionsARM;

import fr.ensimag.ima.pseudocode.ARMRegister;
import fr.ensimag.ima.pseudocode.ARMStackInstruction;

public class push extends ARMStackInstruction {
    public push(ARMRegister ... reg) {
        super(reg);
    }
    
}
