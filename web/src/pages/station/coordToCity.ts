export function coordToCity(
    x: number,
    y: number,
    cityCoords: [string, { x: number, y: number}][],
    boxWidth = 60,
    boxHeight = 30,
): string | undefined {
    const optionalCity = cityCoords.find(([city, coord]) =>
        coord.x >= x-boxWidth/2
        && coord.x <= x+boxWidth/2
        && coord.y >= y-boxHeight/2
        && coord.y <= y+boxHeight/2
    );

    return optionalCity && optionalCity[0];
}