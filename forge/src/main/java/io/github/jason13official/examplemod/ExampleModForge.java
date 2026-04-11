package io.github.jason13official.examplemod;

import java.util.function.Consumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(Constants.MOD_ID)
public class ExampleModForge {

  public static IEventBus EVENT_BUS;

  public ExampleModForge(final FMLJavaModLoadingContext context) {
    EVENT_BUS = context.getModEventBus();

    EVENT_BUS.addListener((Consumer<FMLCommonSetupEvent>) event -> ExampleMod.init());
    
    if (FMLLoader.getDist() == Dist.CLIENT) {
      new ExampleModClientForge(EVENT_BUS);
    }
  }

  @Deprecated
  @SuppressWarnings("all")
  public ExampleModForge() {
    this(FMLJavaModLoadingContext.get());
  }
}