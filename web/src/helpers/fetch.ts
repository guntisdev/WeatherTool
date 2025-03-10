export const fetchJson = (url: string, options = {}) => {
    return fetch(url, options).then(async response => {
        let responseCode = ''
        if (!response.ok) {
            responseCode = `error ${response.status}`
        }
    
        let data
        let responseMessage = ''
        try {
            data = await response.json()
            if (data.error) responseMessage = data.error
        } catch (err) {
            responseMessage = 'Invalid JSON response'
        }

        if (responseCode) {
            if (responseMessage) throw new Error(`${responseCode}: ${responseMessage}`)
            else throw new Error(responseCode)
        } else if (responseMessage) {
            throw new Error(responseMessage)
        }

        return data
    })
}

export const fetchText = (url: string, options = {}) => {
    return fetch(url, options).then(async response => {
        let responseCode = ''
        if (!response.ok) {
            responseCode = `error ${response.status}`
        }
    
        let text = ''
        let responseMessage = ''
        try {
            text = await response.text()
        } catch (err) {
            responseMessage = 'Invalid TEXT response'
        }

        if (responseCode) {
            if (responseMessage) throw new Error(`${responseCode}: ${responseMessage}`)
            else throw new Error(responseCode)
        } else if (responseMessage) {
            throw new Error(responseMessage)
        }

        return text
    })
}


export const fetchBuffer = (url: string, options = {}) => {
    return fetch(url, options).then(async response => {
        let responseCode = ''
        if (!response.ok) {
            responseCode = `error ${response.status}`
        }
    
        let data
        try {
            data = await response.arrayBuffer()
        } catch (err) {
            throw new Error('Invalid ArrayBuffer response')
        }

        if (responseCode) {
            throw new Error(responseCode)
        }

        return data
    })
}
