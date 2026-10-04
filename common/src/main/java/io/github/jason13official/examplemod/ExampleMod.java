package io.github.jason13official.examplemod;

import net.minecraft.resources.ResourceLocation;

public class ExampleMod {

  public static void init() {

    // Sailing.register(Constants.MOD_ID, MonoLib.createFilename(Constants.MOD_ID, "1.21.1", "1.0.0"));
  }

  public static ResourceLocation identifier(final String path) {
    return new ResourceLocation(Constants.MOD_ID, path);
  }
}