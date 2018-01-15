package notProvided.shading;

import geometry.Vertex3D;
import notProvided.client.interpreter.SimpInterpreter;
import notProvided.geometry.Transformation;
import windowing.graphics.Color;

public class Lighting {
	private Transformation lightCTM;
	private Color intensity;
	private double attA, attB;
	private Vertex3D lightPos;
	
	public Lighting(Transformation CTM, Color intensity, double attA, double attB) {
		lightCTM = Transformation.make();
		lightCTM.Rewrite(CTM.matrixCopy());
		this.intensity = intensity;
		this.attA = attA;
		this.attB = attB;
		this.lightPos = 	new Vertex3D(0, 0, 0, intensity);
	}
	
	public Vertex3D getLightPos() {
		lightPos = lightCTM.multiply(lightPos);
		lightPos = SimpInterpreter.TransToCamera(lightPos);
		return new Vertex3D(lightPos.getPoint3D(), intensity);
	}
	
	public void resetLightPos() {
		lightPos = 	new Vertex3D(0, 0, 0, intensity);
	}
	
	public Color getIntensity() {
		return intensity;
	}
	
	public double getFatti(Vertex3D vertex) {
		double dis = lightPos.distanceCS(vertex);
		
		return (1.0/(attA+attB*dis));
	}
}
