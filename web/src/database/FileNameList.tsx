import moment from "moment";
import { Accessor, Component, createResource, Setter } from "solid-js";
import { apiHost, FETCH_DELAY_MS } from "../consts";

import "../css/spinner.css";
import { LoadingSpinner } from "../components/LoadingSpinner";

export const FileNameList: Component<{
    getDate: Accessor<Date>,
    setFileName: Setter<string>,
}> = (props) => {
    const fetchFileNames = async (fetchDate: Date) => {
        await new Promise(resolve => setTimeout(resolve, FETCH_DELAY_MS))
        const response = await fetch(`${apiHost}/api/show/date/${moment(fetchDate).format("YYYYMMDD")}`);
        const json = await response.json();
        return json;
    }

    const [fileNameResource] = createResource(props.getDate, fetchFileNames);

    function clickFileName(fileName: string) {
        props.setFileName(fileName);
    }

    return (
        <div>
            { fileNameResource.loading && <LoadingSpinner text={"Loading date: "+moment(props.getDate()).format("YYYYMMDD")} /> }
            { fileNameResource.error && (
                <div>Error while loading file names: ${fileNameResource.error}</div>
            )}
            { fileNameResource() && (
                <ul>
                    { (fileNameResource() as any).map((fileName: any) =>
                        <li onClick={() => clickFileName(fileName)}>{fileName}</li>
                    )}
                </ul>
            )}
        </div>
    )
    return <div>yoyoyoy</div>
}