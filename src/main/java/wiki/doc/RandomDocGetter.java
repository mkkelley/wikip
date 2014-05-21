package wiki.doc;

import wiki.DbConnector;

import java.util.List;

/**
 * Created by Michael Kelley on 5/11/14.
 * See LICENSE file for license information.
 */
public class RandomDocGetter {
    private DbConnector dbc;
    List<Doc> randomDocs;
    public RandomDocGetter(DbConnector dbc) {
        this.dbc = dbc;

        repopulateDocs();
    }

    private void repopulateDocs() {
        randomDocs = dbc.jdbcTemplate.query("SELECT id, title FROM pages ORDER BY random() LIMIT 1000", new DocMapper());
    }

    public Doc getRandomDoc() {
        if (randomDocs.size() == 0) {
            repopulateDocs();
        }
        return randomDocs.remove(0);
    }
}
