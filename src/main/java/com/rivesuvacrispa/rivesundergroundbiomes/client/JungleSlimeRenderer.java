package com.rivesuvacrispa.rivesundergroundbiomes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

import java.util.EnumSet;
import java.util.Set;

public class JungleSlimeRenderer extends SlimeRenderer {
    public static final ModelLayerLocation SPIKES =
            new ModelLayerLocation(RivesUndergroundBiomes.id("jungle_slime"), "spikes");
    private static final ResourceLocation TEXTURE = RivesUndergroundBiomes.id("textures/entity/jungle_slime.png");
    private static final ResourceLocation SPIKES_TEXTURE =
            RivesUndergroundBiomes.id("textures/entity/jungle_slime_spikes.png");

    public JungleSlimeRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new SpikesLayer(this, context.bakeLayer(SPIKES)));
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        return TEXTURE;
    }

    private static final Set<Direction> WEST = EnumSet.of(Direction.WEST);
    private static final Set<Direction> UP = EnumSet.of(Direction.UP);
    private static final Set<Direction> NORTH = EnumSet.of(Direction.NORTH);

    // each plane has one face only and the no cull render type shows it from both sides
    // two coplanar faces with opposite normals z-fight with different shading
    // which drew dark lines on the horizontal planes
    public static LayerDefinition createSpikesLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "x_planes",
                CubeListBuilder.create()
                        .texOffs(0, -10)
                        .addBox(-1.5F, 15.0F, -5.0F, 0.0F, 10.0F, 10.0F, WEST)
                        .addBox(1.5F, 15.0F, -5.0F, 0.0F, 10.0F, 10.0F, WEST),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "y_planes",
                CubeListBuilder.create()
                        .texOffs(-20, 0)
                        .addBox(-5.0F, 18.5F, -5.0F, 10.0F, 0.0F, 10.0F, UP)
                        .addBox(-5.0F, 21.5F, -5.0F, 10.0F, 0.0F, 10.0F, UP),
                PartPose.ZERO);
        root.addOrReplaceChild(
                "z_planes",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, 15.0F, -1.5F, 10.0F, 10.0F, 0.0F, NORTH)
                        .addBox(-5.0F, 15.0F, 1.5F, 10.0F, 10.0F, 0.0F, NORTH),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 10, 10);
    }

    private static class SpikesLayer extends RenderLayer<Slime, SlimeModel<Slime>> {
        private final ModelPart spikes;

        SpikesLayer(RenderLayerParent<Slime, SlimeModel<Slime>> parent, ModelPart spikes) {
            super(parent);
            this.spikes = spikes;
        }

        @Override
        public void render(
                PoseStack poseStack,
                MultiBufferSource buffers,
                int light,
                Slime slime,
                float limbSwing,
                float limbSwingAmount,
                float partialTick,
                float ageInTicks,
                float netHeadYaw,
                float headPitch) {
            if (slime.isInvisible()) return;
            spikes.render(
                    poseStack,
                    buffers.getBuffer(RenderType.entityCutoutNoCull(SPIKES_TEXTURE)),
                    light,
                    LivingEntityRenderer.getOverlayCoords(slime, 0.0F));
        }
    }
}
