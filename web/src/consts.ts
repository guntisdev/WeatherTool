export const apiHost = import.meta.env.VITE_API_HOST;

export const cityList = ["Ainaži", "Alūksne", "Bauska", "Dagda", "Daugavgrīva", "Daugavpils", "Dobele", "Gulbene", "Jelgava", "Kalnciems", "Kolka", "Kuldīga", "Lielpēči", "Liepāja", "Madona", "Mērsrags", "Pāvilosta", "Piedruja", "Priekuļi", "Rēzekne", "Rīga", "Rucava", "Rūjiena", "Saldus", "Sigulda", "Sīļi", "Skrīveri", "Skulte", "Stende", "Ventspils", "Vičaki", "Zīlāni", "Zosēni"];

export const weatherField = ["tempMax", "tempMin", "tempAvg", "precipitation", "windAvg", "windMax", "visibilityMin", "visibilityAvg", "snowAvg", "atmPressire", "dewPoint", "humidity", "sunDuration", "phenomena"];

export const aggregateKey = ["min", "max", "avg", "sum", "list", "distinct"];

export type ResultKeyVal = [key: string, value: number | Array<any>];

export const resultOrder = {
    "A -> Z": (a: ResultKeyVal, b: ResultKeyVal) => a[0] > b[0] ? 1 : -1,
    "Z -> A":  (a: ResultKeyVal, b: ResultKeyVal) => a[0] > b[0] ? -1 : 1,
    "Min": (a: ResultKeyVal, b: ResultKeyVal) => a[1] > b[1] ? 1 : -1,
    "Max": (a: ResultKeyVal, b: ResultKeyVal) => a[1] > b[1] ? -1 : 1,
};

type ResultOrder = typeof resultOrder;
export type ResultOrderKeys = keyof ResultOrder;