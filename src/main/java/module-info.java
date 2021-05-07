module com.abysl.harryplotter {
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core.jvm;
    requires kotlinx.coroutines.javafx;
    requires kotlinx.serialization.core.jvm;
    requires kotlinx.serialization.json.jvm;
    requires java.prefs;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

    opens com.abysl.harryplotter.controller to javafx.fxml;
    opens com.abysl.harryplotter.data to kotlinx.serialization.core.jvm;
    exports com.abysl.harryplotter;
}