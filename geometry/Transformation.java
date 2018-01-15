package notProvided.geometry;

import geometry.Point3DH;
import geometry.Vertex3D;

public class Transformation{
	private double matrix[][];
	private double invMatrix[][];
	
	private Transformation() {
		matrix = new double[4][4];
		invMatrix= new double[4][4];
		for(int i = 0; i <= 3; i++){
			for(int j = 0; j <= 3; j++){
				matrix[i][j] = (i==j)?1:0;
				invMatrix[i][j] = (i==j)?1:0;
			}
		}
	}
	
	public static Transformation ViewToScreen(double viewWidth, double viewHeight) {
		Transformation trans = new Transformation();
		trans.matrix[0][0] = 650*1.0/viewWidth;
		trans.matrix[0][3] = 325.0;
		trans.matrix[1][1] = 650*1.0/viewHeight;
		trans.matrix[1][3] = 325.0;
		return trans;
	}
	
	public void Rewrite(double matrix[][]) {
		this.matrix = matrix;
	}
	
	public void RewriteInv(double Invmatrix[][]) {
		this.invMatrix = Invmatrix;
	}
	
	public double[][] matrixInverse(){
		double ma[][] = new double[4][4];
		for(int i = 0; i <= 3; i++){
			for(int j = 0; j <= 3; j++){
				ma[i][j] = invMatrix[i][j];
			}
		}
		return ma;
	}
	public double[][] matrixCopy(){
		double ma[][] = new double[4][4];
		for(int i = 0; i <= 3; i++){
			for(int j = 0; j <= 3; j++){
				ma[i][j] = matrix[i][j];
			}
		}
		return ma;
	}
	
	public static Transformation make() {
		return new Transformation();
	}
	
	public void scale(double sx, double sy, double sz)
	{
		for(int i = 0; i <= 3; i++)
		{
			matrix[i][0] *=sx;
			matrix[i][1] *=sy;
			matrix[i][2] *=sz;
			
			
				invMatrix[0][i] /= sx;
				invMatrix[1][i] /= sy;
				invMatrix[2][i] /= sz;
			
		}
	}
	
	public void translate(double tx, double ty, double tz)
	{
		for(int i = 0; i <= 3; i++)
		{
			matrix[i][3] += (tx*matrix[i][0]+ty*matrix[i][1]+tz*matrix[i][2]);
			
			
				invMatrix[0][i] -= (tx*invMatrix[3][i]);
				invMatrix[1][i] -= (ty*invMatrix[3][i]);
				invMatrix[2][i] -= (tz*invMatrix[3][i]);
			
		}
		
	}

	public void rotateX(double angle)
	{
		double t1, t2, t3, t4;
		for(int i = 0; i <= 3; i++)
		{
			t1 = matrix[i][1];
			t2 = matrix[i][2];
			matrix[i][1] = t1*Math.cos(angle)+t2*Math.sin(angle);
			matrix[i][2] = -t1*Math.sin(angle)+t2*Math.cos(angle);
			
		
				t3 = invMatrix[1][i];
				t4 = invMatrix[2][i];
				invMatrix[1][i] = t3*Math.cos(angle)+t4*Math.sin(angle);
				invMatrix[2][i] = -t3*Math.sin(angle)+t4*Math.cos(angle);
		
		}
	}

	public void rotateY(double angle)
	{
		double t1, t2, t3, t4;
		for(int i = 0; i <= 3; i++)
		{
			t1 = matrix[i][0];
			t2 = matrix[i][2];
			matrix[i][0] = t1*Math.cos(angle)-t2*Math.sin(angle);
			matrix[i][2] = t2*Math.cos(angle)+t1*Math.sin(angle);
			
		
				t3 = invMatrix[0][i];
				t4 = invMatrix[2][i];
				invMatrix[0][i] = t3*Math.cos(angle)-t4*Math.sin(angle);
				invMatrix[2][i] = t3*Math.sin(angle)+t4*Math.cos(angle);
			
		}
	}

	public void rotateZ(double angle)
	{
		double t1, t2, t3, t4;
		for(int i = 0; i <= 3; i++)
		{
			t1 = matrix[i][0];
			t2 = matrix[i][1];
			matrix[i][0] = t1*Math.cos(angle)+t2*Math.sin(angle);
			matrix[i][1] = -t1*Math.sin(angle)+t2*Math.cos(angle);
			
			t3 = invMatrix[0][i];
			t4 = invMatrix[1][i];
			invMatrix[0][i] = t3*Math.cos(angle)+t4*Math.sin(angle);
			invMatrix[1][i] = -t3*Math.sin(angle)+t4*Math.cos(angle);
			
		}
	}
	// left multiply
	public Vertex3D multiply(Vertex3D vertex) {
		double x = vertex.getX()*matrix[0][0]+vertex.getY()*matrix[0][1]+vertex.getZ()*matrix[0][2]+matrix[0][3];
		double y = vertex.getX()*matrix[1][0]+vertex.getY()*matrix[1][1]+vertex.getZ()*matrix[1][2]+matrix[1][3];
		double z = vertex.getX()*matrix[2][0]+vertex.getY()*matrix[2][1]+vertex.getZ()*matrix[2][2]+matrix[2][3];
		double w = vertex.getX()*matrix[3][0]+vertex.getY()*matrix[3][1]+vertex.getZ()*matrix[3][2]+matrix[3][3];
		
		return vertex.replacePoint(new Point3DH(x, y, z, w));
	}
	// right multiply the inverse
	public Point3DH multiplyInv(Point3DH vector) {
		double x = vector.getX()*invMatrix[0][0]+vector.getY()*invMatrix[1][0]+vector.getZ()*invMatrix[2][0]+invMatrix[3][0];
		double y = vector.getX()*invMatrix[0][1]+vector.getY()*invMatrix[1][1]+vector.getZ()*invMatrix[2][1]+invMatrix[3][1];
		double z = vector.getX()*invMatrix[0][2]+vector.getY()*invMatrix[1][2]+vector.getZ()*invMatrix[2][2]+invMatrix[3][2];
		double w = vector.getX()*invMatrix[0][3]+vector.getY()*invMatrix[1][3]+vector.getZ()*invMatrix[2][3]+invMatrix[3][3];
		
		return new Point3DH(x, y, z, w);
	}
//	public Point3DH multiply(Point3DH vertex) {
//		double x = vertex.getX()*matrix[0][0]+vertex.getY()*matrix[0][1]+vertex.getZ()*matrix[0][2]+matrix[0][3];
//		double y = vertex.getX()*matrix[1][0]+vertex.getY()*matrix[1][1]+vertex.getZ()*matrix[1][2]+matrix[1][3];
//		double z = vertex.getX()*matrix[2][0]+vertex.getY()*matrix[2][1]+vertex.getZ()*matrix[2][2]+matrix[2][3];
//		double w = vertex.getX()*matrix[3][0]+vertex.getY()*matrix[3][1]+vertex.getZ()*matrix[3][2]+matrix[3][3];
//		
//		return new Point3DH(x, y, z, w).euclidean();
//	}
}
