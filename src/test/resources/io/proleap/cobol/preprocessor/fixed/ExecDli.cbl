000100 Identification Division.                                         12345678
000100 Program-ID. ExecDli.                                             12345678
000100 Procedure Division.                                              12345678
000100                                                                  12345678
000100   EXEC DLI GU USING PCB(PAUT-PCB-NUM)                            12345678
000100       SEGMENT (PAUTSUM0)                                         12345678
000100       INTO (PENDING-AUTH-SUMMARY)                                12345678
000100       WHERE (ACCNTID = PA-ACCT-ID)                               12345678
000100   END-EXEC.                                                      12345678
000100                                                                  12345678
000100   EXEC DLI SCHD PSB((PSB-NAME)) NODHABEND END-EXEC.              12345678
000100                                                                  12345678
000100   IF SOME-FLAG = 1                                               12345678
000100     EXEC DLI TERM                                                12345678
000100     END-EXEC                                                     12345678
000100   ELSE                                                           12345678
000100     PERFORM NOTHING-TO-DO.                                       12345678