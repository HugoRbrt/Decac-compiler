package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.ima.pseudocode.instructions.*;

import java.io.PrintStream;

public class InstanceOf extends AbstractExpr {
    private AbstractExpr expr;
    private AbstractIdentifier comparedTo;

    public InstanceOf(AbstractExpr expr, AbstractIdentifier comparedTo) {
        this.expr = expr;
        this.comparedTo = comparedTo;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {
        Type t1 = expr.verifyExpr(compiler, localEnv, currentClass);
        Type t2 = comparedTo.verifyType(compiler);
        comparedTo.setDefinition(compiler.getEnvTypes().get(comparedTo.getName()));
        Type resType = ContextTools.typeInstanceOf(compiler, t1, t2, getLocation());
        setType(resType);
        return resType;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        expr.decompile(s);
        s.print(" instanceof ");
        comparedTo.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, false);
        comparedTo.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
        comparedTo.iter(f);
    }

    protected void codeGenInst(DecacCompiler compiler){
        Label start = new Label();
        Label success = new Label();
        Label failure = new Label();
        Label end = new Label();
        expr.codeGenInst(compiler);
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.R0), Register.R0));
        compiler.addInstruction(new LEA(  compiler.getstackTable().getClass( comparedTo.getName()) , Register.R1));
        compiler.addLabel(start);
        compiler.addInstruction(new CMP(Register.R0, Register.R1));
        compiler.addInstruction(new BEQ(success));
        compiler.addInstruction(new LOAD(new RegisterOffset( 0 , Register.R0), Register.R0));
        compiler.addInstruction(new CMP( new NullOperand(), Register.R0));
        compiler.addInstruction(new BEQ(failure));
        compiler.addInstruction(new BRA(start));
        compiler.addLabel(success);
        compiler.addInstruction(new LOAD(new ImmediateInteger(1), Register.R0));
        compiler.addInstruction(new BRA(end));
        compiler.addLabel(failure);
        compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.R0));
        compiler.addLabel(end);
    }
}
