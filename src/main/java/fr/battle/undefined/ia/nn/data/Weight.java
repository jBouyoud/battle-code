package fr.battle.undefined.ia.nn.data;

import java.io.Serializable;

/**
 * @author Kunuk Nykjaer
 * 
 */
public class Weight implements Serializable {
	
	private static final long serialVersionUID = 2L;
	
	public int nodeId;
	public int connectionId;
	public double weight;
	
	public Weight(int n, int c, double w)
	{
		nodeId=n;
		connectionId=c;
		weight=w;
	}
}