package engine.gal.condition;

import engine.controller.KeyManager;
import engine.entity.Entity;
import oop.graphics.VirtualKeyCodes;

public class KeyCondition extends GALCondition implements VirtualKeyCodes {

	private final String keyName;
	private final int keyCode;

	public KeyCondition(String keyName) {
		if (keyName == null || keyName.isBlank()) {
			throw new IllegalArgumentException("keyName cannot be null or blank");
		}

		this.keyName = keyName.trim();
		this.keyCode = toKeyCode(this.keyName);
	}

	@Override
	public boolean eval(Entity entity) {
		return KeyManager.isPressed(keyCode);
	}

	public String keyName() {
		return keyName;
	}

	public int keyCode() {
		return keyCode;
	}

	private int toKeyCode(String keyName) {
		String key = keyName.trim().toUpperCase();

		switch (key) {
		case "FU":
		case "UP":
			return VK_UP;

		case "FD":
		case "DOWN":
			return VK_DOWN;

		case "FL":
		case "LEFT":
			return VK_LEFT;

		case "FR":
		case "RIGHT":
			return VK_RIGHT;

		case "SPACE":
			return VK_SPACE;

		case "ENTER":
			return VK_ENTER;

		case "ESC":
		case "ESCAPE":
			return VK_ESCAPE;
		case "d":
			return VK_D;

		default:
			return standardKeyCode(key);
		}
	}

	private int standardKeyCode(String key) {
		if (key.length() == 1) {
			char c = key.charAt(0);

			if (c >= 'A' && c <= 'Z') {
				return VK_A + (c - 'A');
			}

			if (c >= '0' && c <= '9') {
				return VK_0 + (c - '0');
			}
		}

		try {
			return Integer.parseInt(key);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Unknown key: " + keyName);
		}
	}
}