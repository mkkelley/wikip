package wiki.doc;

import wiki.DbConnector;

import java.util.List;

/**
 * Created by Michael Kelley on 5/13/14.
 * See LICENSE file for license information.
 */
public class DocId {
    public final long id;

    public DocId(long id) {
        this.id = id;
    }

    public Doc getDoc(DbConnector dbc) {
        return DocResource.getDoc(id, dbc);
    }

    public List<DocId> getLinkedDocs(DbConnector dbc) {
        return DocResource.getLinkedDocs(this, dbc);
    }

    public List<DocId> getLinkingDocs(DbConnector dbc) {
        return DocResource.getLinkingDocs(this, dbc);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DocId)) {
            return false;
        }
        DocId d = (DocId)o;
        return d.id == this.id;
    }

    @Override
    public int hashCode() {
        return (int)id;
    }
}
