package com.opengis.gis.geo;

/**
 * 
 * @author chenlly(chenlly@126.com)
 * 
 */
public class Pos {
	public int x;
	public int y;

	private int buf;

	public Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setBuffer(int buf) {
		this.buf = buf;
	}

	public boolean equals(Object pt) {
		if (pt instanceof Pos)
			return (Math.abs(this.x - ((Pos) pt).x) <= buf && Math.abs(this.y
					- ((Pos) pt).y) <= buf);
		return false;
	}

	public int hashCode() {
		return Integer.valueOf(x + "" + y);
	}

}
