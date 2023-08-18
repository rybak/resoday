import org.ajoberstar.grgit.Grgit

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
	val defaultVersion: String = "1.7-nongit"
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

project.version = calculateVersion()
