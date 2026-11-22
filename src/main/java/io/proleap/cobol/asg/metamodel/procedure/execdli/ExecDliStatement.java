/*
 * Copyright (C) 2017, Ulrich Wolffgang <ulrich.wolffgang@proleap.io>
 * All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package io.proleap.cobol.asg.metamodel.procedure.execdli;

import io.proleap.cobol.Cobol85Parser.ExecDliStatementContext;
import io.proleap.cobol.asg.metamodel.procedure.Statement;

/**
 * EXEC DLI statement (IMS DL/I command level). The structure of the statement,
 * i.e. function, PCB, PSB, segments with INTO, FROM and WHERE phrases, ID and
 * options, is available from the parse tree context.
 */
public interface ExecDliStatement extends Statement {

	@Override
	ExecDliStatementContext getCtx();

}
