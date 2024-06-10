package dev.nolij.zume.integration.embeddium;

import dev.nolij.zume.api.config.v1.ZumeConfig;
import dev.nolij.zume.api.config.v1.ZumeConfigAPI;
import dev.nolij.zume.impl.ZumeConstants;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import org.embeddedt.embeddium.client.gui.options.OptionIdentifier;

public final class ZumeOptionsStorage implements OptionStorage<ZumeConfig> {
	
	//region Pages
	public static final OptionIdentifier<Void> GENERAL =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "general");
	public static final OptionIdentifier<Void> ADVANCED =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "advanced");
	//endregion
	
	//region Groups
	public static final OptionIdentifier<Void> BEHAVIOUR =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "general/behaviour");
	public static final OptionIdentifier<Void> ANIMATIONS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "general/animations");
	public static final OptionIdentifier<Void> THIRD_PERSON =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "general/third_person");
	
	public static final OptionIdentifier<Void> EXPONENTS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "advanced/exponents");
	public static final OptionIdentifier<Void> MISC =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "advanced/misc");
	//endregion
	
	//region Options
	public static final OptionIdentifier<Boolean> ENABLE_CINEMATIC_ZOOM =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "enable_cinematic_zoom", boolean.class);
	public static final OptionIdentifier<Integer> MOUSE_SENSITIVITY_FLOOR =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "mouse_sensitivity_floor", int.class);
	public static final OptionIdentifier<Integer> ZOOM_SPEED =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "zoom_speed", int.class);
	public static final OptionIdentifier<Boolean> ENABLE_ZOOM_SCROLLING =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "enable_zoom_scrolling", boolean.class);
	public static final OptionIdentifier<Integer> ZOOM_SMOOTHNESS_MS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "zoom_smoothness_ms", int.class);
	public static final OptionIdentifier<Integer> ANIMATION_EASING_EXPONENT =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "animation_easing_exponent", int.class);
	public static final OptionIdentifier<Integer> ZOOM_EASING_EXPONENT =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "zoom_easing_exponent", int.class);
	public static final OptionIdentifier<Integer> DEFAULT_ZOOM =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "default_zoom", int.class);
	public static final OptionIdentifier<Boolean> FIRST_PERSON_TOGGLE_MODE =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "first_person_toggle_mode", boolean.class);
	public static final OptionIdentifier<Boolean> THIRD_PERSON_TOGGLE_MODE =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "third_person_toggle_mode", boolean.class);
	public static final OptionIdentifier<Integer> MIN_FOV =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "min_fov", int.class);
	public static final OptionIdentifier<Integer> MAX_THIRD_PERSON_ZOOM_BLOCKS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "max_third_person_zoom_blocks", int.class);
	public static final OptionIdentifier<Integer> MIN_THIRD_PERSON_ZOOM_BLOCKS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "min_third_person_zoom_blocks", int.class);
	public static final OptionIdentifier<Boolean> DISABLE =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "disable", boolean.class);
	//endregion
	
	private final ZumeConfig storage = ZumeConfigAPI.getSnapshot();
	
	@Override
	public ZumeConfig getData() {
		return storage;
	}
	
	@Override
	public void save() {
		ZumeConfigAPI.replaceConfig(storage);
	}
	
}
