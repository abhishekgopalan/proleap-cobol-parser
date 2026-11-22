package io.proleap.cobol.preprocessor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;
import io.proleap.cobol.preprocessor.impl.CobolPreprocessorImpl;

/**
 * SPARKLE fork (issue #100): the preprocessed text of an EXEC DLI block whose
 * EXEC and DLI words are not separated by a single blank. The tag stays on the
 * line of EXEC and the number of lines is the one of the source, so the line
 * numbers of everything after the block (and of the block itself) do not
 * move.
 */
public class ExecDliWhitespacePreprocessedTest {

	protected String process(final String code) {
		return new CobolPreprocessorImpl().process(code, CobolSourceFormatEnum.TANDEM);
	}

	protected static int lines(final String text) {
		return text.split("\n", -1).length;
	}

	@Test
	public void singleBlankIsUnchangedFromTheFirstForkRelease() {
		assertEquals(" Procedure Division.\n*>EXECDLI TERM END-EXEC.\n STOP RUN.",
				process(" Procedure Division.\n EXEC DLI TERM END-EXEC.\n STOP RUN."));
	}

	@Test
	public void severalBlanksAndATabGiveTheSameTextAsOneBlank() {
		final String reference = process(" Procedure Division.\n EXEC DLI TERM END-EXEC.\n STOP RUN.");

		assertEquals(reference, process(" Procedure Division.\n EXEC   DLI TERM END-EXEC.\n STOP RUN."));
		assertEquals(reference, process(" Procedure Division.\n EXEC\tDLI TERM END-EXEC.\n STOP RUN."));
	}

	@Test
	public void aLineBreakKeepsTheTagOnTheExecLineAndTheLineCount() {
		final String code = " Procedure Division.\n EXEC\n DLI TERM END-EXEC.\n STOP RUN.";

		assertEquals(" Procedure Division.\n*>EXECDLI\nTERM END-EXEC.\n STOP RUN.", process(code));
		assertEquals(lines(code), lines(process(code)));
	}

	@Test
	public void severalLineBreaksAndACommentLineKeepTheLineCount() {
		final String blank = " Procedure Division.\n EXEC\n\n DLI TERM END-EXEC.\n STOP RUN.";
		final String comment = " Procedure Division.\n EXEC\n* between\n DLI TERM\n END-EXEC.\n STOP RUN.";

		assertEquals(" Procedure Division.\n*>EXECDLI\n\nTERM END-EXEC.\n STOP RUN.", process(blank));
		assertEquals(lines(blank), lines(process(blank)));
		assertEquals(lines(comment), lines(process(comment)));
		assertEquals(" Procedure Division.\n*>EXECDLI\n*>  between\nTERM\nEND-EXEC.\n STOP RUN.", process(comment));
	}
}
