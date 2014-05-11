package wiki.link;

import wiki.DbConnector;
import wiki.Savable;

/**
 * Created by Michael Kelley on 4/30/14.
 * See LICENSE file for license information.
 */
public class Link implements Savable {
    public final long from;
    public final long to;

    public Link(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void save(DbConnector dbc) {
        LinkResource.save(this, dbc);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Link)) {
            return false;
        }
        Link wl = (Link)o;
        return wl.to == this.to && wl.from == this.from;
    }
}
