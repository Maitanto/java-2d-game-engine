package engine.controller;

import engine.move.Stunt;
import oop.graphics.Canvas;
//import oop.graphics.VirtualKeyCodes;

/**
 * Using Gal now, unused class
 */
public class Controller implements Canvas.KeyListener {
	// private Stunt stunt = null;

	public Controller(Stunt stunt) {
		// this.stunt = stunt;
	}

	@Override
	public void pressed(Canvas canvas, int keyCode, char keyChar) {

//		if (stunt == null)
//			return;
//		if (keyCode == VirtualKeyCodes.VK_LEFT) {
//			stunt.set(180);
//		} else if (keyCode == VirtualKeyCodes.VK_UP) {
//			stunt.set(270);
//		} else if (keyCode == VirtualKeyCodes.VK_RIGHT) {
//			stunt.set(0);
//		} else if (keyCode == VirtualKeyCodes.VK_DOWN) {
//			stunt.set(90);
//		}
	}

	@Override
	public void released(Canvas canvas, int keyCode, char keyChar) {
//		if (stunt == null)
//			return;
//		if (keyCode == VirtualKeyCodes.VK_LEFT) {
//
//		} else if (keyCode == VirtualKeyCodes.VK_UP) {
//
//		} else if (keyCode == VirtualKeyCodes.VK_RIGHT) {
//
//		} else if (keyCode == VirtualKeyCodes.VK_DOWN) {
//
//		}
	}

	@Override
	public void typed(Canvas canvas, char keyChar) {
//		if (stunt == null)
//			return;
	}

}
