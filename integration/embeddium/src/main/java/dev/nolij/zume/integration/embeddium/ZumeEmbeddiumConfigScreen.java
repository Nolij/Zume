package dev.nolij.zume.integration.embeddium;

import com.google.common.collect.ImmutableList;
import dev.nolij.zume.api.config.v1.ZumeConfig;
import dev.nolij.zume.api.config.v1.ZumeConfigAPI;
import dev.nolij.zume.impl.ZumeConstants;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.OptionFlag;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.Control;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.network.chat.Component;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import org.embeddedt.embeddium.api.eventbus.EventHandlerRegistrar;
import org.embeddedt.embeddium.client.gui.options.OptionIdentifier;

import java.util.ArrayList;
import java.util.List;

public class ZumeEmbeddiumConfigScreen implements EventHandlerRegistrar.Handler<OptionGUIConstructionEvent> {
	
	//region Pages
	private static final OptionIdentifier<Void> GENERAL =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "general");
	private static final OptionIdentifier<Void> ADVANCED =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "advanced");
	//endregion
	
	//region Groups
	private static final OptionIdentifier<Void> BEHAVIOUR =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "general/behaviour");
	private static final OptionIdentifier<Void> ANIMATIONS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "general/animations");
	private static final OptionIdentifier<Void> THIRD_PERSON =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "general/third_person");
	
	private static final OptionIdentifier<Void> EXPONENTS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "advanced/exponents");
	private static final OptionIdentifier<Void> MISC =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "advanced/misc");
	//endregion
	
	//region Options
	private static final OptionIdentifier<Boolean> ENABLE_CINEMATIC_ZOOM =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "enable_cinematic_zoom", boolean.class);
	private static final OptionIdentifier<Integer> MOUSE_SENSITIVITY_FLOOR =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "mouse_sensitivity_floor", int.class);
	private static final OptionIdentifier<Integer> ZOOM_SPEED =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "zoom_speed", int.class);
	private static final OptionIdentifier<Boolean> ENABLE_ZOOM_SCROLLING =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "enable_zoom_scrolling", boolean.class);
	private static final OptionIdentifier<Integer> ZOOM_SMOOTHNESS_MS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "zoom_smoothness_ms", int.class);
	private static final OptionIdentifier<Integer> ANIMATION_EASING_EXPONENT =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "animation_easing_exponent", int.class);
	private static final OptionIdentifier<Integer> ZOOM_EASING_EXPONENT =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "zoom_easing_exponent", int.class);
	private static final OptionIdentifier<Integer> DEFAULT_ZOOM =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "default_zoom", int.class);
	private static final OptionIdentifier<Boolean> FIRST_PERSON_TOGGLE_MODE =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "first_person_toggle_mode", boolean.class);
	private static final OptionIdentifier<Boolean> THIRD_PERSON_TOGGLE_MODE =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "third_person_toggle_mode", boolean.class);
	private static final OptionIdentifier<Integer> MIN_FOV =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "min_fov", int.class);
	private static final OptionIdentifier<Integer> MAX_THIRD_PERSON_ZOOM_BLOCKS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "max_third_person_zoom_blocks", int.class);
	private static final OptionIdentifier<Integer> MIN_THIRD_PERSON_ZOOM_BLOCKS =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "min_third_person_zoom_blocks", int.class);
	private static final OptionIdentifier<Boolean> DISABLE =
		OptionIdentifier.create(ZumeConstants.MOD_ID, "disable", boolean.class);
	//endregion
	
	private static final class ZumeOptionsStorage implements OptionStorage<ZumeConfig> {
		
		private final ZumeConfig storage = ZumeConfigAPI.getSnapshot();
		
		@Override
		public ZumeConfig getData() {
			return storage;
		}
		
		@Override
		public void save() {
			try {
				ZumeConfigAPI.replaceConfig(storage);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	private Control<Integer> percentageControl(final Option<Integer> option) {
		return new SliderControl(option, 0, 100, 1, ControlValueFormatter.percentage());
	}
	
	private Control<Integer> exponentControl(final Option<Integer> option) {
		return new SliderControl(option, 100, 500, 25, v -> switch (v) {
			case 100 -> Component.translatable("zume.linear");
			case 200 -> Component.translatable("zume.quad");
			case 300 -> Component.translatable("zume.cubic");
			case 400 -> Component.translatable("zume.quart");
			case 500 -> Component.translatable("zume.quint");
			default -> Component.literal("x^" + (v / 100D));
		});
	}
	
	public ZumeEmbeddiumConfigScreen() {
		OptionGUIConstructionEvent.BUS.addListener(this);
	}
	
	@Override
	public void acceptEvent(OptionGUIConstructionEvent event) {
		final ZumeOptionsStorage storage = new ZumeOptionsStorage();
		
		final List<OptionGroup> generalGroups = new ArrayList<>();
		final List<OptionGroup> advancedGroups = new ArrayList<>();
		
		generalGroups.add(OptionGroup.createBuilder()
			.setId(BEHAVIOUR)
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(ZOOM_SPEED)
				.setControl(option -> new SliderControl(option, 5, 50, 5, ControlValueFormatter.number()))
				.setBinding(
					(config, value) -> config.zoomSpeed = value.shortValue(),
					config -> (int) config.zoomSpeed)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(ENABLE_ZOOM_SCROLLING)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.isZoomScrollingEnabled = value,
					config -> config.isZoomScrollingEnabled)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(ENABLE_CINEMATIC_ZOOM)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.isCinematicZoomEnabled = value,
					config -> config.isCinematicZoomEnabled)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(MOUSE_SENSITIVITY_FLOOR)
				.setControl(this::percentageControl)
				.setBinding(
					(config, value) -> config.mouseSensitivityFloor = value * 0.01D,
					config -> (int) Math.round(config.mouseSensitivityFloor * 100D))
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(DEFAULT_ZOOM)
				.setControl(this::percentageControl)
				.setBinding(
					(config, value) -> config.defaultZoom = value * 0.01D,
					config -> (int) Math.round(config.defaultZoom * 100D))
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(FIRST_PERSON_TOGGLE_MODE)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.isFirstPersonToggleModeEnabled = value,
					config -> config.isFirstPersonToggleModeEnabled)
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(THIRD_PERSON_TOGGLE_MODE)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.isThirdPersonToggleModeEnabled = value,
					config -> config.isThirdPersonToggleModeEnabled)
				.build())
			.build());
		generalGroups.add(OptionGroup.createBuilder()
			.setId(ANIMATIONS)
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(ZOOM_SMOOTHNESS_MS)
				.setControl(option ->
					new SliderControl(option, 0, 500, 25,
						v -> v > 0
						     ? Component.literal(v + "ms")
						     : Component.translatable("zume.instant")))
				.setBinding(
					(config, value) -> config.zoomSmoothnessMilliseconds = value.shortValue(),
					config -> (int) config.zoomSmoothnessMilliseconds)
				.build())
			.build());
		generalGroups.add(OptionGroup.createBuilder()
			.setId(THIRD_PERSON)
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(MAX_THIRD_PERSON_ZOOM_BLOCKS)
				.setControl(option ->
					new SliderControl(option, 0, 30, 1,
						v -> v > 0
						     ? Component.translatable("zume.blocks", v)
						     : Component.translatable("zume.disabled")))
				.setBinding(
					(config, value) -> config.maximumThirdPersonZoomBlocks = value,
					config -> (int) config.maximumThirdPersonZoomBlocks)
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(MIN_THIRD_PERSON_ZOOM_BLOCKS)
				.setControl(option ->
					new SliderControl(option, 0, 10, 1,
						v -> v > 0
						     ? Component.translatable("zume.blocks", v)
						     : Component.translatable("zume.blocks", 0.5)))
				.setBinding(
					(config, value) -> config.minimumThirdPersonZoomBlocks = value > 0 ? value : 0.5,
					config -> (int) config.minimumThirdPersonZoomBlocks)
				.build())
			.build());
		
		advancedGroups.add(OptionGroup.createBuilder()
			.setId(EXPONENTS)
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(ANIMATION_EASING_EXPONENT)
				.setControl(this::exponentControl)
				.setBinding(
					(config, value) -> config.animationEasingExponent = value / 100D,
					config -> (int) Math.round(config.animationEasingExponent * 100))
				.build())
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(ZOOM_EASING_EXPONENT)
				.setControl(this::exponentControl)
				.setBinding(
					(config, value) -> config.zoomEasingExponent = value / 100D,
					config -> (int) Math.round(config.zoomEasingExponent * 100))
				.build())
			.build());
		advancedGroups.add(OptionGroup.createBuilder()
			.setId(MISC)
			.add(OptionImpl.createBuilder(int.class, storage)
				.setId(MIN_FOV)
				.setControl(option ->
					new SliderControl(option, -2, 1, 1,
						v -> Component.literal(String.valueOf(Math.pow(10, v)))))
				.setBinding(
					(config, value) -> config.minimumFOV = Math.pow(10, value),
					config -> (int) Math.log10(config.minimumFOV))
				.build())
			.add(OptionImpl.createBuilder(boolean.class, storage)
				.setId(DISABLE)
				.setControl(TickBoxControl::new)
				.setBinding(
					(config, value) -> config.isDisabled = value,
					config -> config.isDisabled)
				.setFlags(OptionFlag.REQUIRES_GAME_RESTART)
				.build())
			.build());
		
		event.getPages().add(new OptionPage(
			GENERAL,
			Component.translatable("zume.options.pages.general"),
			ImmutableList.copyOf(generalGroups)));
		event.getPages().add(new OptionPage(
			ADVANCED,
			Component.translatable("zume.options.pages.advanced"),
			ImmutableList.copyOf(advancedGroups)));
	}
	
}
