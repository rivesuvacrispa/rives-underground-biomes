package com.rivesuvacrispa.rivesundergroundbiomes;

import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.AttachedBlockFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.BeeNestFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.BuriedRelicFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.DungeonFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.GiantHiveFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.JungleShaftFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.MossCoatFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.NoiseNetFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.RibCageFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.RootTangleFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.SpeleothemFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.SpiderNestFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.UnderwaterGardenFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.VineCurtainFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public final class ModFeatures {
    public static final SpeleothemFeature SPELEOTHEM = register("speleothem", new SpeleothemFeature());
    public static final AttachedBlockFeature ATTACHED_BLOCK = register("attached_block", new AttachedBlockFeature());
    public static final RootTangleFeature ROOT_TANGLE = register("root_tangle", new RootTangleFeature());
    public static final BeeNestFeature BEE_NEST = register("bee_nest", new BeeNestFeature());
    public static final GiantHiveFeature GIANT_HIVE = register("giant_hive", new GiantHiveFeature());
    public static final SpiderNestFeature SPIDER_NEST = register("spider_nest", new SpiderNestFeature());
    public static final RibCageFeature RIB_CAGE = register("rib_cage", new RibCageFeature());
    public static final BuriedRelicFeature BURIED_RELIC = register("buried_relic", new BuriedRelicFeature());
    public static final NoiseNetFeature NOISE_NET = register("noise_net", new NoiseNetFeature());
    public static final MossCoatFeature MOSS_COAT = register("moss_coat", new MossCoatFeature());
    public static final VineCurtainFeature VINE_CURTAIN = register("vine_curtain", new VineCurtainFeature());
    public static final UnderwaterGardenFeature UNDERWATER_GARDEN =
            register("underwater_garden", new UnderwaterGardenFeature());
    public static final DungeonFeature DUNGEON = register("dungeon", new DungeonFeature());
    public static final JungleShaftFeature JUNGLE_SHAFT = register("jungle_shaft", new JungleShaftFeature());

    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String name, F feature) {
        return Registry.register(BuiltInRegistries.FEATURE, RivesUndergroundBiomes.id(name), feature);
    }

    // loads the class so the static fields register
    public static void init() {}

    private ModFeatures() {}
}
