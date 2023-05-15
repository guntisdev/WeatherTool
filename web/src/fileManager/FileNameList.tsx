import { Accessor, Component, createResource, Setter } from "solid-js";
import { apiHost } from "../consts";

import "../css/spinner.css";

export const FileNameList: Component<{
    getDate: Accessor<string>,
    setFileName: Setter<string>,
}> = (props) => {
    const fetchFileNames = async (date: string) => {
        if (props.getDate() === "") return;
        await new Promise(resolve => setTimeout(resolve, 500))
        const response = await fetch(`${apiHost}/api/show/date/${date.replace(/-/g, "")}`);
        const json = await response.json();
        return json;
    }

    const [fileNameResource] = createResource(props.getDate, fetchFileNames);

    function clickFileName(fileName: string) {
        console.log(fileName);
        props.setFileName(fileName);
    }

    return (
        <div>
            { fileNameResource.loading && (
                <div>
                    <span class="spinner"></span>
                    <span style={{ "padding-left": "16px" }}>loading {props.getDate()}</span>
                </div>
            )}
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