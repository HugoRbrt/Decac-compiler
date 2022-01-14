package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.OPP;
import fr.ensimag.ima.pseudocode.instructions.SUB;
import fr.ensimag.ima.pseudocode.instructions.BOV;

/**
 * @author gl49
 * @date 01/01/2022
 */
public class Minus extends AbstractOpArith {
    public Minus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    public void codeGenOperations(Register Reg1, Register storedRegister, DecacCompiler compiler){
        //to do 'a-b' with :
        //a : Reg1
        //b : storedRegister
        //return storedRegister
        compiler.addInstruction(new SUB(Reg1, storedRegister));
        compiler.addInstruction(new OPP(storedRegister, storedRegister));
        if(super.getLeftOperand() instanceof FloatLiteral || super.getRightOperand() instanceof FloatLiteral){
            compiler.addInstruction(new BOV(compiler.getErrorManager().getErrorLabel("float_arithmetic")));
        }
    }

    @Override
    protected String getOperatorName() {
        return "-";
    }
}
