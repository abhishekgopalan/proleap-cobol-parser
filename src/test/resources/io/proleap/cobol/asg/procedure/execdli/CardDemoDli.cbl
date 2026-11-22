      * Every EXEC DLI block of the CardDemo authorization module
      * (CBPAUP0C, COPAUA0C, COPAUS0C, COPAUS1C), copied verbatim,
      * including sequence numbers, from
      * https://github.com/aws-samples/aws-mainframe-modernization-carddemo
      * (Apache License 2.0), directory app/app-authorization-ims-db2-mq/cbl.
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CARDDEMODLI.
       PROCEDURE DIVISION.
       BLOCKS-CBPAUP0C.
            EXEC DLI GN USING PCB(PAUT-PCB-NUM)
                 SEGMENT (PAUTSUM0)
                 INTO (PENDING-AUTH-SUMMARY)
            END-EXEC
            EXEC DLI GNP USING PCB(PAUT-PCB-NUM)
                 SEGMENT (PAUTDTL1)
                 INTO (PENDING-AUTH-DETAILS)
            END-EXEC
            EXEC DLI DLET USING PCB(PAUT-PCB-NUM)
                 SEGMENT (PAUTDTL1)
                 FROM (PENDING-AUTH-DETAILS)
            END-EXEC
            EXEC DLI DLET USING PCB(PAUT-PCB-NUM)
                 SEGMENT (PAUTSUM0)
                 FROM (PENDING-AUTH-SUMMARY)
            END-EXEC
           EXEC DLI CHKP ID(WK-CHKPT-ID)
           END-EXEC
           .
       BLOCKS-COPAUA0C.
           EXEC DLI SCHD                                                        
                PSB((PSB-NAME))                                                 
                NODHABEND                                                       
           END-EXEC                                                             
              EXEC DLI TERM                                                     
              END-EXEC                                                          
              EXEC DLI SCHD                                                     
                   PSB((PSB-NAME))                                              
                   NODHABEND                                                    
              END-EXEC                                                          
           EXEC DLI GU USING PCB(PAUT-PCB-NUM)                                  
               SEGMENT (PAUTSUM0)                                               
               INTO (PENDING-AUTH-SUMMARY)                                      
               WHERE (ACCNTID = PA-ACCT-ID)                                     
           END-EXEC                                                             
              EXEC DLI REPL USING PCB(PAUT-PCB-NUM)                             
                   SEGMENT (PAUTSUM0)                                           
                   FROM (PENDING-AUTH-SUMMARY)                                  
              END-EXEC                                                          
              EXEC DLI ISRT USING PCB(PAUT-PCB-NUM)                             
                   SEGMENT (PAUTSUM0)                                           
                   FROM (PENDING-AUTH-SUMMARY)                                  
              END-EXEC                                                          
           EXEC DLI ISRT USING PCB(PAUT-PCB-NUM)
                SEGMENT (PAUTSUM0)
                WHERE (ACCNTID = PA-ACCT-ID)
                SEGMENT (PAUTDTL1)
                FROM (PENDING-AUTH-DETAILS)
                SEGLENGTH (LENGTH OF PENDING-AUTH-DETAILS)
           END-EXEC
              EXEC DLI TERM END-EXEC                                            
           .
       BLOCKS-COPAUS0C.
           EXEC DLI GNP USING PCB(PAUT-PCB-NUM)
               SEGMENT (PAUTDTL1)
               INTO (PENDING-AUTH-DETAILS)
           END-EXEC
           EXEC DLI GNP USING PCB(PAUT-PCB-NUM)
               SEGMENT (PAUTDTL1)
               INTO (PENDING-AUTH-DETAILS)
               WHERE (PAUT9CTS = PA-AUTHORIZATION-KEY)
           END-EXEC
           EXEC DLI GU USING PCB(PAUT-PCB-NUM)
               SEGMENT (PAUTSUM0)
               INTO (PENDING-AUTH-SUMMARY)
               WHERE (ACCNTID = PA-ACCT-ID)
           END-EXEC
           EXEC DLI SCHD
                PSB((PSB-NAME))
                NODHABEND
           END-EXEC
              EXEC DLI TERM
              END-EXEC
              EXEC DLI SCHD
                   PSB((PSB-NAME))
                   NODHABEND
              END-EXEC
           .
       BLOCKS-COPAUS1C.
           EXEC DLI GU USING PCB(PAUT-PCB-NUM)
               SEGMENT (PAUTSUM0)
               INTO (PENDING-AUTH-SUMMARY)
               WHERE (ACCNTID = PA-ACCT-ID)
           END-EXEC
              EXEC DLI GNP USING PCB(PAUT-PCB-NUM)
                  SEGMENT (PAUTDTL1)
                  INTO (PENDING-AUTH-DETAILS)
                  WHERE (PAUT9CTS = PA-AUTHORIZATION-KEY)
              END-EXEC
           EXEC DLI GNP USING PCB(PAUT-PCB-NUM)
               SEGMENT (PAUTDTL1)
               INTO (PENDING-AUTH-DETAILS)
           END-EXEC
           EXEC DLI REPL USING PCB(PAUT-PCB-NUM)
                SEGMENT (PAUTDTL1)
                FROM (PENDING-AUTH-DETAILS)
           END-EXEC
           EXEC DLI SCHD
                PSB((PSB-NAME))
                NODHABEND
           END-EXEC
              EXEC DLI TERM
              END-EXEC
              EXEC DLI SCHD
                   PSB((PSB-NAME))
                   NODHABEND
              END-EXEC
           .
           STOP RUN.
