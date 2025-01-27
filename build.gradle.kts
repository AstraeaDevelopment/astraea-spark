plugins {
	id("fabric-loom") version "1.9-SNAPSHOT"
	id("legacy-looming") version "1.9-SNAPSHOT" // Version must be the same as fabric-loom's
}

base {
	archivesName = properties["archives_base_name"] as String
	version = properties["mod_version"] as String
	group = properties["maven_group"] as String
}

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
}

legacyLooming {
	// 	The generation of intermediary to use, default is 1.
	// 	legacy.yarn() will automatically point at the variant for the set intermediary generation.
	// 	However do not forget to set the right build number of legacy yarn as they are different per variant.
	//	intermediaryVersion = 2

	// 	Wether to use Legacy Fabric intermediaries or Upstream/Official FabricMC ones.
	//	Default to true: use Legacy Fabric intermediaries.
	// 	If setting to false, don't forget to also set intermediaryVersion to generation 2.
	//	useLFIntermediary.set(false)
}

dependencies {
	minecraft("com.mojang:minecraft:${properties["minecraft_version"] as String}")
	mappings(legacy.yarn(properties["minecraft_version"] as String, properties["yarn_build"] as String))
	modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"] as String}")

	// Legacy Fabric API provides hooks for events, item registration, and more. As most mods will need this, it's included by default.
	// If you know for a fact you don't, it's not required and can be safely removed.
	// As of September 2024, available for MC 1.6.4, 1.7.10, 1.8, 1.8.9, 1.9.4, 1.10.2, 1.11.2 and 1.12.2.
	// As of September 2024, available only for intermediary generation 1.
	modImplementation("net.legacyfabric.legacy-fabric-api:legacy-fabric-api:${properties["fabric_version"] as String}")

	// You can retrieve a specific api module using this notation.
	// modImplementation(legacy.apiModule("legacy-fabric-item-groups-v1", properties["fabric_version"] as String))
}

tasks {
	processResources {
		val properties = mapOf(
			"version" to project.version
		)

		filteringCharset = "UTF-8"

		inputs.properties(properties)

		filesMatching("fabric.mod.json") {
			expand(properties)
		}
	}

	withType<JavaCompile> {
		// Ensure that the encoding is set to UTF-8, no matter what the system default is
		// this fixes some edge cases with special characters not displaying correctly
		// see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
		options.encoding = "UTF-8"
		options.release = 8
	}

	java {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8

		// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
		// if it is present.
		// If you remove this line, sources will not be generated.
		withSourcesJar()
	}

	jar {
		val projectIdentifier = project.base.archivesName.get()
		from("LICENSE") {
			rename { "${it}_${projectIdentifier}" }
		}
	}
}
