package us.physion.ovation.integration;


import org.junit.Test;
import us.physion.ovation.DataContext;
import us.physion.ovation.api.DataStoreCoordinator;
import us.physion.ovation.api.Ovation;

import static org.junit.Assert.assertTrue;

public class TopLevelInteractiveAPITest {

    /**
     * As an Ovation user, I should be able to create a new DataStoreCoordinator from a top-level API call
     * so that I can use Ovation from, e.g. Python interactive.
     */
    @Test
    public void should_create_data_store_coordinator() {
        DataStoreCoordinator dsc = Ovation.newDataStoreCoordinator();

        DataContext ctx = dsc.getContext();
        String name = "name";
        char[] password = "password".toCharArray();
        ctx.addUser(name, "email@email.com", password);

        assertTrue(ctx.authenticateUser(name, password));
    }
}
