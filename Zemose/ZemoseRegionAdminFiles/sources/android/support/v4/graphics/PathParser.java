package android.support.v4.graphics;

import android.graphics.Path;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.util.Log;
import java.util.ArrayList;

@RestrictTo({Scope.LIBRARY_GROUP})
public class PathParser {
    private static final String LOGTAG = "PathParser";

    private static class ExtractFloatResult {
        int mEndPosition;
        boolean mEndWithNegOrDot;

        ExtractFloatResult() {
        }
    }

    public static class PathDataNode {
        @RestrictTo({Scope.LIBRARY_GROUP})
        public float[] mParams;
        @RestrictTo({Scope.LIBRARY_GROUP})
        public char mType;

        PathDataNode(char type, float[] params) {
            this.mType = type;
            this.mParams = params;
        }

        PathDataNode(PathDataNode n) {
            this.mType = n.mType;
            float[] fArr = n.mParams;
            this.mParams = PathParser.copyOfRange(fArr, 0, fArr.length);
        }

        public static void nodesToPath(PathDataNode[] node, Path path) {
            float[] current = new float[6];
            char previousCommand = 'm';
            for (int i = 0; i < node.length; i++) {
                addCommand(path, current, previousCommand, node[i].mType, node[i].mParams);
                previousCommand = node[i].mType;
            }
        }

        public void interpolatePathDataNode(PathDataNode nodeFrom, PathDataNode nodeTo, float fraction) {
            int i = 0;
            while (true) {
                float[] fArr = nodeFrom.mParams;
                if (i < fArr.length) {
                    this.mParams[i] = (fArr[i] * (1.0f - fraction)) + (nodeTo.mParams[i] * fraction);
                    i++;
                } else {
                    return;
                }
            }
        }

        private static void addCommand(Path path, float[] current, char previousCmd, char cmd, float[] val) {
            int incr;
            int k;
            float reflectiveCtrlPointX;
            float reflectiveCtrlPointY;
            float reflectiveCtrlPointY2;
            float reflectiveCtrlPointX2;
            Path path2 = path;
            float[] fArr = val;
            float currentX = current[0];
            float currentY = current[1];
            float ctrlPointX = current[2];
            float ctrlPointY = current[3];
            float currentSegmentStartX = current[4];
            float currentSegmentStartY = current[5];
            switch (cmd) {
                case 'A':
                case 'a':
                    incr = 7;
                    break;
                case 'C':
                case 'c':
                    incr = 6;
                    break;
                case 'H':
                case 'V':
                case 'h':
                case 'v':
                    incr = 1;
                    break;
                case 'L':
                case 'M':
                case 'T':
                case 'l':
                case 'm':
                case 't':
                    incr = 2;
                    break;
                case 'Q':
                case 'S':
                case 'q':
                case 's':
                    incr = 4;
                    break;
                case 'Z':
                case 'z':
                    path.close();
                    currentX = currentSegmentStartX;
                    currentY = currentSegmentStartY;
                    ctrlPointX = currentSegmentStartX;
                    ctrlPointY = currentSegmentStartY;
                    path2.moveTo(currentX, currentY);
                    incr = 2;
                    break;
                default:
                    incr = 2;
                    break;
            }
            int k2 = 0;
            float currentX2 = currentX;
            float currentY2 = currentY;
            float ctrlPointX2 = ctrlPointX;
            float ctrlPointY2 = ctrlPointY;
            float currentSegmentStartX2 = currentSegmentStartX;
            float currentSegmentStartY2 = currentSegmentStartY;
            char previousCmd2 = previousCmd;
            while (k2 < fArr.length) {
                switch (cmd) {
                    case 'A':
                        k = k2;
                        drawArc(path, currentX2, currentY2, fArr[k + 5], fArr[k + 6], fArr[k + 0], fArr[k + 1], fArr[k + 2], fArr[k + 3] != 0.0f, fArr[k + 4] != 0.0f);
                        currentX2 = fArr[k + 5];
                        currentY2 = fArr[k + 6];
                        ctrlPointX2 = currentX2;
                        ctrlPointY2 = currentY2;
                        break;
                    case 'C':
                        float f = currentY2;
                        float f2 = currentX2;
                        k = k2;
                        path.cubicTo(fArr[k + 0], fArr[k + 1], fArr[k + 2], fArr[k + 3], fArr[k + 4], fArr[k + 5]);
                        currentX2 = fArr[k + 4];
                        currentY2 = fArr[k + 5];
                        ctrlPointX2 = fArr[k + 2];
                        ctrlPointY2 = fArr[k + 3];
                        break;
                    case 'H':
                        float f3 = currentX2;
                        k = k2;
                        path2.lineTo(fArr[k + 0], currentY2);
                        currentX2 = fArr[k + 0];
                        break;
                    case 'L':
                        float f4 = currentY2;
                        float f5 = currentX2;
                        k = k2;
                        path2.lineTo(fArr[k + 0], fArr[k + 1]);
                        currentX2 = fArr[k + 0];
                        currentY2 = fArr[k + 1];
                        break;
                    case 'M':
                        float f6 = currentY2;
                        float f7 = currentX2;
                        k = k2;
                        currentX2 = fArr[k + 0];
                        currentY2 = fArr[k + 1];
                        if (k <= 0) {
                            path2.moveTo(fArr[k + 0], fArr[k + 1]);
                            currentSegmentStartX2 = currentX2;
                            currentSegmentStartY2 = currentY2;
                            break;
                        } else {
                            path2.lineTo(fArr[k + 0], fArr[k + 1]);
                            break;
                        }
                    case 'Q':
                        float f8 = currentY2;
                        float f9 = currentX2;
                        k = k2;
                        path2.quadTo(fArr[k + 0], fArr[k + 1], fArr[k + 2], fArr[k + 3]);
                        float ctrlPointX3 = fArr[k + 0];
                        float ctrlPointY3 = fArr[k + 1];
                        currentX2 = fArr[k + 2];
                        currentY2 = fArr[k + 3];
                        ctrlPointX2 = ctrlPointX3;
                        ctrlPointY2 = ctrlPointY3;
                        break;
                    case 'S':
                        char previousCmd3 = previousCmd2;
                        float currentY3 = currentY2;
                        float currentX3 = currentX2;
                        k = k2;
                        float reflectiveCtrlPointX3 = currentX3;
                        float reflectiveCtrlPointY3 = currentY3;
                        if (previousCmd3 == 'c' || previousCmd3 == 's' || previousCmd3 == 'C' || previousCmd3 == 'S') {
                            reflectiveCtrlPointX = (currentX3 * 2.0f) - ctrlPointX2;
                            reflectiveCtrlPointY = (currentY3 * 2.0f) - ctrlPointY2;
                        } else {
                            reflectiveCtrlPointX = reflectiveCtrlPointX3;
                            reflectiveCtrlPointY = reflectiveCtrlPointY3;
                        }
                        path.cubicTo(reflectiveCtrlPointX, reflectiveCtrlPointY, fArr[k + 0], fArr[k + 1], fArr[k + 2], fArr[k + 3]);
                        ctrlPointX2 = fArr[k + 0];
                        ctrlPointY2 = fArr[k + 1];
                        currentX2 = fArr[k + 2];
                        currentY2 = fArr[k + 3];
                        break;
                    case 'T':
                        char previousCmd4 = previousCmd2;
                        float currentY4 = currentY2;
                        float currentX4 = currentX2;
                        k = k2;
                        float reflectiveCtrlPointX4 = currentX4;
                        float reflectiveCtrlPointY4 = currentY4;
                        if (previousCmd4 == 'q' || previousCmd4 == 't' || previousCmd4 == 'Q' || previousCmd4 == 'T') {
                            reflectiveCtrlPointX4 = (currentX4 * 2.0f) - ctrlPointX2;
                            reflectiveCtrlPointY4 = (currentY4 * 2.0f) - ctrlPointY2;
                        }
                        path2.quadTo(reflectiveCtrlPointX4, reflectiveCtrlPointY4, fArr[k + 0], fArr[k + 1]);
                        float ctrlPointX4 = reflectiveCtrlPointX4;
                        float ctrlPointY4 = reflectiveCtrlPointY4;
                        currentX2 = fArr[k + 0];
                        currentY2 = fArr[k + 1];
                        ctrlPointX2 = ctrlPointX4;
                        ctrlPointY2 = ctrlPointY4;
                        break;
                    case 'V':
                        float f10 = currentY2;
                        k = k2;
                        path2.lineTo(currentX2, fArr[k + 0]);
                        currentY2 = fArr[k + 0];
                        break;
                    case 'a':
                        float f11 = fArr[k2 + 5] + currentX2;
                        float currentY5 = currentY2;
                        float currentX5 = currentX2;
                        boolean z = fArr[k2 + 3] != 0.0f;
                        k = k2;
                        drawArc(path, currentX2, currentY2, f11, fArr[k2 + 6] + currentY2, fArr[k2 + 0], fArr[k2 + 1], fArr[k2 + 2], z, fArr[k2 + 4] != 0.0f);
                        currentX2 = currentX5 + fArr[k + 5];
                        currentY2 = currentY5 + fArr[k + 6];
                        ctrlPointX2 = currentX2;
                        ctrlPointY2 = currentY2;
                        break;
                    case 'c':
                        path.rCubicTo(fArr[k2 + 0], fArr[k2 + 1], fArr[k2 + 2], fArr[k2 + 3], fArr[k2 + 4], fArr[k2 + 5]);
                        float ctrlPointX5 = fArr[k2 + 2] + currentX2;
                        float ctrlPointY5 = fArr[k2 + 3] + currentY2;
                        currentX2 += fArr[k2 + 4];
                        currentY2 += fArr[k2 + 5];
                        ctrlPointX2 = ctrlPointX5;
                        ctrlPointY2 = ctrlPointY5;
                        k = k2;
                        break;
                    case 'h':
                        path2.rLineTo(fArr[k2 + 0], 0.0f);
                        currentX2 += fArr[k2 + 0];
                        k = k2;
                        break;
                    case 'l':
                        path2.rLineTo(fArr[k2 + 0], fArr[k2 + 1]);
                        currentX2 += fArr[k2 + 0];
                        currentY2 += fArr[k2 + 1];
                        k = k2;
                        break;
                    case 'm':
                        currentX2 += fArr[k2 + 0];
                        currentY2 += fArr[k2 + 1];
                        if (k2 <= 0) {
                            path2.rMoveTo(fArr[k2 + 0], fArr[k2 + 1]);
                            currentSegmentStartX2 = currentX2;
                            currentSegmentStartY2 = currentY2;
                            k = k2;
                            break;
                        } else {
                            path2.rLineTo(fArr[k2 + 0], fArr[k2 + 1]);
                            k = k2;
                            break;
                        }
                    case 'q':
                        path2.rQuadTo(fArr[k2 + 0], fArr[k2 + 1], fArr[k2 + 2], fArr[k2 + 3]);
                        float ctrlPointX6 = fArr[k2 + 0] + currentX2;
                        float ctrlPointY6 = fArr[k2 + 1] + currentY2;
                        currentX2 += fArr[k2 + 2];
                        currentY2 += fArr[k2 + 3];
                        ctrlPointX2 = ctrlPointX6;
                        ctrlPointY2 = ctrlPointY6;
                        k = k2;
                        break;
                    case 's':
                        if (previousCmd2 == 'c' || previousCmd2 == 's' || previousCmd2 == 'C' || previousCmd2 == 'S') {
                            reflectiveCtrlPointX2 = currentX2 - ctrlPointX2;
                            reflectiveCtrlPointY2 = currentY2 - ctrlPointY2;
                        } else {
                            reflectiveCtrlPointX2 = 0.0f;
                            reflectiveCtrlPointY2 = 0.0f;
                        }
                        char c = previousCmd2;
                        path.rCubicTo(reflectiveCtrlPointX2, reflectiveCtrlPointY2, fArr[k2 + 0], fArr[k2 + 1], fArr[k2 + 2], fArr[k2 + 3]);
                        float ctrlPointX7 = fArr[k2 + 0] + currentX2;
                        float ctrlPointY7 = fArr[k2 + 1] + currentY2;
                        currentX2 += fArr[k2 + 2];
                        currentY2 += fArr[k2 + 3];
                        ctrlPointX2 = ctrlPointX7;
                        ctrlPointY2 = ctrlPointY7;
                        k = k2;
                        break;
                    case 't':
                        float reflectiveCtrlPointX5 = 0.0f;
                        float reflectiveCtrlPointY5 = 0.0f;
                        if (previousCmd2 == 'q' || previousCmd2 == 't' || previousCmd2 == 'Q' || previousCmd2 == 'T') {
                            reflectiveCtrlPointX5 = currentX2 - ctrlPointX2;
                            reflectiveCtrlPointY5 = currentY2 - ctrlPointY2;
                        }
                        path2.rQuadTo(reflectiveCtrlPointX5, reflectiveCtrlPointY5, fArr[k2 + 0], fArr[k2 + 1]);
                        float ctrlPointX8 = currentX2 + reflectiveCtrlPointX5;
                        float ctrlPointY8 = currentY2 + reflectiveCtrlPointY5;
                        currentX2 += fArr[k2 + 0];
                        currentY2 += fArr[k2 + 1];
                        ctrlPointX2 = ctrlPointX8;
                        ctrlPointY2 = ctrlPointY8;
                        char c2 = previousCmd2;
                        k = k2;
                        break;
                    case 'v':
                        path2.rLineTo(0.0f, fArr[k2 + 0]);
                        currentY2 += fArr[k2 + 0];
                        char c3 = previousCmd2;
                        k = k2;
                        break;
                    default:
                        char c4 = previousCmd2;
                        float f12 = currentY2;
                        float f13 = currentX2;
                        k = k2;
                        break;
                }
                previousCmd2 = cmd;
                k2 = k + incr;
            }
            char c5 = previousCmd2;
            float currentY6 = currentY2;
            int i = k2;
            current[0] = currentX2;
            current[1] = currentY6;
            current[2] = ctrlPointX2;
            current[3] = ctrlPointY2;
            current[4] = currentSegmentStartX2;
            current[5] = currentSegmentStartY2;
        }

        private static void drawArc(Path p, float x0, float y0, float x1, float y1, float a, float b, float theta, boolean isMoreThanHalf, boolean isPositiveArc) {
            double cy;
            double cx;
            float f = x0;
            float f2 = y0;
            float f3 = x1;
            float f4 = y1;
            float f5 = a;
            float f6 = b;
            boolean z = isPositiveArc;
            double thetaD = Math.toRadians((double) theta);
            double cosTheta = Math.cos(thetaD);
            double sinTheta = Math.sin(thetaD);
            double x0p = ((((double) f) * cosTheta) + (((double) f2) * sinTheta)) / ((double) f5);
            double y0p = ((((double) (-f)) * sinTheta) + (((double) f2) * cosTheta)) / ((double) f6);
            double x1p = ((((double) f3) * cosTheta) + (((double) f4) * sinTheta)) / ((double) f5);
            double y1p = ((((double) (-f3)) * sinTheta) + (((double) f4) * cosTheta)) / ((double) f6);
            double dx = x0p - x1p;
            double dy = y0p - y1p;
            double xm = (x0p + x1p) / 2.0d;
            double ym = (y0p + y1p) / 2.0d;
            double dsq = (dx * dx) + (dy * dy);
            if (dsq == 0.0d) {
                Log.w(PathParser.LOGTAG, " Points are coincident");
                return;
            }
            double disc = (1.0d / dsq) - 0.25d;
            if (disc < 0.0d) {
                String str = PathParser.LOGTAG;
                StringBuilder sb = new StringBuilder();
                sb.append("Points are too far apart ");
                sb.append(dsq);
                Log.w(str, sb.toString());
                float adjust = (float) (Math.sqrt(dsq) / 1.99999d);
                float f7 = adjust;
                double d = dsq;
                boolean z2 = z;
                drawArc(p, x0, y0, x1, y1, f5 * adjust, f6 * adjust, theta, isMoreThanHalf, isPositiveArc);
                return;
            }
            boolean z3 = z;
            double s = Math.sqrt(disc);
            double sdx = s * dx;
            double sdy = s * dy;
            if (isMoreThanHalf == z3) {
                cx = xm - sdy;
                cy = ym + sdx;
            } else {
                cx = xm + sdy;
                cy = ym - sdx;
            }
            double d2 = s;
            double eta0 = Math.atan2(y0p - cy, x0p - cx);
            double d3 = sdx;
            double eta1 = Math.atan2(y1p - cy, x1p - cx);
            double sweep = eta1 - eta0;
            if (z3 != (sweep >= 0.0d)) {
                if (sweep > 0.0d) {
                    sweep -= 6.283185307179586d;
                } else {
                    sweep += 6.283185307179586d;
                }
            }
            double d4 = eta1;
            double cx2 = cx * ((double) f5);
            double cy2 = ((double) f6) * cy;
            double cy3 = (cx2 * sinTheta) + (cy2 * cosTheta);
            double d5 = cy3;
            arcToBezier(p, (cx2 * cosTheta) - (cy2 * sinTheta), cy3, (double) f5, (double) f6, (double) f, (double) f2, thetaD, eta0, sweep);
        }

        private static void arcToBezier(Path p, double cx, double cy, double a, double b, double e1x, double e1y, double theta, double start, double sweep) {
            double d = a;
            int numSegments = (int) Math.ceil(Math.abs((sweep * 4.0d) / 3.141592653589793d));
            double eta1 = start;
            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);
            double cosEta1 = Math.cos(eta1);
            double sinEta1 = Math.sin(eta1);
            double anglePerSegment = sweep / ((double) numSegments);
            double ep1x = (((-d) * cosTheta) * sinEta1) - ((b * sinTheta) * cosEta1);
            double ep1y = ((-d) * sinTheta * sinEta1) + (b * cosTheta * cosEta1);
            double e1x2 = e1x;
            double e1y2 = e1y;
            double d2 = eta1;
            int i = 0;
            double eta12 = d2;
            while (i < numSegments) {
                double eta2 = eta12 + anglePerSegment;
                double sinEta2 = Math.sin(eta2);
                double cosEta2 = Math.cos(eta2);
                double anglePerSegment2 = anglePerSegment;
                double anglePerSegment3 = (cx + ((d * cosTheta) * cosEta2)) - ((b * sinTheta) * sinEta2);
                double cosEta12 = cosEta1;
                double e2y = cy + (d * sinTheta * cosEta2) + (b * cosTheta * sinEta2);
                double sinEta12 = sinEta1;
                double ep2x = (((-d) * cosTheta) * sinEta2) - ((b * sinTheta) * cosEta2);
                int numSegments2 = numSegments;
                int i2 = i;
                double ep2y = ((-d) * sinTheta * sinEta2) + (b * cosTheta * cosEta2);
                double tanDiff2 = Math.tan((eta2 - eta12) / 2.0d);
                double alpha = (Math.sin(eta2 - eta12) * (Math.sqrt(((tanDiff2 * 3.0d) * tanDiff2) + 4.0d) - 1.0d)) / 3.0d;
                double q1x = e1x2 + (alpha * ep1x);
                int numSegments3 = numSegments2;
                double cosTheta2 = cosTheta;
                double q1y = e1y2 + (alpha * ep1y);
                double sinTheta2 = sinTheta;
                double q2x = anglePerSegment3 - (alpha * ep2x);
                double ep2y2 = ep2y;
                double q2y = e2y - (alpha * ep2y);
                p.rLineTo(0.0f, 0.0f);
                double d3 = q1x;
                double d4 = q1y;
                double d5 = q2y;
                p.cubicTo((float) q1x, (float) q1y, (float) q2x, (float) q2y, (float) anglePerSegment3, (float) e2y);
                eta12 = eta2;
                e1x2 = anglePerSegment3;
                e1y2 = e2y;
                ep1x = ep2x;
                ep1y = ep2y2;
                i = i2 + 1;
                numSegments = numSegments3;
                sinEta1 = sinEta12;
                anglePerSegment = anglePerSegment2;
                cosEta1 = cosEta12;
                cosTheta = cosTheta2;
                sinTheta = sinTheta2;
                d = a;
            }
            Path path = p;
            double d6 = anglePerSegment;
            int i3 = numSegments;
            int i4 = i;
            double d7 = cosTheta;
            double d8 = sinTheta;
            double d9 = cosEta1;
            double d10 = sinEta1;
        }
    }

    static float[] copyOfRange(float[] original, int start, int end) {
        if (start <= end) {
            int originalLength = original.length;
            if (start < 0 || start > originalLength) {
                throw new ArrayIndexOutOfBoundsException();
            }
            int resultLength = end - start;
            float[] result = new float[resultLength];
            System.arraycopy(original, start, result, 0, Math.min(resultLength, originalLength - start));
            return result;
        }
        throw new IllegalArgumentException();
    }

    public static Path createPathFromPathData(String pathData) {
        Path path = new Path();
        PathDataNode[] nodes = createNodesFromPathData(pathData);
        if (nodes == null) {
            return null;
        }
        try {
            PathDataNode.nodesToPath(nodes, path);
            return path;
        } catch (RuntimeException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error in parsing ");
            sb.append(pathData);
            throw new RuntimeException(sb.toString(), e);
        }
    }

    public static PathDataNode[] createNodesFromPathData(String pathData) {
        if (pathData == null) {
            return null;
        }
        int start = 0;
        int end = 1;
        ArrayList<PathDataNode> list = new ArrayList<>();
        while (end < pathData.length()) {
            int end2 = nextStart(pathData, end);
            String s = pathData.substring(start, end2).trim();
            if (s.length() > 0) {
                addNode(list, s.charAt(0), getFloats(s));
            }
            start = end2;
            end = end2 + 1;
        }
        if (end - start == 1 && start < pathData.length()) {
            addNode(list, pathData.charAt(start), new float[0]);
        }
        return (PathDataNode[]) list.toArray(new PathDataNode[list.size()]);
    }

    public static PathDataNode[] deepCopyNodes(PathDataNode[] source) {
        if (source == null) {
            return null;
        }
        PathDataNode[] copy = new PathDataNode[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = new PathDataNode(source[i]);
        }
        return copy;
    }

    public static boolean canMorph(PathDataNode[] nodesFrom, PathDataNode[] nodesTo) {
        if (nodesFrom == null || nodesTo == null || nodesFrom.length != nodesTo.length) {
            return false;
        }
        for (int i = 0; i < nodesFrom.length; i++) {
            if (nodesFrom[i].mType != nodesTo[i].mType || nodesFrom[i].mParams.length != nodesTo[i].mParams.length) {
                return false;
            }
        }
        return true;
    }

    public static void updateNodes(PathDataNode[] target, PathDataNode[] source) {
        for (int i = 0; i < source.length; i++) {
            target[i].mType = source[i].mType;
            for (int j = 0; j < source[i].mParams.length; j++) {
                target[i].mParams[j] = source[i].mParams[j];
            }
        }
    }

    private static int nextStart(String s, int end) {
        while (end < s.length()) {
            char c = s.charAt(end);
            if (((c - 'A') * (c - 'Z') <= 0 || (c - 'a') * (c - 'z') <= 0) && c != 'e' && c != 'E') {
                return end;
            }
            end++;
        }
        return end;
    }

    private static void addNode(ArrayList<PathDataNode> list, char cmd, float[] val) {
        list.add(new PathDataNode(cmd, val));
    }

    private static float[] getFloats(String s) {
        if (s.charAt(0) == 'z' || s.charAt(0) == 'Z') {
            return new float[0];
        }
        try {
            float[] results = new float[s.length()];
            int count = 0;
            int startPosition = 1;
            ExtractFloatResult result = new ExtractFloatResult();
            int totalLength = s.length();
            while (startPosition < totalLength) {
                extract(s, startPosition, result);
                int endPosition = result.mEndPosition;
                if (startPosition < endPosition) {
                    int count2 = count + 1;
                    results[count] = Float.parseFloat(s.substring(startPosition, endPosition));
                    count = count2;
                }
                if (result.mEndWithNegOrDot) {
                    startPosition = endPosition;
                } else {
                    startPosition = endPosition + 1;
                }
            }
            return copyOfRange(results, 0, count);
        } catch (NumberFormatException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("error in parsing \"");
            sb.append(s);
            sb.append("\"");
            throw new RuntimeException(sb.toString(), e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x003c A[LOOP:0: B:1:0x0007->B:20:0x003c, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0040 A[SYNTHETIC] */
    private static void extract(String s, int start, ExtractFloatResult result) {
        int currentIndex = start;
        boolean foundSeparator = false;
        result.mEndWithNegOrDot = false;
        boolean secondDot = false;
        boolean isExponential = false;
        while (currentIndex < s.length()) {
            boolean isPrevExponential = isExponential;
            isExponential = false;
            char currentChar = s.charAt(currentIndex);
            if (currentChar != ' ') {
                if (currentChar != 'E' && currentChar != 'e') {
                    switch (currentChar) {
                        case ',':
                            break;
                        case '-':
                            if (currentIndex != start && !isPrevExponential) {
                                foundSeparator = true;
                                result.mEndWithNegOrDot = true;
                                break;
                            }
                        case '.':
                            if (secondDot) {
                                foundSeparator = true;
                                result.mEndWithNegOrDot = true;
                                break;
                            } else {
                                secondDot = true;
                                break;
                            }
                    }
                } else {
                    isExponential = true;
                    if (!foundSeparator) {
                        result.mEndPosition = currentIndex;
                    }
                    currentIndex++;
                }
            }
            foundSeparator = true;
            if (!foundSeparator) {
            }
        }
        result.mEndPosition = currentIndex;
    }

    private PathParser() {
    }
}
