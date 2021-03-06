package fr.ensimag.ima.pseudocode;

/**
 * General Purpose Register operand (R0, R1, ... R15).
 * 
 * @author Ensimag
 * @date 01/01/2022
 */
public class ARMGPRegister extends ARMRegister {
    
    private int number;

    /**
     * Available if not used for saving a result
     */
    private boolean availability = true;
    
    /**
     * Accessed by push if the register was used in a PUSH expression
     * With this, we can keep track of the future POP
     */
    private int nbPushOnRegister = 0;

    /**
     * @return the number of the register, e.g. 12 for R12.
     */
    public int getNumber() {
        return number;
    }
    
    public void incrNbPushOnRegister(int nbPush) {
        nbPushOnRegister += nbPush;
    }
    
    public void decrNbPushOnRegister(int nbPop) {
        nbPushOnRegister -= nbPop;
    }
    
    public int getNbPushOnRegister() {
        return nbPushOnRegister;
    }
    
    
    /**
     * @return true if the register is available for use, else false
     */
    public boolean available(){
        return availability;
    }

    
    ARMGPRegister(String name, int number) {
        super(name);
        this.number = number;
    }

    /**
     * Makes the register unavailable for saves
     */
    public void use(){
        assert(availability);
        availability=false;
    }

    /**
     * Free an unavailable register to make it usable for saves
     */
    public void free(){
        assert(!availability);
        availability=true;
    }

    public String debugDisplay(){
        String s;
        if (availability) {
            // available
            s = " [ ]";
        } else {        
            if (nbPushOnRegister < 0) {
                s = " [X]"; // abnormal state
            } else {
                // used with a certain number of PUSH on it
                s = " [" + Integer.toString(nbPushOnRegister) + "]";
            }
        }
        
        return s;
    }    

}
