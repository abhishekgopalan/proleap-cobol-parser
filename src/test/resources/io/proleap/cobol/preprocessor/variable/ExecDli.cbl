       Identification Division.
       Program-ID. ExecDli.
       Procedure Division.

         EXEC DLI GU USING PCB(PAUT-PCB-NUM)
             SEGMENT (PAUTSUM0)
             INTO (PENDING-AUTH-SUMMARY)
             WHERE (ACCNTID = PA-ACCT-ID)
         END-EXEC.

         EXEC DLI SCHD PSB((PSB-NAME)) NODHABEND END-EXEC.

         IF SOME-FLAG = 1
           EXEC DLI TERM
           END-EXEC
         ELSE
           PERFORM NOTHING-TO-DO.