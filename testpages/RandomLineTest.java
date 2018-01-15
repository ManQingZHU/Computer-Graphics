package notProvided.client.testpages;

import java.util.Random;

import geometry.Vertex3D;
import line.LineRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class RandomLineTest {
	private static final int NUM_LINES = 30;
	private static final int MAX_POS = 300;
	
	private final LineRenderer renderer;
	private final Drawable panel;
	Vertex3D p1, p2;

	public RandomLineTest(Drawable panel, LineRenderer renderer) {
		this.panel = panel;
		this.renderer = renderer;
		
		render();
	}
	
	private void render() {
		long SEED = 9876543210L;
		Random random = new Random(SEED);
		for(int i = 0; i < NUM_LINES; i++)
		{
			Color color = Color.random(random);
			double x = random.nextDouble()*MAX_POS;
			double y = random.nextDouble()*MAX_POS;
			p1 = new Vertex3D(x, y, 0.0, color);
			
			x = random.nextDouble()*MAX_POS;
			y = random.nextDouble()*MAX_POS;
			p2 = new Vertex3D(x, y, 0.0, color);
			
			renderer.drawLine(p1, p2, panel);
		}
	}

}
