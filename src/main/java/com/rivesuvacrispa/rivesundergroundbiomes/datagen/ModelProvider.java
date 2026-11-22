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
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class ModelProvider extends FabricModelProvider {
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
        generators
                .family(ModBlocks.PERMAFROST_BRICKS)
                .generateFor(new BlockFamily.Builder(ModBlocks.PERMAFROST_BRICKS)
                        .stairs(ModBlocks.PERMAFROST_BRICK_STAIRS)
                        .slab(ModBlocks.PERMAFROST_BRICK_SLAB)
                        .wall(ModBlocks.PERMAFROST_BRICK_WALL)
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
    }

    private static void vanillaTextured(
            BlockModelGenerators generators, net.minecraft.world.level.block.Block block, String texture) {
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
