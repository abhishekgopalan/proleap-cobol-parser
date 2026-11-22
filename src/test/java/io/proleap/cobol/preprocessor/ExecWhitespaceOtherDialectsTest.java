package io.proleap.cobol.preprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;
import io.proleap.cobol.preprocessor.impl.CobolPreprocessorImpl;

/**
 * SPARKLE fork (issue #100): characterizes how ProLeap 2.4.0 handles
 * whitespace between EXEC and the dialect word for EXEC SQL, EXEC SQLIMS and
 * EXEC CICS, and pins that the fork did not change it. Only EXEC DLI is
 * whitespace tolerant in the fork (ExecDliWhitespaceTest).
 *
 * <ul>
 * <li>The preprocessor only runs when the text contains one of its literal
 * triggers ("exec sql", "exec sqlims", "exec cics", ...; one blank). A program
 * whose only EXEC statement has two blanks, a tab or a line break is returned
 * unchanged, untagged.</li>
 * <li>When another trigger makes the preprocessor run, blanks and tabs between
 * EXEC and the dialect word are accepted by the grammar (they are hidden
 * tokens), but a line break there is a syntax error.</li>
 * </ul>
 */
public class ExecWhitespaceOtherDialectsTest {

	protected static final String[] DIALECTS = { "CICS", "SQL", "SQLIMS" };

	protected String process(final String code) {
		return new CobolPreprocessorImpl().process(code, CobolSourceFormatEnum.TANDEM);
	}

	protected static String program(final String between, final String dialect, final String extra) {
		return " Procedure Division.\n" + extra + "  EXEC" + between + dialect + " Y END-EXEC.\n";
	}

	@Test
	public void withoutAnotherTriggerOnlyTheSingleBlankFormIsRecognized() {
		for (final String dialect : DIALECTS) {
			assertTrue(dialect, process(program(" ", dialect, "")).contains("*>EXEC" + dialect));

			for (final String between : new String[] { "  ", "\t", "\n  " }) {
				final String code = program(between, dialect, "");
				// the preprocessor drops the final line break only
				assertEquals(dialect + " with <" + between + ">: returned unchanged", code.trim(), process(code).trim());
			}
		}
	}

	@Test
	public void withAnotherTriggerBlanksAndTabsAreAcceptedButALineBreakIsASyntaxError() {
		for (final String dialect : DIALECTS) {
			for (final String between : new String[] { "  ", "\t" }) {
				assertTrue(dialect, process(program(between, dialect, "  EJECT\n")).contains("*>EXEC" + dialect));
			}

			try {
				process(program("\n  ", dialect, "  EJECT\n"));
				throw new AssertionError(dialect + ": expected a syntax error");
			} catch (final RuntimeException e) {
				assertTrue(e.getMessage(), e.getMessage().contains("syntax error"));
			}
		}
	}

	@Test
	public void execDliIsTheOnlyDialectThatToleratesALineBreakWithAnotherTriggerPresent() {
		assertTrue(process(program("\n  ", "DLI", "  EJECT\n")).contains("*>EXECDLI"));
		assertTrue(process(program("\n  ", "DLI", "")).contains("*>EXECDLI"));
	}
}
