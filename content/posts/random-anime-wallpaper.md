---
title: 随机动漫壁纸 API
date: 2019-02-14 10:02:36
tags:
  - "Python"
cover:
  image: https://i.loli.net/2019/02/14/5c64d129c19ae.jpg

---

早就看首页单调的背景不爽了，这次趁着迁移网站的机会顺便造了一个随机获取和显示动漫壁纸的轮子。

实现思路：从 [/r/Animewallpaper](https://www.reddit.com/r/Animewallpaper/) 上爬墙带 `Desktop` flair 的帖子。 [来源](https://www.reddit.com/r/anime/comments/5ec91v/any_api_for_anime_wallpapers/dad0kt9/)

(效果图的壁纸来自 [pixiv id 66568021 by 千夜QYS3](https://www.pixiv.net/member_illust.php?mode=medium&illust_id=66568021))

<!--more-->

实现细节：
1. 从 https://www.reddit.com/r/Animewallpaper/search.rss?q=flair_name%3A%22Desktop%22&restrict_sr=1 获取 RSS feed，解析获取前 25 条结果。该结果缓存 10 分钟。
```python
def entry_to_dictionary(entry):
    return {
        "source": entry.find("{http://www.w3.org/2005/Atom}link").attrib["href"],
        "title":entry.find("{http://www.w3.org/2005/Atom}title").text,
        "img": RE_EXTRACT_IMG.search(entry.find("{http://www.w3.org/2005/Atom}content").text).groups(0)[0] 
    }

def fetch_feed():
    global last_fetch_time
    global cached_results
    
    if time.time() - last_fetch_time <= CACHE_VALID_TIME and cached_results:
        return cached_results
    
    results = []
    e = xml.etree.ElementTree.fromstring(requests.get(FEED_URL, headers={"User-Agent":USER_AGENT}).text.strip())
    for entry in e.findall("{http://www.w3.org/2005/Atom}entry"):
        try:
            results.append(entry_to_dictionary(entry))
        except:
            pass
            
    cached_results = results
    last_fetch_time = time.time()
    
    return results
```

2. 前端通过ajax从`/random_anime_wallpaper`获取一份随机壁纸。

3. 为了优化国内用户的加载速度，考虑在服务端获取壁纸后直接发给用户（相当于一个反代）。
   1. 一开始打算用 base64 编码壁纸然后丢在 json 里返回。然而这么实现的话客户端要下载完整张图片才能渲染显示。除此以外，由于使用了 gunicorn，每个 worker 的缓存都是独立的，也非常浪费内存。
   2. 故打算在服务端下载到硬盘，并直接返回静态路径，这样只有在第一次调用 API 的时候会稍有延迟。实现如下：
   ```python
   def download_img(src):
    ext = src.split(".")[-1]
    name = "static/" + hashlib.sha1(src.encode()).hexdigest() + "." + ext
    if os.path.isfile(name):
        return name
    print("Downloading ", src, " to ", name)
    with open(name, 'wb') as f:
        f.write(requests.get(src).content)
    return name

   @app.route("/random_anime_wallpaper")
    def random_anime_wallpaper():
        res = fetch_feed()
        selected = copy.deepcopy(res[random.randint(0, len(res))])
        if request.args.get('download', False):
            selected["img_src"] = selected["img"]
            selected["img"] = URL_BASE_PATH + download_img(selected["img"])
        return jsonify(selected)
    ```
4. 最后直接使用 nginx 反代请求到 python 上就可以了：
   ```
    location /api/wallpapers/ {
        proxy_pass http://127.0.0.1:8001/;
    }
   ```

从思路到实现都非常简单~

> [Demo](https://harrynull.tech) | [API]( https://harrynull.tech/api/wallpapers/random_anime_wallpaper) | [Source]( https://gist.github.com/harrynull/0194a5c1119a9c1a3020f3a551559262)