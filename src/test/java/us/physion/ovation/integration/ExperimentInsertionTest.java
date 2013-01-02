/*
 * Copyright (c) 2013. Physion Consulting LLC. All rights reserved.
 */

package us.physion.ovation.integration;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.joda.time.DateTime;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import us.physion.ovation.DataContext;
import us.physion.ovation.api.DataStoreCoordinator;
import us.physion.ovation.api.OvationApiModule;
import us.physion.ovation.domain.Experiment;
import us.physion.ovation.domain.Project;

import static org.junit.Assert.*;

@RunWith(JukitoRunner.class)
public class ExperimentInsertionTest {

    private static final String USER_NAME = "user";
    private static final String EMAIL = "email@email.com";
    private static final char[] PASSWORD = "password".toCharArray();

    public static class Module extends JukitoModule {

        @Override
        protected void configureTest() {
            new OvationApiModule().configure(binder());
        }
    }

    @Inject
    DataStoreCoordinator dsc;

    @Before
    public void create_user() {
        DataContext ctx = dsc.getContext();
        ctx.addUser(USER_NAME, EMAIL, PASSWORD);
    }

    @After
    public void clean_up(CouchDbInstance server,
                         CouchDbConnector db) {
        server.deleteDatabase(db.path());
    }

    /**
     * As an authenticated user, given a Project, when I insert an Experiment into that project, the Experiment should
     * be persisted and retrievable (bi-directionally).
     */
    @Test
    public void should_insert_experiment() {
        DataContext ctx = dsc.getContext();
        ctx.authenticateUser(USER_NAME, PASSWORD);

        String name = "name";
        String purpose = "purpose";
        String protocolNotes = "my protocol\nwith a line break.";
        DateTime start = new DateTime();
        Project p = ctx.insertProject(name, purpose, start);

        Experiment e = p.insertExperiment(purpose, protocolNotes, start);

        assertNotNull(e);

        assertEquals(purpose, e.getPurpose());
        assertEquals(protocolNotes, e.getProtocolNotes());
        assertEquals(start, e.getStart());

        assertTrue(Iterables.contains(e.getProjects(), p));
        assertTrue(Iterators.contains(p.getExperiments(), e));
    }

    /**
     * As an authenticated user, when I add an Experiment to a second project, it should be retrievable from
     * both parent projects
     */

    @Test
    public void should_add_experiment_to_project() {
        DataContext ctx = dsc.getContext();
        ctx.authenticateUser(USER_NAME, PASSWORD);

        String name = "name";
        String purpose = "purpose";
        String protocolNotes = "my protocol\nwith a line break.";
        DateTime start = new DateTime();
        Project p = ctx.insertProject(name, purpose, start);

        Experiment e = p.insertExperiment(purpose, protocolNotes, start);

        Project newProject = ctx.insertProject(name, purpose, start);

        newProject.addExperiment(e);

        assertTrue(Iterables.contains(e.getProjects(), p));
        assertTrue(Iterators.contains(p.getExperiments(), e));

        assertTrue(Iterables.contains(e.getProjects(), newProject));

        ctx.getProjectRepository().clear();
        ctx.getExperimentRepository().clear();
        newProject = (Project) ctx.getObjectWithUuid(newProject.getUuid());
        e = (Experiment) ctx.getObjectWithUuid(e.getUuid());

        assertTrue(Iterators.contains(newProject.getExperiments(), e));
    }

    /**
     * As an authenticated user, when I remove an Experiment from a project, it should no longer be retrievable
     * from that project
     */
    @Test
    public void should_remove_experiment_from_project() {
        DataContext ctx = dsc.getContext();
        ctx.authenticateUser(USER_NAME, PASSWORD);

        String name = "name";
        String purpose = "purpose";
        String protocolNotes = "my protocol\nwith a line break.";
        DateTime start = new DateTime();
        Project p = ctx.insertProject(name, purpose, start);

        Experiment e = p.insertExperiment(purpose, protocolNotes, start);

        Project newProject = ctx.insertProject(name, purpose, start);

        newProject.addExperiment(e);

        p.removeExperiment(e);

        ctx.getProjectRepository().clear();
        ctx.getExperimentRepository().clear();

        p = (Project) ctx.getObjectWithUuid(p.getUuid());
        e = (Experiment) ctx.getObjectWithUuid(e.getUuid());

        assertFalse(Iterables.contains(e.getProjects(), p));
        assertFalse(Iterators.contains(p.getExperiments(), e));

        assertTrue(Iterables.contains(e.getProjects(), newProject));
        assertTrue(Iterators.contains(newProject.getExperiments(), e));
    }
}
