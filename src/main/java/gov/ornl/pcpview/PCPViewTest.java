package gov.ornl.pcpview;

import gov.ornl.datatable.DataTable;
import gov.ornl.datatable.IOUtilities;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by csg on 8/22/16.
 */
public class PCPViewTest extends Application {
    public static final Logger log = Logger.getLogger(PCPViewTest.class.getName());

    private PCPView pcpView;
    private DataTable dataModel;

    @Override
    public void init() {

    }

    @Override
    public void start(Stage stage) throws Exception {
//        StackPane pane = new StackPane(pcpView);
//        pane.setPadding(new Insets(20));

//        stage.setOnShown(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                try {
//                    IOUtilities.readCSV(new File("data/csv/titan-performance.csv"),
//                            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"), "Date", dataModel);
//                } catch (IOException e) {
//                    System.exit(0);
//                    e.printStackTrace();
//                }
//            }
//        });

        pcpView = new PCPView();
        pcpView.setPrefHeight(500);
        pcpView.setAxisSpacing(100);
        pcpView.setPadding(new Insets(10));

        pcpView.setDisplayMode(PCPView.DISPLAY_MODE.PCP_LINES);

        ScrollPane scrollPane = new ScrollPane(pcpView);
        scrollPane.setFitToWidth(pcpView.getFitToWidth());
        scrollPane.setFitToHeight(true);

        Button dataButton = new Button("Load Data");
        dataButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    long start = System.currentTimeMillis();
//                    IOUtilities.readCSV(new File("data/csv/cars.csv"), null, null,
//                            null, null, dataModel);
//                    IOUtilities.readCSV(new File("/Users/csg/Dropbox (ORNL)/projects/SciDAC/data/2018-01-RiccuitoEnsemble/QMCdaily_US_combined.csv"),
//                            null, null, null, null, dataModel);

                    ArrayList<String> categoricalColumnNames = new ArrayList<>();
                    categoricalColumnNames.add("Origin");
                    IOUtilities.readCSV(new File("data/csv/cars-cat.csv"), null, categoricalColumnNames,
                            null, null, dataModel);
//                    ArrayList<String> temporalColumnNames = new ArrayList<>();
//                    temporalColumnNames.add("Date");
//                    ArrayList<DateTimeFormatter> temporalColumnFormatters = new ArrayList<>();
//                    temporalColumnFormatters.add(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
////                    ArrayList<String> ignoreColumnNames = new ArrayList<>();
//////                    ignoreColumnNames.add("StageoutPilots");
////
//                    IOUtilities.readCSV(new File("data/csv/titan-performance.csv"), null, null,
//                            temporalColumnNames, temporalColumnFormatters, dataModel);
                    long elapsed = System.currentTimeMillis() - start;
                    log.info("Reading data and populating data model took " + elapsed + " ms");
                } catch (IOException e) {
                    System.exit(0);
                    e.printStackTrace();
                }
            }
        });

        ChoiceBox<PCPView.DISPLAY_MODE> displayModeChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(PCPView.DISPLAY_MODE.PCP_LINES,
                PCPView.DISPLAY_MODE.PCP_BINS, PCPView.DISPLAY_MODE.HISTOGRAM, PCPView.DISPLAY_MODE.SUMMARY));
        displayModeChoiceBox.getSelectionModel().select(0);
        displayModeChoiceBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                PCPView.DISPLAY_MODE newDisplayMode = displayModeChoiceBox.getValue();
                pcpView.setDisplayMode(newDisplayMode);
            }
        });

//        ChoiceBox<Orientation> orientationChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList(Orientation.HORIZONTAL, Orientation.VERTICAL));
//        orientationChoiceBox.getSelectionModel().select(pcpView.getOrientation());
//        orientationChoiceBox.setOnAction(event -> {
//            pcpView.setOrientation(orientationChoiceBox.getValue());
//        });

        Slider opacitySlider = new Slider(0., 1., pcpView.getDataItemsOpacity());
        opacitySlider.valueProperty().bindBidirectional(pcpView.dataItemsOpacityProperty());
        opacitySlider.setShowTickLabels(false);
        opacitySlider.setShowTickMarks(false);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(2.);
        buttonBox.getChildren().addAll(dataButton, displayModeChoiceBox, opacitySlider);

        BorderPane rootNode = new BorderPane();
        rootNode.setCenter(scrollPane);
        rootNode.setBottom(buttonBox);

        Scene scene = new Scene(rootNode, 960, 500, true, SceneAntialiasing.BALANCED);

        stage.setTitle("PCPView Test");
        stage.setScene(scene);
        stage.show();

        dataModel = new DataTable();
        pcpView.setDataTable(dataModel);
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    public static void main (String[] args) {
        launch(args);
    }
}
