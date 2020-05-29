function listSources() {
    return fetch("/encode/source")
        .then(response => inspectResponse(response))
        .then(response => response.json());
}

function inspectResponse(response) {
    console.info(response.status, response.headers);
    return response;
}

function startEncoding(source, target) {

    const data = {
        source: source, target: target
    };

    return fetch('encode', {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(data),

    }).then(response => inspectResponse(response))
        .then(response => response.json());
}

export {listSources as ls, startEncoding};