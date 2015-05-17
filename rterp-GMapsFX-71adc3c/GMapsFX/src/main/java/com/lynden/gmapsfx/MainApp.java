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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
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
    private ComboBox box ;
    private ComboBox<MapTypeIdEnum> mapTypeCombo;
    public String findStop = "";
    public String tmpStop = "";
    public ArrayList lst = new ArrayList();
    public ArrayList stops = new ArrayList();
    Polygon pg;

    @Override
    public void start(final Stage stage) throws Exception {
        mapComponent = new GoogleMapView();
        mapComponent.addMapInializedListener(this);

        BorderPane bp = new BorderPane();
        ToolBar tb = new ToolBar();

        btnZoomIn = new Button("Zoom In");
        btnZoomIn.setOnAction(e -> {
            map.zoomProperty().set(map.getZoom() + 1);
        });
        btnZoomIn.setDisable(true);

        btnZoomOut = new Button("Zoom Out");
        btnZoomOut.setOnAction(e -> {
            map.zoomProperty().set(map.getZoom() - 1);
        });
        btnZoomOut.setDisable(true);

        lblZoom = new Label();
        lblCenter = new Label();
        lblClick = new Label();

        mapTypeCombo = new ComboBox<>();
        mapTypeCombo.setOnAction(e -> {
            map.setMapType(mapTypeCombo.getSelectionModel().getSelectedItem());
        });
        mapTypeCombo.setDisable(true);

        Button btnType = new Button("Map type");
        btnType.setOnAction(e -> {
            map.setMapType(MapTypeIdEnum.HYBRID);
        });
        this.getStops();
        box = new ComboBox();
        box.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                System.out.println(box.getValue());
                getRoute(box.getValue().toString());
                drawOnMap(lst);
                
            }
        });
        box.getItems().addAll(stops.toArray());
        tb.getItems().addAll(btnZoomIn, btnZoomOut, mapTypeCombo,
                new Label("Zoom: "), lblZoom,
                new Label("Center: "), lblCenter,
                new Label("Click: "), lblClick, box);

        bp.setTop(tb);
        bp.setCenter(mapComponent);

        Scene scene = new Scene(bp);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void mapInitialized() {
        //Once the map has been loaded by the Webview, initialize the map details.
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
                BufferedReader readerStops = new BufferedReader(new FileReader("stops.txt"));
                BufferedReader readerCoord = new BufferedReader(new FileReader("cord2.txt"));
                lst.removeAll(lst);
                if (pg != null) {
                    map.removeMapShape(pg);
                }
                String stop;
                String coord = " ";
                String coordinates = " ";
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
                        findStop = "";
                        for (int i = 0; i < mass.length - 3; i++) {
                            findStop = findStop + mass[i] + " ";
                        }
                    }
                }
                getRoute(findStop);
                drawOnMap(lst);
                
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        btnZoomIn.setDisable(
                false);
        btnZoomOut.setDisable(
                false);
        mapTypeCombo.setDisable(
                false);

        mapTypeCombo.getItems()
                .addAll(MapTypeIdEnum.ALL);

        map.addUIEventHandler(pg, UIEventType.click,
                (JSObject obj) -> {
                    //polygOpts.editable(true);
                    pg.setEditable(!pg.getEditable());
                }
        );

        LatLong centreC = new LatLong(47.545481, -121.87384);
        CircleOptions cOpts = new CircleOptions()
                .center(centreC)
                .radius(5000)
                .strokeColor("green")
                .strokeWeight(2)
                .fillColor("orange")
                .fillOpacity(0.3);

        Circle c = new Circle(cOpts);

        map.addMapShape(c);

        map.addUIEventHandler(c, UIEventType.click,
                (JSObject obj) -> {
                    c.setEditable(!c.getEditable());
                }
        );

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

    public void getStops() {
        try {
            BufferedReader readerStops = new BufferedReader(new FileReader("stops.txt"));
            String stop = "";
            String tmpStop = "";
            String[] mass;
            while ((stop = readerStops.readLine()) != null) {
                mass = stop.split(" ");
                tmpStop = "";
                for (int i = 0; i < mass.length - 3; i++) {
                    tmpStop = tmpStop + mass[i] + " ";
                }
                stops.add(tmpStop);
            }
            Collections.sort(stops);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getRoute(String stop) {
        try {
            lst.removeAll(lst);
            BufferedReader readerRoute = new BufferedReader(new FileReader("route.txt"));
            String route = "";
            String[] mass;
            String coordinates = " ";
            while ((route = readerRoute.readLine()) != null) {
                mass = route.split(" ");
                String tmpStop = "";
                for (int i = 3; i < mass.length - 4; i++) {
                    tmpStop = tmpStop + mass[i] + " ";
                }
                if (tmpStop.equals(stop)) {
                    readerRoute.readLine();
                    while (((coordinates = readerRoute.readLine()) != null) && !(coordinates.equals(""))) {
                        lst.add(coordinates.split(" ")[0]);
                        lst.add(coordinates.split(" ")[2]);
                    }
                }
                
            }
            box.setValue(stop);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void drawOnMap(ArrayList lst) { 
        if(lst.size() % 2 != 0)
            lst.add(lst.get(lst.size()));
        LatLong[] pAry = new LatLong[lst.size()];
        
        for (int i = 0, j = 0; i <lst.size(); i += 2, j++) {
            pAry[j] = new LatLong(Double.parseDouble(lst.get(i).toString()), Double.parseDouble(lst.get(i + 1).toString()));
            pAry[lst.size()-j-1] = new LatLong(Double.parseDouble(lst.get(i).toString()), Double.parseDouble(lst.get(i + 1).toString()));
        }
        MVCArray pmvc = new MVCArray(pAry);

        PolygonOptions polygOpts = new PolygonOptions()
                .paths(pmvc)
                .strokeColor("blue")
                .strokeWeight(2)
                .editable(false)
                .fillColor("lightBlue")
                .fillOpacity(0.5);

        pg = new Polygon(polygOpts);
        
        map.addMapShape(pg);
        
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        launch(args);
    }

}
