
const protocol = window.location.protocol;
const host = window.location.hostname;
const port = window.location.port;
export const apiHost = import.meta.env.MODE === "development"
    ? import.meta.env.VITE_API_HOST : `${protocol}//${host}:${port}`;
// export const apiHost = import.meta.env.VITE_API_HOST;

export const FETCH_DELAY_MS = 500; // delays by 500ms for server requests, so user sees that something is loading

export const cityList = ["Ainaži", "Alūksne", "Bauska", "Dagda", "Daugavgrīva", "Daugavpils", "Dobele", "Gulbene", "Jelgava", "Kalnciems", "Kolka", "Kuldīga", "Lielpēči", "Liepāja", "Madona", "Mērsrags", "Pāvilosta", "Piedruja", "Priekuļi", "Rēzekne", "Rīga", "Rucava", "Rūjiena", "Saldus", "Sigulda", "Sīļi", "Skrīveri", "Skulte", "Stende", "Ventspils", "Vičaki", "Zīlāni", "Zosēni"];

export const weatherField = ["tempMax", "tempMin", "tempAvg", "precipitation", "windAvg", "windMax", "visibilityMin", "visibilityAvg", "snowAvg", "atmPressire", "dewPoint", "humidity", "sunDuration", "phenomena"];

export const weatherFieldNumeric = weatherField.filter(f => f !== "phenomena");

// export const aggregateKey = ["min", "max", "avg", "sum", "list", "distinct"];
export const aggregateKey = ["min", "max", "avg", "sum", "list"];

export const aggregateGranularity = ["hour", "day", "month", "year"];

export type ResultKeyVal = [key: string, value: number | Array<any>];

export const resultOrder = {
    "A -> Z": (a: ResultKeyVal, b: ResultKeyVal) => a[0] > b[0] ? 1 : -1,
    "Z -> A":  (a: ResultKeyVal, b: ResultKeyVal) => a[0] > b[0] ? -1 : 1,
    "Min": (a: ResultKeyVal, b: ResultKeyVal) => a[1] > b[1] ? 1 : -1,
    "Max": (a: ResultKeyVal, b: ResultKeyVal) => a[1] > b[1] ? -1 : 1,
};

type ResultOrder = typeof resultOrder;
export type ResultOrderKeys = keyof ResultOrder;