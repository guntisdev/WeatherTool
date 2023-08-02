import moment from "moment";

export function formatDateString(str: string): string {
    const date = moment(new Date(str));
    switch (str.length) {
      // year: 2023
      case 4: return date.format("yyyy");
      // year-month: 2023-06
      case 7: return date.format("yyyy-MM");
      // year-month-day: 2023-06-23
      case 10: return date.format("yyyy-MM-DD");
      // year-month-day hour:minute 2023-06-23T23:59
      default: return date.format("HH:mm");
    }
}

export interface DataQuery {
  cities: string[];
  field: string;
  granularity: string;
  key: string;
}

export function isDataQuery(variable: any): variable is DataQuery {
  return variable && 
      Array.isArray(variable.cities) && 
      typeof variable.field === 'string' && 
      typeof variable.granularity === 'string' &&
      typeof variable.key === 'string';
}
