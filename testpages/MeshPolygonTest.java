package notProvided.client.testpages;

import java.util.Random;

import geometry.Vertex3D;
import polygon.Polygon;
import polygon.PolygonRenderer;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class MeshPolygonTest {
	public static final boolean NO_PERTURBATION = false;
	public static final boolean USE_PERTURBATION = true;
	
	private static final double FRACTION_OF_PANEL_FOR_DRAWING = 0.9;
	private static final int NUM_ROWS = 9;
	private static final int NUM_COLS = 9;
	private static final int RANGE = 25;
	private static final long SEED = 9876543210L;
	private static final Random random = new Random(SEED);
	
	private final Drawable panel;
	private final PolygonRenderer renderer;
	private final boolean Perturbation;
	Vertex3D startPoint;
	
	public MeshPolygonTest(Drawable panel, PolygonRenderer renderer, boolean Perturbation) {
		this.panel = panel;
		this.renderer = renderer;
		this.Perturbation = Perturbation;
		
		FindStartPoint();
		render();
	}

	private void render() {
		Vertex3D initialVertices[] = new Vertex3D[3];
		Vertex3D Matrix[][] = makeMap();
		for(int row = 0; row < NUM_ROWS; row++)
		{
			for(int col = 0; col < NUM_COLS; col++)
			{				
				initialVertices[0] = new Vertex3D(Matrix[col][row].getX(), Matrix[col][row].getY(), 0.0, Matrix[col][row].getColor());
				initialVertices[1] = new Vertex3D(Matrix[col+1][row].getX(), Matrix[col+1][row].getY(), 0.0, Matrix[col+1][row].getColor());
				initialVertices[2] = new Vertex3D(Matrix[col+1][row+1].getX(), Matrix[col+1][row+1].getY(), 0.0, Matrix[col+1][row+1].getColor());
				Polygon lower = Polygon.make(initialVertices);
				renderer.drawPolygon(lower, panel);
				
				initialVertices[0] = new Vertex3D(Matrix[col][row].getX(), Matrix[col][row].getY(), 0.0, Matrix[col][row].getColor());
				initialVertices[1] = new Vertex3D(Matrix[col+1][row+1].getX(), Matrix[col+1][row+1].getY(), 0.0, Matrix[col+1][row+1].getColor());
				initialVertices[2] = new Vertex3D(Matrix[col][row+1].getX(), Matrix[col][row+1].getY(), 0.0, Matrix[col][row+1].getColor());
				Polygon upper = Polygon.make(initialVertices);
				renderer.drawPolygon(upper, panel);
			}
		}
	}
	
	private Vertex3D[][] makeMap() {
		Vertex3D Matrix[][] = new Vertex3D[NUM_COLS+1][NUM_ROWS+1];
		int sX = startPoint.getIntX();
		int sY = startPoint.getIntY();
		double sideLength = computeSideLength();
		double x, y;
		Color color;
		
		y = sY;
		for(int row = 0; row <= NUM_ROWS; row++)
		{
			x = sX;
			for(int col = 0; col <= NUM_COLS; col++)
			{
				color = Color.random();
				if(Perturbation)
					Matrix[col][row] = new Vertex3D(x+random.nextInt(RANGE)-12,y+random.nextInt(RANGE)-12,0.0, color);
				else Matrix[col][row] = new Vertex3D(x,y,0.0, color);
				x += sideLength;
			}
			y += sideLength;
		}
		return Matrix;
	}

	private double computeSideLength() {		
		return (panel.getHeight() / NUM_ROWS * FRACTION_OF_PANEL_FOR_DRAWING);
	}

	private void FindStartPoint() {
		double startX = panel.getWidth()*(1-FRACTION_OF_PANEL_FOR_DRAWING)/2;
		double startY = panel.getHeight()*(1-FRACTION_OF_PANEL_FOR_DRAWING)/2;
		startPoint = new Vertex3D(startX, startY, 0.0, Color.WHITE);
	}
	
	
	
}
