package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;

import java.io.PrintStream;

public abstract class AbstractDeclMethod extends Tree {

    protected abstract void verifyMethod(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, int counter) throws ContextualError;

    protected abstract void verifyMethodBody(DecacCompiler compiler, EnvironmentExp classEnv,
            ClassDefinition currentClass) throws ContextualError;


    protected abstract void codeGenTable(DecacCompiler compiler, SymbolTable.Symbol classSymbol);

    /**
     * Generate assembly code for the IMA instruction.
     *
     * @param compiler
     */
    protected abstract void codeGen(DecacCompiler compiler, String className);
}
