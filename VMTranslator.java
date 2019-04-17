// Fadhar J. Castillo
// VM Translator
import java.io.File;
import java.util.ArrayList;

public class VMTranslator {

	public static void main(String[] args) {
		Parser parser;
		CodeWriter codeWriter = null;
		File fileOrDirectory;
		File assemblyFile;
		ArrayList<File> vmFiles;
		String filePath = "C:\\Users\\thedr_000\\Downloads\\nand2tetris\\nand2tetris\\projects\\07\\StackArithmetic\\StackTest";
		//Check if there is more than 1 command line argument
		if(true)
		{
			//args.length == 1
			//Check if it is a single file or a directory
			fileOrDirectory = new File(filePath);
			if(fileOrDirectory.isFile())
			{
				if(fileOrDirectory.getName().endsWith(".vm"))
				{
					assemblyFile = new File(filePath.replaceAll(".vm", ".asm"));
					parser = new Parser(fileOrDirectory);
					codeWriter = new CodeWriter(assemblyFile);
					parseAndTranslate(parser, codeWriter, assemblyFile);
					codeWriter.Close();
				}
			}
			else if(fileOrDirectory.isDirectory())
			{
				File[] files = fileOrDirectory.listFiles();
				vmFiles = new ArrayList<File>();
				for(File file:files)
				{
					if(file.getName().endsWith(".vm"))
					{
						vmFiles.add(file);
					}
				}
				for(File vmFile : vmFiles)
				{
					assemblyFile = new File(vmFile.getAbsolutePath().replaceAll(".vm",".asm"));
					parser = new Parser(vmFile);
					if(codeWriter == null)
					{
						codeWriter = new CodeWriter(assemblyFile);
					}
					else
					{
						codeWriter.setFileName(assemblyFile.getAbsolutePath());
					}
					parseAndTranslate(parser, codeWriter, assemblyFile);
					codeWriter.Close();
				}
			}
		}
	}
	/*
	 * Function: Parses parses the VM commands by calling the Parser, translates the VM commands to
	 * Hack Assembly and writes to file by calling the CodeWriter
	 */
	private static void parseAndTranslate(Parser p, CodeWriter cw, File assemblyFile)
	{
		while(p.hasMoreCommands())
		{
			p.advance();
			Parser.C_COMMAND_TYPE commandType = p.commandType();
			switch(commandType)
			{
			case C_ARITHMETIC:
				cw.writeArithmetic(p.arg1());
				break;
			case C_PUSH: case C_POP:
				cw.WritePushPop(commandType, p.arg1(), p.arg2());
				break;
			default:
				break;
			}
		}
	}
}
