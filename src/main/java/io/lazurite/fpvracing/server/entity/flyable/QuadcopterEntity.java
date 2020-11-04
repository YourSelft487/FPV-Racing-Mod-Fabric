package io.lazurite.fpvracing.server.entity.flyable;

import io.lazurite.fpvracing.client.input.InputTick;
import io.lazurite.fpvracing.network.tracker.Config;
import io.lazurite.fpvracing.network.tracker.GenericDataTrackerRegistry;
import io.lazurite.fpvracing.physics.thrust.QuadcopterThrust;
import io.lazurite.fpvracing.physics.entity.ClientPhysicsHandler;
import io.lazurite.fpvracing.server.ServerInitializer;
import io.lazurite.fpvracing.server.entity.FlyableEntity;
import io.lazurite.fpvracing.server.item.QuadcopterItem;
import io.lazurite.fpvracing.util.math.BetaflightHelper;
import io.lazurite.fpvracing.util.math.QuaternionHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import javax.vecmath.Quat4f;

/**
 * @author Ethan Johnson
 * @author Patrick Hofmann
 */
public class QuadcopterEntity extends FlyableEntity {
	public static final GenericDataTrackerRegistry.Entry<Float> RATE = GenericDataTrackerRegistry.register(new Config.Key<>("rate", ServerInitializer.FLOAT_TYPE), 0.5F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Float> SUPER_RATE = GenericDataTrackerRegistry.register(new Config.Key<>("superRate", ServerInitializer.FLOAT_TYPE), 0.5F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Float> EXPO = GenericDataTrackerRegistry.register(new Config.Key<>("expo", ServerInitializer.FLOAT_TYPE), 0.0F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Float> THRUST_CURVE = GenericDataTrackerRegistry.register(new Config.Key<>("thrustCurve", ServerInitializer.FLOAT_TYPE), 0.95F, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Integer> THRUST = GenericDataTrackerRegistry.register(new Config.Key<>("thrust", ServerInitializer.INTEGER_TYPE), 50, QuadcopterEntity.class);
	public static final GenericDataTrackerRegistry.Entry<Integer> CAMERA_ANGLE = GenericDataTrackerRegistry.register(new Config.Key<>("cameraAngle", ServerInitializer.INTEGER_TYPE), 0, QuadcopterEntity.class);

	/**
	 * @param type  the {@link EntityType}
	 * @param world the {@link World} that the {@link QuadcopterEntity} will be spawned in
	 */
	public QuadcopterEntity(EntityType<?> type, World world) {
		super(type, world);
		thrust = new QuadcopterThrust(this);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void stepInput(float delta) {
		ClientPhysicsHandler physics = (ClientPhysicsHandler) getPhysics();
		float deltaX = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currX, getValue(RATE), getValue(EXPO), getValue(SUPER_RATE), delta);
		float deltaY = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currY, getValue(RATE), getValue(EXPO), getValue(SUPER_RATE), delta);
		float deltaZ = (float) BetaflightHelper.calculateRates(InputTick.axisValues.currZ, getValue(RATE), getValue(EXPO), getValue(SUPER_RATE), delta);

		physics.rotateX(deltaX);
		physics.rotateY(deltaY);
		physics.rotateZ(deltaZ);

		physics.applyForce(thrust.getForce());
	}

	@Override
	public void updateEulerRotations() {
		super.updateEulerRotations();

		Quat4f cameraPitch = physics.getOrientation();
		QuaternionHelper.rotateX(cameraPitch, -getValue(CAMERA_ANGLE));
		pitch = QuaternionHelper.getPitch(cameraPitch);
	}

	/**
	 * Gets whether the {@link QuadcopterEntity} can be killed
	 * by conventional means (e.g. punched, rained on, set on fire, etc.)
	 *
	 * @return whether or not the {@link QuadcopterEntity} is killable
	 */
	@Override
	public boolean isKillable() {
		return !(getValue(FlyableEntity.GOD_MODE) || noClip);
	}

	/**
	 * Break the {@link QuadcopterEntity} when it's shot or otherwise damaged in some way.
	 *
	 * @param source the source of the damage
	 * @param amount the amount of damage taken
	 * @return
	 */
	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof PlayerEntity || (isKillable() && source instanceof ProjectileDamageSource)) {
			this.kill();
			return true;
		}
		return false;
	}

	/**
	 * Called whenever the {@link QuadcopterEntity} is killed. Drops {@link QuadcopterItem} containing tag info.
	 */
	@Override
	public void kill() {
		super.kill();

		if (world.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			ItemStack itemStack = new ItemStack(ServerInitializer.DRONE_SPAWNER_ITEM);
			writeTagToSpawner(itemStack);
			dropStack(itemStack);
		}
	}
}