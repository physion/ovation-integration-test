package us.physion.ovation.integration;

import com.google.inject.Inject;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.junit.After;
import org.junit.Before;
import us.physion.ovation.DataContext;
import us.physion.ovation.api.DataStoreCoordinator;
import us.physion.ovation.domain.User;

import java.util.UUID;


public class IntegrationTestBase {
    protected static final String USER_NAME = "user";
    protected static final char[] PASSWORD = "password".toCharArray();
    private static final String EMAIL = "email@email.com";

    UUID USER_UUID;

    @Inject
    DataStoreCoordinator dsc;

    @Before
    public void create_user() {
        DataContext ctx = dsc.getContext();
        User u = ctx.addUser(USER_NAME, EMAIL, PASSWORD);
        USER_UUID = u.getUuid();
    }

    @After
    public void clean_up(CouchDbInstance server,
                         CouchDbConnector db) {
        server.deleteDatabase(db.path());
    }
}
