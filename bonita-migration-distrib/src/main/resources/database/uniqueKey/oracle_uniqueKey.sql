SELECT
    c.TABLE_NAME,
    c.CONSTRAINT_NAME
FROM
    user_CONSTRAINTS C
WHERE
    LOWER(c.TABLE_NAME) = LOWER( ? )
    AND LOWER( c.CONSTRAINT_NAME ) = LOWER( ? )
    AND c.CONSTRAINT_TYPE = 'U'
