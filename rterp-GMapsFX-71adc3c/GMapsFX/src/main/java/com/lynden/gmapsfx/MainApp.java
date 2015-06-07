package com.lynden.gmapsfx;

import com.lynden.gmapsfx.service.elevation.ElevationResult;
import com.lynden.gmapsfx.service.elevation.ElevationService;
import com.lynden.gmapsfx.service.elevation.ElevationServiceCallback;
import com.lynden.gmapsfx.service.elevation.ElevationStatus;
import com.lynden.gmapsfx.service.elevation.LocationElevationRequest;
import com.lynden.gmapsfx.service.elevation.PathElevationRequest;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.Animation;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.LatLongBounds;
import com.lynden.gmapsfx.javascript.object.MVCArray;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.shapes.ArcBuilder;
import com.lynden.gmapsfx.shapes.Circle;
import com.lynden.gmapsfx.shapes.CircleOptions;
import com.lynden.gmapsfx.shapes.Polygon;
import com.lynden.gmapsfx.shapes.PolygonOptions;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import com.lynden.gmapsfx.shapes.Rectangle;
import com.lynden.gmapsfx.shapes.RectangleOptions;
import com.lynden.gmapsfx.zoom.MaxZoomResult;
import com.lynden.gmapsfx.zoom.MaxZoomService;
import com.lynden.gmapsfx.zoom.MaxZoomServiceCallback;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

/**
 * Example Application for creating and loading a GoogleMap into a JavaFX
 * application
 *
 * @author Rob Terpilowski
 */
public class MainApp extends Application implements MapComponentInitializedListener {

    protected GoogleMapView mapComponent;
    protected GoogleMap map;

    private Button btnZoomIn;
    private Button btnZoomOut;
    private Label lblZoom;
    private Label lblCenter;
    private Label lblClick;
    private ComboBox boxRoute;
    private ComboBox routeSection;
    private ComboBox<MapTypeIdEnum> mapTypeCombo;
    public String findStop = "";
    public String tmpStop = "";
    public ArrayList lst = new ArrayList();
    public ArrayList stops = new ArrayList();
    public ArrayList lstSegment = new ArrayList();
    Polygon segment = null;
    Polygon route = null;
    Circle circle = null;
    public ArrayList< Map<String, List<Double>>> maps;
    public Map<String, ArrayList<Double>> map1;

    @Override
    public void start(final Stage stage) throws Exception {
        mapComponent = new GoogleMapView();
        mapComponent.setPrefSize(1200, 600);
        mapComponent.addMapInializedListener(this);

        BorderPane bp = new BorderPane();
        ToolBar tb = new ToolBar();

        /*btnZoomIn = new Button("Zoom In");
        btnZoomIn.setOnAction(e -> {
            map.zoomProperty().set(map.getZoom() + 1);
        });
        btnZoomIn.setDisable(true);

        btnZoomOut = new Button("Zoom Out");
        btnZoomOut.setOnAction(e -> {
            map.zoomProperty().set(map.getZoom() - 1);
        });
        btnZoomOut.setDisable(true);*/

        lblZoom = new Label();
        lblCenter = new Label();
        lblClick = new Label();

        /*mapTypeCombo = new ComboBox<>();
        mapTypeCombo.setOnAction(e -> {
            map.setMapType(mapTypeCombo.getSelectionModel().getSelectedItem());
        });
        mapTypeCombo.setDisable(true);*/

        Button btnType = new Button("Map type");
        btnType.setOnAction(e -> {
            map.setMapType(MapTypeIdEnum.HYBRID);
        });
        getStops();
        boxRoute = new ComboBox();
        boxRoute.setMinWidth(200);
        boxRoute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                getRoutes(getStopNameFromString(boxRoute.getValue().toString()));
                addCircleStop(boxRoute.getValue().toString());
                fillRouteSection();
            }
        });
        routeSection = new ComboBox();
        routeSection.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (route != null) {
                    map.removeMapShape(route);
                }
                ArrayList lss = map1.get(routeSection.getValue());
                if (lss != null) {
                    drawOnMap(lss, "blue", "route");
                }

            }
        });
        boxRoute.getItems().addAll(stops.toArray());
        tb.getItems().addAll(//btnZoomIn, btnZoomOut, mapTypeCombo,
                new Label("Zoom: "), lblZoom,
                new Label("Center: "), lblCenter,
                new Label("Click: "), lblClick, boxRoute, routeSection);

        bp.setTop(tb);
        bp.setCenter(mapComponent);

        Scene scene = new Scene(bp);
        stage.setScene(scene);
        stage.show();
    }

    @Override

    public void mapInitialized() {
        LatLong center = new LatLong(55.00723620, 82.93401138);
        mapComponent.addMapReadyListener(() -> {
            // This call will fail unless the map is completely ready.
        });

        MapOptions options = new MapOptions();
        options.center(center)
                .mapMarker(true)
                .zoom(12)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .mapType(MapTypeIdEnum.TERRAIN);
        
        map = mapComponent.createMap(options);

        map.setHeading(123.2);

        MarkerOptions markerOptions = new MarkerOptions();
        LatLong markerLatLong = new LatLong(47.606189, -122.335842);
        markerOptions.position(markerLatLong)
                .title("My new Marker")
                .animation(Animation.DROP)
                .visible(true);

        final Marker myMarker = new Marker(markerOptions);

        MarkerOptions markerOptions2 = new MarkerOptions();
        LatLong markerLatLong2 = new LatLong(47.906189, -122.335842);
        markerOptions2.position(markerLatLong2)
                .title("My new Marker")
                .visible(true);

        Marker myMarker2 = new Marker(markerOptions2);

        map.addMarker(myMarker);
        map.addMarker(myMarker2);

        InfoWindowOptions infoOptions = new InfoWindowOptions();
        infoOptions.content("<h2>Here's an info window</h2><h3>with some info</h3>")
                .position(center);

        InfoWindow window = new InfoWindow(infoOptions);
        lblCenter.setText(map.getCenter().toString());
        map.centerProperty().addListener((ObservableValue<? extends LatLong> obs, LatLong o, LatLong n) -> {
            lblCenter.setText(n.toString());
        });

        lblZoom.setText(Integer.toString(map.getZoom()));
        map.zoomProperty().addListener((ObservableValue<? extends Number> obs, Number o, Number n) -> {
            lblZoom.setText(n.toString());
        });
        map.addUIEventHandler(UIEventType.click, (JSObject obj) -> {

            try {
                BufferedReader readerSegment = new BufferedReader(new FileReader("Segments.txt"));

                lst.clear();
                if (segment != null) {
                    map.removeMapShape(segment);
                }
                String stop;
                String[] mass;
                int i = 0;
                Double min = 9999.0;
                Double tempmin = 9999.0;
                Double min1 = 9999.5D;
                Double min2 = 9999.5D;
                LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
                lblClick.setText(ll.toString());
                while ((stop = readerSegment.readLine()) != null) {
                    i++;
                    mass = stop.split(" ");
                    if (mass.length >= 2) {
                        min1 = Math.abs(Double.parseDouble(ll.toString().split(" ")[1].replace(",", ".")) - Double.parseDouble(mass[0].replace(",", ".")));
                        min2 = Math.abs(Double.parseDouble(ll.toString().split(" ")[3].replace(",", ".")) - Double.parseDouble(mass[2].replace(",", ".")));
                        tempmin = Math.sqrt(Math.pow(min1, 2) + Math.pow(min2, 2));
                        if (!Objects.equals(min, tempmin) && tempmin < min) {
                            min = tempmin;
                            readerSegment.reset();
                            lstSegment.clear();
                            while (!(stop = readerSegment.readLine()).equals("")) {
                                lstSegment.add(stop.split(" ")[0]);
                                lstSegment.add(stop.split(" ")[2]);
                            }
                        }
                    } else {
                        readerSegment.mark(99999);
                    }
                }
                drawOnMap(lstSegment, "red", "Segment");
                boxRoute.setValue(stop);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        map.addUIEventHandler(UIEventType.rightclick, (JSObject obj) -> {

            findStop = findStop(obj);
            //getRoutess(findStop);
            //fillRouteSection();
            //drawOnMap(lst, "blue", "Route");
            boxRoute.setValue(findStop);

        });

        btnZoomIn.setDisable(
                false);
        btnZoomOut.setDisable(
                false);
        mapTypeCombo.setDisable(
                false);

        mapTypeCombo.getItems()
                .addAll(MapTypeIdEnum.ALL);

        //Event для изменения полигонов
        /*map.addUIEventHandler(pg, UIEventType.click,
         (JSObject obj) -> {
         //polygOpts.editable(true);
         pg.setEditable(!pg.getEditable());
         }
         );*/
        //Event для изменения Кругов
       /* map.addUIEventHandler(c, UIEventType.click,
         (JSObject obj) -> {
         c.setEditable(!c.getEditable());
         }
         );*/
        LatLongBounds llb = new LatLongBounds(new LatLong(47.533893, -122.89856), new LatLong(47.580694, -122.80312));
        RectangleOptions rOpts = new RectangleOptions()
                .bounds(llb)
                .strokeColor("black")
                .strokeWeight(2)
                .fillColor("null");

        Rectangle rt = new Rectangle(rOpts);

        map.addMapShape(rt);

        LatLong arcC = new LatLong(47.227029, -121.81641);
        double startBearing = 0;
        double endBearing = 30;
        double radius = 30000;

        MVCArray path = ArcBuilder.buildArcPoints(arcC, startBearing, endBearing, radius);

        path.push(arcC);

        Polygon arc = new Polygon(new PolygonOptions()
                .paths(path)
                .strokeColor("blue")
                .fillColor("lightBlue")
                .fillOpacity(0.3)
                .strokeWeight(2)
                .editable(false));

        map.addMapShape(arc);
        map.addUIEventHandler(arc, UIEventType.click,
                (JSObject obj) -> {
                    arc.setEditable(!arc.getEditable());
                }
        );

    }

    public String findStop(JSObject obj) {
        try {
            BufferedReader readerStops = new BufferedReader(new FileReader("stops.txt"));
            lst.clear();
            if (route != null) {
                map.removeMapShape(route);
            }
            String stop;
            String[] mass;
            Double min = 9999.0;
            Double tempmin = 9999.0;
            Double min1 = 9999.5D;
            Double min2 = 9999.5D;
            LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
            lblClick.setText(ll.toString());

            while ((stop = readerStops.readLine()) != null) {
                mass = stop.split(" ");
                min1 = Math.abs(Double.parseDouble(ll.toString().split(" ")[1].replace(",", ".")) - Double.parseDouble(mass[mass.length - 3].replace(",", ".")));
                min2 = Math.abs(Double.parseDouble(ll.toString().split(" ")[3].replace(",", ".")) - Double.parseDouble(mass[mass.length - 1].replace(",", ".")));
                tempmin = Math.sqrt(Math.pow(min1, 2) + Math.pow(min2, 2));
                if (!Objects.equals(min, tempmin) && tempmin < min) {
                    min = tempmin;
                    findStop = stop;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return findStop;
    }

    public void getStops() {
        try {
            BufferedReader readerStops = new BufferedReader(new FileReader("stops.txt"));
            String stop = "";
            while ((stop = readerStops.readLine()) != null) {
                stops.add(stop);
            }
            Collections.sort(stops);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getStopNameFromString(String stop) {
        String stopName = "";
        String[] mass = stop.split(" ");;
        for (int i = 0; i < mass.length - 3; i++) 
            stopName = stopName + mass[i] + " ";
        return stopName;
        }
    
    public String getStopCoordinateFromString(String stop){
        String stopCoordinate = "";
        String[] mass = stop.split(" ");
        for (int i = mass.length-3; i < mass.length; i++) 
            stopCoordinate = stopCoordinate + mass[i] + " ";
        return stopCoordinate;
    }
 
    public void getRoutes(String stop) {
        try {
            maps = new ArrayList<Map<String, List<Double>>>();
            map1 = new HashMap<String, ArrayList<Double>>();
            ArrayList<Double> lsst;
            BufferedReader readerRoute = new BufferedReader(new FileReader("route.txt"));
            String route = "";
            String[] mass;
            int count = 0;
            boolean noData = false;
            String coordinates = " ";
            while ((route = readerRoute.readLine()) != null) {
                mass = route.split(" ");
                String tmpStop = "";
                for (int i = 3; i < mass.length - 4; i++) {
                    tmpStop = tmpStop + mass[i] + " ";
                }
                if (tmpStop.equals(stop)) {
                    noData = false;
                    lsst = new ArrayList<Double>();
                    String temp = readerRoute.readLine();
                    if (temp.equals("")) {
                        break;
                    }
                    while (((coordinates = readerRoute.readLine()) != null) && !(coordinates.equals(""))) {//
                        if (coordinates.split(" ").length > 3 && !coordinates.split(" ")[6].equals("нет")) {
                            noData = true;
                        } else {
                            lsst.add(Double.parseDouble(coordinates.split(" ")[0]));
                            lsst.add(Double.parseDouble(coordinates.split(" ")[2]));
                        }
                    }
                    if (noData == true) {
                        break;
                    }
                    readerRoute.mark(999);
                    mass = readerRoute.readLine().split(" ");
                    String nextStop = "";
                    for (int i = 3; i < mass.length - 4; i++) {
                        nextStop = nextStop + mass[i] + " ";
                    }
                    map1.put(stop.concat(" -> ").concat(nextStop).concat("" + count), lsst);
                    readerRoute.reset();
                    count++;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void drawSegment() {

    }

    public void drawOnMap(ArrayList lst, String color, String segmentOrRoute) {
        if (lst != null) {
            if (lst.size() % 2 != 0) {
                lst.add(lst.get(lst.size()));
            }
            LatLong[] pAry = new LatLong[lst.size()];

            for (int i = 0, j = 0; i < lst.size(); i += 2, j++) {
                pAry[j] = new LatLong(Double.parseDouble(lst.get(i).toString()), Double.parseDouble(lst.get(i + 1).toString()));
                pAry[lst.size() - j - 1] = new LatLong(Double.parseDouble(lst.get(i).toString()), Double.parseDouble(lst.get(i + 1).toString()));
            }
            MVCArray pmvc = new MVCArray(pAry);

            PolygonOptions polygOpts = new PolygonOptions()
                    .paths(pmvc)
                    .strokeColor(color)
                    .strokeWeight(2)
                    .editable(false)
                    .fillColor("lightBlue")
                    .fillOpacity(0.5);
            if (segmentOrRoute.equals("Segment")) {
                segment = new Polygon(polygOpts);
                map.addMapShape(segment);
            } else {
                route = new Polygon(polygOpts);
                map.addMapShape(route);
            }
        }
    }

    public void addCircleStop(String stop) {
        if(circle != null)
            map.removeMapShape(circle);
        String coordinates = getStopCoordinateFromString(stop);
        LatLong centreC = new LatLong(Double.parseDouble(coordinates.split(" ")[0]), Double.parseDouble(coordinates.split(" ")[2]));
        CircleOptions cOpts = new CircleOptions()
                .center(centreC)
                .radius(30)
                .strokeColor("orange")
                .strokeWeight(2)
                .fillColor("red")
                .fillOpacity(0.3);

        circle = new Circle(cOpts);
        map.addMapShape(circle);
        map.setCenter(centreC);
        map.setZoom(17);
        

    }

    public void fillRouteSection() {
        routeSection.getItems().clear();
        routeSection.getItems().addAll(map1.keySet());
        routeSection.setValue("Выберете интересующий вас маршрут");
    }

 

    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        launch(args);
    }

}
