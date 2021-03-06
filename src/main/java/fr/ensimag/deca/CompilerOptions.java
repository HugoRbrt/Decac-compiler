package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl49
 * @date 01/01/2022
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO  = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }
    
    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }
    
    public boolean getParse() {
        return parse;
    }

    public boolean getVerification() {
        return verification;
    }
    
    public boolean getNoCheck() {
        return noCheck;
    }
    
    public int getRegisters() {
        return registers;
    }
    
    public boolean getWarnings() {
        return warnings;
    }

    public boolean getArmBool() {
        return armBool;
    }


    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private List<File> sourceFiles = new ArrayList<File>();
    
    private boolean parse = false;
    private boolean verification = false;
    private boolean noCheck = false;
    private int registers = 16;
    private boolean warnings = false;

    // new argument to check if we want an ARM program
    private boolean armBool = false;
    
    // ugly way to get rid of the case -b -r 16 
    private boolean optionRSpotted = false;
    
    public void parseArgs(String[] args) throws CLIException {

        for (int k = 0; k < args.length; k++) {
            
            if ( args[k].equals("-r") && ( k+1 < args.length ) ) {
                // we try to associate the next argument to a number of
                // registers
                try{
                    optionRSpotted = true;
                    k++; // go to next token
                    checkRegisters(args[k]);
                } catch (CLIException e) {
                    throw e;
                }
                
            } else if ( args[k].equals("-r") && ( k+1 >= args.length )) {
                // no number of registers will be recognized
                throw new CLIException("Nothing after -r");
            } else {
                try{
                    processArg(args, k);
                } catch (CLIException e) {
                    throw e;
                }
            }
            
        }

        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        if(warnings){
            logger.setLevel(Level.WARN);
        }
        switch (getDebug()) {
        case QUIET: break; // keep default
        case INFO:
            logger.setLevel(Level.INFO); break;
        case DEBUG:
            logger.setLevel(Level.DEBUG); break;
        case TRACE:
            logger.setLevel(Level.TRACE); break;
        default:
            logger.setLevel(Level.ALL); break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }
        
        // -a and -r cannot be used together at the moment	
        if (optionRSpotted && armBool) {
            throw new CLIException("-r X and -a are uncompatible");
        }
 	
        // if no arguments were seen, we explain how decac should be used
        if (args.length == 0) {
            throw new CLIException("No option nor file given");
        }
        
        if (sourceFiles.size() == 0) {
            // if no file was detected
            if ( printBanner && ( parallel || (debug != 0) || parse ||
                    verification || noCheck || warnings || 
                    optionRSpotted || armBool ) ) {
                // if printBanner was written but other options too
                throw new CLIException("-b is uncompatible with" +
                        "other options");
            } else if ( ( parallel || (debug != 0) || parse ||
                    verification || noCheck || warnings || 
                    optionRSpotted || armBool ) ) {
                // options were given without file
                throw new CLIException("no file given");
            }
                // if we get here : all good
        } else {
            // a file is given so -b cannot be an option
            if ( printBanner ) {
                throw new CLIException("-b in uncompatible with files");
            }
        }

        //throw new UnsupportedOperationException("not yet implemented");
    }

    protected void displayUsage() {
        System.out.println("\033[1;95m    Usage : decac [[-p | -v] [-n] [-a | -r X] [-d]*" + 
                " [-P] [-w] <deca file>...] | [-b]\u001B[m");
        System.out.println(); 
        System.out.println("\033[96m-b  (banner): prints the team banner");
        System.out.println("-p  (parse): stops at the tree building step and " +
                "displays its decompilation (i.e. one source file should " +
                "output a syntaxically correct deca program");
        System.out.println("-v  (verification): stops decac " +
                "after the verification test");
        System.out.println("-n  (no check): deletes programs that " +
                "are incorrect, or correct but cannot be executed" +
                " due to machine performances");
        System.out.println("-r X (registers): limits the number of " +
                "registers to R0 ... R{X-1} with 4 <= X <= 16");
        System.out.println("-d (debug): activates debug traces. " +
                "Repeat several time for diferent traces level: " +
                "INFO, DEBUG, TRACE");
        System.out.println("-P  (parallel): if several source files "+
                "are given, starts their parallel compilations");
        System.out.println("-w  (warnings): enables warning messages during "+
                "compilation");
	System.out.println("-a  (arm target): produces a .s file instead of a"+
                " regular .ass file \u001B[0m");
    }


    // function to process the arguments (except -r X)
    private void processArg(String[] args, int k)
        throws CLIException {
        
        String arg = args[k];
        
        if (arg.equals("-b")) {
            printBanner = true;
        }
        
        else if (arg.equals("-p")) {
            // -p and -v are uncompatible
	    if (verification) {
                throw new CLIException("-p uncompatible with -v");
            } else {
                parse = true;
            }
        }
        
        else if (arg.equals("-v")) {
            // -v and -p are uncompatible
            if (parse) {
                throw new CLIException("-v uncompatible with -p");
            } else {
                verification = true;
            }
        }
        
        else if (arg.equals("-n")) {
            noCheck = true;
        }

        else if (arg.equals("-d")) {
            debug++;
            // no higher log level than TRACE
            if (debug > TRACE) {
                debug = TRACE;
            }
        }
        
        else if (arg.equals("-P")) {
            parallel = true;
        }
        
        // we only accept files ending with the extension .deca
        else if (arg.endsWith(".deca")) {
            // if the file is already present
            // do not add it again
            File decaFile = new File(arg);
            if (! sourceFiles.contains(decaFile)) {
                sourceFiles.add(decaFile);
            }
        }
        
        else if (arg.equals("-w")) {
            warnings = true;
        }

	else if (arg.equals("-a")) {
            armBool = true;
        }
        
        else {
            throw new CLIException("option or file not recognized: " + arg);
        }
        
    }
        
    // Treats the argument following "-r" to get the correct number of registers
    private void checkRegisters(String nbRegistersString)
        throws CLIException {
        
        int nbRegisters = -1;
        try {
            nbRegisters = Integer.parseInt(nbRegistersString);
        } catch (NumberFormatException nfe) {
            throw new CLIException("-r X has not X as a number");
        }
        if ((nbRegisters < 4) || (nbRegisters > 16)) {
                throw new CLIException("-r X does not match 4 <= X <= 16");
        } else {
            registers = nbRegisters;
        }
    }
    
    // debug function
    @Override
    public String toString() {
        String s = "CompilerOptions[\n";
        s += "-b (printBanner): " + Boolean.toString(printBanner) + "\n";
        s += "-d (debug): " + Integer.toString(debug) + "\n";
        s += "-P (parallel): " + Boolean.toString(parallel) + "\n";
        s += "-v (verification): " + Boolean.toString(verification) + "\n";
        s += "-p (parse): " + Boolean.toString(parse) + "\n";
        s += "-n (noCheck): " + Boolean.toString(noCheck) + "\n";
        s += "-w (warnings): " + Boolean.toString(warnings) + "\n";
        s += "-r (registers): " + Integer.toString(registers) + "\n";
	s += "-a (armBool): " + Boolean.toString(armBool) + "\n";
        s += "-files: " + sourceFiles.toString() + "\n";
        s += "]";
        
        return s;
    }
    

}
