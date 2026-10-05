# URL Metadata Extractor AI Specification

## Objective

Given any user-supplied URL, extract the best available:

- title
- description
- thumbnailImage
- contentType

The implementation should maximize compatibility across arbitrary websites and provide behavior similar to:

- LINE Link Preview
- Discord Link Preview
- Telegram Preview
- Notion Bookmark Preview

---

## Overall Flow

```text
1. Query Local Cache
2. Query Cloudflare Cache
3. HEAD Content-Type Detection
4. GET Partial HTML Download
5. OpenGraph Extraction
6. Twitter Card Extraction
7. JSON-LD Extraction
8. Site-Specific Parsers
9. Normalize URLs
10. Store Cache
```

---

## Stage 1: Local Cache

Input:

```text
url
```

If URL exists locally:

```text
return cached metadata
stop processing
```

---

## Stage 2: Cloudflare Cache

Request:

```json
{
  "url": "https://example.com"
}
```

Expected Response:

```json
{
  "title": "",
  "description": "",
  "imageUrl": "",
  "contentType": ""
}
```

If any metadata exists:

```text
title OR description OR imageUrl
```

Use cache result.

---

## Metadata Validity Rule

DO NOT require both title and description.

Use:

```text
hasMetadata =
    title is not empty
 OR description is not empty
 OR imageUrl is not empty
```

Only when ALL fields are empty should extraction continue.

---

## Stage 3: Detect Content Type

First attempt:

```http
HEAD
```

Headers:

```text
User-Agent
Accept: */*
Accept-Encoding: gzip, deflate, br
```

Supported Media Types:

```text
image/*
video/*
audio/*
```

If detected:

```text
mark as media
```

---

## Media Fallback Detection

If Content-Type unavailable:

Check URL extension.

Supported:

```text
.jpg
.jpeg
.png
.gif
.webp
.bmp
.avif
```

If matched:

```text
mark as media
```

---

## Stage 4: Partial GET

When page metadata must be extracted:

```http
GET
```

Headers:

```text
User-Agent
Accept: */*
Accept-Encoding: gzip, deflate, br
Range: bytes=0-262144
```

Download Limit:

```text
256 KB
```

Avoid full-page downloads.

---

## HTML Detection

Treat as HTML when:

```text
contentType contains html
OR
contentType contains xhtml
```

Parse DOM.

---

## Metadata Extraction Priority

### Title

Priority order:

```html
<title>
```

then:

```html
<meta property="og:title">
```

then:

```html
<meta name="twitter:title">
```

---

### Description

Priority order:

```html
<meta name="description">
```

then:

```html
<meta property="og:description">
```

then:

```html
<meta name="twitter:description">
```

---

### Thumbnail Image

Priority order:

```html
<meta property="og:image">
```

then:

```html
<meta property="og:image:url">
```

then:

```html
<meta name="twitter:image">
```

then:

```html
<meta property="og:images">
```

then:

```html
<img id="landingImage">
```

then:

```html
<link rel="icon">
```

---

## Video Detection

Read:

```html
<meta property="og:type">
```

If value begins with:

```text
video
```

mark media preview.

Comparison must be case-insensitive.

---

## JSON-LD Support

Parse:

```html
<script type="application/ld+json">
```

Common fields:

```json
{
  "headline": "title",
  "description": "description",
  "image": "thumbnail"
}
```

Use when OpenGraph or Twitter Card is missing.

---

## URL Normalization

### Protocol Relative URLs

Input:

```text
//cdn.example.com/image.jpg
```

Output:

```text
https://cdn.example.com/image.jpg
```

---

### Relative URLs

Input:

```text
/images/a.jpg
```

Output:

```text
https://example.com/images/a.jpg
```

Use absolute URL conversion.

---

## Site-Specific Rules

### PTT

Cookies:

```text
over18=1
```

---

### Amazon

Prefer desktop user agent.

---

### YouTube

Prefer desktop user agent.

---

### Bilibili

Parse:

```javascript
window.__INITIAL_STATE__
```

Extract:

```text
description
thumbnail
```

Allow Bilibili data to override incomplete OpenGraph values.

---

## Cache Rules

Never cache completely empty results.

Required:

```text
title
OR
description
OR
imageUrl
```

must exist before:

```text
save local cache
upload cloudflare cache
```

---

## Failure Handling

Any extraction failure:

```text
return original url
avoid crash
log exception
```

Do not stop processing because of:

```text
HEAD failure
OpenGraph missing
Twitter Card missing
JSON-LD parse failure
```

Continue with remaining strategies.

---

## Success Criteria

Output structure:

```json
{
  "url": "",
  "contentType": "",
  "title": "",
  "description": "",
  "thumbnailImage": "",
  "isMedia": false
}
```

System goals:

```text
Highest metadata extraction success rate.
Graceful fallback chain.
Low bandwidth usage.
No application crash.
Compatible with arbitrary URLs.
```
