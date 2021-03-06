package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.apache.commons.lang.Validate;

import java.io.PrintStream;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl49
 * @date 01/01/2022
 */
public class DeclClass extends AbstractDeclClass {

    final private AbstractIdentifier className;
    final private AbstractIdentifier superClass;
    final private ListDeclField fields;
    final private ListDeclMethod methods;

    public DeclClass(AbstractIdentifier className, AbstractIdentifier superClass,
            ListDeclField fields, ListDeclMethod methods) {
        Validate.notNull(className);
        Validate.notNull(superClass);
        Validate.notNull(fields);
        Validate.notNull(methods);
        this.className = className;
        this.superClass = superClass;
        this.fields = fields;
        this.methods = methods;
    }

    public AbstractIdentifier getSuperClass(){
        return superClass;
    }

    public SymbolTable.Symbol getClassName() {
        return className.getName();
    }

    /**
     * Class declaration context check (first pass). First checks whether the superclass exists, then checks
     * whether the class hasn't already been declared.
     *
     * @param compiler contains the predefined EnvironmentType.
     * @throws ContextualError if the class has already been declared.
     */
    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {
        EnvironmentType envT = compiler.getEnvTypes();
        SymbolTable.Symbol nameSymb = className.getName();
        superClass.setType(superClass.verifyType(compiler));
        superClass.setDefinition(envT.get(superClass.getName()));
        ClassDefinition superCl = (ClassDefinition) envT.get(superClass.getName());
        ClassType cl = new ClassType(nameSymb, getLocation(), superCl);
        try {
            envT.declare(nameSymb, cl.getDefinition());
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError(
                    "(RULE 1.3) Class '" + className.getName() + "' has already been declared.",
                    className.getLocation());
        }
        className.setDefinition(envT.get(nameSymb));
        className.setType(envT.get(nameSymb).getType());
    }

    /**
     * Context check second pass. Checks field declarations (NOT initializations)
     * and method signatures.
     *
     * @throws ContextualError exceptions passed on from throws in invalid fields
     * or methods.
     */
    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {
        ClassDefinition superCl = (ClassDefinition) compiler.getEnvTypes().get(superClass.getName());
        ClassDefinition cl = (ClassDefinition) compiler.getEnvTypes().get(className.getName());
        cl.setNumberOfFields(superCl.getNumberOfFields());
        for (AbstractDeclField f: fields.getList()) {
            cl.incNumberOfFields();
            f.verifyField(compiler, cl.getMembers(), cl, cl.getNumberOfFields());
        }
        cl.setNumberOfMethods(superCl.getNumberOfMethods());
        for (AbstractDeclMethod m: methods.getList()) {
            m.verifyMethod(compiler, cl.getMembers(), cl, cl.getNumberOfMethods());
        }
    }

    /**
     * Context check third pass. Checks field initializations and method bodies.
     */
    @Override
    protected void verifyClassBody(DecacCompiler compiler) throws ContextualError {
        ClassDefinition cl = (ClassDefinition) compiler.getEnvTypes().get(className.getName());
        for (AbstractDeclField f: fields.getList()) {
            f.verifyFieldInitialization(compiler, cl.getMembers(), cl);
        }
        for (AbstractDeclMethod m: methods.getList()) {
            m.verifyMethodBody(compiler,cl.getMembers(), cl);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        className.decompile(s);
        s.print(" extends ");
        superClass.decompile(s);
        s.println(" {");
        s.indent();
        fields.decompile(s);
        methods.decompile(s);
        s.unindent();
        s.println("}");
    }


    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, false);
        superClass.prettyPrint(s, prefix, false);
        fields.prettyPrint(s, prefix, false, false);
        methods.prettyPrint(s, prefix, true, false);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        className.iter(f);
        superClass.iter(f);
        fields.iter(f);
        methods.iter(f);
    }

    protected void codeGenTable(DecacCompiler compiler){
        compiler.addComment("construction of Method Table for "+className.getName().getName());
        RegisterOffset superClassAdress = compiler.getstackTable().getClass(superClass.getName());
        compiler.getstackTable().putClass(className.getName(), Register.GB);
        compiler.addInstruction(new LEA(superClassAdress, Register.R0));
        compiler.addInstruction(new STORE(Register.R0, compiler.getstackTable().getClass(className.getName())));
        compiler.incrMethodsTableSize(2);

        SymbolTable.Symbol[] symbolList = new SymbolTable.Symbol[className.getClassDefinition().getNumberOfMethods()];
        className.getClassDefinition().getMembers().getSymbolMethod(new SymbolTable(), symbolList, className.getClassDefinition());

        for(SymbolTable.Symbol symbol : symbolList){
            symbol.setName(symbol.getName());
            compiler.getstackTable().put(symbol, Register.GB);
            compiler.addInstruction(new LOAD(new LabelOperand(new Label(symbol.getName())), Register.R0));
            compiler.addInstruction(new STORE(Register.R0, compiler.getstackTable().get(symbol)));
            compiler.incrMethodsTableSize(2);
        }
    }

    protected void codeGen(DecacCompiler compiler){
        String stringClassName = className.getName().getName();
        compiler.addComment(" --------------------------------------------------");
        compiler.addComment("             Class "+ stringClassName);
        compiler.addComment(" --------------------------------------------------");
        fields.codeGen(compiler, className, superClass);
        methods.codeGen(compiler, stringClassName, fields.getList() );
    }
}
