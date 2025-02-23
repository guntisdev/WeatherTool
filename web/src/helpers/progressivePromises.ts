export async function handleProgressivePromises<T>(
    promises: Promise<T>[],
    onProgress: (result: T) => void,
): Promise<(T | undefined)[]> {
    const allPromises = promises.map(async promise => {
        try {
            const result = await promise;
            onProgress(result)
            return result
        } catch (error) {
            return undefined
        }
    })

    const results = await Promise.allSettled(allPromises)
    return results.map(result => 
        result.status === 'fulfilled' ? result.value : undefined
    )
}
