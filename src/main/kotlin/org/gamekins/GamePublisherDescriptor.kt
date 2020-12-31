/*
 * Copyright 2020 Gamekins contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gamekins

import hudson.Extension
import hudson.model.AbstractProject
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.Publisher
import hudson.util.FormValidation
import org.gamekins.challenge.*
import org.gamekins.util.PublisherUtil
import org.jenkinsci.Symbol
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.QueryParameter
import javax.annotation.Nonnull

/**
 * Registers the [GamePublisher] to Jenkins as an extension and also works as an communication
 * point between the Jetty server and the [GamePublisher]. Also serves as a Pipeline identifier.
 *
 * @author Philipp Straubinger
 * @since 1.0
 */
@Extension
@Symbol("gamekins")
class GamePublisherDescriptor : BuildStepDescriptor<Publisher?>(GamePublisher::class.java) {

    /**
     * Add third party [Challenge]s to the [HashMap] [challenges] here from your own code. This class is initialised
     * during startup of Jenkins and it can be assumed that all built-in Challenges are registered here.
     *
     * The key denotes here the Java [Class] of the [Challenge] and the value the weight of the [Challenge]. Choose
     * bigger weights for a higher probability that the [Challenge] is chosen for generation, and lower values for
     * the opposite.
     */
    companion object {
        @Transient val challenges: HashMap<Class<out Challenge>, Int> = hashMapOf()
    }

    init {
        initChallengesMap()
    }

    /**
     * Checks whether the path of the JaCoCo csv file [jacocoCSVPath] exists in the [project].
     */
    fun doCheckJacocoCSVPath(@AncestorInPath project: AbstractProject<*, *>?,
                             @QueryParameter jacocoCSVPath: String?): FormValidation {
        if (project?.someWorkspace == null) {
            return FormValidation.ok()
        }
        return if (PublisherUtil.doCheckJacocoCSVPath(project.someWorkspace!!, jacocoCSVPath!!))
            FormValidation.ok()
        else FormValidation.error("The file could not be found")
    }

    /**
     * Checks whether the path of the JaCoCo index.html file [jacocoResultsPath] exists in the [project].
     */
    fun doCheckJacocoResultsPath(@AncestorInPath project: AbstractProject<*, *>?,
                                 @QueryParameter jacocoResultsPath: String?): FormValidation {
        if (project?.someWorkspace == null) {
            return FormValidation.ok()
        }
        return if (PublisherUtil.doCheckJacocoResultsPath(project.someWorkspace!!, jacocoResultsPath!!))
            FormValidation.ok()
        else FormValidation.error("The folder is not correct")
    }

    @Nonnull
    override fun getDisplayName(): String {
        return "Publisher for Gamekins plugin"
    }

    /**
     * Adds the built-in [Challenge]s to the [HashMap] [challenges] for generation.
     */
    private fun initChallengesMap() {
        challenges[ClassCoverageChallenge::class.java] = 2
        challenges[LineCoverageChallenge::class.java] = 4
        challenges[MethodCoverageChallenge::class.java] = 3
        challenges[TestChallenge::class.java] = 1
    }

    /**
     * The configuration is only shown in jobs of the type [AbstractProject], since it is otherwise called via a
     * Pipeline script.
     *
     * @see BuildStepDescriptor.isApplicable
     */
    override fun isApplicable(jobType: Class<out AbstractProject<*, *>?>): Boolean {
        return AbstractProject::class.java.isAssignableFrom(jobType)
    }
}