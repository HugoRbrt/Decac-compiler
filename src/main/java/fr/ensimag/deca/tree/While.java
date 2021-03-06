package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ARMRegister;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import java.io.PrintStream;

import fr.ensimag.ima.pseudocode.instructionsARM.b;
import fr.ensimag.ima.pseudocode.instructionsARM.bne;
import fr.ensimag.ima.pseudocode.instructionsARM.cmp;
import org.apache.commons.lang.Validate;

/**
 *
 * @author gl49
 * @date 01/01/2022
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        Label beginWhile = new Label();
        Label endWhile = new Label();
        compiler.addLabel(beginWhile);
        condition.codeGenInst(compiler);
        compiler.addInstruction(new CMP(new ImmediateInteger(1), compiler.getListRegister().R0));
        compiler.addInstruction(new BNE(endWhile));
        body.codeGenListInst(compiler);
        compiler.addInstruction(new BRA(beginWhile));
        compiler.addLabel(endWhile);
    }

    @Override
    protected void codeGenInstARM(DecacCompiler compiler) {
        Label beginWhile = new Label();
        Label endWhile = new Label();
        compiler.addLabel(beginWhile);
        condition.codeGenInstARM(compiler);
        compiler.addInstruction(new cmp(ARMRegister.r0, 1));
        compiler.addInstruction(new bne(endWhile.toString()));
        body.codeGenListInstARM(compiler);
        compiler.addInstruction(new b(beginWhile.toString()));
        compiler.addLabel(endWhile);
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
        this.condition.verifyCondition(compiler, localEnv, currentClass);
        this.body.verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        condition.iter(f);
        body.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

}
