// Fadhar J. Castillo
// VM Translator: CodeWriter Module
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
	private BufferedWriter writer = null;
	private int jumpIndex;
	private File file;
	private final String addSubAndOrAssemblyTemplate = "@SP\n AM=M-1\n D=M\n A=A-1\n";
	private String assembly = "";
	/**
	 * Function: Private helper function that returns a string with a unique label assembly instruction
	 * @param jumpInstruction
	 * @return
	 */
	private String etLtGtAssemblyTemplate(String jumpInstruction) {
		return "D=M-D\n"+
			"@FALSE"+jumpIndex+"\n"+
			"D;"+jumpInstruction+"\n"+
			"@SP\n"+
			"A=M-1\n"+
			"M=-1\n"+
			"@CONTINUE"+jumpIndex+"\n"+
			"0;JMP\n"+
			"(FALSE"+jumpIndex+")\n"+
			"@SP\n"+
			"A=M-1\n"+
			"M=0\n"+
			"(CONTINUE"+jumpIndex+")\n";
	}
	/**
	 * Function: Private helper function that returns a string with a unique label assembly instruction
	 * If index >= 0, push instructions whose segments are argument, local, this, that, and temp
	 * otherwise, push instructions whose segments are static and pointer
	 * @param segment, index
	 * @return
	 */
	private String pushAssemblyTemplate(String segment, int index)
	{
		if(index == -1)
		{	
		return "@"+segment+"\n"+
			"D=M\n"+
			"@SP\n"+
			"A=M\n"+
			"M=D\n"+
			"@SP\n"+
			"M=M+1\n";
		}
		else
		{
			return "@"+segment+"\n"+
				"D=M\n"+
				"@"+index+"\n"+
				"A=D+A\n"+
				"D=M\n"+
				"@SP\n"+
				"A=M\n"+
				"M=D\n"+
				"@SP\n"+
				"M=M+1\n";
		}
	}
	
	private String popAssemblyTemplate(String segment, int index)
	{
		if(index == -1)
		{
			return "@" + segment + "\n" +
            "D=A\n" +
            "@R13\n" +
            "M=D\n" +
            "@SP\n" +
            "AM=M-1\n" +
            "D=M\n" +
            "@R13\n" +
            "A=M\n" +
            "M=D\n";
		}
		else {
			return "@" + segment + "\n" +
			"D=M\n@" + index + "\nD=D+A\n" +
            "@R13\n" +
			"M=D\n" +
            "@SP\n" +
            "AM=M-1\n" +
            "D=M\n" +
            "@R13\n" +
            "A=M\n" +
            "M=D\n";
		}
	}
	
	/** Function: Opens the output file/stream and gets ready to write into it.
	 * @param filePath Specifies the directory where the collection of .vm files are
	 */
	CodeWriter(File file)
	{
		this.file = file;
		writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(this.file));
		} catch (Exception e) {
			System.out.println("Unable to create output file");
			System.exit(0);
		}
		jumpIndex = 0;
	}
	/**
	 * Function: Informs the CodeWriter that the translation of a new VM
	 * file is started.
	 * @param fileName
	 */
	public void setFileName(String fileName)
	{
		writer = null;
		try {
			File fileToWrite = new File(fileName);
			writer = new BufferedWriter(new FileWriter(fileToWrite));
		} catch (Exception e) {
			System.out.println("Unable to create output file");
			System.exit(0);
		}
	}
	/** Writes the assembly code that is the translation of the given arithmetic
	 * command.
	 * @param command
	 */
	public void writeArithmetic(String command)
	{
		switch(command)
		{
		case "add":
			assembly = addSubAndOrAssemblyTemplate + "M=M+D\n";
			break;
		case "sub":
        	assembly = addSubAndOrAssemblyTemplate + "M=M-D\n";
        	break;
		case "and":
        	assembly = addSubAndOrAssemblyTemplate + "M=M&D\n";
        	break;
		case "or":
        	assembly = addSubAndOrAssemblyTemplate + "M=M|D\n";
        	break;
		case "gt":
            assembly = etLtGtAssemblyTemplate("JLE");
            jumpIndex++;
            break;
		case "lt":
        	 assembly = etLtGtAssemblyTemplate("JGE");//not <=
             jumpIndex++;
             break;
		case "eq":
        	 assembly = etLtGtAssemblyTemplate("JNE");//not <=
             jumpIndex++;
             break;
		case "not":
			assembly = "@SP\nA=M-1\nM=!M\n";
			break;
		case "neg":
			assembly = "D=0\\n@SP\\nA=M-1\\nM=D-M\\n";
			break;
		default:
            System.out.println(command +"is not a valid arithmetic Virtual Machine command");
		}
		try {
			writer.write(assembly);
		}catch(IOException e)
		{
			System.out.println("Unable to write translation to file");
		}
	}
	/** Function: Writes the assembly code that is the translation of the given command.
	 * Where command is either C_PUSH or C_POP
	 * @param command
	 * @param segment
	 * @param index
	 */
	public void WritePushPop(Parser.C_COMMAND_TYPE command, String segment, int index)
	{
		switch(command)
		{
		case C_PUSH:
			switch(segment)
			{
			case "argument":
				assembly = pushAssemblyTemplate("ARG", index);
				break;
			case "local":
				assembly = pushAssemblyTemplate("LCL", index);
				break;
			case "static":
				assembly = pushAssemblyTemplate(String.valueOf(index+16), -1);
				break;
			case "constant":
				 assembly = "@" + index + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n";
				break;
			case "this":
				assembly = pushAssemblyTemplate("THIS", index);
				break;
			case "that":
				assembly = pushAssemblyTemplate("THAT", index);
				break;
			case "pointer":
				switch(index)
				{
				case 0:
					assembly = pushAssemblyTemplate("THAT", index);
					break;
				case 1:
					assembly = pushAssemblyTemplate("THAT", index);
					break;
				default:
					System.out.println("Pointer Segment Index out of Range");
				}
				break;
			case "temp":
				assembly = pushAssemblyTemplate("R5", index+5);
				break;
			}
			break;
		case C_POP:
			switch(segment)
			{
			case "argument":
				assembly = popAssemblyTemplate("ARG", index);
				break;
			case "local":
				assembly = popAssemblyTemplate("LCL", index);
				break;
			case "static":
				assembly = popAssemblyTemplate(String.valueOf(index+16), -1);
				break;
			case "constant":
				assembly = "@" + index + "\n" + "D=A\n" + "@SP\n" + "A=M\n" + "M=D\n" + "@SP\n" + "M=M+1\n";
				break;
			case "this":
				assembly = popAssemblyTemplate("THIS", index);
				break;
			case "that":
				assembly = popAssemblyTemplate("THAT", index);
				break;
			case "pointer":
				switch(index)
				{
				case 0:
					assembly = popAssemblyTemplate("THAT", index);
					break;
				case 1:
					assembly = popAssemblyTemplate("THAT", index);
					break;
				default:
					System.out.println("Pointer Segment Index out of Range");
				}
				break;
			case "temp":
				assembly = popAssemblyTemplate("R5", index+5);
				break;
			}
			break;
		default:
			System.out.println("Invalid command type");
		}
		try {
			writer.write(assembly);
		} catch (IOException e) {
			System.out.println("Unable to write translation to file");
		}
	}
	
	/** Function: Closes the output file.
	 */
	public void Close()
	{
		try {
			writer.close();
		} catch (IOException e) {
			System.out.println("Unable to close file");
		}
	}
}
