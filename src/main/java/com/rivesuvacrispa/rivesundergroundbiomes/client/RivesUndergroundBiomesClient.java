package com.rivesuvacrispa.rivesundergroundbiomes.client;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.entity.JungleSpider;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Zombie;

public class RivesUndergroundBiomesClient implements ClientModInitializer {
    private static final ResourceLocation ANT = RivesUndergroundBiomes.id("textures/entity/ant.png");
    private static final ResourceLocation MUMMY_SKELETON =
            RivesUndergroundBiomes.id("textures/entity/mummy_skeleton.png");
    private static final ResourceLocation FROZEN = RivesUndergroundBiomes.id("textures/entity/frozen.png");
    private static final ResourceLocation JUNGLE_SPIDER =
            RivesUndergroundBiomes.id("textures/entity/jungle_spider.png");

    @Override
    public void onInitializeClient() {
        ModBlocks.decorations().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.translucent(), ModBlocks.THIN_ICE);

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
