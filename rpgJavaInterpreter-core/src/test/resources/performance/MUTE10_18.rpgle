     V*=====================================================================
     V* MODIFICHE Ril.  T Au Descrizione
     V* gg/mm/aa  nn.mm i xx Breve descrizione
     V*=====================================================================
     V* 09/01/20  001425  BMA   Creato
     V*=====================================================================
     D* OBIETTIVO
     D*  Programma finalizzato ai test performance su Statement vari
     V*---------------------------------------------------------------------
      * Considerare i seguenti codici operativi
      *+----------+--+---------!--+
      *!RPGLE     !ST!BUILT-IN !ST!
      *+-------------+ --------!--+
      *+----------+--+---------+--+
     D $CICL           S              7  0 INZ(100000)
     D TXT             S            100    DIM(10) PERRCD(1) CTDATA             _NOTXT
     D ST1             S             50
     D ST2             S            100
     D $TIMST          S               Z   INZ
     D $TIMEN          S               Z   INZ
     D $TIMMS          S             10  0
     D$MSG             S             52
      *
      * Main
     C                   EXSR      EXECUTE
      *
    MU* Type="NOXMI"
    MU* TIMEOUT(15)
     C                   SETON                                        LR
      *---------------------------------------------------------------------
    RD* Routine test su statement diversi
      *---------------------------------------------------------------------
     C     EXECUTE       BEGSR
      *
      * Start time
     C                   TIME                    $TIMST
      * EVAL (*ALL'')
     C                   DO        $CICL
     C                   EVAL      ST1=*ALL'A'
     C                   EVAL      ST2=*ALL'B'
     C                   ENDDO
      *
      * End time
     C                   TIME                    $TIMEN
      * Elapsed time
     C     $TIMEN        SUBDUR    $TIMST        $TIMMS:*MS
     C                   EVAL      $TIMMS=$TIMMS/1000
      * Display message
     C                   EVAL      $MSG=%trim(TXT(1))+' '+
     C                             %TRIM(%EDITC($TIMMS:'Q'))+'ms'
     C     $MSG          DSPLY     £PDSSU
      *
     C                   ENDSR
** TXT
Time spent
