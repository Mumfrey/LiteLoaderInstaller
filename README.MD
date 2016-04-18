![LiteLoaderInstaller Logo](docs/logo.png?raw=true)

**LiteLoaderInstaller** is an installer program for
[LiteLoader](http://liteloader.com) based on the
[Minecraft Forge Installer](https://github.com/MinecraftForge/Installer) but
with visual and internal improvements.

### Features

* Improved User Interface
* Support for system look-and-feel
* Allows installed profile to extend (cascade) from any compatible version
* Automatically merges command-line args from version being extended
* Upgrade existing library in-place

![Screenshot](docs/screenshot.jpg?raw=true)

### Building the project
**LiteLoaderInstaller** uses the [Gradle](http://gradle.org/) build automation
system. It is designed to be built alongside the LiteLoader project, and
requires that the upstream artefact is specified using the `upstreamJar` project
property. The property will be evaluated with `x` being substituted for the
target `project` object, so for example you may use:

```
gradle -P-PupstreamJar='"${x.projectDir}/../LiteLoader/build/libs/liteloader-${x.mcversion}-SNAPSHOT-release.jar"'
```