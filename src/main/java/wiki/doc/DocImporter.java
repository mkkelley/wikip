package wiki.doc;

import wiki.DbConnector;

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
public class DocImporter {
    public static void main(String[] args) {
        DocImporter wr = new DocImporter();
        String path = null;
        for (int i = 0; i < args.length; i++) {
            if ("path".equals(args[i]) && i + 1 < args.length ) {
                path = args[i + 1];
            }
        }
        if (path == null) {
            System.out.println("Please specify the file path.");
            System.exit(1);
        }
        //wr.readXML("/mnt/dev/wiki/enwiki-20140203-pages-articles.xml");
        wr.readXML(path);
    }
    public boolean readXML(String filename) {
        DbConnector dbc = new DbConnector("localhost");
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
                                DocResource.insertAll(toSave, dbc);
                                System.out.println(n);
                                toSave.clear();
                            }
                        }
                    }
                }
            }
            if (!toSave.isEmpty()) {
                DocResource.insertAll(toSave, dbc);
                System.out.println(n);
                toSave.clear();
            }
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
        return true;
    }
}
