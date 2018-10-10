# Shockbytes Plugin for IntelliJ / Android Studio

Personal productivity enhancing IntelliJ/Android Studio plugin.
For now the plugin includes utility methods for Android development,
Gradle dependency management, Workspace crawling (searching your
workspace) and screen capture methods. 

Screengrab support and a general Fastlane support is planned in a later
release.

## Versions

### 4.2
- [ ] Resolve Google dependencies
- [ ] Screengrab support
- [ ] Show current version somewhere
- [ ] Use Dagger for dependency injection

### 4.1
- [x] Store updated dependencies in gradle_dependencies.json
- [x] Changer dagger generated classes to Kotlin
- [ ] Supply/Fastlane support via PlayStoreWorker

# 4.0
* Decouple views from worker implementation

### 3.0
* Android Utilities
* Workspace Crawler
* Android Screen Capturing
* Android Gradle dependency management

## Troubleshooting

### Problems with Maven
Updating dependencies in the `pom.xml` file can cause some errors
in the `plugin.iml` file. Apply these steps to fix the build configuration:
1. Change in line 2 the tpe from `JAVA_MODULE` to `PLUGIN_MODULE`.
2. Add directly below the line `<component name="DevKit.ModuleBuildProperties" url="file://$MODULE_DIR$/resources/META-INF/plugin.xml" />`.
3. Mark the `/test` folder as test root folder.
4. Mark the `/resources` folder as resources root folder. Alternatively one can add the line `<sourceFolder url="file://$MODULE_DIR$/resources" type="java-resource" />` in the `<content>` body. 

If all of this still does not help, then change the Sandbox to a new IntelliJ instance (Project Structure -> SDKs -> Sandbox Home)
