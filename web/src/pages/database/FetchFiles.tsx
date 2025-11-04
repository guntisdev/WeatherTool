import moment from "moment";
import { createSignal } from "solid-js";
import { apiHost } from "../../consts";

export function FetchFiles() {
    const [getFetchResult, setFetchResult] = createSignal("");
    const [isSpinner, setIsSpinner] = createSignal(false);

    async function handleSubmit(e: MouseEvent) {
        e.preventDefault();
        setIsSpinner(true);
        const response = await fetch(`${apiHost}/api/fetch/lvgmc/stations`);
        const text = await response.text();
        setFetchResult(text);
        await new Promise(resolve => setTimeout(resolve, 200));
        setIsSpinner(false)
    }

    return (
        <form>
            <h3>Manual fetch</h3>
            <p>
                <input type="submit" onClick={handleSubmit} value="Fetch most recent .csv file" />
            </p>
            <p>result:</p>
            {isSpinner() && <div>
                <span class="spinner"></span>
                <span style={{ "padding-left": "16px" }}>Fetching csv files</span>
            </div>
            }
            <p>{getFetchResult()}</p>
        </form>
    )
}

