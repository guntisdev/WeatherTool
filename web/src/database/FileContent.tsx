import { Accessor, Component, createResource, JSXElement } from "solid-js";
import { apiHost, FETCH_DELAY_MS } from "../consts";
import { PrettifyCSV } from "./PrettifyCSV";
import { LoadingSpinner } from "../components/LoadingSpinner";

export const FileContent: Component<{getFileName: Accessor<string>}> = (props) => {
    const fetchFileContent = async (fileName: string) => {
        if (fileName === "") return;
        await new Promise(resolve => setTimeout(resolve, FETCH_DELAY_MS))
        const response = await fetch(`${apiHost}/api/show/datetime/${fileName}`);
        const text = await response.json();
        return text;
    }

    const [contentResource] = createResource(props.getFileName, fetchFileContent);
    const lines = () => contentResource();
    
    return (
        <div>
            { contentResource.loading && <LoadingSpinner text="Loading date-time weather" /> }
            { contentResource.error && (
                <div>Error while loading file content: ${contentResource.error}</div>
            )}
            { contentResource() && (
                <PrettifyCSV lines={lines} />
            )}
        </div>
    )
}