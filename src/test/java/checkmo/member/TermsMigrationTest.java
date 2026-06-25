package checkmo.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.assertj.core.api.SoftAssertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class TermsMigrationTest {

    @Test
    void 약관_버전_스키마와_v1_활성_약관을_생성한다() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(migratedDataSource());

        List<Map<String, Object>> activeTerms = jdbcTemplate.queryForList(
                """
                        select terms_type, title, term_url, version, is_active, is_required
                        from terms
                        where version = 1 and is_active = true
                        order by terms_type
                        """
        );

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(columnsOf(jdbcTemplate, "terms"))
                    .containsExactlyInAnyOrder(
                            "id",
                            "terms_type",
                            "title",
                            "term_url",
                            "version",
                            "is_active",
                            "is_required",
                            "created_at",
                            "updated_at"
                    );
            softly.assertThat(columnsOf(jdbcTemplate, "member_terms"))
                    .containsExactlyInAnyOrder(
                            "id",
                            "member_id",
                            "terms_id",
                            "is_agreed",
                            "created_at",
                            "updated_at"
                    );
            softly.assertThat(activeTerms)
                    .extracting(row -> row.get("TERMS_TYPE"))
                    .containsExactly("MARKETING", "PRIVACY_COLLECTION", "SERVICE_TERMS", "THIRD_PARTY_PROVISION");
            softly.assertThat(activeTerms)
                    .extracting(row -> row.get("TITLE"))
                    .containsExactly(
                            "마케팅 및 이벤트 정보 수신 동의",
                            "서비스 이용을 위한 개인정보 수집·이용 동의",
                            "책모 이용약관 동의",
                            "개인정보 제3자 제공 동의"
                    );
            softly.assertThat(activeTerms)
                    .extracting(row -> row.get("TERM_URL"))
                    .containsExactly(
                            "https://www.checkmo.co.kr/support/terms/marketing/v1",
                            "https://www.checkmo.co.kr/support/terms/privacy-collection/v1",
                            "https://www.checkmo.co.kr/support/terms/service/v1",
                            "https://www.checkmo.co.kr/support/terms/third-party-provision/v1"
                    );
            softly.assertThat(activeTerms)
                    .extracting(row -> booleanValue(row.get("IS_REQUIRED")))
                    .containsExactly(false, true, true, false);
            softly.assertThat(jdbcTemplate.queryForObject("select count(*) from terms", Integer.class)).isEqualTo(4);
            softly.assertThat(jdbcTemplate.queryForObject("select count(*) from member_terms", Integer.class)).isZero();
        });
    }

    @Test
    void 회원을_하드_삭제하면_동의_이력만_삭제하고_약관은_보존한다() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(migratedDataSource());
        Long termsId = jdbcTemplate.queryForObject(
                "select id from terms where terms_type = 'SERVICE_TERMS' and version = 1",
                Long.class
        );
        String memberId = "LOCAL_" + UUID.randomUUID().toString().substring(0, 8);
        insertMember(jdbcTemplate, memberId);

        jdbcTemplate.update(
                """
                        insert into member_terms(member_id, terms_id, is_agreed, created_at, updated_at)
                        values (?, ?, true, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
                        """,
                memberId,
                termsId
        );
        jdbcTemplate.update(
                """
                        insert into member_terms(member_id, terms_id, is_agreed, created_at, updated_at)
                        values (?, ?, true, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6))
                        """,
                memberId,
                termsId
        );
        Integer consentRowsBeforeDelete = jdbcTemplate.queryForObject(
                "select count(*) from member_terms where member_id = ? and terms_id = ?",
                Integer.class,
                memberId,
                termsId
        );

        assertThatThrownBy(() -> jdbcTemplate.update("delete from terms where id = ?", termsId))
                .isInstanceOf(Exception.class);

        jdbcTemplate.update("delete from member where id = ?", memberId);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(consentRowsBeforeDelete).isEqualTo(2);
            softly.assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from terms where id = ?",
                    Integer.class,
                    termsId
            )).isOne();
            softly.assertThat(jdbcTemplate.queryForObject("select count(*) from terms", Integer.class)).isEqualTo(4);
            softly.assertThat(jdbcTemplate.queryForObject(
                    "select count(*) from member_terms where member_id = ?",
                    Integer.class,
                    memberId
            )).isZero();
        });
    }

    @Test
    void 같은_약관_종류와_버전은_중복_등록할_수_없다() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(migratedDataSource());

        assertThatThrownBy(() -> jdbcTemplate.update(
                """
                        insert into terms(terms_type, title, term_url, version, is_active, is_required, created_at, updated_at)
                        values (
                            'SERVICE_TERMS',
                            'duplicate',
                            'https://example.com/duplicate',
                            1,
                            true,
                            true,
                            CURRENT_TIMESTAMP(6),
                            CURRENT_TIMESTAMP(6)
                        )
                        """
        )).isInstanceOf(Exception.class);
    }

    private DataSource migratedDataSource() {
        String databaseName = "terms_migration_" + UUID.randomUUID().toString().replace("-", "");
        String url = "jdbc:h2:mem:" + databaseName
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
        DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
        dataSource.setDriverClassName("org.h2.Driver");
        Path migrationDirectory = copyTermsBaselineMigrations();

        try {
            Flyway.configure()
                    .dataSource(dataSource)
                    .locations("filesystem:" + migrationDirectory.toAbsolutePath())
                    .load()
                    .migrate();
        } finally {
            deleteRecursively(migrationDirectory);
        }

        return dataSource;
    }

    private Path copyTermsBaselineMigrations() {
        try {
            Path migrationDirectory = Files.createTempDirectory("terms-migration-test");
            copyMigrationIfPresent(migrationDirectory, "V1__init_schema.sql");
            copyMigrationIfPresent(migrationDirectory, "V20260120_1__create_terms_table.sql");
            writeH2CompatibleMemberTermsForeignKeyMigration(migrationDirectory);
            copyMigrationIfPresent(migrationDirectory, "V20260625__version_terms_schema_and_seed.sql");
            return migrationDirectory;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to prepare terms migration test directory", exception);
        }
    }

    private void copyMigrationIfPresent(Path migrationDirectory, String filename) throws IOException {
        Path source = Path.of("src/main/resources/db/migration", filename);
        if (Files.exists(source)) {
            Files.copy(source, migrationDirectory.resolve(filename));
        }
    }

    private void writeH2CompatibleMemberTermsForeignKeyMigration(Path migrationDirectory) throws IOException {
        Files.writeString(
                migrationDirectory.resolve("V20260120_2__alter_member.sql"),
                """
                        ALTER TABLE member
                            ADD COLUMN name VARCHAR(10) NULL;

                        ALTER TABLE member
                            ADD COLUMN phone_number VARCHAR(255) NULL;

                        ALTER TABLE member
                            MODIFY COLUMN nick_name VARCHAR(20) NOT NULL;

                        ALTER TABLE member
                            MODIFY COLUMN description VARCHAR(40);

                        ALTER TABLE member_terms
                            ADD CONSTRAINT FK_member_terms_member
                                FOREIGN KEY (member_id) REFERENCES member (id);

                        ALTER TABLE member_terms
                            ADD CONSTRAINT FK_member_terms_terms
                                FOREIGN KEY (terms_id) REFERENCES terms (id);
                        """
        );
    }

    private List<String> columnsOf(JdbcTemplate jdbcTemplate, String tableName) {
        return jdbcTemplate.queryForList(
                """
                        select column_name
                        from information_schema.columns
                        where table_schema = 'public' and table_name = ?
                        order by ordinal_position
                        """,
                String.class,
                tableName
        );
    }

    private void insertMember(JdbcTemplate jdbcTemplate, String memberId) {
        jdbcTemplate.update(
                """
                        insert into member(id, email, nick_name, description, name, phone_number)
                        values (?, ?, ?, '', '테스트', '01000000000')
                        """,
                memberId,
                memberId + "@example.com",
                memberId
        );
    }

    private boolean booleanValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private void deleteRecursively(Path root) {
        try (var paths = Files.walk(root)) {
            paths.sorted((left, right) -> right.compareTo(left))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException("Failed to clean migration test file " + path, exception);
                        }
                    });
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to clean migration test directory", exception);
        }
    }
}
