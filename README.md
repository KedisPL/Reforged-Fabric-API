## DISCONTINUED!
## Use Forgified Fabric API if you want the same thing. It's better than my port.

# What is Reforged Fabric API?
It's a core library, which adds features for mod developers to make forge mods easier and also to port Fabric mods to Forge easier.

Example features:
- Biome API - With that you can modify vanilla biomes or add new End or Nether biomes.
- You can register entities, features, etc. without using Forge Registries. (Doesn't work with every type of registry!)
- Compatibility with many mods.
- And so much more!
 
# Other information:
This is a port of Fabric API made by FabricMC to Forge mod loader.
This is still in alpha so expect bugs.

## How to use Reforged Fabric API to develop mods with Forge MDK?

To set up a Forge development environment, check out the [Forge MDK](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) and follow the instructions there.

To include Reforged Fabric API in the development environment, add the following to your `dependencies` block in the gradle buildscript:

```
implementation fg.deobf("curse.maven:reforged-fabric-api-872386:4721584")
```


Then add the folowing to your `build.gradle` file:

```
repositories {
    maven {
        url "https://cursemaven.com"
    }
}
```
