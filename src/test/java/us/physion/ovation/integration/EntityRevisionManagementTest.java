package us.physion.ovation.integration;

import org.joda.time.DateTime;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import us.physion.ovation.DataContext;
import us.physion.ovation.api.OvationApiModule;
import us.physion.ovation.domain.Project;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Repository should set the entity revision on save
 */
@RunWith(JukitoRunner.class)
public class EntityRevisionManagementTest extends IntegrationTestBase
{
    public static class Module extends JukitoModule
    {

        @Override
        protected void configureTest()
        {
            new OvationApiModule().configure(binder());
        }
    }

    @Test
    public void should_update_revision_on_first_save()
    {
        DataContext ctx = dsc.getContext(USER_UUID, PASSWORD);

        String name = "name";
        String purpose = "purpose";
        DateTime start = new DateTime();
        Project p = ctx.insertProject(name, purpose, start);

        assertNotNull(p.getRevision());
    }

    @Test
    public void should_update_revision_after_update()
    {
        DataContext ctx = dsc.getContext(USER_UUID, PASSWORD);

        String name = "name";
        String purpose = "purpose";
        DateTime start = new DateTime();
        Project p = ctx.insertProject(name, purpose, start);

        String rev = p.getRevision();

        p.addTag("tag");

        assertThat(p.getRevision(), not(equalTo(rev)));
    }
}
