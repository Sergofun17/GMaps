package com.lynden.gmapsfx;

import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MVCArray;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.shapes.Circle;
import com.lynden.gmapsfx.shapes.CircleOptions;
import com.lynden.gmapsfx.shapes.Polygon;
import com.lynden.gmapsfx.shapes.PolygonOptions;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class MainApp extends Application implements MapComponentInitializedListener {

    protected GoogleMapView mapComponent;
    protected GoogleMap map;
    private ComboBox boxRoute;
    private ComboBox routeSection;
    private ComboBox routeSegmentSection;
    private ComboBox SegmentSection;
    public String findStop = "";
    public String tmpStop = "";
    public ArrayList stops = new ArrayList();
    public ArrayList lstSegment = new ArrayList();
    LinkedHashMap<String, Map> mapRoute;
    LinkedHashMap<String, String> routeList;
    LinkedHashMap<String, LinkedHashMap<String, ArrayList>> mapRouteSegments = new LinkedHashMap<String, LinkedHashMap<String, ArrayList>>();
    LinkedHashMap<String, ArrayList> mapSegmentOne = new LinkedHashMap<String, ArrayList>();
    Polygon segment = null;
    Polygon routeSegment = null;
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

        getStops();
        GetListRoute();
        boxRoute = new ComboBox();
        SegmentSection = new ComboBox();
        routeSegmentSection = new ComboBox();
        routeSection = new ComboBox();
        boxRoute.setMaxWidth(250);
        boxRoute.setMinWidth(250);
        boxRoute.getItems().addAll(stops.toArray());
        boxRoute.setValue("Stops");
        boxRoute.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                addCircleStop(boxRoute.getValue().toString());
                getListSegmentsRoute(boxRoute.getValue().toString());
                fillRouteSection();
            }
        });
        
        routeSection.setMaxWidth(250);
        routeSection.setMinWidth(250);
        routeSection.setValue("Route");
       // routeSection.setVisibleRowCount(15);
        routeSection.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (map1 != null) {
                    map1.clear();
                }
                LinkedHashMap map1 = mapRouteSegments.get(routeSection.getValue());
                if (map1 != null) {
                    routeSegmentSection.getItems().clear();
                    routeSegmentSection.getItems().addAll(map1.keySet());
                    String s = String.valueOf(map1.keySet());
                    getCoordinateSegment(s);
                    SegmentSection.getItems().clear();
                    SegmentSection.getItems().addAll(mapSegmentOne.keySet());
                    routeSegmentSection.setValue("Choose Segment");
                    SegmentSection.setValue("Choose SegmentOne");
                   
                }
            }
        });

        
        routeSegmentSection.setMaxWidth(250);
        routeSegmentSection.setMinWidth(250);
        routeSegmentSection.setValue("RouteSegments");
        //routeSegmentSection.setVisibleRowCount(5);
        routeSegmentSection.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (routeSegment != null) {
                    map.removeMapShape(routeSegment);
                }
                if(routeSegmentSection.getItems().size() > 0 && routeSection.getItems().size() >0){
                ArrayList listCordinate = mapRouteSegments.get(routeSection.getValue()).get(routeSegmentSection.getValue());
                if (listCordinate != null) {
                    drawOnMap(listCordinate, "blue", "route");
                }
                }

            }
        });

       
        SegmentSection.setMaxWidth(250);
        SegmentSection.setMinWidth(250);
        SegmentSection.setValue("SegmentOne");
        //SegmentSection.setVisibleRowCount(5);
        SegmentSection.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (segment != null) {
                    map.removeMapShape(segment);
                }
                ArrayList listCordinate = mapSegmentOne.get(SegmentSection.getValue());
                if (listCordinate != null) {
                    drawOnMap(listCordinate, "red", "Segment");
                }

            }
        });

        tb.getItems().addAll(//btnZoomIn, btnZoomOut, mapTypeCombo,
                //new Label("Zoom: "), lblZoom,
                //new Label("Center: "), lblCenter,
                //new Label("Click: "), lblClick,
                boxRoute, routeSection, routeSegmentSection, SegmentSection);
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

        /*MarkerOptions markerOptions = new MarkerOptions();
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
         });*/
        map.addUIEventHandler(UIEventType.click, (JSObject obj) -> {
            try {
                BufferedReader readerSegment = new BufferedReader(new FileReader("Segments.txt"));
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
                while ((stop = readerSegment.readLine()) != null) {
                    i++;
                    mass = stop.split(" ");
                    if (mass.length >= 2) {
                        min1 = Math.abs(Double.parseDouble(ll.toString().split(" ")[1].replace(",", ".")) - Double.parseDouble(mass[1].replace(",", ".")));
                        min2 = Math.abs(Double.parseDouble(ll.toString().split(" ")[3].replace(",", ".")) - Double.parseDouble(mass[3].replace(",", ".")));
                        tempmin = Math.sqrt(Math.pow(min1, 2) + Math.pow(min2, 2));
                        if (!Objects.equals(min, tempmin) && tempmin < min) {
                            min = tempmin;
                            readerSegment.reset();
                            lstSegment.clear();
                            while (!(stop = readerSegment.readLine()).equals("")) {
                                lstSegment.add(stop.split(" ")[1]);
                                lstSegment.add(stop.split(" ")[3]);
                            }
                        }
                    } else {
                        readerSegment.mark(99999);
                    }
                }
                drawOnMap(lstSegment, "red", "Segment");
                //boxRoute.setValue(stop);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        map.addUIEventHandler(UIEventType.rightclick, (JSObject obj) -> {
            findStop = findStop(obj);
            boxRoute.setValue(findStop);
        });

        /*btnZoomIn.setDisable(
         false);
         btnZoomOut.setDisable(
         false);
         mapTypeCombo.setDisable(
         false);

         mapTypeCombo.getItems()
         .addAll(MapTypeIdEnum.ALL);
         */
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
        /*LatLongBounds llb = new LatLongBounds(new LatLong(47.533893, -122.89856), new LatLong(47.580694, -122.80312));
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
         );*/
    }

    public String findStop(JSObject obj) {
        try {
            BufferedReader readerStops = new BufferedReader(new FileReader("stops.txt"));
            String stop;
            String[] mass;
            Double min = 9999.0;
            Double tempmin = 9999.0;
            Double min1 = 9999.5D;
            Double min2 = 9999.5D;
            LatLong ll = new LatLong((JSObject) obj.getMember("latLng"));
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

    /*public String getStopNameFromString(String stop) {
     String stopName = "";
     String[] mass = stop.split(" ");;
     for (int i = 0; i < mass.length - 3; i++) {
     stopName = stopName + mass[i] + " ";
     }
     return stopName;
     }*/
    /*public void getRoutes(String stop) {
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
     */
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
                routeSegment = new Polygon(polygOpts);
                map.addMapShape(routeSegment);
            }
        }
    }

    public void addCircleStop(String stop) {
        if (circle != null) {
            map.removeMapShape(circle);
        }
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

    public String getStopCoordinateFromString(String stop) {
        String stopCoordinate = "";
        String[] mass = stop.split(" ");
        for (int i = mass.length - 3; i < mass.length; i++) {
            stopCoordinate = stopCoordinate + mass[i] + " ";
        }
        return stopCoordinate;
    }

    public void fillRouteSection() {
        routeSection.getItems().clear();
        routeSegmentSection.getItems().clear();
        SegmentSection.getItems().clear();
        routeSection.getItems().addAll(mapRouteSegments.keySet());
        routeSection.setValue("Choose Route");
        routeSegmentSection.setValue("RouteSegment");
        SegmentSection.setValue("SegmentOne");
    }

    public void getListSegmentsRoute(String stop) {
        mapRouteSegments.clear();
        for (Map.Entry entry : mapRoute.entrySet()) {
            String nameRouteStops = "";
            String values = entry.getValue().toString();
            ArrayList coordinatesList;
            LinkedHashMap<String, ArrayList> map3 = new LinkedHashMap<String, ArrayList>();
            while (values.contains(stop)) {
                coordinatesList = new ArrayList();
                int i = values.indexOf(stop);
                int end = values.indexOf("[", i);
                int endCoord = values.indexOf("]", end);
                if (values.indexOf("->") < i) {
                    values = values.substring(endCoord + 2);
                } else {
                    nameRouteStops = values.substring(i, end - 1);
                    String coordinates = values.substring(end + 1, endCoord - 1);
                    for (int j = 0; j < coordinates.split(",").length; j++) {
                        String s = coordinates.split(",")[j];
                        coordinatesList.add(s);
                    }
                    map3.put(nameRouteStops, coordinatesList);
                    mapRouteSegments.put(entry.getKey().toString(), map3);
                    values = values.substring(endCoord);
                }
            }
        }
    }

    public void drawRouteSegment(ArrayList lst, String color, String segmentOrRoute) {
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
                segment = new Polygon(polygOpts);
                map.addMapShape(segment);
            }
        }
    }

    public void getCoordinateSegment(String s) {
        ArrayList listCoordinateSegmentOne = new ArrayList();
        while (s.contains("idSegment")) {
            if ((Math.abs(s.indexOf("->") - s.indexOf("idSegment")) < 18)) {
                String tmp = s.substring(s.indexOf("idSegment") + 10, s.indexOf("->") - 1);
                listCoordinateSegmentOne.add(Integer.parseInt(s.substring(s.indexOf("idSegment") + 10, s.indexOf("->") - 1)));
                s = s.substring(s.indexOf("->") + 3);
            } else {
                s = s.substring(s.indexOf("idSegment") + 10);
            }
        }
        if (listCoordinateSegmentOne != null) {
            findSegment(listCoordinateSegmentOne);
        }
    }

    public void findSegment(ArrayList lst) {
        try {
            BufferedReader readerSegment = new BufferedReader(new FileReader("Segments.txt"));
            String str = " ";
            mapSegmentOne.clear();
            int idSegment;
            String[] mass;
            LinkedHashMap <Integer, ArrayList> mapTmp = new LinkedHashMap <Integer, ArrayList>();
            ArrayList lstTmp;
            while ((str = readerSegment.readLine()) != null) {
                idSegment = Integer.parseInt(str);
                lstTmp = new ArrayList();
                readerSegment.readLine();
                while((str = readerSegment.readLine()) != null && !str.equals("")){
                    lstTmp.add(str.split(" ")[1]);
                    lstTmp.add(str.split(" ")[3]);
                }
                mapTmp.put(idSegment, lstTmp);
            }
            for(int i =0; i< lst.size(); i++){
                int s = Integer.parseInt(lst.get(i).toString());
                ArrayList ss = mapTmp.get(s);
                mapSegmentOne.put(String.valueOf(lst.get(i)), mapTmp.get(lst.get(i)));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void GetListRoute() {
        try {
            BufferedReader readerRoute = new BufferedReader(new FileReader("route.txt"));
            String s = " ";
            int idRoute = 0;
            String nameRoute = "";
            ArrayList<Double> point;
            Map<String, List> maps1;
            mapRoute = new LinkedHashMap<String, Map>();
            while (!(s = readerRoute.readLine()).equals("EndOfFile")) {
                if (s.equals("NextRoute")) {
                    s = readerRoute.readLine();
                    String[] mass;
                    nameRoute = "";
                    String nameStopRoute = "";
                    mass = s.split(" ");
                    maps1 = new LinkedHashMap<String, List>();
                    idRoute = Integer.parseInt(mass[mass.length - 1]);
                    if (mass[0].contains(":")) {
                        for (int i = 1; i < mass.length - 4; i++) {
                            nameRoute = nameRoute + " " + mass[i];
                        }
                        nameRoute = nameRoute + " " + idRoute;
                    }
                    readerRoute.mark(999);
                    s = readerRoute.readLine();
                    mass = s.split(" ");
                    while (mass.length > 2 && mass[mass.length - 4].equals("idStatistik:")) {
                        point = new ArrayList<Double>();
                        nameStopRoute = s;
                        readerRoute.readLine();
                        while (!(s = readerRoute.readLine()).equals("")) {
                            point.add(Double.parseDouble(s.split(" ")[1]));
                            point.add(Double.parseDouble(s.split(" ")[3]));
                        }
                        s = readerRoute.readLine();
                        mass = s.split(" ");
                        readerRoute.mark(999);
                        if (!s.equals("NextRoute")) {
                            maps1.put(nameStopRoute + " -> " + s, point);
                            readerRoute.reset();
                        }
                    }
                    mapRoute.put(nameRoute, maps1);
                    readerRoute.reset();
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        System.setProperty("java.net.useSystemProxies", "true");
        launch(args);
    }

}
