package wiki.doc;

import wiki.DbConnector;
import wiki.Savable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Michael Kelley on 4/30/14.
 * See LICENSE file for license information.
 */
public class Doc implements Savable {
    public final long id;
    public final String title;
    public final String text;

    public Doc(long id, String title, String text) {
        this.id = id;
        this.title = title;
        this.text = text;
    }

    public List<Doc> getLinkedDocs(DbConnector dbc) {
        return DocResource.getLinkedDocs(this, dbc);
    }

    public ArrayList<String> getTextLinks() {
        ArrayList<String> linkList = new ArrayList<>();

        byte[] bytes = text.getBytes();
        for (int i = 0; i < bytes.length - 1; i++) {
            if (bytes[i] == '[' && bytes[i+1] == '[') {
                for (int j = i + 2; j < bytes.length - 1; j++) {
                    if (bytes[j] == ']' && bytes[j+1] == ']') {
                        String s = new String(bytes, i + 2, j - i - 2);
                        s = s.split("\\|", -1)[0];
                        linkList.add(s);
                        i = j;
                        break;
                    }
                }
            }
        }
        return linkList;
    }

    @Override
    public void save(DbConnector dbc) {
        DocResource.save(this, dbc);
    }

    @Override
    public String toString() {
        return this.title;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Doc)) {
            return false;
        }
        Doc d = (Doc)o;
        return d.id == this.id;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }
}

