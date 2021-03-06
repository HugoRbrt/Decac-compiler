package fr.ensimag.deca.context;

import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tree.Location;
import org.apache.commons.lang.Validate;

/**
 * Type defined by a class.
 *
 * @author gl49
 * @date 01/01/2022
 */
public class ClassType extends Type {
    
    protected ClassDefinition definition;
    
    public ClassDefinition getDefinition() {
        return this.definition;
    }
            
    @Override
    public ClassType asClassType(String errorMessage, Location l) {
        return this;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isClassOrNull() {
        return true;
    }

    /**
     * Standard creation of a type class.
     */
    public ClassType(Symbol className, Location location, ClassDefinition superClass) {
        super(className);
        this.definition = new ClassDefinition(this, location, superClass);
    }

    /**
     * Creates a type representing a class className.
     * (To be used by subclasses only)
     */
    protected ClassType(Symbol className) {
        super(className);
    }
    

    @Override
    public boolean sameType(Type otherType) {
        if (!otherType.isClass()) {
            return false;
        }
        if (this.getName().toString().equals(otherType.getName().toString())) {
            return true;
        }
        return false;
    }

    /**
     * Return true if potentialSuperClass is a superclass of this class.
     */
    public boolean isSubClassOf(ClassType potentialSuperClass) {
        if (potentialSuperClass.isNull()) {
            return false;
        }
        if (sameType(potentialSuperClass)) {
            return true;
        }
        ClassDefinition hierarchy = definition.getSuperClass();
        while (hierarchy != null) {
            if (hierarchy.getType().getName().toString().equals(
                    potentialSuperClass.getName().toString())) {
                return true;
            }
            hierarchy = hierarchy.getSuperClass();
        }
        return false;
    }


}
