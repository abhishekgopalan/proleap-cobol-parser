/*
 * Copyright (C) 2017, Ulrich Wolffgang <ulrich.wolffgang@proleap.io>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package io.proleap.cobol.preprocessor.sub.document.impl;

import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import io.proleap.cobol.Cobol85PreprocessorLexer;
import io.proleap.cobol.Cobol85PreprocessorParser;
import io.proleap.cobol.Cobol85PreprocessorParser.StartRuleContext;
import io.proleap.cobol.asg.params.CobolParserParams;
import io.proleap.cobol.asg.runner.ThrowingErrorListener;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;
import io.proleap.cobol.preprocessor.sub.document.CobolDocumentParser;
import io.proleap.cobol.preprocessor.sub.document.CobolDocumentParserListener;

/**
 * Preprocessor, which parses and processes COPY REPLACE and EXEC SQL
 * statements.
 */
public class CobolDocumentParserImpl implements CobolDocumentParser {

	protected final String[] triggers = new String[] { "cbl", "copy", "exec sql", "exec sqlims", "exec cics", "exec dli",
			"process", "replace", "eject", "skip1", "skip2", "skip3", "title" };

	/**
	 * EXEC DLI with any whitespace between the two words: several blanks, a tab, or
	 * a line break (optionally with comment lines in between), e.g. EXEC at the end
	 * of one line and DLI on the next. Only DLI is tolerant like this (SPARKLE fork
	 * addition); the literal triggers above, including "exec sql", "exec sqlims" and
	 * "exec cics", are unchanged and keep the behavior of ProLeap 2.4.0.
	 */
	protected final Pattern execDliTrigger = Pattern.compile("exec(?:\\s|\\*>[^\\r\\n]*+)+dli");

	protected boolean containsTrigger(final String code, final String[] triggers) {
		final String codeLowerCase = code.toLowerCase();
		boolean result = false;

		for (final String trigger : triggers) {
			final boolean containsTrigger = codeLowerCase.contains(trigger);

			if (containsTrigger) {
				result = true;
				break;
			}
		}

		if (!result) {
			result = execDliTrigger.matcher(codeLowerCase).find();
		}

		return result;
	}

	protected CobolDocumentParserListener createDocumentParserListener(final CobolSourceFormatEnum format,
			final CobolParserParams params, final CommonTokenStream tokens) {
		return new CobolDocumentParserListenerImpl(format, params, tokens);
	}

	@Override
	public String processLines(final String code, final CobolSourceFormatEnum format, final CobolParserParams params) {
		final boolean requiresProcessorExecution = containsTrigger(code, triggers);
		final String result;

		if (requiresProcessorExecution) {
			result = processWithParser(code, format, params);
		} else {
			result = code;
		}

		return result;
	}

	protected String processWithParser(final String code, final CobolSourceFormatEnum format,
			final CobolParserParams params) {
		// run the lexer
		final Cobol85PreprocessorLexer lexer = new Cobol85PreprocessorLexer(CharStreams.fromString(code));

		if (!params.getIgnoreSyntaxErrors()) {
			// register an error listener, so that preprocessing stops on errors
			lexer.removeErrorListeners();
			lexer.addErrorListener(new ThrowingErrorListener());
		}

		// get a list of matched tokens
		final CommonTokenStream tokens = new CommonTokenStream(lexer);

		// pass the tokens to the parser
		final Cobol85PreprocessorParser parser = new Cobol85PreprocessorParser(tokens);

		if (!params.getIgnoreSyntaxErrors()) {
			// register an error listener, so that preprocessing stops on errors
			parser.removeErrorListeners();
			parser.addErrorListener(new ThrowingErrorListener());
		}

		// specify our entry point
		final StartRuleContext startRule = parser.startRule();

		// analyze contained copy books
		final CobolDocumentParserListener listener = createDocumentParserListener(format, params, tokens);
		final ParseTreeWalker walker = new ParseTreeWalker();

		walker.walk(listener, startRule);

		final String result = listener.context().read();
		return result;
	}
}
