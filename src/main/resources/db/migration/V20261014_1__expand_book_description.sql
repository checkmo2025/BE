-- ISBN/참조/기존 소개를 보존한다. 이미 LONGTEXT라면 재변경하지 않는다.
SET @book_description_ddl = (
    SELECT IF(DATA_TYPE IN ('char', 'varchar', 'tinytext', 'text', 'mediumtext'),
              'ALTER TABLE book MODIFY COLUMN description LONGTEXT NULL',
              'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'book' AND COLUMN_NAME = 'description'
);
PREPARE book_description_statement FROM @book_description_ddl;
EXECUTE book_description_statement;
DEALLOCATE PREPARE book_description_statement;
