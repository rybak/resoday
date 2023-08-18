import org.ajoberstar.grgit.Grgit
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
	id("java")
	alias(libs.plugins.grgit)
}

tasks.withType<JavaCompile>().configureEach {
	options.apply {
		encoding = "UTF-8"
		release.set(17) // don't forget to update README.md
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// when adding new dependencies, please update task processResources
	implementation(libs.directories) // https://github.com/dirs-dev/directories-jvm
	implementation(libs.gson)

	testImplementation(platform(libs.junitBom))
	testImplementation(libs.junitJupiter)
	testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.named<Test>("test") {
	useJUnitPlatform()

	testLogging { // add stdout logging for running `./gradlew test`
		lifecycle {
			events("passed", "skipped", "failed")
			exceptionFormat = TestExceptionFormat.FULL
		}
	}

	afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ description, result ->
		if (description.parent == null) {
			val stats: String = "${result.testCount} tests run, ${result.successfulTestCount} successes, " +
				"${result.failedTestCount} failures, ${result.skippedTestCount} ignored"
			println("-".repeat(stats.length))
			println("Testing result for ${project.name}: ${result.resultType}")
			println(stats)
			println("-".repeat(stats.length))
		}
	}))

	reports {
		junitXml.required.set(true)
		html.required.set(true) // see ./build/reports/tests/test/index.html
	}
}

fun Process.waitForOrKill(millis: Long) {
	if (!this.waitFor(millis, TimeUnit.MILLISECONDS)) {
		this.destroy()
	}
}

/**
 * Generate a version string with a lot of information.
 * Logic is based on a Shell script used for generating version string of Git:
 * https://git.kernel.org/pub/scm/git/git.git/tree/GIT-VERSION-GEN
 * TODO maybe generate Version.java from Gradle?
 */
fun calculateVersion(): String {
	val defaultVersion: String = "1.6-nongit"
	try {
		val git: Grgit = Grgit.open(mapOf("dir" to project.rootDir))
		/*
		 * If possible, use an annotated tag which starts with letter 'v' and some numbers.
		 */
		val description: String? = git.describe(mapOf("match" to listOf("v[0-9]*"), "commit" to "HEAD"))
		if (description != null && description.matches(Regex("^v[0-9]+[^ ]*"))) {
			val updateIndex: Process = ProcessBuilder("git", "update-index", "-q", "--refresh").start()
			updateIndex.waitForOrKill(10000)
			val diffIndex: Process = ProcessBuilder("git", "diff-index", "--name-only", "HEAD", "--", ".").start()
			val outputIsEmpty: Boolean = diffIndex.inputReader().read() == -1
			val version = description.substring(1) // cut off initial 'v'
			if (!outputIsEmpty) {
				return "$version-dirty"
			}
			return version
		}
	} catch (e: Exception) {
		logger.warn("Could not use Git.", e)
	}
	return defaultVersion
}

/**
 * Calculate who has built the artifacts.
 */
fun calculateResodayBuilderName(): String {
	try {
		val gitUserName: Process = ProcessBuilder("git", "config", "user.name").start()
		gitUserName.waitForOrKill(10000)
		return gitUserName.inputReader().readLine()
	} catch (ignored: Exception) {
		return System.getProperty("user.name")
	}
}

project.version = calculateVersion()

tasks.named<Copy>("processResources") {
	filesMatching("**/about.html") {
		expand(mapOf("version" to project.version))
	}
	filesMatching("**/third-party-software.html") {
		expand(mapOf(
			"directoriesVersion" to libs.directories.get().version,
			"gsonVersion" to libs.gson.get().version,
			"gradleVersion" to rootProject.gradle.gradleVersion,
			"junitVersion" to libs.junitBom.get().version,
			"grgitVersion" to libs.plugins.grgit.get().version,
		))
	}
}

val resodayJarAttributes: Map<String, Any> = mapOf(
	"Implementation-Title" to "Resoday built by " + calculateResodayBuilderName(),
	"Implementation-Version" to project.version,
	"Main-Class" to "dev.andrybak.resoday.Resoday"
)

val jarTask: TaskProvider<Jar> = tasks.named<Jar>("jar") {
	manifest {
		attributes(resodayJarAttributes)
	}
}

tasks.register<Jar>("releaseJar") {
	group = "release"
	description = "Create a release jar of Resoday"

	archiveBaseName.set("resoday")
	archiveAppendix.set("release")

	destinationDirectory.set(file("build/distributions/"))

	manifest {
		attributes(resodayJarAttributes)
	}

	with(jarTask.get() as CopySpec)
	from(configurations.runtimeClasspath.get().map {
		if (it.isDirectory()) it else zipTree(it)
	})
}

tasks.register("release") {
	group = "release"
	description = "Run all tests and create a release jar"
	dependsOn("build", "releaseJar")
}

tasks.named<Wrapper>("wrapper") {
	distributionType = Wrapper.DistributionType.ALL
}
