/*
 * ModelLoadException.java
 *
 * Created on March 13, 2008, 9:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.java.joglutils.model;

/**
 *
 * @author RodgersGB
 */
public class ModelLoadException extends Exception {
    
	private static final long serialVersionUID = 6933827187982017656L;

	/** Creates a new instance of ModelLoadException */
    public ModelLoadException() {
    }
    
    public ModelLoadException(String message) {
        super(message);
    }
}
