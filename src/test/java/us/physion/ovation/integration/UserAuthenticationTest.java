package us.physion.ovation.integration;

import com.google.inject.Inject;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import us.physion.ovation.DataContext;
import us.physion.ovation.api.DataStoreCoordinator;
import us.physion.ovation.api.OvationApiModule;
import us.physion.ovation.couch.api.UserDao;
import us.physion.ovation.domain.User;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JukitoRunner.class)
public class UserAuthenticationTest
{

    public static class Module extends JukitoModule
    {

        @Override
        protected void configureTest()
        {
            new OvationApiModule().configure(binder());
        }
    }

    @Inject
    DataStoreCoordinator dsc;

    @After
    public void delete_users(UserDao dao)
    {
        for (us.physion.ovation.couch.dto.User u : dao.getAll()) {
            dao.remove(u);
        }
    }

    /**
     * As an Ovation user, I should be able to add the first account to a new database
     * and authenticate using the new account.
     */
    @Test
    public void should_authenticate_first_user()
    {
        DataContext ctx = dsc.getContext();
        char[] password = "some password".toCharArray();
        User u = ctx.addUser("me", "me@me.com", password);

        assertFalse(ctx.isAuthenticated());

        assertTrue(ctx.authenticateUser(u.getUuid(), password));

        assertTrue(ctx.isAuthenticated());
    }

    /**
     * As an Ovation user, I should be able to authenticate multiple DataContexts
     */
    @Test
    public void should_authenticate_across_contexts()
    {
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
