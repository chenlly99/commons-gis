package com.opengis.gis.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.opengis.gis.io.mif.MifFeature;
import com.opengis.gis.io.mif.MifReader;
import com.opengis.gis.io.mif.MifWriter;
import com.opengis.gis.io.mif.MifWriter.WType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * 
 * @author chenlly(chenlly@126.com)
 * 
 */
public class PathCombiner {

	public static void main(String[] args) {
		// System.out.println(Math.tan(Math.PI * 85 / 180.0));

		String[] comFields = { "KIND", "DIRECTION", "PATHNAME" };
		String[] delKindS = { "|08", "|09", "|0a", "|0b" };
		String[] delKindE = { "06|", "07|", "0b|", "16|", "17|" };
		String[] kindE = { "04|" };
		double coef = 0.02;
		double diff = 1;
		double tan = 10;

		String mifpath = args[0];
		Map<String, List<MifFeature>> fmaps = new HashMap<String, List<MifFeature>>();
		List<MifFeature> flist = new LinkedList<MifFeature>();

		String dpath = mifpath.substring(0, mifpath.lastIndexOf('.'))
				+ "_d.mif";
		MifWriter dmw = new MifWriter(dpath, false);
		boolean dfirst = true;
		int count = 0;
		int countd = 0;
		MifReader mr = new MifReader(mifpath);
		while (mr.hasNext()) {
			MifFeature mf = mr.next();

			count++;
			String kind = mf.getString("KIND").toLowerCase();
			// delete feature by kind
			if (checkKind(kind, delKindS, true)
					|| checkKind(kind, delKindE, false)) {
				countd++;

				// write
				if (dfirst) {
					dmw.writeln(WType.MIF, mf.getSchema().toString());
					dfirst = false;
				}
				reduceCoords(mf, coef);
				dmw.writeln(WType.MID, mf.toMidString());
				dmw.writeln(WType.MIF, mf.toMifString());
				continue;
			}

			// no combining and direct writing
			if (checkKind(kind, kindE, false)) {
				flist.add(mf);
				continue;
			}

			String key = getKey(mf, comFields);
			if (fmaps.containsKey(key)) {
				fmaps.get(key).add(mf);
			} else {
				List<MifFeature> lf = new ArrayList<MifFeature>(1);
				lf.add(mf);
				fmaps.put(key, lf);
			}
		}
		dmw.close();
		System.out.println("total features: " + count);
		System.out.println("delete features: " + countd);
		System.out.println("map size: " + fmaps.size());

		// check and combine in map<list>
		for (Iterator<List<MifFeature>> fi = fmaps.values().iterator(); fi
				.hasNext();) {
			List<MifFeature> lf = fi.next();
			if (lf.size() == 1)
				continue;

			for (int i = 0; i < lf.size(); i++) {
				if (lf.get(i) == null)
					continue;
				for (int j = 0; j < lf.size(); j++) {
					if (i == j || lf.get(j) == null)
						continue;

					MifFeature pre = lf.get(i);
					MifFeature cur = lf.get(j);

					LineString mfLine = (LineString) pre.getGeometry();
					LineString fLine = (LineString) cur.getGeometry();

					Coordinate mfStart = mfLine.getCoordinateN(0);
					Coordinate mfEnd = mfLine.getCoordinateN(mfLine
							.getNumPoints() - 1);
					Coordinate fStart = fLine.getCoordinateN(0);
					Coordinate fEnd = fLine
							.getCoordinateN(fLine.getNumPoints() - 1);

					// geometry1 cross geometry2
					String dir = pre.getString("DIRECTION");
					String name = pre.getString("PATHNAME");
					if (!dir.equals("1")
							|| (!name.isEmpty() && dir.equals("1"))) {
						if (Math.abs(mfEnd.x - fStart.x) <= 2e-6
								&& Math.abs(mfEnd.y - fStart.y) <= 2e-6) {
							pre.setGeometry(end2start(mfLine, fLine));
							lf.set(i, pre);
							lf.set(j, null);
						} else if (Math.abs(mfStart.x - fEnd.x) <= 2e-6
								&& Math.abs(mfStart.y - fEnd.y) <= 2e-6) {
							pre.setGeometry(start2end(mfLine, fLine));
							lf.set(i, pre);
							lf.set(j, null);
						}

						if (dir.equals("1")) {
							if (Math.abs(mfEnd.x - fEnd.x) <= 2e-6
									&& Math.abs(mfEnd.y - fEnd.y) <= 2e-6) {
								pre.setGeometry(end2end(mfLine, fLine));
								lf.set(i, pre);
								lf.set(j, null);
							} else if (Math.abs(mfStart.x - fStart.x) <= 2e-6
									&& Math.abs(mfStart.y - fStart.y) <= 2e-6) {
								pre.setGeometry(start2start(mfLine, fLine));
								lf.set(i, pre);
								lf.set(j, null);
							}
						}
					} else {
						if (Math.abs(mfEnd.x - fStart.x) <= 2e-6
								&& Math.abs(mfEnd.y - fStart.y) <= 2e-6) {
							mfStart = mfLine.getCoordinateN(mfLine
									.getNumPoints() - 2);
							fEnd = fLine.getCoordinateN(1);
							double ypre = (mfEnd.y - mfStart.y)
									/ (mfEnd.x - mfStart.x);
							double ynext = (fEnd.y - fStart.y)
									/ (fEnd.x - fStart.x);
							boolean same = (ypre >= tan || ypre <= -tan)
									&& (ynext >= tan || ynext <= -tan);
							same = same || Math.abs(ynext - ypre) <= diff;
							if (same) {
								pre.setGeometry(end2start(mfLine, fLine));
								lf.set(i, pre);
								lf.set(j, null);
							}
						} else if (Math.abs(mfStart.x - fEnd.x) <= 2e-6
								&& Math.abs(mfStart.y - fEnd.y) <= 2e-6) {
							mfEnd = mfLine.getCoordinateN(1);
							fStart = fLine
									.getCoordinateN(fLine.getNumPoints() - 2);
							double ypre = (mfEnd.y - mfStart.y)
									/ (mfEnd.x - mfStart.x);
							double ynext = (fEnd.y - fStart.y)
									/ (fEnd.x - fStart.x);
							boolean same = (ypre >= tan || ypre <= -tan)
									&& (ynext >= tan || ynext <= -tan);
							same = same || Math.abs(ynext - ypre) <= diff;
							if (same) {
								pre.setGeometry(start2end(mfLine, fLine));
								lf.set(i, pre);
								lf.set(j, null);
							}
						} else if (Math.abs(mfEnd.x - fEnd.x) <= 2e-6
								&& Math.abs(mfEnd.y - fEnd.y) <= 2e-6) {
							mfStart = mfLine.getCoordinateN(mfLine
									.getNumPoints() - 2);
							fStart = fLine
									.getCoordinateN(fLine.getNumPoints() - 2);
							double ypre = (mfEnd.y - mfStart.y)
									/ (mfEnd.x - mfStart.x);
							double ynext = (fStart.y - fEnd.y)
									/ (fStart.x - fEnd.x);
							boolean same = (ypre >= tan || ypre <= -tan)
									&& (ynext >= tan || ynext <= -tan);
							same = same || Math.abs(ynext - ypre) <= diff;
							if (same) {
								pre.setGeometry(end2end(mfLine, fLine));
								lf.set(i, pre);
								lf.set(j, null);
							}
						} else if (Math.abs(mfStart.x - fStart.x) <= 2e-6
								&& Math.abs(mfStart.y - fStart.y) <= 2e-6) {
							mfEnd = mfLine.getCoordinateN(1);
							fEnd = fLine.getCoordinateN(1);
							double ypre = (mfStart.y - mfEnd.y)
									/ (mfStart.x - mfEnd.x);
							double ynext = (fEnd.y - fStart.y)
									/ (fEnd.x - fStart.x);
							boolean same = (ypre >= tan || ypre <= -tan)
									&& (ynext >= tan || ynext <= -tan);
							same = same || Math.abs(ynext - ypre) <= diff;
							if (same) {
								pre.setGeometry(start2start(mfLine, fLine));
								lf.set(i, pre);
								lf.set(j, null);
							}
						}
					}// end if
				}// end for j
			}// end for i
		}
		System.out.println("path combine end!");

		// save file
		String path = mifpath.substring(0, mifpath.lastIndexOf('.')) + "_r.mif";
		MifWriter mw = new MifWriter(path, false);
		boolean first = true;
		for (Iterator<List<MifFeature>> fi = fmaps.values().iterator(); fi
				.hasNext();) {
			List<MifFeature> lf = fi.next();
			for (Iterator<MifFeature> lfi = lf.iterator(); lfi.hasNext();) {
				MifFeature f = lfi.next();
				if (f == null)
					continue;

				// coordinate reduce
				reduceCoords(f, coef);
				if (first) {
					mw.writeln(WType.MIF, f.getSchema().toString());
					first = false;
				}
				mw.writeln(WType.MID, f.toMidString());
				mw.writeln(WType.MIF, f.toMifString());
			}
		}
		fmaps.clear();
		for (Iterator<MifFeature> i = flist.iterator(); i.hasNext();) {
			MifFeature f = i.next();
			if (f == null)
				continue;

			// coordinate reduce
			reduceCoords(f, coef);
			if (first) {
				mw.writeln(WType.MIF, f.getSchema().toString());
				first = false;
			}
			mw.writeln(WType.MID, f.toMidString());
			mw.writeln(WType.MIF, f.toMifString());
		}
		flist.clear();
		mw.close();
	}

	/**
	 * mf[ E---<---S ] and f[ S--->---E ]
	 */
	static LineString start2start(LineString mfLine, LineString fLine) {
		Coordinate[] coords = new Coordinate[mfLine.getNumPoints()
				+ fLine.getNumPoints() - 1];
		for (int i = mfLine.getNumPoints() - 1; i >= 0; i--) {
			coords[mfLine.getNumPoints() - i - 1] = mfLine.getCoordinateN(i);
		}
		for (int i = 1; i < fLine.getNumPoints(); i++) {
			coords[mfLine.getNumPoints() + i - 1] = fLine.getCoordinateN(i);
		}
		return mfLine.getFactory().createLineString(coords);
	}

	/**
	 * mf[ S--->---E ] and f[ E---<---S ]
	 */
	static LineString end2end(LineString mfLine, LineString fLine) {
		Coordinate[] coords = new Coordinate[mfLine.getNumPoints()
				+ fLine.getNumPoints() - 1];
		for (int i = 0; i < mfLine.getNumPoints(); i++) {
			coords[i] = mfLine.getCoordinateN(i);
		}
		for (int i = fLine.getNumPoints() - 2; i >= 0; i--) {
			coords[mfLine.getNumPoints() + fLine.getNumPoints() - 2 - i] = fLine
					.getCoordinateN(i);
		}
		return mfLine.getFactory().createLineString(coords);
	}

	/**
	 * mf[ E---<---S ] and f[ E---<---S ]
	 */
	static LineString start2end(LineString mfLine, LineString fLine) {
		Coordinate[] coords = new Coordinate[mfLine.getNumPoints()
				+ fLine.getNumPoints() - 1];
		for (int i = 0; i < fLine.getNumPoints(); i++) {
			coords[i] = fLine.getCoordinateN(i);
		}
		for (int i = 1; i < mfLine.getNumPoints(); i++) {
			coords[fLine.getNumPoints() + i - 1] = mfLine.getCoordinateN(i);
		}
		return mfLine.getFactory().createLineString(coords);
	}

	/**
	 * mf[ S--->---E ] and f[ S--->---E ]
	 */
	static LineString end2start(LineString mfLine, LineString fLine) {
		Coordinate[] coords = new Coordinate[mfLine.getNumPoints()
				+ fLine.getNumPoints() - 1];
		for (int i = 0; i < mfLine.getNumPoints(); i++) {
			coords[i] = mfLine.getCoordinateN(i);
		}
		for (int i = 1; i < fLine.getNumPoints(); i++) {
			coords[mfLine.getNumPoints() + i - 1] = fLine.getCoordinateN(i);
		}
		return mfLine.getFactory().createLineString(coords);
	}

	static void reduceCoords(MifFeature f, double coef) {
		LineString line = (LineString) f.getGeometry();
		List<Coordinate> coords = new LinkedList<Coordinate>();
		for (int i = 0; i < line.getNumPoints(); i++) {
			Coordinate ccoord = line.getCoordinateN(i);
			ccoord.x = (int) (ccoord.x * 1e6) / 1.0e6;
			ccoord.y = (int) (ccoord.y * 1e6) / 1.0e6;
			if (i == 0 || i == line.getNumPoints() - 1) {
				coords.add(ccoord);
			} else {
				Coordinate pcoord = coords.get(coords.size() - 1);
				pcoord.x = (int) (pcoord.x * 1e6) / 1.0e6;
				pcoord.y = (int) (pcoord.y * 1e6) / 1.0e6;
				Coordinate ncoord = line.getCoordinateN(i + 1);
				ncoord.x = (int) (ncoord.x * 1e6) / 1.0e6;
				ncoord.y = (int) (ncoord.y * 1e6) / 1.0e6;
				double pre = (ccoord.y - pcoord.y) / (ccoord.x - pcoord.x);
				double next = (ncoord.y - ccoord.y) / (ncoord.x - ccoord.x);
				if (((next >= 0 && pre >= 0) || (next <= 0 && pre <= 0))
						&& Math.abs(next - pre) >= coef) {
					coords.add(ccoord);
				} else if ((next > 0 && pre < 0) || (next < 0 && pre > 0)) {
					coords.add(ccoord);
				}
			}
		}
		f.setGeometry(line.getFactory().createLineString(
				coords.toArray(new Coordinate[0])));
	}

	static String getKey(MifFeature mf, String[] comFields) {
		StringBuffer sb = new StringBuffer();
		for (String f : comFields) {
			sb.append(mf.getString(f)).append("|");
		}
		return sb.toString();
	}

	static boolean checkKind(String kind, String[] kinds, boolean head) {
		if (kinds == null || kinds.length == 0)
			return false;

		if (head) {
			kind = "|" + kind;
		} else {
			kind = kind + "|";
		}

		for (String f : kinds) {
			if (kind.indexOf(f) >= 0) {
				return true;
			}
		}
		return false;
	}

}
