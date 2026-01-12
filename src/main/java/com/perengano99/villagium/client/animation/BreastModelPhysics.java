package com.perengano99.villagium.client.animation;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class BreastModelPhysics {
	
	//X-Axis
	private float bounceVelX = 0, targetBounceX = 0, velocityX = 0, positionX, prePositionX;
	//Y-Axis
	private float bounceVel = 0, targetBounceY = 0, velocity = 0, positionY, prePositionY;
	//Rotation
	private float bounceRotVel = 0, targetRotVel = 0, rotVelocity = 0, wfg_bounceRotation, wfg_preBounceRotation;
	
	private float breastSize = 0, preBreastSize = 0;
	
	private Pose lastPose;
	private int lastSwingDuration = 6, lastSwingTick = 0;
	private Vec3 prePos;
	
	private int randomB = 1;
	private double lastVerticalMoveVelocity;
	private boolean alreadyFalling = false;
	
	private final Random random = new Random();
	
	public BreastModelPhysics() {
	}
	
	private static boolean vehicleSuppressesRotation(Entity vehicle) {
		return (vehicle instanceof Chicken || vehicle instanceof AbstractHorse horseLike && !horseLike.isSaddled() || vehicle instanceof Camel camel && camel.isCamelSitting());
	}
	
	public void update(LivingEntity host, float targetBreastSize) {
		this.prePositionY          = this.positionY;
		this.prePositionX          = this.positionX;
		this.wfg_preBounceRotation = this.wfg_bounceRotation;
		this.preBreastSize         = this.breastSize;
		if (this.prePos == null) {
			this.prePos = host.position();
			return;
		}
		float breastWeight = targetBreastSize * 1.25F;
		this.breastSize += (this.breastSize < targetBreastSize) ? (Math.abs(this.breastSize - targetBreastSize) / 2.0F) : (-Math.abs(
				this.breastSize - targetBreastSize) / 2.0F);
		Vec3 motion = host.position().subtract(this.prePos);
		this.prePos = host.position();
		float bounceIntensity = targetBreastSize * 3.0F * Math.round(0.333f * 3.0F * 100.0F) / 100.0F;
		bounceIntensity *= random.nextFloat() + 0.5f;
		if (host.fallDistance > 0.0F && !this.alreadyFalling) {
			this.randomB        = host.level().getRandom().nextBoolean() ? -1 : 1;
			this.alreadyFalling = true;
		}
		if (host.fallDistance == 0.0F)
			this.alreadyFalling = false;
		this.targetBounceY = (float) motion.y * bounceIntensity;
		this.targetBounceY += breastWeight;
		this.targetRotVel  = calcRotation(host, bounceIntensity);
		this.targetRotVel += (float) motion.y * bounceIntensity * this.randomB;
		float f = (float) host.getDeltaMovement().lengthSqr() / 0.2F;
		f = f * f * f;
		if (f < 1.0F)
			f = 1.0F;
		this.targetBounceY += Mth.cos(host.walkAnimation.position() * 0.6662F + 3.1415927F) * 0.5F * host.walkAnimation.speed() * 0.5F / f;
		Pose pose = host.getPose();
		if (pose != this.lastPose) {
			if (pose == Pose.CROUCHING || this.lastPose == Pose.CROUCHING) {
				this.targetBounceY += bounceIntensity;
			} else if (pose == Pose.SLEEPING || this.lastPose == Pose.SLEEPING) {
				this.targetBounceY = bounceIntensity;
			}
			this.lastPose = pose;
		}
		if (host.getVehicle() != null) {
			Entity entity1 = host.getVehicle();
			if (entity1 instanceof Boat boat) {
				int rowTime = (int) boat.getRowingTime(0, host.walkAnimation.position());
				int rowTime2 = (int) boat.getRowingTime(1, host.walkAnimation.position());
				float rotationL = (float) Mth.clampedLerp(-1.0471975803375244D, -0.2617993950843811D, ((Mth.sin(-rowTime2) + 1.0F) / 2.0F));
				float rotationR = (float) Mth.clampedLerp(-0.7853981852531433D, 0.7853981852531433D, ((Mth.sin(-rowTime + 1.0F) + 1.0F) / 2.0F));
				if (rotationL < -1.0F || rotationR < -0.6F)
					this.targetBounceY = bounceIntensity / 3.25F;
			} else {
				entity1 = host.getVehicle();
				if (entity1 instanceof Minecart cart) {
					float speed = (float) cart.getDeltaMovement().lengthSqr();
					if (Math.random() * speed < 0.5D && speed > 0.2F) {
						this.targetBounceY = ((Math.random() > 0.5D) ? -bounceIntensity : bounceIntensity) / 6.0F;
						this.targetBounceY += breastWeight;
					}
				} else {
					entity1 = host.getVehicle();
					if (entity1 instanceof AbstractHorse horse) {
						float movement = (float) horse.getDeltaMovement().length();
						if (horse.tickCount % clampMovement(movement) == 5 && movement > 0.05F) {
							this.targetBounceY = bounceIntensity / 4.0F;
							this.targetBounceY += breastWeight;
						}
					} else {
						entity1 = host.getVehicle();
						if (entity1 instanceof Pig pig) {
							float movement = (float) pig.getDeltaMovement().length();
							if (pig.tickCount % clampMovement(movement) == 5 && movement > 0.002F) {
								this.targetBounceY = bounceIntensity * Mth.clamp(movement * 75.0F, 0.1F, 1.0F) / 4.0F;
								this.targetBounceY += breastWeight;
							}
						} else {
							entity1 = host.getVehicle();
							if (entity1 instanceof Strider strider) {
								double heightOffset = strider.getBbHeight() - 0.19D + (0.12F * Mth.cos(
										strider.walkAnimation.position() * 1.5F) * 2.0F * Math.min(0.25F, strider.walkAnimation.speed()));
								this.targetBounceY += ((float) (heightOffset * 3.0D) - 4.5F) * bounceIntensity;
							}
						}
					}
				}
			}
		}
		int swingDuration = host.getCurrentSwingDuration();
		if ((swingDuration > 1 || this.lastSwingDuration > 1) && pose != Pose.SLEEPING) {
			float amplifier = 0.0F;
			if (swingDuration < 6) {
				amplifier = 0.15F * (6 - swingDuration);
			} else if (swingDuration > 6) {
				amplifier = -0.067F * (swingDuration - 6);
			}
			amplifier = Mth.clamp(1.0F + amplifier, 0.6F, 1.3F);
			int everyNthTick = Mth.clamp(swingDuration - 1, 1, 5);
			if (host.swinging && host.tickCount % everyNthTick == 0) {
				float hasteMult = Mth.clamp(everyNthTick / 5.0F, 0.4F, 1.0F);
				this.targetBounceY += ((Math.random() > 0.5D) ? -0.25F : 0.25F) * amplifier * bounceIntensity * hasteMult;
			}
			int swingTickDelta = host.swingTime - this.lastSwingTick;
			float swingProgress = distanceFromMedian(this.lastSwingDuration, Mth.clamp(this.lastSwingTick, 0, this.lastSwingDuration));
			HumanoidArm swingingArm = (host.swingingArm == InteractionHand.MAIN_HAND) ? host.getMainArm() : host.getMainArm().getOpposite();
			if (swingTickDelta < 0 && this.lastSwingTick != this.lastSwingDuration - 1) {
				this.targetRotVel += ((swingingArm == HumanoidArm.RIGHT) ? -2.5F : 2.5F) * Math.abs(swingProgress) * bounceIntensity;
			} else if (host.swinging && swingDuration > 1) {
				HumanoidArm swingingToward = (swingProgress > 0.0F) ? swingingArm.getOpposite() : swingingArm;
				this.targetRotVel += ((swingingToward == HumanoidArm.RIGHT) ? -0.2F : 0.2F) * amplifier * bounceIntensity;
			}
			this.lastSwingTick = host.swingTime;
		}
		if (!host.swinging)
			this.lastSwingTick = 0;
		this.lastSwingDuration = Math.max(swingDuration, 1);
		float percent = 1;
		float bounceAmount = 0.45F * (1.0F - percent) + 0.15F;
		bounceAmount = Mth.clamp(bounceAmount, 0.15F, 0.6F);
		float delta = 2.25F - bounceAmount;
		float distanceFromMin = Math.abs(this.bounceVel + 0.5F) * 0.5F;
		float distanceFromMax = Math.abs(this.bounceVel - 2.65F) * 0.5F;
		if (this.bounceVel < -0.5F)
			this.targetBounceY += distanceFromMin;
		if (this.bounceVel > 2.5F)
			this.targetBounceY -= distanceFromMax;
		this.targetBounceY      = Mth.clamp(this.targetBounceY, -1.5F, 2.5F);
		this.targetRotVel       = Mth.clamp(this.targetRotVel, -25.0F, 25.0F);
		this.velocity           = Mth.lerp(bounceAmount, this.velocity, (this.targetBounceY - this.bounceVel) * delta);
		this.bounceVel += this.velocity * percent * 1.1625F;
		this.velocityX          = Mth.lerp(bounceAmount, this.velocityX, (this.targetBounceX - this.bounceVelX) * delta);
		this.bounceVelX += this.velocityX * percent;
		this.rotVelocity        = Mth.lerp(bounceAmount, this.rotVelocity, (this.targetRotVel - this.bounceRotVel) * delta);
		this.bounceRotVel += this.rotVelocity * percent;
		this.wfg_bounceRotation = this.bounceRotVel;
		this.positionX          = this.bounceVelX;
		this.positionY          = this.bounceVel;
		if (this.positionY < -0.5F)
			this.positionY = -0.5F;
		if (this.positionY > 1.5F) {
			this.positionY = 1.5F;
			this.velocity  = 0.0F;
		}
	}
	
	public float getPositionY(float partialTicks) {
		return Mth.lerp(partialTicks, prePositionY, positionY);
	}
	
	public float getPositionX(float partialTicks) {
		return Mth.lerp(partialTicks, prePositionX, positionX);
	}
	
	public float getBounceRotation(float partialTicks) {
		return Mth.lerp(partialTicks, wfg_preBounceRotation, wfg_bounceRotation);
	}
	
	private int clampMovement(float movement) {
		return Math.max((int) (10 - 2 * movement), 1);
	}
	
	private static boolean shouldUseVehicleYaw(LivingEntity rider, Entity vehicle) {
		return (vehicle.hasControllingPassenger()
		        || vehicle instanceof Boat
		        || vehicle.getVisualRotationYInDegrees() == rider.getVisualRotationYInDegrees());
	}
	
	private static float calcRotation(LivingEntity entity, float bounceIntensity) {
		Entity vehicle = entity.getVehicle();
		if (vehicle != null) {
			if (vehicleSuppressesRotation(vehicle))
				return 0.0F;
			if (shouldUseVehicleYaw(entity, vehicle)) {
				if (vehicle instanceof LivingEntity livingVehicle) {
					return -((livingVehicle.yBodyRot - livingVehicle.yBodyRotO) / 15.0F) * bounceIntensity;
				}
				return -((vehicle.getYRot() - vehicle.yRotO) / 15.0F) * bounceIntensity;
			}
		}
		return -((entity.yBodyRot - entity.yBodyRotO) / 15.0F) * bounceIntensity;
	}
	
	private static float distanceFromMedian(int p2, float point) {
		if (0 >= p2)
			throw new IllegalArgumentException("p2 must be greater than p1");
		if (point < 0 || point > p2)
			throw new IllegalArgumentException(point + " is not within bounds of (" + point + ", " + 0 + ")");
		if (point == 0 || point == p2)
			return 0.0F;
		float median = p2 / 2.0F;
		point -= 0;
		if (point > median)
			point = -(median - point - median);
		return point / median;
	}
}
