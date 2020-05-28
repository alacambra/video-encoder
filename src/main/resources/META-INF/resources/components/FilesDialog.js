// import * as EncoderClient from "/EncoderClient.js"
import {ls} from "../EncoderClient.js"

class FileList extends HTMLElement {

    constructor() {
        super();

    }

    connectedCallback() {
        console.log('Custom square element added to page.');
        const ul = document.createElement("ul");
        this.list().then(json => {
            json
                .forEach(l =>{
                    const il = document.createElement("il");
                    il.innerText=l;
                    il.style="display:block";
                    ul.appendChild(il);
                });

            this.appendChild(ul);
        })

    }

    async list() {
        let json = await ls();
        // let json = await EncoderClient.listSources();
        console.info("response", json);
        return json;
    }
}

customElements.define('file-list', FileList);