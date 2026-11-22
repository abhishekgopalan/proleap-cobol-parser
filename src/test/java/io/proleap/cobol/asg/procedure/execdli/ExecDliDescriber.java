package io.proleap.cobol.asg.procedure.execdli;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import io.proleap.cobol.Cobol85Parser.DliClauseContext;
import io.proleap.cobol.Cobol85Parser.DliOptionContext;
import io.proleap.cobol.Cobol85Parser.DliQualificationContext;
import io.proleap.cobol.Cobol85Parser.DliSegmentClauseContext;
import io.proleap.cobol.Cobol85Parser.DliSegmentPhraseContext;
import io.proleap.cobol.Cobol85Parser.ExecDliStatementContext;

/**
 * Renders the structure of an EXEC DLI parse tree as one line, e.g.
 * <code>GU PCB(P) SEGMENT(S) INTO(I) WHERE(F = V)</code>, so tests can assert
 * that each part (function, PCB, PSB, segment phrases, ID, options) came out of
 * its own grammar rule and not from the raw text.
 */
public class ExecDliDescriber {

	/**
	 * Source text of a context including whitespace between its tokens.
	 */
	protected static String text(final ParserRuleContext ctx) {
		return ctx.getStart().getInputStream()
				.getText(Interval.of(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex()));
	}

	protected static String describeOption(final DliOptionContext option) {
		final String name = option.dliOptionName().getText();

		if (option.dliOptionArguments() == null) {
			return "OPTION(" + name + ")";
		} else {
			return "OPTION(" + name + " " + text(option.dliOptionArguments()) + ")";
		}
	}

	protected static String describeQualification(final DliQualificationContext qualification) {
		return qualification.dliFieldName().getText() + " " + qualification.relationalOperator().getText() + " "
				+ text(qualification.dliOperand());
	}

	protected static String describeSegment(final DliSegmentClauseContext segment) {
		final List<String> parts = new ArrayList<String>();
		parts.add("SEGMENT(" + text(segment.dliOperand()) + ")");

		for (final DliSegmentPhraseContext phrase : segment.dliSegmentPhrase()) {
			if (phrase.dliIntoPhrase() != null) {
				parts.add("INTO(" + text(phrase.dliIntoPhrase().dliOperand()) + ")");
			} else if (phrase.dliFromPhrase() != null) {
				parts.add("FROM(" + text(phrase.dliFromPhrase().dliOperand()) + ")");
			} else if (phrase.dliWherePhrase() != null) {
				final List<String> qualifications = new ArrayList<String>();

				for (final DliQualificationContext qualification : phrase.dliWherePhrase().dliCondition()
						.dliQualification()) {
					qualifications.add(describeQualification(qualification));
				}

				parts.add("WHERE(" + String.join(" ; ", qualifications) + ")");
			} else {
				parts.add(describeOption(phrase.dliOption()));
			}
		}

		return String.join(" ", parts);
	}

	public static String describe(final ExecDliStatementContext ctx) {
		final List<String> parts = new ArrayList<String>();
		parts.add(ctx.dliFunction().getText());

		for (final DliClauseContext clause : ctx.dliClause()) {
			if (clause.dliUsingPcbClause() != null) {
				parts.add("PCB(" + text(clause.dliUsingPcbClause().dliOperand()) + ")");
			} else if (clause.dliPsbClause() != null) {
				if (clause.dliPsbClause().dliParenthesizedOperand() != null) {
					parts.add("PSB((" + text(clause.dliPsbClause().dliParenthesizedOperand().dliOperand()) + "))");
				} else {
					parts.add("PSB(" + text(clause.dliPsbClause().dliOperand()) + ")");
				}
			} else if (clause.dliIdClause() != null) {
				parts.add("ID(" + text(clause.dliIdClause().dliOperand()) + ")");
			} else if (clause.dliSegmentClause() != null) {
				parts.add(describeSegment(clause.dliSegmentClause()));
			} else {
				parts.add(describeOption(clause.dliOption()));
			}
		}

		return String.join(" ", parts);
	}
}
