package org.pyfuscator.utils;

public class ObfuscationConfig {
	private int varLength = 8;
	private String varPrefix = "v";
	private long seed = System.currentTimeMillis();
	private boolean preserveBuiltins = true;
	private boolean preserveImports = true;
	private String inputFile;
	private String outputFile;
	private boolean keepTemp = false;
	private boolean verbose = false;

	private boolean renameFunctions = false;
	private boolean renameClasses = false;
	private boolean foldConstants = false;
	private boolean removeDeadCode = false;
	private boolean obfuscateStrings = false;
    private boolean removeDocs = false;

	public static ObfuscationConfig createDefault() {
		return new ObfuscationConfig();
	}

	public int getVarLength() {
		return varLength;
	}

	public void setVarLength(int varLength) {
		this.varLength = varLength;
	}

	public String getVarPrefix() {
		return varPrefix;
	}

	public void setVarPrefix(String varPrefix) {
		this.varPrefix = varPrefix;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public boolean isPreserveBuiltins() {
		return preserveBuiltins;
	}

	public void setPreserveBuiltins(boolean preserveBuiltins) {
		this.preserveBuiltins = preserveBuiltins;
	}

	public boolean isPreserveImports() {
		return preserveImports;
	}

	public void setPreserveImports(boolean preserveImports) {
		this.preserveImports = preserveImports;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public boolean isKeepTemp() {
		return keepTemp;
	}

	public void setKeepTemp(boolean keepTemp) {
		this.keepTemp = keepTemp;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isRenameFunctions() {
		return renameFunctions;
	}

	public void setRenameFunctions(boolean renameFunctions) {
		this.renameFunctions = renameFunctions;
	}

	public boolean isRenameClasses() {
		return renameClasses;
	}

	public void setRenameClasses(boolean renameClasses) {
		this.renameClasses = renameClasses;
	}

	public boolean isFoldConstants() {
		return foldConstants;
	}

	public void setFoldConstants(boolean foldConstants) {
		this.foldConstants = foldConstants;
	}

	public boolean isRemoveDeadCode() {
		return removeDeadCode;
	}

	public void setRemoveDeadCode(boolean removeDeadCode) {
		this.removeDeadCode = removeDeadCode;
	}

	public boolean isObfuscateStrings() {
		return obfuscateStrings;
	}

	public void setObfuscateStrings(boolean obfuscateStrings) {
		this.obfuscateStrings = obfuscateStrings;
	}

    public boolean isRemoveDocs() { return removeDocs; }

    public void setRemoveDocs(boolean removeDocs) { this.removeDocs = removeDocs; }
	// validation method
	public void validate() {
		if (inputFile == null || inputFile.isEmpty()) {
			throw new IllegalArgumentException("Input file must be specified");
		}
		if (outputFile == null || outputFile.isEmpty()) {
			throw new IllegalArgumentException("Output file must be specified");
		}
	}

	// toString method for debugging (not used)
	@Override
	public String toString() {
		return "ObfuscationConfig{varLength=" + varLength +
				", varPrefix='" + varPrefix + '\'' +
				", inputFile='" + inputFile + '\'' +
				", outputFile='" + outputFile + '\'' + '}';
	}
}
