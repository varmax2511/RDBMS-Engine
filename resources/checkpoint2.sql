   CREATE TABLE PART (
	P_PARTKEY		INTEGER,
	P_NAME			STRING,
	P_MFGR			STRING,
	P_BRAND			STRING,
	P_TYPE			STRING,
	P_SIZE			INTEGER,
	P_CONTAINER		STRING,
	P_RETAILPRICE	DOUBLE,
	P_COMMENT		STRING
);

CREATE TABLE SUPPLIER (
	S_SUPPKEY		INTEGER,
	S_NAME			STRING,
	S_ADDRESS		STRING,
	S_NATIONKEY		INTEGER NOT NULL, -- references N_NATIONKEY
	S_PHONE			STRING,
	S_ACCTBAL		DOUBLE,
	S_COMMENT		STRING
);

CREATE TABLE PARTSUPP (
	PS_PARTKEY		INTEGER NOT NULL, -- references P_PARTKEY
	PS_SUPPKEY		INTEGER NOT NULL, -- references S_SUPPKEY
	PS_AVAILQTY		INTEGER,
	PS_SUPPLYCOST	DOUBLE,
	PS_COMMENT		STRING
);


CREATE TABLE CUSTOMER (
	C_CUSTKEY		INTEGER,
	C_NAME			STRING,
	C_ADDRESS		STRING,
	C_NATIONKEY		INTEGER NOT NULL, -- references N_NATIONKEY
	C_PHONE			STRING,
	C_ACCTBAL		DOUBLE,
	C_MKTSEGMENT	STRING,
	C_COMMENT		STRING
);


CREATE TABLE ORDERS (
	O_ORDERKEY		INTEGER,
	O_CUSTKEY		INTEGER NOT NULL, -- references C_CUSTKEY
	O_ORDERSTATUS	STRING,
	O_TOTALPRICE	DOUBLE,
	O_ORDERDATE		DATE,
	O_ORDERPRIORITY	STRING,
	O_CLERK			STRING,
	O_SHIPPRIORITY	INTEGER,
	O_COMMENT		STRING
);


CREATE TABLE LINEITEM (
	L_ORDERKEY		INTEGER NOT NULL, -- references O_ORDERKEY
	L_PARTKEY		INTEGER NOT NULL, -- references P_PARTKEY (compound fk to PARTSUPP)
	L_SUPPKEY		INTEGER NOT NULL, -- references S_SUPPKEY (compound fk to PARTSUPP)
	L_LINENUMBER	INTEGER,
	L_QUANTITY		DOUBLE,
	L_EXTENDEDPRICE	DOUBLE,
	L_DISCOUNT		DOUBLE,
	L_TAX			DOUBLE,
	L_RETURNFLAG	STRING,
	L_LINESTATUS	STRING,
	L_SHIPDATE		DATE,
	L_COMMITDATE	DATE,
	L_RECEIPTDATE	DATE,
	L_SHIPINSTRUCT	STRING,
	L_SHIPMODE		STRING,
	L_COMMENT		STRING
);

CREATE TABLE NATION (
	N_NATIONKEY		INTEGER,
	N_NAME			STRING,
	N_REGIONKEY		INTEGER NOT NULL,  -- references R_REGIONKEY
	N_COMMENT		STRING
);

CREATE TABLE REGION (
	R_REGIONKEY	INTEGER,
	R_NAME		STRING,
	R_COMMENT	STRING
);

CREATE TABLE PLAYERS (
	ID STRING,
	FIRSTNAME STRING,
	LASTNAME STRING,
	FIRSTSEASON INTEGER,
	LASTSEASON INTEGER,
	WEIGHT INTEGER,
	BIRTHDATE DATE
);


/* Query 1 */
SELECT
    l_extendedprice, l_discount, l_quantity
FROM
    lineitem
WHERE
    l_shipdate >= '1994-01-01'
    AND l_shipdate < '1994-01-03'
   AND l_discount > 0.05
    AND l_discount < 0.07
    AND l_quantity > 49
ORDER BY l_extendedprice
LIMIT 20;

/* Query 2 Note: NOT WRONG */
SELECT C.*, N.n_name, R.r_name
FROM Customer C, Nation N, Region R
WHERE N.n_nationkey = C.c_nationkey
     AND N.n_nationkey < 3
     AND C.c_mktsegment = 'FURNITURE'
     AND C.c_acctbal > 9995;


/* Query 3 */
SELECT P1_FIRSTNAME, P1_LASTNAME, 
       P2_FIRSTNAME, P2_LASTNAME 
FROM (
  SELECT P1.FIRSTNAME AS P1_FIRSTNAME, P1.LASTNAME AS P1_LASTNAME,
         P2.FIRSTNAME AS P2_FIRSTNAME, P2.LASTNAME AS P2_LASTNAME,
         P1.FIRSTSEASON AS P1_FIRSTSEASON, P1.LASTSEASON AS P1_LASTSEASON,
         P2.FIRSTSEASON AS P2_FIRSTSEASON, P2.LASTSEASON AS P2_LASTSEASON
    FROM PLAYERS P1, PLAYERS P2 
  WHERE P1.ID<>P2.ID
  ) SUB_Q 
WHERE P1_FIRSTSEASON<P2_FIRSTSEASON 
  AND P1_LASTSEASON>P2_LASTSEASON;


/* Query 4 */
SELECT D.c_name, D.n_name
FROM
(SELECT C.*, N.n_name, R.r_name
FROM Customer C, Nation N, Region R
WHERE R.r_regionkey = N.n_regionkey
     AND N.n_nationkey = C.c_nationkey
     AND R.r_name = 'AMERICA') D,
Orders O, Lineitem L
WHERE O.o_orderkey = L.l_orderkey
	AND O.o_custkey = D.c_custkey
	AND L.l_shipdate < '1992-01-05'
ORDER BY D.c_name
LIMIT 5;


/* Query 5 */
SELECT
    L.l_extendedprice, L.l_discount
FROM
    lineitem L,
    part P
WHERE
P.p_partkey = L.l_partkey
AND L.l_discount > 0.09
AND    
   ((
        P.p_brand = 'Brand#12'
        AND P.p_container in ('SM CASE', 'SM BOX', 'SM PACK', 'SM PKG')
        AND L.l_quantity >= 1 AND L.l_quantity <= 11
        AND P.p_size >= 1 AND P.p_size <= 5
        AND L.l_shipmode in ('AIR', 'AIR REG')
        AND L.l_shipinstruct = 'DELIVER IN PERSON'
    )
    OR
    (
        P.p_brand = 'Brand#23'
        AND P.p_container in ('MED BAG', 'MED BOX', 'MED PKG', 'MED PACK')
        AND L.l_quantity >= 10 AND L.l_quantity <= 20
        AND P.p_size >= 1 AND P.p_size <= 10
        AND L.l_shipmode in ('AIR', 'AIR REG')
        AND L.l_shipinstruct = 'DELIVER IN PERSON'
    )
    OR
    (
        P.p_brand = 'Brand#34'
        AND P.p_container in ('LG CASE', 'LG BOX', 'LG PACK', 'LG PKG')
        AND L.l_quantity >= 20 AND L.l_quantity <= 30
        AND P.p_size >= 1 AND P.p_size <= 15
        AND L.l_shipmode in ('AIR', 'AIR REG')
        AND L.l_shipinstruct = 'DELIVER IN PERSON'
    ))
ORDER BY L.l_extendedprice DESC;
