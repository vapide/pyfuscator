package org.pyfuscator;

import org.pyfuscator.ast.JsonASTLoader;
import org.pyfuscator.ast.JsonASTWriter;
import org.pyfuscator.ast.Node;
import org.pyfuscator.scope.ScopeManager;
import org.pyfuscator.transform.ASTTransformer;
import org.pyfuscator.transform.passes.VariableRenamePass;
import org.pyfuscator.utils.NameGenerator;
import org.pyfuscator.utils.ObfuscationConfig;
import org.pyfuscator.utils.RunPython;

import java.io.File;

public class Main {
	public static void main(String[] args) {
		try {
			// parse the arguments
			ObfuscationConfig config = parseArgs(args);
			config.validate();
			obfuscate(config);
		} catch (IllegalArgumentException e) {
			System.err.println("error: " + e.getMessage());
			printUsage();
			System.exit(1);
		} catch (Exception e) {
			System.err.println("obfuscation failed: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}


	private static ObfuscationConfig parseArgs(String[] args) {
		// default config
		ObfuscationConfig config = ObfuscationConfig.createDefault();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			// switch statement for parsing args and setting config vars
			switch (arg) {
				case "--input":
					config.setInputFile(args[++i]);
					break;

				case "--output":
					config.setOutputFile(args[++i]);
					break;

				case "--var-length":
					config.setVarLength(Integer.parseInt(args[++i]));
					break;

				case "--var-prefix":
					config.setVarPrefix(args[++i]);
					break;

				case "--seed":
					config.setSeed(Long.parseLong(args[++i]));
					break;

				case "--preserve-builtins":
					config.setPreserveBuiltins(true);
					break;

				case "--no-preserve-builtins":
					config.setPreserveBuiltins(false);
					break;

				case "--preserve-imports":
					config.setPreserveImports(true);
					break;

				case "--no-preserve-imports":
					config.setPreserveImports(false);
					break;

				case "--keep-temp":
					config.setKeepTemp(true);
					break;

				case "--verbose":
					config.setVerbose(true);
					break;

				case "--rename-functions":
					config.setRenameFunctions(true);
					break;

				case "--rename-classes":
					config.setRenameClasses(true);
					break;

				case "--fold-constants":
					config.setFoldConstants(true);
					break;

				case "--remove-dead-code":
					config.setRemoveDeadCode(true);
					break;

				case "--obfuscate-strings":
					config.setObfuscateStrings(true);
					break;

				case "--help":
				case "-h":
					printUsage();
					System.exit(0);
					break;

				default:
					throw new IllegalArgumentException("unknown option: " + arg);
			}
		}

		return config;
	}

	private static void obfuscate(ObfuscationConfig config) throws Exception {
		// initialize stuff
		RunPython python = new RunPython("python");
		ScopeManager scopeManager = new ScopeManager();
		NameGenerator nameGenerator = new NameGenerator(config);

		File inputFile = new File(config.getInputFile());
		String absoluteInputPath = inputFile.getAbsolutePath();
		File outputFile = new File(config.getOutputFile());
		String absoluteOutputPath = outputFile.getAbsolutePath();

		String parserPath = RunPython.getAbsoluteParserPath();
		String tempJsonPath = absoluteInputPath + ".temp.json";

		// run the parser and check for errors
		RunPython.Result parseResult = python.run(parserPath, absoluteInputPath, tempJsonPath);
		if (parseResult.exitCode() != 0) {
			throw new RuntimeException("parser failed: " + parseResult.stderr());
		}

		JsonASTLoader loader = new JsonASTLoader();
		Node rootNode = loader.loadFromFile(new File(tempJsonPath));

		ASTTransformer transformer = new ASTTransformer();
		transformer.addPass(new VariableRenamePass(scopeManager, nameGenerator, config));
		Node transformedNode = transformer.transform(rootNode);

		String transformedJsonPath = absoluteOutputPath + ".temp.json";
		JsonASTWriter writer = new JsonASTWriter();
		writer.writeToFile(transformedNode, transformedJsonPath);

		String compilerPath = RunPython.getAbsouteCompilerPath();
		RunPython.Result compileResult = python.run(compilerPath, transformedJsonPath, absoluteOutputPath);
		// same thing but for compiler
		if (compileResult.exitCode() != 0) {
			throw new RuntimeException("compiler failed: " + compileResult.stderr());
		}

		// clean up temp files
		if (!config.isKeepTemp()) {
			new File(tempJsonPath).delete();
			new File(transformedJsonPath).delete();
		}

		System.out.println("Done! Output saved to: " + absoluteOutputPath);
	}

	//usage information
	private static void printUsage() {
		System.out.println("usage: java -jar obfuscator.jar [options]");
		System.out.println();
		System.out.println("required:");
		System.out.println("  --input <file>          input Python file");
		System.out.println("  --output <file>         output file");
		System.out.println();
		System.out.println("options:");
		System.out.println("  --var-length <n>        variable name length (default: 8)");
		System.out.println("  --var-prefix <str>      variable prefix (default: 'v')");
		System.out.println("  --seed <n>              random seed");
		System.out.println("  --preserve-builtins     don't rename builtins (default)");
		System.out.println("  --no-preserve-builtins  rename builtins");
		System.out.println("  --preserve-imports      don't rename imports (default)");
		System.out.println("  --keep-temp             keep temp files");
		//System.out.println("  --verbose               verbose output");
		System.out.println("  --rename-functions      rename functions");
		System.out.println("  --rename-classes        rename classes");
		System.out.println("  --help, -h              show help");
	}
}
