# IMPORTANT LICENSE NOTICE

By using this project in any form, you hereby give your "express assent" for the terms of the license of this project (see [License](#license)), and acknowledge that I (the author of this project) have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License".

# Zume

A simple zoom mod for Fabric.

This mod adds a keybind which zooms in your FOV while it's held down, allowing you to see further away, and keybinds for increasing and decreasing the zoom level.

# FAQ

#### Q: What version is this for?

A: According to my testing, this should support just about any Minecraft version supported by Fabric itself, from 14.4 (earlier versions lacked a proper key bind API) to the latest snapshot (as of writing).

#### Q: Why Fabric?

A: I'm used to Fabric, not Forge. Additionally use of Fabric is what makes this mod so universally compatible with Minecraft. I have no intention as of now to port to Forge/NeoForge, and it should work fine on Quilt so long as their Fabric compatibility continues to work.

#### Q: Where is the config?

A: You'll find the config at `.minecraft/config/zume.json5`. You need to re-launch the game after modifying it for your changes to apply. I plan to eventually add some way of reloading this config file and modifying it in game at some point in the future (potentially a UI, maybe some client-side commands), but for now this is what is supported.

#### Q: What kind of weird license is this?

A: OSL-3.0 is the closest equivalent to a LAGPL I could find. AGPL and GPL are incompatible with Minecraft, and LGPL doesn't protect network use. OSL-3.0 protects network use and is compatible with Minecraft.

#### Q: Why though? It's so strict!!!!

A: This is, and will remain, free, copyleft software. Any requests to change the license other than to make it even stronger will be denied immediately (unfortunately GPL and AGPL aren't compatible with Minecraft due to linking restrictions, as much as I'd like to use them). Even in situations where I use parts of other projects with more "permissive" licenses, I will treat them as copyleft, free software.

#### Q: discord where
A: https://discord.gg/6ZjX4mvCMR

## License

This project is licensed under OSL-3.0. For more information, see [LICENSE](LICENSE).
