package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.*;
import fr.ensimag.ima.pseudocode.instructionsARM.*;
import fr.ensimag.ima.pseudocode.ARMRegister;
import java.io.PrintStream;

/**
 *
 * @author gl49
 * @date 01/01/2022
 */
public class ReadFloat extends AbstractReadExpr {

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type currentType = compiler.getEnvTypes().get(compiler.getSymbTable().create("float")).getType();
        setType(currentType);
        return currentType;
    }

    public void codeGenInst(DecacCompiler compiler) {
        compiler.addInstruction(new RFLOAT());
        if (!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(compiler.getErrorManager().getErrorLabel("Input/Output Error")));
        }
        compiler.addInstruction(new LOAD(compiler.getListRegister().R1, compiler.getListRegister().R0));
    }

    protected void codeGenPrint(DecacCompiler compiler, boolean printHex){
        codeGenInst(compiler);
        compiler.addInstruction(new LOAD(compiler.getListRegister().R0, compiler.getListRegister().R1));
        if (printHex) {
            compiler.addInstruction(new WFLOATX());
        } else {
            compiler.addInstruction(new WFLOAT());
        }

    }

    public void codeGenInstARM(DecacCompiler compiler) {
        compiler.addInstruction(new ldr(ARMRegister.r0, "=flottant"));
        compiler.addInstruction(new ldr(ARMRegister.r1, "=tmpfloat"));
        compiler.addInstruction(new bl("scanf"));
        compiler.addInstruction(new ldr(ARMRegister.r1, "=tmpfloat"));
        compiler.addInstruction(new ldr(ARMRegister.r0, "[r1]"));
    }

    protected void codeGenPrintARM(DecacCompiler compiler, boolean printHex){
        codeGenInstARM(compiler);
        compiler.addInstruction(new vmov(ARMRegister.s0, ARMRegister.r0));
        compiler.addARMBlock("        vcvt.f64.f32 d0, s0");
        compiler.addInstruction(new vmov(ARMRegister.r2, ARMRegister.r3, ARMRegister.d0));
        compiler.addInstruction(new ldr(ARMRegister.r0, "=flottant"));
        compiler.addInstruction(new bl("printf"));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readFloat()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

}
