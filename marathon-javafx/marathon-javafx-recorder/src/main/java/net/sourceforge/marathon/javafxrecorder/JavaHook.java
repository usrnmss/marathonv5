package net.sourceforge.marathon.javafxrecorder;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.javafx.stage.StageHelper;
import com.sun.javafx.util.Utils;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import net.sourceforge.marathon.javafxrecorder.component.RFXComponentFactory;
import net.sourceforge.marathon.javafxrecorder.component.RFXComponent;
import net.sourceforge.marathon.javafxrecorder.http.HTTPRecorder;

public class JavaHook implements EventHandler<Event> {

	private static final Logger logger = Logger.getLogger(JavaHook.class.getName());

	public static String DRIVER = "Java";
	public static String DRIVER_VERSION = "1.0";
	public static String PLATFORM = System.getProperty("java.runtime.name");
	public static String PLATFORM_VERSION = System.getProperty("java.version");
	public static String OS = System.getProperty("os.name");
	public static String OS_ARCH = System.getProperty("os.arch");
	public static String OS_VERSION = System.getProperty("os.version");

	private static String windowTitle;

	private JSONOMapConfig objectMapConfiguration;
	private RFXComponentFactory finder;
	private IJSONRecorder recorder;
	private RFXComponent current;

	public JavaHook(int port) {
		try {
			logger.info("Starting HTTP Recorder on : " + port);
			recorder = new HTTPRecorder(port);
			objectMapConfiguration = recorder.getObjectMapConfiguration();
			finder = new RFXComponentFactory(objectMapConfiguration);
			ObservableList<Stage> stages = StageHelper.getStages();
			for (Stage stage : stages) {
				addEventFilter(stage);
			}
			stages.addListener(new ListChangeListener<Stage>() {
				@Override
				public void onChanged(javafx.collections.ListChangeListener.Change<? extends Stage> c) {
					c.next();
					if (c.wasAdded()) {
						List<? extends Stage> addedSubList = c.getAddedSubList();
						for (Stage stage : addedSubList) {
							addEventFilter(stage);
						}
					}
					if (c.wasRemoved()) {
						List<? extends Stage> removed = c.getRemoved();
						for (Stage stage : removed) {
							removeEventFilter(stage);
						}
					}
				}

			});
			// contextMenuHandler = new ContextMenuHandler(recorder, finder);
		} catch (UnknownHostException e) {
			logger.log(Level.WARNING, "Error in Recorder startup", e);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error in Recorder startup", e);
		}
	}

	private static final EventType<?> events[] = { MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED,
			MouseEvent.MOUSE_CLICKED, KeyEvent.KEY_PRESSED, KeyEvent.KEY_RELEASED, KeyEvent.KEY_TYPED };

	private void removeEventFilter(Stage stage) {
		for (EventType<?> eventType : events) {
			stage.getScene().getRoot().removeEventFilter(eventType, JavaHook.this);
		}
	}

	private void addEventFilter(Stage stage) {
		for (EventType<?> eventType : events) {
			stage.getScene().getRoot().addEventFilter(eventType, JavaHook.this);
		}
	}

	public static void premain(final String args) throws Exception {
		logger.info("JavaVersion: " + System.getProperty("java.version"));
		final int port;
		if (args != null && args.trim().length() > 0)
			port = Integer.parseInt(args.trim());
		else
			throw new Exception("Port number not specified");
		windowTitle = System.getProperty("start.window.title", "");
		ObservableList<Stage> stages = StageHelper.getStages();
		stages.addListener(new ListChangeListener<Stage>() {
			boolean done = false;

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Stage> c) {
				if (done)
					return;
				if (!"".equals(windowTitle)) {
					logger.warning("WindowTitle is not supported yet... Ignoring it.");
				}
				c.next();
				if (c.wasAdded()) {
					AccessController.doPrivileged(new PrivilegedAction<Object>() {
						@Override
						public Object run() {
							return new JavaHook(port);
						}
					});
					done = true;
				}
			}
		});
	}

	@Override
	public void handle(Event event) {
		if (!(event.getTarget() instanceof Node) || !(event.getSource() instanceof Node))
			return;
		Node target = (Node) event.getTarget();
		Node source = (Node) event.getSource();
		target = finder.getComponent(target);
		Point2D point = null;
		if (event instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) event;
			point = Utils.pointRelativeTo(source, target, HPos.LEFT, VPos.TOP, me.getX(), me.getY(), false);
			event = new MouseEvent(source, target, me.getEventType(), point.getX(), point.getY(), me.getScreenX(),
					me.getScreenX(), me.getButton(), me.getClickCount(), me.isShiftDown(), me.isControlDown(),
					me.isAltDown(), me.isMetaDown(), me.isPrimaryButtonDown(), me.isMiddleButtonDown(),
					me.isSecondaryButtonDown(), me.isSynthesized(), me.isPopupTrigger(), false, me.getPickResult());
		}
		RFXComponent c = finder.findRComponent((Node) target, point, recorder);
		if (isFocusChangeEvent(event.getEventType()) && !c.equals(current)) {
			if (current != null && isShowing(current))
				current.focusLost(c);
			c.focusGained(current);
			current = c;
		}
		// We Need This.
		if (c.equals(current))
			c = current;
		c.processEvent(event);
	}

	private boolean isShowing(RFXComponent component) {
		try {
			return component.getComponent().getScene().getWindow().isShowing();
		} catch (Throwable t) {
			return false;
		}
	}

	private boolean isFocusChangeEvent(EventType<? extends Event> eventType) {
		boolean b = eventType != MouseEvent.MOUSE_ENTERED && eventType != MouseEvent.MOUSE_ENTERED_TARGET
				&& eventType != MouseEvent.MOUSE_EXITED_TARGET && eventType != MouseEvent.MOUSE_MOVED
				&& eventType != MouseEvent.MOUSE_EXITED;
		if (b) {
			System.out.println("JavaHook.isFocusChangeEvent(): " + eventType);
		}
		return b;
	}
}
