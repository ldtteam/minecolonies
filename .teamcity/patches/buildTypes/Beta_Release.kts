package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.VcsLabeling
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.vcsLabeling
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'Beta_Release'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("Beta_Release")) {
    expectSteps {
        gradle {
            name = "Compile"
            id = "RUNNER_9"
            tasks = "build createChangelog curseforge publish"
            buildFile = "build.gradle"
            enableStacktrace = true
            param("org.jfrog.artifactory.selectedDeployableServer.deployReleaseText", "%Project.Type%")
            param("org.jfrog.artifactory.selectedDeployableServer.publishBuildInfo", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
            param("org.jfrog.artifactory.selectedDeployableServer.urlId", "2")
            param("org.jfrog.artifactory.selectedDeployableServer.envVarsExcludePatterns", "*password*,*secret*")
            param("org.jfrog.artifactory.selectedDeployableServer.resolvingRepo", "modding")
            param("org.jfrog.artifactory.selectedDeployableServer.deployReleaseFlag", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.targetRepo", "libraries")
        }
    }
    steps {
        check(stepsOrder == arrayListOf("RUNNER_85", "RUNNER_9")) {
            "Unexpected build steps order: $stepsOrder"
        }
        stepsOrder = arrayListOf("RUNNER_9")
    }

    features {
        val feature1 = find<VcsLabeling> {
            vcsLabeling {
                id = "BUILD_EXT_11"
                vcsRootId = "${DslContext.settingsRoot.id}"
                labelingPattern = "%env.Version%"
                successfulOnly = true
                branchFilter = ""
            }
        }
        feature1.apply {
            vcsRootId = "LetSDevTogether_General"
        }
    }
}
