package wiki.link;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import wiki.DbConnector;
import wiki.doc.DocMapper;
import wiki.doc.Doc;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael Kelley on 4/30/14.
 * See LICENSE file for license information.
 */
public class LinkImporter {
    public static void main(String[] args) {
        LinkImporter wr = new LinkImporter();
        wr.readXML("/mnt/dev/wiki/enwiki-20140203-pages-articles.xml");
    }
    public boolean readXML(String filename) {

        System.exit(1);
        //MAKE BLOODY SURE YOU HAVE SOME HOURS.
        DbConnector dbc = new DbConnector("jdbc:postgresql:wikip");
        dbc.jdbcTemplate.update("TRUNCATE links;");
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLStreamReader xsr = xif.createXMLStreamReader(new FileInputStream(filename));

            long n = 0;
            List<Doc> toSave = new ArrayList<>();
            while (xsr.hasNext()) {
                xsr.next();
                if (xsr.getEventType() == XMLStreamReader.START_ELEMENT) {
                    if (xsr.getLocalName().equals("page")) {
                        long id = -1;
                        String title = null;
                        String text = null;
                        while (xsr.hasNext()) {
                            xsr.next();
                            if (xsr.getEventType() == XMLStreamReader.START_ELEMENT) {
                                if (xsr.getLocalName().equals("id") && id == -1) {
                                    id = Long.parseLong(xsr.getElementText());
                                }
                                if (xsr.getLocalName().equals("title")) {
                                    title = xsr.getElementText();
                                }
                                if (xsr.getLocalName().equals("text")) {
                                    text = xsr.getElementText();
                                }
                            } else if (xsr.getEventType() == XMLStreamReader.END_ELEMENT && xsr.getLocalName().equals("page")) {
                                break;
                            }
                        }
                        if (id != -1 && title != null && text != null) {
                            Doc wd = new Doc(id, title, text);
                            toSave.add(wd);
                            n++;
                            if (n % 1000 == 0) {
                                insertLinks(toSave, dbc);
//                                WikiDoc.insertAll(toSave, dbc);
                                System.out.println(n);
                                toSave.clear();
                            }
                        }
                    }
                }
            }
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String getParamList(int n) {
        StringBuilder sb = new StringBuilder("(?");
        for (int i = 1; i < n; i++) {
            sb.append(", ?");
        }
        sb.append(")");
        return sb.toString();
    }

    private List<Doc> getPages(List<String> links, DbConnector dbc) {
        long n = links.size();
        // Max is actually in the area of 2^15, margin included for safety.
        // Limit is imposed by the JDBC driver limit on # of parameters for
        // a prepared statement.
        if (n <= 30000) {
            String[] linkArray = new String[links.size()];
            linkArray = links.toArray(linkArray);
            List<Doc> pages = dbc.jdbcTemplate.query("SELECT * FROM pages WHERE title IN " + getParamList(links.size()),
                    linkArray,
                    new DocMapper());
            return pages;
        } else {
            // Running into issues with stack space is a possibility here
            // But if the code is hundreds of levels of deep in 30000 element
            // lists, there should be some more thought as to why it's getting
            // 3,000,000+ pages for a set of links.
            List<Doc> first = getPages(links.subList(0, 30000), dbc);
            first.addAll(getPages(links.subList(30000, links.size()), dbc));
            return first;
        }
    }

    private void insertLinks(List<Doc> docs, DbConnector dbc) {
        ArrayList<Link> linksToSave = new ArrayList<>();
        for (Doc linkedFrom : docs) {
            ArrayList<String> links = linkedFrom.getTextLinks();

            if (links.size() == 0) {
                continue;
            }
            try {
                List<Doc> pages = getPages(links, dbc);
                for (Doc linkedTo : pages) {
                    Link wl = new Link(linkedFrom.id, linkedTo.id);
                    linksToSave.add(wl);
                }
            } catch (CannotGetJdbcConnectionException e) {
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    System.exit(1);
                    //do nothing. should never be interrupted
                }
                insertLinks(docs, dbc);
                return;
            }
        }
        try {
            LinkResource.insertAll(linksToSave, dbc);
        } catch (CannotGetJdbcConnectionException e) {
            try {
                Thread.sleep(120000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                System.exit(1);
                //do nothing. should never be interrupted
            }
            LinkResource.insertAll(linksToSave, dbc);
        }
    }
}
