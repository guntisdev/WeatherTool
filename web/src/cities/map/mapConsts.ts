import map_1920x1080 from "../../assets/map_1920x1080.webp";
import map_1920x1080_wind from "../../assets/map_1920x1080_wind.webp";
import map_3840x1440 from "../../assets/map_3840x1440.webp";
import map_3840x1440_wind from "../../assets/map_3840x1440_wind.webp";

export type MapResolution = "map_1920x1080" | "map_1920x1080_wind" | "map_3840x1440" | "map_3840x1440_wind";

export type ResolutionPropsValue = {
    offsetX: number;
    offsetY: number;
    width: number;
    height: number;
    scale: number;
    showWind: boolean;
    map: string;
    fontSize: number;
    boxSize: number;
};

export const resolutionProps: Record<string, ResolutionPropsValue> = {
    "map_1920x1080":        { offsetX: 290, offsetY: 120, width: 1920, height: 1080, scale: 1.35, fontSize: 30, boxSize: 80, showWind: false, map: map_1920x1080 },
    "map_1920x1080_wind":   { offsetX: 90, offsetY: 120, width: 1920, height: 1080, scale: 1.35, fontSize: 30, boxSize: 80, showWind: true, map: map_1920x1080_wind },
    "map_3840x1440":        { offsetX: 800, offsetY: 120, width: 3840, height: 1440, scale: 2.2, fontSize: 68, boxSize: 140, showWind: false, map: map_3840x1440 },
    "map_3840x1440_wind":   { offsetX: 800, offsetY: 120, width: 3840, height: 1440, scale: 2.2, fontSize: 68, boxSize: 140, showWind: true, map: map_3840x1440_wind },
}

export type WindProps = {
    offsetX: number;
    offsetY: number;
    scaleY: number;
};

export const windProps: Record<string, WindProps> = {
    "map_1920x1080_wind": { offsetX: 0, offsetY: 0, scaleY: 1, },
    "map_3840x1440_wind": { offsetX: 1610, offsetY: 80, scaleY: 1.24, },
}
