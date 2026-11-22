 Identification Division.
 Program-ID. ExecDliMixed.
 Procedure Division.

  EXEC CICS
    SEND MAP('SOMEMAP')
  END-EXEC.

  exec dli gu using pcb(pcb-num) segment(some-seg) into(some-area)
       where(some-field >= some-value and other-field <> 'X') end-exec.

  EXEC SQL
    OPEN SOMECUR
  END-EXEC.

  EXEC DLI ISRT USING PCB(PCB-NUM) SEGMENT(PARENT-SEG) WHERE(KEY-FIELD = PARENT-KEY) SEGMENT(CHILD-SEG) FROM(CHILD-AREA) END-EXEC.

  EXEC DLI
    SCHD PSB(MYPSB)
  END-EXEC.

  EXEC SQLIMS
    OPEN SOMECUR
  END-EXEC.

  EXEC DLI GN USING PCB(SOME-TABLE(IDX)) SEGMENT(SOME-SEG OF SOME-GROUP) INTO(SOME-AREA) FIRST END-EXEC.
