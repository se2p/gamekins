/*
 * Copyright 2021 Gamekins contributors
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

package org.gamekins.achievement

import hudson.FilePath
import hudson.model.Run
import hudson.model.TaskListener
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.gamekins.GameUserProperty
import org.gamekins.util.AchievementUtil
import org.gamekins.util.JacocoUtil

class AchievementTest: AnnotationSpec() {

    private lateinit var achievement: Achievement
    private val classes = arrayListOf<JacocoUtil.ClassDetails>()
    private val constants = hashMapOf<String, String>()
    private val run = mockkClass(Run::class)
    private val property = mockkClass(GameUserProperty::class)
    private val workspace = mockkClass(FilePath::class)

    @BeforeEach
    fun init() {
        mockkStatic(AchievementInitializer::class)
        mockkStatic(AchievementUtil::class)

        achievement = AchievementInitializer.initializeAchievement("solve_challenge.json")
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun clone() {
        achievement.clone() shouldBe achievement
    }

    @Test
    fun getSolvedTimeString() {
        achievement.solvedTimeString shouldBe "Not solved"

        every { AchievementUtil.solveXChallenges(any(), any(), any(), any(), any(), any(), any()) } returns true
        achievement.isSolved(classes, constants, run, property, workspace, TaskListener.NULL) shouldBe true
        achievement.solvedTimeString shouldNotBe "Not solved"
    }

    @Test
    fun testEquals() {
        achievement.equals(null) shouldBe false

        achievement.equals(classes) shouldBe false

        val achievement2 = mockkClass(Achievement::class)
        every { achievement2.description } returns ""
        every { achievement2.title } returns ""
        (achievement == achievement2) shouldBe false

        every { achievement2.description } returns "Solve your first Challenge"
        (achievement == achievement2) shouldBe false

        every { achievement2.description } returns ""
        every { achievement2.title } returns "I took the first Challenge"
        (achievement == achievement2) shouldBe false

        every { achievement2.description } returns "Solve your first Challenge"
        (achievement == achievement2) shouldBe true
    }

    @Test
    fun isSolved() {
        every { AchievementUtil.solveXChallenges(any(), any(), any(), any(), any(), any(), any()) } returns false
        achievement.isSolved(classes, constants, run, property, workspace, TaskListener.NULL) shouldBe false

        every { AchievementUtil.solveXChallenges(any(), any(), any(), any(), any(), any(), any()) } returns true
        achievement.isSolved(classes, constants, run, property, workspace, TaskListener.NULL) shouldBe true
    }

    @Test
    fun printToXML() {
        achievement.printToXML("") shouldBe "<Achievement title=\"I took the first Challenge\" description=\"Solve your first Challenge\" secret=\"false\" solved=\"0\"/>"
    }

    @Test
    fun testToString() {
        achievement.toString() shouldBe "I took the first Challenge: Solve your first Challenge"
    }
}