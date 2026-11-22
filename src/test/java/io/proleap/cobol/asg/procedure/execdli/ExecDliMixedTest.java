package io.proleap.cobol.asg.procedure.execdli;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import io.proleap.cobol.CobolTestBase;
import io.proleap.cobol.asg.metamodel.Program;
import io.proleap.cobol.asg.metamodel.procedure.ProcedureDivision;
import io.proleap.cobol.asg.metamodel.procedure.StatementTypeEnum;
import io.proleap.cobol.asg.metamodel.procedure.execdli.ExecDliStatement;
import io.proleap.cobol.asg.runner.impl.CobolParserRunnerImpl;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;

/**
 * EXEC DLI next to EXEC CICS, EXEC SQL and EXEC SQLIMS, in lower case, on a
 * single line and with operand shapes beyond the CardDemo corpus.
 */
public class ExecDliMixedTest extends CobolTestBase {

	@Test
	public void test() throws Exception {
		final File inputFile = new File("src/test/resources/io/proleap/cobol/asg/procedure/execdli/ExecDliMixed.cbl");
		final Program program = new CobolParserRunnerImpl().analyzeFile(inputFile, CobolSourceFormatEnum.TANDEM);

		final ProcedureDivision procedureDivision = program.getCompilationUnit("ExecDliMixed").getProgramUnit()
				.getProcedureDivision();
		assertEquals(7, procedureDivision.getStatements().size());

		assertEquals(StatementTypeEnum.EXEC_CICS, procedureDivision.getStatements().get(0).getStatementType());

		assertEquals(StatementTypeEnum.EXEC_DLI, procedureDivision.getStatements().get(1).getStatementType());
		assertEquals(
				"gu PCB(pcb-num) SEGMENT(some-seg) INTO(some-area) WHERE(some-field >= some-value ; other-field <> 'X')",
				ExecDliDescriber.describe(((ExecDliStatement) procedureDivision.getStatements().get(1)).getCtx()));

		assertEquals(StatementTypeEnum.EXEC_SQL, procedureDivision.getStatements().get(2).getStatementType());

		assertEquals(StatementTypeEnum.EXEC_DLI, procedureDivision.getStatements().get(3).getStatementType());
		assertEquals(
				"ISRT PCB(PCB-NUM) SEGMENT(PARENT-SEG) WHERE(KEY-FIELD = PARENT-KEY) SEGMENT(CHILD-SEG) FROM(CHILD-AREA)",
				ExecDliDescriber.describe(((ExecDliStatement) procedureDivision.getStatements().get(3)).getCtx()));

		// EXEC DLI and the function on different lines, PSB with a literal name
		assertEquals(StatementTypeEnum.EXEC_DLI, procedureDivision.getStatements().get(4).getStatementType());
		assertEquals("SCHD PSB(MYPSB)",
				ExecDliDescriber.describe(((ExecDliStatement) procedureDivision.getStatements().get(4)).getCtx()));

		assertEquals(StatementTypeEnum.EXEC_SQLIMS, procedureDivision.getStatements().get(5).getStatementType());

		// subscripted PCB, qualified segment name, option keyword without argument
		assertEquals(StatementTypeEnum.EXEC_DLI, procedureDivision.getStatements().get(6).getStatementType());
		assertEquals("GN PCB(SOME-TABLE(IDX)) SEGMENT(SOME-SEG OF SOME-GROUP) INTO(SOME-AREA) OPTION(FIRST)",
				ExecDliDescriber.describe(((ExecDliStatement) procedureDivision.getStatements().get(6)).getCtx()));
	}
}
