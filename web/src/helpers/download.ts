export async function downloadImagesAsZip(images: [string, ImageBitmap][]) {
    downloadCompressedImage(images[0][1], '00.gzip')
}

async function compressImage(bitmap: ImageBitmap): Promise<Blob> {
    // First convert ImageBitmap to WebP using canvas
    const canvas = document.createElement('canvas');
    canvas.width = bitmap.width;
    canvas.height = bitmap.height;
    
    const ctx = canvas.getContext('2d');
    if (!ctx) throw new Error('Could not get canvas context');
    
    ctx.drawImage(bitmap, 0, 0);
    
    // Get WebP blob
    const webpBlob = await new Promise<Blob>((resolve) => {
      canvas.toBlob((blob) => {
        if (blob) resolve(blob);
      }, 'image/webp', 0.8); // Quality 0.8, adjust as needed
    });
  
    // Convert Blob to ReadableStream
    const webpStream = webpBlob.stream();
    
    // Apply GZIP compression
    const compressedStream = webpStream.pipeThrough(
      new CompressionStream('gzip')
    );
  
    // Return as Blob
    return new Response(compressedStream).blob();
  }
  
  // Usage example:
  async function downloadCompressedImage(bitmap: ImageBitmap, filename: string) {
    const compressedBlob = await compressImage(bitmap);
    
    const url = URL.createObjectURL(compressedBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    
    URL.revokeObjectURL(url);
  }