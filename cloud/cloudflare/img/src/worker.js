/**
 * Welcome to Cloudflare Workers! This is your first worker.
 *
 * - Run "npm run dev" in your terminal to start a development server
 * - Open a browser tab at http://localhost:8787/ to see your worker in action
 * - Run "npm run deploy" to publish your worker
 *
 * Learn more at https://developers.cloudflare.com/workers/
 */

// 定義常見圖片和影片副檔名與其對應的 MIME 類型映射
const mimeTypeMap = {
  // 圖片格式
  'jpg': 'image/jpeg',
  'jpeg': 'image/jpeg',
  'png': 'image/png',
  'gif': 'image/gif',
  'webp': 'image/webp',
  'svg': 'image/svg+xml',
  'ico': 'image/x-icon',
  // 影片格式
  'mp4': 'video/mp4',
  'webm': 'video/webm',
  'mov': 'video/quicktime',
  'avi': 'video/x-msvideo',
  'mkv': 'video/x-matroska',
  'flv': 'video/x-flv',
  'wmv': 'video/x-ms-wmv',
};

// 文件大小限制 (單位: bytes)
const FILE_SIZE_LIMITS = {
  image: 10 * 1024 * 1024,  // 圖片: 10MB
  video: 20 * 1024 * 1024, // 影片: 20MB
};

// 識別檔案類型
const getFileType = (ext) => {
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'ico'];
  const videoExts = ['mp4', 'webm', 'mov', 'avi', 'mkv', 'flv', 'wmv'];
  
  const lowerExt = ext.toLowerCase();
  if (imageExts.includes(lowerExt)) return 'image';
  if (videoExts.includes(lowerExt)) return 'video';
  return null;
};

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    // 測試排程
    if (url.pathname === "/cleanup") {
      await this.scheduled(null, env, ctx);
      return new Response("Cleanup triggered");
    }

    // 上傳圖片或影片
    if (url.pathname === "/upload" && request.method === "POST") {
      const form = await request.formData();
      const file = form.get("image");

      if (!(file instanceof File)) {
        return new Response("No file", {status: 400});
      }

      const ext = file.name.split(".").pop();
      const fileType = getFileType(ext);

      // 驗證檔案類型
      if (!fileType) {
        return new Response("Unsupported file type", {status: 400});
      }

      // 驗證檔案大小
      const sizeLimit = FILE_SIZE_LIMITS[fileType];
      if (file.size > sizeLimit) {
        const limitMB = sizeLimit / (1024 * 1024);
        return new Response(`File too large. ${fileType} limit: ${limitMB}MB`, {status: 413});
      }

      const id = crypto.randomUUID().replace(/-/g, '').slice(0, 10);
      const filename = `${id}.${ext}`;

      const date = new Date().toISOString().slice(0, 10);
      const key = `uploads/${date}/${filename}`;

      let detectedContentType = file.type; // 從瀏覽器獲取原始的 Content-Type

      // 如果瀏覽器提供的類型是通用類型或缺失，則嘗試從副檔名推斷
      if (!detectedContentType || detectedContentType === 'application/octet-stream') {
        detectedContentType = mimeTypeMap[ext.toLowerCase()] || 'application/octet-stream';
      }

      await env.IMAGES.put(key, await file.arrayBuffer(), {
        httpMetadata: {
          contentType: detectedContentType
        },
        customMetadata: {
          uploadAt: Date.now().toString()
        }
      });

      return Response.json({
        url: `https://img.kodakjerec.workers.dev/${filename}`
      });
    }

    // 讀取圖片或影片
    const filename = url.pathname.slice(1);
    if (!filename) return new Response("Not found", { status:404 });

    const objects = await env.IMAGES.list({
      prefix: "uploads"
    });

    const object = objects.objects.find(o => 
      o.key.endsWith(`${filename}`)
    );

    if (!object) return new Response("Not found 2", { status: 404 });

    const img = await env.IMAGES.get(object.key);
    if (!img) return new Response("Not found 3", { status: 404 });

    let responseContentType = img.httpMetadata.contentType;

    // 如果沒有分類, 就依照副檔名推斷 Content-Type
    const ext = filename.split(".").pop();
    responseContentType = mimeTypeMap[ext.toLowerCase()] || 'image/png';

    console.log(responseContentType)

    return new Response(img.body, {
      headers: {
        "Content-Type": responseContentType,
        "Cache-Control": "public, max-age=86400"
      }
    })
  },

  // 排程(每天刪圖片)
  async scheduled(event, env, ctx) {
    const sevenDaysAgo = Date.now() - 7*24*60*60*1000;
    const list = await env.IMAGES.list({ prefix: "uploads/"});

    for (const obj of list.objects) {
      const uploadedAt = Number(obj.customMetadata?.uploadedAt);
      
      if (uploadedAt < sevenDaysAgo) {
        await env.IMAGES.delete(obj.key);
      }
    }
  }
};