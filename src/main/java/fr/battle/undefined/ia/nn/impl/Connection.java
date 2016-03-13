package fr.battle.undefined.ia.nn.impl;

/**
 * @author Kunuk Nykjaer and others
 * 
 */
public class Connection 
{	
	double weight=0;
	double prevDeltaWeight=0; // for momentum
	double deltaWeight=0;
		
	public Neuron leftNeuron;
	Neuron rightNeuron;
	public static int counter = 0;
	public int id; // auto increment
	
	public Connection(Neuron fromN, Neuron toN){
		leftNeuron = fromN;
		rightNeuron = toN;
		id=counter;
		counter++;
	}
	
	public double getWeight(){
		return weight;
	}
	public void setWeight(double w){
		weight = w;
	}
	
	public void setDeltaWeight(double w){
		prevDeltaWeight = deltaWeight;
		deltaWeight = w;
	}	
	
	public double getPrevDeltaWeight(){
		return prevDeltaWeight;
	}
	
	public Neuron getFromNeuron() {
		return leftNeuron;
	}
	public void setFromNeuron(Neuron fromNeuron) {
		this.leftNeuron = fromNeuron;
	}
	public Neuron getToNeuron() {
		return rightNeuron;
	}
	public void setToNeuron(Neuron toNeuron) {
		this.rightNeuron = toNeuron;
	}
}