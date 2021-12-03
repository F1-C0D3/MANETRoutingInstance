package de.app.commander;

public class CommandArgument<P> {
	private final boolean defaultFlag;
	public String commandShort;
	public String commandLong;
	public P value;

	public CommandArgument(String commandLong, String commandShort, P value) {
		this.commandLong = commandLong;
		this.commandShort = commandShort;
		this.value = value;
		this.defaultFlag = true;
	}

	public CommandArgument(String commandLong, String commandShort) {
		this.commandLong = commandLong;
		this.commandShort = commandShort;
		this.defaultFlag=false;
	}

	public void setValue(P value) {
		this.value = value;
	}
	
	public boolean hasDefaultValue() {
		return defaultFlag;
	}
}
