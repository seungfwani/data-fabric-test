grammar sqlite;

// statements
sql_stmt_list
: ';'* sql_stmt (';'+ sql_stmt)* ';'*
;

sql_stmt
: (K_EXPLAIN (K_QUERY K_PLAN)?)? ( select_stmt
//                                 | update_stmt
//                                 | update_stmt_limited )
)
;

select_stmt
: (K_WITH K_RECURSIVE? common_table_expression (',' common_table_expression)*)?
  select_core (compound_operator select_core)*
  (K_ORDER K_BY ordering_term (',' ordering_term)*)?
  (K_LIMIT expr ((K_OFFSET | ',') expr)?)?
;

//update_stmt:;
//update_stmt_limited:;


common_table_expression:;
select_core
: K_SELECT (K_DISTINCT | K_ALL)? result_column (',' result_column)*
  (K_FROM (table_or_subquery (',' table_or_subquery)* | join_clause))?
  (K_WHERE expr)?
  (K_GROUP K_BY expr (',' expr)* (K_HAVING expr)?)?  // group by 없이 having 절을 사용하는 경우는 제외 함
  (K_WINDOW window_name K_AS window_defn (',' window_name K_AS window_defn)*)?
| K_VALUES '(' expr (',' expr)* ')' (',' '(' expr (',' expr)* ')')*
;
compound_operator:;
result_column:;
table_or_subquery:;
join_clause:;
window_name:;
window_defn:;
ordering_term:;
expr:;


// keywords
// A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
K_ALL: A L L;
K_AS: A S;
K_BY: B Y;
K_DISTINCT: D I S T I N C T;
K_EXPLAIN: E X P L A I N;
K_FROM: F R O M;
K_GROUP: G R O U P;
K_HAVING: H A V I N G;
K_LIMIT: L I M I T;
K_OFFSET: O F F S E T;
K_ORDER: O R D E R;
K_PLAN: P L A N;
K_QUERY: Q U E R Y;
K_RECURSIVE: R E C U R S I V E;
K_SELECT: S E L E C T;
K_VALUES: V A L U E S;
K_WHERE: W H E R E;
K_WINDOW: W I N D O W;
K_WITH: W I T H;


fragment DIGIT : [0-9];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];
