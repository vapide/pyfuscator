package org.pyfuscator.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RunPython {
	private final String pythonCommand;

	public RunPython() {
		this("python");
	}

	public RunPython(String pythonCommand) {
		this.pythonCommand = pythonCommand;
	}

	public static String getAbsoluteParserPath() {
		return extractResourceToTemp("python/parser.py");
	}

	public static String getAbsouteCompilerPath() {
		return extractResourceToTemp("python/compiler.py");
	}

	// extracts resource (a resource is the file inside the jar) to a temporary file and returns the absolute path instead of relative
	private static String extractResourceToTemp(String resourcePath) {
		try {
			File devFile = new File(System.getProperty("user.dir") + File.separator + "src" +
					File.separator + "main" + File.separator + resourcePath.replace("/", File.separator)); // devfile is the file in the source directory
			if (devFile.exists()) {
				return devFile.getAbsolutePath();
			}

			// break file path semantically into name and extension to create temp file
			InputStream is = RunPython.class.getClassLoader().getResourceAsStream(resourcePath);
			String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
			String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
			String extension = fileName.substring(fileName.lastIndexOf('.'));

			File tempFile = File.createTempFile("pyfuscator_" + baseName + "_", extension);
			tempFile.deleteOnExit();

			java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile); // basically a stringbuilder for bytes of the file
			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) { // reads until the end of file
				fos.write(buffer, 0, bytesRead); // writes
			}
			fos.close();
			is.close();

			return tempFile.getAbsolutePath();
		} catch (Exception e) {
			throw new RuntimeException("Failed to extract resource: " + resourcePath, e);
		}
	}

	// runs the python command with the given arguments using processbuilder
	public Result run(String... args) throws Exception {
		List<String> command = new ArrayList<>();
		command.add(pythonCommand);
		command.addAll(Arrays.asList(args));

		ProcessBuilder pb = new ProcessBuilder(command);
		Process p = pb.start();

		p.waitFor();

		String stdout = readStream(p.getInputStream());
		String stderr = readStream(p.getErrorStream());

		return new Result(p.exitValue(), stdout, stderr);
	}

	private String readStream(InputStream stream) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	public record Result(int exitCode, String stdout, String stderr) {
	}

}
