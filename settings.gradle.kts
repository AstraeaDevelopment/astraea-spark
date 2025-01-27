pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "legacy-fabric"
			url = uri("https://maven.legacyfabric.net/")
		}
		gradlePluginPortal()
	}
}
