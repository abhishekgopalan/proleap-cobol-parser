package io.proleap.cobol.asg.procedure.execdli;

import java.io.File;

import org.junit.Test;

import io.proleap.cobol.CobolTestBase;
import io.proleap.cobol.asg.runner.impl.CobolParserRunnerImpl;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;

/**
 * An EXEC DLI block the grammar cannot parse fails loudly instead of being
 * skipped silently.
 */
public class ExecDliInvalidTest extends CobolTestBase {

	@Test(expected = RuntimeException.class)
	public void test() throws Exception {
		final File inputFile = new File("src/test/resources/io/proleap/cobol/asg/procedure/execdli/ExecDliInvalid.cbl");
		new CobolParserRunnerImpl().analyzeFile(inputFile, CobolSourceFormatEnum.TANDEM);
	}
}
