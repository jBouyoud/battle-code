package fr.battle.undefined.ia.nn.data;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author Kunuk Nykjaer
 * 
 */
public class Weights implements Serializable{

	private static final long serialVersionUID = 1L;
	public LinkedList<Weight> weights = new LinkedList<Weight>();
}