package notProvided.client;

import line.LineRenderer;
import notProvided.line.DDALineRenderer;
import notProvided.polygon.FilledPolygonRenderer;
import notProvided.polygon.WireframePolygonRenderer;
import polygon.PolygonRenderer;

public class RendererTrio {
	private LineRenderer line;
	private PolygonRenderer polygon;
	private WireframePolygonRenderer wireframe;
	
	private RendererTrio() {
		this.line = DDALineRenderer.make();
		this.polygon = FilledPolygonRenderer.make();
		this.wireframe = WireframePolygonRenderer.make();
	}

	public LineRenderer getLineRenderer() {
		return line;
	}

	public PolygonRenderer getFilledRenderer() {
		return polygon;
	}

	public PolygonRenderer getWireframeRenderer() {
		return wireframe;
	}
	public static RendererTrio make() {
		return new RendererTrio();
	}
}
