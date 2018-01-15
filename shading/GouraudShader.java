package notProvided.shading;

import java.util.ArrayList;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class GouraudShader implements Shader{
	private Color ambientLight;
	private ArrayList<Lighting> lightPoints;
	private double kSpecular;
	private double specularExp;
	
	public GouraudShader(Color ambientLight, ArrayList<Lighting> lightPoints, double kSpecular, double specularExp) {
		this.ambientLight = ambientLight;
		this.lightPoints = lightPoints;
		this.kSpecular = kSpecular;
		this.specularExp = specularExp;
	}
	
	public Polygon shade(Polygon p) {
		Point3DH Norm;
		Point3DH V;
		Color Kd;
		Vertex3D lightPos;
		Color lightIntensity;
		double Fatti;
		Point3DH Li, Ri;
		double Ks;
		double cos1, cos2, term1, term2;
		double lightCalcR, lightCalcG, lightCalcB;
		Color lightCalc;
		
		
		
		Vertex3D vertex;
		Vertex3D[] newVertices = new Vertex3D[p.length()];
		
		for(int j = 0; j < p.length(); j++) {
			vertex = p.get(j);
			Norm = (Point3DH) vertex.getNormal();
			if(Norm.IsEmpty())
				Norm = p.getNorm();
			
			Kd = vertex.getColor();
			V = new Point3DH(-vertex.getX(), -vertex.getY(), -vertex.getZ());
			V = V.normalize();
			
			lightCalcR = Kd.getR()*ambientLight.getR();
			lightCalcB = Kd.getB()*ambientLight.getB();
			lightCalcG = Kd.getG()*ambientLight.getG();
			
			for(int i = 0; i < lightPoints.size(); i++) {
				lightPos = lightPoints.get(i).getLightPos();
				lightIntensity = lightPos.getColor();
				Fatti = lightPoints.get(i).getFatti(vertex);
				Li = lightPos.getPoint3D().subtract(vertex.getPoint3D());
				Li = Li.normalize();
				cos1 = Norm.dotMultiply(Li);
				if(cos1 < 0) cos1 = 0;
				Ri = Norm.scale(2*cos1).subtract(Li);
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
			lightCalc = new Color(lightCalcR, lightCalcG, lightCalcB);
			newVertices[j] = vertex.replaceColor(lightCalc);
		}
		return Polygon.make(newVertices);
		
		
	}

	@Override
	public Color shade(Vertex3D vertex) {
		// TODO Auto-generated method stub
		return null;
	}
}

