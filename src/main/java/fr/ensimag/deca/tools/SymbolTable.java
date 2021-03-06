package fr.ensimag.deca.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Manage unique symbols.
 * 
 * A Symbol contains the same information as a String, but the SymbolTable
 * ensures the uniqueness of a Symbol for a given String value. Therefore,
 * Symbol comparison can be done by comparing references, and the hashCode()
 * method of Symbols can be used to define efficient HashMap (no string
 * comparison or hashing required).
 * 
 * @author gl49
 * @date 01/01/2022
 */
public class SymbolTable {
    private Map<String, Symbol> map = new HashMap<String, Symbol>();

    /**
     * Create or reuse a symbol.
     * 
     * If a symbol already exists with the same name in this table, then return
     * this Symbol. Otherwise, create a new Symbol and add it to the table.
     */
    public Symbol create(String name) {
        if (!map.containsKey(name))
            map.put(name, new Symbol(name));
        return map.get(name);
    }

    public Symbol get(String s) {
        if (!map.containsKey(s)) {
            throw new IllegalArgumentException("symbol not found in SymbolTable");
        }
        return map.get(s);
    }

    public static class Symbol {
        // Constructor is private, so that Symbol instances can only be created
        // through SymbolTable.create factory (which thus ensures uniqueness
        // of symbols).
        private Symbol(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String n) {
            this.name = n;
        }

        @Override
        public String toString() {
            return name;
        }

        private String name;
    }
}
