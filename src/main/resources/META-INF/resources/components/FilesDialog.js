// import * as EncoderClient from "/EncoderClient.js"
import {ls, startEncoding} from "../EncoderClient.js"

class FileList extends HTMLElement {

    constructor() {
        super();

    }

    connectedCallback() {

        console.log('Custom square element added to page.');
        const ul = document.createElement("ul");
        this.list().then(json => {
            json
                .forEach(l => {
                    const il = document.createElement("il");
                    il.innerText = `${l.name}   -  ${l.size}`;
                    il.style = "display:block;width:100%;cursor: pointer;";
                    il.addEventListener("click", ev => {
                        const sourceName = l.name;
                        const destName = buildTargetName(sourceName);
                        console.info("event", l.name);
                        if (confirm(`should proceed: ${sourceName} to ${destName}`)) {
                            this.beginEncoding(sourceName, destName);
                        }
                    });
                    ul.appendChild(il);
                });

            this.appendChild(ul);
        })
    }

    async list() {
        let json = await ls();
        console.info("response", json);
        return json;
    }

    async beginEncoding(sourceName, destName) {

        let json = await startEncoding(sourceName, destName);

        console.info("Json received", json);

    }
}


function buildTargetName(sourceName) {

    const split = sourceName.split(".");

    if (split.length === 1) {
        return sourceName + ".mp4";
    }

    split.pop();
    return split.join(".") + ".out.mp4";
}

customElements.define('file-list', FileList);