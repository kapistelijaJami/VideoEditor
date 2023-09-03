package videoeditor;

public class Clip implements Comparable<Clip> {
	private double originalStart; //in milliseconds
	private double originalEnd;
	/*private double newStart;
	private double newEnd;*/
	
	private boolean disabled = false;
	
	public Clip(double start, double end) {
		this.originalStart = start;
		this.originalEnd = end;
	}
	
	public double getOriginalStart() {
		return originalStart;
	}
	
	public double getOriginalEnd() {
		return originalEnd;
	}
	
	/*public double getNewStart() {
		return newStart;
	}
	
	public double getNewEnd() {
		return newEnd;
	}*/
	
	public void setOriginalStart(double time) {
		this.originalStart = time;
	}
	
	public void setOriginalEnd(double time) {
		this.originalEnd = time;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	@Override
	public int compareTo(Clip o) {
		return (int) (this.originalStart - o.originalStart) * 1000;
	}
}
