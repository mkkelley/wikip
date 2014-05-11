package wiki.doc;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import wiki.DbConnector;

import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Kelley on 5/2/14.
 * See LICENSE file for license information.
 */
public class DocResource {

    public static Doc getDoc(long id, DbConnector dbc) {
        List<Doc> docs = dbc.jdbcTemplate.query("SELECT * FROM pages WHERE id = ?",
                new Object[]{id},
                new DocMapper());
        if (docs.size() != 1) {
            throw new IllegalArgumentException("Doc not found: id " + id);
        }
        return docs.get(0);
    }

    private static PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory("SELECT pages.id, title FROM pages " +
            "INNER JOIN links ON links.toPage = pages.id " +
            "WHERE links.fromPage = ?",
            new int[]{JDBCType.BIGINT.ordinal()});
    public static List<Doc> getLinkedDocs(Doc doc, DbConnector dbc) {

        PreparedStatementCreator psc = pscf.newPreparedStatementCreator(new Object[]{doc.id});
        List<Doc> docs = dbc.jdbcTemplate.query(psc, (resultSet,i) -> {
            long id = resultSet.getLong("id");
            String title = resultSet.getString("title");
            return new Doc(id, title, "");
        });
        return docs;
    }

    private static String getParamList(int n) {
        StringBuilder sb = new StringBuilder("(?");
        for (int i = 1; i < n; i++) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }
    public static List<Doc> getAllLinkedDocs(List<Doc> docs, DbConnector dbc) {
        if (docs.size() == 0) {
            return new ArrayList<>(0);
        } else if (docs.size() <= 30000) {
            String inList = getParamList(docs.size());
            String sql = "SELECT pages.id FROM pages " +
                    "INNER JOIN links ON links.toPage = pages.id " +
                    "WHERE links.fromPage IN " + inList;
            Object[] ids = docs.stream().map((doc) -> doc.id).toArray();
            List<Doc> linkedDocs = dbc.jdbcTemplate.query(sql, ids, (resultSet, i) -> {
                long id = resultSet.getLong("id");
                return new Doc(id, "", "");
            });

            return linkedDocs;
        } else {
            List<Doc> first = getAllLinking(docs.subList(0, 30000), dbc);
            first.addAll(getAllLinking(docs.subList(30000, docs.size()), dbc));
            return first;
        }
    }

    private static PreparedStatementCreatorFactory pscf_linking = new PreparedStatementCreatorFactory(
            "SELECT pages.id, title FROM pages " +
                    "INNER JOIN links ON links.fromPage = pages.id " +
                    "WHERE links.toPage = ?",
            new int[]{JDBCType.BIGINT.ordinal()});
    public static List<Doc> getLinkingDocs(Doc target, DbConnector dbc) {
        PreparedStatementCreator psc = pscf_linking.newPreparedStatementCreator(new Object[]{target.id});
        List<Doc> docs = dbc.jdbcTemplate.query(psc, (resultSet, i) -> {
            long id = resultSet.getLong("id");
            String title = resultSet.getString("title");
            return new Doc(id, title, "");
        });
        return docs;
    }

    public static List<Doc> getAllLinking(List<Doc> docs, DbConnector dbc) {
        if (docs.size() == 0) {
            return new ArrayList<>(0);
        } else if (docs.size() <= 30000) {
            String inList = getParamList(docs.size());
            String sql = "SELECT pages.id FROM pages " +
                    "INNER JOIN links ON links.fromPage = pages.id " +
                    "WHERE links.toPage IN " + inList;
            Object[] ids = docs.stream().map((doc) -> doc.id).toArray();
            List<Doc> linkingDocs = dbc.jdbcTemplate.query(sql, ids, (resultSet, i) -> {
                long id = resultSet.getLong("id");
                return new Doc(id, "", "");
            });

            return linkingDocs;
        } else {
            List<Doc> first = getAllLinking(docs.subList(0, 30000), dbc);
            first.addAll(getAllLinking(docs.subList(30000, docs.size()), dbc));
            return first;
        }
    }


    public static void insertAll(final List<Doc> docs, DbConnector dbc) {
        BatchPreparedStatementSetter bpss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setString(1, docs.get(i).title);
                preparedStatement.setLong(2, docs.get(i).id);
            }

            @Override
            public int getBatchSize() {
                return docs.size();
            }
        };

        dbc.jdbcTemplate.batchUpdate("INSERT INTO pages(title, id) values(?, ?)", bpss);
    }

    public static void save(Doc wd, DbConnector dbc) {
        List<Doc> pages = dbc.jdbcTemplate.query("SELECT * FROM pages WHERE id = ?", new Object[] {wd.id}, new DocMapper());
        if (pages.size() != 0) {
            if (!pages.get(0).title.equals(wd.title)) {
                dbc.jdbcTemplate.update("UPDATE pages SET title = ? WHERE id = ?", wd.title, wd.id);
            }
        } else {
            dbc.jdbcTemplate.update("INSERT INTO pages(id, title) values(?, ?)", wd.id, wd.title);
        }
    }
}
