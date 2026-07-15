package cn.net.rms.xaeromapsync_r.xaero;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class ReflectiveXaeroWaypointBridge implements XaeroWaypointBridge {
	private final Method getCurrentSession;
	private final Method getWaypointsManager;
	private final Method getModMain;
	private final Method getCurrentWorld;
	private final Method getAutoWorld;
	private final Method getCurrentSet;
	private final Method getSets;
	private final Method getList;
	private final Method getSetName;
	private final Method getSettings;
	private final Method saveWaypoints;
	private final Constructor<?> waypointConstructor;
	private final Method getX;
	private final Method getY;
	private final Method getZ;
	private final Method getName;
	private final Method getSymbol;
	private final Method getColor;
	private final Method setX;
	private final Method setY;
	private final Method setZ;
	private final Method setName;
	private final Method setSymbol;
	private final Method setColor;

	ReflectiveXaeroWaypointBridge(ClassLoader classLoader) throws ReflectiveOperationException {
		Class<?> sessionClass = Class.forName("xaero.common.XaeroMinimapSession", false, classLoader);
		Class<?> managerClass = Class.forName("xaero.common.minimap.waypoints.WaypointsManager", false, classLoader);
		Class<?> worldClass = Class.forName("xaero.common.minimap.waypoints.WaypointWorld", false, classLoader);
		Class<?> setClass = Class.forName("xaero.common.minimap.waypoints.WaypointSet", false, classLoader);
		Class<?> waypointClass = Class.forName("xaero.common.minimap.waypoints.Waypoint", false, classLoader);
		Class<?> modMainClass = Class.forName("xaero.common.AXaeroMinimap", false, classLoader);
		Class<?> settingsClass = Class.forName("xaero.common.settings.ModSettings", false, classLoader);

		getCurrentSession = requireMethod(sessionClass, "getCurrentSession", sessionClass);
		if (!Modifier.isStatic(getCurrentSession.getModifiers())) {
			throw new NoSuchMethodException("XaeroMinimapSession.getCurrentSession must be static");
		}
		getWaypointsManager = requireMethod(sessionClass, "getWaypointsManager", managerClass);
		getModMain = requireMethod(sessionClass, "getModMain", modMainClass);
		getCurrentWorld = requireMethod(managerClass, "getCurrentWorld", worldClass);
		getAutoWorld = requireMethod(managerClass, "getAutoWorld", worldClass);
		getCurrentSet = requireMethod(worldClass, "getCurrentSet", setClass);
		getSets = requireMethod(worldClass, "getSets", java.util.HashMap.class);
		getList = requireMethod(setClass, "getList", java.util.ArrayList.class);
		getSetName = requireMethod(setClass, "getName", String.class);
		getSettings = requireMethod(modMainClass, "getSettings", settingsClass);
		saveWaypoints = requireMethod(settingsClass, "saveWaypoints", void.class, worldClass);
		waypointConstructor = waypointClass.getConstructor(int.class, int.class, int.class, String.class, String.class, int.class);
		getX = requireMethod(waypointClass, "getX", int.class);
		getY = requireMethod(waypointClass, "getY", int.class);
		getZ = requireMethod(waypointClass, "getZ", int.class);
		getName = requireMethod(waypointClass, "getName", String.class);
		getSymbol = requireMethod(waypointClass, "getSymbol", String.class);
		getColor = requireMethod(waypointClass, "getColor", int.class);
		setX = requireMethod(waypointClass, "setX", void.class, int.class);
		setY = requireMethod(waypointClass, "setY", void.class, int.class);
		setZ = requireMethod(waypointClass, "setZ", void.class, int.class);
		setName = requireMethod(waypointClass, "setName", void.class, String.class);
		setSymbol = requireMethod(waypointClass, "setSymbol", void.class, String.class);
		setColor = requireMethod(waypointClass, "setColor", void.class, int.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Target currentTarget() throws ReflectiveOperationException {
		Object world = currentWorld();
		Object set = invoke(getCurrentSet, world);
		if (set == null) {
			throw new IllegalStateException("Xaero current waypoint set is not initialized");
		}
		return new Target(world, (List<Object>) invoke(getList, set));
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<LocalWaypointValues> readLocalWaypoints() throws ReflectiveOperationException {
		Object world = autoWorld();
		Map<String, Object> sets = (Map<String, Object>) invoke(getSets, world);
		if (sets == null) {
			throw new IllegalStateException("Xaero waypoint sets are not initialized");
		}

		List<LocalWaypointValues> result = new ArrayList<>();
		for (Object set : new ArrayList<>(sets.values())) {
			if (set == null) {
				continue;
			}
			String category = (String) invoke(getSetName, set);
			List<Object> waypoints = (List<Object>) invoke(getList, set);
			for (Object waypoint : new ArrayList<>(waypoints)) {
				WaypointValues values = readValues(waypoint);
				if (!XaeroWaypointIdentity.isManagedName(values.name)) {
					result.add(new LocalWaypointValues(values, category));
				}
			}
		}
		return result;
	}

	@Override
	public Object create(WaypointValues values) throws ReflectiveOperationException {
		try {
			return waypointConstructor.newInstance(values.x, values.y, values.z, values.name, values.symbol, values.color);
		} catch (InvocationTargetException exception) {
			throw unwrap(exception);
		}
	}

	@Override
	public WaypointValues read(Object waypoint) throws ReflectiveOperationException {
		return readValues(waypoint);
	}

	private WaypointValues readValues(Object waypoint) throws ReflectiveOperationException {
		return new WaypointValues(
			(int) invoke(getX, waypoint),
			(int) invoke(getY, waypoint),
			(int) invoke(getZ, waypoint),
			(String) invoke(getName, waypoint),
			(String) invoke(getSymbol, waypoint),
			(int) invoke(getColor, waypoint)
		);
	}

	private Object currentWorld() throws ReflectiveOperationException {
		Object manager = currentManager();
		Object world = invoke(getCurrentWorld, manager);
		if (world == null) {
			throw new IllegalStateException("Xaero current waypoint world is not initialized");
		}
		return world;
	}

	private Object autoWorld() throws ReflectiveOperationException {
		Object manager = currentManager();
		Object world = invoke(getAutoWorld, manager);
		if (world == null) {
			throw new IllegalStateException("Xaero automatic waypoint world is not initialized");
		}
		return world;
	}

	private Object currentManager() throws ReflectiveOperationException {
		Object session = invoke(getCurrentSession, null);
		if (session == null) {
			throw new IllegalStateException("Xaero minimap session is not initialized");
		}
		Object manager = invoke(getWaypointsManager, session);
		if (manager == null) {
			throw new IllegalStateException("Xaero waypoints manager is not initialized");
		}
		return manager;
	}

	@Override
	public void update(Object waypoint, WaypointValues values) throws ReflectiveOperationException {
		invoke(setX, waypoint, values.x);
		invoke(setY, waypoint, values.y);
		invoke(setZ, waypoint, values.z);
		invoke(setName, waypoint, values.name);
		invoke(setSymbol, waypoint, values.symbol);
		invoke(setColor, waypoint, values.color);
	}

	@Override
	public void save(Object world) throws ReflectiveOperationException {
		Object session = invoke(getCurrentSession, null);
		Object modMain = invoke(getModMain, session);
		Object settings = invoke(getSettings, modMain);
		invoke(saveWaypoints, settings, world);
	}

	private static Method requireMethod(Class<?> owner, String name, Class<?> returnType, Class<?>... parameterTypes) throws NoSuchMethodException {
		Method method = owner.getMethod(name, parameterTypes);
		if (method.getReturnType() != returnType) {
			throw new NoSuchMethodException(owner.getName() + "." + name + " has return type " + method.getReturnType().getName() + ", expected " + returnType.getName());
		}
		return method;
	}

	private static Object invoke(Method method, Object target, Object... arguments) throws ReflectiveOperationException {
		try {
			return method.invoke(target, arguments);
		} catch (InvocationTargetException exception) {
			throw unwrap(exception);
		}
	}

	private static ReflectiveOperationException unwrap(InvocationTargetException exception) throws ReflectiveOperationException {
		Throwable cause = exception.getCause();
		if (cause instanceof ReflectiveOperationException) {
			return (ReflectiveOperationException) cause;
		}
		if (cause instanceof RuntimeException) {
			throw (RuntimeException) cause;
		}
		if (cause instanceof Error) {
			throw (Error) cause;
		}
		return new ReflectiveOperationException("Xaero invocation failed", cause);
	}
}
