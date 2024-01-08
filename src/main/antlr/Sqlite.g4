grammar Sqlite;

// EOF 사용 이유 : https://github.com/antlr/antlr4/blob/master/doc/parser-rules.md#start-rules-and-eof
parse
: sql_stmt_list* EOF
;

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

common_table_expression
: table_name ('(' column_name (',' column_name)* ')')?
  K_AS (K_NOT? K_MATERIALIZED)? '(' select_stmt ')'
;

select_core
: K_SELECT (K_DISTINCT | K_ALL)? result_column (',' result_column)*
  (K_FROM (table_or_subquery (',' table_or_subquery)* | join_clause))?
  (K_WHERE expr)?
  (K_GROUP K_BY expr (',' expr)* (K_HAVING expr)?)?  // group by 없이 having 절을 사용하는 경우는 제외 함
  (K_WINDOW window_name K_AS window_defn (',' window_name K_AS window_defn)*)?
| K_VALUES '(' expr (',' expr)* ')' (',' '(' expr (',' expr)* ')')*
;

// operator 우선순위 https://sqlite.org/lang_expr.html
// unary operator
// ||
// * / %
// + -
// & | << >>
// ESCAPE
// < > <= >=
// =   <>   !=   IS   IS NOT   IS DISTINCT FROM   IS NOT DISTINCT FROM
//     BETWEEN expr AND expr   IN   MATCH   LIKE   REGEXP   GLOB
//     ISNULL   NOTNULL   NOT NULL
// NOT expr
// AND
// OR
expr
: literal_value
| BIND_PARAMETER
| ((schema_name '.')? table_name '.')? column_name
| unary_operator expr
| expr '||' expr
| expr ('*' | '/' | '%') expr
| expr ('+' | '-') expr
| expr ('&' | '|' | '<<' | '>>') expr
| expr ('<' | '>' | '<=' | '>=') expr
| expr ('=' | '<>' | '!=') expr
| expr K_AND expr
| expr K_OR expr
| function_name '(' function_arguments ')' filter_clause? over_clause?
| '(' expr (',' expr)* ')'
| K_CAST '(' expr K_AS type_name ')'
| expr K_COLLATE collation_name
| expr K_NOT? (K_LIKE expr (K_ESCAPE expr)? | (K_GLOB | K_REGEXP | K_MATCH) expr)
| expr (K_ISNULL | K_NOTNULL | K_NOT K_NULL)
| expr K_IS K_NOT? (K_DISTINCT K_FROM)? expr
| expr K_NOT? K_BETWEEN expr K_AND expr
| expr K_NOT? K_IN ('(' (select_stmt | expr (',' expr)*)? ')'
                   | (schema_name '.')? table_name)  // schema_name.function(...) 은 사용 안함
| (K_NOT? K_EXISTS)? '(' select_stmt ')'
| K_CASE expr? (K_WHEN expr K_THEN expr)+ (K_ELSE expr)? K_END
| raise_function
;

filter_clause
: K_FILTER '(' K_WHERE expr ')'
;

over_clause
: K_OVER ( window_name
         | '(' base_window_name?
         (K_PARTITION K_BY expr (',' expr)*)?
         (K_ORDER K_BY ordering_term (',' ordering_term)*)?
         frame_spec? ')')
;

join_clause
: table_or_subquery (join_operator table_or_subquery join_constraint
                    (join_operator table_or_subquery join_constraint)*)?
;

ordering_term
: expr (K_COLLATE collation_name)? (K_ASC | K_DESC)? (K_NULLS (K_FIRST | K_LAST))?
;

result_column
: expr (K_AS? column_alias)?
| '*'
| table_name '.' '*'
;

table_or_subquery
: (schema_name '.')? table_name (K_AS? table_alias)?
  (K_INDEXED K_BY index_name | K_NOT K_INDEXED)?
| '(' select_stmt ')' (K_AS? table_alias)?
| '(' (table_or_subquery (',' table_or_subquery)* | join_clause) ')'
;

window_defn
: '(' base_window_name?
  (K_PARTITION K_BY expr (',' expr)*)?
  (K_ORDER K_BY ordering_term (',' ordering_term)*)?
  frame_spec?
  ')'
;

frame_spec
: (K_RANGE | K_ROWS | K_GROUPS)
  ( K_BETWEEN ( K_UNBOUNDED K_PRECEDING
              | expr K_PRECEDING
              | K_CURRENT K_ROW
              | expr K_FOLLOWING)
              K_AND
              ( expr K_PRECEDING
              | K_CURRENT K_ROW
              | expr K_FOLLOWING
              | K_UNBOUNDED K_FOLLOWING)
  | K_UNBOUNDED K_PRECEDING
  | expr K_PRECEDING
  | K_CURRENT K_ROW)
  ( K_EXCLUDE K_NO K_OTHERS
  | K_EXCLUDE K_CURRENT K_ROW
  | K_EXCLUDE K_GROUP
  | K_EXCLUDE K_TIES)?
;

function_arguments
: K_DISTINCT? expr (',' expr)* (K_ORDER K_BY ordering_term (',' ordering_term)*)?
| '*'?
;

raise_function
: K_RAISE '(' ( K_IGNORE
              | (K_ROLLBACK | K_ABORT | K_FAIL) ',' error_message)
 ')'
;


error_message
: STRING_LITERAL
;

signed_number
: ('+' | '-')? NUMERIC_LITERAL
;

join_constraint
: (K_ON expr
  | K_USING '(' column_name (',' column_name)* ')'
  )?
;

keyword  // https://sqlite.org/lang_keywords.html
: K_ABORT
//| K_ACTION
//| K_ADD
//| K_AFTER
| K_ALL
//| K_ALTER
//| K_ANALYZE
| K_AND
| K_AS
| K_ASC
//| K_ATTACH
//| K_AUTOINCREMENT
//| K_BEFORE
//| K_BEGIN
| K_BETWEEN
| K_BY
//| K_CASCADE
| K_CASE
| K_CAST
//| K_CHECK
| K_COLLATE
//| K_COLUMN
//| K_COMMIT
//| K_CONFLICT
//| K_CONSTRAINT
//| K_CREATE
| K_CROSS
| K_CURRENT_DATE
| K_CURRENT_TIME
| K_CURRENT_TIMESTAMP
//| K_DATABASE
//| K_DEFAULT
//| K_DEFERRABLE
//| K_DEFERRED
//| K_DELETE
| K_DESC
//| K_DETACH
| K_DISTINCT
//| K_DROP
//| K_EACH
| K_ELSE
| K_END
//| K_ENABLE
| K_ESCAPE
| K_EXCEPT
//| K_EXCLUSIVE
| K_EXISTS
| K_EXPLAIN
| K_FAIL
//| K_FOR
//| K_FOREIGN
| K_FROM
| K_FULL
| K_GLOB
| K_GROUP
| K_HAVING
//| K_IF
| K_IGNORE
//| K_IMMEDIATE
| K_IN
//| K_INDEX
| K_INDEXED
//| K_INITIALLY
| K_INNER
//| K_INSERT
//| K_INSTEAD
| K_INTERSECT
//| K_INTO
| K_IS
| K_ISNULL
| K_JOIN
//| K_KEY
| K_LEFT
| K_LIKE
| K_LIMIT
| K_MATCH
| K_NATURAL
| K_NO
| K_NOT
| K_NOTNULL
| K_NULL
//| K_OF
| K_OFFSET
| K_ON
| K_OR
| K_ORDER
| K_OUTER
| K_PLAN
//| K_PRAGMA
//| K_PRIMARY
| K_QUERY
| K_RAISE
| K_RECURSIVE
//| K_REFERENCES
| K_REGEXP
//| K_REINDEX
//| K_RELEASE
//| K_RENAME
//| K_REPLACE
//| K_RESTRICT
| K_RIGHT
| K_ROLLBACK
| K_ROW
//| K_SAVEPOINT
| K_SELECT
//| K_SET
//| K_TABLE
//| K_TEMP
//| K_TEMPORARY
| K_THEN
//| K_TO
//| K_TRANSACTION
//| K_TRIGGER
| K_UNION
//| K_UNIQUE
//| K_UPDATE
| K_USING
//| K_VACUUM
| K_VALUES
//| K_VIEW
//| K_VIRTUAL
| K_WHEN
| K_WHERE
| K_WITH
//| K_WITHOUT
//| K_NEXTVAL
;

// names
type_name
: name+ ('(' signed_number (',' signed_number)? ')')?
;

base_window_name: any_name;
collation_name: any_name;
column_alias: any_name;
column_name: any_name;
function_name: any_name;
index_name: any_name;
name: any_name;
schema_name: any_name;
table_alias: any_name;
table_name: any_name;
window_name: any_name;

any_name
: IDENTIFIER
| '`' keyword '`'
| STRING_LITERAL
| '(' any_name ')'
;

compound_operator
: K_UNION K_ALL?
| K_INTERSECT
| K_EXCEPT
;

join_operator
: ','
| (K_NATURAL? ((K_LEFT | K_RIGHT | K_FULL) K_OUTER? | K_INNER)?
  | K_CROSS?)
  K_JOIN
;

unary_operator
: '-'
| '+'
| '~'
| K_NOT
;

literal_value
: NUMERIC_LITERAL
| STRING_LITERAL
| BLOB_LITERAL
| K_NULL
| K_TRUE
| K_FALSE
| K_CURRENT_TIME
| K_CURRENT_DATE
| K_CURRENT_TIMESTAMP
;

// keywords
// A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
K_ABORT: A B O R T;
K_ALL: A L L;
K_AND: A N D;
K_AS: A S;
K_ASC: A S C;
K_BETWEEN: B E T W E E N;
K_BY: B Y;
K_CASE: C A S E;
K_CAST: C A S T;
K_COLLATE: C O L L A T E;
K_CROSS: C R O S S;
K_CURRENT: C U R R E N T;
K_CURRENT_TIME: C U R R E N T '_' T I M E;
K_CURRENT_DATE: C U R R E N T '_' D A T E;
K_CURRENT_TIMESTAMP: C U R R E N T '_' T I M E S T A M P;
K_DESC: D E S C;
K_DISTINCT: D I S T I N C T;
K_ELSE: E L S E;
K_END: E N D;
K_ESCAPE: E S C A P E;
K_EXCEPT: E X C E P T;
K_EXCLUDE: E X C L U D E;
K_EXISTS: E X I S T S;
K_EXPLAIN: E X P L A I N;
K_FAIL: F A I L;
K_FALSE: F A L S E;
K_FILTER: F I L T E R;
K_FIRST: F I R S T;
K_FOLLOWING: F O L L O W I N G;
K_FROM: F R O M;
K_FULL: F U L L;
K_GLOB: G L O B;
K_GROUP: G R O U P;
K_GROUPS: G R O U P S;
K_HAVING: H A V I N G;
K_IGNORE: I G N O R E;
K_IN: I N;
K_INDEXED: I N D E X E D;
K_INNER: I N N E R;
K_INTERSECT: I N T E R S E C T;
K_IS: I S;
K_ISNULL: I S N U L L;
K_JOIN: J O I N;
K_LAST: L A S T;
K_LEFT: L E F T;
K_LIKE: L I K E;
K_LIMIT: L I M I T;
K_MATCH: M A T C H;
K_MATERIALIZED: M A T E R I A L I Z E D;
K_NATURAL: N A T U R A L;
K_NO: N O;
K_NOT: N O T;
K_NOTNULL: N O T N U L L;
K_NULL: N U L L;
K_NULLS: N U L L S;
K_OFFSET: O F F S E T;
K_ON: O N;
K_OR: O R;
K_ORDER: O R D E R;
K_OTHERS: O T H E R S;
K_OUTER: O U T E R;
K_OVER: O V E R;
K_PARTITION: P A R T I T I O N;
K_PLAN: P L A N;
K_PRECEDING: P R E C E D I N G;
K_QUERY: Q U E R Y;
K_RAISE: R A I S E;
K_RANGE: R A N G E;
K_RECURSIVE: R E C U R S I V E;
K_REGEXP: R E G E X P;
K_RIGHT: R I G H T;
K_ROLLBACK: R O L L B A C K;
K_ROW: R O W;
K_ROWS: R O W S;
K_SELECT: S E L E C T;
K_THEN: T H E N;
K_TIES: T I E S;
K_TRUE: T R U E;
K_UNBOUNDED: U N B O U N D E D;
K_UNION: U N I O N;
K_USING: U S I N G;
K_VALUES: V A L U E S;
K_WHEN: W H E N;
K_WHERE: W H E R E;
K_WINDOW: W I N D O W;
K_WITH: W I T H;

IDENTIFIER  // TODO check: 더 필요한 정보 있으면 추가 해야 함
: '`' (~'`' | '``')* '`'
| [a-zA-Z_ㄱ-ㅎ가-힣] [a-zA-Z_ㄱ-ㅎ가-힣0-9]*
;

BIND_PARAMETER  // https://www.sqlite.org/c3ref/bind_blob.html
: '?' DIGIT*
| [:@$] IDENTIFIER
;

// literals https://sqlite.org/lang_expr.html
NUMERIC_LITERAL
: ( DIGIT+ ('.' DIGIT*)?
  | '.' DIGIT+ ) (E [+-]? DIGIT+)?
| '0x' HEXDIGIT+
;

STRING_LITERAL
: '\'' ( ~'\'' | '\'\'' )* '\''
;

BLOB_LITERAL
: X STRING_LITERAL
;

SINGLE_LINE_COMMENT
: '--' ~[\r\n]* -> channel(HIDDEN)
;

MULTILINE_COMMENT
: '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN)
;

SPACES
: [ \u000B\t\r\n] -> channel(HIDDEN)
;

UNEXPECTED_CHAR
: .
;

fragment DIGIT : [0-9];
fragment HEXDIGIT : [0-9A-F];

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
