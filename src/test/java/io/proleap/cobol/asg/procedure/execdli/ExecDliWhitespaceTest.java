package io.proleap.cobol.asg.procedure.execdli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.proleap.cobol.CobolTestBase;
import io.proleap.cobol.asg.metamodel.Program;
import io.proleap.cobol.asg.metamodel.procedure.ProcedureDivision;
import io.proleap.cobol.asg.metamodel.procedure.Statement;
import io.proleap.cobol.asg.metamodel.procedure.StatementTypeEnum;
import io.proleap.cobol.asg.metamodel.procedure.execdli.ExecDliStatement;
import io.proleap.cobol.asg.runner.impl.CobolParserRunnerImpl;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;

/**
 * SPARKLE fork (issue #100): EXEC DLI is recognized with several blanks, a tab
 * or a line break between EXEC and DLI, and the resulting statement equals the
 * single-blank form, line numbers included. A malformed block still fails
 * loudly. EXEC SQL, EXEC SQLIMS and EXEC CICS are not touched (see
 * {@link io.proleap.cobol.preprocessor.ExecWhitespaceOtherDialectsTest}).
 */
public class ExecDliWhitespaceTest extends CobolTestBase {

	/**
	 * A function with every kind of clause, over several lines; %s is what stands
	 * between EXEC and DLI.
	 */
	protected static final String GU_BODY = "GU USING PCB(PAUT-PCB-NUM)\n" //
			+ "      SEGMENT (PAUTSUM0)\n" //
			+ "      INTO (PENDING-AUTH-SUMMARY)\n" //
			+ "      WHERE (ACCNTID = PA-ACCT-ID)\n" //
			+ "  END-EXEC.\n";

	protected static final String GU_DESCRIPTION = "GU PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) INTO(PENDING-AUTH-SUMMARY) WHERE(ACCNTID = PA-ACCT-ID)";

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	protected static String tandemProgram(final String between, final String body) {
		return " Identification Division.\n" //
				+ " Program-ID. ExecDliWs.\n" //
				+ " Procedure Division.\n" //
				+ "\n" //
				+ "  EXEC" + between + "DLI " + body //
				+ "\n" //
				+ "  STOP RUN.\n";
	}

	protected Program analyze(final String code, final CobolSourceFormatEnum format) throws IOException {
		// the compilation unit is named after the file, which must match the Program-ID
		final File file = new File(folder.newFolder(), "ExecDliWs.cbl");
		Files.write(file.toPath(), code.getBytes(StandardCharsets.ISO_8859_1));
		return new CobolParserRunnerImpl().analyzeFile(file, format);
	}

	protected static int lineOf(final String code, final String text) {
		final String[] lines = code.split("\n", -1);

		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains(text)) {
				return i + 1;
			}
		}

		throw new IllegalArgumentException(text);
	}

	/**
	 * Asserts the program has exactly one EXEC DLI statement and a STOP, that the
	 * DLI statement reads like {@code expectedDescription}, starts on the line of
	 * EXEC and that STOP is still on its own source line.
	 */
	protected void assertSingleDli(final String code, final CobolSourceFormatEnum format,
			final String expectedDescription) throws IOException {
		final Program program = analyze(code, format);
		final ProcedureDivision procedureDivision = program.getCompilationUnit("ExecDliWs").getProgramUnit()
				.getProcedureDivision();
		final List<Statement> statements = procedureDivision.getStatements();

		assertEquals(code, 2, statements.size());
		assertEquals(StatementTypeEnum.EXEC_DLI, statements.get(0).getStatementType());
		assertEquals(StatementTypeEnum.STOP, statements.get(1).getStatementType());

		final ExecDliStatement dli = (ExecDliStatement) statements.get(0);
		assertEquals(expectedDescription, ExecDliDescriber.describe(dli.getCtx()));
		assertEquals("DLI statement starts on the line of EXEC", lineOf(code, "EXEC"), dli.getCtx().getStart().getLine());
		assertEquals("STOP keeps its source line", lineOf(code, "STOP RUN"),
				statements.get(1).getCtx().getStart().getLine());
	}

	@Test
	public void singleBlankIsTheReference() throws Exception {
		assertSingleDli(tandemProgram(" ", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
	}

	@Test
	public void twoAndSeveralBlanksAreRecognized() throws Exception {
		assertSingleDli(tandemProgram("  ", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
		assertSingleDli(tandemProgram("      ", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
	}

	@Test
	public void aTabIsRecognized() throws Exception {
		assertSingleDli(tandemProgram("\t", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
		assertSingleDli(tandemProgram(" \t ", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
	}

	@Test
	public void aLineBreakBetweenExecAndDliIsRecognized() throws Exception {
		assertSingleDli(tandemProgram("\n  ", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
		// TANDEM format: column 1 is the indicator column, so the DLI line starts with a blank
		assertSingleDli(tandemProgram("\n ", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
		assertSingleDli(tandemProgram("  \n        ", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
	}

	@Test
	public void severalLineBreaksBetweenExecAndDliAreRecognized() throws Exception {
		assertSingleDli(tandemProgram("\n\n  ", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
	}

	@Test
	public void aCommentLineBetweenExecAndDliIsRecognized() throws Exception {
		// column 1 asterisk is a comment line in TANDEM format
		assertSingleDli(tandemProgram("\n* a comment line\n  ", GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
	}

	@Test
	public void everyFunctionKeepsItsStructureAcrossALineBreak() throws Exception {
		assertSingleDli(tandemProgram("\n  ", "SCHD PSB((PSB-NAME)) NODHABEND END-EXEC.\n"), CobolSourceFormatEnum.TANDEM,
				"SCHD PSB((PSB-NAME)) OPTION(NODHABEND)");
		assertSingleDli(tandemProgram("\n  ", "TERM END-EXEC.\n"), CobolSourceFormatEnum.TANDEM, "TERM");
		assertSingleDli(tandemProgram("\n  ", "CHKP ID(WK-CHKPT-ID)\n  END-EXEC\n"), CobolSourceFormatEnum.TANDEM,
				"CHKP ID(WK-CHKPT-ID)");
	}

	@Test
	public void theDliWordOnTheNextLineWithNothingAfterItStillWorks() throws Exception {
		// "DLI" alone on its line, the function on the one after
		assertSingleDli(tandemProgram("\n  ", "\n  " + GU_BODY), CobolSourceFormatEnum.TANDEM, GU_DESCRIPTION);
	}

	/**
	 * Fixed format: columns 1-6 sequence area, 7 indicator, 8-72 text, 73-80
	 * ignored.
	 */
	protected static String fixedLine(final String text) {
		final StringBuilder sb = new StringBuilder("      " + " " + text);

		while (sb.length() < 72) {
			sb.append(' ');
		}

		return sb.append("ABCDEFGH").toString();
	}

	protected static String fixedProgram(final String execLine, final String dliLines) {
		return fixedLine("Identification Division.") + "\n" //
				+ fixedLine("Program-ID. ExecDliWs.") + "\n" //
				+ fixedLine("Procedure Division.") + "\n" //
				+ fixedLine("") + "\n" //
				+ execLine + "\n" //
				+ dliLines //
				+ fixedLine("") + "\n" //
				+ fixedLine("  STOP RUN.");
	}

	/**
	 * The lines of the GU block after its first line; with a prefix, the GU line
	 * itself comes first (for DLI on the line after EXEC).
	 */
	protected static String fixedGuLines(final String firstLinePrefix) {
		return (firstLinePrefix == null ? "" : fixedLine(firstLinePrefix + "GU USING PCB(PAUT-PCB-NUM)") + "\n") //
				+ fixedLine("      SEGMENT (PAUTSUM0)") + "\n" //
				+ fixedLine("      INTO (PENDING-AUTH-SUMMARY)") + "\n" //
				+ fixedLine("      WHERE (ACCNTID = PA-ACCT-ID)") + "\n" //
				+ fixedLine("  END-EXEC.") + "\n";
	}

	@Test
	public void fixedFormatSingleBlankIsTheReference() throws Exception {
		assertSingleDli(fixedProgram(fixedLine("  EXEC DLI GU USING PCB(PAUT-PCB-NUM)"), fixedGuLines(null)),
				CobolSourceFormatEnum.FIXED, GU_DESCRIPTION);
	}

	@Test
	public void fixedFormatExecAtColumn72AndDliOnTheNextLine() throws Exception {
		// EXEC occupies columns 69-72 (the last text columns), DLI starts the next line
		final String execLine = fixedLine(spaces(61) + "EXEC");
		assertEquals(72, execLine.indexOf("EXEC") + 4);
		assertSingleDli(fixedProgram(execLine, fixedGuLines("DLI ")), CobolSourceFormatEnum.FIXED, GU_DESCRIPTION);
	}

	@Test
	public void fixedFormatTwoBlanksBetweenExecAndDli() throws Exception {
		assertSingleDli(fixedProgram(fixedLine("  EXEC  DLI GU USING PCB(PAUT-PCB-NUM)"),
				fixedLine("      SEGMENT (PAUTSUM0)") + "\n" //
						+ fixedLine("      INTO (PENDING-AUTH-SUMMARY)") + "\n" //
						+ fixedLine("      WHERE (ACCNTID = PA-ACCT-ID)") + "\n" //
						+ fixedLine("  END-EXEC.") + "\n"),
				CobolSourceFormatEnum.FIXED, GU_DESCRIPTION);
	}

	protected static String spaces(final int n) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < n; i++) {
			sb.append(' ');
		}

		return sb.toString();
	}

	@Test
	public void aMalformedBlockStillFailsLoudlyInEveryWhitespaceForm() throws Exception {
		final String[] betweens = { " ", "  ", "\t", "\n  " };

		for (final String between : betweens) {
			assertFails(tandemProgram(between, "NOSUCHFUNCTION USING PCB(PCB-NUM)\n  END-EXEC.\n"));
		}
	}

	@Test
	public void anUnterminatedBlockStillFailsLoudlyInEveryWhitespaceForm() throws Exception {
		final String[] betweens = { " ", "  ", "\t", "\n  " };

		for (final String between : betweens) {
			assertFails(tandemProgram(between, "GU USING PCB(PCB-NUM)\n"));
		}
	}

	protected void assertFails(final String code) throws IOException {
		try {
			analyze(code, CobolSourceFormatEnum.TANDEM);
		} catch (final RuntimeException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("syntax error"));
			return;
		}

		throw new AssertionError("expected a syntax error for\n" + code);
	}
}
