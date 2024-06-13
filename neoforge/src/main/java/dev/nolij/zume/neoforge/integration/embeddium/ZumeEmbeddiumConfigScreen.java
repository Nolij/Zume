package dev.nolij.zume.neoforge.integration.embeddium;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import org.embeddedt.embeddium.api.OptionGUIConstructionEvent;
import org.embeddedt.embeddium.api.eventbus.EventHandlerRegistrar;
import org.embeddedt.embeddium.api.options.control.Control;
import org.embeddedt.embeddium.api.options.control.ControlValueFormatter;
import org.embeddedt.embeddium.api.options.control.SliderControl;
import org.embeddedt.embeddium.api.options.control.TickBoxControl;
import org.embeddedt.embeddium.api.options.structure.Option;
import org.embeddedt.embeddium.api.options.structure.OptionFlag;
import org.embeddedt.embeddium.api.options.structure.OptionGroup;
import org.embeddedt.embeddium.api.options.structure.OptionImpl;
import org.embeddedt.embeddium.api.options.structure.OptionPage;

import java.util.ArrayList;
import java.util.List;

import static dev.nolij.zume.neoforge.integration.embeddium.ZumeOptionsStorage.*;

public class ZumeEmbeddiumConfigScreen implements EventHandlerRegistrar.Handler<OptionGUIConstructionEvent> {
	
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
					(config, value) -> config.mouseSensitivityFloor = Math.max(value * 0.01D, 0.01D),
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
						v -> Component.translatable("zume.blocks", v > 0 ? v : 0.5)))
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
