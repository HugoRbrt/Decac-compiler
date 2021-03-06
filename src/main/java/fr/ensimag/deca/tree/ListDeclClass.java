package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.*;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.instructions.*;
import org.apache.log4j.Logger;

/**
 *
 * @author gl49
 * @date 01/01/2022
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);
    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
            s.println();
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClass: start");
        for (AbstractDeclClass decl: getList()) {
            decl.verifyClass(compiler);
        }
        LOG.debug("verify listClass: end");
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclClass decl: getList()) {
            decl.verifyClassMembers(compiler);
        }
    }
    
    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        for (AbstractDeclClass decl: getList()) {
            decl.verifyClassBody(compiler);
        }
    }

    protected void codeGenTable(DecacCompiler compiler) {
        compiler.addComment(" --------------------------------------------------");
        compiler.addComment("             Construction of Method Table");
        compiler.addComment(" --------------------------------------------------");


        compiler.addComment("construction of Method Table for Object");
        compiler.addInstruction(new LOAD(new NullOperand(), Register.R0));

        Symbol objectSymbol = compiler.getSymbTable().get("Object");
        compiler.getstackTable().putClass(objectSymbol, Register.GB);
        compiler.addInstruction(new STORE(Register.R0, compiler.getstackTable().getClass(objectSymbol)));

        Symbol equalsObjectSymbol = compiler.getSymbTable().create("code.Object.equals");
        compiler.getstackTable().put(equalsObjectSymbol, Register.GB);
        compiler.addInstruction(new LOAD(new LabelOperand(new Label(equalsObjectSymbol.getName())), Register.R0));
        compiler.addInstruction(new STORE(Register.R0,  compiler.getstackTable().get(equalsObjectSymbol)));


        for (AbstractDeclClass decl: getList()) {
            decl.codeGenTable(compiler);
        }
    }

    protected void codeGen(DecacCompiler compiler) {
        compiler.addComment(" --------------------------------------------------");
        compiler.addComment("             Object Class");
        compiler.addComment(" --------------------------------------------------");

        compiler.addComment(" ---------- equals Method");
        compiler.addLabel(new Label("init.Object"));
        compiler.addInstruction(new RTS());

        Symbol equalsObjectSymbol = compiler.getSymbTable().get("code.Object.equals");
        compiler.addLabel(new Label(equalsObjectSymbol.getName()));
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.R1));
        compiler.addInstruction(new LOAD(new RegisterOffset(-3, Register.LB), Register.R0));
        compiler.addInstruction(new CMP(Register.R1, Register.R0));
        compiler.addInstruction(new SEQ(Register.R0));
        compiler.addInstruction(new RTS());

        for( Symbol s: compiler.getstackTable().getListDeclVar()){
            compiler.getstackTable().remove(s);
        }

        for (AbstractDeclClass decl: getList()) {
            decl.codeGen(compiler);
        }
    }

}
