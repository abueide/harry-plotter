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

package com.abysl.harryplotter.ui.main

import com.abysl.harryplotter.config.Config
import com.abysl.harryplotter.config.Prefs
import com.abysl.harryplotter.model.GithubRelease
import com.abysl.harryplotter.ui.all.Window
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.request.get
import javafx.application.HostServices
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ReleaseWindow(val hostServices: HostServices) : Window<ReleaseView>() {

    val client = HttpClient(Java)

    fun show() {
        val release = getRelease()
        if (release != null &&
            !release.version.contains(Config.version) &&
            !release.version.contains(Prefs.lastReleaseShown) &&
            !release.version.contains("-RC")
        ) {
            Prefs.lastReleaseShown = release.version
            val controller = create(
                "New Release is available, would you like to download it?",
                "fxml/Release.fxml"
            )
            controller.initialized(release, hostServices)
        }
    }

    fun getRelease(): GithubRelease? {
        var release: GithubRelease? = null
        runBlocking {
            val response: String = client.get("https://api.github.com/repos/abueide/harry-plotter/releases")
            val releases = Json.parseToJsonElement(response) as JsonArray
            val firstRelease = releases.firstOrNull()
            firstRelease?.let {
                val version = it.jsonObject["name"]?.jsonPrimitive?.content
                val message = it.jsonObject["body"]?.jsonPrimitive?.content
                if (version == null || message == null) {
                    println("Release parse error")
                } else {
                    release = GithubRelease(version, message)
                }
            }
            client.close()
        }
        return release
    }
}
