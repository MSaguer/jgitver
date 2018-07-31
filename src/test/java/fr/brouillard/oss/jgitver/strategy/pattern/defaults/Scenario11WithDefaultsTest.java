/**
 * Copyright (C) 2016 Matthieu Brouillard [http://oss.brouillard.fr/jgitver] (matthieu@brouillard.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.brouillard.oss.jgitver.strategy.pattern.defaults;

import fr.brouillard.oss.jgitver.GitVersionCalculator;
import fr.brouillard.oss.jgitver.Misc;
import fr.brouillard.oss.jgitver.Scenarios;
import fr.brouillard.oss.jgitver.Scenarios.Scenario;
import fr.brouillard.oss.jgitver.Strategies;
import fr.brouillard.oss.jgitver.metadata.Metadatas;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.*;

import java.io.IOException;

import static fr.brouillard.oss.jgitver.Lambdas.mute;
import static fr.brouillard.oss.jgitver.Lambdas.unchecked;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Scenario11WithDefaultsTest {
    private static Scenario scenario;
    private Repository repository;
    private Git git;
    private GitVersionCalculator versionCalculator;

    /**
     * Initialiaze the whole junit class tests ; creates the git scenario.
     */
    @BeforeClass
    public static void initClass() {
        scenario = Scenarios.s11_linear_no_tag_repository();
        if (Misc.isDebugMode()) {
            System.out.println("git repository created under: " + scenario.getRepositoryLocation());
        }
    }

    /**
     * Cleanup the whole junit scenario ; deletes the created git repository.
     */
    @AfterClass
    public static void cleanupClass() {
        try {
            Misc.deleteDirectorySimple(scenario.getRepositoryLocation());
        } catch (Exception ignore) {
            System.err.println("cannot remove " + scenario.getRepositoryLocation());
        }
    }

    /**
     * Prepare common variables to access the git repository.
     * 
     * @throws IOException if a disk error occurred
     */
    @Before
    public void init() throws IOException {
        repository = new FileRepositoryBuilder().setGitDir(scenario.getRepositoryLocation()).build();
        git = new Git(repository);
        versionCalculator = GitVersionCalculator.location(scenario.getRepositoryLocation())
                .setStrategy(Strategies.PATTERN);

        // reset the head to master
        unchecked(() -> git.checkout().setName("master").call());
    }

    /**
     * Cleanups after each tests.
     */
    @After
    public void clean() {
        mute(() -> git.close());
        mute(() -> repository.close());
        mute(() -> versionCalculator.close());
    }

    @Test
    public void head_is_on_master_by_default() throws Exception {
        assertThat(repository.getBranch(), is("master"));
    }

    @Test
    public void version_of_A_commit() {
        ObjectId aCommit = scenario.getCommits().get("A");

        // checkout the first commit in scenario
        unchecked(() -> git.checkout().setName(aCommit.name()).call());
        assertThat(versionCalculator.getVersion(), is("0.0.0-0"));
    }
    
    @Test
    public void version_of_B_commit() {
        ObjectId bCommit = scenario.getCommits().get("B");
        
        // checkout the first commit in scenario
        unchecked(() -> git.checkout().setName(bCommit.name()).call());
        assertThat(versionCalculator.getVersion(), is("0.0.0-1"));
    }
    
    @Test
    public void version_of_C_commit() {
        ObjectId cCommit = scenario.getCommits().get("C");
        
        // checkout the first commit in scenario
        unchecked(() -> git.checkout().setName(cCommit.name()).call());
        assertThat(versionCalculator.getVersion(), is("0.0.0-2"));
    }

    @Test
    public void version_of_master() {
        // checkout the commit in scenario
        unchecked(() -> git.checkout().setName("master").call());
        assertThat(versionCalculator.getVersion(), is("0.0.0-2"));

        assertThat(versionCalculator.meta(Metadatas.NEXT_MAJOR_VERSION).get(), is("1.0.0"));
        assertThat(versionCalculator.meta(Metadatas.NEXT_MINOR_VERSION).get(), is("0.1.0"));
        assertThat(versionCalculator.meta(Metadatas.NEXT_PATCH_VERSION).get(), is("0.0.1"));
    }
}
