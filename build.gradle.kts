import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
	id("java")
	id("resoday-build.version-convention")
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
			"grgitVersion" to libs.grgit.get().version,
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
