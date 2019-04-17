// Fadhar J. Castillo
// VM Translator: Parser Module
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {
	public enum C_COMMAND_TYPE {C_ARITHMETIC, C_PUSH, C_POP, 
		C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL};
	private File file;
	private String commandLine, arg1;
	private String[] arithmeticCommands = {"add", "sub", "neg", "eq", "gt", "lt", "and", "not", "or"};
	private String[] segments = {"argument", "local", "static", "constant", "this", "that", "pointer", "temp"};
	private String[] flowAndFunctionCommands = {"label", "goto", "if-goto","function", "call"};
	private C_COMMAND_TYPE commandType;
	private int commandLineNumber, arg2, argumentTwoStart;
	private ArrayList<String> vmCommands;
	/* Function: Opens the input file/stream and gets ready to parse it */
	public Parser(File file)
	{
		this.file=file;
		vmCommands = new ArrayList<String>();
		try 
		{
			FileReader reader = new FileReader(file);
			BufferedReader buffReader = new BufferedReader(reader);
			String line;
			while((line = buffReader.readLine()) != null)
			{
				if(line.equals(""))
					continue;
				else
				{
					String lineNoWhiteSpace = line.replaceAll("\\s", "");
					if(lineNoWhiteSpace.contains("//"))
					{
						int indexOfComment = lineNoWhiteSpace.indexOf("//");
						if(indexOfComment == 0) continue;
						else vmCommands.add(lineNoWhiteSpace.substring(0,indexOfComment));
					}
					else vmCommands.add(lineNoWhiteSpace);
				}
			}
			commandLineNumber = 0;
			buffReader.close();
			reader.close();
		} catch(FileNotFoundException e){
			System.out.println("File Not Found.");
		} catch (IOException e) {
			System.out.println("Error Reading File.");
		}

	}
	
	/* Function: Return true if there are more commands in the .vm file
	 * false otherwise
	 */
	public boolean hasMoreCommands()
	{
		return (commandLineNumber < vmCommands.size());
	}
	/* Function: Reads the next command from the input and makes it
	 * the current command. Should be called only if hasMoreCommands()
	 * is true. Initially there is no current command.
	 */
	public void advance()
	{
		commandLine = vmCommands.get(commandLineNumber);
		commandLineNumber++;
	}
	
	/* Function: Returns the type of the current VM command.
	 * C_ARITHMETIC is returned for all the arithmetic commands.
	 */
	public C_COMMAND_TYPE commandType()
	{
		if(commandLine != null || !commandLine.equals(""))
		{
			for(int i = 0; i < arithmeticCommands.length; i++)
			{
				if(commandLine.contains(arithmeticCommands[i])) {
					commandType = C_COMMAND_TYPE.C_ARITHMETIC;
					return commandType;
				}
			}
			if(commandLine.contains("push"))
				commandType = C_COMMAND_TYPE.C_PUSH;
			else if(commandLine.contains("pop"))
				commandType = C_COMMAND_TYPE.C_POP;
			else if(commandLine.contains("label"))
				commandType = C_COMMAND_TYPE.C_LABEL;
			else if(commandLine.contains("if-goto"))
				commandType = C_COMMAND_TYPE.C_GOTO;
			else if(commandLine.contains("goto"))
				commandType = C_COMMAND_TYPE.C_IF;
			else if(commandLine.contains("function"))
				commandType = C_COMMAND_TYPE.C_FUNCTION;
			else if(commandLine.contains("call"))
				commandType = C_COMMAND_TYPE.C_CALL;
			else if(commandLine.contains("return"))
				commandType = C_COMMAND_TYPE.C_RETURN;
		} else commandType = null;
		return commandType;
	}
	
	/* Fuction: Returns the first argument of the current command.
	 * In the case of C_ARITHMETIC the command itself (add, sub, etc.)
	 * is returned. Should not be called if the current command is C_RETURN.
	 */
	public String arg1() //change command to commandLine, command is for the actual command
	{
		if(commandType.equals(C_COMMAND_TYPE.C_ARITHMETIC))
			for(int i = 0; i < arithmeticCommands.length; i++)
			{
				if(commandLine.contains(arithmeticCommands[i])) {
					arg1 = arithmeticCommands[i];
					break;
				}
			}
		else if(commandType.equals(C_COMMAND_TYPE.C_PUSH) || commandType.equals(C_COMMAND_TYPE.C_POP))
		{
			for(int i = 0; i < segments.length; i++)
			{
				if(commandLine.contains(segments[i])) {
					arg1 = segments[i];
					argumentTwoStart = commandLine.indexOf(arg1)+arg1.length();
					break;
				}
			}
		}
		else if(commandType.equals(C_COMMAND_TYPE.C_FUNCTION)||commandType.equals(C_COMMAND_TYPE.C_CALL)||
				commandType.equals(C_COMMAND_TYPE.C_LABEL)||commandType.equals(C_COMMAND_TYPE.C_GOTO)||
				commandType.equals(C_COMMAND_TYPE.C_IF))
		{
			int functNameStartIndex = 0;
			int functNameEndIndex = commandLine.length();
			for(int i = 0; i < flowAndFunctionCommands.length; i++)
			{
				if(commandLine.contains(flowAndFunctionCommands[i]))
				{
					functNameStartIndex = commandLine.lastIndexOf(flowAndFunctionCommands[i])+1;
					break;
				}
			}
			for(int i = functNameStartIndex; i < commandLine.length(); i++)
			{
				if ((commandLine.charAt(i) >= '0' && commandLine.charAt(i) <= '9')|| (i == commandLine.length()-1))
				{
					functNameEndIndex = i;
					break;
				}
			}
			arg1 = commandLine.substring(functNameStartIndex, functNameEndIndex);
			argumentTwoStart = functNameEndIndex+1;
		}
		else arg1 = null;
		return arg1;
	}
	
	/* Function: Returns the second argument of the current command.
	 * Should be called only if the current command is C_PUSH, C_POP,
	 * C_FUNCTION, or C_CALL.
	 */
	public int arg2()
	{
		if(commandType.equals(C_COMMAND_TYPE.C_PUSH)||commandType.equals(C_COMMAND_TYPE.C_POP)||
				commandType.equals(C_COMMAND_TYPE.C_FUNCTION)||commandType.equals(C_COMMAND_TYPE.C_CALL))
		{
			try {
				arg2 = Integer.parseInt(commandLine.substring(argumentTwoStart));
			}catch(NumberFormatException nfe) {
				System.out.println("Argument 2 is not a positive constant");}
		}
		return arg2;
	}
	
}
