package latmod.ftbu.core;

public enum Phase
{
	PRE,
	POST;
	
	public boolean isPre()
	{ return this == PRE; }
	
	public boolean isPost()
	{ return this == POST; }
}