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

const corsHeaders = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type",
};

export default {
    async fetch(request, env, ctx) {
        // 處理瀏覽器的預檢請求 (Preflight)
        if (request.method === "OPTIONS") {
            return new Response(null, { headers: corsHeaders });
        }

        try {
            // 呼叫原本的邏輯
            const response = await this.handleRequest(request, env, ctx);

            // 將 CORS headers 強制塞進所有回傳的 Response 中
            const newResponse = new Response(response.body, response);
            for (const [key, value] of Object.entries(corsHeaders)) {
                newResponse.headers.set(key, value);
            }
            return newResponse;
        } catch (error) {
            return new Response(error.message, { status: 500, headers: corsHeaders });
        }
    },

    // 原本的 fetch 邏輯移到這裡
    async handleRequest(request, env, ctx) {
        const url = new URL(request.url);

        // 測試排程
        if (url.pathname === "/cleanup") {
            await this.scheduled(null, env, ctx);
            return new Response("Cleanup triggered");
        }

        // 從 D1 SELECT 最新圖片並支援分頁 (limit/offset)。
        if (url.pathname === "/list" && request.method === "GET") {
            // 為了安全與效能，限制單次最多拿取筆數 (預設 20 筆，最多 50 筆)
            let limit = parseInt(url.searchParams.get("limit")) || 20;
            if (limit > 50) limit = 50;

            const offset = parseInt(url.searchParams.get("offset")) || 0;

            try {
                // 從 D1 查詢最新上傳的圖片
                const { results } = await env.imgs.prepare(
                    "SELECT id, file_key, created_at FROM images ORDER BY created_at DESC LIMIT ?1 OFFSET ?2"
                ).bind(limit, offset).all();

                // 將資料庫裡的 file_key 轉換成前端可以直接讀取的圖片網址
                const images = results.map(row => {
                    // row.file_key 格式為 "uploads/2026-10-08/123456.jpg"
                    // 但你的抓圖路由是 /123456.jpg，所以要取最後面的檔名
                    const filename = row.file_key.split('/').pop();
                    return {
                        id: row.id,
                        url: `https://img.kodakjerec.work/${filename}`,
                        created_at: row.created_at
                    };
                });

                return Response.json({
                    images: images,
                    next_offset: offset + images.length, // 提供前端下一次要呼叫的 offset
                    has_more: images.length === limit    // 判斷是否還有下一頁
                });
            } catch (error) {
                return Response.json({ error: error.message }, { status: 500 });
            }
        }

        // 上傳圖片或影片
        if (url.pathname === "/upload" && request.method === "POST") {
            const form = await request.formData();
            const file = form.get("image");

            if (!(file instanceof File)) {
                return new Response("No file", { status: 400 });
            }

            const ext = file.name.split(".").pop();
            const fileType = getFileType(ext);

            // 驗證檔案類型
            if (!fileType) {
                return new Response("Unsupported file type", { status: 400 });
            }

            // 驗證檔案大小
            const sizeLimit = FILE_SIZE_LIMITS[fileType];
            if (file.size > sizeLimit) {
                const limitMB = sizeLimit / (1024 * 1024);
                return new Response(`File too large. ${fileType} limit: ${limitMB}MB`, { status: 413 });
            }

            const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_';
            const bytes = new Uint8Array(6);
            crypto.getRandomValues(bytes);
            const id = Array.from(bytes, b => charset[b % 64]).join('');
            const filename = `${id}.${ext}`;

            const date = new Date().toISOString().slice(0, 10);
            const key = `uploads/${date}/${filename}`;

            let detectedContentType = file.type; // 從瀏覽器獲取原始的 Content-Type

            // 如果瀏覽器提供的類型是通用類型或缺失，則嘗試從副檔名推斷
            if (!detectedContentType || detectedContentType === 'application/octet-stream') {
                detectedContentType = mimeTypeMap[ext.toLowerCase()] || 'application/octet-stream';
            }

            // 將檔案資訊寫入 D1 資料庫
            const createdAt = Date.now();
            await env.imgs.prepare(
                "INSERT INTO images (id, file_key, created_at) VALUES (?1, ?2, ?3)"
            ).bind(id, key, createdAt).run();

            // 將檔案上傳到 R2
            await env.IMAGES.put(key, await file.arrayBuffer(), {
                httpMetadata: {
                    contentType: detectedContentType
                },
                customMetadata: {
                    uploadAt: Date.now().toString()
                }
            });

            return Response.json({
                url: `https://img.kodakjerec.work/${filename}`
            });
        }

        // 讀取圖片或影片
        const filename = url.pathname.slice(1);
        if (!filename) return new Response("Not found", { status: 404 });

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
        const sevenDaysAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);

        // 刪除 D1 中超過七天的紀錄 (注意：D1 存的是 timestamp 數字，所以要轉型)
        await env.imgs.prepare("DELETE FROM images WHERE created_at < ?1").bind(sevenDaysAgo.getTime()).run();

        let truncated = true;
        let cursor = undefined;

        while (truncated) {
            const list = await env.IMAGES.list({
                prefix: "uploads/",
                cursor: cursor
            });

            for (const obj of list.objects) {
                // 直接使用 R2 物件內建的 uploaded (Date 物件) 屬性來判斷
                if (obj.uploaded < sevenDaysAgo) {
                    await env.IMAGES.delete(obj.key);
                }
            }

            truncated = list.truncated;
            cursor = list.cursor;
        }
    }
};