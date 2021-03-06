package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import fr.ensimag.ima.pseudocode.instructionsARM.*;
import fr.ensimag.ima.pseudocode.ARMRegister;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * String literal
 *
 * @author gl49
 * @date 01/01/2022
 */
public class StringLiteral extends AbstractStringLiteral {
    private static int i;
    private static int printCounter=0;

    @Override
    public String getValue() {
        return value;
    }

    private String value;

    public StringLiteral(String value) {
        Validate.notNull(value);
        value = value.replaceAll("^\"|\"$", "");
        this.value = value;
        RemoveBackslash();
    }

    private void RemoveBackslash() {
        boolean found = false;
        String newstr = "";
        for (int i = 0; i < value.length(); i++) {
            if (!found && Character.compare(value.charAt(i), '\\') == 0) {
                found = true;
            }
            else {
                found = false;
                newstr += value.charAt(i);
            }
        }
        value = newstr;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) {
        StringType currentType = new StringType(compiler.getSymbTable().create("string"));
        setType(currentType);
        return currentType;
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler, boolean printHex) {
        compiler.addInstruction(new WSTR(new ImmediateString(value)));
    }

    @Override
    protected void codeGenPrintARM(DecacCompiler compiler, boolean printHex) {
        String msgName = "msg"+printCounter;
        printCounter++;
        compiler.addARMBlock(".data");
        compiler.addARMBlock(msgName + ": " + ".asciz " + "\"" + value + "\"");  //label with name of variable 
        compiler.addARMBlock(".text");
        compiler.addInstruction(new ldr(ARMRegister.r0, "=" + msgName));
        compiler.addInstruction(new bl("printf"));
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("\"");
        s.print(value.replace("\"", "\\\""));
        s.print("\"");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }
    
    @Override
    String prettyPrintNode() {
        return "StringLiteral (" + value + ")";
    }

    public String toString() {
        return prettyPrintNode();
    }

}
