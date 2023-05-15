import { createSignal } from "solid-js";
import "../css/FileManager.css"
import { DateList } from "./DateList";
import { FileContent } from "./FileContent";
import { FileNameList } from "./FileNameList";

export function FileManager() {
    const [getDate, setDate] = createSignal("");
    const [getFileName, setFileName] = createSignal("");

    return (
        <div class="fileManager">
            <h2>File manager</h2>
            <div class="container">
                <div class="column">
                    <DateList setDate={setDate} />
                </div>
                <div class="column">
                    <h3>{getDate()}</h3>
                    <FileNameList getDate={getDate} setFileName={setFileName} />
                </div>
                <div class="column">
                    <h3>{getFileName()}</h3>
                    <FileContent getFileName={getFileName} />
                </div>
            </div>
        </div>
    );
}