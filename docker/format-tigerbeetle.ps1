docker run --rm `
    --security-opt seccomp=unconfined `
    -v "${PSScriptRoot}/tigerbeetle/data:/data" `
    ghcr.io/tigerbeetle/tigerbeetle:0.17.8 `
    format `
    --cluster=0 `
    --replica=0 `
    --replica-count=1 `
    /data/0_0.tigerbeetle