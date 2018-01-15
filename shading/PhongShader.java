package notProvided.shading;

import java.util.ArrayList;

import geometry.Point3DH;
import geometry.Vertex3D;
import polygon.Polygon;
import windowing.graphics.Color;

public class PhongShader implements Shader{
	private Color ambientLight;
	private ArrayList<Lighting> lightPoints;
	private double kSpecular;
	private double specularExp;
	private Color Kd;
	
	public PhongShader(Color ambientLight, ArrayList<Lighting> lightPoints, double kSpecular, double specularExp) {
		this.ambientLight = ambientLight;
		this.lightPoints = lightPoints;
		this.kSpecular = kSpecular;
		this.specularExp = specularExp;
	}
	
	@Override
	public Color shade(Vertex3D vertex) {
		Point3DH Norm = (Point3DH) vertex.getNormal();
		Point3DH V = new Point3DH(-vertex.getCSX(), -vertex.getCSY(), -vertex.getCSZ());
		V = V.normalize();
		Kd = vertex.getColor();

		Vertex3D lightPos;
		Color lightIntensity;
		double Fatti;
		Point3DH Li, Ri;
		double Ks;
		double cos1, cos2, term1, term2;
		
		double lightCalcR, lightCalcG, lightCalcB;
		Color lightCalc;
		
		lightCalcR = Kd.getR()*ambientLight.getR();
		lightCalcB = Kd.getB()*ambientLight.getB();
		lightCalcG = Kd.getG()*ambientLight.getG();
			
		for(int i = 0; i < lightPoints.size(); i++) {
			lightPos = lightPoints.get(i).getLightPos();
			lightIntensity = lightPos.getColor();
			Fatti = lightPoints.get(i).getFatti(vertex);
			Li = lightPos.getPoint3D().subtract(vertex.getCSPoint());
			Li = Li.normalize();
			cos1 = Norm.dotMultiply(Li);
			Ri = Norm.scale(2*cos1).subtract(Li);
			Ri = Ri.normalize();
			cos2 = V.dotMultiply(Ri);
			if(cos1 < 0) {
				cos1 = 0;
			}
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
		
		return lightCalc;
	}
}
