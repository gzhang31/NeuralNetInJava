import java.awt.event.KeyListener;

public class UserInput implements KeyListener {

	@Override
	public void keyPressed(java.awt.event.KeyEvent e) {
		
		switch(e.getKeyChar()) {
		case 'w':
			Main.makeWindow = !Main.makeWindow;
			return;
		}
	}

	@Override
	public void keyReleased(java.awt.event.KeyEvent e) {

	}

	@Override
	public void keyTyped(java.awt.event.KeyEvent e) {
		
	}

}
