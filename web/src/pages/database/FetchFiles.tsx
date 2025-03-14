import moment from "moment";
import { createSignal } from "solid-js";
import { apiHost } from "../../consts";

export function FetchFiles() {
    const [getStart, setStart] = createSignal(new Date());
    const [getEnd, setEnd] = createSignal(new Date());
    const [getFetchResult, setFetchResult] = createSignal("");
    const [isSpinner, setIsSpinner] = createSignal(false);

    const startStr = () => moment(getStart()).format("YYYY-MM-DD");
    const endStr = () => moment(getEnd()).format("YYYY-MM-DD");

    function handleSubmit(e: MouseEvent) {
        e.preventDefault();
        const dates = getDatesBetween(getStart(), getEnd());
        timerFetch(dates);
    }

    async function timerFetch(dates: Date[]) {
        setIsSpinner(true);
        for (const date of dates) {
            const response = await fetch(`${apiHost}/api/fetch/date/${moment(date).format("YYYYMMDD")}`);
            const text = await response.text();
            setFetchResult(text);
            await new Promise(resolve => setTimeout(resolve, 200));
        }
        setIsSpinner(false);
    }

    return (
        <form>
            <h3>Date range fetch</h3>
            <p>
                <input
                    type="date"
                    value={ startStr() }
                    onChange={e => setStart(new Date(e.target.value))}
                /> start
            </p>
            <p>
                <input
                    type="date"
                    value={ endStr() }
                    onChange={e => setEnd(new Date(e.target.value))}
                /> end
            </p>
            <p>
                <input type="submit" onClick={handleSubmit} value="Fetch .csv files" />
            </p>
            <p>result:</p>
            { isSpinner() && <div>
                    <span class="spinner"></span>
                    <span style={{ "padding-left": "16px" }}>Fetching csv files</span>
                </div>
            }
            <p>{ getFetchResult() }</p>
        </form>
    )
}

function getDatesBetween(startDate: Date, endDate: Date): Date[] {
    const dates: Date[] = [];
    let currentDate = new Date(startDate); // start from the start date

    while (currentDate <= endDate) {
        dates.push(new Date(currentDate)); // add current date to the list
        currentDate.setDate(currentDate.getDate() + 1); // increment the date
    }

    return dates;
}