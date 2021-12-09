import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.vcsLabeling
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.GradleBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.githubIssues
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.2"

project {
    description = "The Minecolonies Minecraft Mod"

    params {
        param("Current Minecraft Version", "main")
        text("Repository", "ldtteam/minecolonies", label = "Repository", description = "The repository for minecolonies.", readOnly = true, allowEmpty = true)
        param("env.Version.Minor", "0")
        param("Project.Type", "mods")
        param("env.Version.Patch", "0")
        param("Upsource.Project.Id", "minecolonies")
        param("env.Version.Suffix", "")
        param("env.Version.Major", "1")
        param("Default.Branch", "version/main")
        param("filename.prefix", "minecolonies")
        param("env.GRADLE_VERSION", "7.2")
        param("env.JDK_VERSION", "jdk16")
        text("env.Version", "%env.Version.Major%.%env.Version.Minor%.%env.Version.Patch%%env.Version.Suffix%", label = "Version", description = "The version of the project.", display = ParameterDisplay.HIDDEN, allowEmpty = true)
    }

    features {
        githubIssues {
            id = "PROJECT_EXT_22"
            displayName = "ldtteam/minecolonies"
            repositoryURL = "https://github.com/ldtteam/minecolonies"
            authType = accessToken {
                accessToken = "credentialsJSON:47381468-aceb-4992-93c9-1ccd4d7aa67f"
            }
        }
    }
    subProjectsOrder = arrayListOf(RelativeId("Release"), RelativeId("UpgradeBetaRelease"), RelativeId("Beta"), RelativeId("UpgradeAlphaBeta"), RelativeId("Alpha"), RelativeId("OfficialPublications"), RelativeId("Branches"), RelativeId("PullRequests_2"))

    subProject(Alpha)
    subProject(Beta)
    subProject(UpgradeAlphaBeta)
    subProject(OfficialPublications)
    subProject(Branches)
    subProject(Release)
    subProject(UpgradeBetaRelease)
    subProject(PullRequests_2)
}


object Alpha : Project({
    name = "Alpha"
    description = "Alpha version builds of minecolonies"

    buildType(Alpha_Release)

    params {
        text("env.crowdinKey", "credentialsJSON:b0ec3acb-677c-4212-9e76-0fb7fbe265da", label = "Crowdin key", description = "The API key for crowdin to pull translations", allowEmpty = true)
        param("Default.Branch", "version/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/version/(*)")
        param("env.CURSERELEASETYPE", "alpha")
        param("env.Version.Suffix", "-ALPHA")
    }
})

object Alpha_Release : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithRelease"))
    name = "Release"
    description = "Releases the mod as Alpha to CurseForge"

    allowExternalStatus = true

    params {
        param("env.Version.Patch", "${OfficialPublications_CommonB.depParamRefs.buildNumber}")
    }

    steps {
        gradle {
            name = "Compile"
            id = "RUNNER_9"
            tasks = "build createChangelog curseforge publish"
            buildFile = "build.gradle"
            enableStacktrace = true
            dockerImagePlatform = GradleBuildStep.ImagePlatform.Linux
            dockerImage = "gradle:%env.GRADLE_VERSION%-%env.JDK_VERSION%"
            dockerRunParameters = """
                -v /opt/buildagent/gradle/caches:/home/gradle/.gradle/caches
                -u 0
            """.trimIndent()
            param("org.jfrog.artifactory.selectedDeployableServer.deployReleaseText", "%Project.Type%")
            param("org.jfrog.artifactory.selectedDeployableServer.publishBuildInfo", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
            param("org.jfrog.artifactory.selectedDeployableServer.urlId", "2")
            param("org.jfrog.artifactory.selectedDeployableServer.envVarsExcludePatterns", "*password*,*secret*")
            param("org.jfrog.artifactory.selectedDeployableServer.resolvingRepo", "modding")
            param("org.jfrog.artifactory.selectedDeployableServer.deployReleaseFlag", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.targetRepo", "libraries")
        }
        gradle {
            name = "Analyze"
            id = "RUNNER_144"
            tasks = "sonarqube"
            buildFile = "build.gradle"
            gradleParams = "-Dsonar.projectKey=ldtteam_minecolonies -Dsonar.host.url=https://code-analysis.ldtteam.com -Dsonar.login=%sonarqube.token%"
            enableStacktrace = true
            dockerImagePlatform = GradleBuildStep.ImagePlatform.Linux
            dockerImage = "gradle:%env.GRADLE_VERSION%-%env.JDK_VERSION%"
            dockerRunParameters = """
                -v /opt/buildagent/gradle/caches:/home/gradle/.gradle/caches
                -u 0
            """.trimIndent()
            param("org.jfrog.artifactory.selectedDeployableServer.deployReleaseText", "%Project.Type%")
            param("org.jfrog.artifactory.selectedDeployableServer.useM2CompatiblePatterns", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.publishBuildInfo", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
            param("org.jfrog.artifactory.selectedDeployableServer.buildDependencies", "Requires Artifactory Pro.")
            param("org.jfrog.artifactory.selectedDeployableServer.envVarsExcludePatterns", "*password*,*secret*")
            param("org.jfrog.artifactory.selectedDeployableServer.publishMavenDescriptors", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.publishIvyDescriptors", "true")
            param("org.jfrog.artifactory.selectedDeployableServer.deployReleaseFlag", "true")
        }
        stepsOrder = arrayListOf("RUNNER_85", "RUNNER_9", "RUNNER_144")
    }

    features {
        vcsLabeling {
            id = "BUILD_EXT_11"
            vcsRootId = "${DslContext.settingsRoot.id}"
            labelingPattern = "%env.Version%"
            successfulOnly = true
            branchFilter = ""
        }
        commitStatusPublisher {
            id = "BUILD_EXT_15"
            enabled = false
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            publisher = upsource {
                serverUrl = "https://code-analysis.ldtteam.com"
                projectId = "%Upsource.Project.Id%"
                userName = "upsource"
                password = "credentialsJSON:f19631a7-1bc1-4a66-88a0-dc2b9cd36734"
            }
        }
    }

    dependencies {
        snapshot(OfficialPublications_CommonB) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})


object Beta : Project({
    name = "Beta"
    description = "Beta version builds of minecolonies"

    buildType(Beta_Release)

    params {
        password("env.crowdinKey", "credentialsJSON:be67336c-4ed1-464c-b531-92270ba39b53", label = "Crowdin key", description = "The API for getting the crowdin translations")
        param("Default.Branch", "testing/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/testing/(*)")
        param("env.CURSERELEASETYPE", "beta")
        param("env.Version.Suffix", "-BETA")
    }
})

object Beta_Release : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithRelease"))
    name = "Release"
    description = "Releases the mod as Alpha to CurseForge"

    params {
        param("env.Version.Patch", "${OfficialPublications_CommonB.depParamRefs.buildNumber}")
    }

    steps {
        gradle {
            name = "Compile"
            id = "RUNNER_9"
            tasks = "build createChangelog curseforge publish"
            buildFile = "build.gradle"
            enableStacktrace = true
            dockerImagePlatform = GradleBuildStep.ImagePlatform.Linux
            dockerImage = "gradle:%env.GRADLE_VERSION%-%env.JDK_VERSION%"
            dockerRunParameters = """
                -v /opt/buildagent/gradle/caches:/home/gradle/.gradle/caches
                -u 0
            """.trimIndent()
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

    dependencies {
        snapshot(OfficialPublications_CommonB) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})


object Branches : Project({
    name = "Branches"
    description = "All none release branches."

    buildType(Branches_Common)
    buildType(Branches_Build)

    params {
        text("Default.Branch", "version/%Current Minecraft Version%", label = "Default branch", description = "The default branch for branch builds", allowEmpty = true)
        param("VCS.Branches", """
            +:refs/heads/(*)
            -:refs/heads/version/*
            -:refs/heads/testing/*
            -:refs/heads/release/*
            -:refs/pull/*/head
            -:refs/heads/CI/*
        """.trimIndent())
        param("env.Version.Suffix", "-PERSONAL")
    }

    cleanup {
        baseRule {
            all(days = 60)
        }
    }
})

object Branches_Build : BuildType({
    templates(AbsoluteId("LetSDevTogether_Build"))
    name = "Build"
    description = "Builds the branch without testing."

    params {
        param("Project.Type", "mods")
        param("env.Version.Patch", "${Branches_Common.depParamRefs.buildNumber}")
    }

    triggers {
        vcs {
            id = "vcsTrigger"
        }
    }

    dependencies {
        snapshot(Branches_Common) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
    
    disableSettings("BUILD_EXT_14")
})

object Branches_Common : BuildType({
    templates(AbsoluteId("LetSDevTogether_CommonBuildCounter"))
    name = "Common Build Counter"
    description = "Tracks the amount of builds run for branches"
})


object OfficialPublications : Project({
    name = "Official Publications"
    description = "Holds projects and builds related to official publications"

    buildType(OfficialPublications_CommonB)
})

object OfficialPublications_CommonB : BuildType({
    templates(AbsoluteId("LetSDevTogether_CommonBuildCounter"))
    name = "Common Build Counter"
    description = "Represents the version counter within Minecolonies for official releases."
})


object PullRequests_2 : Project({
    name = "Pull Requests"
    description = "All open pull requests"

    buildType(PullRequests_2_BuildAndTest)
    buildType(PullRequests_2_CommonBuildCounter)

    params {
        text("Default.Branch", "version/%Current Minecraft Version%", label = "Default branch", description = "The default branch for pull requests.", allowEmpty = false)
        param("VCS.Branches", """
            -:refs/heads/*
            +:refs/pull/(*)/head
            -:refs/heads/(CI/*)
        """.trimIndent())
        param("env.Version", "%env.Version.Major%.%env.Version.Minor%.%build.counter%-PR")
    }

    cleanup {
        baseRule {
            all(days = 60)
        }
    }
})

object PullRequests_2_BuildAndTest : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithTesting"))
    name = "Build and Test"
    description = "Builds and Tests the pull request."

    artifactRules = """
        +:build\libs\*.jar => build\libs
        +:build\distributions\mods-*.zip => build\distributions
    """.trimIndent()

    params {
        param("env.Version.Patch", "${PullRequests_2_CommonBuildCounter.depParamRefs.buildNumber}")
        param("env.Version.Suffix", "-PR")
    }

    features {
        feature {
            id = "com.ldtteam.teamcity.github.commenting.GithubCommentingBuildFeature"
            type = "com.ldtteam.teamcity.github.commenting.GithubCommentingBuildFeature"
            param("privateKey", "-----")
            param("appId", "154983")
            param("branch", "%teamcity.build.branch%")
        }
    }

    dependencies {
        snapshot(PullRequests_2_CommonBuildCounter) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
    
    disableSettings("BUILD_EXT_15")
})

object PullRequests_2_CommonBuildCounter : BuildType({
    templates(AbsoluteId("LetSDevTogether_CommonBuildCounter"))
    name = "Common Build Counter"
    description = "Defines version numbers uniquely over all Pull Request builds"
})


object Release : Project({
    name = "Release"
    description = "Beta version builds of minecolonies"

    buildType(Release_Release)

    params {
        password("env.crowdinKey", "credentialsJSON:be67336c-4ed1-464c-b531-92270ba39b53", label = "Crowdin key", description = "The API key for getting crowdin translations")
        param("Default.Branch", "release/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/release/(*)")
        param("env.CURSERELEASETYPE", "release")
        param("env.Version.Suffix", "-RELEASE")
    }
})

object Release_Release : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithRelease"))
    name = "Release"
    description = "Releases the mod as Alpha to CurseForge"

    params {
        param("env.Version.Patch", "${OfficialPublications_CommonB.depParamRefs.buildNumber}")
    }

    steps {
        gradle {
            name = "Compile"
            id = "RUNNER_9"
            tasks = "build createChangelog curseforge publish"
            buildFile = "build.gradle"
            useGradleWrapper = false
            enableStacktrace = true
            dockerImagePlatform = GradleBuildStep.ImagePlatform.Linux
            dockerImage = "gradle:%env.GRADLE_VERSION%-%env.JDK_VERSION%"
            dockerRunParameters = "-u 0 -v /opt/buildagent/gradle/caches:/home/gradle/.gradle/caches"
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

    dependencies {
        snapshot(OfficialPublications_CommonB) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})


object UpgradeAlphaBeta : Project({
    name = "Upgrade - Alpha -> Beta"
    description = "Updates the current alpha to beta."

    buildType(Beta_UpgradeAlphaBeta)
})

object Beta_UpgradeAlphaBeta : BuildType({
    templates(AbsoluteId("LetSDevTogether_Upgrade"))
    name = "Upgrade - Alpha -> Beta"
    description = "Upgrades the current Alpha to Beta."

    params {
        param("Source.Branch", "version")
        param("Default.Branch", "testing/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/testing/(*)")
        param("Target.Branch", "testing")
        param("env.Version", "%env.Version.Major%.%env.Version.Minor%.%build.counter%-BETA")
    }

    triggers {
        schedule {
            id = "TRIGGER_1"
            schedulingPolicy = weekly {
                timezone = "Europe/Berlin"
            }
            triggerBuild = always()
            param("revisionRuleBuildBranch", "<default>")
        }
    }
    
    disableSettings("BUILD_EXT_9")
})


object UpgradeBetaRelease : Project({
    name = "Upgrade Beta -> Release"
    description = "Upgrades the current Beta to Release"

    buildType(UpgradeBetaRelease_UpgradeBetaRelease)
})

object UpgradeBetaRelease_UpgradeBetaRelease : BuildType({
    templates(AbsoluteId("LetSDevTogether_Upgrade"))
    name = "Upgrade Beta -> Release"
    description = "Upgrades the current Beta to Release."

    params {
        param("Source.Branch", "testing")
        param("Default.Branch", "release/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/release/(*)")
        param("Target.Branch", "release")
        param("env.Version", "%env.Version.Major%.%env.Version.Minor%.%build.counter%-RELEASE")
    }
})
