package com.perengano99.villagium.client.model;

import com.perengano99.villagium.client.renderer.state.NvHumanoidRenderState;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public abstract class NvHumanoidModel<T extends NvHumanoidRenderState> extends EntityModel<T> implements ArmedModel<T> {
	
	public static final float HAIR_DEFORMATION = 0.013f;
	public static final float HAIR_OV_DEFORMATION = 0.1f;
	public static final float INNER_CLOTHES_DEFORMATION = 0.01f;
	public static final float OUTER_CLOTHES_DEFORMATION = 0.11f;
	
	// Claves para las partes del modelo
	protected static final String KEY_BODY = "body";
	protected static final String KEY_HEAD = "head";
	protected static final String KEY_RIGHT_ARM = "right_arm";
	protected static final String KEY_LEFT_ARM = "left_arm";
	protected static final String KEY_RIGHT_LEG = "right_leg";
	protected static final String KEY_LEFT_LEG = "left_leg";
	protected static final String KEY_BODY_OV = "body_ov";
	protected static final String KEY_HEAD_OV = "head_ov";
	protected static final String KEY_RIGHT_ARM_OV = "right_arm_ov";
	protected static final String KEY_LEFT_ARM_OV = "left_arm_ov";
	protected static final String KEY_RIGHT_LEG_OV = "right_leg_ov";
	protected static final String KEY_LEFT_LEG_OV = "left_leg_ov";
	
	protected final ModelPart root;
	// Partes base
	protected final ModelPart body;
	protected final ModelPart head;
	protected final ModelPart rightArm;
	protected final ModelPart leftArm;
	protected final ModelPart rightLeg;
	protected final ModelPart leftLeg;
	
	// Partes del Overlay (pueden ser nulas si el modelo no las tiene)
	@Nullable public final ModelPart body_ov;
	@Nullable protected final ModelPart head_ov;
	@Nullable protected final ModelPart rightArm_ov;
	@Nullable protected final ModelPart leftArm_ov;
	@Nullable protected final ModelPart rightLeg_ov;
	@Nullable protected final ModelPart leftLeg_ov;
	
	protected NvHumanoidModel(ModelPart root) {
		super(root);
		
		this.root     = root;
		this.body     = root.getChild(KEY_BODY);
		this.head     = root.getChild(KEY_HEAD);
		this.rightArm = body.getChild(KEY_RIGHT_ARM);
		this.leftArm  = body.getChild(KEY_LEFT_ARM);
		this.rightLeg = root.getChild(KEY_RIGHT_LEG);
		this.leftLeg  = root.getChild(KEY_LEFT_LEG);
		
		// Inicializa las partes del overlay, comprobando si existen primero.
		this.head_ov     = root.hasChild(KEY_HEAD_OV) ? root.getChild(KEY_HEAD_OV) : null;
		this.body_ov     = root.hasChild(KEY_BODY_OV) ? root.getChild(KEY_BODY_OV) : null;
		this.rightLeg_ov = root.hasChild(KEY_RIGHT_LEG_OV) ? root.getChild(KEY_RIGHT_LEG_OV) : null;
		this.leftLeg_ov  = root.hasChild(KEY_LEFT_LEG_OV) ? root.getChild(KEY_LEFT_LEG_OV) : null;
		
		// Los brazos del overlay son hijos de body_ov, así que comprobamos body_ov primero.
		if (this.body_ov != null) {
			this.rightArm_ov = this.body_ov.hasChild(KEY_RIGHT_ARM_OV) ? this.body_ov.getChild(KEY_RIGHT_ARM_OV) : null;
			this.leftArm_ov  = this.body_ov.hasChild(KEY_LEFT_ARM_OV) ? this.body_ov.getChild(KEY_LEFT_ARM_OV) : null;
		} else {
			this.rightArm_ov = null;
			this.leftArm_ov  = null;
		}
	}
	
	@Override
	public void setupAnim(T state) {
		super.setupAnim(state);
		
		animateHeadLook(state.xRot, state.yRot);
		animate(state);
		
		copyBasePoseToOverlays();
	}
	
	protected void animate(T state) {}
	
	protected void animateHeadLook(float netHeadYaw, float headPitch) {
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		head.xRot = headPitch * Mth.DEG_TO_RAD;
		
		float tilt = netHeadYaw * Mth.DEG_TO_RAD * -.05f;
		head.zRot = Mth.clamp(tilt, -.1f, .12f);
	}
	
	protected void copyBasePoseToOverlays() {
		if (head_ov != null) head_ov.loadPose(head.storePose());
		if (body_ov != null) body_ov.loadPose(body.storePose());
		if (rightLeg_ov != null) rightLeg_ov.loadPose(rightLeg.storePose());
		if (leftLeg_ov != null) leftLeg_ov.loadPose(leftLeg.storePose());
		if (rightArm_ov != null) rightArm_ov.loadPose(rightArm.storePose());
		if (leftArm_ov != null) leftArm_ov.loadPose(leftArm.storePose());
	}
	
	public ModelPart getHead() {
		return head;
	}
	
	public ModelPart getBody() {
		return body;
	}
	
	public ModelPart getRightArm() {
		return rightArm;
	}
	
	public ModelPart getLeftArm() {
		return leftArm;
	}
	
	public ModelPart getRightLeg() {
		return rightLeg;
	}
	
	public ModelPart getLeftLeg() {
		return leftLeg;
	}
}
