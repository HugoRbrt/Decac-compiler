package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.SymbolTable;

public abstract class AbstractDeclParam extends Tree {

    protected abstract Type verifySignature(DecacCompiler compiler) throws
            ContextualError;

    protected abstract void verifyDeclParam(DecacCompiler compiler, EnvironmentExp localEnv)
            throws ContextualError;

    protected abstract SymbolTable.Symbol getName();
}
