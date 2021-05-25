/*
 *     Copyright (c) 2021 Andrew Bueide
 *
 *     This file is part of Harry Plotter.
 *
 *     Harry Plotter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Harry Plotter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Harry Plotter.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.abysl.harryplotter.view

import com.abysl.harryplotter.model.records.GithubRelease
import javafx.application.HostServices
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.stage.Stage

class ReleaseView {

    @FXML
    lateinit var releaseVersion: Label
    @FXML
    lateinit var changeLog: TextArea

    lateinit var hostServices: HostServices

    fun initialized(release: GithubRelease, hostServices: HostServices) {
        changeLog.wrapTextProperty().set(true)
        releaseVersion.text = release.version
        changeLog.text = release.changeLog
        this.hostServices = hostServices
    }

    fun onCancel() {
        (releaseVersion.scene.window as Stage).close()
    }

    fun onDownload() {
        hostServices.showDocument("https://github.com/abueide/harry-plotter/releases")
    }
}
