package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BiomeProvider extends FabricDynamicRegistryProvider {
    public BiomeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(Registries.CONFIGURED_FEATURE));
        entries.addAll(registries.lookupOrThrow(Registries.PLACED_FEATURE));
        entries.addAll(registries.lookupOrThrow(Registries.BIOME));
    }

    @Override
    public String getName() {
        return "Underground biomes and features";
    }

    // features use the same BiomeDefaultFeatures calls in vanilla order
    // any other order crashes with a feature order cycle
    public static void bootstrap(BootstapContext<Biome> context) {
        context.register(Zone.DESERT.biome, undergroundDesert(context));
        context.register(Zone.JUNGLE.biome, undergroundJungle(context));
        context.register(Zone.ICE.biome, iceCaves(context));
        context.register(Zone.REEF.biome, reef(context, 0x2FC9E0, 0x062A3A));
    }

    private static Biome undergroundDesert(BootstapContext<Biome> context) {
        MobSpawnSettings.Builder spawns = spawns(EntityType.HUSK, ModEntities.MUMMY_SKELETON, EntityType.SPIDER);
        BiomeGenerationSettings.Builder generation = generation(context);
        BiomeDefaultFeatures.addFossilDecoration(generation);
        globalOverworldGeneration(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        ModWorldgen.addUndergroundDesert(generation);
        return biome(
                false,
                2.0F,
                0.0F,
                0x9CA88A,
                0x2B2818,
                spawns,
                generation,
                SoundEvents.MUSIC_BIOME_DESERT,
                new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.002F));
    }

    private static Biome undergroundJungle(BootstapContext<Biome> context) {
        MobSpawnSettings.Builder spawns = spawns(EntityType.ZOMBIE, EntityType.SKELETON, ModEntities.JUNGLE_SPIDER);
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(ModEntities.JUNGLE_SLIME, 50, 1, 3));
        BiomeGenerationSettings.Builder generation = generation(context);
        globalOverworldGeneration(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        ModWorldgen.addUndergroundJungle(generation);
        return biome(
                true,
                0.95F,
                0.9F,
                0x3E9C7C,
                0x0B2A1A,
                spawns,
                generation,
                SoundEvents.MUSIC_BIOME_LUSH_CAVES,
                new AmbientParticleSettings(ParticleTypes.SPORE_BLOSSOM_AIR, 0.003F));
    }

    private static Biome iceCaves(BootstapContext<Biome> context) {
        MobSpawnSettings.Builder spawns = spawns(ModEntities.FROZEN, EntityType.STRAY, EntityType.SPIDER);
        BiomeGenerationSettings.Builder generation = generation(context);
        globalOverworldGeneration(generation);
        BiomeDefaultFeatures.addDefaultOres(generation);
        BiomeDefaultFeatures.addDefaultSoftDisks(generation);
        ModWorldgen.addIceCaves(generation);
        return biome(
                true,
                -0.5F,
                0.5F,
                0x8FC8F0,
                0x1E4A6E,
                spawns,
                generation,
                SoundEvents.MUSIC_BIOME_FROZEN_PEAKS,
                new AmbientParticleSettings(ParticleTypes.SNOWFLAKE, 0.004F));
    }

    private static Biome reef(BootstapContext<Biome> context, int waterColor, int waterFogColor) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        spawns.addSpawn(
                MobCategory.UNDERGROUND_WATER_CREATURE,
                new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 4, 6));
        spawns.addSpawn(
                MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
        spawns.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
        BiomeGenerationSettings.Builder generation = generation(context);
        BiomeDefaultFeatures.addDefaultOres(generation);
        ModWorldgen.addReef(generation);
        return biome(
                true,
                0.5F,
                0.5F,
                waterColor,
                waterFogColor,
                spawns,
                generation,
                SoundEvents.MUSIC_BIOME_LUSH_CAVES,
                null);
    }

    private static MobSpawnSettings.Builder spawns(EntityType<?> zombie, EntityType<?> skeleton, EntityType<?> spider) {
        MobSpawnSettings.Builder spawns = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.caveSpawns(spawns);
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(spider, 100, 4, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(zombie, 100, 4, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(skeleton, 100, 4, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 100, 4, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 100, 4, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
        spawns.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
        return spawns;
    }

    private static BiomeGenerationSettings.Builder generation(BootstapContext<Biome> context) {
        return new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
    }

    // copy of the private OverworldBiomes.globalOverworldGeneration
    private static void globalOverworldGeneration(BiomeGenerationSettings.Builder generation) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(generation);
        BiomeDefaultFeatures.addDefaultCrystalFormations(generation);
        // custom dungeons replace vanilla's
        // BiomeDefaultFeatures.addDefaultMonsterRoom(generation);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(generation);
        BiomeDefaultFeatures.addDefaultSprings(generation);
        BiomeDefaultFeatures.addSurfaceFreezing(generation);
    }

    private static Biome biome(
            boolean precipitation,
            float temperature,
            float downfall,
            int waterColor,
            int waterFogColor,
            MobSpawnSettings.Builder spawns,
            BiomeGenerationSettings.Builder generation,
            Holder<SoundEvent> music,
            @Nullable AmbientParticleSettings particles) {
        Music backgroundMusic = Musics.createGameMusic(music);
        BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .waterColor(waterColor)
                .waterFogColor(waterFogColor)
                .fogColor(12638463)
                .skyColor(skyColor(temperature))
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .backgroundMusic(backgroundMusic);
        if (particles != null) effects.ambientParticle(particles);
        return new Biome.BiomeBuilder()
                .hasPrecipitation(precipitation)
                .temperature(temperature)
                .downfall(downfall)
                .specialEffects(effects.build())
                .mobSpawnSettings(spawns.build())
                .generationSettings(generation.build())
                .build();
    }

    // copy of the private OverworldBiomes.calculateSkyColor
    private static int skyColor(float temperature) {
        float t = Mth.clamp(temperature / 3.0F, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - t * 0.05F, 0.5F + t * 0.1F, 1.0F);
    }
}
