package com.perengano99.villagium.client.gui.widgets;

import com.perengano99.villagium.Villagium;
import com.perengano99.villagium.client.animation.face.FaceModelAnimator;
import com.perengano99.villagium.client.animation.face.IFaceModelAnimation;
import com.perengano99.villagium.client.animation.face.StaticFaceExpressionAnimation;
import com.perengano99.villagium.client.model.parts.FacePartModel;
import com.perengano99.villagium.client.renderer.HslTextureBaker;
import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import com.perengano99.villagium.core.registration.ModAttachments;
import com.perengano99.villagium.data.AppearanceLoader;
import com.perengano99.villagium.data.TonesLoader;
import com.perengano99.villagium.data.TonesLoader.ToneColorEntry;
import com.perengano99.villagium.social.profile.AppearanceData;
import com.perengano99.villagium.social.profile.ProfileData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NpcPortraitWidget extends AbstractWidget {
	
	private static final Identifier PORTRAIT_CONTAINER = Identifier.fromNamespaceAndPath(Villagium.MODID, "container/npc_card");
	
	@Nullable private LivingEntity entity;
	@Nullable private ProfileData profile;
	
	private final NvHumanoidRenderState fakeRenderState = new NvHumanoidRenderState();
	// TODO: Permitir modificar la expresión del retrato con expresiones estáticas predefinidas en FaceModelAnimator.
	private final FaceModelAnimator<NvHumanoidRenderState> portraitFaceAnimator = new FaceModelAnimator<>();
	
	private IFaceModelAnimation<NvHumanoidRenderState> baseExpression = new StaticFaceExpressionAnimation<>();
	private IFaceModelAnimation<NvHumanoidRenderState> timedExpression = null;
	private int timedExpressionTicks = 0;
	
	public NpcPortraitWidget(int x, int y, int width, int height, @Nullable LivingEntity entity) {
		super(x, y, width, height, Component.empty());
		this.entity = entity;
		if (entity != null) this.profile = entity.getData(ModAttachments.PROFILE_DATA.get());
		updateActiveAnimation();
	}
	
	public NpcPortraitWidget(int x, int y, int width, int height, @Nullable ProfileData profile) {
		super(x, y, width, height, Component.empty());
		this.profile = profile;
		if (profile != null) this.entity = profile.entity();
		updateActiveAnimation();
	}
	
	public void setEntity(@Nullable LivingEntity entity) {
		this.entity = entity;
		if (entity != null) this.profile = entity.getData(ModAttachments.PROFILE_DATA.get());
	}
	
	public void setProfile(@Nullable ProfileData profile) {
		this.profile = profile;
		if (profile != null) this.entity = profile.entity();
	}
	
	public FaceModelAnimator<NvHumanoidRenderState> getPortraitFaceAnimator() {
		return portraitFaceAnimator;
	}
	
	public IFaceModelAnimation<NvHumanoidRenderState> getBaseExpression() {
		return baseExpression;
	}
	
	public void setExpression(IFaceModelAnimation<NvHumanoidRenderState> expression) {
		this.baseExpression = expression;
		this.timedExpression = null;
		this.timedExpressionTicks = 0;
		updateActiveAnimation();
	}
	
	public void setTimedExpression(IFaceModelAnimation<NvHumanoidRenderState> expression, int durationTicks) {
		this.timedExpression = expression;
		this.timedExpressionTicks = durationTicks;
		updateActiveAnimation();
	}
	
	private void updateActiveAnimation() {
		this.portraitFaceAnimator.clearAnimations();
		IFaceModelAnimation<NvHumanoidRenderState> active = (timedExpression != null) ? timedExpression : baseExpression;
		if (active != null) this.portraitFaceAnimator.registerAnimation(active);
	}
	
	@Override
	protected void extractWidgetRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PORTRAIT_CONTAINER, getX(), getY(), getWidth(), getHeight());
		
		if (profile == null && entity != null) profile = entity.getData(ModAttachments.PROFILE_DATA.get());
		if (profile == null) return;
		
		if (timedExpressionTicks > 0) {
			timedExpressionTicks--;
			if (timedExpressionTicks == 0) {
				timedExpression = null;
				updateActiveAnimation();
			}
		}
		
		AppearanceData app = profile.appearance();
		ToneColorEntry tone = TonesLoader.getColors(app.toneGroupId(), app.toneIndex());
		
		Identifier skinTex = AppearanceLoader.getTexturePath(app.skinId());
		Identifier hairTex = AppearanceLoader.getTexturePath(app.hairId());
		Identifier clothesTex = AppearanceLoader.getTexturePath(app.clothesId());
		Identifier faceTex = AppearanceLoader.getTexturePath(app.faceId());
		
		Identifier bakedSkinTex = HslTextureBaker.getBakedTexture(tone.skin(), skinTex);
		Identifier bakedHairTex = HslTextureBaker.getBakedTexture(tone.hair(), hairTex);
		
		int innerX = getX() + 4;
		int innerY = getY() + 4;
		int innerW = getWidth() - 8;
		int innerH = getHeight() - 8;
		
		graphics.enableScissor(innerX, innerY, innerX + innerW, innerY + innerH);
		
		// 1. Piel base (Skin Head Front: u=8, v=8, w=8, h=8 en 64x64)
		graphics.blit(RenderPipelines.GUI_TEXTURED, bakedSkinTex, innerX, innerY, 8.0f, 8.0f, innerW, innerH, 8, 8, 64, 64);
		
		// 2. Rostro renderizado alimentado por fakeRenderState y procesado por portraitFaceAnimator
		extractFace(graphics, tone, faceTex, innerX, innerY, innerW, innerH, partialTicks);
		
		// 3. Ropa (Clothes Head Front: u=40, v=24, w=8, h=8 en 64x64)
		graphics.blit(RenderPipelines.GUI_TEXTURED, clothesTex, innerX, innerY, 40.0f, 24.0f, innerW, innerH, 8, 8, 64, 64);
		
		// 4. Cabello base y capa externa (Hair Head Front: u=40, v=24 y u=40, v=56)
		graphics.blit(RenderPipelines.GUI_TEXTURED, bakedHairTex, innerX, innerY, 40.0f, 24.0f, innerW, innerH, 8, 8, 64, 64);
		graphics.blit(RenderPipelines.GUI_TEXTURED, bakedHairTex, innerX, innerY, 40.0f, 56.0f, innerW, innerH, 8, 8, 64, 64);
		
		graphics.disableScissor();
	}
	
	private void extractFace(@NotNull GuiGraphicsExtractor graphics, ToneColorEntry tone, Identifier faceTex, int innerX, int innerY, int innerW, int innerH, float partialTicks) {
		fakeRenderState.irisColor   = tone.eye();
		fakeRenderState.faceTexture = faceTex;
		fakeRenderState.partialTick = partialTicks;
		
		portraitFaceAnimator.update(fakeRenderState);
		
		int irisColor = 0xFF000000 | fakeRenderState.irisColor;
		
		float faceCenterX = innerX + innerW * 0.5f;
		float faceCenterY = innerY + innerH * 0.45f + 4.0f;
		float faceUnitScaleX = innerW / 8.0f;
		float faceUnitScaleY = innerH / 8.0f;
		
		for (FacePartModel part : portraitFaceAnimator.getAllParts()) {
			int color = part.isIrisTinted ? irisColor : 0xFFFFFFFF;
			
			graphics.pose().pushMatrix();
			graphics.pose().translate(faceCenterX, faceCenterY);
			graphics.pose().scale(faceUnitScaleX, faceUnitScaleY);
			
			part.applyGuiTransformations(graphics.pose(), partialTicks);
			
			graphics.blit(RenderPipelines.GUI_TEXTURED, faceTex,
					0, 0, (float) part.texU, (float) part.texV, (int) part.baseWidth, (int) part.baseHeight, (int) part.baseWidth, (int) part.baseHeight, 32, 32, color);
			
			graphics.pose().popMatrix();
		}
	}
	
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
}
