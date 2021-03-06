package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.*;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.HALT;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import java.io.PrintStream;

public class MethodBody extends AbstractMethodBody {
    private static final Logger LOG = Logger.getLogger(MethodBody.class);

    private ListDeclVar declVariables;
    private ListInst insts;
    public MethodBody(ListDeclVar declVariables, ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    /**
     * Context check third pass. Checks the correctness of the method body's
     * declarations and instructions.
     */
    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp localEnv,
                ClassDefinition currentClass, Type returnType) throws ContextualError {
        declVariables.verifyListDeclVariable(compiler, localEnv, currentClass);
        insts.verifyListInst(compiler, localEnv, currentClass, returnType);
        if (compiler.getEmitWarnings() && insts.isEmpty() && !returnType.isVoid()) {
            Warning warning = new Warning(
                    "No return statement in non-void method.",
                    getLocation());
            warning.emit();
        }
    }

    @Override
    protected void codeGenMethodBody(DecacCompiler compiler) {
        compiler.addComment("             Method Body");
        declVariables.codeGenListDeclLocalVar(compiler);
        insts.codeGenListInst(compiler);
    }

    @Override
    public int getNumberLocalVariables() {
        return declVariables.getNumberLocalVariable();
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }
}
