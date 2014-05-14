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
        Doc one = DocResource.getDoc(789980L, dbc);
        Doc two = DocResource.getDoc(3026593L, dbc);
        EfficientIndirectionCalculator eic = new EfficientIndirectionCalculator(one, two, dbc, 6);
        eic.call().save(dbc);
        EfficientIndirectionCalculator eic2 = new EfficientIndirectionCalculator(one, two, dbc, 6);
        eic2.call();
    }
}
