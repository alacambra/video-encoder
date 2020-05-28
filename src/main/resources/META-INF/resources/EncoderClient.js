
function listSources(){
   return fetch("/encode/source")
       .then(response => inspectResponse(response))
       .then(response => response.json());
}

function inspectResponse(response) {
    console.info(response.status, response.headers);
    return response;
}

export {listSources as ls};