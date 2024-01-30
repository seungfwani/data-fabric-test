# Fabric tester

## parser

- 주요 기능
  - string -> tree 로 파싱
  - 사용을 원하는 model 리스트 추출
    - model 사용이 가능한지 인증/인가
  - 모델을 해당하는 spark hive metadata 로 변경
  - 변경된 sql 생성/반환
- 특별 규칙
  - 실제 sqlite 쿼리보다 축소된 형태의 동작
  - tree 의 값을 수정 -> sql string 변환 API
  - 사용 가능한 function 정의

- spark
  - `create temporary table using jdbc` 형태로 테이블 만들고, `persist()` 같은 함수로 캐싱하면 될 것
  - spark sql 을 temporary table 을 사용하도록 변경하면 sql 로 수행 가능 할 것
  - 참고 : [Spark SQL: Relational Data Processing in Spark](https://citeseerx.ist.psu.edu/document?repid=rep1&type=pdf&doi=080ed793c12d97436ae29851b5e34c54c07e3816)
  - 