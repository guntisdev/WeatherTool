// converted from C code https://opendatadocs.dmi.govcloud.dk/Data/Forecast_Data_Weather_Model_HARMONIE
export function  rotateWind(
    rot_lat: number,
    rot_lon: number,
    reg_lat: number,
    reg_lon: number,
    u_in: number,
    v_in: number,
    southpole_lat = 26.5,
    southpole_lon = -40,
): [direction: number, strength: number, u_out: number, v_out: number]
/* Given either a point in the regular grid (set `*rot_lat' <= -999.0)
* or a point in the rotated grid, calculate the corresponding point
* in the opposite grid, change the (u, v)-vector from rotated to
* regular grid and calculate the wind force (`*strength') and the
* wind direction in the regular grid. `southpole_lat' and
* `southpole_lon' defines the coordinate of the southpole in
* the roated grid */
{
    /* Find the missing point, whether is is the rotated or the regular */
    if (rot_lat <= -999.0) [rot_lat, rot_lon] = reg2rot(reg_lat, reg_lon, southpole_lat, southpole_lon)
    else [reg_lat, reg_lon] = rot2reg(rot_lat, rot_lon, southpole_lat, southpole_lon);

    /* Calculate the wind strength */
    const strength = Math.sqrt(u_in*u_in + v_in*v_in);

    /* Add a small distance in the direction of the wind to the rotated
    * grid point, changing the distance into degrees */
    const rot_lat2 = rot_lat + 0.1*v_in/(strength);
    let clat = Math.cos(rot_lat*Math.PI/180.0)
    if (0.0001 > clat && clat > -0.0001) {
        throw new Error("Internal error: Too close to pole to calculate rotated wind")
    }
    const rot_lon2 = rot_lon + 0.1*u_in/(strength * clat);

    /* Translate new rotated grid point to regular grid */
    const [reg_lat2, reg_lon2] = rot2reg(rot_lat2, rot_lon2, southpole_lat, southpole_lon)

    /* Transform offset in lat-lon to offset in x-y */
    clat = Math.cos(reg_lat*Math.PI/180.0)
    const dx = clat*(reg_lon2 - reg_lon)

    /* Calculate the direction of the wind vector in the regular grid */
    const direc = Math.atan2(reg_lat2 - reg_lat, dx);

    /* Regular direction in degrees */
    let direction = 630.0 - direc*180.0 / Math.PI
    while (direction > 360.0) direction -= 360.0

    const u_out = Math.cos(direc) * strength
    const v_out = Math.sin(direc) * strength

    return [direction, strength, u_out, v_out]
}


function rot2reg(
    rot_lat: number,
    rot_lon: number,
    southpole_lat: number,
    southpole_lon: number,
): [reg_lat: number, reg_lon: number]
/* Convert from rotated latitude-longitude to regular latitude-longitude
with the transformation defined by the southpole coordinates.
Coordinates are given in degrees N (negative for S) and degrees E
(negative for W). */
{
    const to_rad = Math.PI/180.0
    const to_deg = 1.0/to_rad

    const sin_y_cen = Math.sin(to_rad*(southpole_lat + 90.0))
    const cos_y_cen = Math.cos(to_rad*(southpole_lat + 90.0))

    const sin_x_rot = Math.sin(to_rad*rot_lon)
    const cos_x_rot = Math.cos(to_rad*rot_lon)
    const sin_y_rot = Math.sin(to_rad*rot_lat)
    const cos_y_rot = Math.cos(to_rad*rot_lat)
    let sin_y_reg = cos_y_cen*sin_y_rot + sin_y_cen*cos_y_rot*cos_x_rot
    if (sin_y_reg < -1.0) sin_y_reg = -1.0
    if (sin_y_reg > 1.0) sin_y_reg = 1.0

    const reg_lat = to_deg*Math.asin(sin_y_reg)

    const cos_y_reg = Math.cos(reg_lat*to_rad);
    let cos_lon_rad = (cos_y_cen*cos_y_rot*cos_x_rot - sin_y_cen*sin_y_rot)/cos_y_reg;
    if (cos_lon_rad < -1.0) cos_lon_rad = -1.0;
    if (cos_lon_rad > 1.0) cos_lon_rad = 1.0;
    const sin_lon_rad = cos_y_rot*sin_x_rot/cos_y_reg;
    let lon_rad = Math.acos(cos_lon_rad);
    if (sin_lon_rad < 0.0) lon_rad = -lon_rad;

    const reg_lon = to_deg*lon_rad + southpole_lon;

    return [reg_lat, reg_lon]
}


function reg2rot(
    reg_lat: number,
    reg_lon: number,
    southpole_lat: number,
    southpole_lon: number,
): [rot_lat: number, rot_lon: number]
/* Convert from regular latitude-longitude to rotated latitude-longitude
with the transformation defined by the southpole coordinates.
Coordinates are given in degrees N (negative for S) and degrees E
(negative for W). */
{
    const to_rad = Math.PI/180.0
    const to_deg = 1.0/to_rad
    const sin_y_cen = Math.sin(to_rad*(southpole_lat + 90.0));
    const cos_y_cen = Math.cos(to_rad*(southpole_lat + 90.0));

    const lon_rad = to_rad*(reg_lon - southpole_lon)
    const sin_lon_rad = Math.sin(lon_rad)
    const cos_lon_rad = Math.cos(lon_rad)
    const sin_y_reg = Math.sin(to_rad*reg_lat)
    const cos_y_reg = Math.cos(to_rad*reg_lat)
    let sin_y_rot = cos_y_cen*sin_y_reg - sin_y_cen*cos_y_reg*cos_lon_rad
    if (sin_y_rot < -1.0) sin_y_rot = -1.0
    if (sin_y_rot > 1.0) sin_y_rot = 1.0

    const rot_lat = Math.asin(sin_y_rot)*to_deg

    const cos_y_rot = Math.cos(rot_lat*to_rad)
    let cos_x_rot = (cos_y_cen*cos_y_reg*cos_lon_rad + sin_y_cen*sin_y_reg)/cos_y_rot
    if (cos_x_rot < -1.0) cos_x_rot = -1.0
    if (cos_x_rot > 1.0) cos_x_rot = 1.0
    const sin_x_rot = cos_y_reg*sin_lon_rad/cos_y_rot

    let rot_lon = Math.acos(cos_x_rot)*to_deg
    if (sin_x_rot < 0.0) rot_lon = -rot_lon

    return [rot_lat, rot_lon]
}
