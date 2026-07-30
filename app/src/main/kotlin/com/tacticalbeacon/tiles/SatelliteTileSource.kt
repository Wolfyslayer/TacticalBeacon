package com.tacticalbeacon.tiles

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex

object SatelliteTileSource : OnlineTileSourceBase(
    "Esri World Imagery",
    0,
    19,
    256,
    ".jpg",
    arrayOf("https://server.arcgisonline.com/")
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/" +
                MapTileIndex.getZoom(pMapTileIndex) + "/" +
                MapTileIndex.getY(pMapTileIndex) + "/" +
                MapTileIndex.getX(pMapTileIndex) +
                ".jpg"
    }
}