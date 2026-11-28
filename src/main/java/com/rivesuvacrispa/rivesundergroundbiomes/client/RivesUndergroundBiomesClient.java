package com.rivesuvacrispa.rivesundergroundbiomes.client;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlockEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.block.BiomeChestBlockEntity;
import com.rivesuvacrispa.rivesundergroundbiomes.entity.JungleSpider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Block;

public class RivesUndergroundBiomesClient implements ClientModInitializer {
    private static final ResourceLocation ANT = RivesUndergroundBiomes.id("textures/entity/ant.png");
    private static final ResourceLocation MUMMY_SKELETON =
            RivesUndergroundBiomes.id("textures/entity/mummy_skeleton.png");
    private static final ResourceLocation FROZEN = RivesUndergroundBiomes.id("textures/entity/frozen.png");
    private static final ResourceLocation JUNGLE_SPIDER =
            RivesUndergroundBiomes.id("textures/entity/jungle_spider.png");
    private static final ResourceLocation PRISMARINE_CRYSTAL_ORE = RivesUndergroundBiomes.id("prismarine_crystal_ore");
    private static final ResourceLocation PRISMARINE_CRYSTAL_GLOW =
            RivesUndergroundBiomes.id("block/prismarine_crystal_ore_emissive");

    @Override
    public void onInitializeClient() {
        ModBlocks.decorations().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(), ModBlocks.THIN_ICE);
        ModBlocks.prismarineBuds().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PRISMARINE_CRYSTAL_ORE, RenderType.cutoutMipped());
        ModelLoadingPlugin.register(plugin -> plugin.modifyModelAfterBake().register((model, context) -> {
            boolean ore = context.id() instanceof ModelResourceLocation id
                    && id.getNamespace().equals(PRISMARINE_CRYSTAL_ORE.getNamespace())
                    && id.getPath().equals(PRISMARINE_CRYSTAL_ORE.getPath());
            return ore && RendererAccess.INSTANCE.hasRenderer()
                    ? new GlowLayerModel(model, PRISMARINE_CRYSTAL_GLOW, RendererAccess.INSTANCE.getRenderer())
                    : model;
        }));

        BlockEntityRenderers.register(ModBlockEntities.BIOME_CHEST, ChestRenderer::new);
        for (Block chest : ModBlocks.chests()) {
            BiomeChestBlockEntity display = new BiomeChestBlockEntity(BlockPos.ZERO, chest.defaultBlockState());
            BuiltinItemRendererRegistry.INSTANCE.register(
                    chest,
                    (stack, mode, poseStack, buffers, light, overlay) -> Minecraft.getInstance()
                            .getBlockEntityRenderDispatcher()
                            .renderItem(display, poseStack, buffers, light, overlay));
        }

        EntityRendererRegistry.register(ModEntities.ANT, context -> new SilverfishRenderer(context) {
            @Override
            public ResourceLocation getTextureLocation(Silverfish entity) {
                return ANT;
            }
        });
        EntityRendererRegistry.register(ModEntities.MUMMY_SKELETON, context -> new SkeletonRenderer(context) {
            @Override
            public ResourceLocation getTextureLocation(AbstractSkeleton entity) {
                return MUMMY_SKELETON;
            }
        });
        EntityRendererRegistry.register(ModEntities.FROZEN, context -> new ZombieRenderer(context) {
            @Override
            public ResourceLocation getTextureLocation(Zombie entity) {
                return FROZEN;
            }
        });
        EntityRendererRegistry.register(
                ModEntities.JUNGLE_SPIDER, context -> new SpiderRenderer<JungleSpider>(context) {
                    @Override
                    public ResourceLocation getTextureLocation(JungleSpider entity) {
                        return JUNGLE_SPIDER;
                    }
                });
        EntityModelLayerRegistry.registerModelLayer(JungleSlimeRenderer.SPIKES, JungleSlimeRenderer::createSpikesLayer);
        EntityRendererRegistry.register(ModEntities.JUNGLE_SLIME, JungleSlimeRenderer::new);
    }
}
