import com.abysl.harryplotter.view.settings.HarryPlotterSettingsView
import com.abysl.harryplotter.windows.Window

class HarryPlotterSettingsWindow : Window<HarryPlotterSettingsView>() {
    fun show() {
        create("Harry Plotter Settings", "fxml/settings/HarryPlotterSettings.fxml")
    }
}
