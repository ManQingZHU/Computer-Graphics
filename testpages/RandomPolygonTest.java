package notProvided.client.testpages;

import java.util.Random;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class RandomPolygonTest {
	private static final int NUM_TRIS = 20;
	private static final int MAX_POS = 300;
	
	private final Drawable panel;
	private final PolygonRenderer renderer;
	public RandomPolygonTest(Drawable panel, PolygonRenderer renderer) {
		this.panel = panel;
		this.renderer = renderer;
		
		render();
	}
	private void render() {
		long SEED = 12341754L;
		Random random = new Random(SEED);
		Vertex3D initialVertices[] = new Vertex3D[3];
		Color color;
		double x, y;
		for(int i = 0; i < NUM_TRIS; i++)
		{
			color = Color.random();
			x= random.nextDouble()*MAX_POS;
			y= random.nextDouble()*MAX_POS;
			initialVertices[0] = new Vertex3D(x, y, 0.0, color);
			
			x= random.nextDouble()*MAX_POS;
			y= random.nextDouble()*MAX_POS;
			initialVertices[1] = new Vertex3D(x, y, 0.0, color);
			
			x= random.nextDouble()*MAX_POS;
			y= random.nextDouble()*MAX_POS;
			initialVertices[2] = new Vertex3D(x, y, 0.0, color);
			
			Polygon p = Polygon.makeEnsuringClockwise(initialVertices);
			renderer.drawPolygon(p, panel);
		}
		
	}

}
