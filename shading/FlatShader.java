package notProvided.shading;

import java.util.ArrayList;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class FlatShader implements Shader{
	private Color ambientLight;
	private ArrayList<Lighting> lightPoints;
	private double kSpecular;
	private double specularExp;
	
	public FlatShader(Color ambientLight, ArrayList<Lighting> lightPoints, double kSpecular, double specularExp) {
		this.ambientLight = ambientLight;
		this.lightPoints = lightPoints;
		this.kSpecular = kSpecular;
		this.specularExp = specularExp;
	}
	
	public Polygon shade(Polygon p) {
		Color Kd = p.get(0).getColor();
		Vertex3D center = p.getCenter();
		Point3DH faceNorm = p.getNorm();
		Point3DH V = new Point3DH(-center.getX(), -center.getY(), -center.getZ());
		V = V.normalize();
		
		Vertex3D lightPos;
		Color lightIntensity;
		double Fatti;
		Point3DH Li, Ri;
		
		double Ks;
		double cos1, cos2, term1, term2;
		
		double lightCalcR = Kd.getR()*ambientLight.getR();
		double lightCalcB = Kd.getB()*ambientLight.getB();
		double lightCalcG = Kd.getG()*ambientLight.getG();
		
		int i;
		for(i = 0; i < lightPoints.size(); i++) {
			lightPos = lightPoints.get(i).getLightPos();
			lightIntensity = lightPos.getColor();
			Fatti = lightPoints.get(i).getFatti(center);
			Li = lightPos.getPoint3D().subtract(center.getPoint3D());
			Li = Li.normalize();
			cos1 = faceNorm.dotMultiply(Li);
			if(cos1 < 0) cos1 = 0;
			Ri = faceNorm.scale(2*cos1).subtract(Li);
			Ri = Ri.normalize();
			cos2 = V.dotMultiply(Ri);
			if(cos2 < 0) cos2 = 0;
		
			
			term2 = Math.pow(cos2, specularExp);
			Ks = kSpecular*term2;
			term1 = cos1*Kd.getR();
			lightCalcR += lightIntensity.getR()*Fatti*(term1+Ks);
			
			term1 = cos1*Kd.getG();
			lightCalcG += lightIntensity.getG()*Fatti*(term1+Ks);
			
			term1 = cos1*Kd.getB();
			lightCalcB += lightIntensity.getB()*Fatti*(term1+Ks);
			
			lightPoints.get(i).resetLightPos();
		}
		
		Color lightCalc = new Color(lightCalcR, lightCalcG, lightCalcB);
		
		Vertex3D[] newVertices = new Vertex3D[p.length()];
		for(i = 0; i < p.length(); i++) {
			newVertices[i] = p.get(i).replaceColor(lightCalc);
		}
		return Polygon.make(faceNorm, newVertices);
	}

	@Override
	public Color shade(Vertex3D vertex) {
		// TODO Auto-generated method stub
		return null;
	}

}
