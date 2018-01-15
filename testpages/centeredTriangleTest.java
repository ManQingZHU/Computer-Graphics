package notProvided.client.testpages;

import java.util.Random;

import geometry.Vertex3D;
import notProvided.client.zBufferingDrawable;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class centeredTriangleTest {
	private static final int radius = 275;
	private static final double colorV[] = {1, 0.85, 0.7, 0.55, 0.4, 0.25};
	private static final int NUM_OF_TRIANGLE = 6;
	private static final long SEED = 54321678L;
	private static final Random random = new Random(SEED);
	
	private Drawable panel;
	private final PolygonRenderer renderer;
	Vertex3D center;
	
	public centeredTriangleTest(Drawable panel, PolygonRenderer renderer) {
		this.panel = new zBufferingDrawable(panel);
//		this.panel = panel;
		this.renderer = renderer;
		
		makeCenter();
		render();
	}

	private void makeCenter() {
		int centerX = panel.getWidth() / 2;
		int centerY = panel.getHeight() / 2;
		center = new Vertex3D(centerX, centerY, 0, Color.WHITE);
	}
	
	private Vertex3D radialPoint(double angle, double z, Color color) {
		angle = angle *Math.PI/180;
		double x = center.getX() + radius * Math.cos(angle);
		double y = center.getY() + radius * Math.sin(angle);
		return new Vertex3D(x, y, z, color);
	}
	
	private void render() {
		double rotationDegree;
		double z;
		Color color;
		Vertex3D initialVertices[] = new Vertex3D[3];
		
		for(int i = 0; i < NUM_OF_TRIANGLE; i++)
		{
			z = -random.nextInt(199)-1;
			rotationDegree = random.nextInt(121);
			color = new Color(colorV[i], colorV[i], colorV[i]);
			
			initialVertices[0] = radialPoint(rotationDegree, z, color);
			initialVertices[1] = radialPoint(rotationDegree+120, z, color);
			initialVertices[2] = radialPoint(rotationDegree+240, z, color);
			Polygon triangle = Polygon.make(initialVertices);
			renderer.drawPolygon(triangle, panel);
		}
		
	}
}
