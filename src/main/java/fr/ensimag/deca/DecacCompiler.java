package fr.ensimag.deca;

import fr.ensimag.deca.context.*;
import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.codegen.ErrorManager;
import fr.ensimag.deca.codegen.ARMErrorManager;
import fr.ensimag.deca.codegen.ARMFunctionManager;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.StackHashTableSymbol;
import fr.ensimag.deca.tools.CodeAnalyzer;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.ima.pseudocode.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Objects;
import java.lang.Runnable;

import fr.ensimag.ima.pseudocode.ARMProgram;
import fr.ensimag.ima.pseudocode.GenericProgram;
import fr.ensimag.ima.pseudocode.IMAProgram;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 * Decac compiler instance.
 *
 * This class is to be instantiated once per source file to be compiled. It
 * contains the meta-data used for compiling (source file name, compilation
 * options) and the necessary utilities for compilation (symbol tables, abstract
 * representation of target file, ...).
 *
 * It contains several objects specialized for different tasks. Delegate methods
 * are used to simplify the code of the caller (e.g. call
 * compiler.addInstruction() instead of compiler.getProgram().addInstruction()).
 *
 * @author gl49
 * @date 01/01/2022
 */
public class DecacCompiler implements Runnable {
    private static final Logger LOG = Logger.getLogger(DecacCompiler.class);

    /**
     * Portable newline character.
     */
    private static final String nl = System.getProperty("line.separator", "\n");

    private EnvironmentExp envExp = new EnvironmentExp(null);
    private SymbolTable symbTable = new SymbolTable();
    private EnvironmentType envTypes = EnvironmentType.getEnvTypes();
    private StackHashTableSymbol stackTable = new StackHashTableSymbol();
    private CodeAnalyzer codeAnalyzer = new CodeAnalyzer();
    private ErrorManager errorManager = new ErrorManager();
    private boolean emitWarnings = false;
    private ARMErrorManager armErrorManager = new ARMErrorManager();
    private ARMFunctionManager armFunctionManager = new ARMFunctionManager();

    public DecacCompiler(CompilerOptions compilerOptions, File source) {
        super();
        this.compilerOptions = compilerOptions;
        if(Objects.isNull(this.compilerOptions) || !this.compilerOptions.getArmBool()){
            program = new IMAProgram();
        }else{
            program = new ARMProgram();
        }

        this.source = source;

        predefinedEnvironments();
    }

    /**
     * Source file associated with this compiler instance.
     */
    public File getSource() {
        return source;
    }

    /**
     * Compilation options (e.g. when to stop compilation, number of registers
     * to use, ...).
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * Environment expressions associated with the program
     */
    public EnvironmentExp getEnvExp() {
        return envExp;
    }

    /**
     * Environment types associated with the program
     */
    public EnvironmentType getEnvTypes() { return envTypes; }

    /**
     * Symbols associated with the program
     */
    public SymbolTable getSymbTable() {
        return symbTable;
    }

    /**
     * Symbols associated with the stack adresse
     */
    public StackHashTableSymbol getstackTable() {
        return stackTable;
    }

    /**
     * Gencode analyzer for the stack's needs
     */
    public CodeAnalyzer getCodeAnalyzer() {
        return codeAnalyzer;
    }
    
    /**
     * ErrorManager to create the approriate code
     */
    public ErrorManager getErrorManager() {
        return errorManager;
    }
    
    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#add(fr.ensimag.ima.pseudocode.AbstractLine)
     */
    public void add(AbstractLine line) {
        program.add(line);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addComment(java.lang.String)
     */
    public void addComment(String comment) {
        program.addComment(comment);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addComment(java.lang.String)
     */

    public void addARMComment(String comment) {
        program.addComment(comment);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addLabel(fr.ensimag.ima.pseudocode.Label)
     */
    public void addLabel(Label label) {
        program.addLabel(label);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addInstruction(Instruction instruction) {
        program.addInstruction(instruction);
    }


    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction,
     * java.lang.String)
     */
    public void addInstruction(Instruction instruction, String comment) {
        program.addInstruction(instruction, comment);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addOther(fr.ensimag.ima.pseudocode.Instruction),
     * * java.lang.String
     */
    public void addARMBlock(String other) {
        ARMProgram armpP = (ARMProgram) program;
        armpP.addARMBlock(other);
    }

    /**
     * New instruction to add at the beginning of the program
     */
    public void addFirstInstruction(Instruction instruction) {
        program.addFirstInstruction(instruction);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#display()
     */
    public String displayProgram() {
        return program.display();
    }


    private final CompilerOptions compilerOptions;
    private final File source;
    private Register ListRegister;
    private ARMRegister ListRegisterARM;

    public void setListRegister(Register list){
        ListRegister = list;
    }

    public Register getListRegister() {
        return ListRegister;
    }


    public boolean getEmitWarnings() { return emitWarnings; }
    
    public void setListRegisterARM(ARMRegister list){
        ListRegisterARM = list;
    }

    public ARMRegister getListRegisterARM() {
        return ListRegisterARM;
    }

    public ARMFunctionManager getArmFunctionManager() {
        return armFunctionManager;
    }

    public ARMErrorManager getArmErrorManager() {
        return armErrorManager;
    }


    /**
     * The main program. Every instruction generated will eventually end up here.
     */
    private final GenericProgram program;


    /**
     * Run the compiler (parse source file, generate code)
     *
     * @return true on error
     */
    public boolean compile() {
        String sourceFile = source.getAbsolutePath();
        String[] tmp = sourceFile.split("\\.");
        String destFile = tmp[0];
        for (int i = 1; i < tmp.length - 1; i++) {
            destFile += "." + tmp[i];
        }
        if (!this.compilerOptions.getArmBool()) {
            destFile += ".ass";
        } else {
            destFile += ".s";
        }
        LOG.info(" dest:"+ destFile);
        PrintStream err = System.err;
        PrintStream out = System.out;
        LOG.debug("Compiling file " + sourceFile + " to assembly file " + destFile);
        try {
            return doCompile(sourceFile, destFile, out, err);
        } catch (LocationException e) {
            e.display(err);
            return true;
        } catch (DecacFatalError e) {
            err.println(e.getMessage());
            return true;
        } catch (StackOverflowError e) {
            LOG.debug("stack overflow", e);
            err.println("Stack overflow while compiling file " + sourceFile + ".");
            return true;
        } catch (NumberFormatException e) {
            LOG.debug("Number exception caught.");
            return true;
        } catch (IllegalArgumentException e) {
            LOG.debug("Error : Number exception caught");
            LOG.debug(ExceptionUtils.getStackTrace(e));
            err.println("Float literal out of range:");
            err.println(e.getMessage());
            return true;
        } catch (Exception e) {
            LOG.fatal("Exception raised while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        } catch (AssertionError e) {
            LOG.fatal("Assertion failed while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        }
    }

    /**
     * First part of the first context analysis pass: initializing the
     * builtin types, including the Object class and its EnvironmentExp,
     * containing the equals method.
     */
    private void predefinedEnvironments() {
        envTypes.put(symbTable.create("void"),
                new TypeDefinition(new VoidType(symbTable.create("void")), Location.BUILTIN));
        envTypes.put(symbTable.create("boolean"),
                new TypeDefinition(new BooleanType(symbTable.create("boolean")), Location.BUILTIN));
        envTypes.put(symbTable.create("float"),
                new TypeDefinition(new FloatType(symbTable.create("float")), Location.BUILTIN));
        envTypes.put(symbTable.create("int"),
                new TypeDefinition(new IntType(symbTable.create("int")), Location.BUILTIN));
        envTypes.put(symbTable.create("null"),
                new TypeDefinition(new NullType(symbTable.create("null")), Location.BUILTIN));
        /*  Defining Deca class types uses ClassDefinition extension of TypeDefinition;
            each defition has its own EnvironmentExp that includes a pointer to
            the superclass's environment. */
        ClassType obj = new ClassType(symbTable.create("Object"), Location.BUILTIN, null);
        envTypes.put(obj.getName(), obj.getDefinition());
        /*  Definition of the equals method, in the EnvironmentExp tied to the
            Object class. */
        ClassDefinition cl = obj.getDefinition();
        Signature params = new Signature(cl.getType());
        cl.getMembers().put(symbTable.create("equals"),
                new MethodDefinition(envTypes.get(symbTable.create("boolean")).getType(),
                        Location.BUILTIN, params, 1));
        cl.setNumberOfFields(0);
        cl.setNumberOfMethods(1);
    }

    /**
     * function that makes the class to implements Runnable interface
     * by calling compile() function (usefull for -P decac option )
     */
    public void run(){
        compile();
    }

    /**
     * Internal function that does the job of compiling (i.e. calling lexer,
     * verification and code generation).
     *
     * @param sourceName name of the source (deca) file
     * @param destName name of the destination (assembly) file
     * @param out stream to use for standard output (output of decac -p)
     * @param err stream to use to display compilation errors
     *
     * @return true on error
     */
    private boolean doCompile(String sourceName, String destName,
                              PrintStream out, PrintStream err)
            throws DecacFatalError, LocationException {
        AbstractProgram prog = doLexingAndParsing(sourceName, err);

        if (prog == null) {
            LOG.info("Parsing failed");
            return true;
        }
        if (compilerOptions.getWarnings()) {
            emitWarnings = true;
        }
        assert(prog.checkAllLocations());
        // Decompile
        if (this.compilerOptions.getParse()) {
            LOG.info("Output decompiled program is: " + destName);
            prog.decompile(out);
            return false;
        }

        prog.verifyProgram(this);
        assert(prog.checkAllDecorations());

        if (this.compilerOptions.getVerification()) {
            LOG.info("Verification is done ");
            return false;
        }

        addComment("start main program");
        if(!this.compilerOptions.getArmBool()){
            prog.codeGenProgram(this);
        }else{
            prog.codeGenProgramARM(this);
        }
        
        addComment("end main program");
        
        LOG.debug("Generated assembly code:" + nl + program.display());
        LOG.info("Output file assembly file is: " + destName);

        FileOutputStream fstream = null;
        try {
            fstream = new FileOutputStream(destName);
        } catch (FileNotFoundException e) {
            throw new DecacFatalError("Failed to open output file: " + e.getLocalizedMessage());
        }

        LOG.info("Writing assembler file ...");

        program.display(new PrintStream(fstream));
        LOG.info("Compilation of " + sourceName + " successful.");
        return false;
    }

    /**
     * Build and call the lexer and parser to build the primitive abstract
     * syntax tree.
     *
     * @param sourceName Name of the file to parse
     * @param err Stream to send error messages to
     * @return the abstract syntax tree
     * @throws DecacFatalError When an error prevented opening the source file
     * @throws DecacInternalError When an inconsistency was detected in the
     * compiler.
     * @throws LocationException When a compilation error (incorrect program)
     * occurs.
     */
    protected AbstractProgram doLexingAndParsing(String sourceName, PrintStream err)
            throws DecacFatalError, DecacInternalError {
        DecaLexer lex;
        try {
            lex = new DecaLexer(CharStreams.fromFileName(sourceName));
        } catch (IOException ex) {
            throw new DecacFatalError("Failed to open input file: " + ex.getLocalizedMessage());
        }
        lex.setDecacCompiler(this);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(this);
        return parser.parseProgramAndManageErrors(err);
    }
    
    
    /**
     * Shortcuts for code analysis
     */
    public void incrPopCount(int nbPop) {
        codeAnalyzer.incrPopCount(nbPop);
    }
    
    public void incrPushCount(int nbPush) {
        codeAnalyzer.incrPushCount(nbPush);
    }
    
    public void incrDeclaredVariables(int nbVariables) {
        codeAnalyzer.incrDeclaredVariables(nbVariables);
    }
    
    public void incrMethodsTableSize(int neededSize) {
        codeAnalyzer.incrMethodsTableSize(neededSize);
    }
    
    public void resetCodeAnalyzer() {
        codeAnalyzer = new CodeAnalyzer();
    }


}
