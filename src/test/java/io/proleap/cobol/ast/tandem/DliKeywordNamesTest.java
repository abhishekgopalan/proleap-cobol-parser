package io.proleap.cobol.ast.tandem;

import java.io.File;

import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;
import io.proleap.cobol.runner.CobolParseTestRunner;
import io.proleap.cobol.runner.impl.CobolParseTestRunnerImpl;
import org.junit.Test;

/**
 * DL/I function and clause words (GU, TERM, PCB, WHERE, ...) are keyword tokens
 * since EXEC DLI is supported, but must still be usable as ordinary names. The
 * expected parse tree was generated with stock ProLeap 2.4.0, i.e. this test
 * proves that non-DLI input parses to the same tree as before.
 */
public class DliKeywordNamesTest {

	@Test
	public void test() throws Exception {
		final File inputFile = new File("src/test/resources/io/proleap/cobol/ast/tandem/DliKeywordNames.cbl");
		final CobolParseTestRunner runner = new CobolParseTestRunnerImpl();
		runner.parseFile(inputFile, CobolSourceFormatEnum.TANDEM);
	}
}
