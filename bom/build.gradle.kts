dependencies {
    constraints {
        rootProject.subprojects
            .toList()
            .forEach { api(it) }
    }
}
