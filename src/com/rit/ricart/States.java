package com.rit.ricart;

public enum States {
	RELEASED(1), 
	WANTED(2), 
	HELD(3);
	
	int state;
	private States(int state) {
		this.state = state;
	}
}
