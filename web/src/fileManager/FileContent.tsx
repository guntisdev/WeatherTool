import { Accessor, Component, createResource, JSXElement } from "solid-js";
import { apiHost } from "../consts";
import { PrettifyCSV } from "./PrettifyCSV";

export const FileContent: Component<{getFileName: Accessor<string>}> = (props) => {
    const fetchFileContent = async (fileName: string) => {
        if (fileName === "") return;
        await new Promise(resolve => setTimeout(resolve, 500))
        const response = await fetch(`${apiHost}/api/show/file/${fileName}`);
        const text = await response.json();
        return text;
    }

    const [contentResource] = createResource(props.getFileName, fetchFileContent);
    const lines = () => contentResource();
    
    return (
        <div>
            { contentResource.loading && (
                <div>
                    <span class="spinner"></span>
                    <span style={{ "padding-left": "16px" }}>load content</span>
                </div>
            )}
            { contentResource.error && (
                <div>Error while loading file content: ${contentResource.error}</div>
            )}
            { contentResource() && (
                <PrettifyCSV lines={lines} />
            )}
        </div>
    )
}