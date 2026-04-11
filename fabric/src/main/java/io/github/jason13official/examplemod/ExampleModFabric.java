package io.github.jason13official.examplemod;

import io.github.jason13official.examplemod.impl.common.registry.ModBlocks;
import io.github.jason13official.examplemod.impl.common.registry.ModEntities;
import io.github.jason13official.examplemod.impl.common.registry.ModItems;
import io.github.jason13official.examplemod.impl.common.registry.ModMenus;
import io.github.jason13official.examplemod.impl.common.registry.ModParticles;
import io.github.jason13official.examplemod.impl.common.registry.ModTabs;
import io.github.jason13official.examplemod.impl.common.registry.ModTiles;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class ExampleModFabric implements ModInitializer {

  @Override
  public void onInitialize() {

    bind(BuiltInRegistries.BLOCK, ModBlocks::register);
    bind(BuiltInRegistries.ENTITY_TYPE, ModEntities::register);
    bind(BuiltInRegistries.ITEM, ModItems::register);
    bind(BuiltInRegistries.PARTICLE_TYPE, ModParticles::register);
    bind(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModTiles::register);
    bind(BuiltInRegistries.MENU, ModMenus::register);
    bind(BuiltInRegistries.CREATIVE_MODE_TAB, ModTabs::register);

    ExampleMod.init();
  }

  public <T> void bind(Registry<T> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {

    source.accept((t, rl) -> Registry.register(registry, rl, t));
  }
}
