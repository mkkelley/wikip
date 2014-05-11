package wiki;

import wiki.doc.Doc;
import wiki.doc.DocResource;
import wiki.doc.EfficientIndirectionCalculator;

/**
 * Created by Michael Kelley on 5/11/14.
 * See LICENSE file for license information.
 */
public class Benchy {
    public static void main(String[] args) {
        DbConnector dbc = new DbConnector("localhost");
        Doc one = DocResource.getDoc(35218121L, dbc);
        Doc two = DocResource.getDoc(946195L, dbc);
        EfficientIndirectionCalculator eic = new EfficientIndirectionCalculator(one, two, dbc, 6);
        eic.call();
    }
}
