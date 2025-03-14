import moment from "moment";
import { createSignal } from "solid-js";

import "../../css/FileManager.css"
import { DateList } from "./DateList";
import { FetchFiles } from "./FetchFiles";
import { FileContent } from "./FileContent";
import { FileNameList } from "./FileNameList";

export function Database() {
    const [getDate, setDate] = createSignal(new Date());
    const [getFileName, setFileName] = createSignal("");

    const stringDate = () => moment(getDate()).format("YYYYMMDD");

    return (
        <div class="fileManager">
            <div class="container">
                <div class="column">
                    <DateList getDate={getDate} setDate={setDate} />
                    <FetchFiles />
                </div>
                <div class="column">
                    <h3>{stringDate()}</h3>
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