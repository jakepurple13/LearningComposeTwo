package anime

import getJsonApi
import org.jsoup.nodes.Document
import java.net.URI

object GogoAnimeApi : ShowApi(
    baseUrl = "https://www.gogoanime1.com",
    allPath = "home/anime-list",
    recentPath = "home/latest-episodes"
) {

    override fun getItems(page: Int) = getRecent(page)

    override fun getRecent(doc: Document): List<ShowInfo> = try {
        doc.allElements.select("div.dl-item").map {
            val tempUrl = it.select("div.name").select("a[href^=http]").attr("abs:href")
            ShowInfo(it.select("div.name").text(), tempUrl.substring(0, tempUrl.indexOf("/episode")), Sources.GOGOANIME)
        }
    } catch (e: Exception) {
        emptyList()
    }

    override fun getList(doc: Document): List<ShowInfo> = try {
        doc.allElements.select("ul.arrow-list").select("li")
            .map { ShowInfo(it.text(), it.select("a[href^=http]").attr("abs:href"), Sources.GOGOANIME) }
    } catch (e: Exception) {
        emptyList()
    }

    override fun getVideoLink(info: EpisodeInfo): List<Storage> = try {
        val storage = Storage(
            link = info.url.toJsoup().select("a[download^=http]").attr("abs:download"),
            source = info.url,
            quality = "Good",
            sub = "Yes"
        )
        val regex = "^[^\\[]+(.*mp4)".toRegex().toPattern().matcher(storage.link!!)
        storage.filename = if (regex.find()) regex.group(1)!! else "${URI(info.url).path.split("/")[2]} ${info.name}.mp4"
        listOf(storage)
    } catch (e: Exception) {
        emptyList()
    }

    override fun getEpisodeInfo(source: ShowInfo, doc: Document) = try {
        val name = doc.select("div.anime-title").text()
        Episode(
            source = source,
            name = name,
            description = doc.select("p.anime-details").text(),
            image = doc.select("div.animeDetail-image").select("img[src^=http]")?.attr("abs:src"),
            genres = doc.select("div.animeDetail-item:contains(Genres)").select("a[href^=http]").eachText(),
            episodes = doc.select("ul.check-list").select("li").map {
                val urlInfo = it.select("a[href^=http]")
                val epName = urlInfo.text().let { info -> if (info.contains(name)) info.substring(name.length) else info }.trim()
                EpisodeInfo(epName, urlInfo.attr("abs:href"), source.sources)
            }.distinctBy(EpisodeInfo::name)
        )
    } catch (e: Exception) {
        null
    }

    override fun searchList(text: CharSequence, list: List<ShowInfo>): List<ShowInfo> {
        return try {
            if (text.isNotEmpty()) getJsonApi<Base>("https://www.gogoanime1.com/search/topSearch?q=$text")
                ?.data
                .orEmpty()
                .map { ShowInfo(it.name.orEmpty(), "https://www.gogoanime1.com/watch/${it.seo_name}", Sources.GOGOANIME) }
            else null
        } catch (e: Exception) {
            null
        } ?: super.searchList(text, list)
    }

    private data class Base(val status: Number?, val data: List<DataShowData>?)

    private data class DataShowData(
        val rel: Number?,
        val anime_id: Number?,
        val name: String?,
        val has_image: Number?,
        val seo_name: String?,
        val score_count: Number?,
        val score: Number?,
        val aired: Number?,
        val episodes: List<Episodes>?
    )

    private data class Episodes(val episode_id: Number?, val episode_seo_name: String?, val episode_name: String?)

}