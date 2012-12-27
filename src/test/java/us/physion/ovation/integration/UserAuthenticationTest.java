package us.physion.ovation.integration;

import com.google.inject.Inject;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import us.physion.ovation.api.DataContext;
import us.physion.ovation.api.DataStoreCoordinator;
import us.physion.ovation.domain.User;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JukitoRunner.class)
public class UserAuthenticationTest {

    public static class Module extends JukitoModule {

        @Override
        protected void configureTest() {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    @Inject
    DataStoreCoordinator dsc;

    @Test
    public void should_authenticate_first_user() {
        DataContext ctx = dsc.getContext();
        char[] password = "some password".toCharArray();
        User u = ctx.addUser("me", "me@me.com", password);

        assertFalse(ctx.isAuthenticated());

        assertTrue(ctx.authenticateUser(u.getUuid(), password));

        assertTrue(ctx.isAuthenticated());
    }

    @Test
    public void should_authenticate_across_contexts() {
        DataContext ctx = dsc.getContext();
        char[] password = "some password".toCharArray();
        User u = ctx.addUser("me", "me@me.com", password);

        //Get a new context
        ctx = dsc.getContext();
        assertFalse(ctx.isAuthenticated());
        assertTrue(ctx.authenticateUser(u.getUuid(), password));
        assertTrue(ctx.isAuthenticated());
    }
}
