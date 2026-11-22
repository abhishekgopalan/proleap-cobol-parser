package io.proleap.cobol.asg.procedure.execdli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.proleap.cobol.CobolTestBase;
import io.proleap.cobol.asg.metamodel.CompilationUnit;
import io.proleap.cobol.asg.metamodel.Program;
import io.proleap.cobol.asg.metamodel.ProgramUnit;
import io.proleap.cobol.asg.metamodel.procedure.ProcedureDivision;
import io.proleap.cobol.asg.metamodel.procedure.Statement;
import io.proleap.cobol.asg.metamodel.procedure.StatementTypeEnum;
import io.proleap.cobol.asg.metamodel.procedure.execdli.ExecDliStatement;
import io.proleap.cobol.asg.metamodel.procedure.ifstmt.IfStatement;
import io.proleap.cobol.asg.runner.impl.CobolParserRunnerImpl;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;

public class ExecDliStatementTest extends CobolTestBase {

	protected void assertExecDli(final String expectedDescription, final Statement statement) {
		assertNotNull(statement);
		assertTrue(statement instanceof ExecDliStatement);
		assertEquals(StatementTypeEnum.EXEC_DLI, statement.getStatementType());

		final ExecDliStatement execDliStatement = (ExecDliStatement) statement;
		assertNotNull(execDliStatement.getCtx());
		assertEquals(expectedDescription, ExecDliDescriber.describe(execDliStatement.getCtx()));
	}

	@Test
	public void test() throws Exception {
		final File inputFile = new File("src/test/resources/io/proleap/cobol/asg/procedure/execdli/ExecDli.cbl");
		final Program program = new CobolParserRunnerImpl().analyzeFile(inputFile, CobolSourceFormatEnum.TANDEM);

		final CompilationUnit compilationUnit = program.getCompilationUnit("ExecDli");
		final ProgramUnit programUnit = compilationUnit.getProgramUnit();
		final ProcedureDivision procedureDivision = programUnit.getProcedureDivision();
		assertEquals(0, procedureDivision.getParagraphs().size());
		assertEquals(13, procedureDivision.getStatements().size());

		// SCHD with PSB((variable)) and an option keyword
		assertExecDli("SCHD PSB((PSB-NAME)) OPTION(NODHABEND)", procedureDivision.getStatements().get(0));

		// GU with SEGMENT, INTO and WHERE
		assertExecDli(
				"GU PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) INTO(PENDING-AUTH-SUMMARY) WHERE(ACCNTID = PA-ACCT-ID)",
				procedureDivision.getStatements().get(1));

		// GN without WHERE
		assertExecDli("GN PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) INTO(PENDING-AUTH-SUMMARY)",
				procedureDivision.getStatements().get(2));

		// GNP nested in an IF statement
		{
			final IfStatement ifStatement = (IfStatement) procedureDivision.getStatements().get(3);
			assertEquals(StatementTypeEnum.IF, ifStatement.getStatementType());
			assertEquals(2, ifStatement.getThen().getStatements().size());

			assertExecDli(
					"GNP PCB(PAUT-PCB-NUM) SEGMENT(PAUTDTL1) INTO(PENDING-AUTH-DETAILS) WHERE(PAUT9CTS = PA-AUTHORIZATION-KEY)",
					ifStatement.getThen().getStatements().get(0));
			assertEquals(StatementTypeEnum.MOVE, ifStatement.getThen().getStatements().get(1).getStatementType());
		}

		// DLET and REPL with FROM
		assertExecDli("DLET PCB(PAUT-PCB-NUM) SEGMENT(PAUTDTL1) FROM(PENDING-AUTH-DETAILS)",
				procedureDivision.getStatements().get(4));
		assertExecDli("REPL PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) FROM(PENDING-AUTH-SUMMARY)",
				procedureDivision.getStatements().get(5));

		// ISRT with two SEGMENT clauses, WHERE belongs to the first one
		assertExecDli(
				"ISRT PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) WHERE(ACCNTID = PA-ACCT-ID) SEGMENT(PAUTDTL1) FROM(PENDING-AUTH-DETAILS) OPTION(SEGLENGTH (LENGTH OF PENDING-AUTH-DETAILS))",
				procedureDivision.getStatements().get(6));

		// CHKP with ID
		assertExecDli("CHKP ID(WK-CHKPT-ID)", procedureDivision.getStatements().get(7));

		// TERM without any clause
		assertExecDli("TERM", procedureDivision.getStatements().get(8));

		// get-hold variants
		assertExecDli("GHU PCB(1) SEGMENT(PAUTSUM0) INTO(PENDING-AUTH-SUMMARY)",
				procedureDivision.getStatements().get(9));
		assertExecDli("GHN PCB(PAUT-PCB-NUM) SEGMENT(PAUTSUM0) INTO(PENDING-AUTH-SUMMARY)",
				procedureDivision.getStatements().get(10));
		assertExecDli(
				"GHNP PCB(PAUT-PCB-NUM) SEGMENT(PAUTDTL1) INTO(PENDING-AUTH-DETAILS) WHERE(PAUT9CTS = PA-AUTHORIZATION-KEY)",
				procedureDivision.getStatements().get(11));

		assertEquals(StatementTypeEnum.STOP, procedureDivision.getStatements().get(12).getStatementType());
	}

	@Test
	public void testCtxIsTheRegisteredParseTreeNode() throws Exception {
		final File inputFile = new File("src/test/resources/io/proleap/cobol/asg/procedure/execdli/ExecDli.cbl");
		final Program program = new CobolParserRunnerImpl().analyzeFile(inputFile, CobolSourceFormatEnum.TANDEM);

		final ProcedureDivision procedureDivision = program.getCompilationUnit("ExecDli").getProgramUnit()
				.getProcedureDivision();
		final ExecDliStatement termStatement = (ExecDliStatement) procedureDivision.getStatements().get(8);

		assertEquals("TERM", termStatement.getCtx().dliFunction().getText());
		assertSame(termStatement, program.getASGElementRegistry().getASGElement(termStatement.getCtx()));
	}
}
