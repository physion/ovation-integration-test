package us.physion.ovation.integration;

import org.joda.time.DateTime;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import us.physion.ovation.DataContext;
import us.physion.ovation.api.OvationApiModule;
import us.physion.ovation.domain.Project;
import us.physion.ovation.values.Resource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

@RunWith(JukitoRunner.class)
public class AnnotationRoundTripTest extends IntegrationTestBase
{
    public static class Module extends JukitoModule
    {

        @Override
        protected void configureTest()
        {
            new OvationApiModule().configure(binder());
        }
    }

    /**
     * As an authenticated user, given an annotatable entity, when I add a tag, the tag should be persisted,
     * and retrievable by my user.
     */
    @Test
    public void should_add_tags() throws InterruptedException
    {
        DataContext ctx = dsc.getContext(USER_UUID, PASSWORD);

        String name = "name";
        String purpose = "purpose";
        DateTime start = new DateTime();
        Project p = ctx.insertProject(name, purpose, start);

        String tag1 = "MYTAG";
        p.addTag(tag1);

        UUID projectUuid = p.getUuid();

        DataContext verifyContext = dsc.getContext(USER_UUID, PASSWORD);
        Project actual = (Project) verifyContext.getObjectWithUuid(projectUuid);

        assertThat(actual.getUserTags(verifyContext.getAuthenticatedUser()), hasItem(tag1));
    }

    /**
     * As an authenticated user, given an annotatable entity, when I add a property, the property should be
     * persisted, and retrievable by my user.
     */
    @Test
    public void should_add_properties() throws InterruptedException
    {
        DataContext ctx = dsc.getContext(USER_UUID, PASSWORD);

        String name = "name";
        String purpose = "purpose";
        DateTime start = new DateTime();
        Project p = ctx.insertProject(name, purpose, start);

        String key = "key";
        String value = "value";

        p.addProperty(key, value);

        UUID projectUuid = p.getUuid();

        DataContext verifyContext = dsc.getContext(USER_UUID, PASSWORD);
        Project actual = (Project) verifyContext.getObjectWithUuid(projectUuid);

        assertEquals(value, actual.getUserProperty(verifyContext.getAuthenticatedUser(), key));
        assertEquals(value, actual.getProperty(key).get(verifyContext.getAuthenticatedUser()));
    }

    /**
     * As an authenticated user, given multiple annotatable entities, when I add a property, the property should be
     * persisted, and retrievable by my user.
     */
    @Test
    public void should_add_properties_distinguishing_entities() throws InterruptedException
    {
        DataContext ctx = dsc.getContext();
        ctx.authenticateUser(USER_NAME, PASSWORD);

        String name = "name";
        String purpose = "purpose";
        DateTime start = new DateTime();
        Project p = ctx.insertProject(name, purpose, start);
        Project p2 = ctx.insertProject(name, purpose, start);


        String key1 = "key";
        String value1 = "value";

        String key2 = "key2";
        int value2 = 10;

        p.addProperty(key1, value1);
        p2.addProperty(key2, value2);


        UUID projectUuid = p.getUuid();
        UUID project2Uuid = p2.getUuid();


        DataContext verifyContext = dsc.getContext(USER_UUID, PASSWORD);
        Project actual1 = (Project) verifyContext.getObjectWithUuid(projectUuid);

        assertEquals(value1, actual1.getUserProperty(verifyContext.getAuthenticatedUser(), key1));
        assertEquals(value1, actual1.getProperty(key1).get(ctx.getAuthenticatedUser()));

        Project actual2 = (Project) verifyContext.getObjectWithUuid(project2Uuid);

        assertEquals(value2, actual2.getUserProperty(verifyContext.getAuthenticatedUser(), key2));
        assertEquals(value2, actual2.getProperty(key2).get(ctx.getAuthenticatedUser()));
    }

    /**
     * As an authenticated user, given an annotatable entity, when I add a Resource, it should be persited,
     * and retrievable by name.
     */
    @Test
    public void should_add_resource() throws URISyntaxException
    {
        DataContext ctx = dsc.getContext(USER_UUID, PASSWORD);

        String name = "name";
        String purpose = "purpose";
        DateTime start = new DateTime();
        Project p = ctx.insertProject(name, purpose, start);

        String rsrcName = "RESOURCE";
        URI rsrcUri = new URI("http://ovation.io/resource1");
        String uti = "public.anything";

        p.addResource(rsrcName, rsrcUri, uti);

        UUID projectUuid = p.getUuid();

        DataContext verifyContext = dsc.getContext(USER_UUID, PASSWORD);
        Project actual = (Project) verifyContext.getObjectWithUuid(projectUuid);
        Resource r = actual.getResource(rsrcName);

        assertThat(actual.getResourceNames(), hasItem(rsrcName));
        assertEquals(rsrcUri, r.getBaseUri());
        assertEquals(uti, r.getUti());
    }
}
