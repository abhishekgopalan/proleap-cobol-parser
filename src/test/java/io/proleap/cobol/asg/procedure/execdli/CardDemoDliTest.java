package io.proleap.cobol.asg.procedure.execdli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.Test;

import io.proleap.cobol.CobolTestBase;
import io.proleap.cobol.asg.metamodel.Program;
import io.proleap.cobol.asg.metamodel.procedure.Paragraph;
import io.proleap.cobol.asg.metamodel.procedure.ProcedureDivision;
import io.proleap.cobol.asg.metamodel.procedure.Statement;
import io.proleap.cobol.asg.metamodel.procedure.StatementTypeEnum;
import io.proleap.cobol.asg.metamodel.procedure.execdli.ExecDliStatement;
import io.proleap.cobol.asg.runner.impl.CobolParserRunnerImpl;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;

/**
 * Parses all 26 EXEC DLI blocks of the CardDemo authorization module (copied
 * verbatim, fixed format with sequence numbers) and checks them against the
 * call counts of that module.
 */
public class CardDemoDliTest extends CobolTestBase {

	protected List<ExecDliStatement> collectExecDliStatements(final ProcedureDivision procedureDivision,
			final Map<String, Integer> blocksPerParagraph) {
		final List<ExecDliStatement> result = new ArrayList<ExecDliStatement>();

		for (final Paragraph paragraph : procedureDivision.getParagraphs()) {
			int blocks = 0;

			for (final Statement statement : paragraph.getStatements()) {
				if (statement instanceof ExecDliStatement) {
					assertEquals(StatementTypeEnum.EXEC_DLI, statement.getStatementType());
					result.add((ExecDliStatement) statement);
					blocks++;
				}
			}

			blocksPerParagraph.put(paragraph.getName(), blocks);
		}

		return result;
	}

	@Test
	public void test() throws Exception {
		final File inputFile = new File("src/test/resources/io/proleap/cobol/asg/procedure/execdli/CardDemoDli.cbl");
		final Program program = new CobolParserRunnerImpl().analyzeFile(inputFile, CobolSourceFormatEnum.FIXED);

		final ProcedureDivision procedureDivision = program.getCompilationUnit("CardDemoDli").getProgramUnit()
				.getProcedureDivision();

		final Map<String, Integer> blocksPerParagraph = new TreeMap<String, Integer>();
		final List<ExecDliStatement> statements = collectExecDliStatements(procedureDivision, blocksPerParagraph);

		// blocks per program
		assertEquals(5, (int) blocksPerParagraph.get("BLOCKS-CBPAUP0C"));
		assertEquals(8, (int) blocksPerParagraph.get("BLOCKS-COPAUA0C"));
		assertEquals(6, (int) blocksPerParagraph.get("BLOCKS-COPAUS0C"));
		assertEquals(7, (int) blocksPerParagraph.get("BLOCKS-COPAUS1C"));
		assertEquals(26, statements.size());

		// calls per function
		final Map<String, Integer> functions = new TreeMap<String, Integer>();
		final Set<String> shapes = new TreeSet<String>();

		for (final ExecDliStatement statement : statements) {
			final String function = statement.getCtx().dliFunction().getText();
			final Integer count = functions.get(function);
			functions.put(function, count == null ? 1 : count + 1);

			shapes.add(ExecDliDescriber.describe(statement.getCtx()));
		}

		assertEquals(9, functions.size());
		assertEquals(6, (int) functions.get("SCHD"));
		assertEquals(5, (int) functions.get("GNP"));
		assertEquals(4, (int) functions.get("TERM"));
		assertEquals(3, (int) functions.get("GU"));
		assertEquals(2, (int) functions.get("ISRT"));
		assertEquals(2, (int) functions.get("REPL"));
		assertEquals(2, (int) functions.get("DLET"));
		assertEquals(1, (int) functions.get("GN"));
		assertEquals(1, (int) functions.get("CHKP"));

		// the distinct shapes used in the module
		final Set<String> expectedShapes = new TreeSet<String>();
		expectedShapes.add("SCHD PSB((PSB-NAME)) OPTION(NODHABEND)");
		expectedShapes.add("TERM");
		expectedShapes.add("CHKP ID(WK-CHKPT-ID)");
		expectedShapes.add("GN PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) INTO(PENDING-AUTH-SUMMARY)");
		expectedShapes.add("GNP PCB(PAUT-PCB-NUM) SEGMENT(PAUTDTL1) INTO(PENDING-AUTH-DETAILS)");
		expectedShapes.add(
				"GNP PCB(PAUT-PCB-NUM) SEGMENT(PAUTDTL1) INTO(PENDING-AUTH-DETAILS) WHERE(PAUT9CTS = PA-AUTHORIZATION-KEY)");
		expectedShapes.add(
				"GU PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) INTO(PENDING-AUTH-SUMMARY) WHERE(ACCNTID = PA-ACCT-ID)");
		expectedShapes.add("DLET PCB(PAUT-PCB-NUM) SEGMENT(PAUTDTL1) FROM(PENDING-AUTH-DETAILS)");
		expectedShapes.add("DLET PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) FROM(PENDING-AUTH-SUMMARY)");
		expectedShapes.add("REPL PCB(PAUT-PCB-NUM) SEGMENT(PAUTDTL1) FROM(PENDING-AUTH-DETAILS)");
		expectedShapes.add("REPL PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) FROM(PENDING-AUTH-SUMMARY)");
		expectedShapes.add("ISRT PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) FROM(PENDING-AUTH-SUMMARY)");
		expectedShapes.add(
				"ISRT PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) WHERE(ACCNTID = PA-ACCT-ID) SEGMENT(PAUTDTL1) FROM(PENDING-AUTH-DETAILS) OPTION(SEGLENGTH (LENGTH OF PENDING-AUTH-DETAILS))");

		for (final String shape : shapes) {
			assertTrue("unexpected shape " + shape, expectedShapes.contains(shape));
		}
	}
}
