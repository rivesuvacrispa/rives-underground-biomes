package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import com.rivesuvacrispa.rivesundergroundbiomes.block.SpeleothemBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class ModelProvider extends FabricModelProvider {
    private static final ModelTemplate CHEST_ITEM = new ModelTemplate(
            Optional.of(new ResourceLocation("minecraft", "item/chest")), Optional.empty(), TextureSlot.PARTICLE);
    private static final ModelTemplate SPELEOTHEM = new ModelTemplate(
            Optional.of(new ResourceLocation("minecraft", "block/pointed_dripstone")),
            Optional.empty(),
            TextureSlot.CROSS);

    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {
        generators.createAxisAlignedPillarBlock(ModBlocks.SANDSLATE, TexturedModel.COLUMN);
        generators.createAxisAlignedPillarBlock(ModBlocks.MUDSTONE, TexturedModel.COLUMN);
        generators.createAxisAlignedPillarBlock(ModBlocks.GLACISLATE, TexturedModel.COLUMN);
        generators.createTrivialCube(ModBlocks.PERMAFROST);
        generators.createTrivialCube(ModBlocks.PEARLSTONE);
        generators.createTrivialCube(ModBlocks.PEARL_SAND);
        generators.createRotatedPillarWithHorizontalVariant(
                ModBlocks.PEARLSTONE_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        generators
                .family(ModBlocks.PERMAFROST_BRICKS)
                .generateFor(new BlockFamily.Builder(ModBlocks.PERMAFROST_BRICKS)
                        .stairs(ModBlocks.PERMAFROST_BRICK_STAIRS)
                        .slab(ModBlocks.PERMAFROST_BRICK_SLAB)
                        .wall(ModBlocks.PERMAFROST_BRICK_WALL)
                        .getFamily());
        generators
                .family(ModBlocks.PEARLSTONE_BRICKS)
                .generateFor(new BlockFamily.Builder(ModBlocks.PEARLSTONE_BRICKS)
                        .stairs(ModBlocks.PEARLSTONE_BRICK_STAIRS)
                        .slab(ModBlocks.PEARLSTONE_BRICK_SLAB)
                        .wall(ModBlocks.PEARLSTONE_BRICK_WALL)
                        .chiseled(ModBlocks.CHISELED_PEARLSTONE)
                        .getFamily());
        generators
                .family(ModBlocks.SMOOTH_PEARLSTONE)
                .generateFor(new BlockFamily.Builder(ModBlocks.SMOOTH_PEARLSTONE)
                        .stairs(ModBlocks.SMOOTH_PEARLSTONE_STAIRS)
                        .slab(ModBlocks.SMOOTH_PEARLSTONE_SLAB)
                        .wall(ModBlocks.SMOOTH_PEARLSTONE_WALL)
                        .getFamily());
        for (Host host : Host.VALUES) {
            for (OreKind kind : OreKind.VALUES) {
                generators.createTrivialCube(ModBlocks.ore(host, kind));
            }
        }

        ModBlocks.speleothems().forEach(block -> speleothem(generators, block));
        ResourceLocation crystal = ModelTemplates.CROSS.create(
                ModBlocks.FROST_CRYSTAL, TextureMapping.cross(ModBlocks.FROST_CRYSTAL), generators.modelOutput);
        generators.blockStateOutput.accept(MultiVariantGenerator.multiVariant(
                        ModBlocks.FROST_CRYSTAL, Variant.variant().with(VariantProperties.MODEL, crystal))
                .with(generators.createColumnWithFacing()));
        generators.createSimpleFlatItemModel(ModBlocks.FROST_CRYSTAL);
        generators.createCrossBlockWithDefaultItem(ModBlocks.GLOWSHROOM, BlockModelGenerators.TintState.NOT_TINTED);
        generators.createCrossBlock(ModBlocks.ANTHILL, BlockModelGenerators.TintState.NOT_TINTED);
        vanillaTextured(generators, ModBlocks.QUICKSAND, "sand");
        vanillaTextured(generators, ModBlocks.THIN_ICE, "ice");
        chest(generators, ModBlocks.DESERT_CHEST, Blocks.CUT_SANDSTONE);
        chest(generators, ModBlocks.JUNGLE_CHEST, Blocks.JUNGLE_PLANKS);
        chest(generators, ModBlocks.FROST_CHEST, ModBlocks.PERMAFROST_BRICKS);
        chest(generators, ModBlocks.PRISMARINE_CHEST, Blocks.PRISMARINE_BRICKS);
        chest(generators, ModBlocks.SPIDER_CHEST, Blocks.OAK_PLANKS);
    }

    // particle only like the vanilla chest
    private static void chest(BlockModelGenerators generators, Block chest, Block particleBlock) {
        TextureMapping particle = TextureMapping.particle(particleBlock);
        ResourceLocation model = ModelTemplates.PARTICLE_ONLY.create(chest, particle, generators.modelOutput);
        generators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(chest, model));
        CHEST_ITEM.create(ModelLocationUtils.getModelLocation(chest.asItem()), particle, generators.modelOutput);
        generators.skipAutoItemBlock(chest);
    }

    private static void vanillaTextured(BlockModelGenerators generators, Block block, String texture) {
        ResourceLocation model = ModelTemplates.CUBE_ALL.create(
                block,
                TextureMapping.cube(new ResourceLocation("minecraft", "block/" + texture)),
                generators.modelOutput);
        generators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, model));
    }

    private static void speleothem(BlockModelGenerators generators, SpeleothemBlock block) {
        PropertyDispatch dispatch = PropertyDispatch.properties(
                        SpeleothemBlock.TIP_DIRECTION, SpeleothemBlock.THICKNESS)
                .generate((tip, thickness) -> {
                    String suffix = "_" + tip.getSerializedName() + "_" + thickness.getSerializedName();
                    ResourceLocation model = SPELEOTHEM.createWithSuffix(
                            block,
                            suffix,
                            TextureMapping.cross(TextureMapping.getBlockTexture(block, suffix)),
                            generators.modelOutput);
                    return Variant.variant().with(VariantProperties.MODEL, model);
                });
        generators.blockStateOutput.accept(
                MultiVariantGenerator.multiVariant(block).with(dispatch));
    }

    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        ModBlocks.speleothems().forEach(block -> generators.generateFlatItem(block.asItem(), ModelTemplates.FLAT_ITEM));
        generators.generateFlatItem(ModBlocks.QUICKSAND_BUCKET, ModelTemplates.FLAT_ITEM);
        ModelTemplate spawnEgg =
                new ModelTemplate(Optional.of(new ResourceLocation("item/template_spawn_egg")), Optional.empty());
        ModEntities.SPAWN_EGGS.forEach(egg -> generators.generateFlatItem(egg, spawnEgg));
    }
}
