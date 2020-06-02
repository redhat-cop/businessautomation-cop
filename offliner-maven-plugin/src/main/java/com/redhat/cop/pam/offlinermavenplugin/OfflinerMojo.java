/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.cop.pam.offlinermavenplugin;

import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;

/**
 * @author cluppi@redhat.com
 */
@Mojo(name = "offliner", defaultPhase = LifecyclePhase.PACKAGE)
public class OfflinerMojo extends AbstractMojo {

    @Parameter(required = true)
    private List<String> artifacts;

    @Parameter(required = false)
    private String settingsFile;

    @Parameter(required = false)
    private String outputDirectory;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    public void execute() throws MojoExecutionException {
        if (outputDirectory == null || outputDirectory.isEmpty()) {
            outputDirectory = project.getBasedir().toPath().toString() + "/target/classes/repository/";
            Paths.get(outputDirectory).toFile().mkdir();
        }
        
        if (System.getProperties().containsKey("sun.java.command")) {
            try {
                final String[] parsedMavenCommand = CommandLineUtils.translateCommandline(System.getProperty("sun.java.command"));
                for(int count = 0; count < parsedMavenCommand.length; count++){
                    final String s = parsedMavenCommand[count];
                    if(s.equals("-s") || s.equals("--settings")) {
                        final String settingFileFromCommandline = parsedMavenCommand[++count];
                        if(settingsFile != null && !settingsFile.isEmpty()){
                            getLog().warn("Overriding settings.xml path from commandline new path is " + settingFileFromCommandline);
                        }else{
                            getLog().info("Setting settings.xml path from commandline " + settingFileFromCommandline);
                        }
                        settingsFile = settingFileFromCommandline;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        System.setProperty("maven.repo.local", outputDirectory);
        
        getLog().info("Start populating " + outputDirectory + " maven repository");
        getMavenResolvedArtifact(artifacts);
        getLog().info("End populating " + outputDirectory + " maven repository");
    }

    private MavenResolvedArtifact[] getMavenResolvedArtifact(final List<String> gavs) {
        MavenResolvedArtifact[] result = null;
        for (final String gav : gavs) {
            if(settingsFile!= null && !settingsFile.isEmpty()) {
                getLog().debug("getMavenResolvedArtifact using provided settings.xml("+ settingsFile +") for GAV " + gav);
                result = ArrayUtils.addAll(result, Maven //
                    .configureResolver() //
                    .fromFile(settingsFile) //
                    .resolve(gav) //
                    .withTransitivity().asResolvedArtifact());
            }else{
                getLog().debug("getMavenResolvedArtifact using default settings.xml for GAV " + gav);
                result = ArrayUtils.addAll(result, Maven //
                    .configureResolver() //
                    .resolve(gav) //
                    .withTransitivity().asResolvedArtifact());
            }
        }
        return result;
    }
}
