- migrate to Kotlin buildscript (I hate Kotlin but I hate Groovy more)
- migrate to Unimined (adding new platforms should be way easier now)
- add support for LexForge 14.4+ (this required 3 separate implementations :harold:)
- add support for NeoForge 20.1+
- remove hard dependency on MixinBooter in Vintage Forge; Zume just verifies that mixins get loaded now, it doesn't 
  care how