package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.AbstractIdentifier;
import fr.ensimag.deca.tree.DeclClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Dictionary associating identifier's ExpDefinition to their names.
 *
 * This is actually a linked list of dictionaries: each EnvironmentExp has a
 * pointer to a parentEnvironment, corresponding to superblock (eg superclass).
 *
 * The dictionary at the head of this list thus corresponds to the "current"
 * block (eg class).
 *
 * Searching a definition (through method get) is done in the "current"
 * dictionary and in the parentEnvironment if it fails.
 *
 * Insertion (through method declare) is always done in the "current" dictionary.
 *
 * @author gl49
 * @date 01/01/2022
 */
public class EnvironmentExp {
    // A FAIRE : implémenter la structure de donnée représentant un
    // environnement (association nom -> définition, avec possibilité
    // d'empilement).

    private Map<Symbol, ExpDefinition> environment = new HashMap<Symbol, ExpDefinition>();

    EnvironmentExp parentEnvironment;

    public EnvironmentExp(EnvironmentExp parentEnvironment) {
        this.parentEnvironment = parentEnvironment;
    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     */
    public ExpDefinition get(Symbol key) {
        if (environment.containsKey(key)) {
            return environment.get(key);
        }
        if (parentEnvironment == null) {
            return null;
        }
        return parentEnvironment.get(key);
    }

     /**
     * @param key The symbol mapped to the definition
     */
    public ExpDefinition getCurrent(Symbol key) {
        if (environment.containsKey(key)) {
            return environment.get(key);
        }
        return null;
    }

    public void getSymbolMethod(SymbolTable symbolTable, Symbol[] symbolList, ClassDefinition className){
        if(parentEnvironment != null){
            parentEnvironment.getSymbolMethod(symbolTable, symbolList, className.getSuperClass());
        }
        //ajouter les symboles dans la table sans se poser de questions (les overrides seront fait automatiquement)
        for(Symbol s : environment.keySet()){
            if(environment.get(s).isMethod()){
                Symbol symbol = symbolTable.create("code."+className.toStringName()+"."+s.getName());
                symbolList[((MethodDefinition)environment.get(s)).getIndex() - 1] = symbol;
            }
        }
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     *
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary
     * - or, hides the previous declaration otherwise.
     *
     * @param name
     *            Name of the symbol to define
     * @param def
     *            Definition of the symbol
     * @throws DoubleDefException
     *             if the symbol is already defined at the "current" dictionary
     *
     */
    public void declare(Symbol name, ExpDefinition def) throws DoubleDefException {

        if (environment.containsKey(name)) {
            throw new DoubleDefException();
        }
        environment.put(name, def);
    }

    /**
     * Adds the definition def associated to the symbol name in the environment.
     * Version without double definition check, for initializing the builtin
     * methods.
     *
     * @param name Name of the symbol to define.
     * @param def Definition of the symbol.
     */
    public void put(Symbol name, ExpDefinition def) { environment.put(name, def); }

    @Override
    public String toString() {
        String s = "";
        for (Symbol symb: environment.keySet()) {
            s += symb + " " + environment.get(symb).getType() + " ";
        }
        return s;
    }
}
