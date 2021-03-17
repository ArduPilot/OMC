/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.airspace;

import eu.mavinci.airspace.Arc.TurnDir;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** */
public class OpenAirspaceParser {

    public static final String encoding = "UTF-8";
    public static final double FT_TO_METER = 0.3048;
    public static final double MNI_TO_GRAD = 1. / 60.;
    public static final double MNI_TO_RAD = MNI_TO_GRAD / 180 * Math.PI;

    private int lineNumber;

    public OpenAirspaceParser(InputStream is) throws IOException {
        airspaces = new ArrayList<IAirspace>();
        try {
            input = new BufferedReader(new InputStreamReader(is, encoding));
            lineNumber = 0;
            parse(input);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public List<IAirspace> getAirspaces() {
        return airspaces;
    }

    boolean isInDebugAC = false;
    String airspaceToDebug = null; // "R31A1 CAZAUX 119.6"; //set null to disable

    Airspace lastAirspace;

    private void parse(BufferedReader rd) throws IOException {
        AirspaceTypes type = null;
        String name = "";
        String floor = "";
        String ceil = "";
        boolean is_circle = false;
        double radius = 0;
        LatLon center_coords = LatLon.ZERO;
        List<IAirspacePointConnector> vertices = new LinkedList<IAirspacePointConnector>();
        TurnDir arc_dir = TurnDir.RIGHT; // this is the default, set always to RIGHT (+) at begin of airspace definition
        boolean has_arc = false;

        while (true) {
            String line = rd.readLine();
            if (line != null) {
                String[] t = line.split(Pattern.quote("*")); // remove comments
                if (t.length > 1) line = t[0];
                line = line.toUpperCase();
                line = line.trim();
            }

            AirspaceTypes lastType = type;
            lineNumber++;
            //			System.out.println(""+lineNumber + ": "+line);
            if (line == null || line.startsWith("AC")) {
                //				System.out.print(name);
                //				System.out.println(type);
                try {
                    try {
                        if (line != null && line.length() > 3) type = airspaceTypefromString(line.substring(3));
                    } catch (Exception e) {
                        type = null;
                        throwParseError("Problems parsing Airspace type", e);
                    }
                    // in case of line==null, dont continue, since this would cause a infinity loop
                    if (line != null && vertices.size() == 0 && center_coords.equals(LatLon.ZERO))
                        continue; // first airspace..
                    if (line != null && name.length() == 0)
                        continue; // probably only brush definition... IGNORE AIRSPACES WITH EMPTY NAME
                    if (lastType != null) {
                        if (is_circle) {
                            lastAirspace =
                                new CircleAirspace(name, lastType, center_coords, radius * Airspace.NM_TO_METER);
                        } else if (has_arc) {
                            lastAirspace = new PolygonAndArcAirspace(name, lastType);
                            for (IAirspacePointConnector v : vertices) {
                                if (v instanceof StraightLine) {
                                    lastAirspace.addVertex(((StraightLine)v).vertex());
                                } else if (v instanceof Arc) {
                                    ((PolygonAndArcAirspace)lastAirspace).addArc((Arc)v);
                                } else throw new RuntimeException("Unknown connecting object");
                            }
                        } else {
                            lastAirspace = new Airspace(name, lastType);
                            for (IAirspacePointConnector v : vertices)
                                lastAirspace.addVertex(v.getConnectingPoints(0).get(0));
                        }

                        parseAlitudeString(floor.trim(), false, lastAirspace);
                        parseAlitudeString(ceil.trim(), true, lastAirspace);
                        lastAirspace.getPolygon(); // assure init polygon
                        if (lastAirspace.getType()
                                != AirspaceTypes.AWY) // TODO reinclude airways as soon they are IMPLEMENTME FIXME
                        airspaces.add(lastAirspace);
                        // TODO check if airspace is valid

                    }
                } catch (Throwable t) {
                    throwParseError("dropping one airspace", t);
                } finally {
                    // init variables to defaults
                    name = "";
                    floor = "";
                    ceil = "";
                    is_circle = false;
                    radius = 0;
                    center_coords = LatLon.ZERO;
                    has_arc = false;
                    arc_dir = Arc.TurnDir.RIGHT;
                    vertices = new LinkedList<IAirspacePointConnector>();
                }

                if (line == null) {
                    return;
                }
            } else if (line.length() == 0) {
                continue;
            } else if (line.startsWith("*") || line.length() == 0) {
                continue; // ignore comment line
            } else if (line.startsWith("AL")) {
                if (line.length() > 3) {
                    floor = line.substring(3).trim();
                } else {
                    floor = "GND";
                }
            } else if (line.startsWith("AN")) {
                name = line.substring(3).trim();
                if (isInDebugAC) {
                    System.out.println(lastAirspace);
                    for (LatLon latLon : lastAirspace.getPolygon()) {
                        System.out.println(latLon);
                    }

                    isInDebugAC = false;
                    return;
                }

                if (name.equals(airspaceToDebug)) {
                    System.out.println("found!");
                    isInDebugAC = true;
                }
                //				System.out.println("name: " + name);
            } else if (line.startsWith("AH")) {
                if (line.length() > 3) {
                    ceil = line.substring(3).trim();
                } else {
                    ceil = "UNL";
                }
            } else if (line.startsWith("SB")) {
                //					System.out.println("ignore select brush command (SB) in line "+lineNumber+":
                // "+line.substring(3).trim());
            } else if (line.startsWith("SP")) {
                //					System.out.println("ignore select pen command (SP) in line "+lineNumber+":
                // "+line.substring(3).trim());
            } else if (line.startsWith("AT")) {
                //					System.out.println("ignore label command (AT) in line "+lineNumber+":
                // "+line.substring(3).trim());
            } else if (line.startsWith("DP")) {
                try {
                    vertices.add(new StraightLine(parseCoordinate(line.substring(3))));
                } catch (Throwable t) {
                    throwParseError("probleme parsing coordinate. -> drop it!", t);
                }
            } else if (line.startsWith("DY")) { // punkte eines airways -> muss nicht geschlossen werden!!
                try {
                    vertices.add(new StraightLine(parseCoordinate(line.substring(3))));
                } catch (Throwable t) {
                    throwParseError("probleme parsing coordinate. -> drop it!", t);
                }
            } else if (line.startsWith("V X=")) {
                try {
                    center_coords = parseCoordinate(line.substring(4));
                } catch (Throwable t) {
                    throwParseError("probleme parsing coordinate. -> drop it!", t);
                }
            } else if (line.startsWith("DC")) {
                is_circle = true;
                radius = Double.valueOf(line.substring(3).trim());
            } else if (line.startsWith("V D=")) {
                has_arc = true;
                if (line.substring(4).trim().equals("-")) arc_dir = Arc.TurnDir.LEFT;
                else if (line.substring(4).trim().equals("+")) arc_dir = Arc.TurnDir.RIGHT;
                else throwParseError("Illegal character");
            } else if (line.startsWith("DB")) {
                //				if (!has_arc) {
                //						System.out.println("Arc definition without Turn direction in line "+lineNumber+", assuming
                // standard clockwise direction");
                //				}
                try {
                    List<LatLon> list = parseCoordinateArray(line.substring(3).trim());
                    vertices.add(new Arc(center_coords, list.get(0), list.get(1), arc_dir));
                    has_arc = true;
                } catch (Throwable t) {
                    throwParseError("probleme parsing coordinate. -> drop it!", t);
                }
            } else if (line.startsWith("DA")) {
                try {
                    String[] str = line.substring(3).trim().split(",");
                    vertices.add(
                        new Arc(
                            center_coords,
                            Double.parseDouble(str[0].trim()),
                            Double.parseDouble(str[1].trim()),
                            Double.parseDouble(str[2].trim()),
                            arc_dir));
                    has_arc = true;
                } catch (Throwable t) {
                    throwParseError("probleme parsing coordinate. -> drop it!", t);
                }
            } else if (line.startsWith("* by ATC")) {
                name = name.concat(" (by ATC)");
            } else if (line.startsWith("V W=")) {
                // width of Airway in nm -> FIXME IMPLEMENT ME
            } else if (line.startsWith("V Z=")) {
                // zoomlevel of Airspace -> to be ignored!
            } else if (line.startsWith("V T=")) {
                // undefined variable -> to be ignored!
            } else if (line.startsWith("AY ")) {
                // undefined variable -> to be ignored!
            } else {
                throwParseError("Unsupported command: \"" + line + "\"");
            }
        }
    }

    protected void throwParseError(String s) {
        ADebug.log.warning("Problem Parsing Airspace in(or before) lineNo=" + lineNumber + ": " + s);
    }

    protected void throwParseError(String s, Throwable t) {
        ADebug.log.log(Level.WARNING, "Problem Parsing Airspace in(or before) lineNo=" + lineNumber + ": " + s, t);
    }

    private int parseAltitudeValue(String str) {
        str = str.trim();
        double unit = FT_TO_METER;

        if (str.endsWith("FT")) {
            str = str.substring(0, str.length() - 2).trim();
            unit = FT_TO_METER;
        } else if (str.endsWith("F")) {
            str = str.substring(0, str.length() - 1).trim();
            unit = FT_TO_METER;
        } else if (str.endsWith("M")) {
            str = str.substring(0, str.length() - 1).trim();
            unit = 1;
        }

        int value;
        if (str.length() == 0) {
            value = 0;
        } else {
            value = Integer.valueOf(str);
        }

        return (int)Math.round(value * unit);
    }

    private String removeNumbersFromString(String str) {
        return str.replaceAll("[0-9]+", Matcher.quoteReplacement(""));
    }

    private void parseAlitudeString(String str, boolean ceiling, Airspace airspace) {
        boolean reference_is_groundActice = false;
        boolean reference_is_seaLevelActive = false;
        double metersGround = 0;
        double metersSeaLevel = 0;
        String[] groundStrings = new String[] {"GND", "SFC", "AGL", "ASFC", "SFC"};
        String[] seaLevelStrings = new String[] {"SEA", "AMSL", "MSL", "STD"};

        boolean done = true;

        if (str.split(Pattern.quote("/")).length == 3) {
            // ground and sealevel in one line like this:
            // "300/900 M GND/MSL" or "300FT/900 M GND/MSL"
            String[] parts = str.split(Pattern.quote("/"));
            String num1 = parts[0].trim();
            parts[1] = parts[1].trim();

            int pos = parts[1].lastIndexOf(" ");
            String type1 = parts[1].substring(pos + 1);
            String type2 = parts[2].trim();
            String num2 = parts[1].substring(0, parts[1].lastIndexOf(" ")).trim();

            // expected content
            // num1="300" or "300FT"
            // num2="900 M"
            // type1 = "GND"
            // type2 = "MSL"

            // try to determine the unit if it is missing in one of the num variables
            String unit1 = removeNumbersFromString(num1);
            String unit2 = removeNumbersFromString(num2);
            if (unit1.length() == 0) {
                num1 += unit2;
            } else if (unit2.length() == 0) {
                num2 += unit1;
            }

            String str1 = num1 + " " + type1;
            String str2 = num2 + " " + type2;
            //			System.out.println("str=" + str + "  ceiling="+ceiling);
            //			System.out.println("str1=" + str1);
            //			System.out.println("str2=" + str2);
            parseAlitudeString(str1, ceiling, airspace);
            //			System.out.println(airspace);
            parseAlitudeString(str2, ceiling, airspace);
            //			System.out.println(airspace);
            //			System.exit(1);
            //			System.out.println(airspace);
            return;
        } else if (str.contains("-") || str.contains("/")) {
            // multiple stuff
            //			System.out.println("str="+str + "   ceiling="+ceiling);
            String[] parts = str.split("[\\/\\-]+");
            if (parts.length != 2)
                throw new IllegalArgumentException("Unrecognized string in line " + lineNumber + ": \"" + str + "\"");
            if (!containsNumbers(parts[1])) {
                //				System.out.println("part0:"+parts[0]);
                //				System.out.println("part1:"+parts[1]);
                parseAlitudeString(parts[0], ceiling, airspace);
                if (parts[1].trim().length() > 0) {
                    // let the second part parsed by the code below and now set the numbers for it
                    metersSeaLevel =
                        metersGround =
                            ceiling
                                ? airspace.getCeilingReferenceGroundOrSeaLevel()
                                : airspace.getFloorReferenceGroundOrSeaLevel();
                    //					System.out.println("###########");
                    //					System.out.println("metersSeaLevel="+metersSeaLevel);
                    //					System.out.println("metersGround="+metersGround);
                    done = false;
                    str = parts[1];
                }
            } else {
                //				System.out.println("str="+str + "   ceiling="+ceiling);
                //				System.out.println("part0="+parts[0]);
                //				System.out.println("part1="+parts[1]);
                parseAlitudeString(parts[0].trim(), ceiling, airspace);
                parseAlitudeString(parts[1].trim(), ceiling, airspace);
                //				System.out.println(airspace);
                return;
            }
        } else if (str.startsWith("UNL")) {
            reference_is_seaLevelActive = true;
            metersSeaLevel = Integer.MAX_VALUE;
        } else if (str.startsWith("FL")) {
            reference_is_seaLevelActive = true;
            str = str.substring(2).trim();

            //			 //sometimes their are a FL AND something else... so use only FL..
            //			int pos = str.indexOf(" ");
            //			if (pos != -1) str = str.substring(0, pos);

            metersSeaLevel = parseAltitudeValue(str) * 100;
        } else {
            done = false;
        }
        //		System.out.println("done1"+done);
        if (!done) {
            for (String postFix : groundStrings) {
                if (str.equals(postFix)) {
                    reference_is_groundActice = true;
                    done = true;
                    break;
                }

                if (str.endsWith(postFix)) {
                    reference_is_groundActice = true;
                    metersGround = parseAltitudeValue(str.substring(0, str.indexOf(postFix)));
                    done = true;
                    break;
                }
            }
        }
        //		System.out.println("done2"+done);
        if (!done) {
            for (String postFix : seaLevelStrings) {
                if (str.equals(postFix)) {
                    reference_is_seaLevelActive = true;
                    done = true;
                    break;
                }

                if (str.endsWith(postFix)) {
                    reference_is_seaLevelActive = true;
                    metersSeaLevel = parseAltitudeValue(str.substring(0, str.indexOf(postFix)));
                    done = true;
                    break;
                }
            }
        }

        if (!done) {
            String num = str.replaceAll("[^0-9]+", Matcher.quoteReplacement(""));
            //			String num = str.trim();
            try {
                int feet = Integer.parseInt(num);
                reference_is_seaLevelActive = true;
                metersSeaLevel = (int)Math.round(feet * FT_TO_METER);
                //				System.err.println("no reference level given in line "+lineNumber+" use MSL as default");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Unrecognized string in line " + lineNumber + ": \"" + str + "\"");
            }
        }
        //		System.out.println("ceiling:"+ceiling);
        //		System.out.println("reference_is_groundActice:"+reference_is_groundActice);
        //		System.out.println("reference_is_seaLevelActive:"+reference_is_seaLevelActive);
        //		System.out.println("metersGround:"+metersGround);
        //		System.out.println("metersSeaLevel:"+metersSeaLevel);

        if (ceiling) {
            if (reference_is_groundActice) airspace.setCeiling(metersGround, true);
            if (reference_is_seaLevelActive) airspace.setCeiling(metersSeaLevel, false);
        } else {
            if (reference_is_groundActice) airspace.setFloor(metersGround, true);
            if (reference_is_seaLevelActive) airspace.setFloor(metersSeaLevel, false);
        }
        //		System.out.println(airspace);
    }

    private boolean containsNumbers(String string) {
        for (int i = 0; i != 10; i++) {
            if (string.contains("" + i)) return true;
        }

        return false;
    }

    private LatLon parseCoordinate(String str) {
        str = str.replaceAll("::", Matcher.quoteReplacement(":"));
        str = str.replaceAll("[^0-9:NESW\\.]+", Matcher.quoteReplacement(""));
        //		System.out.println("parse line: "+lineNumber + "  coordinate: "+str);
        StringTokenizer tok = new StringTokenizer(str, ":NESW", true);

        Angle latitude = parseDegree(tok, true);
        Angle longitude = parseDegree(tok, false);

        //		System.out.println(str + " -> " +new LatLon(latitude, longitude));
        return new LatLon(latitude, longitude);
    }

    private Angle parseDegree(StringTokenizer tok, boolean northSouthTrue) {
        double degrees = Double.parseDouble(tok.nextToken());
        String northSouth = tok.nextToken();
        double minutes = 0, seconds = 0;
        if (northSouth.equals(":")) {
            minutes = Double.parseDouble(tok.nextToken());
            northSouth = tok.nextToken(); // guess its ":"
            if (northSouth.equals(":")) {
                seconds = Double.parseDouble(tok.nextToken());
                northSouth = tok.nextToken();
            }
        }

        Angle angle = Angle.fromDegrees(degrees + minutes / 60 + seconds / 3600);
        if (northSouthTrue) { // latitude
            if (!northSouth.equals("N") && !northSouth.equals("S")) throw new RuntimeException();
            if (northSouth.equals("S")) angle = angle.multiply(-1);
            if (!Angle.isValidLatitude(angle.degrees)) throw new RuntimeException();
        } else { // longitude
            if (!northSouth.equals("E") && !northSouth.equals("W")) throw new RuntimeException();
            if (northSouth.equals("W")) angle = angle.multiply(-1);
            if (!Angle.isValidLongitude(angle.degrees)) throw new RuntimeException();
        }

        return angle;
    }

    private List<LatLon> parseCoordinateArray(String str) {
        StringTokenizer line = new StringTokenizer(str, ",", false);
        ArrayList<LatLon> list = new ArrayList<LatLon>();
        while (line.hasMoreTokens()) {
            list.add(parseCoordinate(line.nextToken()));
        }

        return list;
    }

    private BufferedReader input;
    private List<IAirspace> airspaces;

    public static void main(String[] args) {
        try (FileInputStream inputPars = new FileInputStream(new File("/home/marco/Downloads/France2011-03a.txt"))) {
            //			OpenAirspaceParser pars = new OpenAirspaceParser(new FileInputStream(new
            // File("/home/marco/mavinci/drohne-src/trunk/MAVinciAirspaces/resource/eu/mavinci/airspace/de.txt")));
            //			OpenAirspaceParser pars = new OpenAirspaceParser(new FileInputStream(new
            // File("/home/marco/mavinci/drohne-src/trunk/MAVinciAirspaces/resource/eu/mavinci/airspace/Air_Swiss_2.txt")));
            OpenAirspaceParser pars = new OpenAirspaceParser(inputPars);
            //			OpenAirspaceParser pars = new OpenAirspaceParser(new FileInputStream(new
            // File("/home/marco/Downloads/Eire2008.txt")));
            for (IAirspace a : pars.getAirspaces()) {
                try {
                    a.getPolygon();
                } catch (Throwable t) {
                    System.out.println(a.getName());
                    t.printStackTrace();
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private AirspaceTypes airspaceTypefromString(String str) throws Exception {
        str = str.trim().toUpperCase();
        if (str.equals("R")) return AirspaceTypes.Restricted;
        if (str.equals("Q")) return AirspaceTypes.Danger;
        if (str.equals("P") || str.equals("ZP")) // TODO ZP seems to be spain class for protected.... check this!
        return AirspaceTypes.Prohibited;
        if (str.equals("A")) return AirspaceTypes.ClassA;
        if (str.equals("B")) return AirspaceTypes.ClassB;
        if (str.equals("C")) return AirspaceTypes.ClassC;
        if (str.equals("D")) return AirspaceTypes.ClassD;
        if (str.equals("E")) return AirspaceTypes.ClassE;
        if (str.equals("F")) return AirspaceTypes.ClassF;
        if (str.equals("G")
                || str.equals(
                    "SV")) // TODO SV seems to be a spain class for glider flying areas... I have to think about how to
                           // handle them
        return AirspaceTypes.ClassG;
        if (str.equals("GP")) return AirspaceTypes.GliderProhibited;
        if (str.equals("CTR")) return AirspaceTypes.CTR;
        if (str.equals("RMZ")) return AirspaceTypes.RMZ;
        if (str.equals("W")) return AirspaceTypes.WaveWindow;
        if (str.equals("TMZ")) return AirspaceTypes.TMZ;
        if (str.equals("UKN")) return AirspaceTypes.UKN;
        if (str.equals("AWY")) return AirspaceTypes.AWY;
        throw new Exception("unknown airspace type: " + str);
    }
}
