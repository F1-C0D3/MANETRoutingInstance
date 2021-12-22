package de.app.commander;

public class CommandLineReader {
	private String commands;

	public CommandLineReader(String[] args) {

		StringBuffer commandBuffer = new StringBuffer();

		for (int i = 0; i < args.length; i++) {
			commandBuffer.append(args[i]);
		}
		commands = commandBuffer.toString();

	}

	public <P> String parse(CommandArgument<P> argument) {
		String commandValue = getCommand(argument);
		return checkCommandValue(commandValue, argument);
	}

	private String cropCommand(String command) {
		int startIndex = 0;
		int suffix = 1;

		String copyCommand = new String(command);
		if (!copyCommand.substring(startIndex + 1, command.length()).contains("-")) {

			return command.substring(0, command.length());

		} else {

			int endIndex = command.substring(startIndex + 1, command.length()).indexOf('-') + suffix;
			return command.substring(0, endIndex);
		}

	}

	private <P> String getCommand(CommandArgument<P> argument) {
		String copyCommand = new String(commands);
		boolean found = false;
		String value = null;

		while (copyCommand.length() > 0 && !found) {

			int startIndex = copyCommand.indexOf('-');
			copyCommand = copyCommand.substring(startIndex, copyCommand.length());

			String part = cropCommand(copyCommand);
			if (part.contains(argument.commandShort)) {
				value = part.substring(argument.commandShort.length());
				value = value.replaceAll(" ", "");
				found = true;
			} else {
				copyCommand = copyCommand.substring(1, copyCommand.length());

				if (!copyCommand.contains("-"))
					copyCommand = new String();
			}
		}
		return value;
	}

	private <P> String checkCommandValue(String value, CommandArgument<P> argument) {
		if (value == null) {
			if (!argument.hasDefaultValue()) {
				StringBuffer promptOutput = new StringBuffer(String.format(
						"[%s], [%s] value note set which is requiret", argument.commandLong, argument.commandShort));
				promptOutput.append("Application will terminate!");
				System.out.println(promptOutput.toString());
				System.exit(0);
			} else {
				value = argument.value.toString();
			}

		}
		return value;
	}
}
