# IMPORTANT LICENSE NOTICE

By using this project in any form, you hereby give your "express assent" for the terms of the license of this 
project (see [License](#license)), and acknowledge that I (the author of this project) have fulfilled my obligation 
under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to 
the terms of this License".

# Zume

An ~~over-engineered~~ simple zoom mod.

This mod adds a keybind which zooms in your FOV while it's held down, allowing you to see further away, and keybinds 
for increasing and decreasing the zoom level.

# FAQ

#### Q: Where is the config?

A: You'll find the config at `.minecraft/config/zume.json5`. You can modify the file while the game is running, and
the config will be automatically reloaded.

#### Q: discord where
A: https://discord.gg/6ZjX4mvCMR

#### Q: What version is this for?

A: Zume supports the following platforms:

- Fabric: Any version supported by Fabric Keybinding API v1 (currently 14.4+)
- NeoForge: 20.4+ (requires NeoForge 20.4.195+)
- LexForge: 14.4 - 20.5 (requires MixinBootstrap before 16.1) (there are currently no plans to support 20.6+)
- Legacy Fabric: Any version supported by Legacy Fabric Keybinding API v1 (currently 7.10 - 12.2)
- Babric (Fabric for Minecraft Beta): Any version supported by Station API (currently b7.3)
- Vintage Forge: 8.9 - 12.2 (requires MixinBooter or UniMixins)
- Archaic Forge: 7.10 (requires UniMixins)

#### Q: Can you add support for \<insert platform here>?

A: Every platform I intend to add support for myself is already supported. PRs are welcome for other platforms **if 
the following conditions are met**:

- Must not break single-jar compatibility with any already supported platform (obviously).
- Must not be for a platform that has a 1st-party compatibility layer for an already supported platform - explicit 
  Quilt support will not be accepted so long as Quilt maintains a Fabric compatibility layer; it'd be a waste of CI 
  time. [Sinytra Connector](https://github.com/Sinytra/Connector) is a 3rd-party compatibility layer, so explicit 
  Forge support will be provided.
- Must not manually maintain overridden game options; implementations that look like 
  [Mooz's](https://github.com/embeddedt/Mooz/blob/92570f7449a7e71c1c0b988788027b10c00f1346/src/main/java/org/embeddedt/mooz/ClientProxy.java#L35-L56)
  will not be accepted - no offense [embeddedt](https://github.com/embeddedt). Direct ASM is fine as long as functionality is similar enough.
- Must make a reasonable effort to be maximize compatibility with existing mods on target platforms - see use of 
  Neo/LexForge API over mixins in Neo/LexForge implementations, and use of `@WrapWithCondition` and 
  `@ModifyExpressionValue` and such from MixinExtras over `@Redirect` in most implementations.
- Must follow existing format - add a Unimined subproject for each newly supported platform.
- Must not have exclusive features without significant justification - if you're adding a feature, add it to every 
  version.

#### Q: What kind of weird license is this?

A: OSL-3.0 is the closest equivalent to a LAGPL I could find. AGPL and GPL are incompatible with Minecraft, and LGPL 
doesn't protect network use. OSL-3.0 protects network use and is compatible with Minecraft.

#### Q: Why though? It's so strict!!!!

A: This is, and will remain, free, copyleft software. Any requests to change the license other than to make it even 
stronger will be denied immediately (unfortunately GPL and AGPL aren't compatible with Minecraft due to linking 
restrictions, as much as I'd like to use them). Even in situations where I use parts of other projects with more 
"permissive" licenses, I will treat them as copyleft, free software.

## License

This project is licensed under OSL-3.0. For more information, see [LICENSE](LICENSE).
