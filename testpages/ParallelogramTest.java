package notProvided.client.testpages;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.drawable.InvertedYDrawable;
import windowing.graphics.Color;

public class ParallelogramTest {
	private static final int X_L1 = 20;
	private static final int X_R1 = 150;
	private static final int Y_T1 = 150;
	private static final int Y_B1 = 80;
	
	private static final int X_L2 = 160;
	private static final int X_R2 = 240;
	private static final int Y_T2 = 270;
	private static final int Y_B2 = 40;
	
	private static final int P_MAX = 50;
	
	private final LineRenderer renderer;
	private final Drawable panel;
	Vertex3D p1, p2;

	public ParallelogramTest(Drawable panel, LineRenderer renderer) {
		this.panel = new InvertedYDrawable(panel);
		this.renderer = renderer;
		
		render();
	}
	
	private void render() {		
		for(int p = 0; p <= P_MAX; p++) {
			p1 = new Vertex3D(X_L1, Y_B1+p, 0.0, Color.WHITE);
			p2 = new Vertex3D(X_R1, Y_T1+p, 0.0, Color.WHITE);
			renderer.drawLine(p1, p2, panel);
			
			p1 = new Vertex3D(X_L2+p, Y_T2, 0.0, Color.WHITE);
			p2 = new Vertex3D(X_R2+p, Y_B2, 0.0, Color.WHITE);
			renderer.drawLine(p1, p2, panel);
		}
	}

}
