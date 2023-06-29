# What is Reforged Fabric API?
It's a core library, which adds features for mod developers to make forge mods easier.
Example features:
- Biome API - With that you can modify vanilla biomes or add new End or Nether biomes.
- You can register entities, features, etc. without using Forge Registries.
- Compatibility with many mods.
 
# Other information:
This is a port of Fabric API made by FabricMC to Forge mod loader.
This is still in alpha so expect bugs.

## How to use Reforged Fabric API to develop mods?

To set up a Forge development environment, check out the [Forge MDK](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) and follow the instructions there.

To include Reforged Fabric API in the development environment, add the following to your `dependencies` block in the gradle buildscript:

```
modImplementation "curse.maven:reforged-fabric-api-FILE_ID:4613629"
```
`FILE_ID` You can find it in the mod file url link. It's the numbers at the end of url.

Then add the folowing to your `settings.gradle` file:

```
repositories {
    maven {
        url "https://cursemaven.com"
    }
}
```
