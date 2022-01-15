package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.ADD;
import fr.ensimag.ima.pseudocode.instructions.BOV;

/**
 * @author gl49
 * @date 01/01/2022
 */
public class Plus extends AbstractOpArith {
    public Plus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    public void codeGenOperations(Register Reg1, Register storedRegister, DecacCompiler compiler){
        compiler.addInstruction(new ADD(Reg1, storedRegister));
        if(super.getLeftOperand() instanceof FloatLiteral || super.getRightOperand() instanceof FloatLiteral){
            compiler.addInstruction(new BOV(compiler.getErrorManager().getErrorLabel("float_arithmetic")));
        }
    }

    @Override
    protected String getOperatorName() {
        return "+";
    }
}
