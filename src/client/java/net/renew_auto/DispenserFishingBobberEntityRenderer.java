package net.renew_auto;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class DispenserFishingBobberEntityRenderer extends EntityRenderer<DispenserFishingBobberEntity> {
   private static final Identifier TEXTURE = new Identifier("textures/entity/fishing_hook.png");
   private static final RenderLayer LAYER;

   public DispenserFishingBobberEntityRenderer(EntityRendererFactory.Context context) {
      super(context);
   }

   @Override
   public void render(DispenserFishingBobberEntity fishingBobberEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light) {
      matrixStack.push();
      matrixStack.push();
      matrixStack.scale(0.5F, 0.5F, 0.5F);
      matrixStack.multiply(this.dispatcher.getRotation());
      matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
      MatrixStack.Entry entry = matrixStack.peek();
      Matrix4f matrix4f = entry.getPositionMatrix();
      Matrix3f matrix3f = entry.getNormalMatrix();
      VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
      vertex(vertexConsumer, matrix4f, matrix3f, light, 0.0F, 0, 0, 1);
      vertex(vertexConsumer, matrix4f, matrix3f, light, 1.0F, 0, 1, 1);
      vertex(vertexConsumer, matrix4f, matrix3f, light, 1.0F, 1, 1, 0);
      vertex(vertexConsumer, matrix4f, matrix3f, light, 0.0F, 1, 0, 0);
      matrixStack.pop();

      if(fishingBobberEntity.getOwnerPosition() != null) {
         double x = MathHelper.lerp((double)g, fishingBobberEntity.prevX, fishingBobberEntity.getX());
         double y = MathHelper.lerp((double)g, fishingBobberEntity.prevY, fishingBobberEntity.getY()) + 0.25D;
         double z = MathHelper.lerp((double)g, fishingBobberEntity.prevZ, fishingBobberEntity.getZ());
         float u = (float)(fishingBobberEntity.getOwnerPosition().getX() - x);
         float v = (float)(fishingBobberEntity.getOwnerPosition().getY() - y);
         float w = (float)(fishingBobberEntity.getOwnerPosition().getZ() - z);
         VertexConsumer vertexConsumer2 = vertexConsumerProvider.getBuffer(RenderLayer.getLineStrip());
         MatrixStack.Entry entry2 = matrixStack.peek();
         
         for(int i = 0; i <= 16; ++i) {
            renderFishingLine(u, v, w, vertexConsumer2, entry2, percentage(i, 16), percentage(i + 1, 16));
         }
      }

      matrixStack.pop();
      super.render(fishingBobberEntity, f, g, matrixStack, vertexConsumerProvider, light);
   }

   private static float percentage(int value, int max) {
      return (float)value / (float)max;
   }

   private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normalMatrix, int light, float x, int y, int u, int v) {
      buffer.vertex(matrix, x - 0.5F, (float)y - 0.5F, 0.0F).color(255, 255, 255, 255).texture((float)u, (float)v).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(normalMatrix, 0.0F, 1.0F, 0.0F).next();
   }

   private static void renderFishingLine(float x, float y, float z, VertexConsumer buffer, MatrixStack.Entry matrices, float segmentStart, float segmentEnd) {
      float f = x * segmentStart;
		float g = y * (segmentStart * segmentStart + segmentStart) * 0.5F + 0.25F;
		float h = z * segmentStart;
		float i = x * segmentEnd - f;
		float j = y * (segmentEnd * segmentEnd + segmentEnd) * 0.5F + 0.25F - g;
		float k = z * segmentEnd - h;
		float l = MathHelper.sqrt(i * i + j * j + k * k);
		i /= l;
		j /= l;
		k /= l;
		buffer.vertex(matrices.getPositionMatrix(), f, g, h).color(0, 0, 0, 255).normal(matrices.getNormalMatrix(), i, j, k).next();
   }

   @Override
   public Identifier getTexture(DispenserFishingBobberEntity fishingBobberEntity) {
      return TEXTURE;
   }

   static {
      LAYER = RenderLayer.getEntityCutout(TEXTURE);
   }
}

