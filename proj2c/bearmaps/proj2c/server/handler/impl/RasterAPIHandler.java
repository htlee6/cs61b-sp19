package bearmaps.proj2c.server.handler.impl;

import bearmaps.proj2c.AugmentedStreetMapGraph;
import bearmaps.proj2c.server.handler.APIRouteHandler;
import spark.Request;
import spark.Response;
import bearmaps.proj2c.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static bearmaps.proj2c.utils.Constants.*;

/**
 * Handles requests from the web browser for map images. These images
 * will be rastered into one large image to be displayed to the user.
 * @author rahul, Josh Hug, _________
 */
public class RasterAPIHandler extends APIRouteHandler<Map<String, Double>, Map<String, Object>> {

    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside RasterAPIHandler.processRequest(). <br>
     * ullat : upper left corner latitude, <br> ullon : upper left corner longitude, <br>
     * lrlat : lower right corner latitude,<br> lrlon : lower right corner longitude <br>
     * w : user viewport window width in pixels,<br> h : user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
            "lrlon", "w", "h"};

    /**
     * The result of rastering must be a map containing all of the
     * fields listed in the comments for RasterAPIHandler.processRequest.
     **/
    private static final String[] REQUIRED_RASTER_RESULT_PARAMS = {"render_grid", "raster_ul_lon",
            "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};


    @Override
    protected Map<String, Double> parseRequestParams(Request request) {
        return getRequestParams(request, REQUIRED_RASTER_REQUEST_PARAMS);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param requestParams Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @param response : Not used by this function. You may ignore.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     *                    can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    @Override
    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {
        System.out.println("yo, wanna know the parameters given by the web browser? They are:");
        System.out.println(requestParams);
        Map<String, Object> results = new HashMap<>();
        // variables declaration
        double lrlon = requestParams.get("lrlon"), ullon = requestParams.get("ullon"), width = requestParams.get("w");
        double lrlat = requestParams.get("lrlat"), ullat = requestParams.get("ullat");
        double queryBoxLonDPP = (lrlon - ullon) / width;

        /* Fail Corner Case I: Partial Coverage */
        // TODO

        /* Fail Corner Case II: No Coverage */
        if (outsideOfRoot(ullon, ullat, lrlon, lrlat) || queryBoxMakesNoSense(ullon, ullat, lrlon, lrlat)) {
            return queryFail();
        }

        int depth = chooseAppropriateDepth(queryBoxLonDPP);

        List<Integer> xIndices = xIndices(depth, ullon, lrlon);
        List<Integer> yIndices = yIndices(depth, ullat, lrlat);
        int xGrids = xIndices.size(), yGrids = yIndices.size();

        String[][] render_grid = new String[yGrids][xGrids];

        Map<String, Double> rasterInfos = rasterCoordinates(depth, xIndices.get(0), xIndices.get(xIndices.size() - 1), yIndices.get(0), yIndices.get(yIndices.size() - 1));
        double raster_ul_lon = rasterInfos.get("raster_ullon"),
                raster_ul_lat = rasterInfos.get("raster_ullat"),
                raster_lr_lon = rasterInfos.get("raster_lrlon"),
                raster_lr_lat = rasterInfos.get("raster_lrlat");

        boolean query_success = depth > 0;

        int i = 0;
        for (Integer x : xIndices) {
            int j = 0;
            for (Integer y : yIndices) {
                String imgName = indexToImgName(depth, x, y);
                render_grid[j][i] = imgName;
                j += 1;
            }
            i += 1;
        }

        results.put("render_grid", render_grid);
        results.put("raster_ul_lon", raster_ul_lon);
        results.put("raster_ul_lat", raster_ul_lat);
        results.put("raster_lr_lon", raster_lr_lon);
        results.put("raster_lr_lat", raster_lr_lat);
        results.put("depth", depth);
        results.put("query_success", query_success);

        /* System.out.println("Since you haven't implemented RasterAPIHandler.processRequest, nothing is displayed in "
                + "your browser.");
         */
        return results;
    }

    @Override
    protected Object buildJsonResponse(Map<String, Object> result) {
        boolean rasterSuccess = validateRasteredImgParams(result);

        if (rasterSuccess) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writeImagesToOutputStream(result, os);
            String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
            result.put("b64_encoded_image_data", encodedImage);
        }
        return super.buildJsonResponse(result);
    }

    private Map<String, Object> queryFail() {
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", null);
        results.put("raster_ul_lon", 0);
        results.put("raster_ul_lat", 0);
        results.put("raster_lr_lon", 0);
        results.put("raster_lr_lat", 0);
        results.put("depth", 0);
        results.put("query_success", false);
        return results;
    }

    /**
     * Validates that Rasterer has returned a result that can be rendered.
     * @param rip : Parameters provided by the rasterer
     */
    private boolean validateRasteredImgParams(Map<String, Object> rip) {
        for (String p : REQUIRED_RASTER_RESULT_PARAMS) {
            if (!rip.containsKey(p)) {
                System.out.println("Your rastering result is missing the " + p + " field.");
                return false;
            }
        }
        if (rip.containsKey("query_success")) {
            boolean success = (boolean) rip.get("query_success");
            if (!success) {
                System.out.println("query_success was reported as a failure");
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the images corresponding to rasteredImgParams to the output stream.
     * In Spring 2016, students had to do this on their own, but in 2017,
     * we made this into provided code since it was just a bit too low level.
     */
    private  void writeImagesToOutputStream(Map<String, Object> rasteredImageParams,
                                                  ByteArrayOutputStream os) {
        String[][] renderGrid = (String[][]) rasteredImageParams.get("render_grid");
        int numVertTiles = renderGrid.length;
        int numHorizTiles = renderGrid[0].length;

        BufferedImage img = new BufferedImage(numHorizTiles * Constants.TILE_SIZE,
                numVertTiles * Constants.TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = img.getGraphics();
        int x = 0, y = 0;

        for (int r = 0; r < numVertTiles; r += 1) {
            for (int c = 0; c < numHorizTiles; c += 1) {
                graphic.drawImage(getImage(Constants.IMG_ROOT + renderGrid[r][c]), x, y, null);
                x += Constants.TILE_SIZE;
                if (x >= img.getWidth()) {
                    x = 0;
                    y += Constants.TILE_SIZE;
                }
            }
        }

        /* If there is a route, draw it. */
        double ullon = (double) rasteredImageParams.get("raster_ul_lon"); //tiles.get(0).ulp;
        double ullat = (double) rasteredImageParams.get("raster_ul_lat"); //tiles.get(0).ulp;
        double lrlon = (double) rasteredImageParams.get("raster_lr_lon"); //tiles.get(0).ulp;
        double lrlat = (double) rasteredImageParams.get("raster_lr_lat"); //tiles.get(0).ulp;

        final double wdpp = (lrlon - ullon) / img.getWidth();
        final double hdpp = (ullat - lrlat) / img.getHeight();
        AugmentedStreetMapGraph graph = SEMANTIC_STREET_GRAPH;
        List<Long> route = ROUTE_LIST;

        if (route != null && !route.isEmpty()) {
            Graphics2D g2d = (Graphics2D) graphic;
            g2d.setColor(Constants.ROUTE_STROKE_COLOR);
            g2d.setStroke(new BasicStroke(Constants.ROUTE_STROKE_WIDTH_PX,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            route.stream().reduce((v, w) -> {
                g2d.drawLine((int) ((graph.lon(v) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(v)) * (1 / hdpp)),
                        (int) ((graph.lon(w) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(w)) * (1 / hdpp)));
                return w;
            });
        }

        rasteredImageParams.put("raster_width", img.getWidth());
        rasteredImageParams.put("raster_height", img.getHeight());

        try {
            ImageIO.write(img, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private BufferedImage getImage(String imgPath) {
        BufferedImage tileImg = null;
        if (tileImg == null) {
            try {
                File in = new File(imgPath);
                tileImg = ImageIO.read(in);
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return tileImg;
    }

    /********************************************************************
     * My helpers
     ********************************************************************/

    private static int chooseAppropriateDepth(double stdLonDPP) {
        int d = 0;
        double computedLonDPP = (ROOT_LRLON - ROOT_ULLON) / TILE_SIZE ;
        for ( ; d < 8; d += 1) {
            if (Double.compare(computedLonDPP, stdLonDPP) < 0) {
                return d;
            }
            computedLonDPP = computedLonDPP / 2;
        }
        return -1;
    }

    private static String indexToImgName(int depth, int x, int y) {
        return "d" + String.valueOf(depth) + "_x" + String.valueOf(x) + "_y" + String.valueOf(y) + ".png";
    }

    private static ArrayList<Integer> xIndices(int depth, double ullon, double lrlon) {
        ArrayList<Integer> res = new ArrayList<>();
        double deltaX = (ROOT_LRLON - ROOT_ULLON) / (depth * depth);
        double currentBox_ul_lon = ROOT_ULLON, currentBox_lr_lon = currentBox_ul_lon + deltaX;
        boolean adding = false;
        int x = 0;
        while (x < depth * depth) {
            if (Double.compare(currentBox_ul_lon, ullon) <= 0 && Double.compare(currentBox_lr_lon, ullon) > 0) {
                adding = true;
            }
            if (Double.compare(currentBox_ul_lon, lrlon) > 0) {
                break;
            }
            if (adding) {
                res.add(x);
            }
            x += 1;
            currentBox_lr_lon += deltaX;
            currentBox_ul_lon += deltaX;
        }
        return res;
    }

    private static ArrayList<Integer> yIndices(int depth, double ullat, double lrlat) {
        ArrayList<Integer> res = new ArrayList<>();
        double deltaY = (ROOT_ULLAT - ROOT_LRLAT) / (depth * depth);
        double currentBox_ul_lat = ROOT_ULLAT, currentBox_lr_lat = currentBox_ul_lat - deltaY;
        boolean adding = false;
        int y = 0;
        while (y < depth * depth) {
            if (Double.compare(currentBox_ul_lat, ullat) >= 0 && Double.compare(currentBox_lr_lat, ullat) < 0) {
                adding = true;
            }
            if (Double.compare(currentBox_ul_lat, lrlat) < 0) {
                adding = false;
            }
            if (adding) {
                res.add(y);
            }
            y += 1;
            currentBox_lr_lat -= deltaY;
            currentBox_ul_lat -= deltaY;
        }
        return res;
    }

    private static Map<String, Double> rasterCoordinates(int depth, int xStart, int xEnd, int yStart, int yEnd) {
        Map<String, Double> res = new HashMap<>();
        double deltaX = (ROOT_LRLON - ROOT_ULLON) / (depth * depth );
        double deltaY = (ROOT_ULLAT - ROOT_LRLAT) / (depth * depth );
        double raster_ullon = ROOT_ULLON + xStart * deltaX,
                raster_lrlon = ROOT_ULLON + (xEnd + 1) * deltaX,
                raster_ullat = ROOT_ULLAT - yStart * deltaY,
                raster_lrlat = ROOT_ULLAT - (yEnd + 1) * deltaY;
        res.put("raster_ullon", raster_ullon);
        res.put("raster_ullat", raster_ullat);
        res.put("raster_lrlon", raster_lrlon);
        res.put("raster_lrlat", raster_lrlat);
        return res;
    }

    private static boolean outsideOfRoot(double ullon, double ullat, double lrlon, double lrlat) {
        if (ullon >= ROOT_ULLON && ullat <= ROOT_ULLAT && lrlon <= ROOT_LRLON && lrlat >= ROOT_LRLAT) {
            return false;
        }
        return true;
    }

    private static boolean queryBoxMakesNoSense(double ullon, double ullat, double lrlon, double lrlat) {
        if (ullon < lrlon && ullat > lrlat) {
            return false;
        }
        return true;
    }

}
