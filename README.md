ProLeap ANTLR4-based parser for COBOL
=====================================

> **Modified fork notice.** This repository is a **modified fork** of
> [uwol/proleap-cobol-parser](https://github.com/uwol/proleap-cobol-parser)
> (Copyright (c) 2017 Ulrich Wolffgang, MIT License, see [LICENSE](LICENSE)). It was forked from the
> upstream release tag **`v2.4.0`**, commit **`b045d8093aab535a21deee4b4da4f3133d30d028`**, and adds
> native **EXEC DLI** (IMS DL/I command level) support, see [EXEC DLI support](#exec-dli-support-fork-change)
> below. Nothing else is changed: the upstream `LICENSE`, the copyright notice and the MIT headers of the
> grammar files are kept as they are, and COBOL input without EXEC DLI is meant to parse to the same
> parse trees as stock 2.4.0. The fork's build and dependencies (`pom.xml`) are unchanged from 2.4.0.

This is a **COBOL parser** based on an [ANTLR4 grammar](src/main/antlr4/io/proleap/cobol/Cobol85.g4), 
which generates an **Abstract Syntax Tree** (AST) and **Abstract Semantic Graph** (ASG) for COBOL code.
The AST represents plain COBOL source code in a syntax tree structure.
The ASG is generated from the AST by **semantic analysis** and provides data and control
flow information (e. g. variable access). EXEC SQL, EXEC SQLIMS and EXEC CICS
statements are extracted as texts; EXEC DLI statements (this fork) are parsed into a structure.

The parser is developed test-driven, passes the **NIST test suite** and has successfully been **applied to numerous COBOL files** from banking and insurance.

💫 **Star** if you like our work.

[![Build](https://img.shields.io/travis/uwol/cobol85parser.svg)](https://travis-ci.org/uwol/cobol85parser)
[![Coverage](https://coveralls.io/repos/github/uwol/cobol85parser/badge.svg?branch=master)](https://coveralls.io/github/uwol/cobol85parser?branch=master)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![ProLeap on Twitter](https://img.shields.io/twitter/follow/proleap_io.svg?style=social&label=Follow)](https://twitter.com/proleap_io)


Example
-------

### Input: COBOL code

```
 Identification Division.
 Program-ID.
  HELLOWORLD.
 Procedure Division.
  Display "Hello world".
  STOP RUN.
```


### Output: Abstract Syntax Tree (AST)

```
(startRule
	(compilationUnit
		(programUnit
			(identificationDivision Identification Division .
				(programIdParagraph Program-ID .
					(programName
						(cobolWord HELLOWORLD)) .))
			(procedureDivision Procedure Division .
				(procedureDivisionBody
					(paragraphs
						(sentence
							(statement
								(displayStatement Display
									(displayOperand
										(literal "Hello world")))) .)
						(sentence
							(statement
								(stopStatement STOP RUN))) .)))))) <EOF>)
```


Getting started
---------------

To include the parser in your Maven project edit your `pom.xml` file as follows

```
<repositories>
	<repository>
		<id>maven.proleap.io</id>
		<url>http://maven.proleap.io</url>
	</repository>
</repositories>
```

```
<dependency>
	<groupId>io.github.uwol</groupId>
	<artifactId>cobol85parser</artifactId>
	<version>2.3.0</version>
</dependency>
```

Use the following code as a starting point for developing own code.

### Simple: Generate an Abstract Semantic Graph (ASG) from COBOL code

```java
// generate ASG from plain COBOL code
java.io.File inputFile = new java.io.File("src/test/resources/io/proleap/cobol/asg/HelloWorld.cbl");
io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum format = io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum.TANDEM;
io.proleap.cobol.asg.metamodel.Program program = new io.proleap.cobol.asg.runner.impl.CobolParserRunnerImpl().analyzeFile(inputFile, format);

// navigate on ASG
io.proleap.cobol.asg.metamodel.CompilationUnit compilationUnit = program.getCompilationUnit("HelloWorld");
io.proleap.cobol.asg.metamodel.ProgramUnit programUnit = compilationUnit.getProgramUnit();
io.proleap.cobol.asg.metamodel.data.DataDivision dataDivision = programUnit.getDataDivision();
io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntry dataDescriptionEntry = dataDivision.getWorkingStorageSection().getDataDescriptionEntry("ITEMS");
Integer levelNumber = dataDescriptionEntry.getLevelNumber();
```

### Complex: Generate an Abstract Semantic Graph (ASG) and traverse the Abstract Syntax Tree (AST)

```java
// generate ASG from plain COBOL code
java.io.File inputFile = new java.io.File("src/test/resources/io/proleap/cobol/asg/HelloWorld.cbl");
io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum format = io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum.TANDEM;
io.proleap.cobol.asg.metamodel.Program program = new io.proleap.cobol.asg.runner.impl.CobolParserRunnerImpl().analyzeFile(inputFile, format);

// traverse the AST
io.proleap.cobol.Cobol85BaseVisitor<Boolean> visitor = new io.proleap.cobol.Cobol85BaseVisitor<Boolean>() {
  @Override
  public Boolean visitDataDescriptionEntryFormat1(final io.proleap.cobol.Cobol85Parser.DataDescriptionEntryFormat1Context ctx) {
    io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntry entry = (io.proleap.cobol.asg.metamodel.data.datadescription.DataDescriptionEntry) program.getASGElementRegistry().getASGElement(ctx);
    String name = entry.getName();

    return visitChildren(ctx);
  }
};

for (final io.proleap.cobol.asg.metamodel.CompilationUnit compilationUnit : program.getCompilationUnits()) {
  visitor.visit(compilationUnit.getCtx());
}
```


Where to look next
------------------

- [ANTLR4 COBOL grammar](src/main/antlr4/io/proleap/cobol/Cobol85.g4)
- [ANTLR4 COBOL preprocessor grammar](src/main/antlr4/io/proleap/cobol/Cobol85Preprocessor.g4)
- [Unit test code examples](src/test/java/io/proleap/cobol/asg/data/workingstorage)


Features
--------

* `EXEC SQL` statements, `EXEC SQLIMS` statements and `EXEC CICS` statements are extracted by the preprocessor and provided as texts in the ASG.
* Passes the [NIST test suite](http://www.itl.nist.gov/div897/ctg/cobol_form.htm).
* Rigorous test-driven development.
* To be used in conjunction with the provided preprocessor, which executes `COPY`, `REPLACE`, `CBL` and `PROCESS` statements.


Build process
-------------

The build process is based on Maven (version 3 or higher). Building requires a JDK 8 and generates a Maven JAR, which can be used in other Maven projects as a dependency.

* Clone or download the repository.
* In [Eclipse](https://eclipse.org) import the directory as a an `existing Maven project`.
* To build, run:

```
$ mvn clean package
```

* The test suite executes AST and ASG tests against COBOL test code and NIST test files. NIST test files come from [Koopa repo](https://github.com/goblindegook/Koopa/tree/master/testsuite/cobol85). Unit tests and parse tree files were generated by class `io.proleap.cobol.TestGenerator` from COBOL test files. The generator derives the COBOL line format from the containing folder name.
* You should see output like this:

```
[INFO] Scanning for projects...
...
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running io.proleap.cobol.ast.fixed.FixedTest
Preprocessing file Fixed.cbl.
Parsing file Fixed.cbl.
Comparing parse tree with file Fixed.cbl.tree.
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.202 sec
Running io.proleap.cobol.ast.fixed.QuotesInCommentEntryTest
...
Results :

Tests run: 680, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

* To install the JAR in your local Maven repository:

```
$ mvn clean install
```

* To only run the tests:

```
$ mvn clean test
```


Release process
---------------

* Releases and snapshots are published in the MAVEN repository `maven.proleap.io`.
* Milestones of the grammar are published in the [ANTLR grammars repo](https://github.com/antlr/grammars-v4).


EXEC DLI support (fork change)
------------------------------

Stock ProLeap 2.4.0 has no `EXEC DLI` support: the preprocessor and the grammar stop at the first block.
This fork parses every `EXEC DLI ... END-EXEC` block natively. The change is limited to
`Cobol85.g4`, `Cobol85Preprocessor.g4`, the `CobolDocumentParserListenerImpl` preprocessor hook, the
new ASG classes under `io.proleap.cobol.asg.metamodel.procedure.execdli`, `StatementTypeEnum.EXEC_DLI`,
the `Scope` and visitor registration, and tests.

**Grammar design: option (a), tagged first line plus native parsing.** The stock EXEC SQLIMS rule is
`execSqlImsStatement : EXECSQLIMSLINE+`, a run of opaque `*>EXECSQLIMS` lines without any structure. For DLI
the structure is wanted, so that shape is not copied:

* `Cobol85Preprocessor.g4` gets `execDliStatement : EXEC DLI charData? END_EXEC DOT?`. The preprocessor
  replaces the words `EXEC DLI` by the tag `*>EXECDLI` and passes the rest of the block through as
  ordinary text, one output line per source line, so line numbers are unchanged. Only the first line is
  tagged, the other lines are not.
* `Cobol85.g4` gets the token `EXECDLITAG : '*>EXECDLI'` and `execDliStatement : EXECDLITAG dliFunction
  dliClause* END_EXEC`, a new alternative of `statement` next to `execSqlImsStatement`. The sub-rules are
  ANTLR rules, no text splitting: `dliFunction` (GU, GN, GNP, GHU, GHN, GHNP, DLET, REPL, ISRT, SCHD, TERM,
  CHKP), `dliUsingPcbClause` (`USING PCB(x)`), `dliPsbClause` (`PSB(x)` and `PSB((x))`), `dliIdClause`
  (`ID(x)`), `dliSegmentClause` (`SEGMENT(x)` with `dliIntoPhrase`, `dliFromPhrase`, `dliWherePhrase` =
  `WHERE(field operator operand [AND|OR ...])` and further options, so `WHERE` belongs to the `SEGMENT` it
  follows, and ISRT may carry several `SEGMENT` clauses) and the lenient `dliOption` (`NODHABEND`,
  `SEGLENGTH(...)`, `FIRST`, any keyword with optional operands). Operands are `literal | identifier`.
  Text the rules cannot parse (for example an unknown function) is a syntax error, never skipped.
* Why (a) and not (b), a second rule set over tagged blocks: one parse tree with real token positions
  instead of a second parser run per block, no duplicated grammar, and the EXEC SQL/CICS/SQLIMS mechanism
  stays untouched.
* The new words (`GU`, `GN`, `GNP`, `GHU`, `GHN`, `GHNP`, `DLET`, `REPL`, `ISRT`, `SCHD`, `TERM`, `CHKP`, `PCB`,
  `PSB`, `WHERE`) are lexer tokens, so they are also added to `cobolWord`: programs that use them as
  ordinary data or paragraph names parse to the same trees as before (checked against stock 2.4.0, see
  `DliKeywordNamesTest`). `END-EXEC` is a new token `END_EXEC`; it is reserved and appears outside of
  tagged lines only in EXEC DLI blocks.

**ASG.** `ExecDliStatement` (+ `ExecDliStatementImpl`), statement type `StatementTypeEnum.EXEC_DLI`,
created by `Scope.addExecDliStatement` from `CobolProcedureStatementVisitorImpl.visitExecDliStatement`,
mirroring `ExecSqlImsStatement`. It exposes the parse tree via `getCtx()`, an
`Cobol85Parser.ExecDliStatementContext`, e.g. `ctx.dliFunction()`, `ctx.dliClause()`.

**Known limits.** The preprocessor trigger is the lower-cased text `exec dli` with a single blank, like the
other EXEC triggers. EXEC DLI is only recognized as a procedure division statement. Functions beyond the
twelve listed (for example ROLB, ROLS, XRST) and the EXEC DLI forms of other DL/I dialects are not in the
grammar yet and fail as syntax errors. PCB ordinals are not resolved here, that belongs to the consumer.

**Third-party grammar.** The DL/I rules were written for this fork. The public grammar
`cschneid-the-elder/mapa` (MIT, Copyright Craig Schneiderwent) was not read and no text was copied
from it, so no additional third-party notice applies.

License
-------

Licensed under the MIT License. See LICENSE for details.
