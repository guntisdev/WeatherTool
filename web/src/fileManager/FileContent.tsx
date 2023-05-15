import { Accessor, Component, createResource, JSXElement } from "solid-js";
import { apiHost } from "../consts";

export const FileContent: Component<{getFileName: Accessor<string>}> = (props) => {
    const fetchFileContent = async (fileName: string) => {
        if (fileName === "") return;
        await new Promise(resolve => setTimeout(resolve, 500))
        const response = await fetch(`${apiHost}/api/show/file/${fileName}`);
        const text = await response.text();
        return text;
    }

    const [contentResource] = createResource(props.getFileName, fetchFileContent);
    
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
                <div>{splitLines(contentResource())}</div>
            )}
        </div>
    )
}

function splitLines(str?: string): JSXElement {
    const lines = str?.split("<br/>") ?? ["empty..."]
    return lines.map(line =>
        <div>{line}</div>
    );
}