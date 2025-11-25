package org.pyfuscator.transform;

import org.pyfuscator.scope.ScopeManager;
import org.pyfuscator.transform.passes.VariableRenamePass;
import org.pyfuscator.utils.NameGenerator;
import org.pyfuscator.utils.ObfuscationConfig;

// unused class, it should do what main does currently but im lazy
public class TransformationPipeline {

	// prefixes for when i was going to make this class work so i can just reference it for this.configvar = var
    /*"varLength=" + varLength +
                ", varPrefix='" + varPrefix + '\'' +
                ", preserveBuiltins=" + preserveBuiltins +
                ", preserveImports=" + preserveImports +
                ", inputFile='" + inputFile + '\'' +
                ", outputFile='" + outputFile + '\'' +
                ", keepTemp=" + keepTemp +
                ", verbose=" + verbose +
                ", renameFunctions=" + renameFunctions +
                ", renameClasses=" + renameClasses +
                ", foldConstants=" + foldConstants +
                ", removeDeadCode=" + removeDeadCode +
                ", obfuscateStrings=" + obfuscateStrings +
     */
/*
    private final ObfuscationConfig config;
    private final long seed;
    private final int varLength;
    private final String varPrefix;
    private final boolean preserveBuiltins;
    private final boolean preserveImports;
    private final String inputFile;
    private final String outputFile;
    private final boolean keepTemp;
    private final boolean verbose;
    private final boolean renameFunctions;
    private final boolean renameClasses;
    private final boolean foldConstants;
    private final boolean removeDeadCode;
    private final boolean obfuscateStrings;
*/
	private final ObfuscationConfig config;

	public TransformationPipeline(ObfuscationConfig config) {
        /*
        this.config = config;
        this.seed = config.getSeed();
        this.varLength = config.getVarLength();
        this.varPrefix = config.getVarPrefix();
        this.preserveBuiltins = config.isPreserveBuiltins();
        this.preserveImports = config.isPreserveImports();
        this.inputFile = config.getInputFile();
        this.outputFile = config.getOutputFile();
        this.keepTemp = config.isKeepTemp();
        this.verbose = config.isVerbose();
        this.renameFunctions = config.isRenameFunctions();
        this.renameClasses = config.isRenameClasses();
        this.foldConstants = config.isFoldConstants();
        this.removeDeadCode = config.isRemoveDeadCode();
        this.obfuscateStrings = config.isObfuscateStrings();
         */
		this.config = config;
	}

	// unused method
	public void renameVariables() {
		NameGenerator nameGenerator = new NameGenerator(this.config.getSeed(), this.config.getVarPrefix(), this.config.getVarLength());
		ASTTransformer transformer = new ASTTransformer();
		// Add VariableRenamePass to the transformer
		transformer.addPass(new VariableRenamePass(new ScopeManager(), nameGenerator, this.config)); // scopemanager null will be fixed later

	}

}
